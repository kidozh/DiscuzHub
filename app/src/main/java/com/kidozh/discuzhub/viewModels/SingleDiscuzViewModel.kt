package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.activities.DrawerActivity.EmptyViewPagerAdapter
import com.kidozh.discuzhub.daos.forumUserBriefInfoDao
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase
import com.kidozh.discuzhub.entities.bbsInformation
import com.kidozh.discuzhub.entities.forumUserBriefInfo

class SingleDiscuzViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var currentBBSMutableLiveData : MutableLiveData<bbsInformation>
    var currentUserMutableLiveData : MutableLiveData<forumUserBriefInfo?>
    lateinit var userDao: forumUserBriefInfoDao
    lateinit var userListLiveData: LiveData<List<forumUserBriefInfo>>

    init {
        currentUserMutableLiveData = MutableLiveData(null)
    }


    fun setBBSInfo(bbsInfo: bbsInformation){
        currentBBSMutableLiveData = MutableLiveData(bbsInfo)
        userDao = forumUserBriefInfoDatabase.getInstance(getApplication()).getforumUserBriefInfoDao()
        userListLiveData = userDao.getAllUserByBBSID(bbsInfo.id)
    }





}