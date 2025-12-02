package com.wakala.fakhr.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [ResultEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "wakala_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
