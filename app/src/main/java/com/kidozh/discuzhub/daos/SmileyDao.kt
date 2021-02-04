package com.kidozh.discuzhub.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kidozh.discuzhub.entities.Smiley


@Dao
interface SmileyDao {

    @Query("SELECT * FROM Smiley WHERE discuzId = :discuzId ORDER BY updateAt DESC")
    fun allSimleys(discuzId : Int):LiveData<List<Smiley>>;

    @Insert
    fun insert(smiley: Smiley)

    @Update
    fun update(smiley: Smiley)

}