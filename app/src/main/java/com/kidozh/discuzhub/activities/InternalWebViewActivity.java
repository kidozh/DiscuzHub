package com.kidozh.discuzhub.activities;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ActivityShowWebPageBinding;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class InternalWebViewActivity extends BaseStatusActivity {
    private final static String TAG = InternalWebViewActivity.class.getSimpleName();

    String startURL;
    

    cookieWebViewClient cookieClient;
    
    ActivityShowWebPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowWebPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentInfo();
        configureActionBar();
        configureWebview();

    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        startURL =  intent.getStringExtra(bbsConstUtils.PASS_URL_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    void configureActionBar(){
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    void configureWebview(){
        WebSettings webSettings = binding.webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        cookieClient = new cookieWebViewClient();
        cookieClient.cookieManager.setAcceptThirdPartyCookies(binding.webview,true);
        // set cookie
        HttpUrl currentHttpUrl = HttpUrl.parse(startURL);
        if(currentHttpUrl!=null){
            List<Cookie> cookies = client.cookieJar().loadForRequest(currentHttpUrl);
            for(int i=0;i<cookies.size();i++){
                Cookie cookie = cookies.get(i);
                String value = cookie.name() + "=" + cookie.value();
                cookieClient.cookieManager.setCookie(cookie.domain(),value);

                Log.d(TAG,"Cookie "+cookie.domain()+" val "+value);
            }
        }


        binding.webview.setWebViewClient(cookieClient);

        binding.webview.loadUrl(startURL);



    }

    public class cookieWebViewClient extends WebViewClient{
        CookieManager cookieManager;

        cookieWebViewClient(){
            cookieManager = CookieManager.getInstance();
        }

        public CookieManager getCookieString(){
            return cookieManager;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if(getSupportActionBar()!=null){
                getSupportActionBar().setIcon(new BitmapDrawable(getApplicationContext().getResources(),favicon));
                getSupportActionBar().setTitle(url);
            }
            binding.progressbar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            binding.progressbar.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
            //return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_webview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_close_web:{
                finishAfterTransition();
                return false;
            }
            case android.R.id.home:
                if(binding.webview.canGoBack()){
                    binding.webview.goBack();
                }
                else {
                    finishAfterTransition();
                }

                return false;

        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean parseURLAndOpen(Context context,
                                          bbsInformation bbsInfo,
                                          forumUserBriefInfo userBriefInfo,
                                          String url) {
        // simple unescape
        url = url
                .replace("&amp;","&")
                .replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&nbsp;"," ");
        final String finalURL = url;
        Log.d(TAG, "Parse and open URL " + url);

        Uri uri = Uri.parse(url);
        Uri baseUri = Uri.parse(bbsInfo.base_url);
        Uri clickedUri = Uri.parse(url);
        if (clickedUri.getHost() == null || clickedUri.getHost().equals(baseUri.getHost())) {
            if (uri != null && uri.getPath() != null) {
                if (uri.getQueryParameter("mod") != null
                        && uri.getQueryParameter("mod").equals("redirect")
                        && uri.getQueryParameter("goto") != null
                        && uri.getQueryParameter("goto").equals("findpost")
                        && uri.getQueryParameter("pid") != null
                        && uri.getQueryParameter("ptid") != null) {
                    String pidString = uri.getQueryParameter("pid");
                    String tidString = uri.getQueryParameter("ptid");
                    int redirectTid = Integer.parseInt(tidString);
                    int redirectPid = Integer.parseInt(pidString);
                    Log.d(TAG, "Find the current " + redirectPid + " tid " + redirectTid);
                    ThreadInfo putThreadInfo = new ThreadInfo();
                    putThreadInfo.tid = redirectTid;
                    Intent intent = new Intent(context, ThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, putThreadInfo);
                    intent.putExtra("FID", 0);
                    intent.putExtra("TID", redirectTid);
                    intent.putExtra("SUBJECT", url);
                    VibrateUtils.vibrateForClick(context);

                    context.startActivity(intent);
                    return true;
                } else if (uri.getQueryParameter("mod") != null
                        && uri.getQueryParameter("mod").equals("viewthread")
                        && uri.getQueryParameter("tid") != null) {
                    String tidString = uri.getQueryParameter("tid");
                    int redirectTid = Integer.parseInt(tidString);
                    ThreadInfo putThreadInfo = new ThreadInfo();
                    putThreadInfo.tid = redirectTid;
                    Intent intent = new Intent(context, ThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, putThreadInfo);
                    intent.putExtra("FID", 0);
                    intent.putExtra("TID", redirectTid);
                    intent.putExtra("SUBJECT", url);
                    VibrateUtils.vibrateForClick(context);

                    context.startActivity(intent);
                    return true;

                } else if (uri.getQueryParameter("mod") != null
                        && uri.getQueryParameter("mod").equals("forumdisplay")
                        && uri.getQueryParameter("fid") != null) {
                    String fidString = uri.getQueryParameter("fid");
                    int fid = Integer.parseInt(fidString);
                    Intent intent = new Intent(context, ForumActivity.class);
                    ForumInfo clickedForum = new ForumInfo();
                    clickedForum.fid = fid;

                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY, clickedForum);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    Log.d(TAG, "put base url " + bbsInfo.base_url);
                    VibrateUtils.vibrateForClick(context);
                    context.startActivity(intent);
                    return true;

                }
                else if(uri.getQueryParameter("mod")!=null
                        && uri.getQueryParameter("mod").equals("space")
                        && uri.getQueryParameter("uid")!=null) {
                    String uidStr = uri.getQueryParameter("uid");
                    int uid = Integer.parseInt(uidStr);
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra("UID", uid);


                    context.startActivity(intent);
                    return true;
                }

                Intent intent = new Intent(context, InternalWebViewActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_URL_KEY, url);
                Log.d(TAG, "Inputted URL " + url);
                context.startActivity(intent);
                return true;

            }
            else {
                return false;
            }

        }
        else {
            return false;
        }
    }
}
