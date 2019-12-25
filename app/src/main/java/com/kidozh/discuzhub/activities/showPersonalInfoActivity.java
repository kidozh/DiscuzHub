package com.kidozh.discuzhub.activities;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.activities.ui.userFriend.userFriendFragment;
import com.kidozh.discuzhub.activities.ui.userThreads.bbsMyThreadFragment;
import com.kidozh.discuzhub.adapter.bbsForumThreadCommentAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.io.InputStream;
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

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    private String userId;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_personal_info);
        ButterKnife.bind(this);
        getIntentInfo();
        configureActionBar();

        renderUserInfo();
        getUserInfo();
        configureViewPager();


    }

    void renderUserInfo(){
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(curBBS.useSafeClient));
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(this)
                .load(bbsURLUtils.getDefaultAvatarUrlByUid(userId))
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(personalInfoAvatar);
    }

    void configureActionBar(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(userId);
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userId = intent.getStringExtra("UID");
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
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);

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
        String apiStr = bbsURLUtils.getProfileApiUrlByUid(Integer.parseInt(userId));
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
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
                                setIconAndTextView(regdate,personalInfoRegdateIcon,personalInfoRegdateTextview);
                                setIconAndTextView(info.get("recentnote"),personalInfoRecentNoteIcon,personalInfoRecentNoteTextview);
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
        personInfoTabLayout.setupWithViewPager(personInfoViewPager);
        personalInfoViewPagerAdapter adapter  = new personalInfoViewPagerAdapter(getSupportFragmentManager());
        personInfoViewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class personalInfoViewPagerAdapter extends FragmentStatePagerAdapter {
        personalInfoViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }


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
                    return userFriendFragment.newInstance(userId);
                case 2:
                    return userFriendFragment.newInstance(userId);
            }
            return new userFriendFragment();


        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.bbs_user_friend);
                case 1:
                    return getString(R.string.bbs_notification_my_pm);
                case 2:
                    return getString(R.string.bbs_notification_my_thread);
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
                this.finish();
                return false;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
