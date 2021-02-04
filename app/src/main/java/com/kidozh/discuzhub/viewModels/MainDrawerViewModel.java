package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.kidozh.discuzhub.daos.DiscuzDao;
import com.kidozh.discuzhub.database.DiscuzDatabase;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.List;

public class MainDrawerViewModel extends AndroidViewModel {
    private static final String TAG = MainDrawerViewModel.class.getSimpleName();
    public MutableLiveData<Discuz> currentBBSInformationMutableLiveData =
            new MutableLiveData<>(null);
    public LiveData<List<Discuz>> allBBSInformationMutableLiveData;
    public MutableLiveData<forumUserBriefInfo> currentForumUserBriefInfoMutableLiveData =
            new MutableLiveData<>(null);
    public MutableLiveData<List<forumUserBriefInfo>> forumUserListMutableLiveData =
            new MutableLiveData<>(null);

    private DiscuzDao DiscuzDao;

    public MainDrawerViewModel(@NonNull Application application) {
        super(application);
        DiscuzDao = DiscuzDatabase.getInstance(getApplication()).getForumInformationDao();
        allBBSInformationMutableLiveData = DiscuzDao.getAllForumInformations();

    }

    public void setCurrentBBSById(LifecycleOwner lifecycleOwner, int bbsId){
        LiveData<Discuz> selectedBBSInfo= DiscuzDao.getForumInformationLiveDataById(bbsId);
        selectedBBSInfo.observe(lifecycleOwner, new Observer<Discuz>() {
            @Override
            public void onChanged(Discuz Discuz) {
                currentBBSInformationMutableLiveData.postValue(Discuz);
            }
        });


    }

}
