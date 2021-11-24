package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.databind.ObjectMapper
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.BaseStatusActivity
import com.kidozh.discuzhub.daos.FavoriteThreadDao
import com.kidozh.discuzhub.database.FavoriteThreadDatabase
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.results.*
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils
import com.kidozh.discuzhub.utilities.bbsParseUtils.DetailedThreadInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ThreadViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = ThreadViewModel::class.java.simpleName
    lateinit var bbsInfo: Discuz
    lateinit var client: OkHttpClient
    private var forum: Forum? = null
    private var tid = 0
    private var user: User? = null
    var networkStatus = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    var notifyLoadAll = MutableLiveData(false)
    var formHash: MutableLiveData<String> = MutableLiveData("")
    var errorText: MutableLiveData<String>
    var pollLiveData: MutableLiveData<Poll?>
    var bbsPersonInfoMutableLiveData: MutableLiveData<User>
    var totalPostListLiveData: MutableLiveData<List<Post>> = MutableLiveData(ArrayList())
    val threadStatusMutableLiveData: MutableLiveData<ViewThreadQueryStatus> = MutableLiveData(ViewThreadQueryStatus(0,1))
    var detailedThreadInfoMutableLiveData: MutableLiveData<DetailedThreadInfo>
    var threadPostResultMutableLiveData: MutableLiveData<ThreadResult?> = MutableLiveData(null)
    var secureInfoResultMutableLiveData: MutableLiveData<SecureInfoResult?> = MutableLiveData(null)
    var recommendResultMutableLiveData = MutableLiveData<ApiMessageActionResult?>(null)
    var reportResultMutableLiveData = MutableLiveData<ApiMessageActionResult?>(null)
    var isFavoriteThreadMutableLiveData: LiveData<Boolean>? = null
    lateinit var favoriteThreadLiveData: LiveData<FavoriteThread?>
    var errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    var interactErrorMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    var threadPriceInfoMutableLiveData = MutableLiveData<BuyThreadResult?>(null)
    var buyThreadResultMutableLiveData = MutableLiveData<BuyThreadResult?>(null)
    var replyResultMutableLiveData = MutableLiveData<ApiMessageActionResult?>(null)
    var dao: FavoriteThreadDao

    var replyPostMutableLiveData: MutableLiveData<Post?> = MutableLiveData<Post?>(null)

    fun setBBSInfo(bbsInfo: Discuz, user: User?, forum: Forum?, tid: Int) {
        this.bbsInfo = bbsInfo
        this.user = user
        this.forum = forum
        this.tid = tid
        URLUtils.setBBS(bbsInfo)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)

        threadStatusMutableLiveData.postValue(ViewThreadQueryStatus(tid, 1))

        isFavoriteThreadMutableLiveData = dao.isFavoriteItem(bbsInfo.id, user?.getUid()
                ?: 0, tid, "tid")
        favoriteThreadLiveData = if(user == null){
            dao.getFavoriteItemByTid(bbsInfo.id, 0, tid, "tid")
        }
        else{
            dao.getFavoriteItemByTid(bbsInfo.id, user.getUid(), tid, "tid")
        }
    }


    val secureInfo: MutableLiveData<SecureInfoResult?>
        get() {
            val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
            val service = retrofit.create(DiscuzApiService::class.java)
            val secureInfoResultCall = service.secureResult("post")
            Log.d(TAG,"Send secure info "+secureInfoResultCall.request().url())
            secureInfoResultCall.enqueue(object : Callback<SecureInfoResult?> {
                override fun onResponse(call: Call<SecureInfoResult?>, response: Response<SecureInfoResult?>) {
                    if (response.isSuccessful && response.body() != null) {
                        secureInfoResultMutableLiveData.postValue(response.body())
                    } else {
                        secureInfoResultMutableLiveData.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SecureInfoResult?>, t: Throwable) {
                    secureInfoResultMutableLiveData.postValue(null)
                }
            })
            return secureInfoResultMutableLiveData
        }

    fun getThreadDetail(viewThreadQueryStatus: ViewThreadQueryStatus) {
        if (!NetworkUtils.isOnline(getApplication())) {
            networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()))
            return
        }
        networkStatus.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        // bbsThreadStatus threadStatus = threadStatusMutableLiveData.getValue();
        Log.d(TAG,"GET thread detail by status "+viewThreadQueryStatus.tid+" PAGE "+viewThreadQueryStatus.page)
        threadStatusMutableLiveData.postValue(viewThreadQueryStatus)
        if (viewThreadQueryStatus.page == 1) {
            // clear it first
            totalPostListLiveData.postValue(ArrayList())
        }
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)

        val threadResultCall = service.viewThreadResult(viewThreadQueryStatus.generateQueryHashMap())
        threadResultCall.enqueue(object : Callback<ThreadResult?> {
            override fun onResponse(call: Call<ThreadResult?>, response: Response<ThreadResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    var totalThreadSize = 0
                    val threadResult = response.body() as ThreadResult
                    var detailedThreadInfo: DetailedThreadInfo? = null
                    threadPostResultMutableLiveData.postValue(threadResult)
                    // update formhash first
                    formHash.postValue(threadResult.threadPostVariables.formHash)
                    // update user
                    bbsPersonInfoMutableLiveData.postValue(threadResult.threadPostVariables.userBriefInfo)
                    // parse detailed info
                    detailedThreadInfo = threadResult.threadPostVariables.detailedThreadInfo
                    detailedThreadInfoMutableLiveData.postValue(threadResult.threadPostVariables.detailedThreadInfo)
                    val pollInfo = threadResult.threadPostVariables.pollInfo
                    if (pollLiveData.value == null && pollInfo != null) {
                        pollLiveData.postValue(pollInfo)
                    }
                    val postInfoList = threadResult.threadPostVariables.postList
                    // remove null object
                    if (postInfoList.isNotEmpty()) {
                        val totalPosts = totalPostListLiveData.value as MutableList<Post>
                        Log.d(TAG,"GET posts "+postInfoList.size+" total posts "+totalPosts.size)
                        totalPosts.addAll(postInfoList.toList())
                        // totalPostListLiveData.postValue(postInfoList)
                        totalPostListLiveData.postValue(totalPosts.toList())
                        totalThreadSize = totalPosts.size

                    } else {
                        if (viewThreadQueryStatus.page == 1 && threadResult.message != null) {
                            errorText.postValue(getApplication<Application>().getString(R.string.parse_failed))
                        }
                        networkStatus.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)
                        // rollback
                        if (viewThreadQueryStatus.page != 1) {
                            viewThreadQueryStatus.page -= 1
                            Log.d(TAG, "Roll back page when page to " + viewThreadQueryStatus.page)
                            threadStatusMutableLiveData.postValue(viewThreadQueryStatus)
                        }
                    }
                    if (threadResult.message != null) {
                        errorMessageMutableLiveData.postValue(threadResult.message!!.toErrorMessage())
                        networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    }

                    // load all?
                    val maxThreadNumber = detailedThreadInfo.replies
                    val currentThreadList: List<Post> = totalPostListLiveData.value as List<Post>
                    val totalThreadCommentsNumber = currentThreadList.size

                    Log.d(TAG, "PAGE " + viewThreadQueryStatus.page + " MAX POSITION " + maxThreadNumber + " CUR " + totalThreadCommentsNumber + " " + totalThreadSize)
                    if (totalThreadSize >= maxThreadNumber + 1) {
                        networkStatus.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)
                    } else {
                        networkStatus.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
                    }

                    // networkStatus.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
                } else {
                    val responseString = response.errorBody()?.string()
                    Log.i(TAG,"response error body ${responseString}")
                    try{

                        Log.i(TAG,"REsp Error RAW ${responseString}")
                        val objectMapper = ObjectMapper()
                        objectMapper.readValue(responseString,ThreadResult::class.java)

                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }

                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                    if (viewThreadQueryStatus.page != 1) {
                        viewThreadQueryStatus.page -= 1
                        Log.d(TAG, "Roll back page when page to " + viewThreadQueryStatus.page)
                        threadStatusMutableLiveData.postValue(viewThreadQueryStatus)
                    }
                    networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                }
            }

            override fun onFailure(call: Call<ThreadResult?>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            }
        })
        Log.d(TAG, "Send request to " + threadResultCall.request().url().toString())
    }

    fun recommendThread(tid: Int, recommend: Boolean) {
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val formHashValue = formHash.value
        if (TextUtils.isEmpty(formHashValue)) {
            return
        }
        val recommendCall: Call<ApiMessageActionResult> = if (recommend) {
            service.recommendThread(formHashValue, tid)
        } else {
            service.unrecommendThread(formHashValue, tid)
        }
        recommendCall.enqueue(object : Callback<ApiMessageActionResult?> {
            override fun onResponse(call: Call<ApiMessageActionResult?>, response: Response<ApiMessageActionResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    val recommendMessageResult = response.body()
                    recommendResultMutableLiveData.postValue(recommendMessageResult)
                } else {
                    interactErrorMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<ApiMessageActionResult?>, t: Throwable) {
                interactErrorMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage))
            }
        })
    }

    fun getThreadPriceInfo(tid: Int) {
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val buyThreadResultCall = service.getThreadPriceInfo(tid)
        Log.d(TAG, "Send price information " + buyThreadResultCall.request().toString())
        buyThreadResultCall.enqueue(object : Callback<BuyThreadResult?> {
            override fun onResponse(call: Call<BuyThreadResult?>, response: Response<BuyThreadResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    threadPriceInfoMutableLiveData.postValue(response.body())
                    Log.d(TAG, "Get price information " + response.toString() + response.body())
                } else {
                    interactErrorMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<BuyThreadResult?>, t: Throwable) {
                interactErrorMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage))
            }
        })
    }

    fun reportPost(pid: Int, message: String?, isOtherReason: Boolean) {
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val formHashValue = formHash.value
        val reportPostCall: Call<ApiMessageActionResult> = if (isOtherReason) {
            service.reportPost(formHashValue, pid, getApplication<Application>().getString(R.string.report_option_others), message)
        } else {
            service.reportPost(formHashValue, pid, message, message)
        }
        Log.d(TAG, "Report to " + reportPostCall.request().url().toString())
        reportPostCall.enqueue(object : Callback<ApiMessageActionResult?> {
            override fun onResponse(call: Call<ApiMessageActionResult?>, response: Response<ApiMessageActionResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    reportResultMutableLiveData.postValue(response.body())
                } else {
                    interactErrorMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<ApiMessageActionResult?>, t: Throwable) {
                interactErrorMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage))
            }
        })
    }

    fun buyThread(tid: Int) {
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val formHashValue = formHash.value
        val buyThreadResultCall = service.buyThread(tid, formHashValue, "pay")
        buyThreadResultCall.enqueue(object : Callback<BuyThreadResult?> {
            override fun onResponse(call: Call<BuyThreadResult?>, response: Response<BuyThreadResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    buyThreadResultMutableLiveData.postValue(response.body())
                } else {
                    interactErrorMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<BuyThreadResult?>, t: Throwable) {
                interactErrorMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage))
            }
        })
    }

    fun favoriteThread(favoriteThread: FavoriteThread, favorite: Boolean, description: String?){
        var favoriteThreadActionResultCall: Call<ApiMessageActionResult>? = null
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val result = threadPostResultMutableLiveData.value

        if (result?.threadPostVariables != null
                && favoriteThread.userId != 0
                && UserPreferenceUtils.syncInformation(getApplication())) {
            if (favorite) {
                favoriteThreadActionResultCall = service.favoriteThreadActionResult(result.threadPostVariables.formHash, favoriteThread.idKey, description)
            } else {
                if (favoriteThread.favid == 0) {
                    // just remove it from database
                } else {
                    favoriteThreadActionResultCall = service.unfavoriteThreadActionResult(
                            result.threadPostVariables.formHash,
                            "true",
                            "a_delete_" + favoriteThread.favid,
                            favoriteThread.favid)
                }
            }
        }
        dao = FavoriteThreadDatabase.getInstance(getApplication()).dao
        favoriteThreadActionResultCall?.enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(call: Call<ApiMessageActionResult>, response: Response<ApiMessageActionResult>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body() as ApiMessageActionResult
                    val key = result.message?.key
                    Thread{
                        if (favorite && key == "favorite_do_success") {
                            dao.insert(favoriteThread)
                        } else if (!favorite && key == "do_success") {
                            dao.delete(favoriteThread)
                            dao.delete(bbsInfo.id, if (user != null) user!!.getUid() else 0, favoriteThread.idKey, "tid")
                        }
                    }.start()

                } else {
                    MessageResult().apply {
                        content = getApplication<Application>().getString(R.string.network_failed)
                        key = response.code().toString()
                    }
                    if(favorite){
                        Thread{
                            dao.delete(bbsInfo.id, if (user != null) user!!.getUid() else 0, favoriteThread.idKey, "tid")
                            dao.insert(favoriteThread)
                        }.start()

                    } else{
                        // clear potential
                        Thread{
                            dao.delete(bbsInfo.id, if (user != null) user!!.getUid() else 0, favoriteThread.idKey, "tid")
                        }.start()

                    }

                }


            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {

            }

        })

    }

    private fun needCaptcha(): Boolean {
        return !(secureInfoResultMutableLiveData.value == null || secureInfoResultMutableLiveData.value!!.secureVariables == null)
    }

    fun sendReplyRequest(fid: Int,message: String, captcha: String){
        Log.d(TAG, "Get message $message Captcha $captcha")
        val replyPost : Post? = replyPostMutableLiveData.value
        if(threadPostResultMutableLiveData.value == null){
            return
        }
        val result = threadPostResultMutableLiveData.value as ThreadResult
        // preparing forum parameters
        val formHash: String = result.threadPostVariables.formHash
        val now = Date()
        val formBody: HashMap<String,String> = HashMap<String,String>()
        formBody["usesig"] = "1"
        formBody["subject"] = ""
        formBody["tid"] = tid.toString()
        formBody["posttime"] = (now.time / 1000 - 1).toString()
        formBody["formhash"] = formHash
        // encoding the charset for discuz encoding
        val charsetType = result.getCharsetType()
        when (charsetType) {
            BaseStatusActivity.CHARSET_GBK -> {
                formBody["message"] = URLEncoder.encode(message, "GBK")

            }
            BaseStatusActivity.CHARSET_BIG5 -> {
                formBody["message"] = URLEncoder.encode(message, "BIG5")
            }
            else -> {
                formBody["message"] = message
            }
        }
        // captcha verification
        if (needCaptcha()) {
            val secureInfoResult = secureInfoResultMutableLiveData.value as SecureInfoResult
            formBody["seccodehash"] = secureInfoResult.secureVariables.secHash
            if(replyPost==null){
                // it's a post
                formBody["seccodemodid"] = "forum::viewthread"
            }
            else{
                formBody["seccodemodid"] = "forum::post"
            }

            Log.d(TAG,"Need captcha and the seccodeHash "+secureInfoResult.secureVariables.secHash)


            when (charsetType) {
                BaseStatusActivity.CHARSET_GBK -> {
                    formBody["seccodeverify"] = URLEncoder.encode(captcha, "GBK")
                }
                BaseStatusActivity.CHARSET_BIG5 -> {
                    formBody["seccodeverify"] = URLEncoder.encode(captcha, "BIG5")
                }
                else -> {
                    formBody["seccodeverify"] = captcha
                }
            }
        }
        if(replyPost != null){
            // reply to someone
            formBody.put("handlekey", "reply")
            formBody.put("reppid", replyPost.pid.toString())
            formBody.put("reppost", replyPost.pid.toString())
            val df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, Locale.getDefault())
            val publishAtString = df.format(replyPost.publishAt)
            val MAX_CHAR_LENGTH = 300
            val trimEnd = MAX_CHAR_LENGTH.coerceAtMost(replyPost.message.length)
            var replyMessage = replyPost.message.substring(0, trimEnd)
            if (replyPost.message.length > MAX_CHAR_LENGTH) {
                replyMessage += "..."
            }
            val noticeAuthorMsg = replyPost.message

            val noticeMsgTrimString = getApplication<Application>().getString(R.string.bbs_reply_notice_author_string,
                    URLUtils.getReplyPostURLInLabel(replyPost.pid, tid),
                    replyPost.author,
                    publishAtString,
                    replyMessage
            )
            when (charsetType) {
                BaseStatusActivity.CHARSET_GBK -> {
                    formBody["message"] = URLEncoder.encode(message, "GBK")
                    formBody["noticeauthormsg"] = URLEncoder.encode(noticeAuthorMsg, "GBK")
                    formBody["noticetrimstr"] = URLEncoder.encode(noticeMsgTrimString, "GBK")

                }
                BaseStatusActivity.CHARSET_BIG5 -> {
                    formBody["message"] = URLEncoder.encode(message, "BIG5")
                    formBody["noticeauthormsg"] = URLEncoder.encode(noticeAuthorMsg, "BIG5")
                    formBody["noticetrimstr"] = URLEncoder.encode(noticeMsgTrimString, "BIG5")
                }
                else -> {
                    formBody["message"] = message
                    formBody["noticeauthormsg"] = noticeAuthorMsg
                    formBody["noticetrimstr"] = noticeMsgTrimString
                }
            }

        }

        // start to send information
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val call = service.replyThread(fid,tid,formBody)
        call.enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(call: Call<ApiMessageActionResult>, response: Response<ApiMessageActionResult>) {
                if(response.isSuccessful && response.body() != null){
                    val result = response.body() as ApiMessageActionResult
                    replyResultMutableLiveData.postValue(result)
                }
                else{
                    interactErrorMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                interactErrorMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage))
            }

        })



    }

    init {
        bbsPersonInfoMutableLiveData = MutableLiveData()
        totalPostListLiveData = MutableLiveData()
        pollLiveData = MutableLiveData(null)
        errorText = MutableLiveData("")
        detailedThreadInfoMutableLiveData = MutableLiveData()
        threadPostResultMutableLiveData = MutableLiveData()
        dao = FavoriteThreadDatabase.getInstance(application).dao
    }
}