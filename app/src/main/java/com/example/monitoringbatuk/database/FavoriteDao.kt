package com.example.monitoringbatuk.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data: Chart)

    @Query("SELECT * from chart")
    fun getAllChart(): Chart


}