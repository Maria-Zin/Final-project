package ru.fefu.countryexplorer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ru.fefu.countryexplorer.data.local.CountryEntity
import ru.fefu.countryexplorer.ui.viewmodel.CountryViewModel
import ru.fefu.countryexplorer.ui.viewmodel.FavouritesViewModel
import ru.fefu.countryexplorer.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryListScreen(
    navController: NavHostController,
    viewModel: CountryViewModel = hiltViewModel(),
    favouritesViewModel: FavouritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favourites by favouritesViewModel.favourites.collectAsStateWithLifecycle()

    val favouriteIds = remember(favourites) {
        favourites.map { it.id }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Country Explorer") },
                actions = {
                    IconButton(onClick = { navController.navigate("favourites") }) {
                        Icon(Icons.Default.Favorite, "Избранное")
                    }
                    IconButton(onClick = { navController.navigate("history") }) {
                        Icon(Icons.Default.History, "История")
                    }
                    IconButton(onClick = { navController.navigate("notes") }) {
                        Icon(Icons.Default.Edit, "Заметки")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Поиск страны...") },
                singleLine = true
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Empty -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Ничего не найдено")
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.retry() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.isOffline) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Автономный режим - данные из кэша",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    LazyColumn {
                        items(state.countries, key = { it.id }) { country ->
                            CountryCard(
                                country = country,
                                isFavourite = favouriteIds.contains(country.id),
                                onFavouriteClick = { viewModel.toggleFavourite(country) },
                                onClick = {
                                    navController.navigate("detail/${country.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryCard(
    country: CountryEntity,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(country.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    country.capital ?: "Нет данных",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onFavouriteClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavourite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (isFavourite)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}