package com.kidozh.discuzhub.activities.ui.HotForums;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.HotForumAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import org.checkerframework.checker.units.qual.A;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

public class HotForumsFragment extends Fragment {
    private static final String TAG = HotForumsFragment.class.getSimpleName();
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    OkHttpClient client;

    public static HotForumsFragment newInstance(bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        HotForumsFragment fragment = new HotForumsFragment();
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
            bbsInfo = (bbsInformation) getArguments().getSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY);
            URLUtils.setBBS(bbsInfo);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);
            client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hot_forum, container, false);
    }

    @BindView(R.id.fragment_hotforum_swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_hotforum_empty_icon)
    ImageView emptyIcon;
    @BindView(R.id.fragment_hotforum_no_item_textView)
    TextView errorTextView;

    @BindView(R.id.fragment_hotforum_recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.hot_forum_empty_view)
    View emptyView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        viewModel = new ViewModelProvider(this).get(HotForumsViewModel.class);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
    }

    HotForumAdapter adapter;


    HotForumsViewModel viewModel;

    private void configureRecyclerview(){
        adapter = new HotForumAdapter(bbsInfo,userBriefInfo);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }

    private void bindViewModel(){
        viewModel.isLoadingMutableLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        viewModel.isErrorMutableLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    errorTextView.setText(viewModel.errorString.getValue());
                    emptyIcon.setImageResource(R.drawable.ic_error_outline_24px);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getHotForumsResult().observe(getViewLifecycleOwner(), new Observer<HotForumsResult>() {
            @Override
            public void onChanged(HotForumsResult hotForumsResult) {
                if(getContext() instanceof BaseStatusInteract){
                    ((BaseStatusInteract) getContext()).setBaseResult(hotForumsResult,
                            hotForumsResult!=null?hotForumsResult.variables:null);
                }
                if(hotForumsResult == null ||
                        hotForumsResult.variables == null){

                    errorTextView.setText(R.string.empty_hot_forum);
                    emptyIcon.setImageResource(R.drawable.ic_user_group_empty_24dp);
                    emptyView.setVisibility(View.VISIBLE);
                }
                else {
                    if(hotForumsResult.variables.hotForumList == null
                    || hotForumsResult.variables.hotForumList.size() == 0){
                        errorTextView.setText(R.string.empty_hot_forum);
                        emptyIcon.setImageResource(R.drawable.ic_user_group_empty_24dp);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    else {
                        emptyView.setVisibility(View.GONE);

                    }
                    Log.d(TAG,"Get hot forums "+hotForumsResult.variables.hotForumList);
                    adapter.setHotForumList(hotForumsResult.variables.hotForumList);
                }
            }
        });


    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.loadHotForums();
            }
        });
    }
}
