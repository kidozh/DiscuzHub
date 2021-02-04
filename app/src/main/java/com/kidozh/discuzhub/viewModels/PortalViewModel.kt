package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.results.AddCheckResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PortalViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var discuzInDatabase: LiveData<Discuz>
    var checkResultLiveData: MutableLiveData<AddCheckResult?>
    lateinit var client: OkHttpClient
    lateinit var errorMessageLiveData: MutableLiveData<ErrorMessage?>
    val dao = DiscuzDatabase.getInstance(application).forumInformationDao


    init {
        checkResultLiveData = MutableLiveData(null)
        client = OkHttpClient()
        errorMessageLiveData = MutableLiveData(null)

    }

    fun setBaseURL(url:String){
        discuzInDatabase = dao.getBBSInformationLiveDataByBaseURL(url)
    }


    public fun verify(baseURL: String) {
        val retrofit = NetworkUtils.getRetrofitInstance(baseURL, client)
        val service = retrofit.create(DiscuzApiService::class.java)

        val checkCall = service.checkResult
        checkCall.enqueue(object : Callback<AddCheckResult> {


            override fun onResponse(call: Call<AddCheckResult>, response: Response<AddCheckResult>) {
                if (response.isSuccessful) {
                    checkResultLiveData.postValue(response.body())
                }
                else {
                    errorMessageLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }
            }

            override fun onFailure(call: Call<AddCheckResult>, t: Throwable) {

                errorMessageLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
            }
        })
    }


}
