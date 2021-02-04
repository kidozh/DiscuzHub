package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.Discuz;

import java.util.List;

@Dao
public interface DiscuzDao {

    @Query("SELECT * FROM Discuz ORDER BY position,id ASC")
    LiveData<List<Discuz>> getAllForumInformations();

    @Query("SELECT * FROM Discuz ORDER BY position,id ASC")
    DataSource.Factory<Integer, Discuz> getBBSPageList();

    @Query("SELECT * FROM Discuz WHERE id=:id")
    LiveData<Discuz> getForumInformationLiveDataById(int id);

    @Query("SELECT * FROM Discuz WHERE id=:id")
    Discuz getForumInformationById(int id);

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' ")
    List<Discuz> getBBSInformationsByBaseURL(String baseURL);

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' LIMIT 1")
    LiveData<Discuz> getBBSInformationLiveDataByBaseURL(String baseURL);

    @Query("SELECT * FROM Discuz WHERE base_url LIKE '%' || :baseURL || '%' LIMIT 1")
    Discuz getBBSInformationByBaseURL(String baseURL);


    @Insert
    void insert(Discuz... Discuzs);

    @Insert
    void insert(Discuz Discuz);

    @Update
    void update(Discuz... Discuzs);

    @Update
    void update(List<Discuz> Discuzs);

    @Delete
    void delete(Discuz... Discuzs);

    @Query("DELETE FROM Discuz")
    void deleteAllForumInformation();
}
