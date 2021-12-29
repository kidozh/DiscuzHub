package com.kidozh.discuzhub.activities.ui.userThreads

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
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class UserThreadViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = UserThreadViewModel::class.simpleName
    val globalPageMutableLiveData : MutableLiveData<Int> = MutableLiveData(1)
    private var user: User? = null
    lateinit var bbsInfo: Discuz
    val networkState : MutableLiveData<Int> = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    val errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    val totalThreadList = MutableLiveData<List<Thread>>(ArrayList())
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService

    fun configureInfo(bbsInfo: Discuz, user: User?){
        this.user = user
        this.bbsInfo = bbsInfo
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
    }

    fun getNextThread(){
        if(networkState.value == ConstUtils.NETWORK_STATUS_LOADING || networkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL){
            return
        }
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        val page = globalPageMutableLiveData.value as Int
        val call = service.myThreadResult(page)
        call.enqueue(object : Callback<DisplayThreadsResult> {
            override fun onResponse(call: Call<DisplayThreadsResult>, response: Response<DisplayThreadsResult>) {
                networkState.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body() as DisplayThreadsResult
                    val newThreadList = result.forumVariables.forumThreadList
                    val oldThreadList = totalThreadList.value
                    //Log.d(TAG,"Get user thread size "+newThreadList.size)
                    if(newThreadList!= null && newThreadList.isEmpty()){
                        errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                                getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                    }
                    else{
                        val totalList: MutableList<Thread> = ArrayList()
                        if (oldThreadList != null) {
                            totalList.addAll(oldThreadList)
                        }
                        if (newThreadList != null) {
                            totalList.addAll(newThreadList)
                        }
                        Log.d(TAG,"Get total user thread size "+totalList.size)
                        totalThreadList.postValue(totalList)
                    }

                    // compare it
                    val pageSize = result.forumVariables.perPage
                    if(newThreadList == null || newThreadList.size < pageSize){
                        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)
                    }
                    else{
                        globalPageMutableLiveData.postValue(page + 1)
                    }
                }
                else {
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: Call<DisplayThreadsResult>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            }

        })
    }


}