package com.kidozh.discuzhub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kidozh.discuzhub.daos.bbsThreadDraftDao;
import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.utilities.DateConverter;


@Database(entities = {bbsThreadDraft.class},version = 3, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class bbsThreadDraftDatabase extends RoomDatabase {
    private static final String DB_NAME = "bbsThreadDraftDatabase.db";
    private static volatile bbsThreadDraftDatabase instance;



    public static synchronized bbsThreadDraftDatabase getInstance(Context context){
        if(instance == null){
            instance = getDatabase(context);
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE bbsThreadDraft "
                    + " ADD COLUMN apiString TEXT ");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE bbsThreadDraft "
                    + " ADD COLUMN password TEXT");
            database.execSQL("ALTER TABLE bbsThreadDraft ADD COLUMN freeMessage TEXT");
        }
    };

    private static bbsThreadDraftDatabase getDatabase(final Context context){
        return Room.databaseBuilder(context, bbsThreadDraftDatabase.class,DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build();
    }

    public abstract bbsThreadDraftDao getbbsThreadDraftDao();


}
