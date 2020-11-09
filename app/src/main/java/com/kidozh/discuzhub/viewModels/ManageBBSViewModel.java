package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.app.ListActivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

public class ManageBBSViewModel extends AndroidViewModel {

    private LiveData<PagedList<bbsInformation>> pagedListLiveData;
    private forumInformationDao forumInformationDao;
    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(10)
            .build();

    public ManageBBSViewModel(@NonNull Application application) {
        super(application);
        forumInformationDao = BBSInformationDatabase.getInstance(application).getForumInformationDao();
        pagedListLiveData = new LivePagedListBuilder<>(forumInformationDao.getBBSPageList(),myPagingConfig).build();
    }

    public LiveData<PagedList<bbsInformation>> getPagedListLiveData() {
        return pagedListLiveData;
    }
}
