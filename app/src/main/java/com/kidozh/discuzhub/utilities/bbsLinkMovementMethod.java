package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.text.Layout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.InternalWebViewActivity;
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;


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
                /**
                 * get you interest span
                 */
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
                Intent intent = new Intent(context, InternalWebViewActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_URL_KEY, url);
                Log.d(TAG, "Inputted URL " + url);
                context.startActivity(intent);
                return true;
            }

        }
        else {
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
            boolean outLinkWarn = prefs.getBoolean(context.getString(R.string.preference_key_outlink_warn),true);
            if(outLinkWarn){

                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(context.getString(R.string.outlink_warn_message,clickedUri.getHost(),baseUri.getHost()))
                        .setNeutralButton(R.string.bbs_show_in_internal_browser, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, InternalWebViewActivity.class);
                                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                intent.putExtra(bbsConstUtils.PASS_URL_KEY,finalURL);
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
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_URL_KEY,url);
                Log.d(TAG,"Inputted URL "+url);
                context.startActivity(intent);
                return true;
            }
        }
    }


}
