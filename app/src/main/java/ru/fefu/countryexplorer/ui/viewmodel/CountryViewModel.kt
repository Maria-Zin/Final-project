package ru.fefu.countryexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.fefu.countryexplorer.data.CountryRepository
import ru.fefu.countryexplorer.data.NetworkUtils
import ru.fefu.countryexplorer.data.local.CountryEntity
import ru.fefu.countryexplorer.data.local.FavouriteEntity
import ru.fefu.countryexplorer.data.local.NoteEntity
import javax.inject.Inject

sealed class UiState {
    object Loading : UiState()
    data class Success(val countries: List<CountryEntity>, val isOffline: Boolean = false) : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val country: CountryEntity, val isFavourite: Boolean, val note: NoteEntity?) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CountryViewModel @Inject constructor(
    private val repository: CountryRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _tagFilter = MutableStateFlow<String?>(null)
    val tagFilter = _tagFilter.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _currentDetailId = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<UiState> = combine(
        _searchQuery.debounce(500).distinctUntilChanged(),
        _tagFilter,
        repository.getCachedCountriesFlow(),
        _refreshTrigger.onStart { emit(Unit) }
    ) { query, tag, cached, _ ->
        Triple(query, tag, cached)
    }.flatMapLatest { (query, tag, cached) ->
        flow {
            emit(UiState.Loading)

            val isOnline = networkUtils.isNetworkAvailable()

            if (cached.isEmpty()) {
                if (isOnline) {
                    try {
                        repository.refreshCountriesCache()
                        val updated = repository.getCachedCountriesFlow().first()
                        if (updated.isEmpty()) emit(UiState.Empty)
                        else {
                            val filtered = filterCountries(updated, query, tag)
                            if (filtered.isEmpty()) emit(UiState.Empty)
                            else emit(UiState.Success(countries = filtered, isOffline = false))
                        }
                    } catch (e: Exception) {
                        emit(UiState.Error("Ошибка загрузки"))
                    }
                } else {
                    emit(UiState.Error("Нет интернета и нет сохранённых данных"))
                }
            } else {
                val filtered = filterCountries(cached, query, tag)
                if (filtered.isEmpty()) emit(UiState.Empty)
                else emit(UiState.Success(countries = filtered, isOffline = !isOnline))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    private fun filterCountries(countries: List<CountryEntity>, query: String, tag: String?): List<CountryEntity> {
        var filtered = if (query.isBlank()) countries
        else countries.filter { it.name.contains(query, ignoreCase = true) }
        if (tag != null) filtered = filtered.filter { it.region.equals(tag, ignoreCase = true) }
        return filtered
    }

    val detailUiState: StateFlow<DetailUiState> = _currentDetailId
        .filterNotNull()
        .flatMapLatest { id ->
            combine(repository.getFavouritesFlow(), repository.getNoteFlow(id)) { favourites, note ->
                Pair(favourites, note)
            }.flatMapLatest { (favourites, note) ->
                flow {
                    emit(DetailUiState.Loading)
                    try {
                        val cached = repository.getCountryById(id)
                        if (cached != null) {
                            val isFav = favourites.any { it.id == id }
                            addToHistoryInternal(cached)
                            emit(DetailUiState.Success(cached, isFav, note))
                        } else emit(DetailUiState.Error("Страна не найдена"))
                    } catch (e: Exception) {
                        emit(DetailUiState.Error("Ошибка"))
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, DetailUiState.Loading)

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun setTagFilter(tag: String?) { _tagFilter.value = tag }
    fun retry() { _refreshTrigger.tryEmit(Unit) }
    fun loadCountryDetail(id: String) { _currentDetailId.value = id }

    private fun addToHistoryInternal(country: CountryEntity) {
        viewModelScope.launch { repository.addToHistory(country) }
    }

    fun toggleFavourite(country: CountryEntity) {
        viewModelScope.launch {
            val isFav = repository.getFavouritesFlow().first().any { it.id == country.id }
            if (isFav) repository.removeFavourite(country.id)
            else repository.addFavourite(FavouriteEntity(country.id, country.name, country.capital, country.region, country.population, country.area))
        }
    }

    fun saveNote(countryId: String, text: String) {
        viewModelScope.launch { repository.saveNote(NoteEntity(countryId, text, System.currentTimeMillis())) }
    }

    fun deleteNote(countryId: String) {
        viewModelScope.launch { repository.deleteNote(countryId) }
    }
}