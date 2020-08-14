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

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType ORDER BY favid")
    DataSource.Factory<Integer, FavoriteThread> getFavoriteItemPageListByBBSId(int bbsId, int userId, String idType);

    @Query("SELECT COUNT(idKey) FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    LiveData<Integer> getFavoriteItemCountLiveData(int bbsId, int userId,String idType);



    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    void delete(int bbsId, int userId,int tid,String idType);

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType) ")
    LiveData<Boolean> isFavoriteItem(int bbsId, int userId, int tid,String idType);

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    LiveData<FavoriteThread> getFavoriteItemByTid(int bbsId, int userId, int tid, String idType);

    @Query("SELECT * FROM FavoriteThread WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:tids) AND idType=:idType")
    List<FavoriteThread> queryFavoriteItemListByTids(int bbsId, int userId, List<Integer> tids, String idType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread... favoriteThreads);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteThread favoriteThread);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(List<FavoriteThread> favoriteThreads);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    void clearFavoriteItemByBBSId(int bbsId, int userId,String idType);

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType AND favid != 0")
    void clearSyncedFavoriteItemByBBSId(int bbsId, int userId,String idType);

    @Delete
    void delete(FavoriteThread favoriteThread);



    @Delete
    void delete(FavoriteThread... favoriteThreads);


}
