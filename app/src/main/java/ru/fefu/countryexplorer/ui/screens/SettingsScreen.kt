package ru.fefu.countryexplorer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ru.fefu.countryexplorer.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isOfflinePreload by viewModel.isOfflinePreload.collectAsStateWithLifecycle()
    val historyRetention by viewModel.historyRetentionDays.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ListItem(
                headlineContent = { Text("Тёмная тема") },
                supportingContent = { Text("Использовать тёмную тему оформления") },
                trailingContent = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) }
                    )
                }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Офлайн-предзагрузка") },
                supportingContent = { Text("Автоматически обновлять кэш стран") },
                trailingContent = {
                    Switch(
                        checked = isOfflinePreload,
                        onCheckedChange = { viewModel.setOfflinePreload(it) }
                    )
                }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Автоочистка истории") },
                supportingContent = { Text("Хранить историю $historyRetention дней") },
                trailingContent = {
                    Slider(
                        value = historyRetention.toFloat(),
                        onValueChange = { viewModel.setHistoryRetention(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 29,
                        modifier = Modifier.width(150.dp)
                    )
                }
            )
        }
    }
}