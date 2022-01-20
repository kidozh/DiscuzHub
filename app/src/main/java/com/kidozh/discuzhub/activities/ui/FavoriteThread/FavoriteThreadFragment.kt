package com.kidozh.discuzhub.activities.ui.FavoriteThread


import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.FavoriteThread.FavoriteThreadFragment
import com.kidozh.discuzhub.adapter.FavoriteThreadAdapter
import com.kidozh.discuzhub.database.FavoriteThreadDatabase
import com.kidozh.discuzhub.databinding.FragmentFavoriteThreadBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.FavoriteForum
import com.kidozh.discuzhub.entities.FavoriteThread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.FavoriteThreadResult
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.syncFavorite
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class FavoriteThreadFragment : Fragment() {
    private var mViewModel: FavoriteThreadViewModel? = null
    private var bbsInfo: Discuz? = null
    private var userBriefInfo: User? = null
    private var idType: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ARG_BBS) as Discuz?
            userBriefInfo = requireArguments().getSerializable(ARG_USER) as User?
            idType = requireArguments().getString(ARG_IDTYPE, "tid") as String
        }
    }

    lateinit var adapter: FavoriteThreadAdapter
    lateinit var binding: FragmentFavoriteThreadBinding
    private var favoritePagingConfig : PagingConfig = PagingConfig(pageSize = 5)
    lateinit var flow : Flow<PagingData<FavoriteForum>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteThreadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(FavoriteThreadViewModel::class.java)
        mViewModel!!.setInfo(bbsInfo!!, userBriefInfo, idType)
        configureRecyclerview()
        bindViewModel()
        configureSwipeRefreshLayout()
        syncFavoriteThreadFromServer()
    }

    private fun configureRecyclerview() {
        binding.favoriteThreadRecyclerview.itemAnimator = getRecyclerviewAnimation(
            requireContext()
        )
        binding.favoriteThreadRecyclerview.layoutManager = LinearLayoutManager(context)
        adapter = FavoriteThreadAdapter()
        adapter.setInformation(bbsInfo, userBriefInfo)

        binding.favoriteThreadRecyclerview.adapter = getAnimatedAdapter(
            requireContext(), adapter
        )
        //binding.favoriteThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private fun configureSwipeRefreshLayout() {
        if (userBriefInfo != null) {
            binding.favoriteThreadSwipelayout.setOnRefreshListener {
                binding.favoriteThreadSyncProgressbar.visibility = View.GONE
                Toasty.info(
                    requireContext(),
                    getString(R.string.sync_favorite_thread_start, bbsInfo!!.site_name),
                    Toast.LENGTH_SHORT
                ).show()
                mViewModel!!.startSyncFavoriteThread()
                binding.favoriteThreadSwipelayout.isRefreshing = false
            }
        } else {
            binding.favoriteThreadSwipelayout.isEnabled = false
        }
    }

    private fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel!!.flow.collectLatest {
                adapter.submitData(it)

                if(adapter.snapshot().items.isNullOrEmpty()){
                    binding.blankFavoriteThreadView.visibility = View.VISIBLE
                }
                else{
                    binding.blankFavoriteThreadView.visibility = View.GONE
                }
            }
        }

        mViewModel!!.errorMsgContent.observe(viewLifecycleOwner, { error: String? ->
            if (!TextUtils.isEmpty(error)) {
                Toasty.error(requireContext(), error!!, Toast.LENGTH_SHORT).show()
            }
        })
        mViewModel!!.resultMutableLiveData.observe(
            viewLifecycleOwner,
            { favoriteThreadResult: FavoriteThreadResult? ->
                if (context is BaseStatusInteract) {
                    if (favoriteThreadResult != null) {
                        (context as BaseStatusInteract).setBaseResult(
                            favoriteThreadResult,
                            favoriteThreadResult.favoriteThreadVariable
                        )
                    }
                }
            })
    }

    fun syncFavoriteThreadFromServer() {
        if (context != null && syncFavorite(requireContext()) && userBriefInfo != null) {
            val page = 1
            // sync information
            // Toasty.info(getContext(),getString(R.string.sync_favorite_thread_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();

            // loop to fetch favorite thread from server
            bindSyncStatus()
            mViewModel!!.startSyncFavoriteThread()
        }
    }

    private fun bindSyncStatus() {
        mViewModel!!.totalCount.observe(viewLifecycleOwner, { count: Int ->
            if (count == -1) {
                binding.favoriteThreadSyncProgressbar.visibility = View.VISIBLE
                binding.favoriteThreadSyncProgressbar.isIndeterminate = true
            }
        })
        mViewModel!!.favoriteThreadInServer.observe(
            viewLifecycleOwner,
            { favoriteThreads: List<FavoriteThread> ->
                if (mViewModel != null) {
                    val count = mViewModel!!.totalCount.value!!
                    if (count == -1) {

                    } else if (count > favoriteThreads.size) {
                        binding.favoriteThreadSyncProgressbar.visibility = View.VISIBLE
                        binding.favoriteThreadSyncProgressbar.max = count
                        binding.favoriteThreadSyncProgressbar.progress = favoriteThreads.size
                    } else {
                        binding.favoriteThreadSyncProgressbar.visibility = View.GONE
                        //Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
                    }
                } else {
                    binding.favoriteThreadSyncProgressbar.visibility = View.GONE
                }
            })
        mViewModel!!.newFavoriteThread.observe(
            viewLifecycleOwner,
            { newFavoriteThreads: List<FavoriteThread> ->
                saveFavoriteItem(newFavoriteThreads)
            })
    }

    fun saveFavoriteItem(favoriteThreadList: List<FavoriteThread>){
        Thread{
            val dao = FavoriteThreadDatabase.getInstance(context).dao
            // query first
            val insertTids: MutableList<Int> = ArrayList()
            for (i in favoriteThreadList.indices) {
                insertTids.add(favoriteThreadList[i].idKey)
                favoriteThreadList[i].belongedBBSId = bbsInfo!!.id
                favoriteThreadList[i].userId = if (userBriefInfo != null) userBriefInfo!!.uid else 0
                //Log.d(TAG,"fav id "+favoriteThreadList.get(i).favid);
            }
            val queryList = dao.queryFavoriteItemListByTids(
                bbsInfo!!.id, if (userBriefInfo != null) userBriefInfo!!.uid else 0,
                insertTids, idType
            )
            if (queryList != null) {
                for (i in queryList.indices) {
                    val tid = queryList[i]?.idKey
                    val queryThread = queryList[i]
                    for (j in favoriteThreadList.indices) {
                        val favoriteThread = favoriteThreadList[j]
                        if (favoriteThread.idKey == tid) {
                            if (queryThread != null) {
                                favoriteThread.id = queryThread.id
                            }
                            break
                        }
                    }
                }
            }
            // remove all synced information
            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0,idType);
            dao.insert(favoriteThreadList)
        }.start()
    }

    companion object {
        private val TAG = FavoriteThreadFragment::class.java.simpleName
        private const val ARG_BBS = "ARG_BBS"
        private const val ARG_USER = "ARG_USER"
        private const val ARG_IDTYPE = "ARG_IDTYPE"
        @JvmStatic
        fun newInstance(
            bbsInfo: Discuz?,
            userBriefInfo: User?,
            idType: String?
        ): FavoriteThreadFragment {
            val fragment = FavoriteThreadFragment()
            val args = Bundle()
            args.putSerializable(ARG_BBS, bbsInfo)
            args.putSerializable(ARG_USER, userBriefInfo)
            args.putString(ARG_IDTYPE, idType)
            fragment.arguments = args
            return fragment
        }
    }
}