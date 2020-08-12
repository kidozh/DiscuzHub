package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {FavoriteThread.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class FavoriteThreadDatabase extends RoomDatabase {
    private static final String DB_NAME = "FavoriteThread.db";
    private static volatile FavoriteThreadDatabase instance;

    public static synchronized FavoriteThreadDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }



    private static FavoriteThreadDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, FavoriteThreadDatabase.class,DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract FavoriteThreadDao getDao();
}
