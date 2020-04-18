package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.database.forumInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

public class LocalBBSViewModel extends AndroidViewModel {

    private LiveData<List<bbsInformation>> forumInformationList;

    public LocalBBSViewModel(Application application){
        super(application);

        forumInformationList = forumInformationDatabase
                .getInstance(this.getApplication())
                .getForumInformationDao()
                .getAllForumInformations();
    }

    public LiveData<List<bbsInformation>> getBBSInformation(){
        if(forumInformationList == null){
            forumInformationList = new MutableLiveData<List<bbsInformation>>();
            loadBBSInformation();
        }
        return forumInformationList;
    }

    public void loadBBSInformation(){

    }

}
