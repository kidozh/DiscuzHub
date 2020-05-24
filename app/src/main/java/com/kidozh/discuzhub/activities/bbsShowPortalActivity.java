package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment;
import com.kidozh.discuzhub.activities.ui.dashboard.DashboardFragment;
import com.kidozh.discuzhub.activities.ui.home.HomeFragment;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.viewpager.widget.ViewPager;

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
        bbsPublicMessageFragment.OnNewMessageChangeListener,
        UserNotificationFragment.OnNewMessageChangeListener,
        NotificationsFragment.onPrivateMessageChangeListener{
    private static final String TAG = bbsShowPortalActivity.class.getSimpleName();

    @BindView(R.id.bbs_portal_nav_view)
    BottomNavigationView navView;
    @BindView(R.id.bbs_portal_nav_viewpager)
    ViewPager portalViewPager;
    HomeFragment homeFragment;
    DashboardFragment dashboardFragment;
    NotificationsFragment notificationsFragment;
    bbsParseUtils.noticeNumInfo noticeNumInfo;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_portal);
        ButterKnife.bind(this);
        recoverInstanceState(savedInstanceState);
        getIntentInfo();
        configureBtmNavigation();
        configureActionBar();
        getNotificationInfo();

    }

    private void recoverInstanceState(Bundle savedInstanceState){
        if(savedInstanceState == null){
            return;
        }
        homeFragment = (HomeFragment) getSupportFragmentManager().getFragment(savedInstanceState,HOME_FRAGMENT_KEY);
        notificationsFragment = (NotificationsFragment) getSupportFragmentManager().getFragment(savedInstanceState,NOTIFICATION_FRAGMENT_KEY);
        dashboardFragment = (DashboardFragment) getSupportFragmentManager().getFragment(savedInstanceState,DASHBOARD_FRAGMENT_KEY);
    }

    private void configureActionBar(){

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }
    }

    private class anonymousViewPagerAdapter extends FragmentStatePagerAdapter{

        public anonymousViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    if(homeFragment == null){
                        homeFragment = new HomeFragment();
                    }
                    return homeFragment;
                case 1:
                    if(dashboardFragment == null){
                        dashboardFragment = new DashboardFragment();
                    }
                    return dashboardFragment;
            }
            return new HomeFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private class userViewPagerAdapter extends FragmentStatePagerAdapter{

        public userViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    if(homeFragment == null){
                        homeFragment = new HomeFragment();
                    }
                    return homeFragment;
                case 1:
                    if(dashboardFragment == null){
                        dashboardFragment = new DashboardFragment();
                    }
                    return dashboardFragment;
                case 2:
                    if(notificationsFragment == null){
                        notificationsFragment = new NotificationsFragment();
                    }
                    if(noticeNumInfo !=null){
                        notificationsFragment.renderTabNumber(noticeNumInfo);
                    }
                    return notificationsFragment;
            }
            return new HomeFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void configureBtmNavigation(){
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        if(curUser == null){
            Log.d(TAG, "Current incognitive user "+curUser);
            portalViewPager.setAdapter(new anonymousViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_incognitive_nav_menu);
        }
        else {
            // use fragment transaction instead
            Log.d(TAG, "Current incognitive user "+curUser.username);
            portalViewPager.setAdapter(new userViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_nav_menu);
        }
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:{
                        portalViewPager.setCurrentItem(0);
                        break;
                    }
                    case R.id.navigation_dashboard:{
                        portalViewPager.setCurrentItem(1);
                        break;
                    }
                    case R.id.navigation_notifications:{
                        portalViewPager.setCurrentItem(2);
                        break;
                    }
                }
                return false;
            }
        });
        portalViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        navView.getMenu().findItem(R.id.navigation_home).setChecked(true);
                        break;
                    case 1:
                        navView.getMenu().findItem(R.id.navigation_dashboard).setChecked(true);
                        break;
                    case 2:
                        navView.getMenu().findItem(R.id.navigation_notifications).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    String HOME_FRAGMENT_KEY = "HOME_FRAGMENT_KEY", DASHBOARD_FRAGMENT_KEY = "DASHBOARD_FRAGMENT_KEY", NOTIFICATION_FRAGMENT_KEY = "NOTIFICATION_FRAGMENT_KEY";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if(homeFragment !=null){
            getSupportFragmentManager().putFragment(outState,HOME_FRAGMENT_KEY,homeFragment);
        }
        if(dashboardFragment !=null){
            getSupportFragmentManager().putFragment(outState,DASHBOARD_FRAGMENT_KEY,dashboardFragment);
        }
        if(notificationsFragment!=null){
            getSupportFragmentManager().putFragment(outState,NOTIFICATION_FRAGMENT_KEY,notificationsFragment);
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if(homeFragment == null && fragment instanceof HomeFragment){
            homeFragment = (HomeFragment) fragment;
        }
        else if(dashboardFragment == null && fragment instanceof DashboardFragment){
            dashboardFragment = (DashboardFragment) fragment;
        }
        else if(notificationsFragment == null && fragment instanceof NotificationsFragment){
            notificationsFragment = (NotificationsFragment) fragment;
        }
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        URLUtils.setBBS(curBBS);
        if(curBBS == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            URLUtils.setBBS(curBBS);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(curBBS.site_name);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getNotificationInfo(){
        Request request = new Request.Builder()
                .url(URLUtils.getLoginApiUrl())
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
                    bbsParseUtils.noticeNumInfo noticeNumInfo = bbsParseUtils.parseNoticeInfo(s);
                    setNotificationsNum(noticeNumInfo);

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
        String currentUrl = URLUtils.getPortalPageUrl();

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:{
                this.finishAfterTransition();
                return false;
            }
            case R.id.bbs_forum_nav_personal_center:{
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                intent.putExtra("UID",String.valueOf(curUser.uid));
                startActivity(intent);
                return true;
            }
            case R.id.bbs_settings:{
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_forum_nav_draft_box:{
                Intent intent = new Intent(this, bbsShowThreadDraftActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_forum_nav_show_in_webview:{
                Intent intent = new Intent(this, showWebPageActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_URL_KEY,currentUrl);
                Log.d(TAG,"Inputted URL "+currentUrl);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_forum_nav_show_in_external_browser:{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
                Log.d(TAG,"Inputted URL "+currentUrl);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_about_app:{
                Intent intent = new Intent(this,aboutAppActivity.class);
                startActivity(intent);
                return true;
            }

            default:{
                return super.onOptionsItemSelected(item);
            }
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
            getMenuInflater().inflate(R.menu.bbs_forum_nav_menu,menu);
        }


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LiveData<Integer> draftNumberLiveData = bbsThreadDraftDatabase.getInstance(this).getbbsThreadDraftDao().getAllDraftsCount(curBBS.getId());
        draftNumberLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });
        return super.onPrepareOptionsMenu(menu);

    }


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

    @Override
    public void setNotificationsNum(bbsParseUtils.noticeNumInfo notificationsNum) {
        Log.d(TAG,"Notification fragment "+notificationsFragment+" notification "+notificationsNum);
        if(notificationsNum == null){
            return;
        }
        noticeNumInfo = notificationsNum;
        Log.d(TAG,"notification number "+notificationsNum.getAllNoticeInfo());
        if(notificationsFragment!=null){
            notificationsFragment.renderTabNumber(notificationsNum);
        }

        setNewMessageNum(notificationsNum.getAllNoticeInfo());
    }
}
