package com.kidozh.discuzhub.activities.ui.userThreads;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.ThreadAdapter;
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding;
import com.kidozh.discuzhub.databinding.FragmentBbsMyThreadBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.Thread;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserThreadFragment extends Fragment {
    private String TAG = UserThreadFragment.class.getSimpleName();

    public UserThreadFragment() {
        // Required empty public constructor
    }

    private forumUserBriefInfo userBriefInfo;
    Discuz bbsInfo;
    private OkHttpClient client;
    ThreadAdapter adapter;
    private int globalPage = 1;
    FragmentBbsMyThreadBinding binding;
    ContentEmptyInformationBinding emptyBinding;

    public static UserThreadFragment newInstance(Discuz Discuz, forumUserBriefInfo userBriefInfo){
        UserThreadFragment fragment = new UserThreadFragment();
        Bundle args = new Bundle();
        args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz);
        args.putSerializable(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (Discuz) getArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY);
            URLUtils.setBBS(bbsInfo);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY);
            client = NetworkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBbsMyThreadBinding.inflate(inflater,container,false);
        emptyBinding = binding.fragmentMyThreadEmptyView;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureRecyclerview();
        configureSwipeRefreshLayout();
        configureEmptyView();

    }


    private void configureEmptyView(){
        emptyBinding.emptyIcon.setImageResource(R.drawable.ic_empty_my_post_list);
        emptyBinding.emptyContent.setText(R.string.empty_post_list);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.fragmentMyThreadRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new ThreadAdapter(null,"",bbsInfo,userBriefInfo);
        binding.fragmentMyThreadRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        binding.fragmentMyThreadRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(getContext(),adapter));

    }

    void configureSwipeRefreshLayout(){
        binding.fragmentMyThreadSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                getMyThread(globalPage);
            }
        });
        getMyThread(globalPage);
    }

    void getMyThread(int page){
        binding.fragmentMyThreadSwipeRefreshLayout.setRefreshing(true);
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
                        binding.fragmentMyThreadSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.fragmentMyThreadSwipeRefreshLayout.setRefreshing(false);
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
                        List<Thread> threadList = threadsResult.forumVariables.forumThreadList;

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addThreadInfoList(threadList, null);
                                if (page == 1 && adapter.threadList.size() != 0) {
                                    adapter.clearList();

                                }
                                adapter.addThreadInfoList(threadList, null);
                                if(threadList == null || threadList.size() == 0){
                                    emptyBinding.emptyView.setVisibility(View.VISIBLE);
                                }
                                else {
                                    emptyBinding.emptyView.setVisibility(View.GONE);
                                }

                            }
                        });

                        globalPage += 1;
                    }
                }
                else {
                    emptyBinding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
