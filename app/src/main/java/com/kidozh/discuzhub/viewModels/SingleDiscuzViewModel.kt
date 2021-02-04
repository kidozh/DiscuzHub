package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.daos.forumUserBriefInfoDao
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.forumUserBriefInfo

class SingleDiscuzViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var currentBBSMutableLiveData : MutableLiveData<Discuz>
    var currentUserMutableLiveData : MutableLiveData<forumUserBriefInfo?>
    lateinit var userDao: forumUserBriefInfoDao
    lateinit var userListLiveData: LiveData<List<forumUserBriefInfo>>

    init {
        currentUserMutableLiveData = MutableLiveData(null)
    }


    fun setBBSInfo(bbsInfo: Discuz){
        currentBBSMutableLiveData = MutableLiveData(bbsInfo)
        userDao = UserDatabase.getInstance(getApplication()).getforumUserBriefInfoDao()
        userListLiveData = userDao.getAllUserByBBSID(bbsInfo.id)
    }





}