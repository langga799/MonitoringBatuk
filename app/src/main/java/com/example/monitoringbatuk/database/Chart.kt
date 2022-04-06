package com.example.monitoringbatuk.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Chart(
    @PrimaryKey
    @ColumnInfo(name = "data")
    val data: Int
)
