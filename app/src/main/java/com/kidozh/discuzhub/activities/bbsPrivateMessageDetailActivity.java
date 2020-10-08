package com.kidozh.discuzhub.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment;
import com.kidozh.discuzhub.adapter.bbsPrivateDetailMessageAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.EmotionInputHandler;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsSmileyPicker;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class bbsPrivateMessageDetailActivity extends BaseStatusActivity implements SmileyFragment.OnSmileyPressedInteraction {

    private static final String TAG = bbsPrivateMessageDetailActivity.class.getSimpleName();

    @BindView(R.id.bbs_private_message_detail_swipeRefreshLayout)
    SwipeRefreshLayout privateMessageDetailSwipeRefreshLayout;
    @BindView(R.id.bbs_private_message_detail_recyclerview)
    RecyclerView privateMessageDetailRecyclerview;
    @BindView(R.id.bbs_private_message_comment_editText)
    EditText privateMessageCommentEditText;
    @BindView(R.id.bbs_private_message_comment_button)
    Button privateMessageCommentButton;
    @BindView(R.id.bbs_private_message_comment_emoij)
    ImageView mCommentEmoijBtn;

    @BindView(R.id.bbs_private_message_comment_smiley_constraintLayout)
    ConstraintLayout mCommentSmileyConstraintLayout;
    @BindView(R.id.bbs_private_message_comment_smiley_tabLayout)
    TabLayout mCommentSmileyTabLayout;
    @BindView(R.id.bbs_private_message_comment_smiley_viewPager)
    ViewPager mCommentSmileyViewPager;

    bbsInformation bbsInfo;

    bbsParseUtils.privateMessage privateMessageInfo;
    bbsPrivateDetailMessageAdapter adapter;
    // private OkHttpClient client;
    private int globalPage = -1;
    private Boolean hasLoadAll=false;
    String formHash;
    String pmid;

    List<bbsParseUtils.smileyInfo> allSmileyInfos;
    int smileyCateNum;


    private bbsSmileyPicker smileyPicker;
    private EmotionInputHandler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_private_message_detail);
        ButterKnife.bind(this);
        getIntentInfo();
        configureActionBar();
        configureSmileyLayout();
        configureRecyclerview();
        configureSwipeLayout();
        getPageInfo(globalPage);
        configureSendBtn();
    }

    private void configureSmileyLayout(){
        handler = new EmotionInputHandler(privateMessageCommentEditText, (enable, s) -> {

        });

        smileyPicker = new bbsSmileyPicker(this);
        smileyPicker.setListener((str,a)->{
            handler.insertSmiley(str,a);
        });
    }

    private void configureSwipeLayout(){
        Handler mHandler = new Handler(Looper.getMainLooper());
        privateMessageDetailSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(globalPage!=0){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getPageInfo(globalPage);
                        }
                    });

                }
                else {
                    privateMessageDetailSwipeRefreshLayout.setRefreshing(false);
                }

            }
        });
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        privateMessageDetailRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new bbsPrivateDetailMessageAdapter(bbsInfo,userBriefInfo);
        privateMessageDetailRecyclerview.setAdapter(adapter);
    }

    private void configureSendBtn(){
        privateMessageCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendMessage = privateMessageCommentEditText.getText().toString();
                if(sendMessage.length()!=0){
                    sendPrivateMessage();
                }
                else {
                    Toasty.warning(getApplication(),getString(R.string.bbs_pm_is_required),Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCommentEmoijBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCommentSmileyConstraintLayout.getVisibility() == View.GONE){
                    // smiley picker not visible
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.vector_drawable_keyboard_24px));

                    privateMessageCommentEditText.clearFocus();
                    // close keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm !=null){
                        imm.hideSoftInputFromWindow(privateMessageCommentEditText.getWindowToken(),0);
                    }
                    mCommentSmileyConstraintLayout.setVisibility(View.VISIBLE);

                    // tab layout binding...
                    getSmileyInfo();
                }
                else {
                    mCommentSmileyConstraintLayout.setVisibility(View.GONE);
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp));
                }


            }
        });

        privateMessageCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && mCommentSmileyConstraintLayout.getVisibility() == View.VISIBLE){
                    mCommentSmileyConstraintLayout.setVisibility(View.GONE);
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp));
                }
            }
        });
    }

    private void getSmileyInfo(){
        Request request = new Request.Builder()
                .url(URLUtils.getSmileyApiUrl())
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(getApplicationContext(),getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    List<bbsParseUtils.smileyInfo> smileyInfoList = bbsParseUtils.parseSmileyInfo(s);
                    int cateNum = bbsParseUtils.parseSmileyCateNum(s);
                    smileyCateNum = cateNum;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            allSmileyInfos = smileyInfoList;
                            // update the UI
                            // viewpager
                            //adapter.setSmileyInfos(smileyInfoList);
                            // interface with tab
                            for(int i=0;i<cateNum;i++){
                                mCommentSmileyTabLayout.removeAllTabs();
                                mCommentSmileyTabLayout.addTab(
                                        mCommentSmileyTabLayout.newTab().setText(String.valueOf(i+1))
                                );

                            }
                            // bind tablayout and viewpager
                            mCommentSmileyTabLayout.setupWithViewPager(mCommentSmileyViewPager);
                            smileyViewPagerAdapter adapter = new smileyViewPagerAdapter(getSupportFragmentManager(),
                                    FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
                            adapter.setCateNum(cateNum);
                            mCommentSmileyViewPager.setAdapter(adapter);

                        }
                    });


                }
            }
        });
    }

    @Override
    public void onSmileyPress(String str, Drawable a) {
        // remove \ and /
        String decodeStr = str.replace("/","")
                .replace("\\","");

        handler.insertSmiley(decodeStr,a);

        Log.d(TAG,"Press string "+decodeStr);
    }

    public class smileyViewPagerAdapter extends FragmentStatePagerAdapter{
        int cateNum = 0;

        public smileyViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void setCateNum(int cateNum) {
            this.cateNum = cateNum;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position+1);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            List<bbsParseUtils.smileyInfo> cateSmileyInfo = new ArrayList<>();
            for(int i=0;i<allSmileyInfos.size();i++){
                bbsParseUtils.smileyInfo smileyInfo = allSmileyInfos.get(i);
                if(smileyInfo.category == position){
                    cateSmileyInfo.add(smileyInfo);
                }
            }

            return SmileyFragment.newInstance(cateSmileyInfo);

        }

        @Override
        public int getCount() {
            return cateNum;
        }
    }




    private void getPageInfo(int page){
        privateMessageDetailSwipeRefreshLayout.setRefreshing(true);
        String apiStr = URLUtils.getPrivatePMDetailApiUrlByTouid(privateMessageInfo.toUid,page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"get public message in page "+page+" "+apiStr);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hasLoadAll = true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        privateMessageDetailSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        privateMessageDetailSwipeRefreshLayout.setRefreshing(false);
                    }
                });

                if(response.isSuccessful()&&response.body()!=null){
                    String s = response.body().string();

                    List<bbsParseUtils.privateDetailMessage> privateDetailMessages =  bbsParseUtils.parsePrivateDetailMessage(s,Integer.parseInt(userBriefInfo.uid));
                    int messagePerPage = bbsParseUtils.parsePrivateDetailMessagePerPage(s);
                    formHash = bbsParseUtils.parseFormHash(s);
                    pmid = bbsParseUtils.parsePrivateDetailMessagePmid(s);
                    globalPage = bbsParseUtils.parsePrivateDetailMessagePage(s);
                    globalPage -= 1;

                    if(privateDetailMessages!=null){
                        Log.d(TAG,"get PM "+privateDetailMessages.size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(page == -1){
                                    adapter.setPrivateDetailMessageList(privateDetailMessages);
                                    //privateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
                                }
                                else {
                                    adapter.addPrivateDetailMessageList(privateDetailMessages);
                                    //privateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
                                }
                            }
                        });


                    }
                    else {
                        hasLoadAll = true;
                    }
                }
                else {
                    hasLoadAll = false;
                }
            }
        });
    }



    private void getIntentInfo(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        privateMessageInfo = (bbsParseUtils.privateMessage) intent.getSerializableExtra(bbsConstUtils.PASS_PRIVATE_MESSAGE_KEY);
        // parse client
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        if(bbsInfo == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(R.string.bbs_notification_my_pm);
            getSupportActionBar().setSubtitle(privateMessageInfo.toUsername);
        }


    }


    private void sendPrivateMessage(){
        FormBody.Builder builder = new FormBody.Builder()
                .add("formhash",formHash)
                .add("topmuid",String.valueOf(privateMessageInfo.toUid));


        switch (getCharsetType()){
            case (CHARSET_GBK):{
                try {
                    builder.addEncoded("message", URLEncoder.encode(privateMessageCommentEditText.getText().toString(),"GBK"));
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            case (CHARSET_BIG5):{
                try {
                    builder.addEncoded("message", URLEncoder.encode(privateMessageCommentEditText.getText().toString(),"BIG5"));
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            default:{
                builder.add("message",privateMessageCommentEditText.getText().toString());
            }
        }
        FormBody formBody = builder.build();

        String apiStr = URLUtils.getSendPMApiUrl(privateMessageInfo.plid,Integer.parseInt(pmid));
        Log.d(TAG,"Send PM "+apiStr+" topmuid "+privateMessageInfo.toUid+" formhash "+formHash);
        Request request = new Request.Builder()
                .url(apiStr)
                .post(formBody)
                .build();
        privateMessageCommentButton.setEnabled(false);


        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        privateMessageCommentEditText.setText("");
                        Toasty.error(getApplicationContext(),
                                getString(R.string.network_failed),
                                Toast.LENGTH_SHORT
                        ).show();
                        privateMessageCommentButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        privateMessageCommentEditText.setText("");
                        privateMessageCommentButton.setEnabled(true);
                    }
                });
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv PM "+s);
                    globalPage = -1;
                    // need to post a delay to get information
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hasLoadAll = false;
                            getPageInfo(globalPage);
                        }
                    },500);

                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(getApplicationContext(),
                                    getString(R.string.network_failed),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
            }
        });
    }

    private void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
