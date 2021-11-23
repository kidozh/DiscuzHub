package com.kidozh.discuzhub.activities.ui.FavoriteForum

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.daos.FavoriteForumDao
import com.kidozh.discuzhub.database.FavoriteForumDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.FavoriteForum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.FavoriteForumResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FavoriteForumViewModel(application: Application) : AndroidViewModel(application) {
    var networkState = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    @JvmField
    var errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    lateinit var favoriteForumCount: LiveData<Int>
    @JvmField
    var totalCount = MutableLiveData(-1)
    @JvmField
    var favoriteForumInServer = MutableLiveData<List<FavoriteForum>?>(ArrayList())
    @JvmField
    var newFavoriteForum = MutableLiveData<List<FavoriteForum>>(ArrayList())
    @JvmField
    var resultMutableLiveData = MutableLiveData<FavoriteForumResult?>()
    private var client: OkHttpClient? = null
    var bbsInfo: Discuz? = null
    var userBriefInfo: User? = null
    var idType: String? = null
    lateinit var dao: FavoriteForumDao
    var favoritePagingConfig : PagingConfig = PagingConfig(pageSize = 5)
//    var favoriteItem = Pager(
//        config = favoritePagingConfig,
//        remoteMediator = FavoriteForumRemoteMediator(0,DiscuzApiService(userBriefInfo), database = dao.allFavoriteForumDataSource)
//    )
//    var favoriteItem = Pager(favoritePagingConfig){
//        dao.getFavoriteItemPageListByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0)
//    }.flow
    lateinit var flow : Flow<PagingData<FavoriteForum>>


    fun setInfo(bbsInfo: Discuz, userBriefInfo: User?) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), userBriefInfo)
        var userId = 0
        if(userBriefInfo != null){
            userId = userBriefInfo.uid;
        }
        flow = Pager(favoritePagingConfig){
            dao.getFavoriteItemPagingListByBBSId(bbsInfo.id,userId)
        }.flow


        flow.cachedIn(viewModelScope)

        favoriteForumCount = dao.getFavoriteItemCountLiveData(bbsInfo.id,userId)
    }

    fun startSyncFavoriteForum() {
        getFavoriteItem(1)
    }

    private fun getFavoriteItem(page: Int) {
        if (!NetworkUtils.isOnline(getApplication())) {
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()))
            return
        }
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client!!)
        val apiService = retrofit.create(DiscuzApiService::class.java)
        val favoriteCall: Call<FavoriteForumResult>
        favoriteCall = apiService.getFavoriteForumResult(page)
        Log.d(TAG, "Get favorite result " + favoriteCall.request().url())
        favoriteCall.enqueue(object : Callback<FavoriteForumResult?> {
            override fun onResponse(
                call: Call<FavoriteForumResult?>,
                response: Response<FavoriteForumResult?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()
                    resultMutableLiveData.postValue(result)
                    if (result!!.isError()) {
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                        errorMessageMutableLiveData.postValue(
                            ErrorMessage(
                                result.errorMessage!!.key,
                                result.errorMessage!!.content
                            )
                        )
                    } else if (result.favoriteForumVariable != null) {
                        totalCount.postValue(result.favoriteForumVariable.count)
                        Log.d(
                            TAG,
                            "Get cnt " + result.favoriteForumVariable.count + " " + result.favoriteForumVariable.FavoriteForumList
                        )
                        newFavoriteForum.postValue(result.favoriteForumVariable.FavoriteForumList)
                        val curFavoriteForumList: MutableList<FavoriteForum> =
                            if (favoriteForumInServer.value == null) ArrayList() else favoriteForumInServer.getValue() as MutableList<FavoriteForum>
                        curFavoriteForumList.addAll(result.favoriteForumVariable.FavoriteForumList)
                        favoriteForumInServer.postValue(curFavoriteForumList)

                        // recursive
                        if (result.favoriteForumVariable.count > curFavoriteForumList.size) {
                            getFavoriteItem(page + 1)
                        }
                    }
                } else {
                    Log.d(TAG, "Get favorite response failed" + response.body())
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    errorMessageMutableLiveData.postValue(
                        ErrorMessage(
                            response.code().toString(),
                            getApplication<Application>().getString(
                                R.string.discuz_network_unsuccessful,
                                response.message()
                            )
                        )
                    )
                }
            }

            override fun onFailure(call: Call<FavoriteForumResult?>, t: Throwable) {
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                errorMessageMutableLiveData.postValue(
                    ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                    )
                )
                t.printStackTrace()
            }
        })
    }

    companion object {
        private val TAG = FavoriteForumViewModel::class.java.simpleName
    }

    init {
        dao = FavoriteForumDatabase.getInstance(application).dao
    }
}