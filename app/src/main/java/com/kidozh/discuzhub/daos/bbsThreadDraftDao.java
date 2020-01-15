package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;

import java.util.List;

@Dao
public interface bbsThreadDraftDao {

    @Query("SELECT * FROM bbsThreadDraft")
    LiveData<List<bbsThreadDraft>> getAllThreadDraft();

    @Query("SELECT * FROM bbsThreadDraft WHERE belongBBSId=:bbsid ORDER BY lastUpdateAt DESC")
    LiveData<List<bbsThreadDraft>> getAllThreadDraftByBBSId(int bbsid);

    @Insert
    void insert(bbsThreadDraft... bbsThreadDrafts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(bbsThreadDraft bbsThreadDraft);

    @Update
    void update(bbsThreadDraft... bbsThreadDrafts);

    @Update
    void update(bbsThreadDraft bbsThreadDraft);

    @Delete
    void delete(bbsThreadDraft bbsThreadDraft);

    @Query("DELETE FROM bbsThreadDraft WHERE belongBBSId=:bbsid")
    void deleteAllForumInformation(int bbsid);

    @Query("SELECT COUNT(id) FROM bbsThreadDraft WHERE belongBBSId =:bbsid")
    LiveData<Integer> getAllDraftsCount(int bbsid);
}
