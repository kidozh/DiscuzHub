package com.kidozh.discuzhub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kidozh.discuzhub.daos.SmileyDao
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.utilities.DateConverter

@Database(entities = [Smiley::class], version = 6, exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class SmileyDatabase : RoomDatabase() {

    companion object{
        private val DB_NAME = "SmileyDatabase.db"
        @Volatile

        private var instance: SmileyDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): SmileyDatabase{
            if(instance == null){
                instance = getDatabase(context)
            }
            return instance as SmileyDatabase
        }

        private fun getDatabase(context: Context): SmileyDatabase {
            return Room.databaseBuilder(context, SmileyDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }


    }

    abstract fun getDao() : SmileyDao


}