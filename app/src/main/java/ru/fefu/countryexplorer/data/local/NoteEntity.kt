package ru.fefu.countryexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val countryId: String,
    val text: String,
    val lastEdited: Long = System.currentTimeMillis()
)