package com.kidozh.discuzhub.activities.ui.bbsDetailedInformation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.List;

public class bbsShowInformationViewModel extends ViewModel {
    public LiveData<List<forumUserBriefInfo>> bbsUserInfoLiveDataList;

    public LiveData<List<forumUserBriefInfo>> getBbsUserInfoLiveDataList() {
        return bbsUserInfoLiveDataList;
    }

    public void setBbsUserInfoLiveDataList(LiveData<List<forumUserBriefInfo>> bbsUserInfoLiveDataList) {
        this.bbsUserInfoLiveDataList = bbsUserInfoLiveDataList;
    }
}
