package ru.fefu.countryexplorer

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import ru.fefu.countryexplorer.data.worker.CacheRefreshWorker
import ru.fefu.countryexplorer.data.worker.HistoryCleanupWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CountryApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initWorkers()
    }

    private fun initWorkers() {
        val workManager = WorkManager.getInstance(this)

        val cacheRequest = PeriodicWorkRequestBuilder<CacheRefreshWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "cache_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            cacheRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "history_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}