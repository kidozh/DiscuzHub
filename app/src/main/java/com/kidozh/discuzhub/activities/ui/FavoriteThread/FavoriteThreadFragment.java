package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardFragment;
import com.kidozh.discuzhub.adapter.FavoriteThreadAdapter;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteThreadFragment extends Fragment {

    private FavoriteThreadViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;

    public FavoriteThreadFragment(){

    }

    public static FavoriteThreadFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        FavoriteThreadFragment fragment = new FavoriteThreadFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER,userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
        }
    }

    FavoriteThreadAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorite_thread_fragment, container, false);
    }

    @BindView(R.id.blank_favorite_thread_view)
    View blankFavoriteThreadView;
    @BindView(R.id.favorite_thread_recyclerview)
    RecyclerView favoriteThreadRecyclerview;
    @BindView(R.id.favorite_thread_swipelayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mViewModel = new ViewModelProvider(this).get(FavoriteThreadViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
    }

    private void configureRecyclerview(){
        favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteThreadAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteThreadListData().observe(getViewLifecycleOwner(),adapter::submitList);
        favoriteThreadRecyclerview.setAdapter(adapter);
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void bindViewModel(){


        mViewModel.getFavoriteThreadListData().observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(favoriteThreads.size() == 0){
                blankFavoriteThreadView.setVisibility(View.VISIBLE);
            }
            else {
                blankFavoriteThreadView.setVisibility(View.GONE);
            }
        });
    }


}