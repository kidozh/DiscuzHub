package com.kidozh.discuzhub.activities.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.LoginActivity;
import com.kidozh.discuzhub.adapter.ForumCategoryAdapter;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private HomeViewModel homeViewModel;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.bbs_portal_recyclerview)
    RecyclerView portalRecyclerView;
    @BindView(R.id.bbs_portal_error_text)
    TextView bbsPortalErrorText;
    @BindView(R.id.bbs_portal_refresh_page)
    Button bbsPortalRefreshPageBtn;

    ForumCategoryAdapter adapter;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    private OkHttpClient client = new OkHttpClient();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.activity_bbs_show_category_forum, container, false);
        ButterKnife.bind(this,root);
        getIntentInfo();

        configurePortalRecyclerview();
        bindLiveDataFromViewModel();
        //getPortalCategoryInfo();
        configureRefreshBtn();
        configureSwipeRefreshLayout();

        return root;
    }

    public HomeFragment(){

    }

    public static HomeFragment newInstance(bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
        args.putSerializable(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);

        }
    }

    private void getIntentInfo(){

        if(bbsInfo != null){
            homeViewModel.setBBSInfo(bbsInfo,userBriefInfo);
            return;
        }
        Intent intent = getActivity().getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(bbsInfo == null){
            getActivity().finish();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            homeViewModel.setBBSInfo(bbsInfo,userBriefInfo);
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }


    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeViewModel.loadForumCategoryInfo();
            }
        });
    }

    private void bindLiveDataFromViewModel(){
        homeViewModel.getForumCategoryInfo().observe(getViewLifecycleOwner(), new Observer<List<BBSIndexResult.ForumCategory>>() {
            @Override
            public void onChanged(List<BBSIndexResult.ForumCategory> forumCategories) {
                if(homeViewModel.bbsIndexResultMutableLiveData.getValue() !=null &&
                        homeViewModel.bbsIndexResultMutableLiveData.getValue().forumVariables !=null){
                    List<ForumInfo> allForumInfo = homeViewModel.bbsIndexResultMutableLiveData.getValue().forumVariables.forumInfoList;
                    adapter.setForumCategoryList(forumCategories,allForumInfo);
                }

            }
        });
        homeViewModel.errorText.observe(getViewLifecycleOwner(), errorText -> {
            if(errorText!=null){

                if(errorText.equals("mobile_is_closed")){
                    bbsPortalErrorText.setText(R.string.bbs_mobile_is_closed);
                }
                else if(errorText.equals("user_banned")){
                    bbsPortalErrorText.setText(R.string.bbs_user_banned);
                }
                else {

                    bbsPortalErrorText.setText(Html.fromHtml(errorText), TextView.BufferType.SPANNABLE);

                }
                bbsPortalErrorText.setVisibility(View.VISIBLE);
                bbsPortalRefreshPageBtn.setVisibility(View.VISIBLE);
            }
            else {
                bbsPortalErrorText.setVisibility(View.GONE);
                bbsPortalRefreshPageBtn.setVisibility(View.GONE);
            }
        });
        homeViewModel.userBriefInfoMutableLiveData.observe(getViewLifecycleOwner(), new Observer<forumUserBriefInfo>() {
            @Override
            public void onChanged(forumUserBriefInfo userBriefInfo) {
                Log.d(TAG,"changed user to "+userBriefInfo);
                if(userBriefInfo == null && userBriefInfo!=null){
                    // raise dialog
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                    //MaterialAlertDialogBuilder builder =  new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.user_login_expired,userBriefInfo.username))
                            .setPositiveButton(getString(R.string.user_relogin, userBriefInfo.username), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();
                }
            }
        });
        homeViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    swipeRefreshLayout.setRefreshing(true);
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
        homeViewModel.bbsIndexResultMutableLiveData.observe(getViewLifecycleOwner(),bbsIndexResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(bbsIndexResult,
                        bbsIndexResult!=null?bbsIndexResult.forumVariables:null);
            }
        });
    }


    private void configurePortalRecyclerview(){
        portalRecyclerView.setHasFixedSize(true);
        portalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ForumCategoryAdapter(getContext(),null,bbsInfo,userBriefInfo);
        portalRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        portalRecyclerView.setAdapter(adapter);
    }

    private void configureRefreshBtn(){
        bbsPortalRefreshPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.loadForumCategoryInfo();
            }
        });
    }
}