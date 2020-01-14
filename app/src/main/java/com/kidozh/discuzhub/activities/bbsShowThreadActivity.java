package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment;
import com.kidozh.discuzhub.adapter.bbsForumThreadCommentAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.utilities.EmotionInputHandler;
import com.kidozh.discuzhub.utilities.RecyclerItemClickListener;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsSmileyPicker;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class bbsShowThreadActivity extends AppCompatActivity implements SmileyFragment.OnSmileyPressedInteraction,
        bbsForumThreadCommentAdapter.onAdapterReply {
    private final static String TAG = bbsShowThreadActivity.class.getSimpleName();
    @BindView(R.id.bbs_thread_detail_recyclerview)
    RecyclerView mRecyclerview;

    @BindView(R.id.bbs_thread_detail_swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.bbs_thread_detail_comment_editText)
    EditText mCommentEditText;
    @BindView(R.id.bbs_thread_detail_comment_button)
    Button mCommentBtn;
    @BindView(R.id.bbs_comment_constraintLayout)
    ConstraintLayout mCommentConstraintLayout;
    @BindView(R.id.bbs_thread_detail_reply_chip)
    Chip mThreadReplyBadge;
    @BindView(R.id.bbs_thread_detail_reply_content)
    TextView mThreadReplyContent;
    @BindView(R.id.bbs_thread_detail_no_more_item_found)
    TextView noMoreThreadFound;
    @BindView(R.id.bbs_comment_smiley_constraintLayout)
    ConstraintLayout mCommentSmileyConstraintLayout;
    @BindView(R.id.bbs_comment_smiley_tabLayout)
    TabLayout mCommentSmileyTabLayout;
    @BindView(R.id.bbs_comment_smiley_viewPager)
    ViewPager mCommentSmileyViewPager;
    @BindView(R.id.bbs_thread_detail_emoij_button)
    ImageView mCommentEmoijBtn;

//    @BindView(R.id.error_thread_cardview)
//    CardView errorThreadCardview;
    int page = 1;
    public String tid,subject,fid;
    private OkHttpClient client = new OkHttpClient();
    private bbsForumThreadCommentAdapter adapter;
    boolean isTaskRunning,hasLoadAll=false;
    String formHash = null;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    forumInfo forum;

    List<bbsParseUtils.smileyInfo> allSmileyInfos;
    int smileyCateNum;


    private threadCommentInfo selectedThreadComment =null;
    private bbsSmileyPicker smileyPicker;
    private EmotionInputHandler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_thread);
        ButterKnife.bind(this);
        configureIntentData();
        configureClient();

        Intent intent = getIntent();
        tid = intent.getStringExtra("TID");
        fid = intent.getStringExtra("FID");
        subject = intent.getStringExtra("SUBJECT");

        configureToolbar();
        configureRecyclerview();
        getThreadComment();

        configureSwipeRefreshLayout();
        configureCommentBtn();
        configureSmileyLayout();

    }

    private void configureIntentData(){
        Intent intent = getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
    }

    private void configureSmileyLayout(){
        handler = new EmotionInputHandler(mCommentEditText, (enable, s) -> {

        });

        smileyPicker = new bbsSmileyPicker(this);
        smileyPicker.setListener((str,a)->{
            handler.insertSmiley(str,a);
        });
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getThreadComment();


            }
        });
    }

    private void configureClient(){
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    private void configureCommentBtn(){
        mCommentBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String commentMessage = mCommentEditText.getText().toString();
                if(commentMessage.length() == 0){
                    Toasty.info(getApplicationContext(),getString(R.string.bbs_require_comment),Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG,"SELECTED THREAD COMMENT "+selectedThreadComment);
                    if(selectedThreadComment == null){
                        // directly comment thread
                        postCommentToThread(commentMessage);

                    }
                    else {
                        String pid = selectedThreadComment.pid;
                        Log.d(TAG,"Send Reply to "+pid);
                        postReplyToSomeoneInThread(pid,commentMessage,selectedThreadComment.message);
                    }

                }
            }
        });

        mCommentEmoijBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCommentSmileyConstraintLayout.getVisibility() == View.GONE){
                    // smiley picker not visible
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.vector_drawable_keyboard_24px));

                    mCommentEditText.clearFocus();
                    // close keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm !=null){
                        imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(),0);
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

        mCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                .url(bbsURLUtils.getSmileyApiUrl())
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

    @Override
    public void replyToSomeOne(int position) {
        threadCommentInfo threadCommentInfo = adapter.threadInfoList.get(position);
        selectedThreadComment = threadCommentInfo;
        mThreadReplyBadge.setText(threadCommentInfo.author);
        mThreadReplyBadge.setVisibility(View.VISIBLE);
        mCommentEditText.setHint(String.format("@%s",threadCommentInfo.author));
        String decodeString = threadCommentInfo.message;

        Spanned sp = Html.fromHtml(decodeString);

        mThreadReplyContent.setText(sp, TextView.BufferType.SPANNABLE);
        mThreadReplyContent.setVisibility(View.VISIBLE);
        mThreadReplyBadge.setOnCloseIconClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mThreadReplyBadge.setVisibility(View.GONE);
                mThreadReplyContent.setVisibility(View.GONE);
                mCommentEditText.setHint(R.string.bbs_thread_say_something);
                selectedThreadComment = null;
            }
        });
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

    private void configureRecyclerview(){
        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new bbsForumThreadCommentAdapter(this,bbsInfo,userBriefInfo);
        adapter.subject =subject;
        mRecyclerview.setAdapter(adapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    getThreadComment();

                }
            }

            public boolean isScrollAtEnd(){

                if (mRecyclerview.computeVerticalScrollExtent() + mRecyclerview.computeVerticalScrollOffset()
                        >= mRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });

        mRecyclerview.addOnItemTouchListener(new RecyclerItemClickListener(this,mRecyclerview,new RecyclerItemClickListener.OnItemClickListener(){

            @Override
            public void onItemClick(View view, int position) {


            }

            @Override
            public void onItemLongClick(View view, int position) {
                threadCommentInfo threadCommentInfo = adapter.threadInfoList.get(position);
                selectedThreadComment = threadCommentInfo;
                mThreadReplyBadge.setText(threadCommentInfo.author);
                mThreadReplyBadge.setVisibility(View.VISIBLE);
                mCommentEditText.setHint(String.format("@%s",threadCommentInfo.author));
                String decodeString = threadCommentInfo.message;

                Spanned sp = Html.fromHtml(decodeString);

                mThreadReplyContent.setText(sp, TextView.BufferType.SPANNABLE);
                mThreadReplyContent.setVisibility(View.VISIBLE);
                mThreadReplyBadge.setOnCloseIconClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        mThreadReplyBadge.setVisibility(View.GONE);
                        mThreadReplyContent.setVisibility(View.GONE);
                        mCommentEditText.setHint(R.string.bbs_thread_say_something);
                        selectedThreadComment = null;
                    }
                });
            }
        }));
        
    }

    private void getThreadComment(){
        if(hasLoadAll){return;}
        swipeRefreshLayout.setRefreshing(true);
        String apiStr = bbsURLUtils.getThreadCommentUrlByFid(Integer.parseInt(tid),page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        isTaskRunning = true;
        Log.d(TAG,"get thread URL "+apiStr);
        Handler mHandler = new Handler(Looper.getMainLooper());
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        Toasty.error(getApplicationContext(),
                                getApplicationContext().getString(R.string.network_failed),
                                Toast.LENGTH_LONG).show();
                        noMoreThreadFound.setVisibility(View.VISIBLE);
                    }
                });
                isTaskRunning = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            if(s == null){
                                noMoreThreadFound.setVisibility(View.VISIBLE);
                                return;
                            }
                            else{
                                Log.d(TAG,"Get Thread "+s);
                            }

                            String curFormHash = bbsParseUtils.parseFormHash(s);
                            forumUserBriefInfo bbsPersonInfo = bbsParseUtils.parseBreifUserInfo(s);

                            if(bbsPersonInfo == null){
                                mCommentConstraintLayout.setVisibility(View.GONE);
                                mCommentBtn.setText(R.string.bbs_require_login_to_comment);
                                mCommentBtn.setEnabled(false);
                                mCommentEditText.setEnabled(false);
                            }

                            if(curFormHash != null){
                                formHash = curFormHash;
                                //mCommentConstraintLayout.setVisibility(View.VISIBLE);
                            }
                            else {

                            }
                            if(s!=null){
                                Log.d(TAG,s);

                            }
                            else {
                                hasLoadAll = true;
                                noMoreThreadFound.setVisibility(View.VISIBLE);
                                Log.d(TAG,"Getting Null value");
                                return;
                            }
                            List<threadCommentInfo> threadInfoList;
                            threadInfoList = bbsParseUtils.parseThreadCommentInfo(s);


                            if(threadInfoList!=null&& threadInfoList.size() !=0){
                                Log.d(TAG,"Getting threadList size "+threadInfoList.size());
                                if(adapter.threadInfoList == null || page == 1 || page == 0){
                                    adapter.setThreadInfoList(threadInfoList);
                                }
                                else {
                                    adapter.threadInfoList.addAll(threadInfoList);
                                    adapter.notifyDataSetChanged();
                                }
                                if(threadInfoList.size()<15){
                                    hasLoadAll = true;
                                    noMoreThreadFound.setVisibility(View.VISIBLE);
                                }

                            }

                            else {
                                Log.d(TAG,"Getting thread List is null get page"+page);
                                noMoreThreadFound.setVisibility(View.VISIBLE);
                                if(page == 1){
                                    hasLoadAll = true;

                                }
                                else {
                                    hasLoadAll = true;
                                }

                            }

                            page += 1;
                            isTaskRunning = false;

                        }
                    });
                }
                else {
                    isTaskRunning = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(getApplicationContext(),getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                            noMoreThreadFound.setVisibility(View.VISIBLE);
                        }
                    });

                }

            }
        });
    }

    private void postCommentToThread(String message){
        Date timeGetTime = new Date();
        FormBody formBody = new FormBody.Builder()
                .add("message", message)
                .add("subject", "")
                .add("usesig", "1")
                .add("posttime",String.valueOf(timeGetTime.getTime() / 1000 - 1))
                .add("formhash",formHash)
                .build();
        Log.d(TAG,"get Form "+message+" hash "
                +formHash+" fid "+fid+" tid "+tid
                + " API ->"+bbsURLUtils.getReplyThreadUrl(fid,tid)+" postTime "+String.valueOf(timeGetTime.getTime() / 1000 - 30));
        Request request = new Request.Builder()
                .url(bbsURLUtils.getReplyThreadUrl(fid,tid))
                .post(formBody)
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        // UI Change
        mCommentBtn.setText(R.string.bbs_commentting);
        mCommentBtn.setEnabled(false);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCommentBtn.setText(R.string.bbs_thread_comment);
                        mCommentBtn.setEnabled(true);
                        Toasty.error(getApplicationContext(),getString(R.string.bbs_comment_failed),Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv comment info "+s);

                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCommentBtn.setText(R.string.bbs_thread_comment);
                        mCommentBtn.setEnabled(true);
                        mCommentEditText.setText("");
                        page = 1;
                        hasLoadAll = false;
                        getThreadComment();
                        Toasty.success(getApplicationContext(),getString(R.string.bbs_comment_successfully),Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void postReplyToSomeoneInThread(String replyPid,String message,String noticeAuthorMsg){

        String discuz_reply_comment_template = getString(R.string.discuz_reply_message_template);
        String replyUserName = selectedThreadComment.author;
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, Locale.getDefault());
        String publishAtString = df.format(selectedThreadComment.publishAt);
        int MAX_CHAR_LENGTH = 300;
        int trimEnd = Math.min(MAX_CHAR_LENGTH,noticeAuthorMsg.length());
        Log.d(TAG,"get Reply Form "+message+" hash "
                +formHash+" reppid "+replyPid+" tid "+tid
                + " API ->"+bbsURLUtils.getReplyThreadUrl(fid,tid)+" noticeTriStr "+String.format(discuz_reply_comment_template,
                replyUserName,publishAtString,noticeAuthorMsg.substring(0,trimEnd)));
        String replyMessage = noticeAuthorMsg.substring(0,trimEnd);
        if(noticeAuthorMsg.length()>MAX_CHAR_LENGTH){
            replyMessage += "...";
        }

        FormBody formBody = new FormBody.Builder()
                .add("formhash",formHash)
                .add("handlekey","reply")

                .add("usesig", "1")
                .add("reppid", replyPid)
                .add("reppost", replyPid)
                .add("message", message)
                .add("noticeauthormsg",noticeAuthorMsg)
                .add("noticetrimstr",String.format(discuz_reply_comment_template,
                        replyUserName,publishAtString,replyMessage))
//                .add("noticeauthor",noticeauthor)
                //.add("subject", message)
                .build();
        Request request = new Request.Builder()
                .url(bbsURLUtils.getReplyToSomeoneThreadUrl(fid,tid))
                .post(formBody)
                .build();

        mCommentBtn.setText(R.string.bbs_commentting);
        mCommentBtn.setEnabled(false);
        String pid = selectedThreadComment.pid;
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCommentBtn.setText(R.string.bbs_thread_comment);
                        mCommentBtn.setEnabled(true);
                        Toasty.error(getApplicationContext(),getString(R.string.bbs_comment_failed),Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv reply comment info "+s);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCommentBtn.setText(R.string.bbs_thread_comment);
                            mCommentBtn.setEnabled(true);
                            mCommentEditText.setText("");
                            page = 1;
                            hasLoadAll = false;
                            getThreadComment();
                            Toasty.success(getApplicationContext(),getString(R.string.bbs_comment_successfully),Toast.LENGTH_LONG).show();
                        }
                    });

                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCommentBtn.setText(R.string.bbs_thread_comment);
                            mCommentBtn.setEnabled(true);
                            Toasty.error(getApplicationContext(),getString(R.string.bbs_comment_failed),Toast.LENGTH_LONG).show();
                        }
                    });
                }


            }
        });


    }

    private void configureToolbar(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(subject);
    }






    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        configureIntentData();
        if(userBriefInfo == null){
            getMenuInflater().inflate(R.menu.menu_bbs_user_status, menu);
        }
        else {

        }


        return true;
    }

}
