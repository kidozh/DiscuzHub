package com.kidozh.discuzhub.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.SubForumAdapter;
import com.kidozh.discuzhub.adapter.ThreadAdapter;
import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ApiMessageActionResult;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.results.MessageResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.viewModels.ForumViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Retrofit;

public class ForumActivity
        extends BaseStatusActivity implements bbsLinkMovementMethod.OnLinkClickedListener{
    private static final String TAG = ForumActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bbs_forum_fab)
    FloatingActionButton fab;
    @BindView(R.id.bbs_forum_description_textview)
    TextView mForumDesciption;
    @BindView(R.id.bbs_forum_alert_textview)
    TextView mForumAlert;
    @BindView(R.id.bbs_forum_rule_textview)
    TextView mForumRule;
    @BindView(R.id.bbs_forum_thread_number_textview)
    TextView mForumThreadNum;
    @BindView(R.id.bbs_forum_post_number_textview)
    TextView mForumPostNum;
    @BindView(R.id.bbs_forum_info_swipe_refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.bbs_forum_thread_recyclerview)
    RecyclerView mRecyclerview;
    @BindView(R.id.more_thread_btn)
    Button moreThreadBtn;
    @BindView(R.id.bbs_forum_constraintLayout)
    ConstraintLayout mForumInfoCardView;
    @BindView(R.id.bbs_forum_thread_type_chipgroup)
    ChipGroup mForumThreadTypeChipGroup;
    @BindView(R.id.bbs_forum_error_textview)
    TextView errorTextview;
    @BindView(R.id.bbs_forum_error_layout)
    View errorLayout;
    @BindView(R.id.bbs_forum_sublist)
    RecyclerView subForumRecyclerview;
    private ForumInfo forum;


    private ThreadAdapter adapter;
    private SubForumAdapter subForumAdapter;
    boolean isTaskRunning;
    String fid;
    String returned_res_json;
    //MutableLiveData<bbsURLUtils.ForumStatus> forumStatusMutableLiveData;

    ForumViewModel forumViewModel;
    private boolean hasLoadOnce = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_forum);
        ButterKnife.bind(this);
        forumViewModel = new ViewModelProvider(this).get(ForumViewModel.class);
        configureIntentData();
        bindViewModel();

        Log.d(TAG, "Get bbs information "+bbsInfo);

        initLiveData();
        configureActionBar();

        configureFab();
        configureForumInfo();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        setForumRuleCollapseListener();

        //getThreadInfo();
        configurePostThreadBtn();
        configureChipGroupFilter();
    }



    private void configureIntentData(){
        Intent intent = getIntent();
        forum = (ForumInfo) intent.getSerializableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        URLUtils.setBBS(bbsInfo);
        fid = String.valueOf(forum.fid);
        forumViewModel.setBBSInfo(bbsInfo,userBriefInfo,forum);
        // hasLoadOnce = intent.getBooleanExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,false);
    }

    private void bindViewModel(){

        forumViewModel.getThreadInfoListLiveData().observe(this, new Observer<List<ThreadInfo>>() {
            @Override
            public void onChanged(List<ThreadInfo> threadInfos) {
                Map<String,String> threadTypeMap = null;
                if(forumViewModel.displayForumResultMutableLiveData.getValue()!=null &&
                        forumViewModel.displayForumResultMutableLiveData.getValue().forumVariables.threadTypeInfo !=null){
                    threadTypeMap = forumViewModel.displayForumResultMutableLiveData.getValue().forumVariables.threadTypeInfo.idNameMap;

                }
                adapter.setThreadInfoList(threadInfos,threadTypeMap);


            }
        });
        Context context = this;
        forumViewModel.hasLoadAll.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                    boolean needVibrate = prefs.getBoolean(getString(R.string.preference_key_vibrate_when_load_all),true);
                    Toasty.success(getApplication(),getString(R.string.thread_has_load_all),Toast.LENGTH_SHORT).show();
                    if(needVibrate){
                        VibrateUtils.vibrateSlightly(context);

                    }
                }
            }
        });

        forumViewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        forumViewModel.jsonString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                // reload chip if possible
                configureThreadTypeChipGroup(s);
                returned_res_json = s;
            }
        });
        forumViewModel.displayForumResultMutableLiveData.observe(this, new Observer<ForumResult>() {
            @Override
            public void onChanged(ForumResult forumResult) {
                setBaseResult(forumResult,forumResult!=null?forumResult.forumVariables:null);
                if(forumResult != null && forumResult.isError()){
                    String errorString = forumResult.message.content;
                    Toasty.error(getApplicationContext(),errorString,Toast.LENGTH_LONG).show();
                    VibrateUtils.vibrateForError(getApplication());
                    moreThreadBtn.setVisibility(View.GONE);
                    errorTextview.setText(errorString);

                    errorLayout.setVisibility(View.VISIBLE);
                }
                else {
                    errorLayout.setVisibility(View.GONE);
                    moreThreadBtn.setVisibility(View.VISIBLE);
                }

                // deal with sublist
                if(forumResult !=null && forumResult.forumVariables!=null){
                    subForumAdapter.setSubForumInfoList(forumResult.forumVariables.subForumLists);
                    if(forumResult.forumVariables.forumInfo !=null){
                        ForumInfo forumInfo = forumResult.forumVariables.forumInfo;
                        forum = forumInfo;
                        if(getSupportActionBar()!=null){
                            getSupportActionBar().setTitle(forumInfo.name);
                            getSupportActionBar().setSubtitle(forumInfo.description);
                        }
                    }
                }
            }

        });

        forumViewModel.forumDetailedInfoMutableLiveData.observe(this, new Observer<ForumInfo>() {
            @Override
            public void onChanged(ForumInfo forumInfo) {
                // for rules
                if(!hasLoadOnce){
                    recordViewHistory(forumInfo);
                    hasLoadOnce = true;
                }

                if(! mForumRule.getText().equals(forumInfo.rules)){
                    String s = forumInfo.rules;
                    if(s!=null && s.length() !=0){
                        MyTagHandler myTagHandler = new MyTagHandler(getApplication(),mForumRule,mForumInfoCardView);
                        MyImageGetter myImageGetter = new MyImageGetter(getApplication(),mForumRule,mForumInfoCardView);
                        Spanned sp = Html.fromHtml(s,myImageGetter,myTagHandler);
                        SpannableString spannableString = new SpannableString(sp);
                        // mForumAlert.setAutoLinkMask(Linkify.ALL);
                        mForumRule.setMovementMethod(new bbsLinkMovementMethod(ForumActivity.this));
                        mForumRule.setText(spannableString, TextView.BufferType.SPANNABLE);
                        //collapseTextView(mForumRule,3);
                    }
                    else {
                        mForumRule.setText(R.string.bbs_rule_not_set);
                    }
                }


                // for description
                if(!mForumDesciption.getText().equals(forumInfo.description)){
                    String s = forumInfo.description;
                    if(s!=null && s.length() !=0){
                        MyTagHandler myTagHandler = new MyTagHandler(getApplication(),mForumAlert,mForumInfoCardView);
                        MyImageGetter myImageGetter = new MyImageGetter(getApplication(),mForumAlert,mForumInfoCardView);
                        Spanned sp = Html.fromHtml(s,myImageGetter,myTagHandler);
                        SpannableString spannableString = new SpannableString(sp);
                        // mForumAlert.setAutoLinkMask(Linkify.ALL);
                        mForumAlert.setMovementMethod(new bbsLinkMovementMethod(ForumActivity.this));
                        mForumAlert.setText(spannableString, TextView.BufferType.SPANNABLE);
                    }
                    else {
                        mForumAlert.setText(R.string.bbs_forum_description_not_set);
                    }
                }

            }
        });

        forumViewModel.favoriteForumLiveData.observe(this, favoriteForum -> {
            Log.d(TAG,"Detecting change favorite forum "+favoriteForum);
            if(favoriteForum!=null){
                Log.d(TAG,"favorite forum id "+favoriteForum.id);
            }
            invalidateOptionsMenu();
        });


        forumViewModel.ruleTextCollapse.observe(this,aBoolean -> {

            if(aBoolean){
                Log.d(TAG,"Collapse rule text "+aBoolean);
                mForumRule.setMaxLines(5);
            }
            else {

                mForumRule.setMaxLines(Integer.MAX_VALUE);
            }
        });

    }

    private void setForumRuleCollapseListener(){
        mForumRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forumViewModel.toggleRuleCollapseStatus();
            }
        });
    }

    private void initLiveData(){

        URLUtils.ForumStatus forumStatus = new URLUtils.ForumStatus(forum.fid,1);
        forumViewModel.forumStatusMutableLiveData.setValue(forumStatus);

    }

    private void recordViewHistory(ForumInfo forumInfo){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean recordHistory = prefs.getBoolean(getString(R.string.preference_key_record_history),false);
        if(recordHistory){

            new InsertViewHistory(new ViewHistory(
                    forumInfo.iconUrl,
                    forumInfo.name,
                    bbsInfo.getId(),
                    forumInfo.description,
                    ViewHistory.VIEW_TYPE_FORUM,
                    forumInfo.fid,
                    0,
                    new Date()
            )).execute();
        }
    }

    private void reConfigureAndRefreshPage(URLUtils.ForumStatus status){
        status.hasLoadAll = false;
        status.page = 1;
        forumViewModel.forumStatusMutableLiveData.postValue(status);
        forumViewModel.setForumStatusAndFetchThread(forumViewModel.forumStatusMutableLiveData.getValue());

    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                URLUtils.ForumStatus status = forumViewModel.forumStatusMutableLiveData.getValue();
                reConfigureAndRefreshPage(status);

            }
        });
    }

    private void configurePostThreadBtn(){
        Context context = this;
        if(userBriefInfo == null){
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(returned_res_json != null){
                    forumUserBriefInfo forumUserBriefInfo = bbsParseUtils.parseBreifUserInfo(returned_res_json);
                    if(forumUserBriefInfo!=null && forumUserBriefInfo.isValid()){
                        Intent intent = new Intent(context, PublishActivity.class);
                        intent.putExtra("fid",fid);
                        intent.putExtra("fid_name",forum.name);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                        intent.putExtra(bbsConstUtils.PASS_POST_TYPE,bbsConstUtils.TYPE_POST_THREAD);

                        Log.d(TAG,"You pass fid name"+forum.name);
                        intent.putExtra("api_result",returned_res_json);
                        startActivity(intent);
                    }
                    else {
                        Toasty.info(context,context.getString(R.string.bbs_require_login_to_comment), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                        startActivity(intent);
                    }
                }
                else {
                    fab.setVisibility(View.GONE);
                }



            }
        });
    }



    private void configureActionBar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(bbsInfo.site_name);
        if(forum.name !=null){
            getSupportActionBar().setSubtitle(forum.name);
        }



    }

    private void configureChipGroupFilter(){
        mForumThreadTypeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                URLUtils.ForumStatus status = forumViewModel.forumStatusMutableLiveData.getValue();
                if(status == null){
                    status = new URLUtils.ForumStatus(forum.fid,1);
                }
                int position = -1;
                for(int i=0;i<ChoiceResList.size();i++){
                    int selectedId = ChoiceResList.get(i);
                    if(selectedId == checkedId){
                        position = i;
                    }
                }
                Log.d(TAG,"choice group "+checkedId+" POS "+position);
                if(position == -1){
                    // failed
                    status.clear();
                    reConfigureAndRefreshPage(status);
                }
                else {
                    ChoiceChipInfo choiceChipInfo = choiceChipInfoList.get(position);
                    // need to
                    switch (choiceChipInfo.filterType){

                        case (URLUtils.FILTER_TYPE_POLL):{
                            status.filter = "specialtype";
                            break;
                        }
                        case (URLUtils.FILTER_TYPE_NEWEST):{
                            status.filter = "lastpost";
                            break;
                        }
                        case (URLUtils.FILTER_TYPE_HEATS):{
                            status.filter = "heat";
                            break;
                        }
                        case (URLUtils.FILTER_TYPE_HOTTEST):{
                            status.filter = "hot";
                            break;
                        }
                        case (URLUtils.FILTER_TYPE_DIGEST):{
                            status.filter = "digest";
                            break;
                        }
                        case (URLUtils.FILTER_TYPE_ID):{
                            status.filterId = choiceChipInfo.filterName;
                            break;
                        }
                        default:{
                            status.clear();
                        }

                    }
                    reConfigureAndRefreshPage(status);
                }
            }
        });
    }



    private void configureForumInfo(){
//        SpannableString spannableString = new SpannableString(Html.fromHtml(forum.description));
//        mForumDesciption.setText(spannableString, TextView.BufferType.SPANNABLE);

        mForumThreadNum.setText(numberFormatUtils.getShortNumberText(forum.threads));
        mForumPostNum.setText(numberFormatUtils.getShortNumberText(forum.posts));
    }

    private void configureRecyclerview(){
        subForumRecyclerview.setHasFixedSize(true);
        subForumRecyclerview.setLayoutManager(new GridLayoutManager(this,4));
        subForumAdapter = new SubForumAdapter(bbsInfo,userBriefInfo);
        subForumRecyclerview.setAdapter(subForumAdapter);

        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerview.setLayoutManager(linearLayoutManager);
        mRecyclerview.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        adapter = new ThreadAdapter(this,null,fid,bbsInfo,userBriefInfo);
        mRecyclerview.setAdapter(adapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    Boolean hasLoadAll = forumViewModel.hasLoadAll.getValue();
                    Boolean loading = forumViewModel.isLoading.getValue();
                    Log.d(TAG,"load all "+hasLoadAll+" page "+ forumViewModel.forumStatusMutableLiveData.getValue().page);
                    if(!loading && !hasLoadAll){
                        URLUtils.ForumStatus status = forumViewModel.forumStatusMutableLiveData.getValue();
                        if(status!=null){
                            status.page += 1;
                            forumViewModel.setForumStatusAndFetchThread(status);
                        }


                    }

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
        moreThreadBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                URLUtils.ForumStatus forumStatus = forumViewModel.forumStatusMutableLiveData.getValue();
                Boolean loadAll = forumViewModel.hasLoadAll.getValue();
                // to next page
                if(forumStatus!=null){
                    if(loadAll){
                        Toasty.info(getApplication(),getString(R.string.bbs_forum_thread_load_all),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        forumStatus.page += 1;
                        forumViewModel.getThreadList(forumStatus);
                    }

                }
            }
        });
    }

    private void configureFab(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void addTypeChipToChipGroup(String typeId, String name){
        choiceChipInfoList.add(new ChoiceChipInfo(URLUtils.FILTER_TYPE_ID,name,typeId));
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice,mForumThreadTypeChipGroup,false);
        Spanned threadSpanned = Html.fromHtml(name);
        SpannableString threadSpannableString = new SpannableString(threadSpanned);
        chip.setText(threadSpannableString);
        chip.setClickable(true);
        mForumThreadTypeChipGroup.addView(chip);
        ChoiceResList.add(chip.getId());
    }

    public void addFilterChipToChipGroup(String filterType,String name){
        choiceChipInfoList.add(new ChoiceChipInfo(filterType,name,name));

        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice,mForumThreadTypeChipGroup,false);

        chip.setChipBackgroundColor(getColorStateList(R.color.chip_background_select_state));
        //chip.setTextColor(createColorStateList(R.color.colorPureWhite,R.color.colorPr));
        chip.setTextColor(getColor(R.color.colorTextDefault));
        chip.setText(name);
        chip.setClickable(true);
        mForumThreadTypeChipGroup.addView(chip);
        ChoiceResList.add(chip.getId());
    }

    @Override
    public boolean onLinkClicked(String url) {
        return bbsLinkMovementMethod.parseURLAndOpen(this,bbsInfo,userBriefInfo,url);
    }


    private class ChoiceChipInfo{
        public String filterType,name,filterName;

        public ChoiceChipInfo(String filterType, String name, String filterName) {
            this.filterType = filterType;
            this.name = name;
            this.filterName = filterName;
        }
    }

    List<ChoiceChipInfo> choiceChipInfoList = new ArrayList<>();
    List<Integer> ChoiceResList = new ArrayList<>();

    boolean firstRenderChipGroup = true;

    private void configureThreadTypeChipGroup(String s){

        if(!firstRenderChipGroup){
            return;
        }
        Log.d(TAG,"Render thread type chips "+firstRenderChipGroup);
        choiceChipInfoList.clear();
        ChoiceResList.clear();
        mForumThreadTypeChipGroup.removeAllViews();
        // add oridnary filter first
        addFilterChipToChipGroup(URLUtils.FILTER_TYPE_POLL,getString(R.string.bbs_thread_filter_poll));
        addFilterChipToChipGroup(URLUtils.FILTER_TYPE_NEWEST,getString(R.string.bbs_thread_filter_newest));
        addFilterChipToChipGroup(URLUtils.FILTER_TYPE_HEATS,getString(R.string.bbs_thread_filter_heat));
        addFilterChipToChipGroup(URLUtils.FILTER_TYPE_HOTTEST,getString(R.string.bbs_thread_filter_hottest));
        addFilterChipToChipGroup(URLUtils.FILTER_TYPE_DIGEST,getString(R.string.bbs_thread_filter_digest));
        try{
            // parse it
            Map<String,String> threadTypeMap = bbsParseUtils.parseThreadType(s);
            for(String key:threadTypeMap.keySet()){
                String name = threadTypeMap.get(key);
                addTypeChipToChipGroup(key,name);
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        firstRenderChipGroup = false;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        String currentUrl = "";
        URLUtils.ForumStatus forumStatus = forumViewModel.forumStatusMutableLiveData.getValue();
        if(forumStatus == null || forumStatus.page == 1){
            currentUrl = URLUtils.getForumDisplayUrl(fid,"1");
        }
        else {
            currentUrl = URLUtils.getForumDisplayUrl(fid,String.valueOf(forumStatus.page-1));
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
            case R.id.bbs_share:{
                ForumResult result = forumViewModel.displayForumResultMutableLiveData.getValue();
                if(result!=null && result.forumVariables!=null && result.forumVariables.forumInfo!=null){
                    ForumInfo forumInfo = result.forumVariables.forumInfo;
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_template,
                            forumInfo.name,URLUtils.getForumDisplayUrl(String.valueOf(forum.fid),"1")));
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                }
                else {
                    Toasty.info(this,getString(R.string.share_not_prepared),Toast.LENGTH_SHORT).show();
                }
                return true;

            }
            case R.id.bbs_favorite:{
                ForumResult result = forumViewModel.displayForumResultMutableLiveData.getValue();
                if(result!=null && result.forumVariables!=null && result.forumVariables.forumInfo!=null){
                    ForumInfo forumInfo = result.forumVariables.forumInfo;

                    FavoriteForum favoriteForum = forumInfo.toFavoriteForm(bbsInfo.getId(),
                            userBriefInfo!=null?userBriefInfo.getUid():0
                    );
                    // save it to the database
                    // boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
                    FavoriteForum favoriteForumInDB = forumViewModel.favoriteForumLiveData.getValue();
                    Log.d(TAG,"Get db favorite formD "+favoriteForumInDB);
                    boolean isFavorite = favoriteForumInDB != null;
                    if(isFavorite){

                        new FavoritingForumAsyncTask(favoriteForumInDB,false).execute();

                    }
                    else {
                        // open up a dialog
                        launchFavoriteForumDialog(favoriteForum);
                        //new FavoritingThreadAsyncTask(favoriteThread,true).execute();
                    }

                }
                else {
                    Toasty.info(this,getString(R.string.favorite_thread_not_prepared),Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class InsertViewHistory extends AsyncTask<Void,Void,Void>{

        ViewHistory viewHistory;
        ViewHistoryDao dao;

        public InsertViewHistory(ViewHistory viewHistory){
            this.viewHistory = viewHistory;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao = ViewHistoryDatabase
                    .getInstance(getApplicationContext())
                    .getDao();
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

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // configureIntentData();
        if(userBriefInfo == null){
            getMenuInflater().inflate(R.menu.menu_incognitive_forum_nav_menu, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.bbs_forum_nav_menu,menu);

        }




        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        FavoriteForum favoriteForum = forumViewModel.favoriteForumLiveData.getValue();
        boolean isFavorite = favoriteForum != null;
        Log.d(TAG,"Triggering favorite status "+isFavorite+" "+favoriteForum);
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

    private void launchFavoriteForumDialog(FavoriteForum favoriteForum){
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
                new FavoritingForumAsyncTask(favoriteForum,true,description).execute();

            }
        });

        favoriteDialog.show();


    }

    public class FavoritingForumAsyncTask extends AsyncTask<Void,Void,Boolean> {

        FavoriteForum favoriteForum;
        FavoriteForumDao dao;
        boolean favorite, error=false;
        Retrofit retrofit;
        retrofit2.Call<ApiMessageActionResult> favoriteForumActionResultCall;
        MessageResult messageResult;
        String description = "";

        public FavoritingForumAsyncTask(FavoriteForum favoriteForum, boolean favorite){

            this.favoriteForum = favoriteForum;
            this.favorite = favorite;
        }

        public FavoritingForumAsyncTask(FavoriteForum favoriteForum, boolean favorite, String description){

            this.favoriteForum = favoriteForum;
            this.favorite = favorite;
            this.description = description;
            favoriteForum.description = description;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
            DiscuzApiService service = retrofit.create(DiscuzApiService.class);
            ForumResult result = forumViewModel.displayForumResultMutableLiveData.getValue();
            dao = FavoriteForumDatabase.getInstance(getApplicationContext()).getDao();
            if(result !=null
                    && result.forumVariables!=null
                    && favoriteForum.userId !=0
                    && UserPreferenceUtils.syncInformation(getApplication())){
                Log.d(TAG,"Favorite formhash "+ result.forumVariables.formHash);
                if(favorite){

                    favoriteForumActionResultCall = service.favoriteForumActionResult(
                            result.forumVariables.formHash
                            , favoriteForum.idKey,description);
                }
                else {
                    Log.d(TAG,"Favorite id "+ favoriteForum.favid);
                    if(favoriteForum.favid == 0){
                        // just remove it from database
                    }
                    else {
                        favoriteForumActionResultCall = service.unfavoriteForumActionResult(
                                result.forumVariables.formHash,
                                "true",
                                "a_delete_"+ favoriteForum.favid,
                                favoriteForum.favid);
                    }

                }

            }


        }

        @Override
        protected Boolean doInBackground(Void... voids) {



            if(favoriteForumActionResultCall!=null){
                try {
                    Log.d(TAG,"request favorite url "+favoriteForumActionResultCall.request().url());
                    retrofit2.Response<ApiMessageActionResult> response = favoriteForumActionResultCall.execute();
                    //Log.d(TAG,"get response "+response.raw().body().string());
                    if(response.isSuccessful() && response.body() !=null){

                        ApiMessageActionResult result = response.body();

                        messageResult = result.message;
                        String key = result.message.key;
                        if(favorite && key.equals("favorite_do_success")
                        ){
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                            dao.insert(favoriteForum);
                        }
                        else if(!favorite && key.equals("do_success")
                        ){
                            if(favoriteForum !=null){
                                dao.delete(favoriteForum);
                            }
                            dao.delete(bbsInfo.getId(),userBriefInfo.getUid(), favoriteForum.idKey);
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
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                            dao.insert(favoriteForum);

                            return true;
                        }
                        else {

                            // clear potential
                            dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                            //dao.delete(favoriteThread);
                            Log.d(TAG,"Just remove it from database "+favoriteForum.idKey);
                            return false;

                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    error = true;
                    messageResult = new MessageResult();
                    messageResult.content = e.getMessage();
                    messageResult.key = e.toString();
                    // insert as local database
                    if(favorite){
                        dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                        dao.insert(favoriteForum);

                        return true;
                    }
                    else {
                        // clear potential
                        dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                        //dao.delete(favoriteThread);
                        Log.d(TAG,"Just remove it from database "+favoriteForum.idKey);
                        return false;

                    }
                }

            }
            else {
                if(favorite){
                    dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                    dao.insert(favoriteForum);

                    return true;
                }
                else {
                    // clear potential
                    dao.delete(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0, favoriteForum.idKey);
                    //dao.delete(favoriteThread);
                    Log.d(TAG,"Just remove it from database "+favoriteForum.idKey);
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
                    Toasty.success(getApplication(),getString(R.string.discuz_error,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
                else if(!favorite && key.equals("do_success")){
                    Toasty.success(getApplication(),getString(R.string.discuz_error,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
                }
                else {
                    Toasty.warning(getApplication(),getString(R.string.discuz_error,messageResult.key,messageResult.content),Toast.LENGTH_LONG).show();
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
