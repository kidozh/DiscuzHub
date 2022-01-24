package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ApiMessageActionResult
import com.kidozh.discuzhub.results.MessageResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class AdminThreadViewModel(application: Application) : AndroidViewModel(application) {

    val adminStatusMutableLiveData: MutableLiveData<AdminStatus> = MutableLiveData(AdminStatus())
    val loadingStatusMutableLiveData : MutableLiveData<Boolean> = MutableLiveData(false)
    val returnedMessage = MutableLiveData<MessageResult?>(null)
    val networkError = MutableLiveData<Boolean>(false)
    val reasonMutableLiveData: MutableLiveData<String> = MutableLiveData<String>("")

    lateinit var discuz: Discuz
    lateinit var user: User
    lateinit var thread: Thread
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService
    var formHash:String = ""
    var fid = 0

    fun initParameter(discuz: Discuz, user: User, fid: Int,thread: Thread, formHash: String){
        this.discuz = discuz
        this.user = user
        this.thread = thread
        this.formHash = formHash
        this.fid = fid
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
    }

    class AdminStatus{
        var operatePin: Boolean = false
        var operateDigest: Boolean = false
        var pinnedLevel : Int = 0
        var digestLevel: Int = 0
        var promote: Boolean = false
    }

    public fun adminThread(){
        // collect operation
        val moderateList: List<Int> = listOf(thread.tid)
        val operationList : MutableList<String> = ArrayList()
        val adminStatus = adminStatusMutableLiveData.value!!
        var digestLevel = 0
        var stickLevel = 0
        if(adminStatus.operatePin){
            operationList.add("stick")
            stickLevel = adminStatus.pinnedLevel
        }
        if(adminStatus.operateDigest){
            operationList.add("digest")
            digestLevel = adminStatus.digestLevel
        }
        loadingStatusMutableLiveData.postValue(true)
        service.adminThread(formHash,fid, moderateList,operationList as List<String>, stickLevel, digestLevel,reasonMutableLiveData.value!!).enqueue(
            object : Callback<ApiMessageActionResult>{
                override fun onResponse(
                    call: Call<ApiMessageActionResult>,
                    response: Response<ApiMessageActionResult>
                ) {
                    val res = response.body()
                    if(response.isSuccessful && res!=null){
                        returnedMessage.postValue(res.message)
                    }
                    else{
                        networkError.postValue(true);
                    }
                    loadingStatusMutableLiveData.postValue(false)
                }

                override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                    networkError.postValue(true)
                    t.printStackTrace()
                    loadingStatusMutableLiveData.postValue(false)
                }

            }
        )
    }
}