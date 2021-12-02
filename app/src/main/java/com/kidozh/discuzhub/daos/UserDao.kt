package com.kidozh.discuzhub.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kidozh.discuzhub.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User WHERE belongedBBSID=:bbs_id ORDER BY position,id ASC")
    fun getAllUserByBBSID(bbs_id: Int): LiveData<List<User>>

    @get:Query("SELECT * FROM User")
    val allUserLiveData: LiveData<List<User>>

    @get:Query("SELECT * FROM User")
    val allUser: List<User>

    @Query("DELETE FROM User WHERE belongedBBSID=:bbs_id")
    fun deleteAllUserByBBSID(bbs_id: Int)

    @Query("SELECT * FROM User WHERE id=:id")
    fun getUserLiveDataById(id: Int): LiveData<User?>

    @Query("SELECT * FROM User WHERE id=:id")
    fun getUserById(id: Int): User?

    @Insert
    fun insert(vararg Users: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(User: User): Long

    @Update
    fun update(vararg Users: User)

    @Update
    fun update(Users: List<User>)

    @Delete
    fun delete(vararg Users: User)

    @Query("SELECT * FROM User WHERE belongedBBSID=:discuzId AND uid=:uid LIMIT 1")
    fun getFirstUserByDiscuzIdAndUid(discuzId: Int, uid: Int): User?

    @Query("DELETE FROM User WHERE belongedBBSID=:discuzId AND uid=:uid")
    fun deleteAllUserByDiscuzIdAndUid(discuzId: Int, uid: Int)
}