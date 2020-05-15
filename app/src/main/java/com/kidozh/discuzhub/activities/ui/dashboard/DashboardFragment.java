package com.kidozh.discuzhub.activities.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getSimpleName();
    private DashboardViewModel dashboardViewModel;
    @BindView(R.id.fragment_dashboard_recyclerview)
    RecyclerView dashboardRecyclerview;
    @BindView(R.id.fragment_dashboard_swipeRefreshLayout)
    SwipeRefreshLayout dashboardSwipeRefreshLayout;
    @BindView(R.id.fragment_dashboard_no_item_textView)
    TextView noItemFoundTextview;
    bbsForumThreadAdapter forumThreadAdapter;
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    private static int globalPage = 1;
    private Boolean isClientRunning = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
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
                    if(dashboardViewModel.pageNum.getValue() == null){
                        dashboardViewModel.setPageNumAndFetchThread(1);
                    }
                    else {
                        dashboardViewModel.setPageNumAndFetchThread(dashboardViewModel.pageNum.getValue()+1);
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
                dashboardViewModel.setPageNumAndFetchThread(1);
                //globalPage = 1;
                //getPageInfo(globalPage);

            }
        });
    }

    private void getIntentInfo(){
        Intent intent = getActivity().getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(curBBS == null){
            getActivity().finish();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            bbsURLUtils.setBBS(curBBS);
            dashboardViewModel.setBBSInfo(curBBS,curUser);
        }
    }

    private void bindVieModel(){
        dashboardViewModel.getThreadListLiveData().observe(getViewLifecycleOwner(), new Observer<List<ThreadInfo>>() {
            @Override
            public void onChanged(List<ThreadInfo> threadInfos) {
                forumThreadAdapter.setThreadInfoList(threadInfos,null);
            }
        });
        dashboardViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dashboardSwipeRefreshLayout.setRefreshing(aBoolean);
            }
        });
        dashboardViewModel.isError.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if(aBoolean){
                    noItemFoundTextview.setVisibility(View.VISIBLE);
                }
                else {
                    noItemFoundTextview.setVisibility(View.GONE);
                }
            }
        });
    }

}