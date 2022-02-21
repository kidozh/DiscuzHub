package com.kidozh.discuzhub.daos

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.kidozh.discuzhub.entities.Discuz

@Dao
interface DiscuzDao {
    @get:Query("SELECT * FROM Discuz ORDER BY position,id ASC")
    val allForumInformations: LiveData<List<Discuz>>

    @get:Query("SELECT * FROM Discuz ORDER BY position,id ASC")
    val bBSPageList: DataSource.Factory<Int, Discuz>

    @Query("SELECT * FROM Discuz WHERE id=:id")
    fun getForumInformationLiveDataById(id: Int): LiveData<Discuz>

    @Query("SELECT * FROM Discuz WHERE id=:id")
    fun getForumInformationById(id: Int): Discuz

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' ")
    fun getBBSInformationsByBaseURL(baseURL: String): List<Discuz>

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' LIMIT 1")
    fun getBBSInformationLiveDataByBaseURL(baseURL: String): LiveData<Discuz>

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' LIMIT 1")
    fun getBBSInformationByBaseURL(baseURL: String): Discuz?



    @Insert
    fun insert(vararg Discuzs: Discuz)

    @Insert
    fun insert(Discuz: Discuz)

    @Update
    fun update(vararg Discuzs: Discuz)

    @Update
    fun update(Discuzs: List<Discuz>)

    @Delete
    fun delete(vararg Discuzs: Discuz)

    @Query("DELETE FROM Discuz")
    fun deleteAllForumInformation()
}