package ru.fefu.countryexplorer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.countryexplorer.data.local.*

@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertHistoryAndReadFlow() = runBlocking {
        val item1 = HistoryEntity(
            countryId = "Russia",
            countryName = "Russia",
            timestamp = System.currentTimeMillis()
        )
        val item2 = HistoryEntity(
            countryId = "France",
            countryName = "France",
            timestamp = System.currentTimeMillis() - 1000
        )

        db.historyDao().insertHistory(item1)
        db.historyDao().insertHistory(item2)

        val result = db.historyDao().getRecentHistoryFlow().first()
        assertEquals(2, result.size)
        assertEquals("Russia", result[0].countryName)
        assertEquals("France", result[1].countryName)
    }

    @Test
    fun insertNoteAndRetrieve() = runBlocking {
        val note = NoteEntity(
            countryId = "Russia",
            text = "Test note about Russia",
            lastEdited = System.currentTimeMillis()
        )
        db.noteDao().insertNote(note)

        val result = db.noteDao().getNote("Russia")
        assertNotNull(result)
        assertEquals("Test note about Russia", result?.text)
        assertEquals("Russia", result?.countryId)
    }

    @Test
    fun cacheCountriesAndReadFlow() = runBlocking {
        val country1 = CountryEntity(
            id = "Russia",
            name = "Russia",
            capital = "Moscow",
            region = "Europe",
            population = 144000000L,
            area = 17000000.0,
            flagUrl = "https://flagcdn.com/w320/ru.png"
        )
        val country2 = CountryEntity(
            id = "France",
            name = "France",
            capital = "Paris",
            region = "Europe",
            population = 67000000L,
            area = 551000.0,
            flagUrl = "https://flagcdn.com/w320/fr.png"
        )

        db.countryDao().insertCountries(listOf(country1, country2))

        val cached = db.countryDao().getAllCachedCountriesFlow().first()
        assertEquals(2, cached.size)
        assertEquals("Russia", cached[0].id)
        assertEquals("France", cached[1].id)
    }

    @Test
    fun deleteOldHistoryRemovesOutdatedRecords() = runBlocking {
        val cutoffTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val oldItem = HistoryEntity(
            countryId = "Old",
            countryName = "Old Country",
            timestamp = cutoffTime - 8 * dayInMillis
        )
        val newItem = HistoryEntity(
            countryId = "New",
            countryName = "New Country",
            timestamp = cutoffTime - 1 * dayInMillis
        )
        val veryNewItem = HistoryEntity(
            countryId = "VeryNew",
            countryName = "VeryNew",
            timestamp = cutoffTime
        )

        db.historyDao().insertHistory(oldItem)
        db.historyDao().insertHistory(newItem)
        db.historyDao().insertHistory(veryNewItem)

        val sevenDaysAgo = cutoffTime - 7 * dayInMillis
        db.historyDao().deleteOldHistory(sevenDaysAgo)

        val remaining = db.historyDao().getRecentHistoryFlow().first()
        assertEquals(2, remaining.size)
        assertEquals("VeryNew", remaining[0].countryName)
        assertEquals("New Country", remaining[1].countryName)
    }

    @Test
    fun addAndRemoveFavourite() = runBlocking {
        val favourite = FavouriteEntity(
            id = "Japan",
            name = "Japan",
            capital = "Tokyo",
            region = "Asia",
            population = 125000000L,
            area = 377975.0
        )

        db.favouriteDao().insertFavourite(favourite)
        var favourites = db.favouriteDao().getAllFavouritesFlow().first()
        assertEquals(1, favourites.size)
        assertEquals("Japan", favourites[0].name)

        db.favouriteDao().deleteFavourite("Japan")
        favourites = db.favouriteDao().getAllFavouritesFlow().first()
        assertTrue(favourites.isEmpty())
    }

    @Test
    fun updateNoteOverwritesPrevious() = runBlocking {
        val note1 = NoteEntity("Russia", "First version", System.currentTimeMillis())
        db.noteDao().insertNote(note1)

        val note2 = NoteEntity("Russia", "Second version", System.currentTimeMillis())
        db.noteDao().insertNote(note2)

        val result = db.noteDao().getNote("Russia")
        assertNotNull(result)
        assertEquals("Second version", result?.text)
    }
}