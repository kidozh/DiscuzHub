package com.kidozh.discuzhub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kidozh.discuzhub.daos.UserDao
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.DateConverter

@Database(entities = [User::class], version = 4, exportSchema = true)
@TypeConverters(
    DateConverter::class
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getforumUserBriefInfoDao(): UserDao

    companion object {
        private const val DB_NAME = "forumUserBriefInfoDatabase.db"

        @Volatile
        private var instance: UserDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): UserDatabase {
            if (instance == null) {
                instance = getDatabase(context)
                return instance as UserDatabase
            }
            else{
                return instance as UserDatabase
            }
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE 'forumUserBriefInfo' "
                            + " ADD COLUMN 'position' INTEGER NOT NULL DEFAULT(0)"
                )
            }
        }

        private fun getDatabase(context: Context): UserDatabase {
            return Room.databaseBuilder(context, UserDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }

        @JvmStatic
        fun getSyncDatabase(context: Context): UserDatabase {
            return Room.databaseBuilder(context, UserDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}