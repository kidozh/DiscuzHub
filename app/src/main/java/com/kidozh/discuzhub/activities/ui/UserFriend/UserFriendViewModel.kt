package com.kidozh.discuzhub.activities.ui.UserFriend

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.UserFriendResult
import com.kidozh.discuzhub.results.UserFriendResult.UserFriend
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.bbsParseUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.*
import java.io.IOException
import kotlin.math.max

class UserFriendViewModel(application: Application) : AndroidViewModel(application) {
    val TAG : String = "UserFriendViewModel"
    var userFriendListMutableData: MutableLiveData<MutableList<UserFriend>?>
    var newFriendListMutableLiveData : MutableLiveData<MutableList<UserFriend>>
    var userFriendResultMutableLiveData: MutableLiveData<UserFriendResult?>
    var isLoadingMutableLiveData : MutableLiveData<Boolean>
    var loadAllMutableLiveData : MutableLiveData<Boolean>
    var errorTextMutableLiveData : MutableLiveData<String>
    var isErrorMutableLiveData : MutableLiveData<Boolean>
    var privacyMutableLiveData : MutableLiveData<Boolean>
    var page = 1
    var user : User? = null
    lateinit var bbsInfo: Discuz
    var client = OkHttpClient()
    var uid = 0
    var friendCounts = 0


    init {
        userFriendListMutableData = MutableLiveData(null)
        userFriendResultMutableLiveData = MutableLiveData(null)
        isLoadingMutableLiveData = MutableLiveData(false)
        loadAllMutableLiveData = MutableLiveData(false)
        errorTextMutableLiveData = MutableLiveData("")
        isErrorMutableLiveData = MutableLiveData(false)
        privacyMutableLiveData = MutableLiveData(false)
        newFriendListMutableLiveData = MutableLiveData(ArrayList())

    }



    fun setInfo(bbsInfo: Discuz, user: User?, uid: Int, friendCounts: Int) {
        this.bbsInfo = bbsInfo
        this.user = user
        this.uid = uid
        this.friendCounts = friendCounts

        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
    }



    fun getFriendInfo(){

        var isLoading: Boolean = isLoadingMutableLiveData.value ?: false
        val hasLoadAll = loadAllMutableLiveData.value ?: false
        val isPrivate = privacyMutableLiveData.value?:false
        if(isLoading || hasLoadAll || isPrivate){
            return
        }
        isLoadingMutableLiveData.postValue(true)
        if(page == 1){
            userFriendListMutableData.postValue(ArrayList())
        }
        val apiStr = URLUtils.getFriendApiUrlByUid(uid, page)
        Log.d(TAG,"Send request to "+apiStr);
        val request = Request.Builder()
                .url(apiStr)
                .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                isErrorMutableLiveData.postValue(true)
                errorTextMutableLiveData.postValue(Application().getString(R.string.network_failed))
                isLoadingMutableLiveData.postValue(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null){
                    isLoadingMutableLiveData.postValue(false)
                    val s = response.body()!!.string()
                    Log.d(TAG,s);
                    val friendsResult = bbsParseUtils.parseUserFriendsResult(s)


                    if(friendsResult?.friendVariables != null){
                        userFriendResultMutableLiveData.postValue(friendsResult)
                        val userFriendList = friendsResult.friendVariables.friendList
                        var currentFriendList = userFriendListMutableData.value

                        if (userFriendList != null && currentFriendList !=null) {
                            currentFriendList.addAll(userFriendList)
                            Log.d(TAG,"get new friend list size "+userFriendList.size);
                            newFriendListMutableLiveData.postValue(userFriendList)
                        }
                        else{
                            currentFriendList = userFriendList?.toMutableList()
                        }
                        userFriendListMutableData.postValue(currentFriendList)
                        isErrorMutableLiveData.postValue(false)
                        // judge load all


                        var currentFriendNum  = currentFriendList?.size ?:0
                        var totalFriendNum = max(friendsResult.friendVariables.count,friendCounts)
                        Log.d(TAG,"total count "+currentFriendList?.size+" required "+totalFriendNum+" "+currentFriendNum);
                        if(totalFriendNum > currentFriendNum ){
                            loadAllMutableLiveData.postValue(false)
                            page += 1
                        }
                        else{
                            loadAllMutableLiveData.postValue(true)
                        }

                        if(totalFriendNum !=0 && currentFriendNum == 0){
                            privacyMutableLiveData.postValue(true)
                        }
                        else{
                            privacyMutableLiveData.postValue(false)

                        }

                        if(friendsResult.isError()){
                            isErrorMutableLiveData.postValue(true)
                            errorTextMutableLiveData.postValue(friendsResult.message?.content)
                        }




                    }
                    else{
                        isErrorMutableLiveData.postValue(true)
                        errorTextMutableLiveData.postValue(getApplication<Application>().getString(R.string.parse_failed))
                    }

                }
                else{
                    isErrorMutableLiveData.postValue(true)
                    errorTextMutableLiveData.postValue(getApplication<Application>().getString(R.string.network_failed))
                }


            }

        })
    }

}