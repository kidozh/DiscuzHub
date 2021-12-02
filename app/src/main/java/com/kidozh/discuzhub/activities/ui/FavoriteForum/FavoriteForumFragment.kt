package com.kidozh.discuzhub.activities.ui.FavoriteForum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.FavoriteForumAdapter
import com.kidozh.discuzhub.database.FavoriteForumDatabase
import com.kidozh.discuzhub.databinding.FragmentFavoriteThreadBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.FavoriteForum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.FavoriteForumResult
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.syncFavorite
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class FavoriteForumFragment : Fragment() {
    private lateinit var mViewModel: FavoriteForumViewModel
    private var bbsInfo: Discuz? = null
    private var userBriefInfo: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ARG_BBS) as Discuz?
            userBriefInfo = requireArguments().getSerializable(ARG_USER) as User?
        }
    }

    var adapter: FavoriteForumAdapter? = null
    var binding: FragmentFavoriteThreadBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteThreadBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this)[FavoriteForumViewModel::class.java]
        mViewModel.setInfo(bbsInfo!!, userBriefInfo)
        configureRecyclerview()
        bindViewModel()
        configureSwipeRefreshLayout()
        syncFavoriteThreadFromServer()
    }

    private fun configureRecyclerview() {
        binding!!.favoriteThreadRecyclerview.itemAnimator = getRecyclerviewAnimation(
            requireContext()
        )
        binding!!.favoriteThreadRecyclerview.layoutManager = LinearLayoutManager(context)
        adapter = FavoriteForumAdapter()
        adapter!!.setInformation(bbsInfo!!, userBriefInfo)
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.flow.collectLatest {
                adapter!!.submitData(it)

            }
        }
        binding!!.favoriteThreadRecyclerview.adapter = getAnimatedAdapter(
            requireContext(), adapter!!
        )
        binding!!.favoriteThreadRecyclerview.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun configureSwipeRefreshLayout() {
        if (userBriefInfo != null) {
            binding!!.favoriteThreadSwipelayout.setOnRefreshListener {
                binding!!.favoriteThreadSyncProgressbar.visibility = View.GONE
                Toasty.info(
                    requireContext(),
                    getString(R.string.sync_favorite_forum_start, bbsInfo!!.site_name),
                    Toast.LENGTH_SHORT
                ).show()
                mViewModel.startSyncFavoriteForum()
                binding!!.favoriteThreadSwipelayout.isRefreshing = false
            }
        } else {
            binding!!.favoriteThreadSwipelayout.isEnabled = false
        }
    }

    private fun bindViewModel() {

        mViewModel.favoriteForumCount.observe(viewLifecycleOwner) { cnt ->

            if (cnt == 0) {
                binding!!.blankFavoriteThreadView.visibility = View.VISIBLE
                binding!!.blankFavoriteThreadNotice.setText(R.string.favorite_forum_not_found)
            } else {
                binding!!.blankFavoriteThreadView.visibility = View.GONE
            }
        }
        mViewModel.errorMessageMutableLiveData.observe(
            viewLifecycleOwner,
            { errorMessage: ErrorMessage? ->
                if (errorMessage != null) {
                    Toasty.error(
                        requireContext(), getString(
                            R.string.discuz_api_message_template,
                            errorMessage.key, errorMessage.content
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
            })
        mViewModel.resultMutableLiveData.observe(
            viewLifecycleOwner,
            { favoriteForumResult: FavoriteForumResult? ->
                if (context is BaseStatusInteract) {
                    (context as BaseStatusInteract?)!!.setBaseResult(
                        favoriteForumResult,
                        favoriteForumResult?.favoriteForumVariable
                    )
                }
            })
    }

    private fun syncFavoriteThreadFromServer() {
        if (context != null && syncFavorite(requireContext()) && userBriefInfo != null) {
            bindSyncStatus()
            mViewModel.startSyncFavoriteForum()
        }
    }

    private fun bindSyncStatus() {
        mViewModel.totalCount.observe(viewLifecycleOwner, { count: Int ->
            if (count == -1) {
                binding!!.favoriteThreadSyncProgressbar.visibility = View.VISIBLE
                binding!!.favoriteThreadSyncProgressbar.isIndeterminate = true
            } else {
                binding!!.favoriteThreadSyncProgressbar.visibility = View.GONE
            }
        })

        mViewModel.favoriteForumInServer.observe(viewLifecycleOwner) { favoriteForums ->
            val count = mViewModel.totalCount.value!!
            if (count == -1) {
                return@observe
            } else if (favoriteForums != null) {
                if (count > favoriteForums.size) {
                    binding!!.favoriteThreadSyncProgressbar.visibility = View.VISIBLE
                    binding!!.favoriteThreadSyncProgressbar.max = count
                    binding!!.favoriteThreadSyncProgressbar.progress = favoriteForums.size
                } else {
                    binding!!.favoriteThreadSyncProgressbar.visibility = View.GONE
                    //Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
                }
            }
        }
        mViewModel.newFavoriteForum.observe(
            viewLifecycleOwner,
            { newFavoriteForums: List<FavoriteForum>? ->
                if (newFavoriteForums!=null){
                    saveFavoriteItem(newFavoriteForums)
                }


            })
    }

    private fun saveFavoriteItem(favoriteForumList: List<FavoriteForum>){
        val dao = FavoriteForumDatabase.getInstance(context).dao
        // query first
        val insertTids: MutableList<Int> = ArrayList()
        for (i in favoriteForumList.indices) {
            insertTids.add(favoriteForumList[i].idKey)
            favoriteForumList[i].belongedBBSId = bbsInfo!!.id
            favoriteForumList[i].userId =
                if (userBriefInfo != null) userBriefInfo!!.uid else 0
            //Log.d(TAG,"fav id "+favoriteForumList.get(i).favid);
        }
        Thread{
            val queryList = dao.queryFavoriteItemListByfids(
                bbsInfo!!.id, if (userBriefInfo != null) userBriefInfo!!.uid else 0,
                insertTids
            )
            if (queryList != null) {
                for (i in queryList.indices) {
                    val tid = queryList[i]?.idKey
                    val queryForum = queryList[i]
                    for (j in favoriteForumList.indices) {
                        val favoriteForum = favoriteForumList[j]
                        if (favoriteForum.idKey == tid) {
                            if (queryForum != null) {
                                favoriteForum.id = queryForum.id
                            }
                            break
                        }
                    }
                }
            }
            // remove all synced information

            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);
            dao.insert(favoriteForumList)
        }.start()


        return
    }

//    private inner class SaveFavoriteItemAsyncTask(private val favoriteForumList: List<FavoriteForum>) :
//        AsyncTask<Void?, Void?, Int>() {
//        protected override fun doInBackground(vararg voids: Void): Int {
//            val dao = FavoriteForumDatabase.getInstance(context).dao
//            // query first
//            val insertTids: MutableList<Int> = ArrayList()
//            for (i in favoriteForumList.indices) {
//                insertTids.add(favoriteForumList[i].idKey)
//                favoriteForumList[i].belongedBBSId = bbsInfo!!.id
//                favoriteForumList[i].userId =
//                    if (userBriefInfo != null) userBriefInfo!!.getUid() else 0
//                //Log.d(TAG,"fav id "+favoriteForumList.get(i).favid);
//            }
//            val queryList = dao.queryFavoriteItemListByfids(
//                bbsInfo!!.id, if (userBriefInfo != null) userBriefInfo!!.getUid() else 0,
//                insertTids
//            )
//            for (i in queryList.indices) {
//                val tid = queryList[i].idKey
//                val queryForum = queryList[i]
//                for (j in favoriteForumList.indices) {
//                    val favoriteForum = favoriteForumList[j]
//                    if (favoriteForum.idKey == tid) {
//                        favoriteForum.id = queryForum.id
//                        break
//                    }
//                }
//            }
//            // remove all synced information
//
//            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);
//            dao.insert(favoriteForumList)
//            return favoriteForumList.size
//        }
//
//        override fun onPostExecute(integer: Int) {
//            super.onPostExecute(integer)
//        }
//    }

    companion object {
        private val TAG = FavoriteForumFragment::class.java.simpleName
        private const val ARG_BBS = "ARG_BBS"
        private const val ARG_USER = "ARG_USER"
        private const val ARG_IDTYPE = "ARG_IDTYPE"
        @JvmStatic
        fun newInstance(bbsInfo: Discuz?, userBriefInfo: User?): FavoriteForumFragment {
            val fragment = FavoriteForumFragment()
            val args = Bundle()
            args.putSerializable(ARG_BBS, bbsInfo)
            args.putSerializable(ARG_USER, userBriefInfo)
            fragment.arguments = args
            return fragment
        }
    }
}