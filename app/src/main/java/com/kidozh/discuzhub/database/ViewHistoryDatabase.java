package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {ViewHistory.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class ViewHistoryDatabase extends RoomDatabase {
    private static final String DB_NAME = "ViewHistory.db";
    private static volatile ViewHistoryDatabase instance;

    public static synchronized ViewHistoryDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    private static ViewHistoryDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, ViewHistoryDatabase.class,DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract ViewHistoryDao getDao();
}
