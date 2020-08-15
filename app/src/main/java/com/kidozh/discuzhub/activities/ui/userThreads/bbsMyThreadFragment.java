package com.kidozh.discuzhub.activities.ui.userThreads;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.ThreadAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class bbsMyThreadFragment extends Fragment {
    private String TAG = bbsMyThreadFragment.class.getSimpleName();

    public bbsMyThreadFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.fragment_my_thread_recyclerview)
    RecyclerView myThreadRecyclerview;
    @BindView(R.id.fragment_my_thread_swipeRefreshLayout)
    SwipeRefreshLayout myThreadSwipeRefreshLayout;
    @BindView(R.id.fragment_my_thread_empty_view)
    View myThreadEmptyView;
    @BindView(R.id.empty_bbs_information_imageview)
    ImageView emptyImageview;
    @BindView(R.id.empty_bbs_information_text)
    TextView emptyTextview;

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    ForumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    ThreadAdapter adapter;
    private int globalPage = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bbs_my_thread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        configureEmptyView();

    }

    private void configureIntentData(){
        Intent intent = getActivity().getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    private void configureEmptyView(){
        emptyImageview.setImageResource(R.drawable.ic_empty_my_post_list);
        emptyTextview.setText(R.string.empty_post_list);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        myThreadRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        myThreadRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new ThreadAdapter(getContext(),null,"",bbsInfo,userBriefInfo);
        myThreadRecyclerview.setAdapter(adapter);

    }

    void configureSwipeRefreshLayout(){
        myThreadSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                getMyThread(globalPage);
            }
        });
        getMyThread(globalPage);
    }

    void getMyThread(int page){
        myThreadSwipeRefreshLayout.setRefreshing(true);
        String apiStr = URLUtils.getUserThreadUrl(page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"get my thread in page "+page);
        Handler mHandler = new Handler(Looper.getMainLooper());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myThreadSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myThreadSwipeRefreshLayout.setRefreshing(false);
                    }
                });

                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Getting PM "+s);


                    DisplayThreadsResult threadsResult = bbsParseUtils.getThreadListInfo(s);
                    if(getContext() instanceof BaseStatusInteract){
                        ((BaseStatusInteract) getContext()).setBaseResult(threadsResult,
                                threadsResult!=null?threadsResult.forumVariables:null);
                    }
                    if(threadsResult!=null && threadsResult.forumVariables !=null) {
                        List<ThreadInfo> threadInfoList = threadsResult.forumVariables.forumThreadList;

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (page == 1) {
                                    adapter.setThreadInfoList(threadInfoList, null);
                                } else {
                                    adapter.addThreadInfoList(threadInfoList, null);
                                }
                                if(threadInfoList == null || threadInfoList.size() == 0){
                                    myThreadEmptyView.setVisibility(View.VISIBLE);
                                }
                                else {
                                    myThreadEmptyView.setVisibility(View.GONE);
                                }

                            }
                        });

                        globalPage += 1;
                    }
                }
                else {
                    myThreadEmptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
