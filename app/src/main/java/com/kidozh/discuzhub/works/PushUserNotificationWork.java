package com.kidozh.discuzhub.works;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PushUserNotificationWork extends Worker {
    private static final String TAG = PushUserNotificationWork.class.getSimpleName();
    private Context context;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInformation;
    public PushUserNotificationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }

    private boolean isMidNight(){
        return false;
    }

    @NonNull
    @Override
    public Result doWork() {
        // register notification channel
        notificationUtils.createUsersUpdateChannel(context);
        Log.d(TAG,"WORK HAS BEEN STARTED");
        // check if settings allow sync

        boolean allowSync = UserPreferenceUtils.syncInformation(context);
        Log.d(TAG,"allow sync permit "+allowSync);
        if(!allowSync){
            return Result.failure();
        }
        // check the time
        boolean dontDisturbAtNight = UserPreferenceUtils.dontDisturbAtNight(context);
        if(dontDisturbAtNight && isMidNight()){
            Log.d(TAG,"dontDisturb "+dontDisturbAtNight);
            return Result.success();
        }

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
        notificationUtils.createBBSUpdateChannel(context,bbsInformation);
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

        OkHttpClient client = NetworkUtils.getPreferredClientWithCookieJarByUserWithDefaultHeader(context,userBriefInfo);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> notificationSituation = prefs.getStringSet(context.getString(R.string.preference_key_recv_notification_situation),new HashSet<>());

        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInformation.base_url,client);
        DiscuzApiService apiService = retrofit.create(DiscuzApiService.class);
        Call<UserNoteListResult> userNoteListResultCall = apiService.userNotificationListResult(1);


        try{

            Response<UserNoteListResult> response = userNoteListResultCall.execute();
            if(response.isSuccessful() && response.body()!=null) {

                UserNoteListResult result = response.body();
                if(result.noteListVariableResult!=null && result.noteListVariableResult.notificationList!=null){
                    int totalCount = result.noteListVariableResult.count;
                    List<UserNoteListResult.UserNotification> notificationList =result.noteListVariableResult.notificationList;
                    int newNum = 0;
                    for(int i=0;i<notificationList.size();i++){
                        UserNoteListResult.UserNotification notification = notificationList.get(i);
                        if(notification.isNew){
                            newNum += 1;
                            // parse it
                            pushNewUserNotification(notification);

                        }
                    }

                    pushGroupNotification(newNum,totalCount);
                    return Result.success();
                }
                else {
                    return Result.failure();
                }


            }
            else {

                return Result.failure();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return Result.failure();
        }


    }

    private PendingIntent getIntent(){
        Intent intent = new Intent(context, DrawerLayout.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userBriefInfo);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return pendingIntent;
    }




    private void pushNewUserNotification(@NonNull UserNoteListResult.UserNotification notification){
        int notificationId = userBriefInfo.getId();
        notificationUtils.createUsersUpdateChannel(context);
        String notificationContent = notification.note;
        String notificationTag = String.valueOf(notification.id);
        notificationContent = notificationContent.replaceAll("<.*?>","");
        Log.d(TAG,"Get notification "+notificationContent);
        PendingIntent pendingIntent;
        if(notification.notificationExtraInfo != null){

            Intent intent = new Intent(context, ThreadActivity.class);
            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
            intent.putExtra("TID",notification.notificationExtraInfo.tid);
            intent.putExtra("FID",notification.authorId);
            intent.putExtra("SUBJECT",notification.notificationExtraInfo.subject);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        }
        else {
            pendingIntent = getIntent();
        }
        Notification newUpdateNotification = new NotificationCompat.Builder(context, notificationUtils.userUpdateNotificationId)
                .setSmallIcon(R.drawable.ic_account_box_24px)
                .setContentTitle(context.getString(R.string.notification_new_message,userBriefInfo.username,bbsInformation.site_name))
                .setContentText(notificationContent)
                //.setLargeIcon()
                .setGroup(notificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationTag,notificationId,newUpdateNotification);
    }

    private void pushGroupNotification(int newNum,int totalNum){
        int notificationId = userBriefInfo.getId();
        notificationUtils.createBBSUpdateChannel(context,bbsInformation);

        Notification newUpdateNotification = new NotificationCompat.Builder(context, notificationUtils.userUpdateNotificationId)
                .setSmallIcon(R.drawable.ic_account_box_24px)
                .setContentTitle(context.getString(R.string.notification_group_new_message_title,userBriefInfo.username,bbsInformation.site_name))
                .setContentText(context.getString(R.string.notification_group_new_message_description,newNum,totalNum))
                //.setLargeIcon()
                .setGroup(notificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId,newUpdateNotification);
    }


}
