package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kidozh.discuzhub.daos.UploadAttachmentDao;
import com.kidozh.discuzhub.daos.ThreadDraftDao;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.entities.ThreadDraft;

@Database(entities = {UploadAttachment.class, ThreadDraft.class}, version = 1, exportSchema = false)
public abstract class UploadAttachmentDatabase extends RoomDatabase {
    private static final String DB_NAME = "UploadAttachment.db";
    private static volatile UploadAttachmentDatabase instance;

    public static synchronized UploadAttachmentDatabase getInstance(Context context){
        if(instance == null){
            instance = getUploadAttachmentDatabase(context);
        }
        return instance;
    }

    private static UploadAttachmentDatabase getUploadAttachmentDatabase(final Context context){
        return Room.databaseBuilder(
                context,
                UploadAttachmentDatabase.class,
                DB_NAME
        ).build();
    }

    public abstract UploadAttachmentDao getUploadAttachmentDao();

    public abstract ThreadDraftDao getBBSThreadDraftDao();
}
