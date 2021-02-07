package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.PrivateMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ApiMessageActionResult
import com.kidozh.discuzhub.results.PrivateMessageResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.FormBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class PrivateMessageViewModel(application: Application) : AndroidViewModel(application){
    final val TAG = PrivateMessageViewModel::class.simpleName
    var networkState : MutableLiveData<Int> = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    var sendNetworkState : MutableLiveData<Int> = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    var formHashMutableLiveData : MutableLiveData<String?> = MutableLiveData<String?>(null)
    var privateMessageResult = MutableLiveData<PrivateMessageResult?>(null)
    var totalPrivateMessageListMutableLiveData: MutableLiveData<List<PrivateMessage>> = MutableLiveData(ArrayList())
    var pageString : String = ""
    lateinit var discuz: Discuz
    var user: User? = null
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService
    var toUid : Int = 0
    var errorMessageMutableLiveData: MutableLiveData<ErrorMessage?> = MutableLiveData(null)

    fun configure(discuz: Discuz, user: User?, toUid : Int){
        this.discuz = discuz
        this.user = user
        this.toUid = toUid
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
    }



    fun queryPrivateMessage(){
        if(networkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL || networkState.value == ConstUtils.NETWORK_STATUS_LOADING){
            return
        }
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        val call = service.getPrivateMessageListResult(toUid,pageString)
        Log.d(TAG,"Send list request "+call.request().url())
        call.enqueue(object : Callback<PrivateMessageResult>{
            override fun onResponse(call: Call<PrivateMessageResult>, response: Response<PrivateMessageResult>) {
                if(response.isSuccessful && response.body() != null){
                    networkState.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
                    val privateMessageResult = response.body() as PrivateMessageResult
                    formHashMutableLiveData.postValue(privateMessageResult.variables.formHash)
                    val totalPrivateMessages = totalPrivateMessageListMutableLiveData.value
                    val mergedPrivateMessageListLiveData = mutableListOf<PrivateMessage>()

                    if (totalPrivateMessages != null) {
                        mergedPrivateMessageListLiveData.addAll(totalPrivateMessages)
                    }
                    mergedPrivateMessageListLiveData.addAll(privateMessageResult.variables.pmList)

                    totalPrivateMessageListMutableLiveData.postValue(mergedPrivateMessageListLiveData)

                    pageString = (privateMessageResult.variables.page - 1).toString()

                    if (privateMessageResult.variables.page == 1) {
                        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)

                    }


                }
                else{
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                }

            }

            override fun onFailure(call: Call<PrivateMessageResult>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            }

        })
    }

    fun sendPrivateMessage(plid: Int, pmId: Int, message: String){
        if(sendNetworkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL || sendNetworkState.value == ConstUtils.NETWORK_STATUS_LOADING){
            return
        }

        if(formHashMutableLiveData.value!=null){
            sendNetworkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
            val formHash = formHashMutableLiveData.value as String
            val call = service.sendPrivateMessage(plid,pmId,formHash,message)
            Log.d(TAG,"Call request "+call.request().url())
            call.enqueue(object : Callback<ApiMessageActionResult>{
                override fun onResponse(call: Call<ApiMessageActionResult>, response: Response<ApiMessageActionResult>) {
                    if(response.isSuccessful && response.body() != null){
                        val returnedMessage = response.body() as ApiMessageActionResult
                        if(returnedMessage.message!= null){
                            Log.d(TAG,"private message returned "+returnedMessage.message!!.key)
                            if(returnedMessage.message!!.key.equals("do_success")){
                                errorMessageMutableLiveData.postValue(ErrorMessage(returnedMessage.message!!.key, returnedMessage.message!!.content))
                                sendNetworkState.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
                            }
                            else{
                                errorMessageMutableLiveData.postValue(ErrorMessage(returnedMessage.message!!.key, returnedMessage.message!!.content))
                                sendNetworkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                            }
                        }



                    }
                    else{
                        errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                                getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                        sendNetworkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    }

                }

                override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                    t.printStackTrace()

                    errorMessageMutableLiveData.postValue(ErrorMessage(
                            getApplication<Application>().getString(R.string.discuz_network_failure_template),
                            if (t.localizedMessage == null) t.toString() else t.localizedMessage
                    ))
                    sendNetworkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                }

            })

        }

    }
}