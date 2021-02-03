package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
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
import kotlin.collections.ArrayList

class ThreadViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = ThreadViewModel::class.java.simpleName
    lateinit var bbsInfo: bbsInformation
    lateinit var client: OkHttpClient
    private var forum: Forum? = null
    private var tid = 0
    private var userBriefInfo: forumUserBriefInfo? = null
    var networkStatus = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    var notifyLoadAll = MutableLiveData(false)
    var formHash: MutableLiveData<String>
    var errorText: MutableLiveData<String>
    var pollLiveData: MutableLiveData<Poll?>
    var bbsPersonInfoMutableLiveData: MutableLiveData<forumUserBriefInfo>
    var totalPostListLiveData: MutableLiveData<MutableList<Post>>
    var newPostList: MutableLiveData<List<Post>> = MutableLiveData(ArrayList())
    lateinit var threadStatusMutableLiveData: MutableLiveData<ViewThreadQueryStatus>
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
    var dao: FavoriteThreadDao
    fun setBBSInfo(bbsInfo: bbsInformation, userBriefInfo: forumUserBriefInfo?, forum: Forum?, tid: Int) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        this.forum = forum
        this.tid = tid
        URLUtils.setBBS(bbsInfo)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), userBriefInfo)
        val viewThreadQueryStatus = ViewThreadQueryStatus(tid, 1)
        threadStatusMutableLiveData.value = viewThreadQueryStatus
        isFavoriteThreadMutableLiveData = dao.isFavoriteItem(bbsInfo.id, userBriefInfo?.getUid()
                ?: 0, tid, "tid")
        if(userBriefInfo == null){
            favoriteThreadLiveData = dao.getFavoriteItemByTid(bbsInfo.id, 0, tid, "tid")
        }
        else{
            favoriteThreadLiveData = dao.getFavoriteItemByTid(bbsInfo.id, userBriefInfo.getUid(), tid, "tid")
        }



        // bbsPersonInfoMutableLiveData.postValue(userBriefInfo);
    }


