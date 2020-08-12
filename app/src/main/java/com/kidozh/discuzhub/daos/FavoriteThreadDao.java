package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kidozh.discuzhub.entities.FavoriteThread;

import java.util.List;

@Dao
public interface FavoriteThreadDao {
    @Query("SELECT * FROM FavoriteThread ORDER BY favid")
    DataSource.Factory<Integer, FavoriteThread> getAllFavoriteThreadDataSource();

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId ORDER BY favid")
    DataSource.Factory<Integer,FavoriteThread> getFavoriteThreadPageListByBBSId(int bbsId, int userId);

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid ) ")
    LiveData<Boolean> isFavoriteThread(int bbsId, int userId, int tid);

    @Query(" SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid ")
    LiveData<FavoriteThread> getFavoriteThreadByTid(int bbsId, int userId, int tid);

    @Query("SELECT * FROM FavoriteThread WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:tids)")
    List<FavoriteThread> queyFavoriteThreadListByTids(int bbsId, int userId, List<Integer> tids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread... favoriteThreads);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread favoriteThread);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(List<FavoriteThread> favoriteThreads);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId")
    void clearFavoriteThreadByBBSId(int bbsId, int userId);

    @Delete
    void delete(FavoriteThread favoriteThread);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType ='tid' ")
    void delete(int bbsId, int userId,int tid);

    @Delete
    void delete(FavoriteThread... favoriteThreads);

    @Query("SELECT COUNT(id) FROM FavoriteThread where belongedBBSId=:bbsId AND userId=:userId")
    public LiveData<Integer> getFavoriteThreadCountLiveData(int bbsId, int userId);
}
