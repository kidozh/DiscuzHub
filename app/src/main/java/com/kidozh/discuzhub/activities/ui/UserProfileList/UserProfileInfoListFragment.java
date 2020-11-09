package com.kidozh.discuzhub.activities.ui.UserProfileList;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UserProfileItemAdapter;
import com.kidozh.discuzhub.databinding.UserProfileInfoListFragmentBinding;
import com.kidozh.discuzhub.entities.UserProfileItem;
import com.kidozh.discuzhub.utilities.AnimationUtils;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;


public class UserProfileInfoListFragment extends Fragment {

    private UserProfileInfoListViewModel mViewModel;
    private String title = "";
    List<UserProfileItem> userProfileItemList = new ArrayList<>();


    UserProfileItemAdapter adapter;
    UserProfileInfoListFragmentBinding binding;

    public UserProfileInfoListFragment(){

    }



    public static UserProfileInfoListFragment newInstance(String title, List<UserProfileItem> userProfileItemList) {
        return new UserProfileInfoListFragment(title,userProfileItemList);
    }

    public UserProfileInfoListFragment(String title, List<UserProfileItem> userProfileItemList){
        this.title = title;
        this.userProfileItemList = userProfileItemList;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserProfileInfoListFragmentBinding.inflate(inflater,container,false);
        
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserProfileInfoListViewModel.class);
        // TODO: Use the ViewModel
        configureRecyclerview();
        bindViewModel();
    }

    private void configureRecyclerview(){
        binding.userProfileInfoRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserProfileItemAdapter();
        binding.userProfileInfoRecyclerview.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        binding.userProfileInfoRecyclerview.addItemDecoration(dividerItemDecoration);
        binding.userProfileInfoRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
    }

    private void bindViewModel(){
        mViewModel.titleMutableLivedata.setValue(title);
        mViewModel.userProfileListMutableLiveData.setValue(userProfileItemList);

        mViewModel.titleMutableLivedata.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.userProfileInfoTitle.setText(s);
            }
        });

        mViewModel.userProfileListMutableLiveData.observe(getViewLifecycleOwner(), new Observer<List<UserProfileItem>>() {
            @Override
            public void onChanged(List<UserProfileItem> userProfileItems) {
                if(userProfileItems == null || userProfileItems.size() == 0){
                    binding.userProfileInfoEmptyView.setVisibility(View.VISIBLE);
                }
                else {
                    binding.userProfileInfoEmptyView.setVisibility(View.GONE);
                }
                adapter.setUserProfileItemList(userProfileItemList);
            }
        });
    }

}
