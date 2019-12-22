package com.kidozh.discuzhub.activities.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsPortalCategoryAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private HomeViewModel homeViewModel;
    @BindView(R.id.bbs_portal_recyclerview)
    RecyclerView portalRecyclerView;
    @BindView(R.id.bbs_portal_error_text)
    TextView bbsPortalErrorText;
    @BindView(R.id.bbs_portal_progressBar)
    ProgressBar bbsPortalProgressbar;
    bbsPortalCategoryAdapter adapter;
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private forumUserBriefInfo userBriefInfo;

    private OkHttpClient client = new OkHttpClient();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.activity_bbs_show_category_forum, container, false);
        ButterKnife.bind(this,root);
        getIntentInfo();
        configureClient();
        configurePortalRecyclerview();
        getPortalCategoryInfo();


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
            bbsURLUtils.setBBS(curBBS);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }
//        if(getSupportActionBar()!=null){
//            getSupportActionBar().setTitle(curBBS.site_name);
//        }


    }

    private void configureClient(){
        //client = networkUtils.getPreferredClient(this);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        Log.d(TAG,"Current user ");
    }

    private void configurePortalRecyclerview(){
        portalRecyclerView.setHasFixedSize(true);
        portalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new bbsPortalCategoryAdapter(getContext(),null,curBBS,userBriefInfo);
        portalRecyclerView.setAdapter(adapter);
    }

    private void getPortalCategoryInfo(){
        Request request = new Request.Builder()
                .url(bbsURLUtils.getBBSForumInfoApi())
                .build();
        Log.d(TAG,"Browse API " + bbsURLUtils.getBBSForumInfoApi());
        bbsPortalProgressbar.setVisibility(View.VISIBLE);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bbsPortalProgressbar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bbsPortalProgressbar.setVisibility(View.GONE);
                    }
                });
                if(response.isSuccessful() && response.body()!=null){

                    String s = response.body().string();
                    Log.d(TAG,"Fetch the information "+s);
                    List<forumCategorySection> categorySectionFidList = bbsParseUtils.parseCategoryFids(s);
                    if(categorySectionFidList!=null){
                        adapter.jsonString = s;
                        adapter.setmCateList(categorySectionFidList);


                        Log.d(TAG,"CATE:"+categorySectionFidList.size());
                    }
                    else {

                        String errorText = bbsParseUtils.parseErrorInformation(s);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bbsPortalErrorText.setVisibility(View.VISIBLE);
                                // setErrorActionBar();
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
                                }
                                else {
                                    Toasty.error(getActivity(),getString(R.string.parse_failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            }
        });
    }
}