package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsPrivateDetailMessageAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class bbsPrivateMessageDetailActivity extends AppCompatActivity {

    private static final String TAG = bbsPrivateMessageDetailActivity.class.getSimpleName();

    @BindView(R.id.bbs_private_message_detail_swipeRefreshLayout)
    SwipeRefreshLayout privateMessageDetailSwipeRefreshLayout;
    @BindView(R.id.bbs_private_message_detail_recyclerview)
    RecyclerView privateMessageDetailRecyclerview;
    @BindView(R.id.bbs_private_message_comment_editText)
    EditText privateMessageCommentEditText;
    @BindView(R.id.bbs_private_message_comment_button)
    Button privateMessageCommentButton;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    bbsParseUtils.privateMessage privateMessageInfo;
    bbsPrivateDetailMessageAdapter adapter;
    private OkHttpClient client;
    private int globalPage = -1;
    private Boolean hasLoadAll=false;
    String formHash;
    String pmid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_private_message_detail);
        ButterKnife.bind(this);
        getIntentInfo();
        configureActionBar();
        configureRecyclerview();
        configureSwipeLayout();
        getPageInfo(globalPage);
        configureSendBtn();
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
        adapter = new bbsPrivateDetailMessageAdapter(curBBS,curUser);
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
    }


    private void getPageInfo(int page){
        privateMessageDetailSwipeRefreshLayout.setRefreshing(true);
        String apiStr = bbsURLUtils.getPrivatePMDetailApiUrlByPlid(privateMessageInfo.plid,page);
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
                                    privateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
                                }
                                else {
                                    adapter.addPrivateDetailMessageList(privateDetailMessages);
                                    privateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
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
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        privateMessageInfo = (bbsParseUtils.privateMessage) intent.getSerializableExtra(bbsConstUtils.PASS_PRIVATE_MESSAGE_KEY);
        // parse client
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
        if(curBBS == null){
            finish();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            bbsURLUtils.setBBS(curBBS);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(R.string.bbs_notification_my_pm);
            getSupportActionBar().setSubtitle(privateMessageInfo.toUsername);
        }


    }


    private void sendPrivateMessage(){

        FormBody formBody = new FormBody.Builder()
                .add("formhash",formHash)
                .add("message",privateMessageCommentEditText.getText().toString())
                .add("topmuid",String.valueOf(privateMessageInfo.toUid))
                .build();

        String apiStr = bbsURLUtils.getSendPMApiUrl(privateMessageInfo.plid,Integer.parseInt(pmid));
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
            this.finish();
            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
