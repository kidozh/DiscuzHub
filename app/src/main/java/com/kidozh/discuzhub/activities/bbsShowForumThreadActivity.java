package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsForumThreadAdapter;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.DisplayForumResult;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.viewModels.ForumThreadViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;

public class bbsShowForumThreadActivity extends AppCompatActivity {
    private static final String TAG = bbsShowForumThreadActivity.class.getSimpleName();


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
    private ForumInfo forum;
    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    private bbsForumThreadAdapter adapter;
    boolean isTaskRunning;
    String fid;
    String returned_res_json;
    //MutableLiveData<bbsURLUtils.ForumStatus> forumStatusMutableLiveData;

    ForumThreadViewModel forumThreadViewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_forum_thread);
        ButterKnife.bind(this);
        forumThreadViewModel = new ViewModelProvider(this).get(ForumThreadViewModel.class);
        configureIntentData();
        bindViewModel();

        Log.d(TAG, "Get bbs information "+bbsInfo);

        initLiveData();
        configureActionBar();

        configureFab();
        configureForumInfo();
        configureRecyclerview();
        configureSwipeRefreshLayout();

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
        forumThreadViewModel.setBBSInfo(bbsInfo,userBriefInfo,forum);
    }

    private void bindViewModel(){

        forumThreadViewModel.getThreadInfoListLiveData().observe(this, new Observer<List<ThreadInfo>>() {
            @Override
            public void onChanged(List<ThreadInfo> threadInfos) {
                Map<String,String> threadTypeMap = null;
                if(forumThreadViewModel.displayForumResultMutableLiveData.getValue()!=null &&
                        forumThreadViewModel.displayForumResultMutableLiveData.getValue().forumVariables.threadTypeInfo !=null){
                    threadTypeMap = forumThreadViewModel.displayForumResultMutableLiveData.getValue().forumVariables.threadTypeInfo.idNameMap;

                }
                adapter.setThreadInfoList(threadInfos,threadTypeMap);


            }
        });
        Context context = this;
        forumThreadViewModel.hasLoadAll.observe(this, new Observer<Boolean>() {
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

        forumThreadViewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                swipeRefreshLayout.setRefreshing(aBoolean);
            }
        });

        forumThreadViewModel.jsonString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                // reload chip if possible
                configureThreadTypeChipGroup(s);
                returned_res_json = s;
            }
        });
        forumThreadViewModel.displayForumResultMutableLiveData.observe(this, new Observer<DisplayForumResult>() {
            @Override
            public void onChanged(DisplayForumResult displayForumResult) {
                if(displayForumResult != null && displayForumResult.isError()){
                    String errorString = displayForumResult.message.content;
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
            }
        });

        forumThreadViewModel.forumDetailedInfoMutableLiveData.observe(this, new Observer<ForumInfo>() {
            @Override
            public void onChanged(ForumInfo forumInfo) {
                // for rules

                if(! mForumRule.getText().equals(forumInfo.rules)){
                    String s = forumInfo.rules;
                    if(s!=null && s.length() !=0){
                        MyTagHandler myTagHandler = new MyTagHandler(getApplication(),mForumRule,mForumInfoCardView);
                        MyImageGetter myImageGetter = new MyImageGetter(getApplication(),mForumRule,mForumInfoCardView);
                        Spanned sp = Html.fromHtml(s,myImageGetter,myTagHandler);
                        SpannableString spannableString = new SpannableString(sp);
                        // mForumAlert.setAutoLinkMask(Linkify.ALL);
                        mForumRule.setMovementMethod(LinkMovementMethod.getInstance());
                        mForumRule.setText(spannableString, TextView.BufferType.SPANNABLE);
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
                        mForumAlert.setMovementMethod(LinkMovementMethod.getInstance());
                        mForumAlert.setText(spannableString, TextView.BufferType.SPANNABLE);
                    }
                    else {
                        mForumAlert.setText(R.string.bbs_forum_description_not_set);
                    }
                }

            }
        });

    }

    private void initLiveData(){

        URLUtils.ForumStatus forumStatus = new URLUtils.ForumStatus(forum.fid,1);
        forumThreadViewModel.forumStatusMutableLiveData.setValue(forumStatus);

    }

    private void reConfigureAndRefreshPage(URLUtils.ForumStatus status){
        status.hasLoadAll = false;
        status.page = 1;
        forumThreadViewModel.forumStatusMutableLiveData.postValue(status);
        forumThreadViewModel.setForumStatusAndFetchThread(forumThreadViewModel.forumStatusMutableLiveData.getValue());

    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                URLUtils.ForumStatus status = forumThreadViewModel.forumStatusMutableLiveData.getValue();
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
                        Intent intent = new Intent(context,bbsPostThreadActivity.class);
                        intent.putExtra("fid",fid);
                        intent.putExtra("fid_name",forum.name);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);

                        Log.d(TAG,"You pass fid name"+forum.name);
                        intent.putExtra("api_result",returned_res_json);
                        startActivity(intent);
                    }
                    else {
                        Toasty.info(context,context.getString(R.string.bbs_require_login_to_comment), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, loginBBSActivity.class);
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(bbsInfo.site_name);
        getSupportActionBar().setSubtitle(forum.name);


    }

    private void configureChipGroupFilter(){
        mForumThreadTypeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                URLUtils.ForumStatus status = forumThreadViewModel.forumStatusMutableLiveData.getValue();
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
        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerview.setLayoutManager(linearLayoutManager);

        adapter = new bbsForumThreadAdapter(this,null,fid,bbsInfo,userBriefInfo);
        mRecyclerview.setAdapter(adapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    Boolean hasLoadAll = forumThreadViewModel.hasLoadAll.getValue();
                    Boolean loading = forumThreadViewModel.isLoading.getValue();
                    Log.d(TAG,"load all "+hasLoadAll+" page "+forumThreadViewModel.forumStatusMutableLiveData.getValue().page);
                    if(!loading && !hasLoadAll){
                        URLUtils.ForumStatus status = forumThreadViewModel.forumStatusMutableLiveData.getValue();
                        if(status!=null){
                            status.page += 1;
                            forumThreadViewModel.setForumStatusAndFetchThread(status);
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
                URLUtils.ForumStatus forumStatus = forumThreadViewModel.forumStatusMutableLiveData.getValue();
                Boolean loadAll = forumThreadViewModel.hasLoadAll.getValue();
                // to next page
                if(forumStatus!=null){
                    if(loadAll){
                        Toasty.info(getApplication(),getString(R.string.bbs_forum_thread_load_all),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        forumStatus.page += 1;
                        forumThreadViewModel.getThreadList(forumStatus);
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
        URLUtils.ForumStatus forumStatus = forumThreadViewModel.forumStatusMutableLiveData.getValue();
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
                Intent intent = new Intent(this, showPersonalInfoActivity.class);
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
            getMenuInflater().inflate(R.menu.bbs_forum_nav_menu,menu);

        }


        return true;
    }

}
