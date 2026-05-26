package ru.fefu.countryexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.fefu.countryexplorer.data.CountryRepository
import ru.fefu.countryexplorer.data.local.HistoryEntity
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    val historyFlow: StateFlow<List<HistoryEntity>> = repository.getRecentHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun deleteItem(item: HistoryEntity) {
        viewModelScope.launch { repository.deleteHistoryItem(item) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAllHistory() }
    }
}