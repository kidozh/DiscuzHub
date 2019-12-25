package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class bbsShowPortalActivity extends AppCompatActivity
        implements bbsPrivateMessageFragment.OnNewMessageChangeListener,
        NotificationsFragment.onPrivateMessageChangeListener{
    private static final String TAG = bbsShowPortalActivity.class.getSimpleName();

    @BindView(R.id.bbs_portal_nav_view)
    BottomNavigationView navView;
    @BindView(R.id.bbs_portal_nav_host_fragment)
    View portalNavHostFragment;
    NotificationsFragment notificationsFragment;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_portal);
        ButterKnife.bind(this);
        getIntentInfo();
        configureBtmNavigation();
        configureActionBar();
        getNotificationInfo();
    }

    private void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }
    }

    private void configureBtmNavigation(){
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        if(curUser == null){
            Log.d(TAG, "Current incognitive user "+curUser);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard)
                    .build();
            // clear cognitive state
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_incognitive_nav_menu);
            NavController navController = Navigation.findNavController(this, R.id.bbs_portal_nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        }
        else {
            Log.d(TAG,"Current user "+curUser.uid);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.bbs_portal_nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        }
        notificationsFragment = (NotificationsFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_notifications);


    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
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

    private void getNotificationInfo(){
        Request request = new Request.Builder()
                .url(bbsURLUtils.getLoginApiUrl())
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    bbsParseUtils.noticeNumInfo noticeInfo = bbsParseUtils.parseNoticeInfo(s);
                    if(noticeInfo!=null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setNewMessageNum(noticeInfo.getAllNoticeInfo());
                            }
                        });
                    }



                }
            }
        });
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

    @Override
    public void setNewMessageNum(int i) {

        if(i == 0){
            if(navView.getBadge(R.id.navigation_notifications)!=null){
                navView.removeBadge(R.id.navigation_notifications);
            }


        }
        else {
            Log.d(TAG,"set notification num "+i);
            BadgeDrawable badgeDrawable = navView.getOrCreateBadge(R.id.navigation_notifications);
            badgeDrawable.setNumber(i);

        }
        if(notificationsFragment!=null){
            notificationsFragment.setNewMessageNum(i);
        }




    }



    @Override
    public void setPrivateMessageNum(int privateMessageNum) {

    }
}
