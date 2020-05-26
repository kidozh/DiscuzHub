package com.kidozh.discuzhub.activities.ui.UserGroup;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.results.UserProfileResult;


public class UserGroupInfoViewModel extends ViewModel {
    public void setGroupVariables(UserProfileResult.GroupVariables groupVariablesMutableLiveData) {
        this.groupVariablesMutableLiveData.setValue(groupVariablesMutableLiveData);
    }

    public void setAdminGroupVariables(UserProfileResult.AdminGroupVariables adminGroupVariablesMutableLiveData) {
        this.adminGroupVariablesMutableLiveData.setValue(adminGroupVariablesMutableLiveData);
    }

    // TODO: Implement the ViewModel
    public MutableLiveData<UserProfileResult.GroupVariables> groupVariablesMutableLiveData;
    public MutableLiveData<UserProfileResult.AdminGroupVariables> adminGroupVariablesMutableLiveData;

    public UserGroupInfoViewModel(){
        groupVariablesMutableLiveData = new MutableLiveData<>();
        adminGroupVariablesMutableLiveData = new MutableLiveData<>();
    }

}
