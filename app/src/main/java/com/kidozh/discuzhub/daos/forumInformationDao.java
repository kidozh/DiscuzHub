package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;

import java.util.List;

@Dao
public interface forumInformationDao {

    @Query("SELECT * FROM bbsInformation ORDER BY position,id ASC")
    LiveData<List<bbsInformation>> getAllForumInformations();

    @Query("SELECT * FROM bbsInformation ORDER BY position,id ASC")
    DataSource.Factory<Integer, bbsInformation> getBBSPageList();

    @Query("SELECT * FROM bbsInformation WHERE id=:id")
    LiveData<bbsInformation> getForumInformationLiveDataById(int id);

    @Query("SELECT * FROM bbsInformation WHERE id=:id")
    bbsInformation getForumInformationById(int id);

    @Query("SELECT * FROM bbsInformation WHERE base_url LIKE '%' || :baseURL || '%' ")
    List<bbsInformation> getBBSInformationsByBaseURL(String baseURL);



    @Insert
    void insert(bbsInformation... bbsInformations);

    @Insert
    void insert(bbsInformation bbsInformation);

    @Update
    void update(bbsInformation... bbsInformations);

    @Update
    void update(List<bbsInformation> bbsInformations);

    @Delete
    void delete(bbsInformation... bbsInformations);

    @Query("DELETE FROM bbsInformation")
    void deleteAllForumInformation();
}
