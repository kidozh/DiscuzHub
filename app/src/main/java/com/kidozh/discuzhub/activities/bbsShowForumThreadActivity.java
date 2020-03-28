package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsForumThreadAdapter;
import com.kidozh.discuzhub.adapter.bbsForumThreadCommentAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadInfo;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.kidozh.discuzhub.utilities.bbsURLUtils.FILTER_TYPE_HEATS;
import static com.kidozh.discuzhub.utilities.bbsURLUtils.FILTER_TYPE_NEWEST;

public class bbsShowForumThreadActivity extends AppCompatActivity {
    private static final String TAG = bbsShowForumThreadActivity.class.getSimpleName();


    @BindView(R.id.bbs_forum_fab)
    FloatingActionButton fab;
    @BindView(R.id.bbs_forum_description_textview)
    TextView mForumDesciption;
    @BindView(R.id.bbs_forum_alert_textview)
    TextView mForumAlert;
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
    @BindView(R.id.bbs_forum_info_cardview)
    CardView mForumInfoCardView;
    @BindView(R.id.bbs_forum_thread_type_chipgroup)
    ChipGroup mForumThreadTypeChipGroup;
    private forumInfo forum;
    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    private bbsForumThreadAdapter adapter;
    boolean isTaskRunning;
    String fid;
    String returned_res_json;
    MutableLiveData<bbsURLUtils.ForumStatus> forumStatusMutableLiveData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_forum_thread);
        ButterKnife.bind(this);
        configureIntentData();

        Log.d(TAG, "Get bbs information "+bbsInfo);

        initLiveData();
        configureActionBar();
        configureLiveData();

        configureFab();
        configureClient();
        configureForumInfo();
        configureRecyclerview();
        configureSwipeRefreshLayout();

        getThreadInfo();
        configurePostThreadBtn();
        configureChipGroupFilter();
    }


    private void configureIntentData(){
        Intent intent = getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        bbsURLUtils.setBBS(bbsInfo);
        fid = String.valueOf(forum.fid);
    }

    private void initLiveData(){

        bbsURLUtils.ForumStatus forumStatus = new bbsURLUtils.ForumStatus(forum.fid,1);
        forumStatusMutableLiveData = new MutableLiveData<>(forumStatus);
        forumStatusMutableLiveData.setValue(forumStatus);

    }

    private void configureLiveData(){
        forumStatusMutableLiveData.observe(this, new Observer<bbsURLUtils.ForumStatus>() {
            @Override
            public void onChanged(bbsURLUtils.ForumStatus status) {
                // trigger UI
                // ensure rollback safe

            }
        });
    }

    private void reConfigureAndRefreshPage(bbsURLUtils.ForumStatus status){
        status.hasLoadAll = false;
        status.page = 1;
        forumStatusMutableLiveData.postValue(status);
        getThreadInfo();

    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bbsURLUtils.ForumStatus status = forumStatusMutableLiveData.getValue();
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

    private void configureClient(){
        // client = networkUtils.getPreferredClient(this);
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
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
                bbsURLUtils.ForumStatus status = forumStatusMutableLiveData.getValue();
                if(status == null){
                    status = new bbsURLUtils.ForumStatus(forum.fid,1);
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

                        case (bbsURLUtils.FILTER_TYPE_POLL):{
                            status.filter = "specialtype";
                            break;
                        }
                        case (bbsURLUtils.FILTER_TYPE_NEWEST):{
                            status.filter = "lastpost";
                            break;
                        }
                        case (bbsURLUtils.FILTER_TYPE_HEATS):{
                            status.filter = "heat";
                            break;
                        }
                        case (bbsURLUtils.FILTER_TYPE_HOTTEST):{
                            status.filter = "hot";
                            break;
                        }
                        case (bbsURLUtils.FILTER_TYPE_DIGEST):{
                            status.filter = "digest";
                            break;
                        }
                        case (bbsURLUtils.FILTER_TYPE_ID):{
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
        mForumPostNum.setText(numberFormatUtils.getShortNumberText(forum.totalPost));
    }

    private void configureRecyclerview(){
        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                linearLayoutManager.getOrientation());
        mRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsForumThreadAdapter(this,"",fid,bbsInfo,userBriefInfo);
        mRecyclerview.setAdapter(adapter);
        moreThreadBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getThreadInfo();
                //new getThreadInfoTask(getApplicationContext()).execute();
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
        choiceChipInfoList.add(new ChoiceChipInfo(bbsURLUtils.FILTER_TYPE_ID,name,typeId));
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice,mForumThreadTypeChipGroup,false);
        chip.setText(name);
        chip.setClickable(true);
        mForumThreadTypeChipGroup.addView(chip);
        ChoiceResList.add(chip.getId());
    }

    private ColorStateList createColorStateList(int pressed, int normal) {
        //状态
        int[][] states = new int[2][];
        //按下
        states[0] = new int[] {android.R.attr.state_pressed};
        //默认
        states[1] = new int[] {};

        //状态对应颜色值（按下，默认）
        int[] colors = new int[] { pressed, normal};
        return new ColorStateList(states, colors);
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
        addFilterChipToChipGroup(bbsURLUtils.FILTER_TYPE_POLL,getString(R.string.bbs_thread_filter_poll));
        addFilterChipToChipGroup(bbsURLUtils.FILTER_TYPE_NEWEST,getString(R.string.bbs_thread_filter_newest));
        addFilterChipToChipGroup(bbsURLUtils.FILTER_TYPE_HEATS,getString(R.string.bbs_thread_filter_heat));
        addFilterChipToChipGroup(bbsURLUtils.FILTER_TYPE_HOTTEST,getString(R.string.bbs_thread_filter_hottest));
        addFilterChipToChipGroup(bbsURLUtils.FILTER_TYPE_DIGEST,getString(R.string.bbs_thread_filter_digest));
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

    private void getThreadInfo(){
        bbsURLUtils.ForumStatus forumStatus = forumStatusMutableLiveData.getValue();


        if(forumStatus!=null && forumStatus.hasLoadAll){
            swipeRefreshLayout.setRefreshing(false);
            Toasty.info(this,getString(R.string.bbs_forum_thread_load_all),Toast.LENGTH_SHORT).show();
            return;
        }
        Request request = new Request.Builder()
                .url(bbsURLUtils.getForumUrlByStatus(forumStatus))
                .build();
        Log.d(TAG,"Get thread info "+request.url().toString());
        Call call = client.newCall(request);

        isTaskRunning = true;
        Handler mHandler = new Handler(Looper.getMainLooper());
        Context context = this;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isTaskRunning = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        Toasty.error(context,getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isTaskRunning = false;
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"recv form thread "+s);
                    returned_res_json = s;
                    swipeRefreshLayout.setRefreshing(false);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            configureThreadTypeChipGroup(s);
                        }
                    });
                    List<threadInfo> threadInfoList;
                    if (forumStatus.page == 1){
                        threadInfoList = bbsParseUtils.parseThreadListInfo(s,true);
                    }
                    else {
                        threadInfoList = bbsParseUtils.parseThreadListInfo(s,false);
                    }

                    if(threadInfoList!=null){
                        if(adapter.threadInfoList == null || forumStatus.page == 1){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setThreadInfoList(threadInfoList,s);
                                }
                            });

                        }
                        else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.addThreadInfoList(threadInfoList,s);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                        // check
                        if(threadInfoList.size()< forumStatus.perPage){
                            forumStatus.hasLoadAll = true;
                            forumStatusMutableLiveData.postValue(forumStatus);
                        }


                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyTagHandler myTagHandler = new MyTagHandler(context,mForumAlert,mForumInfoCardView);
                            MyImageGetter myImageGetter = new MyImageGetter(context,mForumAlert,mForumInfoCardView);
                            Spanned sp;
                            if (bbsParseUtils.getThreadRuleString(s)!=null && bbsParseUtils.getThreadRuleString(s).equals("")){
                                sp = Html.fromHtml(bbsParseUtils.getThreadDescriptionString(s),myImageGetter,myTagHandler);

                            }
                            else {
                                sp = Html.fromHtml(bbsParseUtils.getThreadRuleString(s),myImageGetter,myTagHandler);
                            }
                            SpannableString spannableString = new SpannableString(sp);
                            // mForumAlert.setAutoLinkMask(Linkify.ALL);
                            mForumAlert.setMovementMethod(LinkMovementMethod.getInstance());
                            mForumAlert.setText(spannableString, TextView.BufferType.SPANNABLE);

                        }
                    });

                    forumStatus.page += 1;
                    forumStatusMutableLiveData.postValue(forumStatus);
                }
                else {

                    Toasty.error(context,getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        String currentUrl = "";
        bbsURLUtils.ForumStatus forumStatus = forumStatusMutableLiveData.getValue();
        if(forumStatus == null || forumStatus.page == 1){
            currentUrl = bbsURLUtils.getForumDisplayUrl(fid,"1");
        }
        else {
            currentUrl = bbsURLUtils.getForumDisplayUrl(fid,String.valueOf(forumStatus.page-1));
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
