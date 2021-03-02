package com.kidozh.discuzhub.activities.ui.HotThreads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardViewModel
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import com.kidozh.discuzhub.adapter.ThreadAdapter
import com.kidozh.discuzhub.databinding.FragmentHotThreadBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.DisplayThreadsResult
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils

class HotThreadsFragment : Fragment() {
    private lateinit var hotThreadsViewModel: HotThreadsViewModel
    private lateinit var dashBoardViewModel: DashBoardViewModel
    lateinit var binding: FragmentHotThreadBinding
    lateinit var forumThreadAdapter: ThreadAdapter
    var networkIndicatorAdapter = NetworkIndicatorAdapter()
    lateinit var concatAdapter: ConcatAdapter
    lateinit var bbsInfo: Discuz
    var userBriefInfo: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
            userBriefInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHotThreadBinding.inflate(inflater, container, false)
        hotThreadsViewModel = ViewModelProvider(this).get(HotThreadsViewModel::class.java)
        dashBoardViewModel = ViewModelProvider(this).get(DashBoardViewModel::class.java)
        configureIntent()
        configureThreadRecyclerview()
        configureSwipeRefreshLayout()
        bindVieModel()
        hotThreadsViewModel.setPageNumAndFetchThread(1)
        return binding.root
    }
    
    fun configureIntent(){
        hotThreadsViewModel.setBBSInfo(bbsInfo, userBriefInfo)
    }


    private fun configureThreadRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding.fragmentHotThreadRecyclerview.layoutManager = linearLayoutManager
        forumThreadAdapter = ThreadAdapter(null, bbsInfo, userBriefInfo)
        forumThreadAdapter.ignoreDigestStyle = true
        concatAdapter = ConcatAdapter(forumThreadAdapter, networkIndicatorAdapter)
        binding.fragmentHotThreadRecyclerview.adapter = getAnimatedAdapter(requireContext(), concatAdapter)
        binding.fragmentHotThreadRecyclerview.itemAnimator = getRecyclerviewAnimation(requireContext())
        // binding.fragmentHotThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        binding.fragmentHotThreadRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (hotThreadsViewModel.pageNum.value == null) {
                        hotThreadsViewModel.setPageNumAndFetchThread(1)
                    } else {
                        hotThreadsViewModel.setPageNumAndFetchThread(hotThreadsViewModel.pageNum.value!! + 1)
                    }
                    // getPageInfo(globalPage);
                }
            }
        })
    }

    private fun configureSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener { hotThreadsViewModel.setPageNumAndFetchThread(1) }
    }
    

    private fun bindVieModel() {
        hotThreadsViewModel.totalThreadListLiveData.observe(viewLifecycleOwner, {
            forumThreadAdapter.updateList(it)

            if (it.isEmpty()) {
                networkIndicatorAdapter.setErrorStatus(ErrorMessage(getString(R.string.empty_result),
                        getString(R.string.empty_hot_threads), R.drawable.ic_empty_hot_thread_64px
                ))
            } else {
                networkIndicatorAdapter.setLoadSuccessfulStatus()
            }

            // point to the next page

            if (hotThreadsViewModel.pageNum.value == 1) {
                binding.fragmentHotThreadRecyclerview.smoothScrollToPosition(0)
            }
        })

        hotThreadsViewModel.isLoading.observe(viewLifecycleOwner, { aBoolean ->
            if (aBoolean) {
                networkIndicatorAdapter.setLoadingStatus()
            } else {
                networkIndicatorAdapter.setLoadSuccessfulStatus()
            }
            binding.swipeRefreshLayout.isRefreshing = aBoolean
        })
        hotThreadsViewModel.errorMessageMutableLiveData.observe(viewLifecycleOwner, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                networkIndicatorAdapter.setErrorStatus(errorMessage)
            }
        })
        hotThreadsViewModel.resultMutableLiveData.observe(viewLifecycleOwner, { displayThreadsResult: DisplayThreadsResult? ->
            if (context is BaseStatusInteract && displayThreadsResult != null) {
                (context as BaseStatusInteract?)!!.setBaseResult(displayThreadsResult,
                        displayThreadsResult.forumVariables)
            }
        })
    }

    companion object {
        private val TAG = HotThreadsFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(Discuz: Discuz, userBriefInfo: User?): HotThreadsFragment {
            val fragment = HotThreadsFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            fragment.arguments = args
            return fragment
        }
    }
}