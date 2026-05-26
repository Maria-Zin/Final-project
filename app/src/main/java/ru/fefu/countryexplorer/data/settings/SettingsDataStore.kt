package ru.fefu.countryexplorer.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_OFFLINE_PRELOAD = booleanPreferencesKey("is_offline_preload")
        val HISTORY_RETENTION_DAYS = intPreferencesKey("history_retention_days")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_THEME] ?: false
    }

    val isOfflinePreload: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_OFFLINE_PRELOAD] ?: false
    }

    val historyRetentionDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[HISTORY_RETENTION_DAYS] ?: 7
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_DARK_THEME] = enabled }
    }

    suspend fun setOfflinePreload(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_OFFLINE_PRELOAD] = enabled }
    }

    suspend fun setHistoryRetentionDays(days: Int) {
        context.dataStore.edit { prefs -> prefs[HISTORY_RETENTION_DAYS] = days }
    }
}