package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.ThreadDraft;

import java.util.List;

@Dao
public interface ThreadDraftDao {

    @Query("SELECT * FROM ThreadDraft")
    LiveData<List<ThreadDraft>> getAllThreadDraft();

    @Query("SELECT * FROM ThreadDraft WHERE belongBBSId=:bbsid ORDER BY lastUpdateAt DESC")
    LiveData<List<ThreadDraft>> getAllThreadDraftByBBSId(int bbsid);

    @Query("SELECT COUNT(id) FROM ThreadDraft")
    LiveData<Integer> getDraftNumber();

    @Insert
    void insert(ThreadDraft... ThreadDrafts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ThreadDraft ThreadDraft);

    @Update
    void update(ThreadDraft... ThreadDrafts);

    @Update
    void update(ThreadDraft ThreadDraft);

    @Delete
    void delete(ThreadDraft ThreadDraft);

    @Query("DELETE FROM ThreadDraft WHERE belongBBSId=:bbsid")
    void deleteAllForumInformation(int bbsid);

    @Query("SELECT COUNT(id) FROM ThreadDraft WHERE belongBBSId =:bbsid")
    LiveData<Integer> getAllDraftsCount(int bbsid);
}
