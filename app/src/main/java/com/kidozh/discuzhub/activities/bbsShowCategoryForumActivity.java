package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsPortalCategoryAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class bbsShowCategoryForumActivity extends AppCompatActivity {
    private static String TAG = bbsShowCategoryForumActivity.class.getSimpleName();
    @BindView(R.id.bbs_portal_recyclerview)
    RecyclerView portalRecyclerView;
    @BindView(R.id.bbs_portal_error_text)
    TextView bbsPortalErrorText;
    @BindView(R.id.bbs_portal_progressBar)
    ProgressBar bbsPortalProgressbar;

    private OkHttpClient client = new OkHttpClient();
    bbsPortalCategoryAdapter adapter;
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private forumUserBriefInfo userBriefInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_category_forum);
        ButterKnife.bind(this);
        configureActionBar();
        getIntentInfo();
        configureClient();

        configurePortalRecyclerview();
        new getPortalInfoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }

    }

    private void setErrorActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorUnSafeStatus)));
            Window window = getWindow();
            //取消状态栏透明
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getColor(R.color.colorUnSafeStatus));
            //设置系统状态栏处于可见状态
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            window.setNavigationBarColor(getColor(R.color.colorUnSafeStatus));



        }

    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(curBBS == null){
            finish();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            bbsURLUtils.setBBS(curBBS);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(curBBS.site_name);
        }


    }



    private void configureClient(){
        //client = networkUtils.getPreferredClient(this);
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        Log.d(TAG,"Current user "+userBriefInfo);
    }


    private void configurePortalRecyclerview(){
        portalRecyclerView.setHasFixedSize(true);
        portalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new bbsPortalCategoryAdapter(this,null,curBBS,userBriefInfo);
        portalRecyclerView.setAdapter(adapter);
    }

    private class getPortalInfoTask extends AsyncTask<Void,Void,String>{
        Context mContext;
        Request request;

        getPortalInfoTask(Context context){
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            request = new Request.Builder()
                    .url(bbsURLUtils.getBBSForumInfoApi())
                    .build();
            Log.d(TAG,"API ->"+bbsURLUtils.getBBSForumInfoApi());
            bbsPortalProgressbar.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                Response resp = client.newCall(request).execute();
                if(resp.isSuccessful() && resp.body()!=null){
                    return resp.body().string();
                }
                else {
                    return null;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //Log.e(TAG,e.printStackTrace());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bbsPortalProgressbar.setVisibility(View.GONE);
            Log.d(TAG,"Info:"+s);
            if(s == null){
                // Make notifications
                Toasty.error(mContext,getString(R.string.network_failed), Toast.LENGTH_LONG).show();

            }
            else {

                List<forumCategorySection> categorySectionFidList = bbsParseUtils.parseCategoryFids(s);
                if(categorySectionFidList!=null){
                    adapter.jsonString = s;
                    adapter.setmCateList(categorySectionFidList);
                    bbsPortalErrorText.setVisibility(View.GONE);
                    Log.d(TAG,"CATE:"+categorySectionFidList.size());
                }
                else {
                    Log.d(TAG,"Failed to parse information "+s);
                    String errorText = bbsParseUtils.parseErrorInformation(s);
                    bbsPortalErrorText.setVisibility(View.VISIBLE);
                    setErrorActionBar();
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
                        Toasty.error(mContext,getString(R.string.parse_failed),Toast.LENGTH_SHORT).show();
                    }
                }

            }


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finish();
            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getIntentInfo();
        if(curUser == null){
            getMenuInflater().inflate(R.menu.menu_bbs_user_status, menu);
        }
        else {

        }


        return true;
    }

}
