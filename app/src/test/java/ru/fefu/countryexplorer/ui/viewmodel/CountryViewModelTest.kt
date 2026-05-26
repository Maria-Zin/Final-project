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
import ru.fefu.countryexplorer.data.NetworkUtils
import ru.fefu.countryexplorer.data.local.CountryEntity
import ru.fefu.countryexplorer.data.local.FavouriteEntity

@OptIn(ExperimentalCoroutinesApi::class)
class CountryViewModelTest {

    private val repository = mockk<CountryRepository>(relaxed = true)
    private val networkUtils = mockk<NetworkUtils>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val testCountry = CountryEntity(
        id = "Russia", name = "Russia", capital = "Moscow",
        region = "Europe", population = 144000000L, area = 17000000.0,
        flagUrl = null
    )
    private val testCountry2 = CountryEntity(
        id = "France", name = "France", capital = "Paris",
        region = "Europe", population = 67000000L, area = 551000.0,
        flagUrl = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { networkUtils.isNetworkAvailable() } returns true
        coEvery { repository.getCachedCountriesFlow() } returns flowOf(listOf(testCountry, testCountry2))
        coEvery { repository.getFavouritesFlow() } returns flowOf(emptyList())
        coEvery { repository.getNoteFlow(any()) } returns flowOf(null)
        coEvery { repository.refreshCountriesCache() } returns Unit
        coEvery { repository.addToHistory(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state emits Success with cached data`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue("Expected Success but got $state", state is UiState.Success)
        assertEquals(2, (state as UiState.Success).countries.size)
    }

    @Test
    fun `toggleFavourite adds country`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.toggleFavourite(testCountry)
        advanceUntilIdle()
        coVerify { repository.addFavourite(any<FavouriteEntity>()) }
    }

    @Test
    fun `toggleFavourite removes country`() = runTest {
        val fav = FavouriteEntity("Russia", "Russia", "Moscow", "Europe", 144000000L, 17000000.0)
        coEvery { repository.getFavouritesFlow() } returns flowOf(listOf(fav))
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.toggleFavourite(testCountry)
        advanceUntilIdle()
        coVerify { repository.removeFavourite("Russia") }
    }

    @Test
    fun `saveNote calls repository`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.saveNote("Russia", "Test")
        advanceUntilIdle()
        coVerify { repository.saveNote(match { it.countryId == "Russia" && it.text == "Test" }) }
    }

    @Test
    fun `deleteNote calls repository`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.deleteNote("Russia")
        advanceUntilIdle()
        coVerify { repository.deleteNote("Russia") }
    }

    @Test
    fun `search filters cached countries`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("Rus")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).countries.size)
        assertEquals("Russia", state.countries[0].name)
    }

    @Test
    fun `search with no match shows Empty`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("ZZZ")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Empty)
    }

    @Test
    fun `retry triggers refresh`() = runTest {
        val viewModel = CountryViewModel(repository, networkUtils)
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()
        coVerify(atLeast = 1) { repository.getCachedCountriesFlow() }
    }
}