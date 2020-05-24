package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.privacyProtect.privacyProtectFragment;
import com.kidozh.discuzhub.activities.ui.userFriend.userFriendFragment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserProfileResult;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.viewModels.UserProfileViewModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

public class UserProfileActivity extends AppCompatActivity implements userFriendFragment.OnFragmentInteractionListener{

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    @BindView(R.id.show_personal_info_avatar)
    ImageView personalInfoAvatar;
    @BindView(R.id.user_signature_textview)
    TextView personalInfoBioTextView;
    @BindView(R.id.show_personal_info_interest_icon)
    ImageView personalInfoInterestIcon;
    @BindView(R.id.show_personal_info_interest_textView)
    TextView personalInfoInterestTextView;
    @BindView(R.id.show_personal_info_username)
    TextView personalInfoUsername;
    @BindView(R.id.show_personal_info_birthplace_textView)
    TextView personInfoBirthPlace;
    @BindView(R.id.show_personal_info_birthplace_icon)
    ImageView personalInfoBirthPlaceIcon;
    @BindView(R.id.show_personal_info_regdate_icon)
    ImageView personalInfoRegdateIcon;
    @BindView(R.id.show_personal_info_regdate_textView)
    TextView personalInfoRegdateTextview;
    @BindView(R.id.show_personal_info_recent_note_icon)
    ImageView personalInfoRecentNoteIcon;
    @BindView(R.id.show_personal_info_recent_note_textView)
    TextView personalInfoRecentNoteTextview;
    @BindView(R.id.show_personal_info_progressbar)
    ProgressBar showPersonalInfoProgressbar;
    @BindView(R.id.show_personal_info_layout)
    ConstraintLayout showPersonalInfoLayout;
    @BindView(R.id.show_personal_info_tabLayout)
    TabLayout personInfoTabLayout;
    @BindView(R.id.show_personal_info_viewpager)
    ViewPager personInfoViewPager;
    @BindView(R.id.show_personal_info_message_btn)
    Button personalInfoPMBtn;
    @BindView(R.id.show_personal_info_focus_btn)
    Button personalInfoFollowBtn;
    @BindView(R.id.show_personal_info_group_icon)
    ImageView personalInfoGroupIcon;
    @BindView(R.id.show_personal_info_group_info)
    TextView personalInfoGroupInfo;
    @BindView(R.id.show_personal_info_last_activity_time)
    TextView personalInfoLastActivityTime;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    private int userId;
    OkHttpClient client;
    String friendNum, threadNum, postsNum;
    String username;
    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_personal_info);
        ButterKnife.bind(this);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        getIntentInfo();
        configureActionBar();
        renderFollowAndPMBtn();
        bindViewModel();
        renderUserInfo();
        configurePMBtn();


    }

    void renderUserInfo(){
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(this,curBBS.useSafeClient));
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
                .into(personalInfoAvatar);
    }

    void renderFollowAndPMBtn(){
        if(curUser ==null){
            personalInfoPMBtn.setVisibility(View.GONE);
            personalInfoFollowBtn.setVisibility(View.GONE);
        }
    }

    void configurePMBtn(){
        personalInfoPMBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bbsParseUtils.privateMessage privateM = new bbsParseUtils.privateMessage(userId,
                        false,"",userId,1,1,
                        username,"",username,""
                );
                Intent intent = new Intent(getApplicationContext(), bbsPrivateMessageDetailActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_PRIVATE_MESSAGE_KEY,privateM);

                startActivity(intent);
            }
        });
    }

    void configureActionBar(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(String.valueOf(userId));
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userId = intent.getIntExtra("UID",0);
        if(curBBS == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            URLUtils.setBBS(curBBS);
            viewModel.setBBSInfo(curBBS,userBriefInfo, userId);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(curBBS.site_name);
        }
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        if(userBriefInfo!=null && userId == Integer.parseInt(userBriefInfo.uid)){
            personalInfoFollowBtn.setVisibility(View.GONE);
            personalInfoPMBtn.setVisibility(View.GONE);
        }

    }

    private void bindViewModel(){
        viewModel.getUserProfileResultLiveData().observe(this, new Observer<UserProfileResult>() {
            @Override
            public void onChanged(UserProfileResult userProfileResult) {
                if(userProfileResult !=null){
                    // for avatar rendering
                    OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(getApplication(),curBBS.useSafeClient));
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
                            .into(personalInfoAvatar);

                    // signature
                    String sigHtml = userProfileResult.userProfileVariableResult.space.sigatureHtml;
                    MyTagHandler myTagHandler = new MyTagHandler(getApplication(),personalInfoBioTextView,personalInfoBioTextView);
                    MyImageGetter myImageGetter = new MyImageGetter(getApplication(),personalInfoBioTextView,personalInfoBioTextView,true);
                    Spanned sp = Html.fromHtml(sigHtml,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);

                    personalInfoBioTextView.setText(spannableString, TextView.BufferType.SPANNABLE);
                    personalInfoBioTextView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        });
    }


    void configureViewPager(){
        Log.d(TAG,"Configuring friend fragment");
        List<String> tabTitles = new ArrayList<>();

        personInfoTabLayout.setupWithViewPager(personInfoViewPager);
        personalInfoViewPagerAdapter adapter  = new personalInfoViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        personInfoViewPager.setAdapter(adapter);
        tabTitles.add(getString(R.string.bbs_user_friend)+String.format(" (%s)",friendNum));
        tabTitles.add(getString(R.string.bbs_forum_thread)+String.format(" (%s)",threadNum));
        tabTitles.add(getString(R.string.bbs_forum_post)+String.format(" (%s)",postsNum));

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRenderSuccessfully() {
        Log.d(TAG,"Redraw view pager");
        personInfoViewPager.invalidate();
        personInfoViewPager.requestLayout();
    }


    public class personalInfoViewPagerAdapter extends FragmentStatePagerAdapter {


        public personalInfoViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return userFriendFragment.newInstance(userId);
                case 1:
                    return privacyProtectFragment.newInstance("","");
                case 2:
                    return privacyProtectFragment.newInstance("","");
            }
            return userFriendFragment.newInstance(userId);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.bbs_user_friend)+String.format(" (%s)",friendNum);
                case 1:
                    return getString(R.string.bbs_forum_thread)+String.format(" (%s)",threadNum);
                case 2:
                    return getString(R.string.bbs_forum_post)+String.format(" (%s)",postsNum);
                default:
                    return "";
            }
        }

        @Override
        public int getCount() {
            return 3;
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


}
