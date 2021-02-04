package com.kidozh.discuzhub.activities.ui.HotForums;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardViewModel;
import com.kidozh.discuzhub.adapter.HotForumAdapter;
import com.kidozh.discuzhub.databinding.FragmentHotForumBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;

import java.util.ArrayList;

public class HotForumsFragment extends Fragment {
    private static final String TAG = HotForumsFragment.class.getSimpleName();
    Discuz bbsInfo;
    forumUserBriefInfo userBriefInfo;

    public static HotForumsFragment newInstance(Discuz Discuz, forumUserBriefInfo userBriefInfo){
        HotForumsFragment fragment = new HotForumsFragment();
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHotForumBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    FragmentHotForumBinding binding;

    

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HotForumsViewModel.class);
        viewModel.setBBSInfo(bbsInfo,userBriefInfo);
        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
    }

    HotForumAdapter adapter;


    HotForumsViewModel viewModel;
    DashBoardViewModel dashBoardViewModel;

    private void configureRecyclerview(){
        adapter = new HotForumAdapter(bbsInfo,userBriefInfo);
        binding.fragmentHotforumRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        binding.fragmentHotforumRecyclerview.setLayoutManager(new GridLayoutManager(getContext(),2));
        binding.fragmentHotforumRecyclerview.setHasFixedSize(true);
        binding.fragmentHotforumRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(getContext(),adapter));

    }

    private void bindViewModel(){
        viewModel.isLoadingMutableLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.fragmentHotforumSwipeRefreshLayout.setRefreshing(aBoolean);
            }
        });
        viewModel.errorMessageMutableLiveData.observe(getViewLifecycleOwner(),errorMessage -> {
            if(errorMessage!=null){
                Log.d(TAG,"Set error message "+errorMessage.key);
                binding.errorView.setVisibility(View.VISIBLE);
                binding.errorContent.setText(errorMessage.content);
                binding.errorValue.setText(errorMessage.key);
                binding.errorIcon.setImageResource(R.drawable.ic_error_outline_24px);
            }
            else {
                binding.errorView.setVisibility(View.GONE);
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
                    adapter.setHotForumList(new ArrayList<>());
                }
                else {
                    if(hotForumsResult.variables.hotForumList == null
                    || hotForumsResult.variables.hotForumList.size() == 0){
                        binding.errorContent.setText(R.string.empty_hot_forum);
                        binding.errorIcon.setImageResource(R.drawable.ic_user_group_empty_24dp);
                        binding.errorView.setVisibility(View.VISIBLE);

                    }
                    else {
                        binding.errorView.setVisibility(View.GONE);

                    }

                    Log.d(TAG,"Get hot forums "+hotForumsResult.variables.hotForumList);
                    adapter.setHotForumList(hotForumsResult.variables.hotForumList);

                }
                dashBoardViewModel.hotForumCountMutableLiveData.postValue(adapter.getItemCount());
            }
        });


    }

    private void configureSwipeRefreshLayout(){
        binding.fragmentHotforumSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.loadHotForums();
            }
        });
    }
}
