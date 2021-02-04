package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.List;

@Dao
public interface forumUserBriefInfoDao {

    @Query("SELECT * FROM forumUserBriefInfo WHERE belongedBBSID=:bbs_id ORDER BY position,id ASC")
    LiveData<List<forumUserBriefInfo>> getAllUserByBBSID(int bbs_id);

    @Query("SELECT * FROM forumUserBriefInfo")
    LiveData<List<forumUserBriefInfo>> getAllUserLiveData();

    @Query("SELECT * FROM forumUserBriefInfo")
    List<forumUserBriefInfo> getAllUser();

    @Query("DELETE FROM forumUserBriefInfo WHERE belongedBBSID=:bbs_id")
    void deleteAllUserByBBSID(int bbs_id);

    @Query("SELECT * FROM forumUserBriefInfo WHERE id=:id")
    LiveData<forumUserBriefInfo> getUserLiveDataById(int id);

    @Query("SELECT * FROM forumUserBriefInfo WHERE id=:id")
    forumUserBriefInfo getUserById(int id);

    @Insert
    void insert(forumUserBriefInfo... forumUserBriefInfos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(forumUserBriefInfo forumUserBriefInfo);

    @Update
    void update(forumUserBriefInfo... forumUserBriefInfos);
    @Update
    void update(List<forumUserBriefInfo> forumUserBriefInfos);

    @Delete
    void delete(forumUserBriefInfo... forumUserBriefInfos);


}
