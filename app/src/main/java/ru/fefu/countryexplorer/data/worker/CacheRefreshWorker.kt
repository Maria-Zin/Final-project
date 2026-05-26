package ru.fefu.countryexplorer.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.fefu.countryexplorer.data.ApiService
import ru.fefu.countryexplorer.data.local.CountryDao
import ru.fefu.countryexplorer.data.local.CountryEntity

@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val countryDao: CountryDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val countries = apiService.getAllCountries()
            val entities = countries.map { country ->
                CountryEntity(
                    id = country.name.replace(" ", "_"),
                    name = country.name,
                    capital = country.capital,
                    region = country.region,
                    population = country.population,
                    area = country.area,
                    flagUrl = country.flags?.png
                )
            }
            countryDao.insertCountries(entities)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}