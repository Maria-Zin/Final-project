package ru.fefu.countryexplorer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastEdited DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE countryId = :countryId")
    suspend fun getNote(countryId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE countryId = :countryId")
    fun getNoteFlow(countryId: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE countryId = :countryId")
    suspend fun deleteNote(countryId: String)
}