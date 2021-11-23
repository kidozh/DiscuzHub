package com.kidozh.discuzhub.daos

import com.kidozh.discuzhub.entities.FavoriteForum
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface FavoriteForumDao {
//    @Query("SELECT * FROM FavoriteForum ORDER BY favid")
//    val allFavoriteForumDataSource: DataSource<Int, FavoriteForum>
//
//    @Query("SELECT * FROM FavoriteForum ORDER BY favid DESC")
//    val allFavoriteForumPagingSource: PagingSource<Int, FavoriteForum>

//    @Query("SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid' ORDER BY favid")
//    fun getFavoriteItemPageListByBBSId(bbsId: Int, userId: Int): DataSource<Int, FavoriteForum>

    @Query("SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid' ORDER BY favid")
    fun getFavoriteItemPagingListByBBSId(bbsId: Int, userId: Int): PagingSource<Int, FavoriteForum>

    @Query("SELECT COUNT(idKey) FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid'")
    fun getFavoriteItemCountLiveData(bbsId: Int, userId: Int): LiveData<Int>

    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid'")
    fun delete(bbsId: Int, userId: Int, fid: Int)

    @Query(" SELECT EXISTS (SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid') ")
    fun isFavoriteItem(bbsId: Int, userId: Int, fid: Int): LiveData<Boolean>

    @Query("SELECT * FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idKey=:fid AND idType='fid'")
    fun getFavoriteItemByfid(bbsId: Int, userId: Int, fid: Int): LiveData<FavoriteForum>

    @Query("SELECT * FROM FavoriteForum WHERE (belongedBBSId=:bbsId AND userId=:userId) AND idKey IN (:fids) AND idType='fid'")
    fun queryFavoriteItemListByfids(
        bbsId: Int,
        userId: Int,
        fids: List<Int?>?
    ): List<FavoriteForum?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg FavoriteForums: FavoriteForum?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(FavoriteForum: FavoriteForum?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(FavoriteForums: List<FavoriteForum?>?)

    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid'")
    fun clearFavoriteItemByBBSId(bbsId: Int, userId: Int)

    @Query("DELETE FROM FavoriteForum WHERE belongedBBSId=:bbsId AND userId=:userId AND idType='fid' AND favid != 0")
    fun clearSyncedFavoriteItemByBBSId(bbsId: Int, userId: Int)

    @Delete
    fun delete(FavoriteForum: FavoriteForum?)

    @Delete
    fun delete(vararg FavoriteForums: FavoriteForum?)
}