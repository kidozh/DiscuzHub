package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.app.ListActivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

public class ManageBBSViewModel extends AndroidViewModel {
    public LiveData<List<bbsInformation>> bbsInfoList;

    public ManageBBSViewModel(@NonNull Application application) {
        super(application);
        loadBBSList();
    }


    private void loadBBSList(){
        bbsInfoList = BBSInformationDatabase.getInstance(getApplication()).getForumInformationDao().getAllForumInformations();
    }
}
