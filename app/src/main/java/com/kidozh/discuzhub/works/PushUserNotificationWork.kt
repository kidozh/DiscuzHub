package com.kidozh.discuzhub.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.database.UserDatabase.Companion.getInstance
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.UserNoteListResult.UserNotification
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.NotificationUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.dontDisturbAtNight
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.syncInformation
import java.lang.String
import kotlin.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.apply

class PushUserNotificationWork(private val context: Context, workerParams: WorkerParameters) :
    Worker(
        context, workerParams
    ) {
    private var userBriefInfo: User? = null
    var Discuz: Discuz? = null
    private val isMidNight: Boolean
        private get() = false

    override fun doWork(): Result {
        // register notification channel
        NotificationUtils.createUsersUpdateChannel(context)
        Log.d(TAG, "WORK HAS BEEN STARTED")
        // check if settings allow sync
        val allowSync = syncInformation(context)
        Log.d(TAG, "allow sync permit $allowSync")
        if (!allowSync) {
            return Result.failure()
        }
        // check the time
        val dontDisturbAtNight = dontDisturbAtNight(context)
        if (dontDisturbAtNight && isMidNight) {
            Log.d(TAG, "dontDisturb $dontDisturbAtNight")
            return Result.success()
        }
        val fetchInfoUserId = inputData.getInt(ConstUtils.WORK_MANAGER_PASS_USER_ID_KEY, -1)
        Log.d(TAG, "GET FETCH USER ID $fetchInfoUserId")
        // fetch the user
        if (fetchInfoUserId == -1) {
            return Result.failure()
        }
        userBriefInfo = getInstance(context)
            .getforumUserBriefInfoDao()
            .getUserById(fetchInfoUserId)
        Log.d(TAG, "Get user info $userBriefInfo")
        // check the bbs's sync info
        if (userBriefInfo == null) {
            return Result.failure()
        }
        Discuz = DiscuzDatabase
            .getInstance(context)
            .forumInformationDao
            .getForumInformationById(userBriefInfo!!.belongedBBSID)
        // needs to set it
        if (Discuz == null) {
            return Result.failure()
        }
        URLUtils.setBBS(Discuz)
        return if (!Discuz!!.isSync) {
            // skip those
            Result.success()
        } else fetchAndParseInfo()
    }

    private fun fetchAndParseInfo(): Result {
        // fetch the information from server now
        val client = NetworkUtils.getPreferredClientWithCookieJarByUserWithDefaultHeader(
            context, userBriefInfo
        )
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        val notificationSituation = prefs.getStringSet(
            context.getString(R.string.preference_key_recv_notification_situation), HashSet()
        )
        val retrofit = NetworkUtils.getRetrofitInstance(Discuz!!.base_url, client)
        val apiService = retrofit.create(DiscuzApiService::class.java)
        val userNoteListResultCall = apiService.userNotificationListResult(1)
        return try {
            val response = userNoteListResultCall.execute()
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()
                if (result!=null) {
                    val totalCount = result.noteListVariableResult.count
                    val notificationList = result.noteListVariableResult.notificationList
                    var newNum = 0
                    for (i in notificationList.indices) {
                        val notification = notificationList[i]
                        if (notification.isNew) {
                            newNum += 1
                            // parse it
                            pushNewUserNotification(notification)
                        }
                    }
                    pushGroupNotification(newNum, totalCount)
                    Result.success()
                } else {
                    Result.failure()
                }
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private val intent: PendingIntent
        private get() {
            val intent = Intent(context, DrawerLayout::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            return PendingIntent.getActivity(context, 0, intent, 0)
        }

    private fun pushNewUserNotification(notification: UserNotification) {
        val notificationId = userBriefInfo!!.id
        NotificationUtils.createUsersUpdateChannel(context)
        var notificationContent = notification.note
        val notificationTag = String.valueOf(notification.id)
        notificationContent = notificationContent.replace("<.*?>".toRegex(), "")
        Log.d(TAG, "Get notification $notificationContent")
        val pendingIntent: PendingIntent = if (notification.notificationExtraInfo != null) {
            var thread: Thread = Thread().apply {
                tid = notification.notificationExtraInfo!!.tid
                authorId = notification.authorId
                subject = notification.notificationExtraInfo!!.subject
            }

            val intent = Intent(context, ThreadActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            intent.putExtra("TID", notification.notificationExtraInfo!!.tid)
            intent.putExtra("FID", notification.authorId)
            intent.putExtra("SUBJECT", notification.notificationExtraInfo!!.subject)
            intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)
            PendingIntent.getActivity(context, 0, intent, 0)

        } else {
            intent
        }

        val newUpdateNotification = NotificationCompat.Builder(
            context, NotificationUtils.userUpdateNotificationId
        )
            .setSmallIcon(R.drawable.ic_account_box_24px)
            .setContentTitle(
                context.getString(
                    R.string.notification_new_message,
                    userBriefInfo!!.username,
                    Discuz!!.site_name
                )
            )
            .setContentText(notificationContent) //.setLargeIcon()
            .setGroup(NotificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(
            context
        )
        notificationManager.notify(notificationTag, notificationId, newUpdateNotification)
    }

    private fun pushGroupNotification(newNum: Int, totalNum: Int) {
        if (newNum == 0) {
            return
        }
        val notificationId = userBriefInfo!!.id
        NotificationUtils.createUsersUpdateChannel(context)
        val newUpdateNotification = NotificationCompat.Builder(
            context, NotificationUtils.userUpdateNotificationId
        )
            .setSmallIcon(R.drawable.ic_account_box_24px)
            .setContentTitle(
                context.getString(
                    R.string.notification_group_new_message_title,
                    userBriefInfo!!.username,
                    Discuz!!.site_name
                )
            )
            .setContentText(
                context.getString(
                    R.string.notification_group_new_message_description,
                    newNum,
                    totalNum
                )
            ) //.setLargeIcon()
            .setGroup(NotificationUtils.NOTIFICATION_GROUP_KEY_USER_GROUP_UPDATE)
            .setAutoCancel(true)
            .setGroupSummary(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(
            context
        )
        notificationManager.notify(notificationId, newUpdateNotification)
    }

    companion object {
        private val TAG = PushUserNotificationWork::class.java.simpleName
    }
}