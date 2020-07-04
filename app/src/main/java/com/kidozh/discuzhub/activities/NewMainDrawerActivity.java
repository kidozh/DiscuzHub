package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment;
import com.kidozh.discuzhub.activities.ui.dashboard.DashboardFragment;
import com.kidozh.discuzhub.activities.ui.home.HomeFragment;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.viewModels.MainDrawerViewModel;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMainDrawerActivity extends BaseStatusActivity implements
        bbsPrivateMessageFragment.OnNewMessageChangeListener,
        bbsPublicMessageFragment.OnNewMessageChangeListener,
        UserNotificationFragment.OnNewMessageChangeListener,
        NotificationsFragment.onPrivateMessageChangeListener{
    private final static String TAG = NewMainDrawerActivity.class.getSimpleName();
    PrimaryDrawerItem bbsDrawerItem;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitleTextview;
    @BindView(R.id.toolbar_subtitle)
    TextView toolbarSubtitleTextview;
    @BindView(R.id.bbs_portal_nav_viewpager)
    ViewPager portalViewPager;
    @BindView(R.id.bbs_portal_nav_view)
    BottomNavigationView navView;

    MainDrawerViewModel viewModel;

    Drawer drawerResult;
    AccountHeader drawerAccountHeader;

    final int MODE_USER_IGCONGTIVE = -18510478;
    final int FUNC_ADD_A_BBS = -2;
    final int FUNC_MANAGE_BBS = -3;
    final int FUNC_ADD_AN_ACCOUNT = -4;
    final int FUNC_MANAGE_ACCOUNT = -5;
    final int FUNC_REGISTER_ACCOUNT = -6;
    final int FOOTER_SETTINGS = -955415674;
    final int FOOTER_ABOUT = -964245451;

    Bundle savedInstanceState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main_drawer);
        ButterKnife.bind(this);
        recoverInstanceState(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainDrawerViewModel.class);
        configureToolbar();
        initBBSDrawer();
        bindViewModel();
        initFragments();



    }

    private void configureToolbar(){
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    private void bindViewModel(){
        viewModel.allBBSInformationMutableLiveData.observe(this, new Observer<List<bbsInformation>>() {
            @Override
            public void onChanged(List<bbsInformation> bbsInformations) {
                drawerAccountHeader.clear();
                if(bbsInformations == null || bbsInformations.size() == 0){
                    // empty

                }
                else {

                    List<IProfile<?>> accountProfiles = new ArrayList<>();
                    for(int i=0;i<bbsInformations.size(); i++){
                        bbsInformation currentBBSInfo = bbsInformations.get(i);
                        //URLUtils.setBBS(currentBBSInfo);
                        Log.d(TAG,"Load url "+URLUtils.getBBSLogoUrl(currentBBSInfo.base_url));
                        ProfileDrawerItem bbsProfile = new ProfileDrawerItem()
                                .withName(currentBBSInfo.site_name)
                                .withNameShown(true)
                                .withIdentifier(currentBBSInfo.getId())
                                .withIcon(URLUtils.getBBSLogoUrl(currentBBSInfo.base_url))
                                .withEmail(currentBBSInfo.base_url);
                        accountProfiles.add(bbsProfile);

                    }
                    drawerAccountHeader.setProfiles(accountProfiles);
                    if(bbsInformations.size() > 0){
                        bbsInformation currentBBSInfo = bbsInformations.get(0);
                        drawerAccountHeader.setActiveProfile(currentBBSInfo.getId(),true);
                    }

                }
                drawerAccountHeader.addProfiles(
                        new ProfileSettingDrawerItem()
                                .withName(R.string.add_a_bbs)
                        .withIdentifier(FUNC_ADD_A_BBS)
                                .withDescription(R.string.title_add_a_forum_by_url)
                                .withSelectable(false)
                        .withIcon(R.drawable.ic_add_24px),
                        new ProfileSettingDrawerItem()
                                .withName(R.string.manage_bbs)
                                .withIdentifier(FUNC_MANAGE_BBS)
                                .withDescription(R.string.manage_bbs_description)
                                .withIcon(R.drawable.ic_manage_bbs_24px)
                                .withSelectable(false)
                );
            }
        });
        viewModel.forumUserListMutableLiveData.observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                // clear it first
                drawerResult.removeAllItems();
                Log.d(TAG,"get forumUsers "+forumUserBriefInfos);
                if(forumUserBriefInfos != null){

                    for(int i=0 ;i<forumUserBriefInfos.size();i++){
                        forumUserBriefInfo userBriefInfo = forumUserBriefInfos.get(i);
                        Log.d(TAG,"Getting user brief info "+userBriefInfo.username);
                        int uid = Integer.parseInt(userBriefInfo.uid);
                        int avatar_num = uid % 16;
                        if(avatar_num < 0){
                            avatar_num = -avatar_num;
                        }

                        int avatarResource = getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),
                                "drawable",getPackageName());

                        drawerResult.addItem(
                                new ProfileDrawerItem().withSelectable(true)
                                        .withName(userBriefInfo.username)
                                        .withIcon(userBriefInfo.avatarUrl)
                                        .withNameShown(true)
                                        //.withIcon(avatarResource)
                                        .withIdentifier(userBriefInfo.getId())
                                        .withEmail(getString(R.string.user_id_description,userBriefInfo.uid))

                        );
                    }
                    if(forumUserBriefInfos.size() > 0){
                        forumUserBriefInfo userBriefInfo = forumUserBriefInfos.get(0);
                        // get first
                        drawerResult.setSelection(userBriefInfo.getId(),true);
                    }

                }

                drawerResult.addItem(
                        new PrimaryDrawerItem().withSelectable(true)
                        .withName(R.string.bbs_anonymous)
                        .withIcon(R.drawable.ic_incognito_user_24px)
                        .withIdentifier(MODE_USER_IGCONGTIVE)
                        .withDescription(R.string.user_anonymous_description));

                drawerResult.addItems(
                        new DividerDrawerItem(),
                        new ProfileSettingDrawerItem().withSelectable(false)
                                .withName(R.string.add_a_account)
                                .withIcon(R.drawable.ic_person_add_24px)
                                .withIdentifier(FUNC_ADD_AN_ACCOUNT)
                                .withDescription(R.string.bbs_add_an_account_description),
                        new ProfileSettingDrawerItem().withSelectable(false)
                                .withName(R.string.register_an_account)
                                .withIcon(R.drawable.ic_register_account_24px)
                                .withIdentifier(FUNC_REGISTER_ACCOUNT)
                                .withDescription(R.string.register_an_account_description),
                        new ProfileSettingDrawerItem().withSelectable(false)
                                .withName(R.string.bbs_manage_users)
                                .withIcon(R.drawable.ic_manage_user_24px)
                                .withIdentifier(FUNC_MANAGE_ACCOUNT)
                                .withDescription(R.string.bbs_manage_users_description)
                );
                if(forumUserBriefInfos == null || forumUserBriefInfos.size() == 0){
                    Log.d(TAG,"Trigger igcontive mode");
                    drawerResult.setSelection(MODE_USER_IGCONGTIVE,true);
                }


            }
        });
        Context context = this;
        viewModel.currentBBSInformationMutableLiveData.observe(this, new Observer<bbsInformation>() {
            @Override
            public void onChanged(bbsInformation bbsInformation) {
                if(bbsInformation != null){
                    toolbarTitleTextview.setText(bbsInformation.site_name);
                    if(getSupportActionBar() !=null){
                        getSupportActionBar().setTitle(bbsInformation.site_name);
                    }

                    int id = bbsInformation.getId();
                    LiveData<List<forumUserBriefInfo>> allUsersInCurrentBBSLiveData = forumUserBriefInfoDatabase.getInstance(getApplication())
                            .getforumUserBriefInfoDao()
                            .getAllUserByBBSID(id);

                    allUsersInCurrentBBSLiveData.observe((LifecycleOwner) context, new Observer<List<forumUserBriefInfo>>() {
                        @Override
                        public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                            Log.d(TAG,"Updating "+id+ " users information "+forumUserBriefInfos.size());
                            viewModel.forumUserListMutableLiveData.postValue(forumUserBriefInfos);
                        }
                    });

                }

            }
        });
        viewModel.currentForumUserBriefInfoMutableLiveData.observe(this, new Observer<forumUserBriefInfo>() {
            @Override
            public void onChanged(forumUserBriefInfo forumUserBriefInfo) {
                if(forumUserBriefInfo == null){
                    toolbarSubtitleTextview.setText(R.string.bbs_anonymous);


                }
                else {
                    toolbarSubtitleTextview.setText(forumUserBriefInfo.username);
                }
                renderViewPageAndBtmView();
            }
        });
    }


    private void initBBSDrawer(){
        Activity activity = this;

        // account header
        drawerAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        int bbsId = (int) profile.getIdentifier();
                        Log.d(TAG,"profile changed "+bbsId+ " name " + profile.getName());
                        if(bbsId > 0){
                            // change view model
                            List<bbsInformation> allBBSList = viewModel.allBBSInformationMutableLiveData.getValue();
                            if(allBBSList !=null && allBBSList.size()>0){
                                for(int i=0 ;i< allBBSList.size(); i++){
                                    bbsInformation curBBS = allBBSList.get(i);
                                    if(curBBS.getId() == bbsId){
                                        viewModel.currentBBSInformationMutableLiveData.setValue(curBBS);
                                        return false;
                                    }
                                }

                            }
                        }
                        else {
                            switch (bbsId){
                                case FUNC_ADD_A_BBS:{
                                    Intent intent = new Intent(activity, bbsAddIntroActivity.class);
                                    startActivity(intent);
                                    return true;
                                }
                            }
                        }


                        return false;
                    }
                })
                .build();




        Context context = this;
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerAccountHeader)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int id = (int) drawerItem.getIdentifier();
                        Log.d(TAG,"Drawer id "+id);
                        if(id > 0){
                            List<forumUserBriefInfo> userBriefInfos = viewModel.forumUserListMutableLiveData.getValue();
                            if(userBriefInfos == null){
                                viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null);
                                return false;
                            }
                            for(int i=0;i<userBriefInfos.size();i++){
                                forumUserBriefInfo userBriefInfo = userBriefInfos.get(i);
                                int userId = userBriefInfo.getId();
                                if(userId == id){
                                    viewModel.currentForumUserBriefInfoMutableLiveData.postValue(userBriefInfo);
                                    return false;
                                }
                            }
                            // not an account
                            viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null);
                            return false;
                        }
                        else {
                            switch (id){
                                case (MODE_USER_IGCONGTIVE):{
                                    viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null);
                                    return false;

                                }
                                case (FUNC_ADD_AN_ACCOUNT):{

                                    Intent intent = new Intent(context, LoginActivity.class);

                                    bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                                    Log.d(TAG,"ADD A account "+forumInfo);
                                    if(forumInfo !=null){
                                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);

                                        Bundle bundle = options.toBundle();
                                        context.startActivity(intent,bundle);
                                    }


                                    return true;
                                }
                                case (FUNC_REGISTER_ACCOUNT):{
                                    bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                                    if(forumInfo!=null){
                                        new MaterialAlertDialogBuilder(context)
                                                .setTitle(context.getString(R.string.bbs_register_an_account)+" "+forumInfo.site_name)
                                                //setMessage是用来显示字符串的
                                                .setMessage(R.string.bbs_register_account_notification)
                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        URLUtils.setBBS(forumInfo);
                                                        Uri uri = Uri.parse(URLUtils.getBBSRegisterUrl(forumInfo.register_name));
                                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                        context.startActivity(intent);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                        return true;
                                    }

                                }
                                case (FOOTER_ABOUT):{
                                    Intent intent = new Intent(activity,aboutAppActivity.class);
                                    startActivity(intent);
                                    return true;
                                }
                                case (FOOTER_SETTINGS):{
                                    Intent intent = new Intent(activity,SettingsActivity.class);
                                    startActivity(intent);
                                    return true;
                                }

                            }
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        drawerResult.addStickyFooterItem(
                new PrimaryDrawerItem()
                .withName(getString(R.string.action_settings))
                        .withIcon(R.drawable.ic_settings_24px)
                        .withIdentifier(FOOTER_SETTINGS)
                .withSelectable(false));
        drawerResult.addStickyFooterItem(
                new PrimaryDrawerItem()
                .withName(R.string.bbs_app_about)
                        .withIdentifier(FOOTER_ABOUT)
                        .withIcon(R.drawable.ic_info_24px)
                .withSelectable(false));



        DrawerImageLoader.Companion.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                super.set(imageView, uri, placeholder, tag);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(getApplication()));
                Glide.get(getApplication()).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
                Glide.with(getApplication())
                        .load(uri)
                        .centerInside()
                        .into(imageView);
            }
        });


    }

    // fragment adapter
    public class anonymousViewPagerAdapter extends FragmentStatePagerAdapter{

        public anonymousViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            bbsInformation bbsInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
            forumUserBriefInfo userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();
            switch (position){
                case 0:
                    homeFragment = new HomeFragment(bbsInfo,userBriefInfo);

                    return homeFragment;
                case 1:
                    DashboardFragment dashboardFragment = new DashboardFragment(bbsInfo,userBriefInfo);
                    return dashboardFragment;
            }
            return new HomeFragment(bbsInfo,userBriefInfo);
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
            bbsInformation bbsInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
            forumUserBriefInfo userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();
            switch (position){
                case 0:
                    homeFragment = new HomeFragment(bbsInfo, userBriefInfo);
                    return homeFragment;
                case 1:
                    dashboardFragment = new DashboardFragment(bbsInfo, userBriefInfo);
                    return dashboardFragment;
                case 2:
                    notificationsFragment = new NotificationsFragment(bbsInfo, userBriefInfo);
//                    if(noticeNumInfo !=null){
//                        notificationsFragment.renderTabNumber(noticeNumInfo);
//                    }
                    return notificationsFragment;
            }
            return new HomeFragment(bbsInfo,userBriefInfo);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    // BBS Render variables

    HomeFragment homeFragment;
    DashboardFragment dashboardFragment;
    NotificationsFragment notificationsFragment;
    final String HOME_FRAGMENT_KEY = "HOME_FRAGMENT_KEY",
            DASHBOARD_FRAGMENT_KEY = "DASHBOARD_FRAGMENT_KEY",
            NOTIFICATION_FRAGMENT_KEY = "NOTIFICATION_FRAGMENT_KEY";


    private void initFragments(){
//        homeFragment = (HomeFragment) getSupportFragmentManager().getFragment(savedInstanceState,HOME_FRAGMENT_KEY);
//        notificationsFragment = (NotificationsFragment) getSupportFragmentManager().getFragment(savedInstanceState,NOTIFICATION_FRAGMENT_KEY);
//        dashboardFragment = (DashboardFragment) getSupportFragmentManager().getFragment(savedInstanceState,DASHBOARD_FRAGMENT_KEY);
    }


    private void renderViewPageAndBtmView(){
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // detecting current bbs
        bbsInformation bbsInformation = viewModel.currentBBSInformationMutableLiveData.getValue();
        if(bbsInformation == null){
            return;
        }

        forumUserBriefInfo curUser = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();

        if(curUser == null){
            Log.d(TAG, "Current incognitive user "+curUser);
            portalViewPager.setAdapter(new anonymousViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
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

    // fragment lifecyle

    private void recoverInstanceState(Bundle savedInstanceState){
        if(savedInstanceState == null){
            return;
        }
        homeFragment = (HomeFragment) getSupportFragmentManager().getFragment(savedInstanceState,HOME_FRAGMENT_KEY);
        notificationsFragment = (NotificationsFragment) getSupportFragmentManager().getFragment(savedInstanceState,NOTIFICATION_FRAGMENT_KEY);
        dashboardFragment = (DashboardFragment) getSupportFragmentManager().getFragment(savedInstanceState,DASHBOARD_FRAGMENT_KEY);
    }

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

    // listener

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
        //noticeNumInfo = notificationsNum;
        Log.d(TAG,"notification number "+notificationsNum.getAllNoticeInfo());
        if(notificationsFragment!=null){
            notificationsFragment.renderTabNumber(notificationsNum);
        }

        setNewMessageNum(notificationsNum.getAllNoticeInfo());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        Log.d(TAG,"You pressed id "+id);
        switch (id){

            case R.id.bbs_settings:{
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.bbs_about_app:{
                Intent intent = new Intent(this,aboutAppActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}