package com.kidozh.discuzhub.services

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.pushes.ReplyThreadPush
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NotificationUtils
import org.json.JSONObject

class FirebasePushService : FirebaseMessagingService() {
    final val TAG = FirebasePushService::class.simpleName

    val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(ParameterNamesModule())

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG,"From ${p0.from}")

        // check if message contains a data payload
        if(p0.data.isNotEmpty()){
            Log.d(TAG, "Message data payload: ${p0.data}")
            // check with the type
            val payloadMap = p0.data

            when(payloadMap["type"]){
                "thread_reply" ->{
                    handleThreadReplyPush(payloadMap)
                }
            }

        }
    }

    private fun handleThreadReplyPush(payloadMap : Map<String,String>){
        val jsonString = JSONObject(payloadMap).toString()
        try {
            val replyThreadPush: ReplyThreadPush = objectMapper.readValue(jsonString, ReplyThreadPush::class.java)
            // check with database
            Thread{
                Log.d(TAG,"GET info ${replyThreadPush.siteHost} ${replyThreadPush.uid}")
                val dao = DiscuzDatabase.getInstance(applicationContext).forumInformationDao
                val discuz: Discuz? = dao.getBBSInformationByBaseURL(replyThreadPush.siteHost)
                Log.d(TAG,"GET discuz ${discuz}")
                // check with user
                if(discuz != null){
                    val userDao = UserDatabase.getInstance(applicationContext).getforumUserBriefInfoDao()
                    val user: User? = userDao.getFirstUserByDiscuzIdAndUid(discuz.id, replyThreadPush.uid)
                    // trigger a notification
                    createPushNotificationForReplyThread(discuz, user, replyThreadPush)
                }




            }.start()


        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun createPushNotificationForReplyThread(discuz: Discuz, user: User?, replyThreadPush: ReplyThreadPush){
        // create it
        // register the notification channel
        NotificationUtils.registerDiscuzNotification(applicationContext, discuz)
        var thread: com.kidozh.discuzhub.entities.Thread = com.kidozh.discuzhub.entities.Thread().apply {
            tid = replyThreadPush.tid
            authorId = replyThreadPush.uid
            subject = replyThreadPush.title
        }
        val intent = Intent(applicationContext, ThreadActivity::class.java)

        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
        intent.putExtra("TID", replyThreadPush.tid)
        intent.putExtra("FID", replyThreadPush.fid)
        intent.putExtra("SUBJECT", replyThreadPush.title)
        intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext,0, intent, FLAG_IMMUTABLE)
        val newUpdateNotification = NotificationCompat.Builder(
            applicationContext, NotificationUtils.getReplyNotificationChannelGroupId(discuz)
        )
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(getString(R.string.dhpush_reply_thread, replyThreadPush.senderName, replyThreadPush.title))
            .setContentText(replyThreadPush.message) //.setLargeIcon()
            .setGroup(NotificationUtils.getChannelGroupId(discuz))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(
            applicationContext
        )
        notificationManager.notify(replyThreadPush.uid.toString(), replyThreadPush.pid, newUpdateNotification)
    }
}