package ru.fefu.countryexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.fefu.countryexplorer.ui.screens.*
import ru.fefu.countryexplorer.ui.viewmodel.SettingsViewModel
import ru.fefu.countryexplorer.ui.theme.CountryExplorerTheme

@Composable
fun AppNavigation() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState(false)

    CountryExplorerTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "list"
        ) {
            composable("list") {
                CountryListScreen(navController)
            }
            composable(
                "detail/{countryId}",
                arguments = listOf(navArgument("countryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
                CountryDetailScreen(countryId, navController)
            }
            composable("favourites") {
                FavouritesScreen(navController)
            }
            composable("history") {
                HistoryScreen(navController)
            }
            composable("notes") {
                PersonalNotesScreen(navController)
            }
            composable("settings") {
                SettingsScreen(navController)
            }
        }
    }
}