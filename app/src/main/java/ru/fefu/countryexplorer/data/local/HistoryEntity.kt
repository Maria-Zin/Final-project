package ru.fefu.countryexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val countryId: String,
    val countryName: String,
    val timestamp: Long = System.currentTimeMillis()
)