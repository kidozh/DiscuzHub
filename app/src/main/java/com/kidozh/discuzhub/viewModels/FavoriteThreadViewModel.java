package com.kidozh.discuzhub.viewModels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.DataFactory.FavoriteThreadDataFactory;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoriteThreadViewModel extends ViewModel {

    private static final String TAG = FavoriteThreadViewModel.class.getSimpleName();
    private Executor executor;
    private LiveData<Integer> networkState;
    private LiveData<PagedList<FavoriteThread>> favoriteThreadListData;
    @SuppressLint("StaticFieldLeak")
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    public FavoriteThreadViewModel(@NonNull Context context, @NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        this.context = context;
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        init();
    }

    private void init(){
        executor = Executors.newFixedThreadPool(5);
        FavoriteThreadDataFactory favoriteThreadDataFactory = new FavoriteThreadDataFactory(context,bbsInfo,userBriefInfo);
        networkState = Transformations.switchMap(favoriteThreadDataFactory.getFavoriteThreadDataSourceMutableLiveData(),
                dataSource -> dataSource.networkStateLiveData
                );
        PagedList.Config pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(15)
                .setPageSize(15)
                .build();
        favoriteThreadListData = new LivePagedListBuilder(favoriteThreadDataFactory,pagedListConfig)
                .setFetchExecutor(executor)
                .build();
    }
}
