package ru.fefu.countryexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.fefu.countryexplorer.data.CountryRepository
import ru.fefu.countryexplorer.data.local.NoteEntity
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    val allNotes: StateFlow<List<NoteEntity>> = repository.getAllNotesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun deleteNote(countryId: String) {
        viewModelScope.launch { repository.deleteNote(countryId) }
    }

    fun updateNote(countryId: String, text: String) {
        viewModelScope.launch { repository.saveNote(NoteEntity(countryId, text, System.currentTimeMillis())) }
    }
}