package com.kidozh.discuzhub.utilities

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz

object NotificationUtils {
    var updateProgressNotificationId = "updateProgressNotificationId"
    var NOTIFICATION_GROUP_KEY_BBS_UPDATE = "com.kidozh.discuzhub.bbsNotificationGroupUpdate"
    @JvmStatic
    fun createUpdateProgressNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence =
                context.getString(R.string.notification_name_update_bbs_info_progress)
            val description = context.getString(R.string.notification_description_update_bbs_info)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(updateProgressNotificationId, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    var userUpdateNotificationId = "userUpdateNotificationId"
    var NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE =
        "com.kidozh.discuzhub.bbsNotificationUserGroupUpdates"

    @JvmStatic
    fun createUsersUpdateChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.notification_name_user_group_update)
            val description = context.getString(R.string.notification_description_user_group_update)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(userUpdateNotificationId, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun getChannelGroupId(discuz: Discuz):String{
        return "discuz_group_${discuz.id}";
    }

    fun getReplyNotificationChannelGroupId(discuz: Discuz):String{
        return "discuz_group_${discuz.id}_reply_thread";
    }

    fun registerDiscuzNotification(context: Context, discuz: Discuz){
        registerChannelGroup(context, discuz)
        registerReplyThreadChannel(context, discuz)
    }

    private fun registerChannelGroup(context: Context, discuz: Discuz){
        val channelGroupId = getChannelGroupId(discuz)
        val channelGroupName = discuz.site_name
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create channel for it
            notificationManager.createNotificationChannelGroup(NotificationChannelGroup(channelGroupId, channelGroupName))

        }
    }

    private fun registerReplyThreadChannel(context: Context, discuz: Discuz){
        val channelName = context.getString(R.string.dhpush_discuz_reply_thread_channel_name)
        val channelDescription = context.getString(R.string.dhpush_discuz_reply_thread_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getReplyNotificationChannelGroupId(discuz),
                channelName,
                importance
            )
            channel.description = channelDescription
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            channel.group = getChannelGroupId(discuz)
            notificationManager.createNotificationChannel(channel)
        }


    }
}