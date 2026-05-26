package ru.fefu.countryexplorer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.fefu.countryexplorer.data.local.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepository @Inject constructor(
    private val apiService: ApiService,
    private val favouriteDao: FavouriteDao,
    private val historyDao: HistoryDao,
    private val noteDao: NoteDao,
    private val countryDao: CountryDao,
    private val networkUtils: NetworkUtils
) {
    fun getCachedCountriesFlow(): Flow<List<CountryEntity>> = countryDao.getAllCachedCountriesFlow()

    suspend fun getCountryById(id: String): CountryEntity? = countryDao.getCachedCountry(id)

    suspend fun refreshCountriesCache() {
        if (networkUtils.isNetworkAvailable()) {
            try {
                val countries = apiService.getAllCountries()
                val entities = countries.map { country ->
                    CountryEntity(
                        id = country.stableId,
                        name = country.name,
                        capital = country.capital,
                        region = country.region,
                        population = country.population,
                        area = country.area,
                        flagUrl = country.flags?.png
                    )
                }
                countryDao.clearCache()
                countryDao.insertCountries(entities)
            } catch (e: Exception) {
            }
        }
    }

    fun getFavouritesFlow(): Flow<List<FavouriteEntity>> = favouriteDao.getAllFavouritesFlow()

    suspend fun addFavourite(country: FavouriteEntity) = favouriteDao.insertFavourite(country)

    suspend fun removeFavourite(id: String) = favouriteDao.deleteFavourite(id)

    fun getRecentHistoryFlow(): Flow<List<HistoryEntity>> = historyDao.getRecentHistoryFlow()

    suspend fun addToHistory(country: CountryEntity) {
        historyDao.insertHistory(
            HistoryEntity(
                countryId = country.id,
                countryName = country.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(item: HistoryEntity) = historyDao.deleteHistoryItem(item)

    suspend fun clearAllHistory() = historyDao.clearAllHistory()

    fun getAllNotesFlow(): Flow<List<NoteEntity>> = noteDao.getAllNotesFlow()

    fun getNoteFlow(countryId: String): Flow<NoteEntity?> = noteDao.getNoteFlow(countryId)

    suspend fun getNote(countryId: String): NoteEntity? = noteDao.getNote(countryId)

    suspend fun saveNote(note: NoteEntity) = noteDao.insertNote(note)

    suspend fun deleteNote(countryId: String) = noteDao.deleteNote(countryId)
}