package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.daos.DiscuzDao;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;

public class ManageBBSViewModel extends AndroidViewModel {

    private LiveData<PagedList<bbsInformation>> pagedListLiveData;
    private DiscuzDao DiscuzDao;
    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(10)
            .build();

    public ManageBBSViewModel(@NonNull Application application) {
        super(application);
        DiscuzDao = BBSInformationDatabase.getInstance(application).getForumInformationDao();
        pagedListLiveData = new LivePagedListBuilder<>(DiscuzDao.getBBSPageList(),myPagingConfig).build();
    }

    public LiveData<PagedList<bbsInformation>> getPagedListLiveData() {
        return pagedListLiveData;
    }
}
