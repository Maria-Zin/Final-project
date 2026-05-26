package ru.fefu.countryexplorer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistoryFlow(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM history WHERE timestamp < :before")
    suspend fun deleteOldHistory(before: Long)

    @Delete
    suspend fun deleteHistoryItem(item: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
}