package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kidozh.discuzhub.daos.forumUserBriefInfoDao;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {forumUserBriefInfo.class},version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class forumUserBriefInfoDatabase extends RoomDatabase {
    private static final String DB_NAME = "forumUserBriefInfoDatabase.db";
    private static volatile forumUserBriefInfoDatabase instance;
    public static synchronized forumUserBriefInfoDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    public static forumUserBriefInfoDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context,forumUserBriefInfoDatabase.class,DB_NAME)
                .build();
    }

    public abstract forumUserBriefInfoDao getforumUserBriefInfoDao();


}
