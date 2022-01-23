package com.kidozh.discuzhub.activities.ui.bbsDetailedInformation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kidozh.discuzhub.database.UserDatabase.Companion.getInstance
import com.kidozh.discuzhub.entities.User

class bbsShowInformationViewModel(application: Application) : AndroidViewModel(application) {
    var bbsUserInfoLiveDataList: LiveData<List<User>>? = null
    fun loadUserList(bbs_id: Int) {
        bbsUserInfoLiveDataList = getInstance(getApplication())
            .getforumUserBriefInfoDao()
            .getAllUserByBBSID(bbs_id)
    }
}