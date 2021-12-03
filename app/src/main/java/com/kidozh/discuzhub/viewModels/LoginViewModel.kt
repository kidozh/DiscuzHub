package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.LoginResult
import com.kidozh.discuzhub.results.SecureInfoResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
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
    var user: User? = null
    lateinit var retrofit:Retrofit
    lateinit var service: DiscuzApiService


    fun setInfo(bbsInfo: Discuz, user: User?, client: OkHttpClient){
        this.bbsInfo = bbsInfo
        this.user = user
        this.client = client
        retrofit  = NetworkUtils.getRetrofitInstance(bbsInfo.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
        loadSecureInfo()

    }

    fun loadSecureInfo(){
        val call = service.secureResult("login")
        call.enqueue(object : Callback<SecureInfoResult> {
            override fun onResponse(call: Call<SecureInfoResult>, response: Response<SecureInfoResult>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    secureInfoResultMutableLiveData.postValue(result)
                } else {
                    errorMessage.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: Call<SecureInfoResult>, t: Throwable) {
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
                "loginfield" to "username",
                "username" to account,
                "password" to password,
                "loginsubmit" to "yes",
                "cookietime" to "2592000"

        )
        if(securityQuestionId !=0){
            options["questionid"] = securityQuestionId.toString()
            options["answer"] = securityAnswer
        }

        if(securityHash != null){
            options["seccodehash"] = securityHash
            options["seccodemodid"] = "member::logging"
            options["seccodeverify"] = captcha
        }

        val loginCall = service.loginCall(options)
        Log.d(TAG,"login call ${loginCall.request()}")
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