package com.kidozh.discuzhub.services

import com.kidozh.discuzhub.results.*
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface DiscuzApiService {
    @GET("$DISCUZ_API_PATH?version=4&module=myfavthread")
    fun getFavoriteThreadResult(@Query("page") page: Int): Call<FavoriteThreadResult>

    @GET("$DISCUZ_API_PATH?version=4&module=myfavforum")
    fun getFavoriteForumResult(@Query("page") page: Int): Call<FavoriteForumResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=favthread&type=thread&ac=favorite&favoritesubmit=true")
    fun favoriteThreadActionResult(
        @Query("formhash") formhash: String,
        @Query("id") tid: Int,
        @Field("description") description: String
    ): Call<ApiMessageActionResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=favthread&type=all&ac=favorite&op=delete&inajax=1&favoritesubmit=true")
    fun unfavoriteThreadActionResult(
        @Field("formhash") formhash: String,
        @Field("deletesubmit") submit: String,
        @Field("handlekey") handleKey: String,
        @Query("favid") favid: Int
    ): Call<ApiMessageActionResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=favforum&type=forum&ac=favorite&favoritesubmit=true")
    fun favoriteForumActionResult(
        @Query("formhash") formhash: String,
        @Query("id") tid: Int,
        @Field("description") description: String
    ): Call<ApiMessageActionResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=favforum&type=all&ac=favorite&op=delete&inajax=1&favoritesubmit=true")
    fun unfavoriteForumActionResult(
        @Field("formhash") formhash: String,
        @Field("deletesubmit") submit: String,
        @Field("handlekey") handleKey: String,
        @Query("favid") favid: Int
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=mobilesign")
    fun mobileSignActionResult(
        @Query("hash") formHash: String
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=mynotelist")
    fun userNotificationListResult(
        @Query("page") page: Int
    ): Call<UserNoteListResult>

    @get:GET("$DISCUZ_API_PATH?version=4&module=login&mod=logging&action=login")
    val loginResult: Call<LoginResult>

    @GET("$DISCUZ_API_PATH?version=4&module=hotforum")
    fun hotForumResult(): Call<HotForumsResult>

    @GET("$DISCUZ_API_PATH?version=4&module=hotthread")
    fun hotThreadResult(
        @Query("page") page: Int
    ): Call<DisplayThreadsResult>

    @GET("$DISCUZ_API_PATH?version=4&module=forumindex")
    fun indexResult(): Call<DiscuzIndexResult>

    @GET("$DISCUZ_API_PATH?version=4&module=forumindex")
    fun indexResultRaw(): Call<String>

    @GET("$DISCUZ_API_PATH?version=4&module=forumdisplay")
    fun forumDisplayResult(@QueryMap options: HashMap<String, String>): Call<ForumResult>

    @GET("$DISCUZ_API_PATH?version=4&module=viewthread")
    fun viewThreadResult(@QueryMap options: HashMap<String, String>): Call<ThreadResult>

    @GET("$DISCUZ_API_PATH?version=4&module=viewthread")
    fun viewThreadResultRaw(@QueryMap options: HashMap<String, String>): Call<String>

    @GET("$DISCUZ_API_PATH?version=4&module=secure")
    fun secureResult(@Query("type") type: String): Call<SecureInfoResult>

    @GET("$DISCUZ_API_PATH?version=4&module=secure")
    fun captchaCall(@Query("type") type: String): Call<SecureInfoResult>

    @GET("$DISCUZ_API_PATH?version=4&module=recommend")
    fun recommendThread(
        @Query("hash") formhash: String,
        @Query("tid") tid: Int
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=recommend&do=substract")
    fun unrecommendThread(
        @Query("hash") formhash: String,
        @Query("tid") tid: Int
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=buythread")
    fun getThreadPriceInfo(
        @Query("tid") tid: Int
    ): Call<BuyThreadResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=buythread&paysubmit=yes")
    fun buyThread(
        @Field("tid") tid: Int,
        @Field("formhash") formhash: String,
        @Query("handlekey") pay: String
    ): Call<BuyThreadResult>

    @POST("$DISCUZ_API_PATH?version=5&module=report&reportsubmit=true&rtype=post&inajax=1")
    fun reportPost(
        @Query("formhash") formhash: String,
        @Query("rid") pid: Int,
        @Query("report_select") option: String,
        @Query("message") message: String
    ): Call<ApiMessageActionResult>

    @get:GET("$DISCUZ_API_PATH?version=4&module=check")
    val checkResult: Call<AddCheckResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=login")
    fun loginCall(@FieldMap options: HashMap<String, String>): Call<LoginResult>

    @GET("$DISCUZ_API_PATH?version=4&module=newthreads&limit=20")
    fun newThreadsResult(
        @Query("fids") fids: String,
        @Query("start") start: Int
    ): Call<NewThreadsResult>

    @get:GET("$DISCUZ_API_PATH?version=4&module=smiley")
    val smileyResult: Call<SmileyResult>

    @GET("$DISCUZ_API_PATH?version=4&module=mypm&subop=view")
    fun getPrivateMessageListResult(
        @Query("touid") toUid: Int,
        @Query("page") pageString: String
    ): Call<PrivateMessageResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&ac=pm&op=send&daterange=0&module=sendpm&pmsubmit=yes")
    fun sendPrivateMessage(
        @Query("plid") plid: Int,
        @Query("pmid") pmid: Int,
        @Field("formhash") formHash: String,
        @Field("message") message: String,
        @Field("touid") toUid: String
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=mythread")
    fun myThreadResult(
        @Query("page") page: Int
    ): Call<DisplayThreadsResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=sendreply&action=reply&replysubmit=yes")
    fun replyThread(
        @Query("fid") fid: Int,
        @Query("tid") tid: Int,
        @FieldMap options: HashMap<String, String>
    ): Call<ApiMessageActionResult>

    @GET("$DISCUZ_API_PATH?version=4&module=mynotelist")
    fun myNotificationList(
        @Query("view") view: String?,
        @Query("type") type: String?,
        @Query("page") page: Int
    ): Call<UserNoteListResult>

    @FormUrlEncoded
    @POST("$DISCUZ_API_PATH?version=4&module=topicadmin&action=moderate&modsubmit=yes&sendreasonpm=on")
    fun adminThread(
        @Field("formhash") formhash: String,
        @Field("fid") fid: Int,
        @Field("moderate[]") tids: List<Int>,
        @Field("operations[]") operations: List<String>,
        @Field("sticklevel") stickLevel :Int,
        @Field("digestlevel") digestLevel :Int,
        @Field("reason") reason: String,
    ): Call<ApiMessageActionResult>

    companion object {
        const val DISCUZ_API_PATH = "/api/mobile/index.php"
    }
}