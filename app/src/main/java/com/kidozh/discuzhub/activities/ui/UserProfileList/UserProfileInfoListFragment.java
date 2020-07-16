package com.kidozh.discuzhub.activities.ui.UserProfileList;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UserProfileItemAdapter;
import com.kidozh.discuzhub.entities.UserProfileItem;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileInfoListFragment extends Fragment {

    private UserProfileInfoListViewModel mViewModel;
    private String title = "";
    List<UserProfileItem> userProfileItemList = new ArrayList<>();

    @BindView(R.id.user_profile_info_title)
    TextView userProfileTitle;
    @BindView(R.id.user_profile_info_recyclerview)
    RecyclerView userProfileRecyclerview;
    @BindView(R.id.user_profile_info_empty_view)
    View userProfileEmptyView;

    UserProfileItemAdapter adapter;

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
        View view = inflater.inflate(R.layout.user_profile_info_list_fragment, container, false);
        ButterKnife.bind(this,view);
        return view;
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
        userProfileRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserProfileItemAdapter();
        userProfileRecyclerview.setAdapter(adapter);
    }

    private void bindViewModel(){
        mViewModel.titleMutableLivedata.setValue(title);
        mViewModel.userProfileListMutableLiveData.setValue(userProfileItemList);

        mViewModel.titleMutableLivedata.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                userProfileTitle.setText(s);
            }
        });

        mViewModel.userProfileListMutableLiveData.observe(getViewLifecycleOwner(), new Observer<List<UserProfileItem>>() {
            @Override
            public void onChanged(List<UserProfileItem> userProfileItems) {
                if(userProfileItems == null || userProfileItems.size() == 0){
                    userProfileEmptyView.setVisibility(View.VISIBLE);
                }
                else {
                    userProfileEmptyView.setVisibility(View.GONE);
                }
                adapter.setUserProfileItemList(userProfileItemList);
            }
        });
    }

}
