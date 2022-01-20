package com.kidozh.discuzhub.activities;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ActivityShowWebPageBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;


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
        discuz = (Discuz) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        user = (User) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        startURL =  intent.getStringExtra(ConstUtils.PASS_URL_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this, user);
    }

    void configureActionBar(){
        setSupportActionBar(binding.toolbar);
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
        int id = item.getItemId();
        if(id == R.id.action_close_web){
            finishAfterTransition();
            return true;
        }
        else if(id == android.R.id.home){
            if(binding.webview.canGoBack()){
                binding.webview.goBack();
            }
            else {
                finishAfterTransition();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
