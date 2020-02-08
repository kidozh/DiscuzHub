package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kidozh.discuzhub.database.forumInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

public class forumInformationViewModel extends AndroidViewModel {
    private LiveData<List<bbsInformation>> allForumInformation;



    forumInformationViewModel(Application application){
        super(application);
        forumInformationDatabase database = forumInformationDatabase.getInstance(this.getApplication());
        allForumInformation = database.getForumInformationDao().getAllForumInformations();
    }


    public LiveData<List<bbsInformation>> getAllForumInformation() {
        return allForumInformation;
    }
}
