package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.bbsPollFragment.bbsPollFragment;
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment;
import com.kidozh.discuzhub.adapter.PostAdapter;
import com.kidozh.discuzhub.adapter.ThreadCountAdapter;
import com.kidozh.discuzhub.adapter.ThreadPropertiesAdapter;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.dialogs.ReportPostDialogFragment;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.entities.ThreadCount;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.ViewThreadQueryStatus;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ApiMessageActionResult;
import com.kidozh.discuzhub.results.BuyThreadResult;
import com.kidozh.discuzhub.results.MessageResult;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.ThreadResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.EmotionInputHandler;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsSmileyPicker;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.viewModels.ThreadViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;


public class ThreadActivity extends BaseStatusActivity implements SmileyFragment.OnSmileyPressedInteraction,
        PostAdapter.onFilterChanged,
        PostAdapter.onAdapterReply,
        PostAdapter.OnLinkClicked,
        bbsPollFragment.OnFragmentInteractionListener,
        ThreadPropertiesAdapter.OnThreadPropertyClicked,
        PostAdapter.OnAdvanceOptionClicked,
        ReportPostDialogFragment.ReportDialogListener,
        ThreadCountAdapter.OnRecommendBtnPressed{
    private final static String TAG = ThreadActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.posts_recyclerview)
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
    
    @BindView(R.id.smiley_root_layout)
    View SmileyRootLayout;
    @BindView(R.id.bbs_comment_smiley_tabLayout)
    TabLayout mCommentSmileyTabLayout;
    @BindView(R.id.bbs_comment_smiley_viewPager)
    ViewPager mCommentSmileyViewPager;
    @BindView(R.id.bbs_thread_detail_emoij_button)
    ImageView mCommentEmoijBtn;
    @BindView(R.id.bbs_thread_interactive_recyclerview)
    RecyclerView mDetailThreadTypeRecyclerview;
    @BindView(R.id.bbs_thread_comment_number)
    TextView mDetailedThreadCommentNumber;
    @BindView(R.id.bbs_thread_view_number)
    TextView mDetailedThreadViewNumber;
    @BindView(R.id.bbs_thread_subject)
    TextView mDetailThreadSubjectTextview;
    @BindView(R.id.bbs_thread_property_recyclerview)
    RecyclerView mDetailThreadPropertyRecyclerview;
    @BindView(R.id.bbs_post_captcha_imageview)
    ImageView mPostCaptchaImageview;
    @BindView(R.id.bbs_post_captcha_editText)
    EditText mPostCaptchaEditText;
    @BindView(R.id.error_content)
    TextView errorContent;
    @BindView(R.id.error_view)
    View errorView;
    @BindView(R.id.error_value)
    TextView errorValue;
    @BindView(R.id.error_icon)
    ImageView errorIcon;
    @BindView(R.id.advance_post_icon)
    ImageView mAdvancePostIcon;


    public String subject;
    public int tid, fid;
    //private OkHttpClient client = new OkHttpClient();
    private PostAdapter adapter;
    private ThreadCountAdapter countAdapter;
    private ThreadPropertiesAdapter propertiesAdapter;
    String formHash = null;

    ForumInfo forum;
    ThreadInfo threadInfo;
    private boolean hasLoadOnce = false, notifyLoadAll = false;

    List<bbsParseUtils.smileyInfo> allSmileyInfos;
    int smileyCateNum;

    bbsPollInfo pollInfo;


    private PostInfo selectedThreadComment =null;
    private bbsSmileyPicker smileyPicker;
    private EmotionInputHandler handler;

    private ThreadViewModel threadDetailViewModel;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_thread);

        ButterKnife.bind(this);
        threadDetailViewModel = new ViewModelProvider(this).get(ThreadViewModel.class);
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
        threadInfo = (ThreadInfo) intent.getSerializableExtra(bbsConstUtils.PASS_THREAD_KEY);
        tid = intent.getIntExtra("TID",0);
        fid = intent.getIntExtra("FID",0);
        subject = intent.getStringExtra("SUBJECT");
        // hasLoadOnce = intent.getBooleanExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,false);
        URLUtils.setBBS(bbsInfo);
        threadDetailViewModel.setBBSInfo(bbsInfo, userBriefInfo, forum, tid);
        if(threadInfo!=null && threadInfo.subject !=null){
            Spanned sp = Html.fromHtml(threadInfo.subject);
            SpannableString spannableString = new SpannableString(sp);
            mDetailThreadSubjectTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
        }

    }

    private void initThreadStatus(){
        ViewThreadQueryStatus viewThreadQueryStatus = new ViewThreadQueryStatus(tid,1);
        Log.d(TAG,"Set status when init data");
        threadDetailViewModel.threadStatusMutableLiveData.setValue(viewThreadQueryStatus);
    }

    private boolean checkWithPerm(int status, int perm){
        return (status & perm) != 0;
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
                if(userBriefInfo == null || userBriefInfo.auth == null){
                    mCommentConstraintLayout.setVisibility(View.GONE);
                }
                else{
                    mCommentConstraintLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        threadDetailViewModel.threadCommentInfoListLiveData.observe(this, new Observer<List<PostInfo>>() {
            @Override
            public void onChanged(List<PostInfo> postInfos) {
                int authorid = 0;
                ThreadResult threadResult = threadDetailViewModel.threadPostResultMutableLiveData.getValue();
                if(threadResult!=null
                        && threadResult.threadPostVariables!=null
                        && threadResult.threadPostVariables.detailedThreadInfo!=null){
                    authorid = threadResult.threadPostVariables.detailedThreadInfo.authorId;
                }
                adapter.setThreadInfoList(postInfos,threadDetailViewModel.threadStatusMutableLiveData.getValue(),authorid);
                if(adapter.getItemCount() == 0){
                    errorView.setVisibility(View.VISIBLE);
                    if(threadDetailViewModel.errorMessageMutableLiveData.getValue() == null){
                        errorView.setVisibility(View.VISIBLE);
                        errorIcon.setImageResource(R.drawable.ic_blank_forum_thread_64px);
                        errorValue.setText("");
                        errorContent.setText(getString(R.string.discuz_network_result_null));
                    }
                }
                else {
                    errorView.setVisibility(View.GONE);
                }
            }
        });

        threadDetailViewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });
        
        threadDetailViewModel.errorMessageMutableLiveData.observe(this, errorMessage ->{
            if(errorMessage!=null){
                Toasty.error(getApplication(), 
                        getString(R.string.discuz_api_message_template,errorMessage.key,errorMessage.content),
                        Toast.LENGTH_LONG).show();
                errorView.setVisibility(View.VISIBLE);
                errorIcon.setImageResource(R.drawable.ic_error_outline_24px);
                errorValue.setText(errorMessage.key);
                errorContent.setText(errorMessage.content);
                VibrateUtils.vibrateForError(getApplication());
            }
        });



        threadDetailViewModel.pollInfoLiveData.observe(this, new Observer<bbsPollInfo>() {
            @Override
            public void onChanged(bbsPollInfo bbsPollInfo) {
                if(bbsPollInfo!=null){
                    Log.d(TAG, "get poll "+ bbsPollInfo.votersCount);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.bbs_thread_poll_fragment,
                            bbsPollFragment.newInstance(bbsPollInfo,userBriefInfo,tid,formHash));
                    fragmentTransaction.commit();
                }
                else {
                    Log.d(TAG,"get poll is null");
                }

            }
        });

        threadDetailViewModel.formHash.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                formHash = s;
            }
        });

        threadDetailViewModel.threadStatusMutableLiveData.observe(this, new Observer<ViewThreadQueryStatus>() {
            @Override
            public void onChanged(ViewThreadQueryStatus viewThreadQueryStatus) {

                Log.d(TAG,"Livedata changed " + viewThreadQueryStatus.datelineAscend);
                if(getSupportActionBar()!=null){

                    if(viewThreadQueryStatus.datelineAscend){

                        getSupportActionBar().setSubtitle(getString(R.string.bbs_thread_status_ascend));
                    }
                    else {
                        getSupportActionBar().setSubtitle(getString(R.string.bbs_thread_status_descend));
                    }
                }

            }
        });
        threadDetailViewModel.detailedThreadInfoMutableLiveData.observe(this, new Observer<bbsParseUtils.DetailedThreadInfo>() {
            @Override
            public void onChanged(bbsParseUtils.DetailedThreadInfo detailedThreadInfo) {
                // closed situation
                // prepare notification list

                List<ThreadCount> threadNotificationList = new ArrayList<>();
                List<ThreadCount> threadPropertyList = new ArrayList<>();
                if(detailedThreadInfo.subject!=null){
                    Spanned sp = Html.fromHtml(detailedThreadInfo.subject);
                    SpannableString spannableString = new SpannableString(sp);
                    mDetailThreadSubjectTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
                }
                if(detailedThreadInfo.closed != 0){
                    mCommentEditText.setEnabled(false);
                    mCommentBtn.setEnabled(false);
                    mCommentEditText.setHint(R.string.thread_is_closed);
                    mCommentEmoijBtn.setClickable(false);
                    mAdvancePostIcon.setVisibility(View.GONE);
                    if(!UserPreferenceUtils.conciseRecyclerView(getApplicationContext())
                            &&detailedThreadInfo.closed == 1){
                        threadPropertyList.add(
                                new ThreadCount(R.drawable.ic_highlight_off_outlined_24px,
                                        getString(R.string.thread_is_closed),getColor(R.color.colorPomegranate))
                        );
                    }

                }
                else {
                    mAdvancePostIcon.setVisibility(View.VISIBLE);
                    mCommentEditText.setEnabled(true);
                    mCommentBtn.setEnabled(true);
                    mCommentEditText.setHint(R.string.bbs_thread_say_something);
                    mCommentEmoijBtn.setClickable(true);
                }

                if(detailedThreadInfo.price!=0){
                    if(detailedThreadInfo.price > 0){
                        threadPropertyList.add(
                                new ThreadCount(R.drawable.ic_price_outlined_24px,
                                        getString(R.string.thread_buy_price,detailedThreadInfo.price),
                                        getColor(R.color.colorPumpkin),
                                        ThreadCount.PROPERTY_BUY
                                )
                        );
                    }
                    else {
                        threadPropertyList.add(
                                new ThreadCount(R.drawable.ic_price_outlined_24px,
                                        getString(R.string.thread_reward_price,detailedThreadInfo.price),getColor(R.color.colorPumpkin))
                        );
                    }

                }

                if(detailedThreadInfo.readperm!=0){
                    ThreadResult result = threadDetailViewModel.threadPostResultMutableLiveData.getValue();
                    if(result != null){
                        forumUserBriefInfo userBriefInfo = result.threadPostVariables.getUserBriefInfo();
                    }
                    if(userBriefInfo!=null && userBriefInfo.readPerm >= detailedThreadInfo.readperm){
                        if(!UserPreferenceUtils.conciseRecyclerView(getApplicationContext())){
                            // not to display in concise mode
                            threadPropertyList.add(
                                    new ThreadCount(R.drawable.ic_verified_user_outlined_24px,
                                            getString(R.string.thread_readperm,detailedThreadInfo.readperm,userBriefInfo.readPerm),getColor(R.color.colorTurquoise))
                            );
                        }

                    }
                    else {
                        if(userBriefInfo == null){
                            threadPropertyList.add(
                                    new ThreadCount(R.drawable.ic_read_perm_unsatisfied_24px,
                                            getString(R.string.thread_anoymous_readperm,detailedThreadInfo.readperm),getColor(R.color.colorAsbestos))
                            );
                        }
                        else {
                            threadPropertyList.add(
                                    new ThreadCount(R.drawable.ic_read_perm_unsatisfied_24px,
                                            getString(R.string.thread_readperm,detailedThreadInfo.readperm,userBriefInfo.readPerm),getColor(R.color.colorAlizarin))
                            );
                        }

                    }

                }



                if(detailedThreadInfo.hidden){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_thread_visibility_off_24px,
                                    getString(R.string.thread_is_hidden),getColor(R.color.colorWisteria))
                    );
                }

                // need another
                if(detailedThreadInfo.highlight !=null && !detailedThreadInfo.highlight.equals("0")){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_highlight_outlined_24px,
                                    getString(R.string.thread_is_highlighted),getColor(R.color.colorPrimary))
                    );
                }

                if(detailedThreadInfo.digest != 0){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_digest_outlined_24px,
                                    getString(R.string.thread_is_digested),getColor(R.color.colorGreensea))
                    );
                }
                
                

                

                if(detailedThreadInfo.is_archived){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_archive_outlined_24px,
                                    getString(R.string.thread_is_archived),getColor(R.color.colorMidnightblue))
                    );
                }

                if(!UserPreferenceUtils.conciseRecyclerView(getApplicationContext())){
                    // only see in not concise mode
                    if(detailedThreadInfo.moderated){
                        threadPropertyList.add(
                                new ThreadCount(R.drawable.ic_moderated_outlined_24px,
                                        getString(R.string.thread_is_moderated),getColor(R.color.colorOrange))
                        );
                    }
                    if(detailedThreadInfo.stickReply){
                        threadPropertyList.add(
                                new ThreadCount(R.drawable.vector_drawable_reply_24px,
                                        getString(R.string.thread_stick_reply),getColor(R.color.colorWetasphalt))
                        );
                    }
                    // recommend?
                    threadNotificationList.add(
                            new ThreadCount(R.drawable.ic_thumb_up_outlined_24px,
                                    String.valueOf(detailedThreadInfo.recommend_add)
                            )
                    );
                    threadNotificationList.add(
                            new ThreadCount(R.drawable.ic_thumb_down_outlined_24px,
                                    String.valueOf(detailedThreadInfo.recommend_sub)
                            )
                    );
                    threadNotificationList.add(
                            new ThreadCount(R.drawable.ic_favorite_24px,String.valueOf(detailedThreadInfo.favtimes),"FAVORITE")
                    );

                    threadNotificationList.add(
                            new ThreadCount(R.drawable.ic_share_outlined_24px,String.valueOf(detailedThreadInfo.sharedtimes),"SHARE")
                    );
                    if(detailedThreadInfo.heats !=0){
                        threadNotificationList.add(
                                new ThreadCount(R.drawable.ic_whatshot_outlined_24px,String.valueOf(detailedThreadInfo.heats))
                        );
                    }
                }

                int status = detailedThreadInfo.status;
                // check with perm
                final int STATUS_CACHE_THREAD_LOCATION = 1, STATUS_ONLY_SEE_BY_POSTER = 2,
                        STATUS_REWARD_LOTTO = 4, STATUS_DESCEND_REPLY = 8, STATUS_EXIST_ICON = 16,
                        STATUS_NOTIFY_AUTHOR = 32;
                if(checkWithPerm(status,STATUS_CACHE_THREAD_LOCATION)){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_cache_thread_location_24px,
                                    getString(R.string.thread_cache_location),getColor(R.color.colorMidnightblue))
                    );
                }
                if(checkWithPerm(status,STATUS_ONLY_SEE_BY_POSTER)){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_reply_only_see_by_poster_24px,
                                    getString(R.string.thread_reply_only_see_by_poster),getColor(R.color.colorNephritis))
                    );
                }
                if(checkWithPerm(status,STATUS_REWARD_LOTTO)){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_thread_reward_lotto_24px,
                                    getString(R.string.thread_reward_lotto),getColor(R.color.colorSunflower))
                    );
                }
                
                if(!UserPreferenceUtils.conciseRecyclerView(getApplicationContext())
                        && checkWithPerm(status,STATUS_NOTIFY_AUTHOR)){
                    threadPropertyList.add(
                            new ThreadCount(R.drawable.ic_thread_notify_author_24px,
                                    getString(R.string.thread_notify_author),getColor(R.color.colorPrimaryDark))
                    );
                }



                countAdapter.setThreadCountList(threadNotificationList);
                propertiesAdapter.setThreadNotificationList(threadPropertyList);
                // for normal rendering
                mDetailedThreadCommentNumber.setText(getString(R.string.bbs_thread_reply_number,detailedThreadInfo.replies));
                mDetailedThreadViewNumber.setText(String.valueOf(detailedThreadInfo.views));


            }
        });

        threadDetailViewModel.threadPostResultMutableLiveData.observe(this, new Observer<ThreadResult>() {
            @Override
            public void onChanged(ThreadResult threadResult) {
                setBaseResult(threadResult,threadResult!=null?threadResult.threadPostVariables:null);
                if(threadResult !=null ){
                    if(threadResult.threadPostVariables !=null
                            && threadResult.threadPostVariables.detailedThreadInfo !=null
                            && threadResult.threadPostVariables.detailedThreadInfo.subject !=null){

                        Spanned sp = Html.fromHtml(threadResult.threadPostVariables.detailedThreadInfo.subject);
                        SpannableString spannableString = new SpannableString(sp);
                        mDetailThreadSubjectTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
                        if(getSupportActionBar()!=null){
                            getSupportActionBar().setTitle(threadResult.threadPostVariables.detailedThreadInfo.subject);
                        }
                        bbsParseUtils.DetailedThreadInfo detailedThreadInfo = threadResult.threadPostVariables.detailedThreadInfo;
                        if(detailedThreadInfo !=null && hasLoadOnce == false){
                            hasLoadOnce = true;
                            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            boolean recordHistory = prefs.getBoolean(getString(R.string.preference_key_record_history),false);
                            if(recordHistory){
                                new InsertViewHistory(new ViewHistory(
                                        URLUtils.getDefaultAvatarUrlByUid(detailedThreadInfo.authorId),
                                        detailedThreadInfo.author,
                                        bbsInfo.getId(),
                                        detailedThreadInfo.subject,
                                        ViewHistory.VIEW_TYPE_THREAD,
                                        detailedThreadInfo.fid,
                                        tid,
                                        new Date()
                                )).execute();

                            }
                        }
                    }

                    //Log.d(TAG,"Thread post result error "+ threadResult.isError()+" "+ threadResult.threadPostVariables.message);


                    Map<String,String> rewriteRule = threadResult.threadPostVariables.rewriteRule;
                    if(rewriteRule!=null){
                        getAndSaveRewriteRule(rewriteRule,UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY);
                        getAndSaveRewriteRule(rewriteRule,UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY);
                        getAndSaveRewriteRule(rewriteRule,UserPreferenceUtils.REWRITE_HOME_SPACE);
                    }
                }

            }
        });

        // for secure reason
        threadDetailViewModel.getSecureInfoResultMutableLiveData().observe(this, new Observer<SecureInfoResult>() {
            @Override
            public void onChanged(SecureInfoResult secureInfoResult) {
                if(secureInfoResult !=null){
                    if(secureInfoResult.secureVariables == null){
                        // don't need a code
                        mPostCaptchaEditText.setVisibility(View.GONE);
                        mPostCaptchaImageview.setVisibility(View.GONE);
                    }
                    else {
                        mPostCaptchaEditText.setVisibility(View.VISIBLE);
                        mPostCaptchaImageview.setVisibility(View.VISIBLE);
                        mPostCaptchaImageview.setImageDrawable(getDrawable(R.drawable.ic_captcha_placeholder_24px));
                        // need a captcha
                        String captchaURL = secureInfoResult.secureVariables.secCodeURL;
                        String captchaImageURL = URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash);
                        // load it
                        if(captchaURL == null){
                            return;
                        }
                        Request captchaRequest = new Request.Builder()
                                .url(captchaURL)
                                .build();
                        // get first
                        client.newCall(captchaRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful() && response.body() != null) {
                                    // get the session


                                    mPostCaptchaImageview.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
                                            Glide.get(getApplication()).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

                                            // forbid cache captcha
                                            RequestOptions options = new RequestOptions()
                                                    .fitCenter()
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .placeholder(R.drawable.ic_captcha_placeholder_24px)
                                                    .error(R.drawable.ic_post_status_warned_24px);
                                            GlideUrl pictureGlideURL = new GlideUrl(captchaImageURL,
                                                    new LazyHeaders.Builder()
                                                    .addHeader("Referer",captchaURL)
                                                    .build()
                                            );

                                            Glide.with(getApplication())
                                                    .load(pictureGlideURL)
                                                    .apply(options)
                                                    .into(mPostCaptchaImageview);
                                        }
                                    });

                                }

                            }
                        });
                    }

                }
                else {
                    // don't know the situation
                    mPostCaptchaEditText.setVisibility(View.GONE);
                    mPostCaptchaImageview.setVisibility(View.GONE);
                }
            }
        });


        threadDetailViewModel.favoriteThreadLiveData.observe(this,favoriteThread -> {
            Log.d(TAG,"Get favorite thread in observer"+favoriteThread);
            invalidateOptionsMenu();
        });

        threadDetailViewModel.recommendResultMutableLiveData.observe(this, apiMessageActionResult -> {
            if(apiMessageActionResult!=null && apiMessageActionResult.message!=null){
                MessageResult messageResult = apiMessageActionResult.message;
                if(messageResult.key.equals("recommend_succeed")){
                    Toasty.success(getApplicationContext(),getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
                else {
                    Toasty.error(getApplicationContext(),getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
            }
        });

        threadDetailViewModel.interactErrorMutableLiveData.observe(this,errorMessage -> {
            if(errorMessage!=null){
                Toasty.error(getApplicationContext(),getString(R.string.discuz_api_message_template,errorMessage.key,errorMessage.content),Toast.LENGTH_LONG).show();
            }
        });
        Context context = this;

        threadDetailViewModel.threadPriceInfoMutableLiveData.observe(this,buyThreadResult -> {
            if(buyThreadResult !=null && buyThreadResult.variableResults!=null){
                BuyThreadResult.BuyThreadVariableResult variableResult = buyThreadResult.variableResults;

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(R.string.buy_thread_title)
                        .setMessage(
                                getString(R.string.buy_thread_dialog_message,
                                        variableResult.author,
                                        String.valueOf(variableResult.price),
                                        variableResult.credit == null?"":variableResult.credit.title,
                                        String.valueOf(variableResult.balance))
                        )
                        .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                            Toasty.info(this,getString(R.string.buy_thread_send),Toast.LENGTH_SHORT).show();
                            threadDetailViewModel.buyThread(tid);
                        }))
                        ;
                builder.show();
            }


        });
        threadDetailViewModel.buyThreadResultMutableLiveData.observe(this,buyThreadResult -> {
            if(buyThreadResult!=null && buyThreadResult.message!=null){
                String key = buyThreadResult.message.key;
                if(key.equals("thread_pay_succeed")){
                    Toasty.success(this,getString(R.string.discuz_api_message_template,
                            buyThreadResult.message.key,
                            buyThreadResult.message.content)).show();
                    reloadThePage();
                }
                else {
                    Toasty.warning(this,getString(R.string.discuz_api_message_template,
                            buyThreadResult.message.key,
                            buyThreadResult.message.content)).show();
                }
            }
        });

        threadDetailViewModel.reportResultMutableLiveData.observe(this, apiMessageActionResult -> {
            if(apiMessageActionResult != null){
                if(apiMessageActionResult.message !=null){
                    if(apiMessageActionResult.message.key.equals("report_succeed")){
                        Toasty.success(this,
                                getString(R.string.discuz_api_message_template,apiMessageActionResult.message.key,apiMessageActionResult.message.content),
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toasty.error(this,
                                getString(R.string.discuz_api_message_template,apiMessageActionResult.message.key,apiMessageActionResult.message.content),
                                Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toasty.error(this,
                            getString(R.string.api_message_return_null),
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void getAndSaveRewriteRule(@NonNull Map<String,String> rewriteRule, @NonNull String key){
        if(rewriteRule.containsKey(key)){
            UserPreferenceUtils.saveRewriteRule(this,bbsInfo,key,rewriteRule.get(key));
        }
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
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    private void configureCommentBtn(){
        // advance post
        Context context  =this;
        mAdvancePostIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mCommentEditText.getText().toString();
                Intent intent = new Intent(context, PublishActivity.class);
                intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forum);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_POST_TYPE,bbsConstUtils.TYPE_POST_REPLY);
                intent.putExtra(bbsConstUtils.PASS_POST_MESSAGE, message);
                intent.putExtra(bbsConstUtils.PASS_REPLY_POST,selectedThreadComment);
                intent.putExtra("tid",tid);
                intent.putExtra("fid",String.valueOf(fid));
                if(forum !=null){
                    intent.putExtra("fid_name",forum.name);
                }

                context.startActivity(intent);
            }
        });

        // captcha
        mPostCaptchaImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update it
                threadDetailViewModel.getSecureInfo();
            }
        });

        mCommentBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String commentMessage = mCommentEditText.getText().toString();
                String captchaString = mPostCaptchaEditText.getText().toString();
                if(needCaptcha() && captchaString.length() == 0){
                    Toasty.warning(getApplicationContext(),getString(R.string.captcha_required),Toast.LENGTH_SHORT).show();
                    return;
                }
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
                        int pid = selectedThreadComment.pid;
                        Log.d(TAG,"Send Reply to "+pid);
                        postReplyToSomeoneInThread(pid,commentMessage,selectedThreadComment.message);
                    }

                }
            }
        });

        mCommentEmoijBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SmileyRootLayout.getVisibility() == View.GONE){
                    // smiley picker not visible
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.vector_drawable_keyboard_24px));

                    mCommentEditText.clearFocus();
                    // close keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm !=null){
                        imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(),0);
                    }
                    SmileyRootLayout.setVisibility(View.VISIBLE);

                    // tab layout binding...
                    getSmileyInfo();
                }
                else {
                    SmileyRootLayout.setVisibility(View.GONE);
                    mCommentEmoijBtn.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp));
                }


            }
        });

        mCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && SmileyRootLayout.getVisibility() == View.VISIBLE){
                    SmileyRootLayout.setVisibility(View.GONE);
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
        PostInfo threadCommentInfo = adapter.getThreadInfoList().get(position);

        selectedThreadComment = threadCommentInfo;
        mThreadReplyBadge.setText(threadCommentInfo.author);
        mThreadReplyBadge.setVisibility(View.VISIBLE);
        mCommentEditText.setHint(String.format("@%s",threadCommentInfo.author));
        String decodeString = threadCommentInfo.message;
        // filter quote
        String quoteRegexInVer4 = "^<div class=\"reply_wrap\">(.+?)</div><br .>";

        // remove it if possible
        Pattern quotePatternInVer4 = Pattern.compile(quoteRegexInVer4,Pattern.DOTALL);
        Matcher quoteMatcherInVer4 = quotePatternInVer4.matcher(decodeString);
        decodeString = quoteMatcherInVer4.replaceAll("");

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
        Log.d(TAG,"POLL is voted");
        pollInfo = null;
        threadDetailViewModel.pollInfoLiveData.setValue(null);

        reloadThePage();
        threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
        //threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());

    }

    @Override
    public void setAuthorId(int authorId) {
        ViewThreadQueryStatus viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();

        if(viewThreadQueryStatus !=null){
            viewThreadQueryStatus.setInitAuthorId(authorId);
        }

        //threadDetailViewModel.threadStatusMutableLiveData.setValue(threadStatus);
        reloadThePage(viewThreadQueryStatus);

        // refresh it
        threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
        // getThreadComment();
    }

    private void parseURLAndOpen(String url){
        Log.d(TAG,"Parse and open URL "+url);
        Uri uri = Uri.parse(url);
        if(uri !=null && uri.getPath() !=null){
            if(uri.getQueryParameter("mod")!=null
                    && uri.getQueryParameter("mod").equals("redirect")
                    && uri.getQueryParameter("goto")!=null
                    && uri.getQueryParameter("goto").equals("findpost")
                    && uri.getQueryParameter("pid")!=null
                    && uri.getQueryParameter("ptid")!=null){
                String pidString = uri.getQueryParameter("pid");
                String tidString = uri.getQueryParameter("ptid");
                int redirectTid = 0;
                int redirectPid = 0;
                try{
                    redirectTid = Integer.parseInt(tidString);
                    redirectPid = Integer.parseInt(pidString);
                }
                catch (Exception e){

                }
                Log.d(TAG,"Find the current "+redirectPid+" tid "+redirectTid);
                if(redirectTid != tid){
                    ThreadInfo putThreadInfo = new ThreadInfo();
                    putThreadInfo.tid = redirectTid;
                    Intent intent = new Intent(this, ThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, putThreadInfo);
                    intent.putExtra("FID",fid);
                    intent.putExtra("TID",redirectTid);
                    intent.putExtra("SUBJECT",url);
                    VibrateUtils.vibrateForClick(this);

                    startActivity(intent);
                    return;
                }
                else {
                    // scroll it
                    List<PostInfo> postInfos = adapter.getThreadInfoList();
                    if(postInfos !=null){
                        for(int i=0; i<postInfos.size(); i++){
                            PostInfo curPost = postInfos.get(i);
                            if(curPost.pid == redirectPid){
                                mRecyclerview.scrollToPosition(i);
                                VibrateUtils.vibrateForClick(this);
                                long postPostion = postInfos.get(i).position;
                                Toasty.success(this,getString(R.string.scroll_to_pid_successfully,postPostion),Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toasty.info(this,getString(R.string.scroll_to_pid_failed,pidString),Toast.LENGTH_SHORT).show();

                    }

                }
            }
            else if(uri.getQueryParameter("mod")!=null
                    && uri.getQueryParameter("mod").equals("viewthread")
                    && uri.getQueryParameter("tid")!=null){
                String tidString = uri.getQueryParameter("tid");
                int redirectTid = 0;
                try{
                    redirectTid = Integer.parseInt(tidString);
                }
                catch (Exception e){

                }
                ThreadInfo putThreadInfo = new ThreadInfo();
                putThreadInfo.tid = redirectTid;
                Intent intent = new Intent(this, ThreadActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, putThreadInfo);
                intent.putExtra("FID",fid);
                intent.putExtra("TID",redirectTid);
                intent.putExtra("SUBJECT",url);
                VibrateUtils.vibrateForClick(this);

                startActivity(intent);
                return;

            }
            else if(uri.getQueryParameter("mod")!=null
                    && uri.getQueryParameter("mod").equals("forumdisplay")
                    && uri.getQueryParameter("fid")!=null){
                String fidString = uri.getQueryParameter("fid");
                int fid = 0;
                try{
                    fid = Integer.parseInt(fidString);
                }
                catch (Exception e){
                    fid = 0;
                }
                Intent intent = new Intent(this, ForumActivity.class);
                ForumInfo clickedForum = new ForumInfo();
                clickedForum.fid = fid;

                intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,clickedForum);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                Log.d(TAG,"put base url "+bbsInfo.base_url);
                VibrateUtils.vibrateForClick(this);
                startActivity(intent);
                return;

            }
            else if(uri.getQueryParameter("mod")!=null
                    && uri.getQueryParameter("mod").equals("space")
                    && uri.getQueryParameter("uid")!=null){
                String uidStr = uri.getQueryParameter("uid");
                int uid = 0;
                try{
                    uid = Integer.parseInt(uidStr);
                }
                catch (Exception e){
                    uid = 0;
                }

                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra("UID",uid);


                startActivity(intent);
                return;

            }
            Intent intent = new Intent(this, InternalWebViewActivity.class);
            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            intent.putExtra(bbsConstUtils.PASS_URL_KEY,url);
            Log.d(TAG,"Inputted URL "+url);
            startActivity(intent);

        }
        else {
            Intent intent = new Intent(this, InternalWebViewActivity.class);
            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            intent.putExtra(bbsConstUtils.PASS_URL_KEY,url);
            Log.d(TAG,"Inputted URL "+url);
            startActivity(intent);
        }
    }





    @Override
    public void onLinkClicked(String url) {
        Context context = this;
        String unescapedURL = url
                .replace("&amp;","&")
                .replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&nbsp;"," ")
                ;
        // judge the host
        String baseURL = URLUtils.getBaseUrl();
        Uri baseUri = Uri.parse(baseURL);
        Uri clickedUri = Uri.parse(unescapedURL);
        if(clickedUri.getHost() == null || clickedUri.getHost().equals(baseUri.getHost())){
            // internal link
            ThreadResult result = threadDetailViewModel.threadPostResultMutableLiveData.getValue();

            if(result !=null && result.threadPostVariables!=null ){
                if(result.threadPostVariables.rewriteRule!=null){
                    Map<String,String> rewriteRules = result.threadPostVariables.rewriteRule;

                    String clickedURLPath = clickedUri.getPath();
                    if(clickedURLPath == null){
                        parseURLAndOpen(unescapedURL);
                    }
                    String basedURLPath = baseUri.getPath();
                    if(clickedURLPath !=null && basedURLPath!=null){
                        if(clickedURLPath.matches("^"+basedURLPath+".*")){
                            clickedURLPath = clickedURLPath.substring(basedURLPath.length());
                        }
                    }
                    // only catch two type : forum_forumdisplay & forum_viewthread
                    // only 8.0+ support reverse copy
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                        if(rewriteRules.containsKey("forum_forumdisplay") ){
                            String rewriteRule = rewriteRules.get("forum_forumdisplay");
                            UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY,rewriteRule);
                            if(rewriteRule == null || clickedURLPath == null){
                                parseURLAndOpen(unescapedURL);
                                return;
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{fid}","(?<fid>\\d+)");
                            rewriteRule = rewriteRule.replace("{page}","(?<page>\\d+)");
                            Pattern pattern = Pattern.compile(rewriteRule);
                            Matcher matcher = pattern.matcher(clickedURLPath);
                            if(matcher.find()){

                                String fidStr = matcher.group("fid");
                                String pageStr = matcher.group("page");
                                // handle it
                                if(fidStr !=null){
                                    int fid = 0;
                                    try{
                                        fid = Integer.parseInt(fidStr);
                                    }
                                    catch (Exception e){
                                        fid = 0;
                                    }

//                                    int page = Integer.parseInt(pageStr);
                                    Intent intent = new Intent(context, ForumActivity.class);
                                    ForumInfo clickedForum = new ForumInfo();
                                    clickedForum.fid = fid;

                                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,clickedForum);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                    Log.d(TAG,"put base url "+bbsInfo.base_url);
                                    VibrateUtils.vibrateForClick(context);
                                    context.startActivity(intent);
                                    return;
                                }

                            }

                        }
                        if(rewriteRules.containsKey("forum_viewthread")){
                            // match template such as t{tid}-{page}-{prevpage}
                            String rewriteRule = rewriteRules.get("forum_viewthread");
                            UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY,rewriteRule);
                            if(rewriteRule == null || clickedURLPath == null){
                                parseURLAndOpen(unescapedURL);
                                return;
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{tid}","(?<tid>\\d+)");
                            rewriteRule = rewriteRule.replace("{page}","(?<page>\\d+)");
                            rewriteRule = rewriteRule.replace("{prevpage}","(?<prevpage>\\d+)");
                            Pattern pattern = Pattern.compile(rewriteRule);
                            Matcher matcher = pattern.matcher(clickedURLPath);
                            if(matcher.find()){

                                String tidStr = matcher.group("tid");
                                String pageStr = matcher.group("page");
                                // handle it
                                if(tidStr !=null){

                                    ThreadInfo putThreadInfo = new ThreadInfo();
                                    int tid = 0;
                                    try{
                                        tid = Integer.parseInt(tidStr);
                                    }
                                    catch (Exception e){
                                        tid = 0;
                                    }

                                    putThreadInfo.tid = tid;
                                    Intent intent = new Intent(context, ThreadActivity.class);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, putThreadInfo);
                                    intent.putExtra("FID",fid);
                                    intent.putExtra("TID",tid);
                                    intent.putExtra("SUBJECT",url);
                                    VibrateUtils.vibrateForClick(context);
                                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);

                                    Bundle bundle = options.toBundle();
                                    context.startActivity(intent,bundle);
                                    return;
                                }

                            }
                        }
                        if(rewriteRules.containsKey("home_space")){
                            // match template such as t{tid}-{page}-{prevpage}
                            String rewriteRule = rewriteRules.get("home_space");
                            Log.d(TAG,"get home space url "+rewriteRule);
                            UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE,rewriteRule);
                            if(rewriteRule == null || clickedURLPath == null){
                                parseURLAndOpen(unescapedURL);
                                return;
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{user}","(?<user>\\d+)");
                            rewriteRule = rewriteRule.replace("{value}","(?<value>\\d+)");
                            Pattern pattern = Pattern.compile(rewriteRule);
                            Matcher matcher = pattern.matcher(clickedURLPath);
                            if(matcher.find()){

                                String userString = matcher.group("user");
                                String uidString = matcher.group("value");
                                // handle it
                                if(uidString !=null){
                                    int uid = 0;
                                    try{
                                        uid = Integer.parseInt(uidString);
                                    }
                                    catch (Exception e){
                                        uid = 0;
                                    }


                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                    intent.putExtra("UID",uid);

                                    VibrateUtils.vibrateForClick(context);
                                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);

                                    Bundle bundle = options.toBundle();
                                    context.startActivity(intent,bundle);
                                    return;
                                }

                            }
                        }

                        parseURLAndOpen(unescapedURL);
                    }
                    else {

                        parseURLAndOpen(unescapedURL);
                    }



                }
                else {
                    parseURLAndOpen(unescapedURL);
                }
            }
            else {
                // parse the URL
                parseURLAndOpen(unescapedURL);
            }

        }
        else {
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
            boolean outLinkWarn = prefs.getBoolean(getString(R.string.preference_key_outlink_warn),true);
            if(outLinkWarn){

                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(getString(R.string.outlink_warn_message,clickedUri.getHost(),baseUri.getHost()))
                        .setNeutralButton(R.string.bbs_show_in_internal_browser, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, InternalWebViewActivity.class);
                                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                intent.putExtra(bbsConstUtils.PASS_URL_KEY,unescapedURL);
                                Log.d(TAG,"Inputted URL "+unescapedURL);
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton(R.string.bbs_show_in_external_browser, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(unescapedURL));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            else {
                Intent intent = new Intent(this, InternalWebViewActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_URL_KEY,unescapedURL);
                Log.d(TAG,"Inputted URL "+unescapedURL);
                startActivity(intent);
                return;
            }


        }

        Log.d(TAG,"You click "+unescapedURL);
    }

    @Override
    public void onRecommend(boolean recommend) {
        threadDetailViewModel.recommendThread(tid,recommend);
    }

    @Override
    public void buyThreadPropertyClicked() {

        // buy the thread dialog
        Toasty.info(this,getString(R.string.buy_thread_loading),Toast.LENGTH_SHORT).show();
        threadDetailViewModel.getThreadPriceInfo(tid);
        // prompt dialog first



    }

    @Override
    public void reportPost(PostInfo postInfo) {
        if(userBriefInfo == null){
            Toasty.warning(this,getString(R.string.report_login_required),Toast.LENGTH_LONG).show();
        }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            ReportPostDialogFragment reportPostDialogFragment = new ReportPostDialogFragment(postInfo);
            reportPostDialogFragment.show(fragmentManager,ReportPostDialogFragment.class.getSimpleName());
        }


    }

    @Override
    public void onReportSubmit(int pid,String reportReason,boolean reportForOtherReason) {
        threadDetailViewModel.reportPost(pid,reportReason,reportForOtherReason);
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
        //mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new PostAdapter(this,
                bbsInfo,
                userBriefInfo,
                threadDetailViewModel.threadStatusMutableLiveData.getValue());
        adapter.subject =subject;
        mRecyclerview.setAdapter(adapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1)
                        && newState==RecyclerView.SCROLL_STATE_IDLE){
                    //Log.d(TAG,"Recyclerview can scroll vert ");
                    ViewThreadQueryStatus viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                    boolean isLoading = threadDetailViewModel.isLoading.getValue();
                    boolean hasLoadAll = threadDetailViewModel.hasLoadAll.getValue();
                    if(!isLoading && viewThreadQueryStatus !=null){
                        if(hasLoadAll){
                            // load all posts
                            if(!notifyLoadAll){
                                // never vibrate before
                                if(UserPreferenceUtils.vibrateWhenLoadingAll(getApplicationContext())){
                                    VibrateUtils.vibrateSlightly(getApplicationContext());
                                }
                                notifyLoadAll = true;
                                threadDetailViewModel.notifyLoadAll.postValue(true);
                                Toasty.success(getApplication(),
                                        getString(R.string.all_posts_loaded_template,adapter.getItemCount()),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                        else {
                            viewThreadQueryStatus.page += 1;
                            threadDetailViewModel.getThreadDetail(viewThreadQueryStatus);
                        }
                    }

                }
            }
        });
        countAdapter = new ThreadCountAdapter();

        if(!UserPreferenceUtils.conciseRecyclerView(getApplicationContext())){
            // not to bind this
            mDetailThreadTypeRecyclerview.setHasFixedSize(true);
            mDetailThreadTypeRecyclerview.setLayoutManager(new GridLayoutManager(this, 5));
            mDetailThreadTypeRecyclerview.setAdapter(countAdapter);
        }

        propertiesAdapter = new ThreadPropertiesAdapter();
        mDetailThreadPropertyRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mDetailThreadPropertyRecyclerview.setAdapter(propertiesAdapter);


    }

    private boolean needCaptcha(){
        if(threadDetailViewModel == null
                || threadDetailViewModel.getSecureInfoResultMutableLiveData().getValue()==null
                || threadDetailViewModel.getSecureInfoResultMutableLiveData().getValue().secureVariables==null){
            return false;
        }
        else {
            return true;
        }
    }

    private void postCommentToThread(String message){
        Date timeGetTime = new Date();

        FormBody.Builder formBodyBuilder = new FormBody.Builder()


                .add("subject", "")
                .add("usesig", "1")
                .add("posttime",String.valueOf(timeGetTime.getTime() / 1000 - 1))
                .add("formhash",formHash);
        switch (getCharsetType()){
            case CHARSET_GBK:{
                try{
                    formBodyBuilder.addEncoded("message", URLEncoder.encode(message,"GBK"));
                    break;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            case CHARSET_BIG5:{
                try{
                    formBodyBuilder.addEncoded("message", URLEncoder.encode(message,"BIG5"));
                    break;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            default:{
                formBodyBuilder.add("message", message);
            }


        }
        if(needCaptcha()){
            SecureInfoResult secureInfoResult = threadDetailViewModel.getSecureInfoResultMutableLiveData().getValue();

            formBodyBuilder.add("seccodehash",secureInfoResult.secureVariables.secHash)
                    .add("seccodemodid", "forum::viewthread");
            String captcha=  mPostCaptchaEditText.getText().toString();
            switch (getCharsetType()){
                case CHARSET_GBK:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"GBK"))
                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                case CHARSET_BIG5:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"BIG5"))
                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                default:{
                    formBodyBuilder.add("seccodeverify", captcha);
                }
            }

        }

        FormBody formBody = formBodyBuilder.build();
        Log.d(TAG,"get Form "+message+" hash "
                +formHash+" fid "+fid+" tid "+tid
                + " API ->"+ URLUtils.getReplyThreadUrl(fid,tid)+" formhash "+formHash);
        Request request = new Request.Builder()
                .url(URLUtils.getReplyThreadUrl(fid,tid))
                .post(formBody)
                .addHeader("referer",URLUtils.getViewThreadUrl(tid,"1"))
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
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv comment info "+s);
                    bbsParseUtils.returnMessage returnedMessage = bbsParseUtils.parseReturnMessage(s);
                    if(returnedMessage!=null && returnedMessage.value.equals("post_reply_succeed")){
                        // success!
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(SmileyRootLayout.getVisibility() == View.VISIBLE){
                                    mCommentEmoijBtn.callOnClick();
                                }
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
        ViewThreadQueryStatus viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        if(viewThreadQueryStatus !=null){

            viewThreadQueryStatus.setInitPage(1);
        }
        Log.d(TAG,"Set status when reload page");
        threadDetailViewModel.threadStatusMutableLiveData.setValue(viewThreadQueryStatus);
        threadDetailViewModel.notifyLoadAll.setValue(false);
        notifyLoadAll = false;
    }

    private void reloadThePage(ViewThreadQueryStatus viewThreadQueryStatus){
        if(viewThreadQueryStatus !=null){
            viewThreadQueryStatus.setInitPage(1);
        }
        Log.d(TAG,"Set status when init data "+ viewThreadQueryStatus);
        threadDetailViewModel.threadStatusMutableLiveData.setValue(viewThreadQueryStatus);
        threadDetailViewModel.notifyLoadAll.setValue(false);
        notifyLoadAll = false;
    }


    private void postReplyToSomeoneInThread(int replyPid,String message,String noticeAuthorMsg){
        // remove noticeAuthorMsg <>
        noticeAuthorMsg = noticeAuthorMsg.replaceAll("<.*>","");

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, Locale.getDefault());
        String publishAtString = df.format(selectedThreadComment.publishAt);
        int MAX_CHAR_LENGTH = 300;
        int trimEnd = Math.min(MAX_CHAR_LENGTH,noticeAuthorMsg.length());
        // not to trim
        //int trimEnd = noticeAuthorMsg.length();
        Log.d(TAG,"Reply msg "+noticeAuthorMsg);
        String replyMessage = noticeAuthorMsg.substring(0,trimEnd);
        if(noticeAuthorMsg.length()>MAX_CHAR_LENGTH){
            replyMessage += "...";
        }
        String noticeMsgTrimString = getString(R.string.bbs_reply_notice_author_string,
                URLUtils.getReplyPostURLInLabel(selectedThreadComment.pid, selectedThreadComment.tid),
                selectedThreadComment.author,
                publishAtString,
                replyMessage

        );
        Log.d(TAG,"Get message "+noticeAuthorMsg+noticeMsgTrimString);




        FormBody.Builder formBodyBuilder = new FormBody.Builder()
                .add("formhash",formHash)
                .add("handlekey","reply")

                .add("usesig", "1")
                .add("reppid", String.valueOf(replyPid))
                .add("reppost", String.valueOf(replyPid));


        switch (getCharsetType()){
            case CHARSET_GBK:{
                try{
                    formBodyBuilder.addEncoded("message", URLEncoder.encode(message,"GBK"));
                    formBodyBuilder.addEncoded("noticeauthormsg",URLEncoder.encode(noticeAuthorMsg,"GBK"))
                            .addEncoded("noticetrimstr",URLEncoder.encode(noticeMsgTrimString,"GBK"));
                    break;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            case CHARSET_BIG5:{
                try{
                    formBodyBuilder.addEncoded("message", URLEncoder.encode(message,"BIG5"));
                    formBodyBuilder.addEncoded("noticeauthormsg",URLEncoder.encode(noticeAuthorMsg,"BIG5"))
                            .addEncoded("noticetrimstr",URLEncoder.encode(noticeMsgTrimString,"BIG5"));
                    break;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            default:{
                formBodyBuilder.add("message", message);
                formBodyBuilder.add("noticeauthormsg",noticeAuthorMsg)
                        .add("noticetrimstr",noticeMsgTrimString);

            }


        }


        if(needCaptcha()){
            SecureInfoResult secureInfoResult = threadDetailViewModel.getSecureInfoResultMutableLiveData().getValue();
            String captcha = mPostCaptchaEditText.getText().toString();
            formBodyBuilder.add("seccodehash",secureInfoResult.secureVariables.secHash)
                    .add("seccodemodid", "forum::viewthread");
            switch (getCharsetType()){
                case CHARSET_GBK:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"GBK"))
                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                case CHARSET_BIG5:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"BIG5"))
                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                default:{
                    formBodyBuilder.add("seccodeverify", captcha);
                }
            }
        }

        FormBody formBody = formBodyBuilder.build();
        Request request = new Request.Builder()
                .url(URLUtils.getReplyThreadUrl(fid,tid))
                .post(formBody)
                .build();

        mCommentBtn.setText(R.string.bbs_commentting);
        mCommentBtn.setEnabled(false);
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
                                if(SmileyRootLayout.getVisibility() == View.VISIBLE){
                                    mCommentEmoijBtn.callOnClick();
                                }
                                mCommentBtn.setText(R.string.bbs_thread_comment);
                                mCommentBtn.setEnabled(true);
                                mCommentEditText.setText("");
                                reloadThePage();
                                threadDetailViewModel.getThreadDetail(threadDetailViewModel.threadStatusMutableLiveData.getValue());
                                //getThreadComment();
                                Toasty.success(getApplicationContext(), getString(R.string.discuz_api_message_template,returnedMessage.value,returnedMessage.string), Toast.LENGTH_LONG).show();
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
                                    Toasty.error(getApplicationContext(), getString(R.string.discuz_api_message_template,returnedMessage.value,returnedMessage.string), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });


    }

    private void configureToolbar(){

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setTitle(subject);

    }

    private void launchFavoriteThreadDialog(FavoriteThread favoriteThread){
        AlertDialog.Builder favoriteDialog = new AlertDialog.Builder(this);
        favoriteDialog.setTitle(R.string.favorite_description);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);

        favoriteDialog.setView(input);

        favoriteDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = input.getText().toString();
                description = TextUtils.isEmpty(description) ?"" :description;
                new FavoritingThreadAsyncTask(favoriteThread,true,description).execute();

            }
        });

        favoriteDialog.show();


    }




    public boolean onOptionsItemSelected(MenuItem item) {
        ViewThreadQueryStatus viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        String currentUrl = "";
        if(viewThreadQueryStatus == null){
            currentUrl = URLUtils.getViewThreadUrl(tid,"1");
        }
        else {
            currentUrl = URLUtils.getViewThreadUrl(tid,String.valueOf(viewThreadQueryStatus.page));
        }

        switch (item.getItemId()) {

            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            case R.id.bbs_forum_nav_personal_center:{
                Intent intent = new Intent(this, UserProfileActivity.class);
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
                Intent intent = new Intent(this, InternalWebViewActivity.class);
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
                viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                Log.d(TAG,"You press sort btn "+ viewThreadQueryStatus.datelineAscend);
                // bbsThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
                if(viewThreadQueryStatus !=null){
                    viewThreadQueryStatus.datelineAscend = !viewThreadQueryStatus.datelineAscend;
                    Log.d(TAG,"Changed Ascend mode "+ viewThreadQueryStatus.datelineAscend);


                    Log.d(TAG,"Apply Ascend mode "+threadDetailViewModel.threadStatusMutableLiveData.getValue().datelineAscend);
                    reloadThePage(viewThreadQueryStatus);
                    Log.d(TAG,"After reload Ascend mode "+threadDetailViewModel.threadStatusMutableLiveData.getValue().datelineAscend);

                    if(viewThreadQueryStatus.datelineAscend){
                        Toasty.success(context,getString(R.string.bbs_thread_status_ascend),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toasty.success(context,getString(R.string.bbs_thread_status_descend),Toast.LENGTH_SHORT).show();
                    }
                    // reload the parameters
                    Log.d(TAG,"dateline ascend "+ viewThreadQueryStatus.datelineAscend);

                    threadDetailViewModel.getThreadDetail(viewThreadQueryStatus);

                    invalidateOptionsMenu();

                }
                return true;
            }
            case R.id.bbs_share:{
                ThreadResult result = threadDetailViewModel.threadPostResultMutableLiveData.getValue();
                if(result!=null && result.threadPostVariables!=null && result.threadPostVariables.detailedThreadInfo!=null){
                    bbsParseUtils.DetailedThreadInfo detailedThreadInfo = result.threadPostVariables.detailedThreadInfo;
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_template,
                            detailedThreadInfo.subject,currentUrl));
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                    return true;
                }
                else {
                    Toasty.info(this,getString(R.string.share_not_prepared),Toast.LENGTH_SHORT).show();
                }
                return true;

            }
            case R.id.bbs_favorite:{
                ThreadResult result = threadDetailViewModel.threadPostResultMutableLiveData.getValue();
                if(result!=null && result.threadPostVariables!=null && result.threadPostVariables.detailedThreadInfo!=null){
                    bbsParseUtils.DetailedThreadInfo detailedThreadInfo = result.threadPostVariables.detailedThreadInfo;
                    FavoriteThread favoriteThread = detailedThreadInfo.toFavoriteThread(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);
                    // save it to the database
                    // boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
                    FavoriteThread favoriteThreadInDB = threadDetailViewModel.favoriteThreadLiveData.getValue();
                    boolean isFavorite = favoriteThreadInDB != null;
                    if(isFavorite){

                        Log.d(TAG,"Get Favroite thread"+ favoriteThreadInDB);
                        new FavoritingThreadAsyncTask(favoriteThreadInDB,false).execute();

                    }
                    else {
                        Log.d(TAG,"is Favorite "+isFavorite);
                        // open up a dialog
                        launchFavoriteThreadDialog(favoriteThread);
                        //new FavoritingThreadAsyncTask(favoriteThread,true).execute();
                    }

                }
                else {
                    Toasty.info(this,getString(R.string.favorite_thread_not_prepared),Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            case R.id.bbs_search:{
                Intent intent = new Intent(this, SearchPostsActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                startActivity(intent);
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

        }

        if(getSupportActionBar()!=null){

            ViewThreadQueryStatus ViewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
            if(ViewThreadQueryStatus !=null){
                Log.d(TAG,"ON CREATE GET ascend mode in menu "+ ViewThreadQueryStatus.datelineAscend);
                if(ViewThreadQueryStatus.datelineAscend){
                    menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(ContextCompat.getDrawable(getApplication(),R.drawable.vector_drawable_arrow_upward_24px));
                }
                else {
                    menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(ContextCompat.getDrawable(getApplication(),R.drawable.vector_drawable_arrow_downward_24px));
                }


            }
        }




        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        ViewThreadQueryStatus ViewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
        if(ViewThreadQueryStatus !=null){
            Log.d(TAG,"ON PREPARE GET ascend mode in menu "+ ViewThreadQueryStatus.datelineAscend);
            if(ViewThreadQueryStatus.datelineAscend){
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_upward_24px));
            }
            else {
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).setIcon(getDrawable(R.drawable.vector_drawable_arrow_downward_24px));
            }

        }
        //boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
        FavoriteThread favoriteThread = threadDetailViewModel.favoriteThreadLiveData.getValue();
        boolean isFavorite = favoriteThread != null;
        Log.d(TAG,"Triggering favorite status "+isFavorite);
        if(!isFavorite){
            menu.findItem(R.id.bbs_favorite).setIcon(getDrawable(R.drawable.ic_not_favorite_24px));
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.favorite);
        }
        else {
            menu.findItem(R.id.bbs_favorite).setIcon(getDrawable(R.drawable.ic_favorite_24px));
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.unfavorite);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public class InsertViewHistory extends AsyncTask<Void,Void,Void> {

        ViewHistory viewHistory;
        ViewHistoryDao dao;

        public InsertViewHistory(ViewHistory viewHistory){

            this.viewHistory = viewHistory;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao = ViewHistoryDatabase.getInstance(getApplicationContext()).getDao();
            List<ViewHistory> viewHistories = dao
                    .getViewHistoryByBBSIdAndTid(viewHistory.belongedBBSId,viewHistory.tid);
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

    public class FavoritingThreadAsyncTask extends AsyncTask<Void,Void,Boolean> {

        FavoriteThread favoriteThread;
        FavoriteThreadDao dao;
        boolean favorite, error=false;
        Retrofit retrofit;
        retrofit2.Call<ApiMessageActionResult> favoriteThreadActionResultCall;
        MessageResult messageResult;
        String description = "";

        public FavoritingThreadAsyncTask(FavoriteThread favoriteThread, boolean favorite){

            this.favoriteThread = favoriteThread;
            this.favorite = favorite;
        }

        public FavoritingThreadAsyncTask(FavoriteThread favoriteThread, boolean favorite, String description){

            this.favoriteThread = favoriteThread;
            this.favorite = favorite;
            this.description = description;
            favoriteThread.description = description;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
            DiscuzApiService service = retrofit.create(DiscuzApiService.class);
            ThreadResult result = threadDetailViewModel.threadPostResultMutableLiveData.getValue();
            if(result !=null
                    && result.threadPostVariables!=null
                    && favoriteThread.userId !=0
                    && UserPreferenceUtils.syncInformation(getApplication())){
                if(favorite){
                    favoriteThreadActionResultCall = service.favoriteThreadActionResult(result.threadPostVariables.formHash
                            , favoriteThread.idKey,description);
                }
                else {
                    Log.d(TAG,"Favorite id "+ favoriteThread.favid);
                    if(favoriteThread.favid == 0){
                        // just remove it from database
                    }
                    else {
                        favoriteThreadActionResultCall = service.unfavoriteThreadActionResult(
                                result.threadPostVariables.formHash,
                                "true",
                                "a_delete_"+ favoriteThread.favid,
                                favoriteThread.favid);
                    }

                }

            }

        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            dao = FavoriteThreadDatabase.getInstance(getApplicationContext()).getDao();

            if(favoriteThreadActionResultCall!=null){
                try {
                    Log.d(TAG,"request favorite url "+favoriteThreadActionResultCall.request().url());
                    retrofit2.Response<ApiMessageActionResult> response = favoriteThreadActionResultCall.execute();
                    //Log.d(TAG,"get response "+response.raw().body().string());
                    if(response.isSuccessful() && response.body() !=null){

                        ApiMessageActionResult result = response.body();
                        messageResult = result.message;
                        String key = result.message.key;
                        if(favorite && key.equals("favorite_do_success")
                        ){
                            dao.insert(favoriteThread);
                        }
                        else if(!favorite && key.equals("do_success")
                        ){
                            if(favoriteThread !=null){
                                dao.delete(favoriteThread);
                            }
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                        }
                        else {
                            error = true;

                        }

                    }
                    else {
                        messageResult = new MessageResult();
                        messageResult.content = getString(R.string.network_failed);
                        messageResult.key = String.valueOf(response.code());
                        if(favorite){
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                            dao.insert(favoriteThread);

                            return true;
                        }
                        else {
                            // clear potential
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                            //dao.delete(favoriteThread);
                            Log.d(TAG,"Just remove it from database "+tid+ " "+ favoriteThread.idKey);
                            return false;

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = true;
                    messageResult = new MessageResult();
                    messageResult.content = e.getMessage();
                    messageResult.key = e.toString();
                    if(favorite){
                        dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                        dao.insert(favoriteThread);

                        return true;
                    }
                    else {
                        // clear potential
                        dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                        //dao.delete(favoriteThread);
                        Log.d(TAG,"Just remove it from database "+tid+ " "+ favoriteThread.idKey);
                        return false;

                    }
                }

            }
            else {
                if(favorite){
                    dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                    dao.insert(favoriteThread);

                    return true;
                }
                else {
                    // clear potential
                    dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteThread.idKey,"tid");
                    //dao.delete(favoriteThread);
                    Log.d(TAG,"Just remove it from database "+tid+ " "+ favoriteThread.idKey);
                    return false;

                }
            }
            return favorite;

        }


        @Override
        protected void onPostExecute(Boolean favorite) {
            super.onPostExecute(favorite);
            if(messageResult!=null){
                String key = messageResult.key;
                if(favorite && key.equals("favorite_do_success")){
                    Toasty.success(getApplication(),getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
                else if(!favorite && key.equals("do_success")){
                    Toasty.success(getApplication(),getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
                else {
                    Toasty.warning(getApplication(),getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
            }
            else {
                if(favorite){
                    Toasty.success(getApplication(),getString(R.string.favorite),Toast.LENGTH_SHORT).show();

                }
                else {
                    Toasty.success(getApplication(),getString(R.string.unfavorite),Toast.LENGTH_SHORT).show();
                }
                VibrateUtils.vibrateSlightly(getApplication());
            }


        }
    }
}
