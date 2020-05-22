package com.kidozh.discuzhub.activities.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.loginBBSActivity;
import com.kidozh.discuzhub.adapter.bbsPortalCategoryAdapter;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
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
    @BindView(R.id.bbs_portal_recyclerview)
    RecyclerView portalRecyclerView;
    @BindView(R.id.bbs_portal_error_text)
    TextView bbsPortalErrorText;
    @BindView(R.id.bbs_portal_progressBar)
    ProgressBar bbsPortalProgressbar;
    @BindView(R.id.bbs_portal_refresh_page)
    Button bbsPortalRefreshPageBtn;

    bbsPortalCategoryAdapter adapter;
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private forumUserBriefInfo userBriefInfo;

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

        return root;
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
            URLUtils.setBBS(curBBS);
            homeViewModel.setBBSInfo(curBBS,curUser);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }


    }

    private void bindLiveDataFromViewModel(){
        homeViewModel.getForumCategoryInfo().observe(getViewLifecycleOwner(), new Observer<List<BBSIndexResult.ForumCategory>>() {
            @Override
            public void onChanged(List<BBSIndexResult.ForumCategory> forumCategories) {
                if(homeViewModel.bbsIndexResultMutableLiveData.getValue() !=null){
                    List<ForumInfo> allForumInfo = homeViewModel.bbsIndexResultMutableLiveData.getValue().forumVariables.forumInfoList;
                    adapter.setForumCategoryList(forumCategories,allForumInfo);
                    bbsPortalProgressbar.setVisibility(View.GONE);
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
                    bbsPortalErrorText.setText(errorText);
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
                if(userBriefInfo == null && curUser!=null){
                    // raise dialog
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                    //MaterialAlertDialogBuilder builder =  new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.user_login_expired,curUser.username))
                            .setPositiveButton(getString(R.string.user_relogin, curUser.username), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getActivity(), loginBBSActivity.class);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
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
                    bbsPortalProgressbar.setVisibility(View.VISIBLE);
                }
                else {
                    bbsPortalProgressbar.setVisibility(View.GONE);
                }

            }
        });
    }


    private void configurePortalRecyclerview(){
        portalRecyclerView.setHasFixedSize(true);
        portalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new bbsPortalCategoryAdapter(getContext(),null,curBBS,userBriefInfo);
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