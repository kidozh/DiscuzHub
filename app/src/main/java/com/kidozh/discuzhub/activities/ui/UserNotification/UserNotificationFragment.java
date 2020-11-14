package com.kidozh.discuzhub.activities.ui.UserNotification;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.NotificationAdapter;
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding;
import com.kidozh.discuzhub.databinding.FragmentBbsNotificationBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;

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



    private OnNewMessageChangeListener mListener;

    public UserNotificationFragment() {
        // Required empty public constructor
    }


    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    
    FragmentBbsNotificationBinding binding;
    ContentEmptyInformationBinding emptyBinding;


    NotificationAdapter adapter;
    private int globalPage = 1;
    private String type, view;

    UserNotificationViewModel viewModel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment bbsNotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_TYPE = "TYPE";
    public static final String ARG_VIEW = "VIEW", ARG_BBS = "ARG_BBS", ARG_USER= "ARG_USER";

    public static UserNotificationFragment newInstance(String view, String type,bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo) {
        UserNotificationFragment fragment = new UserNotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_VIEW, view);
        args.putSerializable(ARG_BBS,bbsInformation);
        args.putSerializable(ARG_USER, userBriefInfo);

        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            view = getArguments().getString(ARG_VIEW);
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBbsNotificationBinding.inflate(inflater,container,false);
        emptyBinding = binding.fragmentBbsNotificationEmptyView;
        viewModel = new ViewModelProvider(this).get(UserNotificationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        bindViewModel();
        configureEmptyView();
    }

    private void configureEmptyView(){
        emptyBinding.emptyIcon.setImageResource(R.drawable.ic_empty_notification_64px);
        emptyBinding.emptyContent.setText(R.string.empty_notification);
    }

    private void configureIntentData(){
        Log.d(TAG,"recv user "+userBriefInfo);
        viewModel.setBBSInfo(bbsInfo,userBriefInfo);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.fragmentBbsNotificationRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        binding.fragmentBbsNotificationRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        binding.fragmentBbsNotificationRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new NotificationAdapter(bbsInfo,userBriefInfo);
        binding.fragmentBbsNotificationRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(getContext(),adapter));


        binding.fragmentBbsNotificationRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(isScrollAtEnd() && viewModel.isLoading.getValue() == false && viewModel.hasLoadedAll.getValue() == false){
                    globalPage += 1;
                    viewModel.getUserNotificationByPage(view,type, globalPage);

                }
            }

            public boolean isScrollAtEnd(){

                if (binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollExtent() + binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollOffset()
                        >= binding.fragmentBbsNotificationRecyclerview.computeVerticalScrollRange()){
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
                if(getContext() instanceof BaseStatusInteract){
                    ((BaseStatusInteract) getContext()).setBaseResult(userNoteListResult,
                            userNoteListResult!=null?userNoteListResult.noteListVariableResult:null);
                }

                Log.d(TAG, "Recv notelist "+userNoteListResult);
                if(userNoteListResult !=null && userNoteListResult.noteListVariableResult!=null){
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
                binding.fragmentBbsNotificationSwipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        viewModel.hasLoadedAll.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    if(globalPage == 1 &&
                            (adapter.getNotificationDetailInfoList() ==null || adapter.getNotificationDetailInfoList().size() == 0)){

                        emptyBinding.emptyView.setVisibility(View.VISIBLE);
                    }
                    else {
                        emptyBinding.emptyView.setVisibility(View.GONE);
                    }


                }
                else {
                    emptyBinding.emptyView.setVisibility(View.GONE);
                }

            }
        });
        viewModel.isError.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    emptyBinding.emptyView.setVisibility(View.VISIBLE);
                    if(globalPage > 1){
                        globalPage -= 1;
                    }
                }
                else {
                    emptyBinding.emptyView.setVisibility(View.GONE);
                }
            }
        });
    }

    void configureSwipeRefreshLayout(){
        binding.fragmentBbsNotificationSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
