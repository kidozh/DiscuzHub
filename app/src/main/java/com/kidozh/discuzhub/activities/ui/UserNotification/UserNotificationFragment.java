package com.kidozh.discuzhub.activities.ui.UserNotification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UserNotificationAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserNotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserNotificationFragment extends Fragment {
    private static final String TAG = UserNotificationFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_TYPE = "TYPE";
    public static final String ARG_VIEW = "VIEW";

    private OnNewMessageChangeListener mListener;

    public UserNotificationFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.fragment_bbs_notification_recyclerview)
    RecyclerView bbsNotificationRecyclerview;
    @BindView(R.id.fragment_bbs_notification_swipeRefreshLayout)
    SwipeRefreshLayout bbsNotificationSwipeRefreshLayout;
    @BindView(R.id.fragment_bbs_notification_empty_view)
    View bbsNotificationEmptyView;

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    ForumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    UserNotificationAdapter adapter;
    private int globalPage = 1;
    private Boolean hasLoadAll = false;
    private String type, view;

    UserNotificationViewModel viewModel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment bbsNotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserNotificationFragment newInstance(String view, String type) {
        UserNotificationFragment fragment = new UserNotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_VIEW, view);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            view = getArguments().getString(ARG_VIEW);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewModel = new ViewModelProvider(this).get(UserNotificationViewModel.class);
        return inflater.inflate(R.layout.fragment_bbs_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        bindViewModel();
    }

    private void configureIntentData(){
        Intent intent = getActivity().getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        viewModel.setBBSInfo(bbsInfo,userBriefInfo);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        bbsNotificationRecyclerview.setLayoutManager(linearLayoutManager);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
//                linearLayoutManager.getOrientation());
//        bbsNotificationRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new UserNotificationAdapter(bbsInfo,userBriefInfo);
        bbsNotificationRecyclerview.setAdapter(adapter);

        bbsNotificationRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(isScrollAtEnd() && viewModel.isLoading.getValue() == false && viewModel.hasLoadedAll.getValue() == false){
                    globalPage += 1;
                    viewModel.getUserNotificationByPage(view,type, globalPage);

                }
            }

            public boolean isScrollAtEnd(){

                if (bbsNotificationRecyclerview.computeVerticalScrollExtent() + bbsNotificationRecyclerview.computeVerticalScrollOffset()
                        >= bbsNotificationRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });

    }

    private void bindViewModel(){
        viewModel.userNoteListResultMutableLiveData.observe(getViewLifecycleOwner(), new Observer<UserNoteListResult>() {
            @Override
            public void onChanged(UserNoteListResult userNoteListResult) {
                Log.d(TAG, "Recv notelist "+userNoteListResult);
                if(userNoteListResult !=null){
                    List<UserNoteListResult.UserNotification> notificationList = userNoteListResult.noteListVariableResult.notificationList;
                    if(globalPage == 1){
                        adapter.setNotificationDetailInfoList(notificationList);
                    }
                    else {
                        adapter.addNotificationDetailInfoList(notificationList);
                    }
                    // judge the loadall
                    if(adapter.getNotificationDetailInfoList()!=null &&
                            adapter.getNotificationDetailInfoList().size() >= userNoteListResult.noteListVariableResult.count){
                        viewModel.hasLoadedAll.postValue(true);
                    }
                    else {
                        viewModel.hasLoadedAll.postValue(false);
                    }
                }
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                bbsNotificationSwipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        viewModel.hasLoadedAll.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    if(globalPage == 1 &&
                            (adapter.getNotificationDetailInfoList() ==null || adapter.getNotificationDetailInfoList().size() == 0)){

                        bbsNotificationEmptyView.setVisibility(View.VISIBLE);
                    }
                    else {
                        bbsNotificationEmptyView.setVisibility(View.GONE);
                    }


                }
                else {
                    bbsNotificationEmptyView.setVisibility(View.GONE);
                }

            }
        });
        viewModel.isError.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    bbsNotificationEmptyView.setVisibility(View.VISIBLE);
                    if(globalPage > 1){
                        globalPage -= 1;
                    }
                }
                else {
                    bbsNotificationEmptyView.setVisibility(View.GONE);
                }
            }
        });
    }

    void configureSwipeRefreshLayout(){
        bbsNotificationSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                viewModel.getUserNotificationByPage(view,type,globalPage);
            }
        });
        viewModel.getUserNotificationByPage(view,type,globalPage);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void setNotificationNum(bbsParseUtils.noticeNumInfo notificationNum) {
        Log.d(TAG,"set message number "+notificationNum.getAllNoticeInfo());
        if (mListener != null) {
            mListener.setNotificationsNum(notificationNum);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewMessageChangeListener) {
            mListener = (OnNewMessageChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNewMessageChangeListener {
        // TODO: Update argument type and name
        void setNotificationsNum(bbsParseUtils.noticeNumInfo notificationsNum);
    }
}
