package com.kidozh.discuzhub.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.FavoriteForumDatabase
import com.kidozh.discuzhub.database.ThreadDraftDatabase
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.results.ForumResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.collapseForumRule
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class ForumViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = ForumViewModel::class.java.simpleName

    lateinit var forumStatusMutableLiveData: MutableLiveData<DisplayForumQueryStatus>
    lateinit var discuz: Discuz
    var user: User? = null
    lateinit var forum: Forum
    lateinit var client: OkHttpClient
    @JvmField
    var networkState = MutableLiveData(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)
    @JvmField
    val totalThreadListMutableLiveData: MutableLiveData<List<Thread>> = MutableLiveData<List<Thread>>(ArrayList())
    private var newThreadListMutableLiveData: MutableLiveData<List<Thread>?>? = null
    var draftNumberLiveData: LiveData<Int>
    @JvmField
    var forumDetailedInfoMutableLiveData: MutableLiveData<Forum>
    @JvmField
    var displayForumResultMutableLiveData: MutableLiveData<ForumResult?>
    @JvmField
    var favoriteForumLiveData: LiveData<FavoriteForum>? = null
    @JvmField
    var ruleTextCollapse = MutableLiveData(true)
    @JvmField
    var errorMessageMutableLiveData = MutableLiveData<ErrorMessage?>(null)
    @JvmField
    var loadAllNoticeOnce = MutableLiveData(false)
    fun getNewThreadListMutableLiveData(): MutableLiveData<List<Thread>?> {
        if (newThreadListMutableLiveData == null) {
            newThreadListMutableLiveData = MutableLiveData(ArrayList())
            val displayForumQueryStatus = DisplayForumQueryStatus(forum.fid, 1)
            setForumStatusAndFetchThread(displayForumQueryStatus)
        }
        return newThreadListMutableLiveData!!
    }

    fun setBBSInfo(discuz: Discuz, user: User?, forum: Forum) {
        this.discuz = discuz
        this.user = user
        this.forum = forum
        URLUtils.setBBS(discuz)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        favoriteForumLiveData = FavoriteForumDatabase.getInstance(getApplication())
                .dao
                .getFavoriteItemByfid(discuz.id, user?.getUid() ?: 0, forum.fid)
        val uid = user?.getUid() ?: 0
        Log.d(TAG, "Get favorite form info " + user + " fid " + forum.fid + " uid " + uid)
        forumStatusMutableLiveData = MutableLiveData(DisplayForumQueryStatus(forum.fid,1))
    }

    fun toggleRuleCollapseStatus() {
        ruleTextCollapse.postValue(!ruleTextCollapse.value!!)
    }

    fun setForumStatusAndFetchThread(displayForumQueryStatus: DisplayForumQueryStatus) {
        forumStatusMutableLiveData.postValue(displayForumQueryStatus)
        getNextThreadList()
    }

    fun getNextThreadList() {
        val displayForumQueryStatus: DisplayForumQueryStatus = forumStatusMutableLiveData.value as DisplayForumQueryStatus
        // check with network operation
        if (!NetworkUtils.isOnline(getApplication())) {
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()))
            networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            newThreadListMutableLiveData!!.postValue(ArrayList())
            return
        }

        // network state
        val loading = networkState.value == ConstUtils.NETWORK_STATUS_LOADING
        var loadAll = networkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL
        if (displayForumQueryStatus.page == 1) {
            loadAll = false
            loadAllNoticeOnce.postValue(false)
            totalThreadListMutableLiveData.postValue(ArrayList())
        }
        if (loading || loadAll) {
            return
        }
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING)
        val retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client!!)
        val service = retrofit.create(DiscuzApiService::class.java)
        val forumResultCall = service.forumDisplayResult(displayForumQueryStatus.generateQueryHashMap())
        Log.d(TAG, "Browse page " + displayForumQueryStatus.page + " url " + forumResultCall.request().url().toString())
        forumResultCall.enqueue(object : Callback<ForumResult?> {
            override fun onResponse(call: Call<ForumResult?>, response: Response<ForumResult?>) {
                // clear status if page == 1
                if (response.isSuccessful && response.body() != null) {
                    val forumResult = response.body()
                    displayForumResultMutableLiveData.postValue(forumResult)
                    Log.d(TAG, "Get forum thread size " + forumResult!!.forumVariables.forumThreadList.size)

                    // for list display
                    val threadList = forumResult.forumVariables.forumThreadList
                    val totalThreadList = totalThreadListMutableLiveData.value
                    if(threadList.isEmpty()){
                        // network failure
                        errorMessageMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.empty_result),
                                getApplication<Application>().getString(R.string.empty_hot_threads), R.drawable.ic_empty_hot_thread_64px
                        ))
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                    }
                    else{
                        // valid response
                        val totalMutableList: MutableList<Thread> = ArrayList()
                        if(totalThreadList != null){
                            totalMutableList.addAll(totalThreadList)
                        }
                        totalMutableList.addAll(threadList)
                        totalThreadListMutableLiveData.postValue(totalMutableList)

                        // check with load all status
                        val totalThreadNumber = forumResult.forumVariables.forum.threadCount
                        if(totalMutableList.size > totalThreadNumber){
                            networkState.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL)
                        }
                        else{
                            // need to point to next
                            displayForumQueryStatus.page += 1
                            forumStatusMutableLiveData.postValue(displayForumQueryStatus)
                        }
                    }
                    networkState.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY)

                    // check with api message
                    if (forumResult.message != null) {
                        errorMessageMutableLiveData.postValue(forumResult.message!!.toErrorMessage())
                    }

                }
                else {
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
                }
            }

            override fun onFailure(call: Call<ForumResult?>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED)
            }
        })
    }

    init {
        draftNumberLiveData = ThreadDraftDatabase
                .getInstance(getApplication())
                .getbbsThreadDraftDao()
                .draftNumber
        forumDetailedInfoMutableLiveData = MutableLiveData()
        displayForumResultMutableLiveData = MutableLiveData(null)
        ruleTextCollapse = MutableLiveData(collapseForumRule(application))
        ruleTextCollapse.postValue(collapseForumRule(application))
    }
}