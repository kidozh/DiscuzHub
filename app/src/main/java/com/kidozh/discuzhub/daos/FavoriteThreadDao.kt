package com.kidozh.discuzhub.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.kidozh.discuzhub.entities.FavoriteThread

@Dao
interface FavoriteThreadDao {
    @get:Query("SELECT * FROM FavoriteThread ORDER BY favid")
    val allFavoriteThreadDataSource: PagingSource<Int, FavoriteThread>

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType ORDER BY favid")
    fun getFavoriteItemPageListByBBSId(
        bbsId: Int,
        userId: Int,
        idType: String?
    ): PagingSource<Int, FavoriteThread>

    @Query("SELECT COUNT(idKey) FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    fun getFavoriteItemCountLiveData(bbsId: Int, userId: Int, idType: String?): LiveData<Int?>?

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    fun delete(bbsId: Int, userId: Int, tid: Int, idType: String?)

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType) ")
    fun isFavoriteItem(bbsId: Int, userId: Int, tid: Int, idType: String?): LiveData<Boolean>

    @Query("SELECT * FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:tid AND idType=:idType")
    fun getFavoriteItemByTid(
        bbsId: Int,
        userId: Int,
        tid: Int,
        idType: String?
    ): LiveData<FavoriteThread?>

    @Query("SELECT * FROM FavoriteThread WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:tids) AND idType=:idType")
    fun queryFavoriteItemListByTids(
        bbsId: Int,
        userId: Int,
        tids: List<Int?>?,
        idType: String?
    ): List<FavoriteThread?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg favoriteThreads: FavoriteThread?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(favoriteThread: FavoriteThread?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(favoriteThreads: List<FavoriteThread?>?)

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType")
    fun clearFavoriteItemByBBSId(bbsId: Int, userId: Int, idType: String?)

    @Query("DELETE FROM FavoriteThread WHERE belongedBBSId=:bbsId AND userId=:userId AND idType=:idType AND favid != 0")
    fun clearSyncedFavoriteItemByBBSId(bbsId: Int, userId: Int, idType: String?)

    @Delete
    fun delete(favoriteThread: FavoriteThread?)

    @Delete
    fun delete(vararg favoriteThreads: FavoriteThread?)
}