//    fun getSecureInfo(): MutableLiveData<SecureInfoResult?> {
//        return secureInfoResultMutableLiveData
//    }

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
        threadStatusMutableLiveData.postValue(viewThreadQueryStatus)
        if (viewThreadQueryStatus.page == 1) {
            // clear it first
            totalPostListLiveData.value = ArrayList()
        }
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val threadResultCall = service.viewThreadResult(viewThreadQueryStatus.generateQueryHashMap())
        threadResultCall.enqueue(object : Callback<ThreadResult?> {
            override fun onResponse(call: Call<ThreadResult?>, response: Response<ThreadResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    var totalThreadSize = 0
                    val threadResult = response.body()
                    var detailedThreadInfo: DetailedThreadInfo? = null
                    threadPostResultMutableLiveData.postValue(threadResult)
                    if (threadResult!!.threadPostVariables != null) {
                        // update formhash first
                        if (threadResult.threadPostVariables.formHash != null) {
                            formHash.postValue(threadResult.threadPostVariables.formHash)
                        }
                        // update user
                        if (threadResult.threadPostVariables != null) {
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
                            if (postInfoList.size != 0) {
                                newPostList.postValue(postInfoList)
                                if (viewThreadQueryStatus.page == 1) {
                                    totalPostListLiveData.postValue(postInfoList)
                                    totalThreadSize = postInfoList.size
                                } else {
                                    var currentThreadInfoList = totalPostListLiveData.value
                                    if (currentThreadInfoList == null) {
                                        currentThreadInfoList = ArrayList()
                                    }
                                    currentThreadInfoList.addAll(postInfoList)
                                    totalPostListLiveData.postValue(currentThreadInfoList)
                                    totalThreadSize = currentThreadInfoList.size
                                }
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
                        }
                        if (threadResult.message != null) {
                            errorMessageMutableLiveData.postValue(threadResult.message.toErrorMessage())
                            networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                        }

                        // load all?
                        if (detailedThreadInfo != null) {
                            val maxThreadNumber = detailedThreadInfo.replies
                            val currentThreadList: List<Post>? = totalPostListLiveData.value
                            var totalThreadCommentsNumber = 0
                            if (currentThreadList != null) {
                                totalThreadCommentsNumber = currentThreadList.size
                            }
                            Log.d(TAG, "PAGE " + viewThreadQueryStatus.page + " MAX POSITION " + maxThreadNumber + " CUR " + totalThreadCommentsNumber + " " + totalThreadSize)
                            if (totalThreadSize >= maxThreadNumber + 1) {
                                networkStatus.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)
                            } else {
                                networkStatus.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
                            }
                        }

                        // networkStatus.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
                    } else {
                        errorMessageMutableLiveData.postValue(ErrorMessage(
                                getApplication<Application>().getString(R.string.empty_result),
                                getApplication<Application>().getString(R.string.discuz_network_result_null)
                        ))
                        networkStatus.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    }
                } else {
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
        val recommendCall: Call<ApiMessageActionResult>
        recommendCall = if (recommend) {
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
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client!!)
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
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client!!)
        val service = retrofit.create(DiscuzApiService::class.java)
        val formHashValue = formHash.value
        val reportPostCall: Call<ApiMessageActionResult>
        reportPostCall = if (isOtherReason) {
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
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client!!)
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

    public fun favoriteThread(favoriteThread: FavoriteThread, favorite: Boolean, description: String?){
        var favoriteThreadActionResultCall: Call<ApiMessageActionResult>? = null
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val result = threadPostResultMutableLiveData.value
        var error = false
        if (result != null && result.threadPostVariables != null
                && favoriteThread.userId != 0
                && UserPreferenceUtils.syncInformation(getApplication())) {
            if (favorite) {
                favoriteThreadActionResultCall = service.favoriteThreadActionResult(result.threadPostVariables.formHash, favoriteThread!!.idKey, description)
            } else {
                if (favoriteThread.favid == 0) {
                    // just remove it from database
                } else {
                    favoriteThreadActionResultCall = service.unfavoriteThreadActionResult(
                            result.threadPostVariables.formHash,
                            "true",
                            "a_delete_" + favoriteThread!!.favid,
                            favoriteThread.favid)
                }
            }
        }
        dao = FavoriteThreadDatabase.getInstance(getApplication()).dao
        if(favoriteThreadActionResultCall != null){
            favoriteThreadActionResultCall.enqueue(object :Callback<ApiMessageActionResult>{
                override fun onResponse(call: Call<ApiMessageActionResult>, response: Response<ApiMessageActionResult>) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()
                        val key = result?.message?.key
                        Thread{
                            if (favorite && key == "favorite_do_success") {
                                dao.insert(favoriteThread)
                            } else if (!favorite && key == "do_success") {
                                dao.delete(favoriteThread)
                                dao.delete(bbsInfo.id, if (userBriefInfo != null) userBriefInfo!!.getUid() else 0, favoriteThread.idKey, "tid")
                            } else {
                                error = true
                            }
                        }.start()

                    }
                    else {
                        val messageResult = MessageResult().apply {
                            content = getApplication<Application>().getString(R.string.network_failed)
                            key = response.code().toString()
                        }
                        if(favorite){
                            Thread{
                                dao.delete(bbsInfo.id, if (userBriefInfo != null) userBriefInfo!!.getUid() else 0, favoriteThread!!.idKey, "tid")
                                dao.insert(favoriteThread)
                            }.start()

                        }
                        else{
                            // clear potential
                                Thread{
                                    dao.delete(bbsInfo!!.id, if (userBriefInfo != null) userBriefInfo!!.getUid() else 0, favoriteThread!!.idKey, "tid")
                                }.start()

                        }

                    }


                }

                override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {

                }

            })
        }

    }

    init {
        formHash = MutableLiveData("")
        bbsPersonInfoMutableLiveData = MutableLiveData()
        totalPostListLiveData = MutableLiveData()
        newPostList = MutableLiveData(ArrayList())
        pollLiveData = MutableLiveData(null)
        threadStatusMutableLiveData = MutableLiveData()
        errorText = MutableLiveData("")
        detailedThreadInfoMutableLiveData = MutableLiveData()
        threadPostResultMutableLiveData = MutableLiveData()
        dao = FavoriteThreadDatabase.getInstance(application).dao
    }
}