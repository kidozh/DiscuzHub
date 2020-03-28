package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Cookie;

public class loginByWebViewActivity extends AppCompatActivity {
    private static String TAG = loginByWebViewActivity.class.getSimpleName();

    @BindView(R.id.login_by_web_webview)
    WebView webView;
    @BindView(R.id.login_by_web_progressBar)
    ProgressBar webViewProgressBar;
    bbsInformation curBBS;
    cookieWebViewClient cookieWebViewClientInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_web_view);
        ButterKnife.bind(this);
        configureIntentData();
        configureActionBar();
        configureAlertDialog();
        configureWebView();

    }

    void configureAlertDialog(){
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.bbs_login_in_webpage_alert)
                .setMessage(R.string.bbs_login_in_webpage_content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    void configureIntentData(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        bbsURLUtils.setBBS(curBBS);
    }

    void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.bbs_login_by_browser);
            getSupportActionBar().setSubtitle(curBBS.site_name);
        }
    }

    void configureWebView(){
        cookieWebViewClientInstance = new cookieWebViewClient();
        Log.d(TAG,"login web url "+bbsURLUtils.getLoginWebUrl());
        webView.loadUrl(bbsURLUtils.getLoginWebUrl());
        webView.clearCache(true);
        WebSettings webSettings = webView.getSettings();
        if(webSettings!=null){

            // to allow authentication to use JS
            webSettings.setJavaScriptEnabled(true);

            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);

            //缩放操作
            webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
            webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
            webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

            // other detailed information
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
            webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
            webView.setWebViewClient(cookieWebViewClientInstance);

        }


    }


    public class cookieWebViewClient extends WebViewClient {
        CookieManager cookieManager;

        cookieWebViewClient(){
            cookieManager = CookieManager.getInstance();
        }

        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            webview.loadUrl(url);
            return true;
        }

        public CookieManager getCookieString(){
            return cookieManager;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webViewProgressBar.setVisibility(View.VISIBLE);
        }

        public void onPageFinished(WebView view, String url) {
            //cookieManager = CookieManager.getInstance();
            String CookieStr = cookieManager.getCookie(url);

            Log.i(TAG, "URL "+url+" Cookies = " + CookieStr);
            super.onPageFinished(view, url);
            webViewProgressBar.setVisibility(View.GONE);
        }

    }

    public void authUserIntergrity(){
        // get cookie from webview first
        forumUserBriefInfo userBriefInfo = new forumUserBriefInfo("","","","","",50,"");
        Log.d(TAG,"Send user id "+userBriefInfo.getId());
        networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        OkHttpClient client = networkUtils.getPreferredClientWithCookieJar(getApplicationContext());
        // networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        String currentUrl = webView.getUrl();
        String cookieString = cookieWebViewClientInstance.cookieManager.getCookie(currentUrl);
        String[] cookieStringArray = cookieString.split(";");
        List<Cookie> cookieList = new ArrayList<>();
        HttpUrl httpUrl = HttpUrl.parse(currentUrl);
        for (int i =0; i<cookieStringArray.length;i++){
            String eachCookieString = cookieStringArray[i];
            Log.d(TAG,"http url "+httpUrl.toString()+" cookie "+eachCookieString);
            Cookie cookie = Cookie.parse(httpUrl,eachCookieString);
            cookieList.add(cookie);
        }
        networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);

        client.cookieJar().saveFromResponse(httpUrl,cookieList);
        // exact login url

        String loginApiUrl = bbsURLUtils.getLoginApiUrl();
        Request request = new Request.Builder()
                .url(loginApiUrl)
                .build();
        // secondary verification
        Log.d(TAG,"Login api url "+loginApiUrl);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(getApplicationContext(),getString(R.string.network_failed), Toast.LENGTH_SHORT).show();
                        // showing error information
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body()!=null){

                    String jsonString = response.body().string();
                    // parse it
                    Log.d(TAG,"get login API json "+jsonString);
                    forumUserBriefInfo parsedUserInfo = bbsParseUtils.parseLoginBreifUserInfo(jsonString);

                    if(parsedUserInfo!=null){
                        Log.d(TAG,"Parse user info "+parsedUserInfo.uid+ " "+parsedUserInfo.getId());
                        // save it to database
                        parsedUserInfo.belongedBBSID = curBBS.getId();
                        //client.cookieJar().saveFromResponse(httpUrl,cookieList);
                        String cookie = response.headers().get("Set-Cookie");
                        Log.d(TAG,"SAVE Cookie to "+httpUrl.toString()+" cookie list "+cookieList.size()+" SET COOKIE"+cookie);
                        new saveUserToDatabaseAsyncTask(parsedUserInfo,client,httpUrl).execute();


                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(getApplicationContext(),getString(R.string.bbs_login_user_null),Toast.LENGTH_SHORT).show();
                                // showing error information
                            }
                        });
                    }
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cookieWebViewClientInstance.cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {

            }
        });
    }

    private class saveUserToDatabaseAsyncTask extends AsyncTask<Void,Void,Void> {
        forumUserBriefInfo userBriefInfo;
        OkHttpClient client;
        long insertedId;
        HttpUrl httpUrl;

        saveUserToDatabaseAsyncTask(forumUserBriefInfo userBriefInfo, OkHttpClient client, HttpUrl httpUrl){
            this.userBriefInfo = userBriefInfo;
            this.client = client;
            this.httpUrl = httpUrl;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertedId = forumUserBriefInfoDatabase.getInstance(getApplicationContext())
                    .getforumUserBriefInfoDao().insert(userBriefInfo);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = getApplicationContext();
            Toasty.success(context,
                    String.format(context.getString(R.string.save_user_to_bbs_successfully_template),userBriefInfo.username,curBBS.site_name),
                    Toast.LENGTH_SHORT
            ).show();
            Log.d(TAG,"save user to database id: "+userBriefInfo.getId()+"  "+insertedId);
            userBriefInfo.setId((int) insertedId);
            OkHttpClient savedClient = networkUtils.getPreferredClientWithCookieJarByUser(getApplicationContext(),userBriefInfo);
            List<Cookie> cookies = client.cookieJar().loadForRequest(httpUrl);
            Log.d(TAG,"Http url "+httpUrl.toString()+" cookie list size "+cookies.size());
            savedClient.cookieJar().saveFromResponse(httpUrl,cookies);
            // manually set the cookie to shared preference
            SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(context.getSharedPreferences(networkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE));
            sharedPrefsCookiePersistor.saveAll(savedClient.cookieJar().loadForRequest(httpUrl));
            Log.d(TAG,"Http url "+httpUrl.toString()+" saved cookie list size "+savedClient.cookieJar().loadForRequest(httpUrl).size());
            // transiting data

//            networkUtils.copySharedPrefence(
//                    context.getSharedPreferences("CookiePersistence",Context.MODE_PRIVATE),
//                    context.getSharedPreferences(networkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE)
//            );
            //networkUtils.clearUserCookieInfo(context);

            finishAfterTransition();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_in_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                finishAfterTransition();
                return false;
            case R.id.action_login_in_web_finished:
                // do secondary authentication
                authUserIntergrity();
                return false;
        }


        return super.onOptionsItemSelected(item);
    }
}
