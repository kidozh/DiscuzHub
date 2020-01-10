package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.List;

@Dao
public interface forumUserBriefInfoDao {

    @Query("SELECT * FROM forumUserBriefInfo WHERE belongedBBSID=:bbs_id")
    LiveData<List<forumUserBriefInfo>> getAllUserByBBSID(int bbs_id);

    @Query("SELECT * FROM forumUserBriefInfo")
    LiveData<List<forumUserBriefInfo>> getAllUser();

    @Query("DELETE FROM forumUserBriefInfo WHERE belongedBBSID=:bbs_id")
    void deleteAllUserByBBSID(int bbs_id);

    @Insert
    void insert(forumUserBriefInfo... forumUserBriefInfos);

    @Insert
    long insert(forumUserBriefInfo forumUserBriefInfo);

    @Update
    void update(forumUserBriefInfo... forumUserBriefInfos);

    @Delete
    void delete(forumUserBriefInfo... forumUserBriefInfos);


}
