package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.DataFactory.FavoriteThreadDataFactory;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoriteThreadViewModel extends AndroidViewModel {

    private static final String TAG = FavoriteThreadViewModel.class.getSimpleName();
    private LiveData<Integer> networkState;
    private LiveData<PagedList<FavoriteThread>> favoriteThreadListData;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    FavoriteThreadDao dao;

    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build();

    public FavoriteThreadViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
    }

    public void setInfo( @NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {

        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        favoriteThreadListData = new LivePagedListBuilder<>(dao.getFavoriteThreadPageListByBBSId(bbsInfo.getId()),myPagingConfig).build();
    }

    public LiveData<PagedList<FavoriteThread>> getFavoriteThreadListData() {
        return favoriteThreadListData;
    }
}
