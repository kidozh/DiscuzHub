package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

@Dao
public interface forumInformationDao {

    @Query("SELECT * FROM bbsInformation")
    LiveData<List<bbsInformation>> getAllForumInformations();

    @Insert
    void insert(bbsInformation... bbsInformations);

    @Insert
    void insert(bbsInformation bbsInformation);

    @Update
    void update(bbsInformation... bbsInformations);

    @Delete
    void delete(bbsInformation... bbsInformations);

    @Query("DELETE FROM bbsInformation")
    void deleteAllForumInformation();
}
