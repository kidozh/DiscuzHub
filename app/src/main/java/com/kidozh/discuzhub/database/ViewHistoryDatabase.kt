package com.kidozh.discuzhub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kidozh.discuzhub.daos.ViewHistoryDao
import com.kidozh.discuzhub.entities.ViewHistory
import com.kidozh.discuzhub.utilities.DateConverter

@Database(entities = [ViewHistory::class], version = 2, exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class ViewHistoryDatabase : RoomDatabase() {
    abstract val dao: ViewHistoryDao

    companion object {
        private const val DB_NAME = "ViewHistory.db"

        @Volatile
        private var instance: ViewHistoryDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): ViewHistoryDatabase {
            synchronized(this){
                if (instance == null) {
                    instance = getDatabase(context)
                }
                return instance as ViewHistoryDatabase
            }

        }

        private fun getDatabase(context: Context): ViewHistoryDatabase {
            return Room.databaseBuilder(context, ViewHistoryDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}