package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.Discuz;

public class ViewHistoryViewModel extends AndroidViewModel {

    public LiveData<PagedList<ViewHistory>> pagedListLiveData;
    private ViewHistoryDao viewHistoryDao;
    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(10)
            .build();

    public ViewHistoryViewModel(Application application){
        super(application);
        viewHistoryDao = ViewHistoryDatabase.getInstance(application).getDao();

        pagedListLiveData = new LivePagedListBuilder<>(viewHistoryDao.getViewHistoryPageList(),myPagingConfig).build();
    }



    public void setBBSInfo(Discuz bbsInfo){
        pagedListLiveData = new LivePagedListBuilder<>(viewHistoryDao.getViewHistoryPageListByBBSId(bbsInfo.getId()),myPagingConfig).build();
    }

    public void setSearchText(Discuz bbsInfo, String text){
        pagedListLiveData = new LivePagedListBuilder<>(viewHistoryDao.getViewHistoryPageListByBBSIdWithSearchText(bbsInfo.getId(),text),myPagingConfig).build();
    }

    public LiveData<PagedList<ViewHistory>> getPagedListLiveData() {
        return pagedListLiveData;
    }
}
