package com.kidozh.discuzhub.activities.ui.NewThreads

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.results.DiscuzIndexResult
import com.kidozh.discuzhub.results.NewThreadsResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class NewThreadsViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val TAG = NewThreadsViewModel::class.simpleName
    lateinit var bbsInfo: bbsInformation
    var userBriefInfo: forumUserBriefInfo? = null
    lateinit var client: OkHttpClient
    var errorMessageMutableLiveData: MutableLiveData<ErrorMessage?> = MutableLiveData(null)
    var discuzIndexMutableLiveData: MutableLiveData<DiscuzIndexResult> = MutableLiveData(null)
    var isLoadingMutableLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var loadAllMutableLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var newThreadsResultMutableLiveData: MutableLiveData<NewThreadsResult?> = MutableLiveData(null)
    var newThreadListMutableLiveData: MutableLiveData<List<Thread>> = MutableLiveData(ArrayList())
    var pageMutableLiveData: MutableLiveData<Int> = MutableLiveData(1)
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService

    fun setBBSInfo(bbsInfo: bbsInformation, userBriefInfo: forumUserBriefInfo?) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        URLUtils.setBBS(bbsInfo)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication<Application>(), userBriefInfo)
        retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
        // trigger broswing
        sendAllForumRequest()
    }

    fun sendAllForumRequest(){
        val indexCall = service.indexResult()
        isLoadingMutableLiveData.postValue(true)
        Log.d(TAG,"Send index request "+indexCall.request().url().toString())
        indexCall.enqueue(object : Callback<DiscuzIndexResult> {
            override fun onResponse(call: Call<DiscuzIndexResult>, response: Response<DiscuzIndexResult>) {
                isLoadingMutableLiveData.postValue(false)
                Log.d(TAG,"GET all forum index "+response.body())
                if (response.isSuccessful && response.body() != null) {
                    discuzIndexMutableLiveData.postValue(response.body())
                } else {
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }

            }

            override fun onFailure(call: Call<DiscuzIndexResult>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                isLoadingMutableLiveData.postValue(false)
            }

        })
    }

    fun loadNewThreads(){
        val bbsIndex  = discuzIndexMutableLiveData.value
        Log.d(TAG, "START to get new threads $bbsIndex")
        if(bbsIndex!=null){
            val page = pageMutableLiveData.value as Int
            val start = page * 20
            val forums = bbsIndex.forumVariables.forumList
            Log.d(TAG, "forum list "+forums)
            if(forums != null && forums.size != 0){
                var fidsString = ""

                for (forum in forums){
                    fidsString+= forum.fid.toString()+","
                }
                val call = service.newThreadsResult(fidsString,start)
                Log.d(TAG,"Send new threads request "+call.request().url().toString())
                isLoadingMutableLiveData.postValue(true)
                call.enqueue(object :Callback<NewThreadsResult>{
                    override fun onResponse(call: Call<NewThreadsResult>, response: Response<NewThreadsResult>) {
                        isLoadingMutableLiveData.postValue(false)
                        if(response.isSuccessful && response.body() != null){
                            val newThreadsResult = response.body() as NewThreadsResult
                            newThreadsResultMutableLiveData.postValue(newThreadsResult)
                            if(page == 1){
                                newThreadListMutableLiveData.postValue(newThreadsResult.forumVariables.forumThreadList)
                            }
                            else{
                                val originalThreads = newThreadListMutableLiveData.value?.toMutableList() as MutableList<Thread>
                                originalThreads.addAll(newThreadsResult.forumVariables.forumThreadList)
                                newThreadListMutableLiveData.postValue(originalThreads)
                            }
                            pageMutableLiveData.postValue(page + 1)
                            if(newThreadsResult.forumVariables.forumThreadList.size < 20){
                                loadAllMutableLiveData.postValue(true)
                            }
                        }
                        else{
                            errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                                    getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                        }

                    }

                    override fun onFailure(call: Call<NewThreadsResult>, t: Throwable) {
                        errorMessageMutableLiveData.postValue(ErrorMessage(
                                getApplication<Application>().getString(R.string.discuz_network_failure_template),
                                if (t.localizedMessage == null) t.toString() else t.localizedMessage
                        ))
                        isLoadingMutableLiveData.postValue(false)
                    }

                })
            }


        }
        else{
            sendAllForumRequest()
        }

    }
}