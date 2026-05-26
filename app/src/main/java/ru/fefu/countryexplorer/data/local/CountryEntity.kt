package ru.fefu.countryexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_countries")
data class CountryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val capital: String?,
    val region: String,
    val population: Long,
    val area: Double?,
    val flagUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)