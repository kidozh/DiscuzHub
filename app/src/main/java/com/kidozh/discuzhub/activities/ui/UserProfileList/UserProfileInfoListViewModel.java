package com.kidozh.discuzhub.activities.ui.UserProfileList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.entities.UserProfileItem;

import java.util.List;

public class UserProfileInfoListViewModel extends AndroidViewModel {
    // TODO: Implement the ViewModel
    public MutableLiveData<List<UserProfileItem>> userProfileListMutableLiveData;
    public MutableLiveData<String> titleMutableLivedata;

    public UserProfileInfoListViewModel(@NonNull Application application) {
        super(application);
        userProfileListMutableLiveData = new MutableLiveData<>();
        titleMutableLivedata = new MutableLiveData<>();
    }


}
