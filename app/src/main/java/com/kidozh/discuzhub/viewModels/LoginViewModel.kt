package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.forumUserBriefInfo
import com.kidozh.discuzhub.results.LoginResult
import com.kidozh.discuzhub.results.SecureInfoResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = LoginViewModel::class.simpleName
    var secureInfoResultMutableLiveData = MutableLiveData<SecureInfoResult?>(null)
    var loginResultMutableLiveData : MutableLiveData<LoginResult?> = MutableLiveData(null)
    var errorMessage: MutableLiveData<ErrorMessage?> = MutableLiveData(null)
    lateinit var bbsInfo: Discuz
    lateinit var client: OkHttpClient
    var userBriefInfo: forumUserBriefInfo? = null
    lateinit var retrofit:Retrofit
    lateinit var service: DiscuzApiService


    public fun setInfo(bbsInfo: Discuz, userBriefInfo: forumUserBriefInfo?, client: OkHttpClient){
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        this.client = client
        retrofit  = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
        loadSecureInfo()

    }

    fun loadSecureInfo(){
        val call = service.secureResult("login")
        call.enqueue(object : retrofit2.Callback<SecureInfoResult> {
            override fun onResponse(call: retrofit2.Call<SecureInfoResult>, response: retrofit2.Response<SecureInfoResult>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    secureInfoResultMutableLiveData.postValue(result)
                } else {
                    errorMessage.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: retrofit2.Call<SecureInfoResult>, t: Throwable) {
                errorMessage.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
            }

        })
    }

    fun login(client: OkHttpClient,
              account:String, password:String,
              securityQuestionId: Int, securityAnswer: String,
              securityHash:String?,  captcha:String){
        retrofit  = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
        val options:HashMap<String,String> = hashMapOf(
                "loginfield" to "username","username" to account,
                "password" to password, "loginsubmit" to "yes",
                "cookietime" to "2592000"

        )
        if(securityQuestionId !=0){
            options.put("questionid",securityQuestionId.toString())
            options.put("answer",securityAnswer)
        }

        if(securityHash != null){
            options.put("seccodehash",securityHash)
            options.put("seccodemodid", "member::logging")
            options.put("seccodeverify",captcha)
        }

        val loginCall = service.loginCall(options)
        Log.d(TAG,"login call "+loginCall.request().url()+" "+loginCall.request().body().toString())
        loginCall.enqueue(object :Callback<LoginResult>{
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                Log.d(TAG,"GET JSON "+response.body().toString())
                if (response.isSuccessful) {
                    val result = response.body()
                    loginResultMutableLiveData.postValue(result)
                } else {
                    errorMessage.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                errorMessage.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
            }

        })
    }
}