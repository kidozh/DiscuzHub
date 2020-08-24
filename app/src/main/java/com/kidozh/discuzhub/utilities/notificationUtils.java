package com.kidozh.discuzhub.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;

public class notificationUtils {
    public static String updateProgressNotificationId = "updateProgressNotificationId";

    public static String NOTIFICATION_GROUP_KEY_BBS_UPDATE = "com.kidozh.discuzhub.bbsNotificationGroupUpdate";

    public static void createUpdateProgressNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_name_update_bbs_info_progress);
            String description = context.getString(R.string.notification_description_update_bbs_info);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(updateProgressNotificationId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }

        }
    }

    public static String userUpdateNotificationId = "userUpdateNotificationId";

    public static String NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE = "com.kidozh.discuzhub.bbsNotificationUserGroupUpdates";

    public static void createUsersUpdateChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_name_user_group_update);
            String description = context.getString(R.string.notification_description_user_group_update);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(userUpdateNotificationId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }

        }
    }

    public static void createBBSUpdateChannel(@NonNull Context context,@NonNull bbsInformation bbsInfo) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name_bbs_information,bbsInfo.site_name,bbsInfo.getId());
            String description = context.getString(R.string.channel_description_bbs_information,bbsInfo.site_name);
            String bbsChannelId = context.getString(R.string.channel_id_bbs_information,bbsInfo.getId());
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(bbsChannelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }

        }
    }
}
