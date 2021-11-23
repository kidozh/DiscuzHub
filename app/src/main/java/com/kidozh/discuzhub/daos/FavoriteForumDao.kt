package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kidozh.discuzhub.entities.FavoriteForum;

import java.util.List;

@Dao
public interface FavoriteForumDao {
    @Query("SELECT * FROM FavoriteForum ORDER BY favid")
    DataSource.Factory<Integer, FavoriteForum> getAllFavoriteForumDataSource();

    @Query("SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid' ORDER BY favid")
    DataSource.Factory<Integer, FavoriteForum> getFavoriteItemPageListByBBSId(int bbsId, int userId);

    @Query("SELECT COUNT(idKey) FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid'")
    LiveData<Integer> getFavoriteItemCountLiveData(int bbsId, int userId);



    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid'")
    void delete(int bbsId, int userId, int fid);

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid') ")
    LiveData<Boolean> isFavoriteItem(int bbsId, int userId, int fid);

    @Query("SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid'")
    LiveData<FavoriteForum> getFavoriteItemByfid(int bbsId, int userId, int fid);

    @Query("SELECT * FROM FavoriteForum WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:fids) AND idType='fid'")
    List<FavoriteForum> queryFavoriteItemListByfids(int bbsId, int userId, List<Integer> fids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteForum... FavoriteForums);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(FavoriteForum FavoriteForum);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(List<FavoriteForum> FavoriteForums);

    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid'")
    void clearFavoriteItemByBBSId(int bbsId, int userId);

    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid' AND favid != 0")
    void clearSyncedFavoriteItemByBBSId(int bbsId, int userId);

    @Delete
    void delete(FavoriteForum FavoriteForum);



    @Delete
    void delete(FavoriteForum... FavoriteForums);


}
