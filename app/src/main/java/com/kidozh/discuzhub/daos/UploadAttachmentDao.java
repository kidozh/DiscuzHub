package com.kidozh.discuzhub.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.kidozh.discuzhub.entities.UploadAttachment;

import java.util.List;

@Dao
public interface UploadAttachmentDao {
    @Query("SELECT * FROM UPLOADATTACHMENT")
    List<UploadAttachment> getAllUploadAttachments();

    @Query("SELECT * FROM UploadAttachment WHERE emp_id=:id")
    LiveData<List<UploadAttachment>> getAllUploadAttachmentFromDraft(int id);

    @Insert
    void insertUploadAttachment(UploadAttachment uploadAttachment);

    @Delete
    void deleteUploadAttachment(UploadAttachment attachment);
}
