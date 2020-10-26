package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.BlankBBSFragment.BlankBBSFragment;
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardFragment;
import com.kidozh.discuzhub.activities.ui.HotThreads.HotThreadsFragment;
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment;
import com.kidozh.discuzhub.activities.ui.home.HomeFragment;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.databinding.ActivityNewMainDrawerBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;
import com.kidozh.discuzhub.viewModels.MainDrawerViewModel;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.widget.AccountHeaderView;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;
import com.mikepenz.materialdrawer.util.MaterialDrawerSliderViewExtensionsKt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import kotlin.jvm.functions.Function3;

public class DrawerActivity extends BaseStatusActivity implements
        bbsPrivateMessageFragment.OnNewMessageChangeListener,
        bbsPublicMessageFragment.OnNewMessageChangeListener,
        UserNotificationFragment.OnNewMessageChangeListener,
        NotificationsFragment.onPrivateMessageChangeListener{
    private final static String TAG = DrawerActivity.class.getSimpleName();
    PrimaryDrawerItem bbsDrawerItem;


    

    MainDrawerViewModel viewModel;

//    Drawer drawerResult;
//    AccountHeader drawerAccountHeader;

    final int MODE_USER_IGCONGTIVE = -18510478;
    final int FUNC_ADD_A_BBS = -2;
    final int FUNC_MANAGE_BBS = -3;
    final int FUNC_ADD_AN_ACCOUNT = -4;
    final int FUNC_MANAGE_ACCOUNT = -5;
    final int FUNC_REGISTER_ACCOUNT = -6;
    final int FOOTER_SETTINGS = -955415674;
    final int FOOTER_ABOUT = -964245451;
    final int FUNC_VIEW_HISTORY = -85642154;

    Bundle savedInstanceState;

    


    AccountHeaderView headerView;
    ActivityNewMainDrawerBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        binding = ActivityNewMainDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recoverInstanceState(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainDrawerViewModel.class);
        configureToolbar();
        initBBSDrawer();
        bindViewModel();
        initFragments();
        checkTermOfUse();
    }

    private void checkTermOfUse(){
        Intent intent = new Intent(this, SplashScreenActivity.class);
        startActivity(intent);
    }

    private void configureToolbar(){
        setSupportActionBar(binding.toolbar);

        if(getSupportActionBar()!=null){
            //getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        binding.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_24px));


    }

    private void bindViewModel(){

        viewModel.allBBSInformationMutableLiveData.observe(this, bbsInformations -> {
            //binding.materialDrawerSliderView.getItemAdapter().clear();

            headerView.clear();
            //drawerAccountHeader.clear();
            if(bbsInformations == null || bbsInformations.size() == 0){
                // empty
                // show bbs page

            }
            else {
                // bind to headview

                List<IProfile> accountProfiles = new ArrayList<>();
                for(int i=0;i<bbsInformations.size(); i++){
                    bbsInformation currentBBSInfo = bbsInformations.get(i);
                    notificationUtils.createUsersUpdateChannel(getApplicationContext());
                    //URLUtils.setBBS(currentBBSInfo);
                    Log.d(TAG,"Load url "+URLUtils.getBBSLogoUrl(currentBBSInfo.base_url));
                    ProfileDrawerItem bbsProfile = new ProfileDrawerItem();

                    bbsProfile.setName(new StringHolder(currentBBSInfo.site_name));
                    bbsProfile.setNameShown(true);
                    bbsProfile.setIdentifier(currentBBSInfo.getId());
                    bbsProfile.setIcon(new ImageHolder(URLUtils.getBBSLogoUrl(currentBBSInfo.base_url)));
                    bbsProfile.setDescription(new StringHolder(currentBBSInfo.base_url));
                    if(currentBBSInfo.getAPIVersion() > 4){
                        // marked as advanced
                        bbsProfile.setBadge(new StringHolder(
                                getString(R.string.bbs_api_advance)
                        ));
                        BadgeStyle badgeStyle = new BadgeStyle();

                        badgeStyle.setBadgeBackground(getDrawable(R.color.colorAPI5BadgeBackgroundColor));
                        ColorHolder colorHolder= new ColorHolder();
                        colorHolder.setColorRes$materialdrawer(R.color.colorPureWhite);
                        badgeStyle.setTextColor(colorHolder);

                        bbsProfile.setBadgeStyle(badgeStyle);
                    }


                    accountProfiles.add(bbsProfile);

                }
                headerView.setProfiles(accountProfiles);
                // drawerAccountHeader.setProfiles(accountProfiles);
                if(bbsInformations.size() > 0){

                    int activeIdentifier = UserPreferenceUtils.getLastSelectedDrawerItemIdentifier(this);
                    if(activeIdentifier >= 0){
                        headerView.setActiveProfile(activeIdentifier,true);
                    }
                    else {
                        bbsInformation currentBBSInfo = bbsInformations.get(0);
                        headerView.setActiveProfile(currentBBSInfo.getId(),true);
                    }

                }

            }
            // add bbs
            ProfileSettingDrawerItem addBBSProfile = new ProfileSettingDrawerItem();
            addBBSProfile.setName(new StringHolder(getString(R.string.add_a_bbs)));
            addBBSProfile.setIdentifier(FUNC_ADD_A_BBS);
            addBBSProfile.setDescription(new StringHolder(getString(R.string.title_add_a_forum_by_url)));
            addBBSProfile.setSelectable(false);
            //addBBSProfile.setNameShown(true);
            addBBSProfile.setIcon(new ImageHolder(R.drawable.ic_add_24px));
            headerView.addProfiles(addBBSProfile);
            Log.d(TAG,"Add a bbs profile");
            // manage bbs
            if(bbsInformations !=null && bbsInformations.size() > 0){
                ProfileSettingDrawerItem manageBBSProfile = new ProfileSettingDrawerItem();
                manageBBSProfile.setName(new StringHolder(getString(R.string.manage_bbs)));
                manageBBSProfile.setIdentifier(FUNC_MANAGE_BBS);
                manageBBSProfile.setDescription(new StringHolder(getString(R.string.manage_bbs_description)));
                manageBBSProfile.setSelectable(false);
                //manageBBSProfile.setNameShown(true);
                manageBBSProfile.setIcon(new ImageHolder(R.drawable.ic_manage_bbs_24px));
                headerView.addProfiles(manageBBSProfile);
            }


        });
        viewModel.forumUserListMutableLiveData.observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                // clear it first
                // drawerResult.removeAllItems();
                binding.materialDrawerSliderView.getItemAdapter().clear();
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
                        ProfileDrawerItem userProfile = new ProfileDrawerItem();

                        userProfile.setSelectable(true);
                        userProfile.setName(new StringHolder(userBriefInfo.username));
                        if(viewModel!=null){
                            URLUtils.setBBS(viewModel.currentBBSInformationMutableLiveData.getValue());
                        }

                        userProfile.setIcon(new ImageHolder(URLUtils.getDefaultAvatarUrlByUid(userBriefInfo.uid)));
//                        userProfile.setIcon(new ImageHolder(userBriefInfo.avatarUrl));
                        userProfile.setNameShown(true);
                        userProfile.setIdentifier(userBriefInfo.getId());
                        userProfile.setDescription(new StringHolder(getString(R.string.user_id_description,userBriefInfo.uid)));
                        binding.materialDrawerSliderView.getItemAdapter().add(userProfile);
                    }
                    if(forumUserBriefInfos.size() > 0){
                        forumUserBriefInfo userBriefInfo = forumUserBriefInfos.get(0);
                        // get first
                        binding.materialDrawerSliderView.setSelection(userBriefInfo.getId(),true);
                    }

                }
                List<bbsInformation> bbsInformationList = viewModel.allBBSInformationMutableLiveData.getValue();
                if(bbsInformationList == null || bbsInformationList.size() == 0){
                    // add bbs
                    PrimaryDrawerItem addBBSProfile = new PrimaryDrawerItem();
                    addBBSProfile.setName(new StringHolder(getString(R.string.add_a_bbs)));
                    addBBSProfile.setIdentifier(FUNC_ADD_A_BBS);
                    addBBSProfile.setDescription(new StringHolder(getString(R.string.title_add_a_forum_by_url)));
                    addBBSProfile.setSelectable(false);

                    //addBBSProfile.setNameShown(true);
                    addBBSProfile.setIcon(new ImageHolder(R.drawable.ic_add_24px));
                    binding.materialDrawerSliderView.getItemAdapter().add(addBBSProfile);
                }
                else {
                    PrimaryDrawerItem incognito = new PrimaryDrawerItem();
                    incognito.setName(new StringHolder(R.string.bbs_anonymous));
                    incognito.setIcon(new ImageHolder(R.drawable.ic_incognito_user_24px));
                    incognito.setSelectable(true);
                    incognito.setIdentifier(MODE_USER_IGCONGTIVE);
                    incognito.setDescription(new StringHolder(R.string.user_anonymous_description));
                    binding.materialDrawerSliderView.getItemAdapter().add(incognito);
                    // other profiles
                    PrimaryDrawerItem addAccount = new PrimaryDrawerItem();
                    addAccount.setName(new StringHolder(R.string.add_a_account));
                    addAccount.setSelectable(false);
                    addAccount.setIcon(new ImageHolder(R.drawable.ic_person_add_24px));
                    addAccount.setIdentifier(FUNC_ADD_AN_ACCOUNT);
                    addAccount.setDescription(new StringHolder(R.string.bbs_add_an_account_description));
                    PrimaryDrawerItem registerAccount = new PrimaryDrawerItem();
                    registerAccount.setName(new StringHolder(R.string.register_an_account));
                    registerAccount.setSelectable(false);
                    registerAccount.setIcon(new ImageHolder(R.drawable.ic_register_account_24px));
                    registerAccount.setIdentifier(FUNC_REGISTER_ACCOUNT);
                    registerAccount.setDescription(new StringHolder(R.string.register_an_account_description));
                    // manage
                    PrimaryDrawerItem manageAccount = new PrimaryDrawerItem();
                    manageAccount.setName(new StringHolder(R.string.bbs_manage_users));
                    manageAccount.setSelectable(false);
                    manageAccount.setIcon(new ImageHolder(R.drawable.ic_manage_user_24px));
                    manageAccount.setIdentifier(FUNC_MANAGE_ACCOUNT);
                    manageAccount.setDescription(new StringHolder(R.string.bbs_manage_users_description));
                    // history
                    PrimaryDrawerItem viewHistory = new PrimaryDrawerItem();
                    viewHistory.setName(new StringHolder(R.string.view_history));
                    viewHistory.setSelectable(false);
                    viewHistory.setIcon(new ImageHolder(R.drawable.ic_history_24px));
                    viewHistory.setIdentifier(FUNC_VIEW_HISTORY);
                    viewHistory.setDescription(new StringHolder(R.string.preference_summary_on_record_history));

                    binding.materialDrawerSliderView.getItemAdapter().add(
                            new DividerDrawerItem(),
                            addAccount,
                            registerAccount,
                            manageAccount,
                            viewHistory,
                            new DividerDrawerItem()
                    );



                    if(forumUserBriefInfos == null || forumUserBriefInfos.size() == 0){
                        Log.d(TAG,"Trigger igcontive mode");
                        binding.materialDrawerSliderView.setSelection(MODE_USER_IGCONGTIVE,true);
                    }
                }




            }
        });
        viewModel.currentBBSInformationMutableLiveData.observe(this, bbsInformation -> {
            bbsInfo = bbsInformation;
            if(bbsInformation != null){
                binding.toolbarTitle.setText(bbsInformation.site_name);
                if(getSupportActionBar() !=null){
                    getSupportActionBar().setTitle(bbsInformation.site_name);
                }

                int id = bbsInformation.getId();
                LiveData<List<forumUserBriefInfo>> allUsersInCurrentBBSLiveData = forumUserBriefInfoDatabase.getInstance(getApplication())
                        .getforumUserBriefInfoDao()
                        .getAllUserByBBSID(id);

                allUsersInCurrentBBSLiveData.observe(this, forumUserBriefInfos -> {
                    Log.d(TAG,"Updating "+id+ " users information "+forumUserBriefInfos.size());
                    viewModel.forumUserListMutableLiveData.postValue(forumUserBriefInfos);
                });
                new QueryCurrentViewHistoryCountAsyncTask().execute();

            } else {
                binding.toolbarTitle.setText(R.string.no_bbs_found_in_db);
            }

        });
        viewModel.currentForumUserBriefInfoMutableLiveData.observe(this, forumUserBriefInfo -> {
            if(forumUserBriefInfo == null){
                binding.toolbarSubtitle.setText(R.string.bbs_anonymous);
            } else {
                binding.toolbarSubtitle.setText(forumUserBriefInfo.username);
            }
            userBriefInfo = forumUserBriefInfo;
            renderViewPageAndBtmView();
        });
    }


    private void initBBSDrawer(){
        Activity activity = this;

        // account header
        headerView = new AccountHeaderView(activity);
        headerView.setOnAccountHeaderListener(new Function3<View, IProfile, Boolean, Boolean>() {
            @Override
            public Boolean invoke(View view, IProfile iProfile, Boolean aBoolean) {
                int bbsId = (int) iProfile.getIdentifier();
                Log.d(TAG,"profile changed "+bbsId+ " name " + iProfile.getName());
                if(bbsId > 0){
                    // change view model
                    List<bbsInformation> allBBSList = viewModel.allBBSInformationMutableLiveData.getValue();
                    if(allBBSList !=null && allBBSList.size()>0){
                        for(int i=0 ;i< allBBSList.size(); i++){
                            bbsInformation curBBS = allBBSList.get(i);
                            if(curBBS.getId() == bbsId){
                                viewModel.currentBBSInformationMutableLiveData.setValue(curBBS);
                                UserPreferenceUtils.saveLastSelectedDrawerItemIdentifier(activity,bbsId);
                                return false;
                            }
                        }

                    }
                }
                else {
                    switch (bbsId){
                        case FUNC_ADD_A_BBS:{
                            Intent intent = new Intent(activity, AddIntroActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case (FUNC_MANAGE_BBS):{
                            Intent intent = new Intent(activity, ManageBBSActivity.class);
                            startActivity(intent);
                            return true;
                        }
                    }
                }


                return false;
            }
        });
        headerView.attachToSliderView(binding.materialDrawerSliderView);
        Context context = this;
        binding.materialDrawerSliderView.setOnDrawerItemClickListener(new Function3<View, IDrawerItem<?>, Integer, Boolean>() {
            @Override
            public Boolean invoke(View view, IDrawerItem<?> iDrawerItem, Integer integer) {
                int id = (int) iDrawerItem.getIdentifier();
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

                            Intent intent = new Intent(activity, LoginActivity.class);

                            bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                            Log.d(TAG,"ADD A account "+forumInfo);
                            if(forumInfo !=null){
                                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);

                                Bundle bundle = options.toBundle();
                                activity.startActivity(intent,bundle);
                            }


                            return true;
                        }
                        case (FUNC_REGISTER_ACCOUNT):{
                            bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                            if(forumInfo!=null){
                                new MaterialAlertDialogBuilder(activity)
                                        .setTitle(getString(R.string.bbs_register_an_account)+" "+forumInfo.site_name)
                                        .setMessage(R.string.bbs_register_account_notification)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                URLUtils.setBBS(forumInfo);
                                                Uri uri = Uri.parse(URLUtils.getBBSRegisterUrl(forumInfo.register_name));
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                activity.startActivity(intent);
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
                        case FUNC_ADD_A_BBS:{
                            Intent intent = new Intent(activity, AddIntroActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case (FUNC_MANAGE_ACCOUNT):{
                            bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                            Intent intent = new Intent(activity, ManageUserActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                            startActivity(intent);
                            return true;
                        }

                        case (FUNC_MANAGE_BBS):{
                            Intent intent = new Intent(activity, ManageBBSActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case (FUNC_VIEW_HISTORY):{
                            bbsInformation forumInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
                            forumUserBriefInfo userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();
                            Intent intent = new Intent(activity, ViewHistoryActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                            startActivity(intent);
                            return true;
                        }
                        case (FOOTER_ABOUT):{
                            Intent intent = new Intent(activity, AboutAppActivity.class);
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
        });

        binding.materialDrawerSliderView.setSavedInstance(savedInstanceState);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this,binding.drawerRoot,binding.toolbar,R.string.drawer_open,R.string.drawer_closed);
        binding.drawerRoot.addDrawerListener(actionBarDrawerToggle);




        DrawerImageLoader.Companion.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                super.set(imageView, uri, placeholder, tag);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(getApplication()));
                Glide.get(getApplication()).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
                Glide.with(getApplication())
                        .load(uri)
                        .centerCrop()
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
            userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();
            switch (position){
                case 0:
                    homeFragment = HomeFragment.newInstance(bbsInfo,userBriefInfo);

                    return homeFragment;
                case 1:
                    return DashBoardFragment.newInstance(bbsInfo,userBriefInfo);
            }
            return HomeFragment.newInstance(bbsInfo,userBriefInfo);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    // fragment adapter
    public class EmptyViewPagerAdapter extends FragmentStatePagerAdapter{

        public EmptyViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            return BlankBBSFragment.newInstance();
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
                    homeFragment = HomeFragment.newInstance(bbsInfo, userBriefInfo);
                    return homeFragment;
                case 1:
                    return DashBoardFragment.newInstance(bbsInfo, userBriefInfo);
                case 2:
                    notificationsFragment = new NotificationsFragment(bbsInfo, userBriefInfo);
                    return notificationsFragment;
            }
            return HomeFragment.newInstance(bbsInfo,userBriefInfo);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    // BBS Render variables

    HomeFragment homeFragment;
    HotThreadsFragment hotThreadsFragment;
    NotificationsFragment notificationsFragment;
    final String HOME_FRAGMENT_KEY = "HOME_FRAGMENT_KEY",
            DASHBOARD_FRAGMENT_KEY = "DASHBOARD_FRAGMENT_KEY",
            NOTIFICATION_FRAGMENT_KEY = "NOTIFICATION_FRAGMENT_KEY";


    private void initFragments(){
    }


    private void renderViewPageAndBtmView(){
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // detecting current bbs
        bbsInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
        if(bbsInfo == null){
            // judge the
            binding.bbsPortalNavViewpager.setAdapter(new EmptyViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            binding.bbsPortalNavView.getMenu().clear();
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu);
            return;
        }

        userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.getValue();

        if(userBriefInfo == null){
            Log.d(TAG, "Current incognitive user "+userBriefInfo);
            binding.bbsPortalNavViewpager.setAdapter(new anonymousViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            binding.bbsPortalNavView.getMenu().clear();
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu);
        } else {
            // use fragment transaction instead
            Log.d(TAG, "Current incognitive user "+userBriefInfo.username);
            binding.bbsPortalNavViewpager.setAdapter(new userViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
            binding.bbsPortalNavView.getMenu().clear();
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_nav_menu);
        }
        binding.bbsPortalNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.navigation_home){
                    binding.bbsPortalNavViewpager.setCurrentItem(0);
                }
                else if(id == R.id.navigation_dashboard){
                    binding.bbsPortalNavViewpager.setCurrentItem(1);
                }
                else if(id == R.id.navigation_notifications){
                    binding.bbsPortalNavViewpager.setCurrentItem(2);
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

    // fragment lifecyle

    private void recoverInstanceState(Bundle savedInstanceState){
        if(savedInstanceState == null){
            return;
        }
        homeFragment = (HomeFragment) getSupportFragmentManager().getFragment(savedInstanceState,HOME_FRAGMENT_KEY);
        notificationsFragment = (NotificationsFragment) getSupportFragmentManager().getFragment(savedInstanceState,NOTIFICATION_FRAGMENT_KEY);
        hotThreadsFragment = (HotThreadsFragment) getSupportFragmentManager().getFragment(savedInstanceState,DASHBOARD_FRAGMENT_KEY);
    }

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

    // listener

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
    public void onBackPressed() {
        if(binding.drawerRoot.isDrawerOpen(binding.materialDrawerSliderView)){
            binding.drawerRoot.closeDrawer(binding.materialDrawerSliderView);
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        Log.d(TAG,"You pressed id "+id);
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return false;
        }
        else if(id == R.id.bbs_share){
            bbsInformation bbsInfo = viewModel.currentBBSInformationMutableLiveData.getValue();
            if(bbsInfo !=null){
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_template,
                        bbsInfo.site_name,bbsInfo.base_url));
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
            else {
                Toasty.info(this,getString(R.string.no_bbs_found_in_db), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if(id == R.id.bbs_settings){
            Intent intent = new Intent(this,SettingsActivity.class);
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
    protected void onResume() {
        super.onResume();
        int activeIdentifier = UserPreferenceUtils.getLastSelectedDrawerItemIdentifier(this);
        if(activeIdentifier >= 0){
            headerView.setActiveProfile(activeIdentifier,true);
        }
        else {
            headerView.setActiveProfile(0,true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        binding.materialDrawerSliderView.saveInstanceState(outState);
        headerView.saveInstanceState(outState);


        super.onSaveInstanceState(outState);
    }

    private class QueryCurrentViewHistoryCountAsyncTask extends AsyncTask<Void,Void,Integer>{

        private int CURRENT_BBS_NULL = -8541;

        @Override
        protected Integer doInBackground(Void... voids) {
            if(viewModel.currentBBSInformationMutableLiveData !=null){
                return ViewHistoryDatabase.getInstance(getApplication()).getDao().getViewHistoryCount();
            }
            else{
                return CURRENT_BBS_NULL;
            }

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Log.d(TAG,"view histories number @@@ "+integer+" identifier "+CURRENT_BBS_NULL);
            if(integer !=CURRENT_BBS_NULL){
                MaterialDrawerSliderViewExtensionsKt.updateBadge(binding.materialDrawerSliderView,FUNC_VIEW_HISTORY, new StringHolder(String.valueOf(integer)));
            }


        }
    }
}