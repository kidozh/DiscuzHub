package com.kidozh.discuzhub.activities.ui.HotThreads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsForumThreadAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

public class HotThreadsFragment extends Fragment {
    private static final String TAG = HotThreadsFragment.class.getSimpleName();
    private HotThreadsViewModel hotThreadsViewModel;
    @BindView(R.id.fragment_dashboard_recyclerview)
    RecyclerView dashboardRecyclerview;
    @BindView(R.id.fragment_dashboard_swipeRefreshLayout)
    SwipeRefreshLayout dashboardSwipeRefreshLayout;
    @BindView(R.id.fragment_dashboard_no_item_textView)
    TextView noItemFoundTextview;
    @BindView(R.id.fragment_dashboard_empty_icon)
    ImageView emptyIconImageview;
    bbsForumThreadAdapter forumThreadAdapter;
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    private static int globalPage = 1;
    private Boolean isClientRunning = false;

    public HotThreadsFragment(){

    }

    public static HotThreadsFragment newInstance(bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        HotThreadsFragment fragment = new HotThreadsFragment();
        Bundle args = new Bundle();
        args.putSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
        args.putSerializable(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curBBS = (bbsInformation) getArguments().getSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY);
            URLUtils.setBBS(curBBS);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);
            client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        }
    }

    private HotThreadsFragment(bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        curBBS = bbsInformation;
        this.userBriefInfo = userBriefInfo;
        curUser = userBriefInfo;
        URLUtils.setBBS(curBBS);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hotThreadsViewModel = new ViewModelProvider(this).get(HotThreadsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.bind(this,root);
        getIntentInfo();

        configureClient();

        configureThreadRecyclerview();
        configureSwipeRefreshLayout();
        bindVieModel();
        return root;
    }

    private void configureClient(){
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    private void configureThreadRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        dashboardRecyclerview.setLayoutManager(linearLayoutManager);
        forumThreadAdapter = new bbsForumThreadAdapter(getContext(),null,null,curBBS,userBriefInfo);
        dashboardRecyclerview.setAdapter(forumThreadAdapter);
        dashboardRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    if(hotThreadsViewModel.pageNum.getValue() == null){
                        hotThreadsViewModel.setPageNumAndFetchThread(1);
                    }
                    else {
                        hotThreadsViewModel.setPageNumAndFetchThread(hotThreadsViewModel.pageNum.getValue()+1);
                    }
                    // getPageInfo(globalPage);

                }
            }

            public boolean isScrollAtEnd(){

                if (dashboardRecyclerview.computeVerticalScrollExtent() + dashboardRecyclerview.computeVerticalScrollOffset()
                        >= dashboardRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });
    }

    private void configureSwipeRefreshLayout(){
        dashboardSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hotThreadsViewModel.setPageNumAndFetchThread(1);
                //globalPage = 1;
                //getPageInfo(globalPage);

            }
        });
    }

    private void getIntentInfo(){
        if(curBBS != null){
            hotThreadsViewModel.setBBSInfo(curBBS,curUser);
            return;
        }
        Intent intent = getActivity().getIntent();

        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(curBBS == null){
            getActivity().finish();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            URLUtils.setBBS(curBBS);
            hotThreadsViewModel.setBBSInfo(curBBS,curUser);
        }
    }

    private void bindVieModel(){
        hotThreadsViewModel.getThreadListLiveData().observe(getViewLifecycleOwner(), new Observer<List<ThreadInfo>>() {
            @Override
            public void onChanged(List<ThreadInfo> threadInfos) {

                forumThreadAdapter.setThreadInfoList(threadInfos,null);
                if(forumThreadAdapter.threadInfoList == null || forumThreadAdapter.threadInfoList.size() == 0){
                    noItemFoundTextview.setVisibility(View.VISIBLE);
                    emptyIconImageview.setVisibility(View.VISIBLE);
                    noItemFoundTextview.setText(R.string.empty_hot_threads);
                    emptyIconImageview.setImageResource(R.drawable.ic_empty_hot_thread_64px);
                }
                else {
                    noItemFoundTextview.setVisibility(View.GONE);
                    emptyIconImageview.setVisibility(View.GONE);
                }
            }
        });
        hotThreadsViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dashboardSwipeRefreshLayout.setRefreshing(aBoolean);
            }
        });
        hotThreadsViewModel.isError.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if(aBoolean){
                    noItemFoundTextview.setVisibility(View.VISIBLE);
                    emptyIconImageview.setVisibility(View.VISIBLE);
                    emptyIconImageview.setImageResource(R.drawable.ic_error_outline_24px);
                    String errorText = hotThreadsViewModel.errorTextLiveData.getValue();
                    if(errorText == null || errorText.length() == 0){
                        noItemFoundTextview.setText(R.string.parse_failed);
                    }
                    else {
                        noItemFoundTextview.setText(errorText);
                    }

                }
                else {
//                    noItemFoundTextview.setVisibility(View.GONE);
//                    emptyIconImageview.setVisibility(View.GONE);
                }
            }
        });
    }

}