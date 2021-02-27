package com.kidozh.discuzhub.utilities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.text.Layout;

import androidx.appcompat.app.AlertDialog;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.InternalWebViewActivity;
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.Forum;
import com.kidozh.discuzhub.entities.Thread;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class bbsLinkMovementMethod extends LinkMovementMethod {

    private static final String TAG = bbsLinkMovementMethod.class.getSimpleName();


    private OnLinkClickedListener mOnLinkClickedListener;

    public bbsLinkMovementMethod(OnLinkClickedListener onLinkClickedListener){
        mOnLinkClickedListener = onLinkClickedListener;
    }

    int x1;
    int x2;
    int y1;
    int y2;



    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN){
            x1 = (int) event.getX();
            y1 = (int) event.getY();
        }

        if (action == MotionEvent.ACTION_UP) {
            x2 = (int) event.getX();
            y2 = (int) event.getY();

            if (Math.abs(x1 - x2) < 10 && Math.abs(y1 - y2) < 10) {

                x2 -= widget.getTotalPaddingLeft();
                y2 -= widget.getTotalPaddingTop();

                x2 += widget.getScrollX();
                y2 += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y2);
                int off = layout.getOffsetForHorizontal(line, x2);
                URLSpan[] spans = buffer.getSpans(off, off, URLSpan.class);
                if (spans.length != 0) {
                    String url = spans[0].getURL();
                    boolean handled = mOnLinkClickedListener.onLinkClicked(url);
                    if(handled){
                        return true;
                    }

                    return super.onTouchEvent(widget, buffer, event);
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    public boolean canSelectArbitrarily() {
        return true;
    }

    public boolean onKeyUp(TextView widget, Spannable buffer, int keyCode,
                           KeyEvent event) {
        return false;
    }

    public interface OnLinkClickedListener {
        boolean onLinkClicked(String url);
    }

    public static boolean parseURLAndOpen(Context context,
                                 Discuz bbsInfo,
                                 User userBriefInfo,
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
                            Forum clickedForum = new Forum();
                            clickedForum.fid = fid;

                            intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY,clickedForum);
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
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

                            Thread putThread = new Thread();
                            int tid = 0;
                            try{
                                tid = Integer.parseInt(tidStr);
                            }
                            catch (Exception e){
                                tid = 0;
                            }

                            putThread.tid = tid;
                            Intent intent = new Intent(context, ThreadActivity.class);
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread);
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
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
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
                    Thread putThread = new Thread();
                    putThread.tid = redirectTid;
                    Intent intent = new Intent(context, ThreadActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread);
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

                    Thread putThread = new Thread();
                    putThread.tid = redirectTid;
                    Intent intent = new Intent(context, ThreadActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread);
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
                    Forum clickedForum = new Forum();
                    clickedForum.fid = fid;

                    intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
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
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                    intent.putExtra("UID", uid);


                    context.startActivity(intent);
                    return true;
                }

                Intent intent = new Intent(context, InternalWebViewActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                intent.putExtra(ConstUtils.PASS_URL_KEY, url);
                Log.d(TAG, "Inputted URL " + url);
                context.startActivity(intent);
                return true;

            }
            else {
                Intent intent = new Intent(context, InternalWebViewActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                intent.putExtra(ConstUtils.PASS_URL_KEY, url);
                Log.d(TAG, "Inputted URL " + url);
                context.startActivity(intent);
                return true;
            }

        }
        else {
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
            boolean outLinkWarn = prefs.getBoolean(context.getString(R.string.preference_key_outlink_warn),true);
            if(outLinkWarn){

                new AlertDialog.Builder(context)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(context.getString(R.string.outlink_warn_message,clickedUri.getHost(),baseUri.getHost()))
                        .setNeutralButton(R.string.bbs_show_in_internal_browser, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, InternalWebViewActivity.class);
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                intent.putExtra(ConstUtils.PASS_URL_KEY,finalURL);
                                Log.d(TAG,"Inputted URL "+finalURL);
                                context.startActivity(intent);
                            }
                        })
                        .setPositiveButton(R.string.bbs_show_in_external_browser, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalURL));
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
            else {

                Intent intent = new Intent(context, InternalWebViewActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(ConstUtils.PASS_URL_KEY,url);
                Log.d(TAG,"Inputted URL "+url);
                context.startActivity(intent);
                return true;
            }
        }
    }


}
