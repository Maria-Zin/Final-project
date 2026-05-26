package ru.fefu.countryexplorer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM cached_countries")
    fun getAllCachedCountriesFlow(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM cached_countries WHERE id = :id")
    suspend fun getCachedCountry(id: String): CountryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Query("DELETE FROM cached_countries")
    suspend fun clearCache()
}