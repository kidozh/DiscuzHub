package com.kidozh.discuzhub.activities.ui.DashBoard;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.FavoriteThread;

public class DashBoardViewModel extends AndroidViewModel {
    private static final String TAG = DashBoardViewModel.class.getSimpleName();

    public LiveData<Integer> FavoriteThreadNumber;
    public LiveData<Integer> favoriteForumNumber;
    public MutableLiveData<Integer> hotThreadCountMutableLiveData = new MutableLiveData<>(0),
                                    hotForumCountMutableLiveData= new MutableLiveData<>(0);

    FavoriteThreadDao dao;
    FavoriteForumDao forumDao;

    public DashBoardViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
        forumDao = FavoriteForumDatabase.getInstance(application).getDao();
    }

    public void setFavoriteThreadInfo(int bbsId, int userId){

        FavoriteThreadNumber = dao.getFavoriteItemCountLiveData(bbsId, userId,"tid");
        favoriteForumNumber = forumDao.getFavoriteItemCountLiveData(bbsId,userId);
        Log.d(TAG,"Set favorite thread "+bbsId+" userId "+userId);
    }


}
