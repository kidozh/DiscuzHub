package com.kidozh.discuzhub.activities.ui.UserNotification

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment
import com.kidozh.discuzhub.adapter.NotificationAdapter
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding
import com.kidozh.discuzhub.databinding.FragmentBbsNotificationBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.bbsParseUtils.noticeNumInfo

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [UserNotificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserNotificationFragment() : Fragment() {
    private var mListener: OnNewMessageChangeListener? = null
    private var userBriefInfo: User? = null
    var bbsInfo: Discuz? = null
    lateinit var binding: FragmentBbsNotificationBinding
    lateinit var emptyBinding: ContentEmptyInformationBinding
    lateinit var adapter: NotificationAdapter
    private var globalPage = 1
    private var type: String? = null
    private var view: String? = null
    var viewModel: UserNotificationViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            type = requireArguments().getString(ARG_TYPE)
            view = requireArguments().getString(ARG_VIEW)
            bbsInfo = requireArguments().getSerializable(ARG_BBS) as Discuz
            userBriefInfo = requireArguments().getSerializable(ARG_USER) as User?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBbsNotificationBinding.inflate(inflater, container, false)
        emptyBinding = binding.fragmentBbsNotificationEmptyView
        viewModel = ViewModelProvider(this).get(UserNotificationViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureIntentData()
        configureRecyclerview()
        configureSwipeRefreshLayout()
        bindViewModel()
        configureEmptyView()
    }

    private fun configureEmptyView() {
        emptyBinding.emptyIcon.setImageResource(R.drawable.ic_empty_notification_64px)
        emptyBinding.emptyContent.setText(R.string.empty_notification)
    }

    private fun configureIntentData() {
        Log.d(TAG, "recv user $userBriefInfo")
        viewModel!!.setBBSInfo(bbsInfo!!, userBriefInfo)
    }

    private fun configureRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding.fragmentBbsNotificationRecyclerview.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(
            context,
            linearLayoutManager.orientation
        )
        binding.fragmentBbsNotificationRecyclerview.itemAnimator = getRecyclerviewAnimation(
            (context)!!
        )
        binding.fragmentBbsNotificationRecyclerview.addItemDecoration(dividerItemDecoration)
        adapter = NotificationAdapter(bbsInfo, userBriefInfo)
        binding.fragmentBbsNotificationRecyclerview.adapter = getAnimatedAdapter(
            (context)!!, adapter
        )
        binding.fragmentBbsNotificationRecyclerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (isScrollAtEnd && !viewModel!!.isLoading.value!! && (viewModel!!.hasLoadedAll.value == false)) {
                    globalPage += 1
                    viewModel!!.getUserNotificationByPage(view, type, globalPage)
                }
            }

            val isScrollAtEnd: Boolean
                get() {
                    return (binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollExtent() + binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollOffset()
                                >= binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollRange())
                }
        })
    }

    private fun bindViewModel() {
        viewModel!!.userNoteListResultMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { userNoteListResult ->
                if (context is BaseStatusInteract) {
                    userNoteListResult?.noteListVariableResult?.let {
                        (context as BaseStatusInteract?)!!.setBaseResult(
                            userNoteListResult,
                            it
                        )
                    }
                }
                Log.d(TAG, "Recv notelist $userNoteListResult")
                if (userNoteListResult?.noteListVariableResult != null) {
                    val notificationList =
                        userNoteListResult.noteListVariableResult.notificationList
                    if (globalPage == 1) {
                        adapter.notificationDetailInfoList = notificationList
                    } else {
                        adapter.addNotificationDetailInfoList(notificationList)
                    }
                    // judge the loadall
                    if (adapter.notificationDetailInfoList != null &&
                        adapter.notificationDetailInfoList.size >= userNoteListResult.noteListVariableResult.count
                    ) {
                        viewModel!!.hasLoadedAll.postValue(true)
                    } else {
                        viewModel!!.hasLoadedAll.postValue(false)
                    }
                }
            })
        viewModel!!.isLoading.observe(viewLifecycleOwner, object : Observer<Boolean?> {
            override fun onChanged(aBoolean: Boolean?) {
                binding.fragmentBbsNotificationSwipeRefreshLayout.isRefreshing = (aBoolean)!!
            }
        })
        viewModel!!.hasLoadedAll.observe(viewLifecycleOwner, object : Observer<Boolean> {
            override fun onChanged(aBoolean: Boolean) {
                if (aBoolean) {
                    if (globalPage == 1 &&
                        (adapter.notificationDetailInfoList == null || adapter.notificationDetailInfoList.size == 0)
                    ) {
                        emptyBinding.emptyView.visibility = View.VISIBLE
                    } else {
                        emptyBinding.emptyView.visibility = View.GONE
                    }
                } else {
                    emptyBinding.emptyView.visibility = View.GONE
                }
            }
        })
        viewModel!!.isError.observe(viewLifecycleOwner, object : Observer<Boolean> {
            override fun onChanged(aBoolean: Boolean) {
                if (aBoolean) {
                    emptyBinding.emptyView.visibility = View.VISIBLE
                    if (globalPage > 1) {
                        globalPage -= 1
                    }
                } else {
                    emptyBinding.emptyView.visibility = View.GONE
                }
            }
        })
    }

    fun configureSwipeRefreshLayout() {
        binding.fragmentBbsNotificationSwipeRefreshLayout.setOnRefreshListener {
            globalPage = 1
            viewModel!!.getUserNotificationByPage(view, type, globalPage)
        }
        viewModel!!.getUserNotificationByPage(view, type, globalPage)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun setNotificationNum(notificationNum: noticeNumInfo) {
        Log.d(TAG, "set message number " + notificationNum.allNoticeInfo)
        if (mListener != null) {
            mListener!!.setNotificationsNum(notificationNum)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNewMessageChangeListener) {
            mListener = context
        } else {
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
    interface OnNewMessageChangeListener {
        // TODO: Update argument type and name
        fun setNotificationsNum(notificationsNum: noticeNumInfo)
    }

    companion object {
        private val TAG = UserNotificationFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment bbsNotificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        val ARG_TYPE = "TYPE"
        val ARG_VIEW = "VIEW"
        val ARG_BBS = "ARG_BBS"
        val ARG_USER = "ARG_USER"
        @JvmStatic
        fun newInstance(
            view: String?,
            type: String?,
            Discuz: Discuz?,
            userBriefInfo: User?
        ): UserNotificationFragment {
            val fragment = UserNotificationFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, type)
            args.putString(ARG_VIEW, view)
            args.putSerializable(ARG_BBS, Discuz)
            args.putSerializable(ARG_USER, userBriefInfo)
            fragment.arguments = args
            return fragment
        }
    }
}