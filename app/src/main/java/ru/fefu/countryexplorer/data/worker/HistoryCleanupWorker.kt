package ru.fefu.countryexplorer.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.fefu.countryexplorer.data.local.HistoryDao
import ru.fefu.countryexplorer.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.first

@HiltWorker
class HistoryCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val historyDao: HistoryDao,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val retentionDays = settingsDataStore.historyRetentionDays.first()
            val cutoffTime = System.currentTimeMillis() - retentionDays * 24 * 60 * 60 * 1000L
            historyDao.deleteOldHistory(cutoffTime)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}