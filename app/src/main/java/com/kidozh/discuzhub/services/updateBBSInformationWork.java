package com.kidozh.discuzhub.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsShowPortalActivity;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class updateBBSInformationWork extends Worker {
    private static final String TAG = updateBBSInformationWork.class.getSimpleName();
    private Context context;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInformation;
    public updateBBSInformationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }

    private static boolean inTime(Date nowTime, Date amBeginTime, Date amEndTime, Date pmBeginTime, Date pmEndTime) {
        //设置当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        //设置开始时间
        Calendar amBegin = Calendar.getInstance();
        amBegin.setTime(amBeginTime);//上午开始时间
        Calendar pmBegin = Calendar.getInstance();
        pmBegin.setTime(pmBeginTime);//下午开始时间
        //设置结束时间
        Calendar amEnd = Calendar.getInstance();
        amEnd.setTime(amEndTime);//上午结束时间
        Calendar pmEnd = Calendar.getInstance();
        pmEnd.setTime(pmEndTime);//下午结束时间
        //处于开始时间之后，和结束时间之前的判断
        if ((date.after(amBegin) && date.before(amEnd)) || (date.after(pmBegin) && date.before(pmEnd))) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean isMidNight(){


        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");// set date format

        try{
            //上午的规定时间
            Date amBeginTime = df.parse("23:00");
            Date amEndTime = df.parse("24:00");
            //下午的规定时间
            Date pmBeginTime = df.parse("00:00");
            Date pmEndTime = df.parse("08:00");
            return inTime(now, amBeginTime, amEndTime,pmBeginTime,pmEndTime);
        }
        catch (Exception e){
            e.printStackTrace();
            // conservative return true
            return true;
        }



    }

    @NonNull
    @Override
    public Result doWork() {
        // register notification channel
        notificationUtils.createUsersUpdateChannel(context);
        Log.d(TAG,"WORK HAS BEEN STARTED");
        // check if settings allow sync
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean allowSync = prefs.getBoolean(context.getString(R.string.preference_key_sync_information),false);
        boolean autoPostBackup = prefs.getBoolean(context.getString(R.string.preference_key_auto_post_backup),false);
        Log.d(TAG,"allow sync permit "+allowSync + " "+autoPostBackup);
        if(!allowSync){
            return Result.success();
        }
        // check the time
//        boolean dontDisturbAtNight = prefs.getBoolean(context.getString(R.string.preference_key_dont_distrub_at_night),true);
//        if(dontDisturbAtNight && isMidNight()){
//            Log.d(TAG,"dontDisturb "+dontDisturbAtNight);
//            return Result.success();
//        }

        int fetchInfoUserId = getInputData().getInt(bbsConstUtils.WORK_MANAGER_PASS_USER_ID_KEY,-1);
        Log.d(TAG,"GET FETCH USER ID "+fetchInfoUserId);
        // fetch the user
        if(fetchInfoUserId == -1){
            return Result.failure();
        }

        userBriefInfo = forumUserBriefInfoDatabase
                .getInstance(context)
                .getforumUserBriefInfoDao()
                .getUserById(fetchInfoUserId);
        Log.d(TAG,"Get user info "+userBriefInfo);
        // check the bbs's sync info
        if(userBriefInfo == null){
            return Result.failure();
        }
        bbsInformation = BBSInformationDatabase
                .getInstance(context)
                .getForumInformationDao()
                .getForumInformationById(userBriefInfo.belongedBBSID);
        // needs to set it
        if(bbsInformation == null){
            return Result.failure();
        }
        URLUtils.setBBS(bbsInformation);
        if(!bbsInformation.isSync){
            // skip those
            return Result.success();
        }


        return fetchAndParseInfo();

    }



    private Result fetchAndParseInfo(){
        // fetch the information from server now

        OkHttpClient client = networkUtils.getPreferredClientWithCookieJarByUserWithDefaultHeader(context,userBriefInfo);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> notificationSituation = prefs.getStringSet(context.getString(R.string.preference_key_recv_notification_situation),new HashSet<>());

        Request request = new Request.Builder()
                .url(URLUtils.getLoginSecondaryUrl())
                .build();

        try{
            Response response = client.newCall(request).execute();
            if(response.isSuccessful() && response.body()!=null){

                String s = response.body().string();
                Log.d(TAG,"get response api "+s);
                bbsParseUtils.noticeNumInfo noticeInfo = bbsParseUtils.parseNoticeInfo(s);
                if(noticeInfo == null){
                    // cannot parse the information
                    if(notificationSituation.contains("failure")){
                        sendAPIFailNotification();
                    }

                    return Result.failure();
                }
                else {
                    if(noticeInfo.getAllNoticeInfo()!=0){
                        // notify the information
                        if(notificationSituation.contains("new_info")){
                            sendNotificationNum(noticeInfo);
                        }

                    }
                    else {
                        // nothing new...
                        if(notificationSituation.contains("nothing")){
                            sendNoUpdateNotification();
                        }

                    }

                    return Result.success();
                }
            }
            else {
                Log.d(TAG,"Failed to get information "+userBriefInfo.username);
                if(notificationSituation.contains("failure")){
                    sendAPIFailNotification();
                }
                return Result.failure();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"Failed to get information "+userBriefInfo.username);
            sendAPIFailNotification();
            return Result.failure();
        }

    }

    private PendingIntent getIntent(){
        Intent intent = new Intent(context, bbsShowPortalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return pendingIntent;
    }

    private void sendNoUpdateNotification(){
        notificationUtils.createUsersUpdateChannel(context);
        // construct string

        int GeneratedNotificationId = userBriefInfo.getId();

        Notification newUpdateNotification = new NotificationCompat.Builder(context, notificationUtils.userUpdateNotificationId)
                .setSmallIcon(R.drawable.vector_drawable_account_box_24px)
                .setContentTitle(context.getString(R.string.bbs_updates_templates,bbsInformation.site_name))
                .setContentText(context.getString(R.string.bbs_update_none_template,
                        bbsInformation.site_name,
                        userBriefInfo.username))
                //.setLargeIcon()
                .setGroup(notificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
                .setAutoCancel(true)
                .setContentIntent(getIntent())
                .setAutoCancel(true)
                .setGroupSummary(true)

                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(GeneratedNotificationId,newUpdateNotification);
    }

    private void sendAPIFailNotification(){
        notificationUtils.createUsersUpdateChannel(context);
        // construct string

        int GeneratedNotificationId = userBriefInfo.getId();

        Notification newUpdateNotification = new NotificationCompat.Builder(context, notificationUtils.userUpdateNotificationId)
                .setSmallIcon(R.drawable.vector_drawable_account_box_24px)
                .setContentTitle(context.getString(R.string.bbs_notification_failed,bbsInformation.site_name))
                .setContentText(context.getString(R.string.bbs_notification_failed_to_parse_api,
                        bbsInformation.site_name,
                        userBriefInfo.username))
                //.setLargeIcon()
                .setGroup(notificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
                .setContentIntent(getIntent())
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(GeneratedNotificationId,newUpdateNotification);
    }

    private void sendNotificationNum(bbsParseUtils.noticeNumInfo noticeNumInfo){
        notificationUtils.createUsersUpdateChannel(context);
        // construct string
        String content = "";

        Resources res = context.getResources();
        if(noticeNumInfo.push !=0){
            content += res.getQuantityString(R.plurals.bbs_newpush_num,noticeNumInfo.push,noticeNumInfo.push);
        }
        if(noticeNumInfo.pm !=0){
            content += res.getQuantityString(R.plurals.bbs_newpm_num,noticeNumInfo.pm,noticeNumInfo.pm);
        }
        if(noticeNumInfo.prompt !=0){
            content += res.getQuantityString(R.plurals.bbs_newprompt_num,noticeNumInfo.prompt,noticeNumInfo.prompt);
        }
        if(noticeNumInfo.mypost !=0){
            content += res.getQuantityString(R.plurals.bbs_newmypost_num,noticeNumInfo.mypost,noticeNumInfo.mypost);
        }
        String notificationContent = context.getString(
                R.string.bbs_notification_update_template,
                bbsInformation.site_name,
                userBriefInfo.username,
                content
                );
        int GeneratedNotificationId = userBriefInfo.getId();

        Notification newUpdateNotification = new NotificationCompat.Builder(context, notificationUtils.userUpdateNotificationId)
                .setSmallIcon(R.drawable.vector_drawable_account_box_24px)
                .setContentTitle(context.getString(R.string.bbs_updates_templates,bbsInformation.site_name))
                .setContentText(notificationContent)
                //.setLargeIcon()
                .setGroup(notificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
                .setContentIntent(getIntent())
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(GeneratedNotificationId,newUpdateNotification);
    }
}
