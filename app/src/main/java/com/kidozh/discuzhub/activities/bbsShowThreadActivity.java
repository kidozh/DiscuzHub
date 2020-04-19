package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.bbsPollFragment.bbsPollFragment;
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment;
import com.kidozh.discuzhub.adapter.bbsForumThreadCommentAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
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
import com.kidozh.discuzhub.viewModels.ThreadDetailViewModel;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        bbsForumThreadCommentAdapter.onFilterChanged,
        bbsForumThreadCommentAdapter.onAdapterReply,
        bbsPollFragment.OnFragmentInteractionListener{
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
    //int page = 1;
    public String tid,subject,fid;
    private OkHttpClient client = new OkHttpClient();
    private bbsForumThreadCommentAdapter adapter;
    boolean isTaskRunning;
    String formHash = null;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    forumInfo forum;

    // MutableLiveData<bbsURLUtils.ThreadStatus> threadStatusMutableLiveData;

    List<bbsParseUtils.smileyInfo> allSmileyInfos;
    int smileyCateNum;

    bbsPollInfo pollInfo;


    private threadCommentInfo selectedThreadComment =null;
    private bbsSmileyPicker smileyPicker;
    private EmotionInputHandler handler;

    private ThreadDetailViewModel threadDetailViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_thread);
        ButterKnife.bind(this);
        threadDetailViewModel = new ViewModelProvider(this).get(ThreadDetailViewModel.class);
        configureIntentData();
        initThreadStatus();
        configureClient();
        configureToolbar();
        bindViewModel();
        configureRecyclerview();
        threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
        // getThreadComment();

        configureSwipeRefreshLayout();
        configureCommentBtn();
        configureSmileyLayout();

    }

    private void configureIntentData(){
        Intent intent = getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        tid = intent.getStringExtra("TID");
        fid = intent.getStringExtra("FID");
        subject = intent.getStringExtra("SUBJECT");
        bbsURLUtils.setBBS(bbsInfo);
        threadDetailViewModel.setBBSInfo(bbsInfo, userBriefInfo, forum, tid);

    }

    private void initThreadStatus(){
        bbsURLUtils.ThreadStatus threadStatus = new bbsURLUtils.ThreadStatus(Integer.parseInt(tid),1);
        Log.d(TAG,"Set status when init data");
        threadDetailViewModel.threadStatusMutableLiveData.setValue(threadStatus);
    }

    private void configureSmileyLayout(){
        handler = new EmotionInputHandler(mCommentEditText, (enable, s) -> {

        });

        smileyPicker = new bbsSmileyPicker(this);
        smileyPicker.setListener((str,a)->{
            handler.insertSmiley(str,a);
        });
    }

    private void bindViewModel(){
        // for personal info
        threadDetailViewModel.bbsPersonInfoMutableLiveData.observe(this, new Observer<forumUserBriefInfo>() {
            @Override
            public void onChanged(forumUserBriefInfo userBriefInfo) {
                Log.d(TAG,"User info "+userBriefInfo);
                if(userBriefInfo == null){
                    mCommentConstraintLayout.setVisibility(View.GONE);
//                    mCommentBtn.setText(R.string.bbs_require_login_to_comment);
//                    mCommentBtn.setEnabled(false);
//                    mCommentEditText.setEnabled(false);
                }
                else{
                    mCommentConstraintLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        threadDetailViewModel.threadCommentInfoListLiveData.observe(this, new Observer<List<threadCommentInfo>>() {
            @Override
            public void onChanged(List<threadCommentInfo> threadCommentInfos) {
                adapter.setThreadInfoList(threadCommentInfos, threadDetailViewModel.threadStatusMutableLiveData.getValue());
            }
        });

        threadDetailViewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        threadDetailViewModel.hasLoadAll.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    noMoreThreadFound.setVisibility(View.VISIBLE);
                }
                else {
                    noMoreThreadFound.setVisibility(View.GONE);
                }
            }
        });

        threadDetailViewModel.error.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toasty.error(getApplication(), getString(R.string.network_failed), Toast.LENGTH_LONG).show();
                }

            }
        });
        threadDetailViewModel.errorText.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorText) {
                if(!errorText.equals("")){
                    noMoreThreadFound.setText(errorText);
                }
                else {
                    noMoreThreadFound.setText(R.string.bbs_no_thread_gotten);
                }
            }
        });

        threadDetailViewModel.pollInfoLiveData.observe(this, new Observer<bbsPollInfo>() {
            @Override
            public void onChanged(bbsPollInfo bbsPollInfo) {
                if(pollInfo!=null){
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.bbs_thread_poll_fragment,bbsPollFragment.newInstance(pollInfo,userBriefInfo,Integer.parseInt(tid),formHash));
                    fragmentTransaction.commit();
                }

            }
        });

        threadDetailViewModel.formHash.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                formHash = s;
            }
        });

        threadDetailViewModel.threadStatusMutableLiveData.observe(this, new Observer<bbsURLUtils.ThreadStatus>() {
            @Override
            public void onChanged(bbsURLUtils.ThreadStatus threadStatus) {

                Log.d(TAG,"Livedata changed " + threadStatus.datelineAscend);
                if(getSupportActionBar()!=null){

                    if(threadStatus.datelineAscend){

                        getSupportActionBar().setSubtitle(getString(R.string.bbs_thread_status_ascend));
                    }
                    else {
                        getSupportActionBar().setSubtitle(getString(R.string.bbs_thread_status_descend));
                    }
                }

            }
        });
    }



    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Boolean isLoading = threadDetailViewModel.isLoading.getValue();
                if(!isLoading){
                    reloadThePage();
                    threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
                }

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
                if(commentMessage.length() < 1){
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
        threadCommentInfo threadCommentInfo = adapter.getThreadInfoList().get(position);
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

    @Override
    public void onPollResultFetched() {
        // reset poll to get realtime result
        pollInfo = null;

        reloadThePage();
        //threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());

    }

    @Override
    public void setAuthorId(int authorId) {
        bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();

        if(threadStatus!=null){
            threadStatus.setInitAuthorId(authorId);
        }

        //threadDetailViewModel.threadStatusMutableLiveData.setValue(threadStatus);
        reloadThePage(threadStatus);

        // refresh it
        threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
        // getThreadComment();
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
        adapter = new bbsForumThreadCommentAdapter(this,
                bbsInfo,
                userBriefInfo,
                threadDetailViewModel.threadStatusMutableLiveData.getValue());
        adapter.subject =subject;
        mRecyclerview.setAdapter(adapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                    if(threadStatus !=null){
                        if(threadDetailViewModel.hasLoadAll.getValue()){
                            //Toasty.info(getApplication(),getString(R.string.bbs_forum_thread_load_all),Toast.LENGTH_LONG).show();
                        }
                        else {
                            threadStatus.page += 1;
                            threadDetailViewModel.getThreadDetail(threadStatus);
                        }

                    }

                    // getThreadComment();

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

    }

    private void getThreadComment(){
        bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        if(threadStatus !=null && threadStatus.hasLoadAll){
            //Toasty.info(this,getString(R.string.bbs_forum_thread_load_all),Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(threadStatus.page == 1){
            // clear it at first
            adapter.setThreadInfoList(new ArrayList<>(),threadStatus);
        }
        //swipeRefreshLayout.setRefreshing(true);
        String apiStr = bbsURLUtils.getThreadCommentUrlByStatus(threadStatus);
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
                    Log.d(TAG,"Get Thread "+s);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            if(s == null){
                                noMoreThreadFound.setVisibility(View.VISIBLE);
                                return;
                            }


                            String curFormHash = bbsParseUtils.parseFormHash(s);
                            if(curFormHash != null){
                                formHash = curFormHash;
                                //mCommentConstraintLayout.setVisibility(View.VISIBLE);
                            }

                            forumUserBriefInfo bbsPersonInfo = bbsParseUtils.parseBreifUserInfo(s);
                            // parse poll if possible
                            if(pollInfo == null){
                                pollInfo = bbsParseUtils.parsePollInfo(s);
                                Log.d(TAG,"POLL info "+pollInfo);
                                // start fragment
                                if(pollInfo!=null){
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.bbs_thread_poll_fragment,bbsPollFragment.newInstance(pollInfo,userBriefInfo,Integer.parseInt(tid),formHash));
                                    fragmentTransaction.commit();
                                }

                            }


                            if(bbsPersonInfo == null){
                                mCommentConstraintLayout.setVisibility(View.GONE);
                                mCommentBtn.setText(R.string.bbs_require_login_to_comment);
                                mCommentBtn.setEnabled(false);
                                mCommentEditText.setEnabled(false);
                            }

                            List<threadCommentInfo> threadInfoList;
                            threadInfoList = bbsParseUtils.parseThreadCommentInfo(s);


                            if(threadInfoList!=null&& threadInfoList.size() !=0){
                                Log.d(TAG,"Getting threadList size "+threadInfoList.size());
                                if(adapter.getThreadInfoList() == null || threadStatus.page == 1){
                                    adapter.setThreadInfoList(threadInfoList,threadStatus);
                                }
                                else {
                                    adapter.getThreadInfoList().addAll(threadInfoList);
                                    adapter.notifyDataSetChanged();
                                }
                                if(threadInfoList.size()==0){
                                    threadStatus.hasLoadAll = true;
                                    noMoreThreadFound.setVisibility(View.VISIBLE);
                                }
                                if(threadInfoList.size() >= threadStatus.perPage){
                                    threadStatus.page += 1;
                                    isTaskRunning = false;
                                }
                                else {
                                    threadStatus.hasLoadAll = true;
                                    noMoreThreadFound.setVisibility(View.VISIBLE);
                                }

                            }
                            else {
                                Log.d(TAG,"Getting thread List is null get page"+threadStatus.page);
                                noMoreThreadFound.setVisibility(View.VISIBLE);
                                threadStatus.hasLoadAll = true;

                            }



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
                + " API ->"+bbsURLUtils.getReplyThreadUrl(fid,tid)+" formbody "+formBody.toString());
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
                    bbsParseUtils.returnMessage returnedMessage = bbsParseUtils.parseReturnMessage(s);
                    if(returnedMessage!=null && returnedMessage.value.equals("post_reply_succeed")){
                        // success!
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCommentBtn.setText(R.string.bbs_thread_comment);
                                mCommentBtn.setEnabled(true);
                                mCommentEditText.setText("");
                                reloadThePage();
                                threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
                                //getThreadComment();
                                Toasty.success(getApplicationContext(),returnedMessage.string,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCommentBtn.setText(R.string.bbs_thread_comment);
                                mCommentBtn.setEnabled(true);
                                if(returnedMessage == null){
                                    Toasty.error(getApplicationContext(), getString(R.string.network_failed), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toasty.error(getApplicationContext(), returnedMessage.string, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }



            }
        });
    }

    private void reloadThePage(){
        bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        if(threadStatus!=null){

            threadStatus.setInitPage(1);
        }
        Log.d(TAG,"Set status when reload page");
        threadDetailViewModel.threadStatusMutableLiveData.setValue(threadStatus);
    }

    private void reloadThePage(bbsURLUtils.ThreadStatus threadStatus){
        if(threadStatus!=null){
            threadStatus.setInitPage(1);
        }
        Log.d(TAG,"Set status when init data "+threadStatus);
        threadDetailViewModel.threadStatusMutableLiveData.setValue(threadStatus);
    }

    private void postReplyToSomeoneInThread(String replyPid,String message,String noticeAuthorMsg){

        String discuz_reply_comment_template = getString(R.string.discuz_reply_message_template);
        String replyUserName = selectedThreadComment.author;
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, Locale.getDefault());
        String publishAtString = df.format(selectedThreadComment.publishAt);
        int MAX_CHAR_LENGTH = 300;
        // int trimEnd = Math.min(MAX_CHAR_LENGTH,noticeAuthorMsg.length());
        // not to trim
        int trimEnd = noticeAuthorMsg.length();
        Log.d(TAG,"get Reply Form "+message+" hash "
                +formHash+" reppid "+replyPid+" tid "+tid
                + " API ->"+bbsURLUtils.getReplyThreadUrl(fid,tid)+
                " noticeTriStr "+String.format(discuz_reply_comment_template,
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
                .url(bbsURLUtils.getReplyThreadUrl(fid,tid))
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
                        Toasty.error(getApplicationContext(),getString(R.string.network_failed),Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String s = response.body().string();
                    bbsParseUtils.returnMessage returnedMessage = bbsParseUtils.parseReturnMessage(s);

                    Log.d(TAG, "Recv reply comment info " + s);
                    if(returnedMessage!=null && returnedMessage.value.equals("post_reply_succeed")) {
                        // success!
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCommentBtn.setText(R.string.bbs_thread_comment);
                                mCommentBtn.setEnabled(true);
                                mCommentEditText.setText("");
                                reloadThePage();
                                threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
                                //getThreadComment();
                                Toasty.success(getApplicationContext(), returnedMessage.string, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCommentBtn.setText(R.string.bbs_thread_comment);
                                mCommentBtn.setEnabled(true);
                                if(returnedMessage == null){
                                    Toasty.error(getApplicationContext(), getString(R.string.network_failed), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toasty.error(getApplicationContext(), returnedMessage.string, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
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
        bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        String currentUrl = "";
        if(threadStatus == null){
            currentUrl = bbsURLUtils.getViewThreadUrl(tid,"1");
        }
        else {
            currentUrl = bbsURLUtils.getViewThreadUrl(tid,String.valueOf(threadStatus.page));
        }

        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            case R.id.bbs_forum_nav_personal_center:{
                Intent intent = new Intent(this, showPersonalInfoActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra("UID",String.valueOf(userBriefInfo.uid));
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
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                startActivity(intent,null);
                return true;

            }
            case R.id.bbs_forum_nav_show_in_webview:{
                Intent intent = new Intent(this, showWebPageActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
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
            case R.id.bbs_forum_nav_dateline_sort:{
                Context context = this;
                threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                Log.d(TAG,"You press sort btn "+threadStatus.datelineAscend);
                // bbsURLUtils.ThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                if(threadStatus!=null){
                    threadStatus.datelineAscend = !threadStatus.datelineAscend;
                    Log.d(TAG,"Changed Ascend mode "+threadStatus.datelineAscend);


                    Log.d(TAG,"Apply Ascend mode "+threadDetailViewModel.threadStatusMutableLiveData.getValue().datelineAscend);
                    reloadThePage(threadStatus);
                    Log.d(TAG,"After reload Ascend mode "+threadDetailViewModel.threadStatusMutableLiveData.getValue().datelineAscend);

                    if(threadStatus.datelineAscend){
                        Toasty.success(context,getString(R.string.bbs_thread_status_ascend),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toasty.success(context,getString(R.string.bbs_thread_status_descend),Toast.LENGTH_SHORT).show();
                    }
                    // reload the parameters
                    Log.d(TAG,"dateline ascend "+threadStatus.datelineAscend);

                    threadDetailViewModel.getThreadDetail(threadStatus);

                    invalidateOptionsMenu();
                    // invalidateOptionsMenu();

                }
                return true;
            }
            case R.id.bbs_about_app:{
                Intent intent = new Intent(this,aboutAppActivity.class);
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // configureIntentData();

        if(userBriefInfo == null){
            getMenuInflater().inflate(R.menu.bbs_incognitive_thread_nav_menu, menu);

        }
        else {
            getMenuInflater().inflate(R.menu.bbs_thread_nav_menu,menu);
            if(getSupportActionBar()!=null){

                bbsURLUtils.ThreadStatus ThreadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                if(ThreadStatus !=null){
                    Log.d(TAG,"ON CREATE GET ascend mode in menu "+ThreadStatus.datelineAscend);
                    if(ThreadStatus.datelineAscend){
                        menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_upward_24px));
                    }
                    else {
                        menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_downward_24px));
                    }

                }

            }
        }


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        bbsURLUtils.ThreadStatus ThreadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        if(ThreadStatus !=null){
            Log.d(TAG,"ON PREPARE GET ascend mode in menu "+ThreadStatus.datelineAscend);
            if(ThreadStatus.datelineAscend){
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_upward_24px));
            }
            else {
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_downward_24px));
            }
            // invalidateOptionsMenu();
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
