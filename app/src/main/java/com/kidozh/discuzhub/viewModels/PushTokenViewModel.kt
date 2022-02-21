package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.PostTokenResult
import com.kidozh.discuzhub.results.TokenResult
import com.kidozh.discuzhub.services.DHPushServices
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class PushTokenViewModel(application: Application) : AndroidViewModel(application) {
    val errorMessage: MutableLiveData<ErrorMessage?> = MutableLiveData(null)
    val pushChannel: MutableLiveData<String> = MutableLiveData("")
    val tokenResult: MutableLiveData<TokenResult?> = MutableLiveData(null)
    val loading : MutableLiveData<Boolean> = MutableLiveData(false)
    var token: MutableLiveData<String> = MutableLiveData("")
    var channel: String = ""
    val postTokenResult: MutableLiveData<PostTokenResult?> = MutableLiveData(null)
    var formHash = ""
    lateinit var discuz: Discuz
    lateinit var user: User
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DHPushServices

    fun loadDiscuzInfo(discuz: Discuz, user: User){
        this.discuz = discuz
        this.user = user
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client)
        service = retrofit.create(DHPushServices::class.java)
        loadTokenListFromServer()
    }

    fun loadTokenListFromServer(){
        loading.postValue(true)
        service.tokenList().enqueue(object : Callback<TokenResult?>{
            override fun onResponse(call: Call<TokenResult?>, response: Response<TokenResult?>) {
                if(response.isSuccessful && response.body()!=null){

                    tokenResult.postValue(response.body())
                    errorMessage.postValue(null)

                }
                else{
                    errorMessage.postValue(ErrorMessage("network_failed",getApplication<Application>().getString(R.string.dhpush_connect_failed)))
                }
                loading.postValue(false)
            }

            override fun onFailure(call: Call<TokenResult?>, t: Throwable) {
                loading.postValue(false)
                errorMessage.postValue(ErrorMessage("api_failed",getApplication<Application>().getString(R.string.dhpush_not_supported)))
            }

        })
    }

    fun sendTokenToServer(token: String, deviceName:String,packageId: String, pushChannel: String){
        service.sendToken(formHash, token, deviceName, packageId, pushChannel).enqueue(object : Callback<PostTokenResult>{
            override fun onResponse(
                call: Call<PostTokenResult>,
                response: Response<PostTokenResult>
            ) {
                if(response.isSuccessful && response.body()!=null){

                    postTokenResult.postValue(response.body())
                    errorMessage.postValue(null)

                }
                else{
                    errorMessage.postValue(ErrorMessage("network_failed",getApplication<Application>().getString(R.string.dhpush_connect_failed)))
                }
                loading.postValue(false)
            }

            override fun onFailure(call: Call<PostTokenResult>, t: Throwable) {
                loading.postValue(false)
                errorMessage.postValue(ErrorMessage("send_token_failed",getApplication<Application>().getString(R.string.dhpush_not_supported)))
            }

        })
    }

}