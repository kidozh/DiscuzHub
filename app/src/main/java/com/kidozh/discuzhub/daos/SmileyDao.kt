package com.kidozh.discuzhub.daos

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.room.*
import com.google.android.material.circularreveal.CircularRevealHelper
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.entities.ViewHistory


@Dao
interface SmileyDao {

    @Query("SELECT * FROM Smiley WHERE discuzId = :discuzId ORDER BY updateAt DESC")
    fun allSimleys(discuzId : Int): DataSource.Factory<Int, Smiley>

    @Query("SELECT * FROM Smiley WHERE discuzId = :discuzId ORDER BY updateAt DESC LIMIT 20")
    fun latestSimleys(discuzId : Int): LiveData<List<Smiley>>

    @Query("SELECT * FROM Smiley WHERE code = :code")
    fun simleybyCode(code : String): Smiley?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(smiley: Smiley): Long

    @Update
    fun update(smiley: Smiley)

}