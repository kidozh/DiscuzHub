package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kidozh.discuzhub.entities.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User WHERE belongedBBSID=:bbs_id ORDER BY position,id ASC")
    LiveData<List<User>> getAllUserByBBSID(int bbs_id);

    @Query("SELECT * FROM User")
    LiveData<List<User>> getAllUserLiveData();

    @Query("SELECT * FROM User")
    List<User> getAllUser();

    @Query("DELETE FROM User WHERE belongedBBSID=:bbs_id")
    void deleteAllUserByBBSID(int bbs_id);

    @Query("SELECT * FROM User WHERE id=:id")
    LiveData<User> getUserLiveDataById(int id);

    @Query("SELECT * FROM User WHERE id=:id")
    User getUserById(int id);

    @Insert
    void insert(User... Users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User User);

    @Update
    void update(User... Users);
    @Update
    void update(List<User> Users);

    @Delete
    void delete(User... Users);


}
