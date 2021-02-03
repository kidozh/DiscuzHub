package com.kidozh.discuzhub.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kidozh.discuzhub.daos.SmileyDao

abstract class SmileyDatabase : RoomDatabase() {

    companion object{
        private val DB_NAME = "SmileyDatabase.db"

        private var instance: SmileyDatabase? = null

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

    public abstract fun getDao() : SmileyDao


}