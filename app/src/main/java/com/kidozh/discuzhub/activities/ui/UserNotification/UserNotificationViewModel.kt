package com.kidozh.discuzhub.activities.ui.UserNotification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.UserNoteListResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class UserNotificationViewModel(application: Application) : AndroidViewModel(application) {
    var userNoteListResultMutableLiveData: MutableLiveData<UserNoteListResult>
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    var hasLoadedAll = MutableLiveData(false)
    var isError = MutableLiveData(false)
    lateinit var curBBS: Discuz
    private var curUser: User? = null
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService
    fun setBBSInfo(curBBS: Discuz, curUser: User?) {
        this.curBBS = curBBS
        this.curUser = curUser
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), curUser)
        retrofit = NetworkUtils.getRetrofitInstance(curBBS.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
    }

    fun getUserNotificationByPage(view: String?, type: String?, page: Int) {
        if (isLoading.value != null && isLoading.value!!) {
            return
        }
        isLoading.value = true

        service.myNotificationList(view,type, page).enqueue(object : retrofit2.Callback<UserNoteListResult>{
            override fun onResponse(
                call: retrofit2.Call<UserNoteListResult>,
                response: retrofit2.Response<UserNoteListResult>
            ) {
                if(response.isSuccessful && response.body()!= null){
                    userNoteListResultMutableLiveData.postValue(response.body())
                }
                isLoading.postValue(false)
            }

            override fun onFailure(call: retrofit2.Call<UserNoteListResult>, t: Throwable) {
                isError.postValue(true)
                isLoading.postValue(false)
            }

        })
    }

    companion object {
        private val TAG = UserNotificationViewModel::class.java.simpleName
    }

    init {
        userNoteListResultMutableLiveData = MutableLiveData()
        isLoading = MutableLiveData(false)
        hasLoadedAll = MutableLiveData(false)
        isError = MutableLiveData(false)
    }
}

