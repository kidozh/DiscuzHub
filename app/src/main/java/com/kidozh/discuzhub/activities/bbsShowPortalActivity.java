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
import com.kidozh.discuzhub.activities.ui.HotThreads.HotThreadsFragment;
import com.kidozh.discuzhub.activities.ui.home.HomeFragment;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.databinding.ActivityBbsShowPortalBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class bbsShowPortalActivity extends BaseStatusActivity
        implements bbsPrivateMessageFragment.OnNewMessageChangeListener,
        bbsPublicMessageFragment.OnNewMessageChangeListener,
        UserNotificationFragment.OnNewMessageChangeListener,
        NotificationsFragment.onPrivateMessageChangeListener{
    private static final String TAG = bbsShowPortalActivity.class.getSimpleName();

    HomeFragment homeFragment;
    HotThreadsFragment hotThreadsFragment;
    NotificationsFragment notificationsFragment;
    bbsParseUtils.noticeNumInfo noticeNumInfo;
    ActivityBbsShowPortalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBbsShowPortalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        hotThreadsFragment = (HotThreadsFragment) getSupportFragmentManager().getFragment(savedInstanceState,DASHBOARD_FRAGMENT_KEY);
    }

    private void configureActionBar(){

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }
    }

    public class anonymousViewPagerAdapter extends FragmentStatePagerAdapter{

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
                    if(hotThreadsFragment == null){
                        hotThreadsFragment = new HotThreadsFragment();
                    }
                    return hotThreadsFragment;
            }
            return new HomeFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public class userViewPagerAdapter extends FragmentStatePagerAdapter{

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
                    if(hotThreadsFragment == null){
                        hotThreadsFragment = new HotThreadsFragment();
                    }
                    return hotThreadsFragment;
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

        if(userBriefInfo == null){
            Log.d(TAG, "Current incognitive user "+userBriefInfo);
            binding.bbsPortalNavViewpager.setAdapter(new anonymousViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            binding.bbsPortalNavView.getMenu().clear();
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu);
        }
        else {
            // use fragment transaction instead
            Log.d(TAG, "Current incognitive user "+userBriefInfo.username);
            binding.bbsPortalNavViewpager.setAdapter(new userViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            binding.bbsPortalNavView.getMenu().clear();
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_nav_menu);
        }
        binding.bbsPortalNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:{
                        binding.bbsPortalNavViewpager.setCurrentItem(0);
                        break;
                    }
                    case R.id.navigation_dashboard:{
                        binding.bbsPortalNavViewpager.setCurrentItem(1);
                        break;
                    }
                    case R.id.navigation_notifications:{
                        binding.bbsPortalNavViewpager.setCurrentItem(2);
                        break;
                    }
                }
                return false;
            }
        });
        binding.bbsPortalNavViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        binding.bbsPortalNavView.getMenu().findItem(R.id.navigation_home).setChecked(true);
                        break;
                    case 1:
                        binding.bbsPortalNavView.getMenu().findItem(R.id.navigation_dashboard).setChecked(true);
                        break;
                    case 2:
                        binding.bbsPortalNavView.getMenu().findItem(R.id.navigation_notifications).setChecked(true);
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
        if(hotThreadsFragment !=null){
            getSupportFragmentManager().putFragment(outState,DASHBOARD_FRAGMENT_KEY, hotThreadsFragment);
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
        else if(hotThreadsFragment == null && fragment instanceof HotThreadsFragment){
            hotThreadsFragment = (HotThreadsFragment) fragment;
        }
        else if(notificationsFragment == null && fragment instanceof NotificationsFragment){
            notificationsFragment = (NotificationsFragment) fragment;
        }
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        URLUtils.setBBS(bbsInfo);
        if(bbsInfo == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(bbsInfo.site_name);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getNotificationInfo(){
        Request request = new Request.Builder()
                .url(URLUtils.getLoginSecondaryUrl())
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
        super.onOptionsItemSelected(item);
        String currentUrl = URLUtils.getPortalPageUrl();

        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return false;
        }
        else if(id == R.id.bbs_forum_nav_personal_center){
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            intent.putExtra("UID",userBriefInfo.uid);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.bbs_settings){
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.bbs_forum_nav_draft_box){
            Intent intent = new Intent(this, bbsShowThreadDraftActivity.class);
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.bbs_forum_nav_show_in_webview){
            Intent intent = new Intent(this, InternalWebViewActivity.class);
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            intent.putExtra(ConstUtils.PASS_URL_KEY,currentUrl);
            Log.d(TAG,"Inputted URL "+currentUrl);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.bbs_forum_nav_show_in_external_browser){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            Log.d(TAG,"Inputted URL "+currentUrl);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.bbs_about_app){
            Intent intent = new Intent(this, AboutAppActivity.class);
            startActivity(intent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getIntentInfo();
        if(userBriefInfo == null){
            getMenuInflater().inflate(R.menu.menu_incognitive_forum_nav_menu, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.bbs_forum_nav_menu,menu);
        }


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LiveData<Integer> draftNumberLiveData = bbsThreadDraftDatabase.getInstance(this).getbbsThreadDraftDao().getAllDraftsCount(bbsInfo.getId());
        draftNumberLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });
        return super.onPrepareOptionsMenu(menu);

    }


    public void setNewMessageNum(int i) {

        if(i == 0){
            if(binding.bbsPortalNavView.getBadge(R.id.navigation_notifications)!=null){
                binding.bbsPortalNavView.removeBadge(R.id.navigation_notifications);
            }


        }
        else {
            Log.d(TAG,"set notification num "+i);
            BadgeDrawable badgeDrawable = binding.bbsPortalNavView.getOrCreateBadge(R.id.navigation_notifications);
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
