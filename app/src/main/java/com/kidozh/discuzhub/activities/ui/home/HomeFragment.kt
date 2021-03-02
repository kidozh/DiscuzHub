package com.kidozh.discuzhub.activities.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.FavoriteForum.FavoriteForumViewModel
import com.kidozh.discuzhub.adapter.FavoriteForumAdapter
import com.kidozh.discuzhub.adapter.ForumCategoryAdapter
import com.kidozh.discuzhub.databinding.ActivityBbsForumIndexBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.DiscuzIndexResult
import com.kidozh.discuzhub.utilities.AnimationUtils.configureRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var favoriteForumViewModel: FavoriteForumViewModel
    lateinit var adapter: ForumCategoryAdapter
    private var favoriteForumAdapter = FavoriteForumAdapter()
    private lateinit var concatAdapter: ConcatAdapter
    lateinit var bbsInfo: Discuz
    var userBriefInfo: User? = null
    private lateinit var activityBbsForumIndexBinding: ActivityBbsForumIndexBinding
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        favoriteForumViewModel = ViewModelProvider(this).get(FavoriteForumViewModel::class.java)
        activityBbsForumIndexBinding = ActivityBbsForumIndexBinding.inflate(layoutInflater)
        val root = activityBbsForumIndexBinding.root
        configureViewModel()
        configureAdapter()
        configurePortalRecyclerview()
        bindLiveDataFromViewModel()
        //getPortalCategoryInfo();
        configureRefreshBtn()
        configureSwipeRefreshLayout()
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
            userBriefInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
        }
    }


    private fun configureViewModel(){
        homeViewModel.setBBSInfo(bbsInfo, userBriefInfo)
        favoriteForumViewModel.setInfo(bbsInfo, userBriefInfo)
    }

    private fun configureAdapter(){
        favoriteForumAdapter.setInformation(bbsInfo, userBriefInfo)
        favoriteForumViewModel.favoriteItemListData.observe(viewLifecycleOwner, {
             favoriteForumAdapter.submitList(it)
        })
    }
    

    private fun configureSwipeRefreshLayout() {
        activityBbsForumIndexBinding.swipeRefreshLayout.setOnRefreshListener { homeViewModel.loadForumCategoryInfo() }
    }

    private fun bindLiveDataFromViewModel() {
        homeViewModel.forumCategoryInfo.observe(viewLifecycleOwner, { forumCategories ->
            if (homeViewModel.bbsIndexResultMutableLiveData.value != null &&
                    homeViewModel.bbsIndexResultMutableLiveData.value!!.forumVariables != null) {
                val allForum = homeViewModel.bbsIndexResultMutableLiveData.value!!.forumVariables.forumList
                adapter.setForumCategoryList(forumCategories!!, allForum)
            }
        })
        homeViewModel.errorMessageMutableLiveData.observe(viewLifecycleOwner, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                activityBbsForumIndexBinding.errorView.visibility = View.VISIBLE
                activityBbsForumIndexBinding.errorIcon.setImageResource(R.drawable.ic_error_outline_24px)
                activityBbsForumIndexBinding.errorValue.text = errorMessage.key
                activityBbsForumIndexBinding.errorContent.text = errorMessage.content
            } else {
                activityBbsForumIndexBinding.errorView.visibility = View.GONE
                adapter.setForumCategoryList(ArrayList(), ArrayList())
            }
        })
        
        homeViewModel.isLoading.observe(viewLifecycleOwner, { aBoolean ->
            activityBbsForumIndexBinding.swipeRefreshLayout.isRefreshing = aBoolean
        })
        homeViewModel.bbsIndexResultMutableLiveData.observe(viewLifecycleOwner, { bbsIndexResult: DiscuzIndexResult? ->
            if (context is BaseStatusInteract) {
                if (bbsIndexResult?.forumVariables != null) {
                    (context as BaseStatusInteract?)!!.setBaseResult(bbsIndexResult, bbsIndexResult.forumVariables)
                }
            }
        })

    }

    private fun configurePortalRecyclerview() {
        activityBbsForumIndexBinding.bbsPortalRecyclerview.itemAnimator = getRecyclerviewAnimation(requireContext())
        activityBbsForumIndexBinding.bbsPortalRecyclerview.setHasFixedSize(true)
        activityBbsForumIndexBinding.bbsPortalRecyclerview.layoutManager = LinearLayoutManager(context)
        configureRecyclerviewAnimation(requireContext(), activityBbsForumIndexBinding.bbsPortalRecyclerview)
        adapter = ForumCategoryAdapter(bbsInfo, userBriefInfo)
        concatAdapter = ConcatAdapter(favoriteForumAdapter,adapter)
        activityBbsForumIndexBinding.bbsPortalRecyclerview.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        activityBbsForumIndexBinding.bbsPortalRecyclerview.adapter = getAnimatedAdapter(requireContext(), concatAdapter)
    }

    private fun configureRefreshBtn() {
        activityBbsForumIndexBinding.bbsPortalRefreshPage.setOnClickListener { homeViewModel.loadForumCategoryInfo() }
    }

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        fun newInstance(Discuz: Discuz?, userBriefInfo: User?): HomeFragment {
            val homeFragment = HomeFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            homeFragment.arguments = args
            return homeFragment
        }
    }
}