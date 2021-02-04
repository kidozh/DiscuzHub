package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.BuildConfig;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserGroup.UserGroupInfoFragment;
import com.kidozh.discuzhub.activities.ui.UserMedal.MedalFragment;
import com.kidozh.discuzhub.activities.ui.UserProfileList.UserProfileInfoListFragment;
import com.kidozh.discuzhub.activities.ui.UserFriend.UserFriendFragment;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.databinding.ActivityShowPersonalInfoBinding;
import com.kidozh.discuzhub.entities.UserProfileItem;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserProfileResult;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.viewModels.UserProfileViewModel;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UserProfileActivity extends BaseStatusActivity implements
        UserFriendFragment.OnFragmentInteractionListener,
        bbsLinkMovementMethod.OnLinkClickedListener {
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    
    private int userId;
    String username;
    private UserProfileViewModel viewModel;
    personalInfoViewPagerAdapter adapter;
    ActivityShowPersonalInfoBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        getIntentInfo();
        configureActionBar();
        renderFollowAndPMBtn();
        bindViewModel();
        renderUserInfo();
        configurePMBtn();
        configureViewPager();


    }

    void renderUserInfo(){
        // making it circle


        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(this,bbsInfo.useSafeClient));
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        int avatar_num = userId % 16;
        if(avatar_num < 0){
            avatar_num = -avatar_num;
        }

        int avatarResource = getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",getPackageName());

        Glide.with(this)
                .load(URLUtils.getDefaultAvatarUrlByUid(userId))
                .error(avatarResource)
                .placeholder(avatarResource)
                .centerInside()
                .into(binding.showPersonalInfoAvatar);
    }

    void renderFollowAndPMBtn(){
        if(userBriefInfo ==null){
            binding.showPersonalInfoMessageBtn.setVisibility(View.GONE);
            binding.showPersonalInfoFocusBtn.setVisibility(View.GONE);
        }
    }

    void configurePMBtn(){
        if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
            binding.showPersonalInfoMessageBtn.setVisibility(View.GONE);
        }
        binding.showPersonalInfoMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bbsParseUtils.privateMessage privateM = new bbsParseUtils.privateMessage(userId,
                        false,"",userId,1,1,
                        username,"",username,""
                );
                Intent intent = new Intent(getApplicationContext(), PrivateMessageActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(ConstUtils.PASS_PRIVATE_MESSAGE_KEY,privateM);

                startActivity(intent);
            }
        });
    }

    void configureActionBar(){
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(username);
        getSupportActionBar().setSubtitle(String.valueOf(userId));
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        bbsInfo = (Discuz) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        userId = intent.getIntExtra("UID",0);
        if(bbsInfo == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            viewModel.setBBSInfo(bbsInfo,userBriefInfo, userId);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(bbsInfo.site_name);
        }
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        if(userBriefInfo!=null && userId == Integer.parseInt(userBriefInfo.uid)){
            binding.showPersonalInfoFocusBtn.setVisibility(View.GONE);
            binding.showPersonalInfoMessageBtn.setVisibility(View.GONE);
        }

    }



    private void bindViewModel(){
        viewModel.getUserProfileResultLiveData().observe(this, new Observer<UserProfileResult>() {
            @Override
            public void onChanged(UserProfileResult userProfileResult) {
                Log.d(TAG,"User profile result "+userProfileResult);

                if(userProfileResult !=null
                        && userProfileResult.userProfileVariableResult !=null
                        && userProfileResult.userProfileVariableResult.space !=null){
                    UserProfileResult.SpaceVariables spaceVariables = userProfileResult.userProfileVariableResult.space;
                    String username = userProfileResult.userProfileVariableResult.space.username;
                    if(getSupportActionBar()!=null){
                        getSupportActionBar().setSubtitle(username);
                    }


                    // for avatar rendering
                    OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(getApplication(),bbsInfo.useSafeClient));
                    Glide.get(getApplicationContext()).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
                    int uid = userProfileResult.userProfileVariableResult.space.uid;
                    int avatar_num = uid % 16;
                    if(avatar_num < 0){
                        avatar_num = -avatar_num;
                    }

                    int avatarResource = getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",getPackageName());

                    Glide.with(getApplication())
                            .load(URLUtils.getDefaultAvatarUrlByUid(uid))
                            .error(avatarResource)
                            .placeholder(avatarResource)
                            .centerInside()
                            .into(binding.showPersonalInfoAvatar);
                    //check with verified status
                    if(spaceVariables.emailStatus){
                        binding.userVerifiedIcon.setVisibility(View.VISIBLE);
                    }
                    else {
                        binding.userVerifiedIcon.setVisibility(View.GONE);
                    }

                    // signature
                    String sigHtml = userProfileResult.userProfileVariableResult.space.sigatureHtml;
                    Log.d(TAG,"Signature html "+sigHtml);
                    MyTagHandler myTagHandler = new MyTagHandler(getApplication(),binding.userSignatureTextview,binding.userSignatureTextview);
                    MyImageGetter myImageGetter = new MyImageGetter(getApplication(),binding.userSignatureTextview,binding.userSignatureTextview,true);
                    Spanned sp = Html.fromHtml(sigHtml,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);

                    binding.userSignatureTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
                    binding.userSignatureTextview.setMovementMethod(new bbsLinkMovementMethod(UserProfileActivity.this));
                    if(userProfileResult.userProfileVariableResult.space.bio.length()!=0){
                        binding.userBioTextview.setText(userProfileResult.userProfileVariableResult.space.bio);
                    }
                    else {
                        binding.userBioTextview.setVisibility(View.GONE);
                    }

                    if(userProfileResult.userProfileVariableResult.space.interest.length()!=0){

                        binding.showPersonalInfoInterestTextView.setVisibility(View.VISIBLE);
                        binding.showPersonalInfoInterestTextView.setText(userProfileResult.userProfileVariableResult.space.interest);
                    }
                    else {

                        binding.showPersonalInfoInterestTextView.setVisibility(View.GONE);
                        binding.showPersonalInfoInterestTextView.setText(userProfileResult.userProfileVariableResult.space.interest);
                    }

                    String birthPlace = userProfileResult.userProfileVariableResult.space.birthprovince +
                            userProfileResult.userProfileVariableResult.space.birthcity +
                            userProfileResult.userProfileVariableResult.space.birthdist +
                            userProfileResult.userProfileVariableResult.space.birthcommunity;
                    if(birthPlace.length()!=0){

                        binding.showPersonalInfoBirthplaceTextView.setVisibility(View.VISIBLE);
                        binding.showPersonalInfoBirthplaceTextView.setText(birthPlace);
                    }
                    else {

                        binding.showPersonalInfoBirthplaceTextView.setVisibility(View.GONE);
                    }
                    binding.showPersonalInfoRegdateTextView.setText(userProfileResult.userProfileVariableResult.space.regdate);
                    binding.showPersonalInfoLastActivityTime.setText(userProfileResult.userProfileVariableResult.space.lastactivity);

                    binding.showPersonalInfoRecentNoteTextView.setText(userProfileResult.userProfileVariableResult.space.recentNote);
                    if(userProfileResult.userProfileVariableResult.space.group!=null){
                        binding.showPersonalInfoGroupInfo.setText(
                                Html.fromHtml(userProfileResult.userProfileVariableResult.space.group.groupTitle),
                                TextView.BufferType.SPANNABLE);
                    }
                    else {
                        binding.showPersonalInfoGroupInfo.setVisibility(View.GONE);
                    }
                    // for detailed information

                    SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean recordHistory = prefs.getBoolean(getString(R.string.preference_key_record_history),false);
                    if(recordHistory){
                        new InsertViewHistory(new ViewHistory(
                                URLUtils.getDefaultAvatarUrlByUid(uid),
                                username,
                                bbsInfo.getId(),
                                userProfileResult.userProfileVariableResult.space.sigatureHtml,
                                ViewHistory.VIEW_TYPE_USER_PROFILE,
                                uid,
                                0,
                                new Date()
                        )).execute();
                    }

                }
                binding.showPersonalInfoViewpager.invalidate();
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.showPersonalInfoProgressbar.setVisibility(View.VISIBLE);
                }
                else {
                    binding.showPersonalInfoProgressbar.setVisibility(View.GONE);
                }
            }
        });

    }

    private UserProfileItem generateUserProfileItem(String title, String content, int iconId, int privateStatus){

        if(content == null || content.length() !=0){
            return new UserProfileItem(title,content,iconId);
        }
        else {
            switch (privateStatus){
                case 0:{
                    return new UserProfileItem(title,getString(R.string.user_profile_item_not_set),iconId);
                }
                case 1:{
                    return new UserProfileItem(title,getString(R.string.user_profile_item_only_visible_to_friend),R.drawable.ic_profile_private_item_24px);
                }
                case 3:{
                    return new UserProfileItem(title,getString(R.string.user_profile_privacy_hidden),R.drawable.ic_profile_private_item_24px);
                }
                default:{
                    return new UserProfileItem(title,getString(R.string.user_profile_item_not_set),R.drawable.ic_profile_private_item_24px);
                }
            }
        }

    }

    private UserProfileItem generateGenderUserProfileItem(int genderStatus, int privateStatus){
        if(privateStatus == 2){
            return generateUserProfileItem(getString(R.string.gender),"",R.drawable.ic_profile_private_item_24px, privateStatus);
        }
        else {
            switch (genderStatus){
                case 0:{
                    return generateUserProfileItem(getString(R.string.gender),getString(R.string.gender_secret),R.drawable.ic_secret_24px, privateStatus);
                }
                case 1:{
                    return generateUserProfileItem(getString(R.string.gender),getString(R.string.gender_male),R.drawable.ic_male_24px, privateStatus);
                }
                case 2:{
                    return generateUserProfileItem(getString(R.string.gender),getString(R.string.gender_female),R.drawable.ic_female_24px, privateStatus);
                }
                default:{
                    return generateUserProfileItem(getString(R.string.gender),getString(R.string.item_parse_failed),R.drawable.ic_error_outline_24px, privateStatus);
                }

            }
        }
    }


    private List<UserProfileItem> getBasicInfoList(){
        UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
        if(userProfileResult == null){
            return new ArrayList<>();
        }
        List<UserProfileItem> userProfileItemList = new ArrayList<>();
        UserProfileResult.SpaceVariables spaceVariables = userProfileResult.userProfileVariableResult.space;
        UserProfileResult.PrivacySetting privacySetting = spaceVariables.privacySetting;
        // gender
        int genderPrivate =  privacySetting.profilePrivacySetting.gender;
        Log.d(TAG,"Gender int "+ spaceVariables.gender);
        userProfileItemList.add(generateGenderUserProfileItem(spaceVariables.gender,genderPrivate));
        // birthday
        int birthPrivate =  privacySetting.profilePrivacySetting.birthday;
        int birthYear = spaceVariables.birthyear;
        int birthMonth = spaceVariables.birthmonth;
        int birthDay = spaceVariables.birthday;
        if(birthYear == 0 || birthMonth == 0 || birthDay == 0){
            userProfileItemList.add(
                    generateUserProfileItem(getString(R.string.birthday),
                            "",R.drawable.ic_cake_outlined_24px,
                            birthPrivate)
            );
        }
        else {
            // construct the date
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.set(birthYear,birthMonth,birthDay);
            Date birthDate = birthCalendar.getTime();
            userProfileItemList.add(
                    generateUserProfileItem(getString(R.string.birthday),
                            df.format(birthDate),
                            R.drawable.ic_cake_outlined_24px,
                            birthPrivate)
            );

            userProfileItemList.add(
                    generateUserProfileItem(getString(R.string.constellation),
                            spaceVariables.constellation,
                            R.drawable.ic_constellation_24px,
                            birthPrivate)
            );
        }
        // birthplace
        int birthPlacePrivate = privacySetting.profilePrivacySetting.birthcity;
        String birthPlace = spaceVariables.birthprovince + spaceVariables.birthcity + spaceVariables.birthdist
                +spaceVariables.birthcommunity;
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.birthplace),
                        birthPlace,R.drawable.ic_child_care_24px,
                        birthPlacePrivate)
        );

        // resident place
        int residentPlacePrivate = privacySetting.profilePrivacySetting.residecity;
        String residentPlace = spaceVariables.resideprovince + spaceVariables.residecity
                + spaceVariables.residedist
                +spaceVariables.residecommunity;
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.resident_location),
                        residentPlace,R.drawable.ic_location_city_24px,
                        residentPlacePrivate)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.married_status),
                        spaceVariables.marriedStatus,R.drawable.ic_marry_status_24px,
                        privacySetting.profilePrivacySetting.affectivestatus)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.profile_looking_for),
                        spaceVariables.lookingfor,R.drawable.ic_looking_for_friend_24px,
                        privacySetting.profilePrivacySetting.lookingfor)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.blood_type),
                        spaceVariables.bloodtype,R.drawable.ic_blood_type_24px,
                        privacySetting.profilePrivacySetting.bloodtype)
        );
        Log.d(TAG,"Blood type "+ spaceVariables.bloodtype);



        return userProfileItemList;

    }

    private List<UserProfileItem> getEduOccupationInfoList(){
        UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
        if(userProfileResult == null){
            return new ArrayList<>();
        }
        List<UserProfileItem> userProfileItemList = new ArrayList<>();
        UserProfileResult.SpaceVariables spaceVariables = userProfileResult.userProfileVariableResult.space;
        UserProfileResult.PrivacySetting privacySetting = spaceVariables.privacySetting;
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_deploma),
                        spaceVariables.education,R.drawable.ic_study_degree_24px,
                        privacySetting.profilePrivacySetting.education)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_graduate_from),
                        spaceVariables.graduateschool,R.drawable.ic_school_24px,
                        privacySetting.profilePrivacySetting.graduateschool)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_company),
                        spaceVariables.company,R.drawable.ic_company_24px,
                        privacySetting.profilePrivacySetting.company)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_occupation),
                        spaceVariables.occupation,R.drawable.ic_work_occupation_24px,
                        privacySetting.profilePrivacySetting.occupation)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_position),
                        spaceVariables.workPosition,R.drawable.ic_work_grade_24px,
                        privacySetting.profilePrivacySetting.position)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_revenue),
                        spaceVariables.revenue,R.drawable.ic_price_outlined_24px,
                        privacySetting.profilePrivacySetting.revenue)
        );




        return userProfileItemList;

    }

    private List<UserProfileItem> getCreditList(){
        UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
        if(userProfileResult == null || userProfileResult.userProfileVariableResult == null){
            return new ArrayList<>();
        }
        List<UserProfileItem> userProfileItemList = new ArrayList<>();

        UserProfileResult.SpaceVariables spaceVariables = userProfileResult.userProfileVariableResult.space;
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.bbs_credit),
                        String.valueOf(spaceVariables.credits),
                        R.drawable.ic_credit_24px,
                        0)
        );
        UserProfileResult.PrivacySetting privacySetting = spaceVariables.privacySetting;
        List<UserProfileResult.extendCredit> extCredits = userProfileResult.userProfileVariableResult.getExtendCredits();
        for(int i=0;i<extCredits.size();i++){
            UserProfileResult.extendCredit extendCredit = extCredits.get(i);
            userProfileItemList.add(
                    generateUserProfileItem(extendCredit.title,
                            extendCredit.value + extendCredit.unit,
                            R.drawable.ic_extend_credit_24px,
                            0)
            );
        }







        return userProfileItemList;

    }

    private List<UserProfileItem> getExtraInfoList(){
        UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
        if(userProfileResult == null){
            return new ArrayList<>();
        }
        List<UserProfileItem> userProfileItemList = new ArrayList<>();
        UserProfileResult.SpaceVariables spaceVariables = userProfileResult.userProfileVariableResult.space;
        UserProfileResult.PrivacySetting privacySetting = spaceVariables.privacySetting;
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_homepage),
                        spaceVariables.site,R.drawable.ic_personal_site_24px,
                        privacySetting.profilePrivacySetting.site)
        );
        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_interest),
                        spaceVariables.interest,R.drawable.ic_flag_24px,
                        privacySetting.profilePrivacySetting.interest)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_favorite_times),
                        String.valueOf(spaceVariables.favtimes),R.drawable.ic_favorite_24px,
                        0)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_share_times),
                        String.valueOf(spaceVariables.sharetimes),R.drawable.ic_share_outlined_24px,
                        0)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_last_visit),
                        spaceVariables.lastvisit,R.drawable.vector_drawable_clock,
                        0)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_last_post),
                        spaceVariables.lastpost,R.drawable.vector_drawable_clock,
                        0)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_last_activity),
                        spaceVariables.lastactivity,R.drawable.vector_drawable_clock,
                        0)
        );

        userProfileItemList.add(
                generateUserProfileItem(getString(R.string.user_profile_last_send_mail),
                        spaceVariables.lastsendmail,R.drawable.ic_email_24px,
                        0)
        );






        return userProfileItemList;

    }


    void configureViewPager(){
        Log.d(TAG,"Configuring friend fragment");
        List<String> tabTitles = new ArrayList<>();

        binding.showPersonalInfoTabLayout.setupWithViewPager(binding.showPersonalInfoViewpager);
        adapter  = new personalInfoViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.showPersonalInfoViewpager.setAdapter(adapter);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRenderSuccessfully() {
        Log.d(TAG,"Redraw view pager");
        binding.showPersonalInfoViewpager.invalidate();
        binding.showPersonalInfoViewpager.requestLayout();
    }

    @Override
    public boolean onLinkClicked(String url) {
        bbsLinkMovementMethod.parseURLAndOpen(this,bbsInfo,userBriefInfo,url);
        return true;
    }


    public class personalInfoViewPagerAdapter extends FragmentStatePagerAdapter {


        public personalInfoViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            Log.d(TAG,"refresh adapter");
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
            switch (position){
                case 0:
                    if(userProfileResult!=null && userProfileResult.userProfileVariableResult!=null
                            && userProfileResult.userProfileVariableResult.space !=null){
                        return MedalFragment.newInstance(userProfileResult.userProfileVariableResult.space.medals);
                    }
                    else {
                        return MedalFragment.newInstance(null);
                    }
                case 1:
                    return UserProfileInfoListFragment.newInstance(getString(R.string.user_profile_extra_information),
                            getCreditList());
                case 2:
                    if(userProfileResult!=null && userProfileResult.userProfileVariableResult!=null
                            && userProfileResult.userProfileVariableResult.space !=null){
                        return UserFriendFragment.newInstance(userId,userProfileResult.userProfileVariableResult.space.friends);
                    }
                    else {
                        return UserFriendFragment.newInstance(userId,0);
                    }

                case 3:
                    return UserProfileInfoListFragment.newInstance(getString(R.string.user_profile_basic_information),
                            getBasicInfoList());
                case 4:
                    return UserProfileInfoListFragment.newInstance(getString(R.string.user_profile_edu_job),
                            getEduOccupationInfoList());
                case 5:
                    return UserProfileInfoListFragment.newInstance(getString(R.string.user_profile_extra_information),
                            getExtraInfoList());
                case 6:
                    if(userProfileResult!=null && userProfileResult.userProfileVariableResult!=null
                    && userProfileResult.userProfileVariableResult.space !=null){
                        return UserGroupInfoFragment.newInstance(userProfileResult.userProfileVariableResult.space.group,
                                userProfileResult.userProfileVariableResult.space.adminGroup);
                    }
                    else {
                        return UserGroupInfoFragment.newInstance(null,null);
                    }

            }
            return UserFriendFragment.newInstance(userId,0);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            UserProfileResult userProfileResult = viewModel.getUserProfileResultLiveData().getValue();
            switch (position){
                case 0:
                    if(userProfileResult!=null && userProfileResult.userProfileVariableResult!=null
                            && userProfileResult.userProfileVariableResult.space !=null
                            && userProfileResult.userProfileVariableResult.space.medals!=null){
                        return getString(R.string.bbs_medals_num,userProfileResult.userProfileVariableResult.space.medals.size());
                    }
                    else {
                        return getString(R.string.user_profile_medal);
                    }

                case 1:
                    return getString(R.string.bbs_credit);
                case 2:
                    if(userProfileResult !=null && userProfileResult.userProfileVariableResult!=null && userProfileResult.userProfileVariableResult.space!=null){
                        return getString(R.string.user_profile_friend_number_template, userProfileResult.userProfileVariableResult.space.friends);
                    }
                    else {
                        return getString(R.string.bbs_user_friend);
                    }

                case 3:
                    return getString(R.string.user_profile_basic_information);
                case 4:
                    return getString(R.string.user_profile_edu_job);
                case 5:
                    return getString(R.string.user_profile_extra_information);
                case 6:
                    return getString(R.string.profile_group_information);
                default:
                    return "";
            }
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:{
                //返回键的id
                this.finishAfterTransition();
                return false;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class InsertViewHistory extends AsyncTask<Void,Void,Void> {

        ViewHistory viewHistory;
        ViewHistoryDao dao;

        public InsertViewHistory(ViewHistory viewHistory){
            this.viewHistory = viewHistory;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(viewHistory.name == null){
                viewHistory.name = "";
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao = ViewHistoryDatabase.getInstance(getApplicationContext()).getDao();
            List<ViewHistory> viewHistories = dao
                    .getViewHistoryByBBSIdAndFid(viewHistory.belongedBBSId,viewHistory.fid);
            if(viewHistories ==null || viewHistories.size() == 0){

                dao.insert(viewHistory);
            }
            else {

                for(int i=0 ;i<viewHistories.size();i++){
                    ViewHistory updatedViewHistory = viewHistories.get(i);
                    updatedViewHistory.recordAt = new Date();
                }
                dao.insert(viewHistories);
            }
            // dao.insert(viewHistory);
            return null;
        }
    }


}
