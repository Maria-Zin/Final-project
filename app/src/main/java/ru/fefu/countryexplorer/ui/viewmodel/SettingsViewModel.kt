package ru.fefu.countryexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.fefu.countryexplorer.data.settings.SettingsDataStore
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isOfflinePreload = settingsDataStore.isOfflinePreload
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val historyRetentionDays = settingsDataStore.historyRetentionDays
        .stateIn(viewModelScope, SharingStarted.Eagerly, 7)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setDarkTheme(enabled) }
    }

    fun setOfflinePreload(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setOfflinePreload(enabled) }
    }

    fun setHistoryRetention(days: Int) {
        viewModelScope.launch { settingsDataStore.setHistoryRetentionDays(days) }
    }
}