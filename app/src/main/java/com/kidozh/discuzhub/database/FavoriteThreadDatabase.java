package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kidozh.discuzhub.daos.FavoriteItemDao;
import com.kidozh.discuzhub.entities.FavoriteItem;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {FavoriteItem.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class FavoriteThreadDatabase extends RoomDatabase {
    private static final String DB_NAME = "FavoriteItem.db";
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

    public abstract FavoriteItemDao getDao();
}
