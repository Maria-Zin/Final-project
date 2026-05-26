package ru.fefu.countryexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val capital: String?,
    val region: String,
    val population: Long,
    val area: Double?
)