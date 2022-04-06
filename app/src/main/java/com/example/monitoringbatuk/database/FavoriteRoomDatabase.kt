package com.example.monitoringbatuk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Chart::class], version = 1)
abstract class ChartRoomDatabase : RoomDatabase() {


    abstract fun chartDao(): FavoriteDao


    companion object {
        @Volatile
        private var INSTANCE: ChartRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): ChartRoomDatabase {
            if (INSTANCE == null) {
                synchronized(ChartRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ChartRoomDatabase::class.java, "favorite_database"
                    )
                        .build()
                }
            }

            return INSTANCE as ChartRoomDatabase
        }
    }

}