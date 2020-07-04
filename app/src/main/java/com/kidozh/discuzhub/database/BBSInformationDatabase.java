package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.DateConverter;


@Database(entities = {bbsInformation.class},version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class BBSInformationDatabase extends RoomDatabase {
    private static final String DB_NAME = "bbsInformation.db";
    private static volatile BBSInformationDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE forumInformation "
                    + " ADD COLUMN isSync Boolean ");
        }
    };


    public static synchronized BBSInformationDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    private static BBSInformationDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, BBSInformationDatabase.class,DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public abstract forumInformationDao getForumInformationDao();


}
