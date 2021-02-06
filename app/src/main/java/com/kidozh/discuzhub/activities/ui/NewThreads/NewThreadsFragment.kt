package com.kidozh.discuzhub.activities.ui.NewThreads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import com.kidozh.discuzhub.adapter.ThreadAdapter
import com.kidozh.discuzhub.databinding.NewThreadsFragmentBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils

class NewThreadsFragment : Fragment() {

    lateinit var bbsInfo: Discuz
    var user: User? = null
    lateinit var threadAdapter: ThreadAdapter
    companion object {
        fun newInstance() = NewThreadsFragment()

        fun newInstance(Discuz: Discuz, user: User?): NewThreadsFragment {
            val fragment = NewThreadsFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, user)
            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = (requireArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?)!!
            user = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
            threadAdapter = ThreadAdapter(null, null, bbsInfo, user)
        }
    }

    private lateinit var viewModel: NewThreadsViewModel
    lateinit var binding: NewThreadsFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = NewThreadsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewThreadsViewModel::class.java)
        // TODO: Use the ViewModel
        bindViewModel()
        viewModel.setBBSInfo(bbsInfo, user)
        configureRecyclerview()

    }


    lateinit var concatAdapter: ConcatAdapter
    private val networkIndicatorAdapter = NetworkIndicatorAdapter()

    fun configureRecyclerview(){
        val linearLayoutManager = LinearLayoutManager(context)
        binding.newThreadsRecyclerview.setLayoutManager(linearLayoutManager)

        threadAdapter.ignoreDigestStyle = true
        concatAdapter = ConcatAdapter(threadAdapter, networkIndicatorAdapter)
        binding.newThreadsRecyclerview.setAdapter(getAnimatedAdapter(requireContext(), concatAdapter))
        binding.newThreadsRecyclerview.setItemAnimator(getRecyclerviewAnimation(requireContext()))

        binding.newThreadsRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(!viewModel.isLoadingMutableLiveData.value!! && !viewModel.loadAllMutableLiveData.value!!){
                        viewModel.loadNewThreads()
                    }


                }
            }
        })

        binding.newThreadsSwipeRefreshLayout.setOnRefreshListener {
            viewModel.pageMutableLiveData.postValue(1)
            viewModel.loadAllMutableLiveData.postValue(false)
            viewModel.loadNewThreads()
        }
    }

    fun bindViewModel(){
        viewModel.discuzIndexMutableLiveData.observe(viewLifecycleOwner, { result ->
            if (result != null) {
                viewModel.loadNewThreads()
            }
        })

        viewModel.isLoadingMutableLiveData.observe(viewLifecycleOwner, { aBool ->
            binding.newThreadsSwipeRefreshLayout.isRefreshing = aBool
            if(aBool){
                networkIndicatorAdapter.setLoadingStatus()
            }
            else{
                networkIndicatorAdapter.setLoadSuccessfulStatus()
            }
        })

        viewModel.loadAllMutableLiveData.observe(viewLifecycleOwner, { aBool ->
            if(aBool){
                networkIndicatorAdapter.setLoadedAllStatus()
            }
        })

        viewModel.errorMessageMutableLiveData.observe(viewLifecycleOwner,{message->
            if(message != null){
                networkIndicatorAdapter.setErrorStatus(message)
            }

        })

        viewModel.newThreadListMutableLiveData.observe(viewLifecycleOwner, { threadList ->
            val page = viewModel.pageMutableLiveData.value
            threadAdapter.threadList = threadList
            threadAdapter.notifyDataSetChanged()

        })
    }

}