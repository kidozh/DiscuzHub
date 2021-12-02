package com.kidozh.discuzhub.activities.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.home.HomeViewModel
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.DiscuzIndexResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private var forumCategories: MutableLiveData<List<DiscuzIndexResult.ForumCategory>>? = null
    var errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    var userBriefInfoMutableLiveData: MutableLiveData<User?>
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    var bbsIndexResultMutableLiveData: MutableLiveData<DiscuzIndexResult?> = MutableLiveData(null)
    var bbsInfo: Discuz? = null
    var userBriefInfo: User? = null
    fun setBBSInfo(bbsInfo: Discuz, userBriefInfo: User?) {
        this.bbsInfo = bbsInfo
        this.userBriefInfo = userBriefInfo
        userBriefInfoMutableLiveData = MutableLiveData(userBriefInfo)
    }

    val forumCategoryInfo: LiveData<List<DiscuzIndexResult.ForumCategory>>
        get() {
            if (forumCategories == null) {
                forumCategories = MutableLiveData()
                loadForumCategoryInfo()
            }
            return forumCategories!!
        }

    fun loadForumCategoryInfo() {
        if (!NetworkUtils.isOnline(getApplication())) {
            isLoading.postValue(false)
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()))
            return
        }
        val client =
            NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), userBriefInfo)
        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val bbsIndexResultCall = service.indexResult()
        isLoading.postValue(true)
        bbsIndexResultCall.enqueue(object : Callback<DiscuzIndexResult?> {
            override fun onResponse(
                call: Call<DiscuzIndexResult?>,
                response: Response<DiscuzIndexResult?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val indexResult = response.body()
                    bbsIndexResultMutableLiveData.postValue(indexResult)
                    if (indexResult!!.forumVariables != null) {
                        val serverReturnedUser = indexResult.forumVariables.userBriefInfo
                        userBriefInfoMutableLiveData.postValue(serverReturnedUser)
                        errorMessageMutableLiveData.postValue(null)
                        // prepare to render index page
                        val categoryList = indexResult.forumVariables.forumCategoryList
                        forumCategories!!.postValue(categoryList)
                    } else {
                        if (indexResult.message != null) {
                            errorMessageMutableLiveData.postValue(indexResult.message!!.toErrorMessage())
                        } else if (indexResult.error.length != 0) {
                            errorMessageMutableLiveData.postValue(
                                ErrorMessage(
                                    getApplication<Application>().getString(R.string.discuz_api_error),
                                    indexResult.error
                                )
                            )
                        } else {
                            errorMessageMutableLiveData.postValue(
                                ErrorMessage(
                                    getApplication<Application>().getString(R.string.empty_result),
                                    getApplication<Application>().getString(R.string.discuz_network_result_null)
                                )
                            )
                        }
                    }
                } else {
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
                isLoading.postValue(false)
            }

            override fun onFailure(call: Call<DiscuzIndexResult?>, t: Throwable) {
                errorMessageMutableLiveData.postValue(
                    ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                    )
                )
                isLoading.postValue(false)
            }
        })
    }

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
    }

    init {
        userBriefInfoMutableLiveData = MutableLiveData(null)
    }
}