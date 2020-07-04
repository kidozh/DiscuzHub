package com.kidozh.discuzhub.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class showWebPageActivity extends BaseStatusActivity {
    private String TAG = showWebPageActivity.class.getSimpleName();

    forumUserBriefInfo userBriefInfo;
    bbsInformation curBBS;

    @BindView(R.id.show_web_page_webview)
    WebView webView;
    @BindView(R.id.show_web_page_progressBar)
    ProgressBar webViewProgressbar;

    String startURL;
    OkHttpClient okHttpClient;

    cookieWebViewClient cookieClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_web_page);
        ButterKnife.bind(this);
        getIntentInfo();
        configureActionBar();
        configureWebview();

    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        startURL =  intent.getStringExtra(bbsConstUtils.PASS_URL_KEY);
        okHttpClient = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    void configureActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    void configureWebview(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        cookieClient = new cookieWebViewClient();
        cookieClient.cookieManager.setAcceptThirdPartyCookies(webView,true);
        // set cookie
        HttpUrl currentHttpUrl = HttpUrl.parse(startURL);
        if(currentHttpUrl!=null){
            List<Cookie> cookies = okHttpClient.cookieJar().loadForRequest(currentHttpUrl);
            for(int i=0;i<cookies.size();i++){
                Cookie cookie = cookies.get(i);
                String value = cookie.name() + "=" + cookie.value();
                cookieClient.cookieManager.setCookie(cookie.domain(),value);

                Log.d(TAG,"Cookie "+cookie.domain()+" val "+value);
            }
        }


        webView.setWebViewClient(cookieClient);

        webView.loadUrl(startURL);



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
            webViewProgressbar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            webViewProgressbar.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
            //return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(webView.canGoBack()){
                    webView.goBack();
                }
                else {
                    finishAfterTransition();
                }

                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
