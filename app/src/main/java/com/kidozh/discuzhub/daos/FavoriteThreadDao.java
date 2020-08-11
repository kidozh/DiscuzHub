package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.ViewHistory;

import java.util.List;

@Dao
public interface FavoriteThreadDao {
    @Query("SELECT * FROM FavoriteThread ORDER BY favid")
    DataSource.Factory<Integer, FavoriteThread> getAllFavoriteThreadDataSource();

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId ORDER BY favid")
    DataSource.Factory<Integer,FavoriteThread> getFavoriteThreadPageListByBBSId(int bbsId);

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND idKey=:tid ) ")
    LiveData<Boolean> isFavoriteThread(int bbsId, int tid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread... favoriteThreads);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread favoriteThread);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(List<FavoriteThread> favoriteThreads);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId")
    void clearFavoriteThreadByBBSId(int bbsId);

    @Delete
    void delete(FavoriteThread favoriteThread);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND idKey=:tid AND idType ='tid' ")
    void delete(int bbsId,int tid);

    @Delete
    void delete(FavoriteThread... favoriteThreads);

    @Query("SELECT COUNT(id) FROM FavoriteThread where belongedBBSId=:belongBBSId")
    public Integer getFavoriteThreadCount(int belongBBSId);
}
