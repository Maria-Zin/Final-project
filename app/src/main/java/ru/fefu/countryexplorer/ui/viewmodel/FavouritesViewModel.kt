package ru.fefu.countryexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.fefu.countryexplorer.data.CountryRepository
import ru.fefu.countryexplorer.data.local.FavouriteEntity
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    val favourites: StateFlow<List<FavouriteEntity>> = repository.getFavouritesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun removeFavourite(id: String) {
        viewModelScope.launch { repository.removeFavourite(id) }
    }
}