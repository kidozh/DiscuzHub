package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kidozh.discuzhub.daos.forumUserBriefInfoDao;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.DateConverter;

@Database(entities = {forumUserBriefInfo.class},version = 2, exportSchema = true)
@TypeConverters(DateConverter.class)
public abstract class UserDatabase extends RoomDatabase {
    private static final String DB_NAME = "forumUserBriefInfoDatabase.db";
    private static volatile UserDatabase instance;
    public static synchronized UserDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'forumUserBriefInfo' "
                    + " ADD COLUMN 'position' INTEGER NOT NULL DEFAULT(0)");
        }
    };

    private static UserDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, UserDatabase.class,DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public static UserDatabase getSyncDatabase(final Context context){
        return Room.databaseBuilder(context, UserDatabase.class,DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public abstract forumUserBriefInfoDao getforumUserBriefInfoDao();


}
