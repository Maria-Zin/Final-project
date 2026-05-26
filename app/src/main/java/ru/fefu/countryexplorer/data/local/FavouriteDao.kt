package ru.fefu.countryexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites")
    fun getAllFavouritesFlow(): Flow<List<FavouriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(country: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteFavourite(id: String)
}
