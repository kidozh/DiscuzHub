package com.kidozh.discuzhub.activities.ui.HotThreads;

import android.os.Bundle;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardViewModel;
import com.kidozh.discuzhub.adapter.ThreadAdapter;
import com.kidozh.discuzhub.databinding.FragmentHotThreadBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

public class HotThreadsFragment extends Fragment {
    private static final String TAG = HotThreadsFragment.class.getSimpleName();
    private HotThreadsViewModel hotThreadsViewModel;
    private DashBoardViewModel dashBoardViewModel;
    FragmentHotThreadBinding binding;
    

    ThreadAdapter forumThreadAdapter;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;


    public HotThreadsFragment(){

    }

    public static HotThreadsFragment newInstance(@NonNull bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
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
            bbsInfo = (bbsInformation) getArguments().getSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hotThreadsViewModel = new ViewModelProvider(this).get(HotThreadsViewModel.class);
        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_hot_thread, container, false);
        ButterKnife.bind(this,root);
        getIntentInfo();

        configureClient();

        configureThreadRecyclerview();
        configureSwipeRefreshLayout();
        bindVieModel();
        return root;
    }

    private void configureClient(){
        if(bbsInfo !=null){
            hotThreadsViewModel.setBBSInfo(bbsInfo,userBriefInfo);
        }

    }

    private void configureThreadRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.fragmentHotThreadRecyclerview.setLayoutManager(linearLayoutManager);
        forumThreadAdapter = new ThreadAdapter(null,null,bbsInfo,userBriefInfo);
        binding.fragmentHotThreadRecyclerview.setAdapter(forumThreadAdapter);
        binding.fragmentHotThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        binding.fragmentHotThreadRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                if (binding.fragmentHotThreadRecyclerview.computeVerticalScrollExtent() + binding.fragmentHotThreadRecyclerview.computeVerticalScrollOffset()
                        >= binding.fragmentHotThreadRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });
    }

    private void configureSwipeRefreshLayout(){
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hotThreadsViewModel.setPageNumAndFetchThread(1);
            }
        });
    }

    private void getIntentInfo(){
        hotThreadsViewModel.setBBSInfo(bbsInfo,userBriefInfo);
    }

    private void bindVieModel(){
        hotThreadsViewModel.getThreadListLiveData().observe(getViewLifecycleOwner(), new Observer<List<ThreadInfo>>() {
            @Override
            public void onChanged(List<ThreadInfo> threadInfos) {

                forumThreadAdapter.setThreadInfoList(threadInfos,null);
                dashBoardViewModel.hotThreadCountMutableLiveData.postValue(forumThreadAdapter.getItemCount());
                if(forumThreadAdapter.threadInfoList == null || forumThreadAdapter.threadInfoList.size() == 0){
                    
                    binding.errorView.setVisibility(View.VISIBLE);
                    binding.errorContent.setText(R.string.empty_result);
                    binding.errorContent.setText(R.string.empty_hot_threads);
                    binding.errorIcon.setImageResource(R.drawable.ic_empty_hot_thread_64px);
                }
                else {
                    binding.errorView.setVisibility(View.GONE);
                    
                }
            }
        });
        hotThreadsViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });
        hotThreadsViewModel.errorMessageMutableLiveData.observe(getViewLifecycleOwner(),errorMessage -> {
            if(errorMessage!=null){
                binding.errorView.setVisibility(View.VISIBLE);
                binding.errorIcon.setImageResource(R.drawable.ic_error_outline_24px);
                binding.errorValue.setText(errorMessage.key);
                binding.errorContent.setText(errorMessage.content);
                
            }
        });
        
        hotThreadsViewModel.resultMutableLiveData.observe(getViewLifecycleOwner(), displayThreadsResult -> {
            if(getContext() instanceof BaseStatusInteract && displayThreadsResult!=null){
                ((BaseStatusInteract) getContext()).setBaseResult(displayThreadsResult,
                        displayThreadsResult.forumVariables);
            }
        });

    }

}