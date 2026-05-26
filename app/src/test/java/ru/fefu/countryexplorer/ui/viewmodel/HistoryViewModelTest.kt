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
import ru.fefu.countryexplorer.data.local.HistoryEntity

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val repository = mockk<CountryRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getRecentHistoryFlow() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `historyFlow emits data from repository`() = runTest {
        val testHistory = listOf(
            HistoryEntity(1, "Russia", "Russia", System.currentTimeMillis()),
            HistoryEntity(2, "France", "France", System.currentTimeMillis() - 1000)
        )
        coEvery { repository.getRecentHistoryFlow() } returns flowOf(testHistory)

        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        assertEquals(2, viewModel.historyFlow.value.size)
        assertEquals("Russia", viewModel.historyFlow.value[0].countryName)
    }

    @Test
    fun `deleteItem calls repository`() = runTest {
        val item = HistoryEntity(1, "Russia", "Russia")
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteItem(item)
        advanceUntilIdle()

        coVerify { repository.deleteHistoryItem(item) }
    }

    @Test
    fun `clearAll calls repository`() = runTest {
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.clearAll()
        advanceUntilIdle()

        coVerify { repository.clearAllHistory() }
    }

    @Test
    fun `initial state is empty`() = runTest {
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.historyFlow.value.isEmpty())
    }
}