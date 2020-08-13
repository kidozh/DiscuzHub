package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kidozh.discuzhub.entities.FavoriteItem;

import java.util.List;

@Dao
public interface FavoriteItemDao {
    @Query("SELECT * FROM FavoriteItem ORDER BY favid")
    DataSource.Factory<Integer, FavoriteItem> getAllFavoriteThreadDataSource();

    @Query("SELECT * FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType ORDER BY favid")
    DataSource.Factory<Integer, FavoriteItem> getFavoriteItemPageListByBBSId(int bbsId, int userId,String idType);

    @Query("SELECT COUNT(idKey) FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    LiveData<Integer> getFavoriteItemCountLiveData(int bbsId, int userId,String idType);



    @Query("DELETE FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    void delete(int bbsId, int userId,int tid,String idType);

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType) ")
    LiveData<Boolean> isFavoriteItem(int bbsId, int userId, int tid,String idType);

    @Query("SELECT * FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    LiveData<FavoriteItem> getFavoriteItemByTid(int bbsId, int userId, int tid,String idType);

    @Query("SELECT * FROM FavoriteItem WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:tids) AND idType=:idType")
    List<FavoriteItem> queryFavoriteItemListByTids(int bbsId, int userId, List<Integer> tids,String idType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteItem... favoriteItems);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteItem favoriteItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(List<FavoriteItem> favoriteItems);

    @Query("DELETE FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    void clearFavoriteItemByBBSId(int bbsId, int userId,String idType);

    @Query("DELETE FROM FavoriteItem WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType AND favid != 0")
    void clearSyncedFavoriteItemByBBSId(int bbsId, int userId,String idType);

    @Delete
    void delete(FavoriteItem favoriteItem);



    @Delete
    void delete(FavoriteItem... favoriteItems);


}
