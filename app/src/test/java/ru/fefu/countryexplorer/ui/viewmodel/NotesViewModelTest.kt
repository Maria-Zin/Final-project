package ru.fefu.countryexplorer.ui.viewmodel

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.fefu.countryexplorer.data.CountryRepository
import ru.fefu.countryexplorer.data.local.NoteEntity

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val repository = mockk<CountryRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getAllNotesFlow() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allNotes emits data from repository`() = runTest {
        val testNotes = listOf(
            NoteEntity("Russia", "Beautiful country", System.currentTimeMillis()),
            NoteEntity("France", "Romantic place", System.currentTimeMillis() - 1000)
        )
        coEvery { repository.getAllNotesFlow() } returns flowOf(testNotes)

        val viewModel = NotesViewModel(repository)
        advanceUntilIdle()

        assertEquals(2, viewModel.allNotes.value.size)
        assertEquals("Russia", viewModel.allNotes.value[0].countryId)
    }

    @Test
    fun `deleteNote calls repository`() = runTest {
        val viewModel = NotesViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteNote("Russia")
        advanceUntilIdle()

        coVerify { repository.deleteNote("Russia") }
    }

    @Test
    fun `updateNote saves to repository`() = runTest {
        val viewModel = NotesViewModel(repository)
        advanceUntilIdle()

        viewModel.updateNote("Russia", "Updated note")
        advanceUntilIdle()

        coVerify { repository.saveNote(match { it.countryId == "Russia" && it.text == "Updated note" }) }
    }

    @Test
    fun `initial state is empty`() = runTest {
        val viewModel = NotesViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.allNotes.value.isEmpty())
    }
}