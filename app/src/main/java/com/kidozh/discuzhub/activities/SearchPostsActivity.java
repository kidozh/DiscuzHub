package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ActivitySearchPostsBinding;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class SearchPostsActivity extends BaseStatusActivity {
    private static final String TAG = SearchPostsActivity.class.getSimpleName();
    Activity activity = this;


    CookieWebViewClient cookieClient;

    String searchPostURL;
    
    ActivitySearchPostsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        getIntentInfo();
        configureActionBar();
        configurePostStartURL();
        configureWebview();



    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    void configureActionBar(){
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configurePostStartURL(){
        // this should be fixed
        searchPostURL = bbsInfo.base_url+"/search.php?mod=forum&mobile=2";
    }

    void configureWebview(){
        WebSettings webSettings = binding.searchPostWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        cookieClient = new CookieWebViewClient();
        cookieClient.cookieManager.setAcceptThirdPartyCookies(binding.searchPostWebview,true);
        // set cookie
        HttpUrl currentHttpUrl = HttpUrl.parse(searchPostURL);
        if(currentHttpUrl!=null){
            List<Cookie> cookies = client.cookieJar().loadForRequest(currentHttpUrl);
            for(int i=0;i<cookies.size();i++){
                Cookie cookie = cookies.get(i);
                String value = cookie.name() + "=" + cookie.value();
                cookieClient.cookieManager.setCookie(cookie.domain(),value);

                Log.d(TAG,"Cookie "+cookie.domain()+" val "+value);
            }
        }


        binding.searchPostWebview.setWebViewClient(cookieClient);

        binding.searchPostWebview.loadUrl(searchPostURL);



    }

    public class CookieWebViewClient extends WebViewClient {
        CookieManager cookieManager;

        CookieWebViewClient(){
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
            binding.searchPostProgressbar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            binding.searchPostProgressbar.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //return false;
            return parseURLAndOpen(activity,bbsInfo,userBriefInfo,request.getUrl().toString());
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
                if(binding.searchPostWebview.canGoBack()){
                    binding.searchPostWebview.goBack();
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
        String clickedURLPath = clickedUri.getPath();
        String basedURLPath = baseUri.getPath();
        if(clickedURLPath !=null && basedURLPath!=null){
            if(clickedURLPath.matches("^"+basedURLPath+".*")){
                clickedURLPath = clickedURLPath.substring(basedURLPath.length());
            }
        }



        if (clickedUri.getHost() == null || clickedUri.getHost().equals(baseUri.getHost())) {
            // check static first
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && clickedURLPath!=null) {

                if(!TextUtils.isEmpty(
                        UserPreferenceUtils.getRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY))
                ){
                    String rewriteRule = UserPreferenceUtils.getRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY);
                    UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY,rewriteRule);

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
                                fid= 0;
                            }
                            Intent intent = new Intent(context, ForumActivity.class);
                            ForumInfo clickedForum = new ForumInfo();
                            clickedForum.fid = fid;

                            intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,clickedForum);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            Log.d(TAG,"put base url "+bbsInfo.base_url);
                            VibrateUtils.vibrateForClick(context);
                            context.startActivity(intent);
                            return true;
                        }

                    }

                }
                if(!TextUtils.isEmpty(UserPreferenceUtils.getRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY))){
                    // match template such as t{tid}-{page}-{prevpage}
                    String rewriteRule = UserPreferenceUtils.getRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY);
                    UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY,rewriteRule);

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
                            intent.putExtra("FID","0");
                            intent.putExtra("TID",tid);
                            intent.putExtra("SUBJECT",url);
                            VibrateUtils.vibrateForClick(context);
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);

                            Bundle bundle = options.toBundle();
                            context.startActivity(intent,bundle);
                            return true;
                        }

                    }
                }

                if(!TextUtils.isEmpty(UserPreferenceUtils.getRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE))){
                    // match template such as s{user}-{name}
                    String rewriteRule = UserPreferenceUtils.getRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE);
                    //UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE,rewriteRule);
                    Log.d(TAG,"Get home space rewrite url "+rewriteRule+" path "+clickedURLPath);

                    // match template such as f{fid}-{page}
                    // crate reverse copy
                    rewriteRule = rewriteRule.replace("{user}","(?<user>\\w+)");
                    rewriteRule = rewriteRule.replace("{value}","(?<value>\\d+)");
                    Pattern pattern = Pattern.compile(rewriteRule);
                    Matcher matcher = pattern.matcher(clickedURLPath);
                    if(matcher.find()){

                        String userString = matcher.group("user");
                        String uidString = matcher.group("value");
                        Log.d(TAG,"Get uid "+uidString);
                        // handle it
                        if(uidString !=null){
                            int uid = 0;
                            try{
                                uid = Integer.parseInt(uidString);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }


                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            intent.putExtra("UID",uid);

                            VibrateUtils.vibrateForClick(context);
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);

                            Bundle bundle = options.toBundle();
                            context.startActivity(intent,bundle);
                            return true;
                        }

                    }
                }

            }


            if (uri != null && uri.getPath() != null) {
                if (uri.getQueryParameter("mod") != null
                        && uri.getQueryParameter("mod").equals("redirect")
                        && uri.getQueryParameter("goto") != null
                        && uri.getQueryParameter("goto").equals("findpost")
                        && uri.getQueryParameter("pid") != null
                        && uri.getQueryParameter("ptid") != null) {
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
                    int redirectTid = 0;
                    try{
                        redirectTid = Integer.parseInt(tidString);
                    }
                    catch (Exception e){
                        redirectTid = 0;
                    }

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
                    int fid = 0;
                    try{
                        fid = Integer.parseInt(fidString);
                    }
                    catch (Exception e){
                        fid = 0;
                    }

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
                    int uid = 0;
                    try{
                        uid = Integer.parseInt(uidStr);
                    }
                    catch (Exception e){
                        uid = 0;
                    }

                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra("UID", uid);


                    context.startActivity(intent);
                    return true;
                }

                return false;


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