package com.kidozh.discuzhub.activities.ui.UserFriend

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.UserFriend.UserFriendFragment
import com.kidozh.discuzhub.adapter.FriendAdapter
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import com.kidozh.discuzhub.databinding.FragmentUserFriendBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.UserFriendResult
import com.kidozh.discuzhub.results.UserFriendResult.UserFriend
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [UserFriendFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [UserFriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFriendFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var uid = 0
    private var friendCounts = 0
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            uid = requireArguments().getInt(UID)
            friendCounts = requireArguments().getInt(FRIEND_COUNTS)
        }
    }

    var binding: FragmentUserFriendBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserFriendBinding.inflate(
            inflater,
            container,
            false
        )
        return binding!!.root
    }

    private var userBriefInfo: User? = null
    var bbsInfo: Discuz? = null
    var forum: Forum? = null
    var adapter: FriendAdapter? = null
    private var viewModel: UserFriendViewModel? = null
    var concatAdapter: ConcatAdapter? = null
    var networkIndicatorAdapter = NetworkIndicatorAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureIntentData()
        configureRecyclerview()
        configureSwipeRefreshLayout()
        bindViewModel()
    }

    private fun configureIntentData() {
        val intent = requireActivity().intent
        forum = intent.getParcelableExtra(ConstUtils.PASS_FORUM_THREAD_KEY)
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        userBriefInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        viewModel = ViewModelProvider(this).get(UserFriendViewModel::class.java)
        Log.d(TAG, "Set bbs $bbsInfo user $userBriefInfo")
        viewModel!!.setInfo(bbsInfo!!, userBriefInfo, uid, friendCounts)
    }

    private fun configureRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding!!.userFriendRecyclerview.layoutManager = linearLayoutManager
        binding!!.userFriendRecyclerview.itemAnimator = getRecyclerviewAnimation(
            requireContext()
        )
        val dividerItemDecoration = DividerItemDecoration(
            context,
            linearLayoutManager.orientation
        )
        binding!!.userFriendRecyclerview.addItemDecoration(dividerItemDecoration)
        adapter = FriendAdapter(bbsInfo, userBriefInfo)
        concatAdapter = ConcatAdapter(adapter, networkIndicatorAdapter)
        binding!!.userFriendRecyclerview.adapter = getAnimatedAdapter(
            requireContext(), concatAdapter!!
        )
        binding!!.userFriendRecyclerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!binding!!.userFriendRecyclerview.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel!!.getFriendInfo()
                }
            }
        })
    }

    private fun bindViewModel() {
        viewModel!!.newFriendListMutableLiveData.observe(
            viewLifecycleOwner,
            { userFriends: List<UserFriend?>? ->
                if (userFriends != null) {
                    Log.d(TAG, "Add new friends " + userFriends.size + " page " + viewModel!!.page)
                    adapter!!.addUserFriendList(userFriends)
                    if (viewModel!!.page == 2) {
                        // means successfully
                        binding!!.userFriendRecyclerview.scrollToPosition(0)
                    }
                }
            })
        viewModel!!.userFriendListMutableData.observe(viewLifecycleOwner, { userFriends ->
            if (userFriends == null || userFriends.size == 0) {
                // check for privacy
                if (viewModel!!.privacyMutableLiveData.value != null && viewModel!!.privacyMutableLiveData.value == false) {
                    binding!!.userFriendErrorTextview.visibility = View.VISIBLE
                    binding!!.userFriendEmptyImageView.visibility = View.VISIBLE
                    binding!!.userFriendErrorTextview.setText(R.string.bbs_no_friend)
                    binding!!.userFriendEmptyImageView.setImageResource(R.drawable.ic_empty_friend_64px)
                } else {
                    binding!!.userFriendErrorTextview.visibility = View.VISIBLE
                    binding!!.userFriendEmptyImageView.visibility = View.VISIBLE
                    binding!!.userFriendEmptyImageView.setImageResource(R.drawable.ic_privacy_24px)
                    binding!!.userFriendErrorTextview.setText(R.string.bbs_privacy_protect_alert)
                }
            } else {
                binding!!.userFriendErrorTextview.visibility = View.GONE
                binding!!.userFriendEmptyImageView.visibility = View.GONE
            }
        })
        viewModel!!.isLoadingMutableLiveData.observe(viewLifecycleOwner, { aBoolean ->
            if (aBoolean) {
                binding!!.userFriendSwipeRefreshLayout.isRefreshing = true
                networkIndicatorAdapter.setLoadingStatus()
            } else {
                binding!!.userFriendSwipeRefreshLayout.isRefreshing = false
                networkIndicatorAdapter.setLoadSuccessfulStatus()
            }
        })
        viewModel!!.isErrorMutableLiveData.observe(viewLifecycleOwner, { aBoolean ->
            if (aBoolean) {
                binding!!.userFriendErrorTextview.visibility = View.VISIBLE
                binding!!.userFriendEmptyImageView.visibility = View.VISIBLE
                binding!!.userFriendEmptyImageView.setImageResource(R.drawable.ic_error_outline_24px)
                val errorText = viewModel!!.errorTextMutableLiveData.value
                if (errorText == null || errorText.length == 0) {
                    binding!!.userFriendErrorTextview.setText(R.string.network_failed)
                } else {
                    binding!!.userFriendErrorTextview.text = errorText
                }
            } else {
                binding!!.userFriendErrorTextview.visibility = View.GONE
                binding!!.userFriendEmptyImageView.visibility = View.GONE
            }
        })
        viewModel!!.privacyMutableLiveData.observe(viewLifecycleOwner, { aBoolean ->
            if (aBoolean) {
                binding!!.userFriendErrorTextview.visibility = View.VISIBLE
                binding!!.userFriendEmptyImageView.visibility = View.VISIBLE
                binding!!.userFriendEmptyImageView.setImageResource(R.drawable.ic_privacy_24px)
                binding!!.userFriendErrorTextview.setText(R.string.bbs_privacy_protect_alert)
            }
        })
        viewModel!!.userFriendResultMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { userFriendResult: UserFriendResult? ->
                if (context is BaseStatusInteract && userFriendResult!=null && userFriendResult.friendVariables!=null) {
                    (context as BaseStatusInteract?)!!.setBaseResult(
                        userFriendResult,
                        (userFriendResult.friendVariables)!!
                    )
                }
            })
        viewModel!!.loadAllMutableLiveData.observe(viewLifecycleOwner, { aBoolean: Boolean ->
            if (aBoolean) {
                networkIndicatorAdapter.setLoadedAllStatus()
            }
        })
    }

    private fun configureSwipeRefreshLayout() {
        binding!!.userFriendSwipeRefreshLayout.setOnRefreshListener {
            viewModel!!.page = 1
            adapter!!.clearList()
            viewModel!!.loadAllMutableLiveData.value = false
            viewModel!!.getFriendInfo()
        }
        viewModel!!.getFriendInfo()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener =
            if (context is OnFragmentInteractionListener) {
                context
            } else {
                throw RuntimeException(
                    context.toString()
                            + " must implement OnFragmentInteractionListener"
                )
            }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
        fun onRenderSuccessfully()
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val UID = "uid"
        private const val FRIEND_COUNTS = "FRIEND_COUNTS"
        private val TAG = UserFriendFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uid Parameter 1.
         * @return A new instance of fragment userFriendFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(uid: Int, friendCounts: Int): UserFriendFragment {
            val fragment = UserFriendFragment()
            val args = Bundle()
            args.putInt(UID, uid)
            args.putInt(FRIEND_COUNTS, friendCounts)
            fragment.arguments = args
            return fragment
        }
    }
}