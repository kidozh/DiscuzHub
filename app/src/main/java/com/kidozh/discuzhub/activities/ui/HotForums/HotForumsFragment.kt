package com.kidozh.discuzhub.activities.ui.HotForums

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardViewModel
import com.kidozh.discuzhub.activities.ui.HotForums.HotForumsFragment
import com.kidozh.discuzhub.adapter.HotForumAdapter
import com.kidozh.discuzhub.databinding.FragmentHotForumBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.URLUtils
import java.util.*

class HotForumsFragment : Fragment() {
    var bbsInfo: Discuz? = null
    var userBriefInfo: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
            URLUtils.setBBS(bbsInfo)
            userBriefInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHotForumBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    var binding: FragmentHotForumBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(HotForumsViewModel::class.java)
        viewModel!!.setBBSInfo(bbsInfo, userBriefInfo)
        dashBoardViewModel = ViewModelProvider(this).get(DashBoardViewModel::class.java)
        configureRecyclerview()
        bindViewModel()
        configureSwipeRefreshLayout()
    }

    var adapter: HotForumAdapter? = null
    var viewModel: HotForumsViewModel? = null
    var dashBoardViewModel: DashBoardViewModel? = null
    private fun configureRecyclerview() {
        adapter = HotForumAdapter(bbsInfo, userBriefInfo)
        binding!!.fragmentHotforumRecyclerview.itemAnimator = getRecyclerviewAnimation(
            requireContext()
        )
        binding!!.fragmentHotforumRecyclerview.layoutManager = GridLayoutManager(context, 2)
        binding!!.fragmentHotforumRecyclerview.setHasFixedSize(true)
        binding!!.fragmentHotforumRecyclerview.adapter = getAnimatedAdapter(
            requireContext(), adapter!!
        )
    }

    private fun bindViewModel() {
        viewModel!!.isLoadingMutableLiveData.observe(
            viewLifecycleOwner,
            { aBoolean -> binding!!.fragmentHotforumSwipeRefreshLayout.isRefreshing = aBoolean!! })
        viewModel!!.errorMessageMutableLiveData.observe(
            viewLifecycleOwner,
            { errorMessage: ErrorMessage? ->
                if (errorMessage != null) {
                    Log.d(TAG, "Set error message " + errorMessage.key)
                    binding!!.errorView.visibility = View.VISIBLE
                    binding!!.errorContent.text = errorMessage.content
                    binding!!.errorValue.text = errorMessage.key
                    if (errorMessage.errorIconResource == 0) {
                        binding!!.errorIcon.setImageResource(R.drawable.ic_error_outline_24px)
                    } else {
                        binding!!.errorIcon.setImageResource(errorMessage.errorIconResource)
                    }
                } else {
                    binding!!.errorView.visibility = View.GONE
                }
            })
        viewModel!!.hotForumsResult.observe(viewLifecycleOwner, { hotForumsResult ->
            if (context is BaseStatusInteract && hotForumsResult!=null && hotForumsResult.variables!=null) {
                (context as BaseStatusInteract).setBaseResult(
                    hotForumsResult,
                    hotForumsResult.variables
                )
            }
            if (hotForumsResult?.variables == null) {
                adapter!!.setHotForumList(ArrayList())
            } else {
                if (hotForumsResult.variables.hotForumList == null
                    || hotForumsResult.variables.hotForumList.size == 0
                ) {
                    binding!!.errorContent.setText(R.string.empty_hot_forum)
                    binding!!.errorIcon.setImageResource(R.drawable.ic_user_group_empty_24dp)
                    binding!!.errorView.visibility = View.VISIBLE
                } else {
                    binding!!.errorView.visibility = View.GONE
                }
                Log.d(TAG, "Get hot forums " + hotForumsResult.variables.hotForumList)
                adapter!!.setHotForumList(hotForumsResult.variables.hotForumList)
            }
            dashBoardViewModel!!.hotForumCountMutableLiveData.postValue(adapter!!.itemCount)
        })
    }

    private fun configureSwipeRefreshLayout() {
        binding!!.fragmentHotforumSwipeRefreshLayout.setOnRefreshListener { viewModel!!.loadHotForums() }
    }

    companion object {
        private val TAG = HotForumsFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(Discuz: Discuz?, userBriefInfo: User?): HotForumsFragment {
            val fragment = HotForumsFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            fragment.arguments = args
            return fragment
        }
    }
}