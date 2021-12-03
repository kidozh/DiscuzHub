package com.kidozh.discuzhub.activities.ui.HotThreads

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.DisplayThreadsResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HotThreadsViewModel(application: Application?) : AndroidViewModel(application!!) {
    private val TAG = HotThreadsViewModel::class.java.simpleName
    private val mText: MutableLiveData<String>? = null
    lateinit var bbsInfo: Discuz
    var userBriefInfo: User? = null
    private var client = OkHttpClient()
    var pageNum = MutableLiveData(1)
    var isLoading: MutableLiveData<Boolean>
    var totalThreadListLiveData: MutableLiveData<List<Thread>> = MutableLiveData(ArrayList())
    var errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    var resultMutableLiveData = MutableLiveData<DisplayThreadsResult?>()
    fun setBBSInfo(bbsInfo: Discuz, userBriefInfo: User?) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        URLUtils.setBBS(bbsInfo)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), userBriefInfo)
    }



    fun setPageNumAndFetchThread(page: Int) {
        pageNum.value = page
        getThreadList(page)
    }

    private fun getThreadList(page: Int) {
        // init page
        if (!NetworkUtils.isOnline(getApplication())) {
            isLoading.postValue(false)
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()))
            return
        }
        isLoading.postValue(true)
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val displayThreadsResultCall = service.hotThreadResult(page)
        Log.d(TAG, "Get hot thread page " + page + " url ${displayThreadsResultCall.request()}")
        displayThreadsResultCall.enqueue(object : Callback<DisplayThreadsResult?> {
            override fun onResponse(call: Call<DisplayThreadsResult?>, response: Response<DisplayThreadsResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    isLoading.postValue(false)
                    val threadsResult = response.body()
                    resultMutableLiveData.postValue(threadsResult)
                    val totalMutableThreadList: MutableList<Thread> = ArrayList()
                    if (threadsResult!!.forumVariables != null) {
                        val threads = threadsResult.forumVariables.forumThreadList
                        if(totalThreadListLiveData.value != null){
                            totalMutableThreadList.addAll(totalThreadListLiveData.value!!)
                        }
                        if (threads != null) {
                            totalMutableThreadList.addAll(threads)

                        }
                        totalThreadListLiveData.postValue(totalMutableThreadList)
                        errorMessageMutableLiveData.postValue(null)
                    } else {
                        if (threadsResult.message != null) {
                            errorMessageMutableLiveData.postValue(threadsResult.message!!.toErrorMessage())
                        } else if (threadsResult.error.length != 0) {
                            errorMessageMutableLiveData.postValue(ErrorMessage(
                                    getApplication<Application>().getString(R.string.discuz_api_error),
                                    threadsResult.error
                            ))
                        } else {
                            errorMessageMutableLiveData.postValue(ErrorMessage(
                                    getApplication<Application>().getString(R.string.empty_result),
                                    getApplication<Application>().getString(R.string.discuz_network_result_null)
                            ))
                        }
                        if (page != 1) {
                            // not at initial state
                            pageNum.postValue(if (pageNum.value == null) 1 else pageNum.value!! - 1)
                        }
                    }

                } else {
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: Call<DisplayThreadsResult?>, t: Throwable) {
                isLoading.postValue(false)
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
            }
        })
    }

    init {
        isLoading = MutableLiveData()
        isLoading.postValue(false)
    }
}