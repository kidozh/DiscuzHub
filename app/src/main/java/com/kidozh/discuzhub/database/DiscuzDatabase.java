package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kidozh.discuzhub.daos.DiscuzDao;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.utilities.DateConverter;


@Database(entities = {Discuz.class},version = 4, exportSchema = true)
@TypeConverters(DateConverter.class)
public abstract class DiscuzDatabase extends RoomDatabase {
    private static final String DB_NAME = "bbsInformation.db";
    private static volatile DiscuzDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'Discuz' "
                    + " ADD COLUMN 'isSync' Boolean");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'Discuz' "
                    + " ADD COLUMN 'position' INTEGER NOT NULL DEFAULT(0)");

//            database.execSQL("ALTER TABLE forumInformation "
//                    + " ADD COLUMN position INT");
        }
    };




    public static synchronized DiscuzDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    private static DiscuzDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, DiscuzDatabase.class,DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build();
    }

    public static DiscuzDatabase getMainUIDatabase(final Context context){
        return Room.databaseBuilder(context, DiscuzDatabase.class,DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }



    public abstract DiscuzDao getForumInformationDao();


}
