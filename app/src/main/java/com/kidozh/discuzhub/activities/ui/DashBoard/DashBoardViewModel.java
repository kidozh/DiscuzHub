package com.kidozh.discuzhub.activities.ui.DashBoard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteThread;

public class DashBoardViewModel extends AndroidViewModel {

    public LiveData<Integer> FavoriteThreadNumber;
    FavoriteThreadDao dao;

    public DashBoardViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
    }

    public void setFavoriteThreadInfo(int bbsId){
        FavoriteThreadNumber = dao.getFavoriteThreadCountLiveData(bbsId);
    }


}
