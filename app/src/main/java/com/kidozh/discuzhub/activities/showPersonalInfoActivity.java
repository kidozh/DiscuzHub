package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
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
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class showPersonalInfoActivity extends AppCompatActivity implements userFriendFragment.OnFragmentInteractionListener{

    private static final String TAG = showPersonalInfoActivity.class.getSimpleName();

    @BindView(R.id.show_personal_info_avatar)
    ImageView personalInfoAvatar;
    @BindView(R.id.show_personal_info_bio_textview)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_personal_info);
        ButterKnife.bind(this);
        getIntentInfo();
        configureActionBar();
        renderFollowAndPMBtn();

        renderUserInfo();
        getUserInfo();
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
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
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

    void setIconAndTextView(String inputtedString, ImageView iconView, TextView textView){
        if(inputtedString!=null && inputtedString.length()>0){
            textView.setText(inputtedString);
        }
        else {
            iconView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }
    }

    void getUserInfo(){
        String apiStr = URLUtils.getProfileApiUrlByUid(userId);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Activity curActivity = this;
        Handler mHandler = new Handler(Looper.getMainLooper());
        showPersonalInfoProgressbar.setVisibility(View.VISIBLE);
        showPersonalInfoLayout.setVisibility(View.GONE);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(getApplication(),getString(R.string.network_failed), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    Map<String,String> info = bbsParseUtils.parseUserProfile(s);
                    if(info!=null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showPersonalInfoProgressbar.setVisibility(View.GONE);
                                showPersonalInfoLayout.setVisibility(View.VISIBLE);
                                getSupportActionBar().setTitle(info.get("username"));

                                String sightml = info.get("sightml");
                                if(sightml!=null && sightml.length()>0){
                                    MyTagHandler myTagHandler = new MyTagHandler(getApplication(),personalInfoBioTextView,personalInfoBioTextView);
                                    MyImageGetter myImageGetter = new MyImageGetter(getApplication(),personalInfoBioTextView,personalInfoBioTextView,true);
                                    Spanned sp = Html.fromHtml(sightml,myImageGetter,myTagHandler);
                                    SpannableString spannableString = new SpannableString(sp);

                                    personalInfoBioTextView.setText(spannableString, TextView.BufferType.SPANNABLE);
                                    personalInfoBioTextView.setMovementMethod(LinkMovementMethod.getInstance());
                                }
                                else {
                                    personalInfoBioTextView.setText(R.string.bbs_bio_not_set);
                                }


                                personalInfoUsername.setText(info.get("username"));
                                username = info.get("username");
                                personalInfoInterestTextView.setText(info.get("interest"));
                                setIconAndTextView(info.get("interest"),personalInfoInterestIcon,personalInfoInterestTextView);
                                setIconAndTextView(
                                        info.get("birthprovince")+info.get("birthcity")+info.get("birthdist")+info.get("birthcommunity"),
                                        personalInfoBirthPlaceIcon,
                                        personInfoBirthPlace
                                );
                                String regdate = info.get("regdate");
                                if(regdate!=null){
                                    regdate = regdate.replace("\n","");
                                }
                                String lastTimeString = info.get("lastactivitydb");
                                if(lastTimeString !=null){
                                    Date lastActivity = new Date(Long.parseLong(lastTimeString)*1000);
                                    personalInfoLastActivityTime.setText(timeDisplayUtils.getLocalePastTimeString(curActivity,lastActivity));
                                }
                                else {
                                    personalInfoLastActivityTime.setText(R.string.not_known);
                                }


                                setIconAndTextView(regdate,personalInfoRegdateIcon,personalInfoRegdateTextview);
                                setIconAndTextView(info.get("recentnote"),personalInfoRecentNoteIcon,personalInfoRecentNoteTextview);
                                friendNum = info.get("friends");
                                threadNum = info.get("threads");
                                postsNum = info.get("posts");
                                configureViewPager();
                                String userCredit = info.get("credits");
                                Map<String,String> userGroupInfo = bbsParseUtils.parseUserGroupInfo(s);
                                if(userGroupInfo!=null){
                                    String type = userGroupInfo.get("type");
                                    if(type.equals("special")){
                                        personalInfoGroupIcon.setImageDrawable(getDrawable(R.drawable.vector_drawable_verified_user_24px));
                                    }
                                    else {
                                        personalInfoGroupIcon.setImageDrawable(getDrawable(R.drawable.vector_drawable_account_box_24px));
                                    }

                                    String groupTitle = userGroupInfo.get("grouptitle");
                                    String groupInfoText = getString(R.string.bbs_personal_info_credit_with_title_template,groupTitle,userCredit);
                                    Spanned styledText = Html.fromHtml(groupInfoText);
                                    personalInfoGroupInfo.setText(styledText, TextView.BufferType.SPANNABLE);

                                }
                            }
                        });
                    }

                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(getApplication(),getString(R.string.network_failed), Toast.LENGTH_LONG).show();
                        }
                    });
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
//        new TabLayoutMediator(personInfoTabLayout,personInfoViewPager,(
//                (tab, position) -> tab.setText(tabTitles.get(position)))
//        ).attach();

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
