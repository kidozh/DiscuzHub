package com.kidozh.discuzhub.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.databinding.ActivityLoginByWebViewBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.LoginResult;
import com.kidozh.discuzhub.results.MessageResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Cookie;
import retrofit2.Retrofit;

public class WebViewLoginActivity extends BaseStatusActivity {
    private static String TAG = WebViewLoginActivity.class.getSimpleName();
    
    cookieWebViewClient cookieWebViewClientInstance;
    ActivityLoginByWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginByWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configureIntentData();
        configureActionBar();
        configureAlertDialog();


    }

    void configureAlertDialog(){
        new AlertDialog.Builder(this)
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
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        if(bbsInfo != null){
            URLUtils.setBBS(bbsInfo);
            configureWebView();
        }
        else {
            // judge whether from QQ
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
                String url = intent.getData().toString();
                Log.d(TAG,"Get QQ Login URL "+url);
                configureQQLoginWebview(url);
            }
        }

    }

    void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.bbs_login_by_browser);
            if(bbsInfo !=null){
                getSupportActionBar().setSubtitle(bbsInfo.site_name);
            }

        }
    }

    void configureWebView(){
        cookieWebViewClientInstance = new cookieWebViewClient();
        Log.d(TAG,"login web url "+ URLUtils.getLoginWebURL(bbsInfo));
        binding.loginByWebWebview.loadUrl(URLUtils.getLoginWebURL(bbsInfo));
        binding.loginByWebWebview.clearCache(true);
        WebSettings webSettings = binding.loginByWebWebview.getSettings();
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
            binding.loginByWebWebview.setWebViewClient(cookieWebViewClientInstance);

        }
    }

    void configureQQLoginWebview(String url){
        cookieWebViewClientInstance = new cookieWebViewClient();
        Log.d(TAG,"login qq url "+ url);
        binding.loginByWebWebview.loadUrl(url);


        // binding.loginByWebWebview.clearCache(true);
        WebSettings webSettings = binding.loginByWebWebview.getSettings();
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
            binding.loginByWebWebview.setWebViewClient(cookieWebViewClientInstance);

        }


    }

    // hook for qq
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String url = intent.getData().toString();
            Log.d(TAG,"Get QQ Login URL "+url);
            binding.loginByWebWebview.loadUrl(url);
        }
    }

    private void triggerQQLoginNoticeDialog(String url){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.qq_login_title))
                .setMessage(getString(R.string.qq_login_message))
                .setPositiveButton(android.R.string.ok,
                        (dialog,which)->{
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivityForResult(intent,0);
                        });
        builder.show();
    }

    public class cookieWebViewClient extends WebViewClient {
        CookieManager cookieManager;

        cookieWebViewClient(){
            cookieManager = CookieManager.getInstance();
        }

        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            if (url.startsWith("wtloginmqq://ptlogin/qlogin")) {
                // to new
                Log.d(TAG,"GET redirect URL "+url);
                // trigger the dialog
                triggerQQLoginNoticeDialog(url);

                return true;
            }
            else {
                webview.loadUrl(url);
                return true;
            }
        }

        public CookieManager getCookieString(){
            return cookieManager;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            binding.loginByWebProgressBar.setVisibility(View.VISIBLE);
        }

        public void onPageFinished(WebView view, String url) {
            //cookieManager = CookieManager.getInstance();
            String CookieStr = cookieManager.getCookie(url);

            Log.i(TAG, "URL "+url+" Cookies = " + CookieStr);
            super.onPageFinished(view, url);
            binding.loginByWebProgressBar.setVisibility(View.GONE);
        }

    }

    public void authUserIntergrity(){
        // get cookie from webview first
        forumUserBriefInfo userBriefInfo = new forumUserBriefInfo("","","","","",50,"");
        Log.d(TAG,"Send user id "+userBriefInfo.getId());
        NetworkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        OkHttpClient client = NetworkUtils.getPreferredClientWithCookieJar(getApplicationContext());
        // networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        String currentUrl = binding.loginByWebWebview.getUrl();
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
        NetworkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);

        client.cookieJar().saveFromResponse(httpUrl,cookieList);
        // exact login url
        Retrofit retrofit;
        if(bbsInfo !=null){
            retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        }
        else {
            String currentURL = binding.loginByWebWebview.getUrl();
            // parse base url
            String[] urlSegements = currentURL.split("/");
            StringBuilder baseURLBuilder = new StringBuilder();
            // match it
            if(urlSegements.length > 1){
                for(int i=0;i<urlSegements.length-1;i++){
                    baseURLBuilder.append(urlSegements[i]).append("/");
                }
            }
            retrofit = NetworkUtils.getRetrofitInstance(baseURLBuilder.toString(),client);


        }
        Handler mHandler = new Handler(Looper.getMainLooper());

        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        retrofit2.Call<LoginResult> loginResultCall = service.getLoginResult();
        loginResultCall.enqueue(new retrofit2.Callback<LoginResult>() {
            @Override
            public void onResponse(retrofit2.Call<LoginResult> call, retrofit2.Response<LoginResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    LoginResult result = response.body();
                    if(result.variables.getUserBriefInfo()!=null){
                        forumUserBriefInfo parsedUserInfo = result.variables.getUserBriefInfo();
                        Log.d(TAG,"Parse user info "+parsedUserInfo.uid+ " "+parsedUserInfo.getId());
                        // save it to database
                        if(bbsInfo != null){
                            parsedUserInfo.belongedBBSID = bbsInfo.getId();
                        }

                        //client.cookieJar().saveFromResponse(httpUrl,cookieList);
                        String cookie = response.headers().get("Set-Cookie");
                        Log.d(TAG,"SAVE Cookie to "+httpUrl.toString()+" cookie list "+cookieList.size()+" SET COOKIE"+cookie);
                        if(bbsInfo !=null){
                            new saveUserToDatabaseAsyncTask(parsedUserInfo,client,httpUrl).execute();
                        }
                        else {
                            String baseURL = retrofit.baseUrl().toString();
                            if(baseURL.endsWith("/")){
                                baseURL = baseURL.substring(0,baseURL.length()-1);
                            }
                            Log.d(TAG,"Parsed QQ base url "+baseURL);
                            new saveUserToDatabaseAsyncTask(parsedUserInfo,client,httpUrl,baseURL).execute();
                        }

                    }
                    else if(result.message!=null) {
                        MessageResult messageResult = result.message;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(getApplicationContext(),
                                        getString(R.string.discuz_api_message_template,messageResult.key,messageResult.content),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(getApplicationContext(),
                                        getString(R.string.parse_failed),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(getApplicationContext(),
                                    getString(R.string.discuz_api_message_template,String.valueOf(response.code()),response.message()),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LoginResult> call, Throwable t) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(t.getLocalizedMessage() !=null){
                            Toasty.error(getApplicationContext(),
                                    t.getLocalizedMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        else {
                            Toasty.error(getApplicationContext(),
                                    t.toString(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    }
                });
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
        HttpUrl httpUrl;
        List<bbsInformation> bbsInformationList = new ArrayList<>();
        List<Long> insertUserIdList = new ArrayList<>();
        String redirectURL;

        saveUserToDatabaseAsyncTask(forumUserBriefInfo userBriefInfo, OkHttpClient client, HttpUrl httpUrl){
            this.userBriefInfo = userBriefInfo;
            this.client = client;
            this.httpUrl = httpUrl;

        }

        saveUserToDatabaseAsyncTask(forumUserBriefInfo userBriefInfo,
                                    OkHttpClient client,
                                    HttpUrl httpUrl,
                                    String redirectURL){
            this.userBriefInfo = userBriefInfo;
            this.client = client;
            this.httpUrl = httpUrl;
            this.redirectURL = redirectURL;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(bbsInfo !=null){
                bbsInformationList.add(bbsInfo);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(bbsInfo == null){
                // search it
                bbsInformationList =
                        BBSInformationDatabase.getInstance(getApplicationContext())
                        .getForumInformationDao()
                        .getBBSInformationsByBaseURL(redirectURL);
                // insert them by bbs
                for(int i =0 ;i<bbsInformationList.size();i++){
                    bbsInformation bbsInfo = bbsInformationList.get(i);
                    userBriefInfo.belongedBBSID = bbsInfo.getId();
                    long insertedId = forumUserBriefInfoDatabase.getInstance(getApplicationContext())
                            .getforumUserBriefInfoDao().insert(userBriefInfo);
                    insertUserIdList.add(insertedId);
                }
            }
            else {
                long insertedId = forumUserBriefInfoDatabase.getInstance(getApplicationContext())
                        .getforumUserBriefInfoDao().insert(userBriefInfo);
                insertUserIdList.add(insertedId);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = getApplicationContext();
            if(bbsInfo!=null){
                Toasty.success(context,
                        String.format(context.getString(R.string.save_user_to_bbs_successfully_template),userBriefInfo.username,bbsInfo.site_name),
                        Toast.LENGTH_SHORT
                ).show();
            }
            else if(bbsInformationList.size() >1){
                Toasty.success(context,
                        String.format(context.getString(R.string.bulk_save_user_to_bbs_successfully_template),bbsInformationList.size()),
                        Toast.LENGTH_SHORT
                ).show();
            }
            else if(bbsInformationList.size() == 1) {
                Toasty.success(context,
                        String.format(context.getString(R.string.save_user_to_bbs_successfully_template),userBriefInfo.username,bbsInformationList.get(0).site_name),
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                Log.d(TAG,"No bbs found");
                Toasty.error(context,
                        String.format(context.getString(R.string.save_user_bbs_not_found),userBriefInfo.username,redirectURL),
                        Toast.LENGTH_LONG
                ).show();
            }
            for(long insertedId:insertUserIdList){
                Log.d(TAG,"save user to database id: "+userBriefInfo.getId()+"  "+insertedId);
                userBriefInfo.setId((int) insertedId);
                OkHttpClient savedClient = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplicationContext(),userBriefInfo);
                List<Cookie> cookies = client.cookieJar().loadForRequest(httpUrl);
                Log.d(TAG,"Http url "+httpUrl.toString()+" cookie list size "+cookies.size());
                savedClient.cookieJar().saveFromResponse(httpUrl,cookies);
                // manually set the cookie to shared preference
                SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(context.getSharedPreferences(NetworkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE));
                sharedPrefsCookiePersistor.saveAll(savedClient.cookieJar().loadForRequest(httpUrl));
                Log.d(TAG,"Http url "+httpUrl.toString()+" saved cookie list size "+savedClient.cookieJar().loadForRequest(httpUrl).size());
            }


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
