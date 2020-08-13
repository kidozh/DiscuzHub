package com.kidozh.discuzhub.activities.ui.DashBoard;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kidozh.discuzhub.daos.FavoriteItemDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;

public class DashBoardViewModel extends AndroidViewModel {
    private static final String TAG = DashBoardViewModel.class.getSimpleName();

    public LiveData<Integer> FavoriteThreadNumber;
    FavoriteItemDao dao;

    public DashBoardViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
    }

    public void setFavoriteThreadInfo(int bbsId, int userId){

        FavoriteThreadNumber = dao.getFavoriteItemCountLiveData(bbsId, userId,"tid");
        Log.d(TAG,"Set favorite thread "+bbsId+" userId "+userId);
    }


}
