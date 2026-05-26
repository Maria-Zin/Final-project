package ru.fefu.countryexplorer.ui.screens

import androidx.compose.foundation.layout.*
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
import ru.fefu.countryexplorer.data.local.NoteEntity
import ru.fefu.countryexplorer.ui.viewmodel.CountryViewModel
import ru.fefu.countryexplorer.ui.viewmodel.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    countryId: String,
    navController: NavHostController,
    viewModel: CountryViewModel = hiltViewModel()
) {
    LaunchedEffect(countryId) {
        viewModel.loadCountryDetail(countryId)
    }

    val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
    var showNoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (detailState is DetailUiState.Success)
                        (detailState as DetailUiState.Success).country.name
                    else
                        "Детали"
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
            when (val state = detailState) {
                is DetailUiState.Loading -> CircularProgressIndicator()
                is DetailUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCountryDetail(countryId) }) {
                            Text("Повторить")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    val country = state.country
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(country.name, style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                InfoRow("Столица", country.capital ?: "Нет данных")
                                InfoRow("Регион", country.region)
                                InfoRow("Население", formatPopulation(country.population))
                                InfoRow("Площадь", "${country.area ?: "Нет данных"} км²")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        state.note?.let { note ->
                            NoteCard(
                                note = note,
                                onEditClick = { showNoteDialog = true }
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        if (state.note == null) {
                            OutlinedButton(
                                onClick = { showNoteDialog = true },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Добавить заметку")
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        Button(
                            onClick = { viewModel.toggleFavourite(country) },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(
                                if (state.isFavourite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isFavourite) "Удалить из избранного" else "В избранное")
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog && detailState is DetailUiState.Success) {
        val country = (detailState as DetailUiState.Success).country
        val currentNote = (detailState as DetailUiState.Success).note

        NoteDialog(
            currentNote = currentNote?.text ?: "",
            countryName = country.name,
            onSave = { text ->
                viewModel.saveNote(country.id, text)
                showNoteDialog = false
            },
            onDelete = {
                viewModel.deleteNote(country.id)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Моя заметка",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                TextButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                note.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Изменено: ${formatDate(note.lastEdited)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun NoteDialog(
    currentNote: String,
    countryName: String,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(currentNote) { mutableStateOf(currentNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (currentNote.isEmpty()) "Добавить заметку" else "Редактировать заметку")
        },
        text = {
            Column {
                Text(
                    "Страна: $countryName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Введите ваши мысли о стране...") },
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Row {
                if (currentNote.isNotEmpty()) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Удалить")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(text.trim())
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun formatPopulation(population: Long): String {
    return when {
        population >= 1_000_000_000 -> "${population / 1_000_000_000}.${(population % 1_000_000_000) / 100_000_000} млрд"
        population >= 1_000_000 -> "${population / 1_000_000}.${(population % 1_000_000) / 100_000} млн"
        population >= 1_000 -> "${population / 1_000}.${(population % 1_000) / 100} тыс"
        else -> population.toString()
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}