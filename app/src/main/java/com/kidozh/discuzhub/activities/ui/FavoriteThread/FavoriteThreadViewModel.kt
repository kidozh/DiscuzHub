package com.kidozh.discuzhub.activities.ui.FavoriteThread

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.FavoriteThread.FavoriteThreadViewModel
import com.kidozh.discuzhub.daos.FavoriteThreadDao
import com.kidozh.discuzhub.database.FavoriteThreadDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.FavoriteThread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.FavoriteThreadResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FavoriteThreadViewModel(application: Application) : AndroidViewModel(application) {
    var networkState = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    var errorMsgKey = MutableLiveData("")
    var errorMsgContent = MutableLiveData("")
    var totalCount = MutableLiveData(-1)
    var favoriteThreadInServer = MutableLiveData<List<FavoriteThread>>(ArrayList())
    var newFavoriteThread = MutableLiveData<List<FavoriteThread>>(ArrayList())
    var resultMutableLiveData = MutableLiveData<FavoriteThreadResult?>()
    private var client: OkHttpClient? = null
    var bbsInfo: Discuz? = null
    var userBriefInfo: User? = null
    var idType: String? = null
    var dao: FavoriteThreadDao = FavoriteThreadDatabase.getInstance(application).dao

    private var favoritePagingConfig : PagingConfig = PagingConfig(pageSize = 5)
    lateinit var flow : Flow<PagingData<FavoriteThread>>

    fun setInfo(bbsInfo: Discuz, userBriefInfo: User?, idType: String?) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        this.idType = idType
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), userBriefInfo)
        flow = Pager(favoritePagingConfig){
            dao.getFavoriteItemPageListByBBSId(
                bbsInfo.id,
                userBriefInfo?.uid ?: 0,
                idType
            )
        }.flow

    }

    fun startSyncFavoriteThread() {
        getFavoriteItem(1)
    }

    private fun getFavoriteItem(page: Int) {
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client!!)
        val apiService = retrofit.create(DiscuzApiService::class.java)
        val favoriteCall: Call<FavoriteThreadResult> = apiService.getFavoriteThreadResult(page)
        Log.d(TAG, "Get favorite result " + favoriteCall.request().url)
        favoriteCall.enqueue(object : Callback<FavoriteThreadResult?> {
            override fun onResponse(
                call: Call<FavoriteThreadResult?>,
                response: Response<FavoriteThreadResult?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()
                    resultMutableLiveData.postValue(result)
                    Log.d(
                        TAG,
                        "Get response result " + result!!.isError() + response.raw().toString()
                    )
                    if (result.isError()) {
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                        errorMsgKey.postValue(result.errorMessage!!.key)
                        errorMsgContent.postValue(result.errorMessage!!.content)
                    } else if (result.favoriteThreadVariable != null) {
                        totalCount.postValue(result.favoriteThreadVariable.count)
                        //Log.d(TAG,"Get cnt "+result.favoriteThreadVariable.count + " "+result.favoriteThreadVariable.favoriteThreadList);
                        newFavoriteThread.postValue(result.favoriteThreadVariable.favoriteThreadList)
                        val curFavoriteThreadList: MutableList<FavoriteThread> =
                            if (favoriteThreadInServer.value == null) ArrayList() else favoriteThreadInServer.value as MutableList<FavoriteThread>
                        curFavoriteThreadList.addAll(result.favoriteThreadVariable.favoriteThreadList)
                        favoriteThreadInServer.postValue(curFavoriteThreadList)

                        // recursive
                        if (result.favoriteThreadVariable.count > curFavoriteThreadList.size) {
                            getFavoriteItem(page + 1)
                        }
                    }
                } else {
                    Log.d(TAG, "Get favorite response failed" + response.body())
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    errorMsgContent.postValue(getApplication<Application>().getString(R.string.network_failed))
                }
            }

            override fun onFailure(call: Call<FavoriteThreadResult?>, t: Throwable) {
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                errorMsgContent.postValue(getApplication<Application>().getString(R.string.network_failed))
                t.printStackTrace()
            }
        })
    }

    companion object {
        private val TAG = FavoriteThreadViewModel::class.java.simpleName
    }

}