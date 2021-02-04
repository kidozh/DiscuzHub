package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.database.DiscuzDatabase;
import com.kidozh.discuzhub.entities.Discuz;

import java.util.List;

public class LocalBBSViewModel extends AndroidViewModel {

    private LiveData<List<Discuz>> forumInformationList;

    public LocalBBSViewModel(Application application){
        super(application);

        forumInformationList = DiscuzDatabase
                .getInstance(this.getApplication())
                .getForumInformationDao()
                .getAllForumInformations();
    }

    public LiveData<List<Discuz>> getBBSInformation(){
        if(forumInformationList == null){
            forumInformationList = new MutableLiveData<List<Discuz>>();
            loadBBSInformation();
        }
        return forumInformationList;
    }

    public void loadBBSInformation(){

    }

}
