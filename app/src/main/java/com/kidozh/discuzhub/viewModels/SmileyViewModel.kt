package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.daos.SmileyDao
import com.kidozh.discuzhub.database.SmileyDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.SmileyResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class SmileyViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = SmileyViewModel::class.simpleName
    var dao : SmileyDao
    lateinit var latestSmileyListLiveData: LiveData<List<Smiley>>
    var errorMessage: MutableLiveData<ErrorMessage?> = MutableLiveData(null)

    lateinit var discuz : Discuz
    lateinit var service: DiscuzApiService
    lateinit var retrofit: Retrofit
    lateinit var client: OkHttpClient

    init {
        dao = SmileyDatabase.getInstance(application).getDao()
    }

    fun configureDiscuz(discuz: Discuz, user: User?){
        this.discuz = discuz
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        latestSmileyListLiveData = dao.latestSimleys(discuz.id)
        val retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)

    }

    var smileyResultLiveData : MutableLiveData<SmileyResult?> = MutableLiveData(null)

    fun getSmileyList(){
        if(smileyResultLiveData.value != null){
            return
        }
        Log.d(TAG,"Smiley list init call ")
        val smileyCall = service.smileyResult
        smileyCall.enqueue(object : Callback<SmileyResult>{
            override fun onResponse(call: Call<SmileyResult>, response: Response<SmileyResult>) {
                if(response.isSuccessful && response.body()!= null){
                    Log.d(TAG,"Get smiley successful ")
                    smileyResultLiveData.postValue(response.body())

                }
                else{
                    Log.d(TAG,"Get null")
                    errorMessage.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful,
                                    response.message()
                            )
                    ))
                }
            }

            override fun onFailure(call: Call<SmileyResult>, t: Throwable) {
                Log.d(TAG,"Get smiley failed "+t.localizedMessage)
                errorMessage.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
            }

        })
    }

}