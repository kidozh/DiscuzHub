package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {FavoriteForum.class}, version = 2, exportSchema = true)
@TypeConverters(DateConverter.class)
public abstract class FavoriteForumDatabase extends RoomDatabase {
    private static final String DB_NAME = "FavoriteForum.db";
    private static volatile FavoriteForumDatabase instance;

    public static synchronized FavoriteForumDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }



    private static FavoriteForumDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, FavoriteForumDatabase.class,DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract FavoriteForumDao getDao();
}
