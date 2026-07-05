package com.lifelen.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifelen.feature.history.HistoryRoute
import com.lifelen.feature.results.ResultsRoute
import com.lifelen.feature.scanner.ScannerRoute
import com.lifelen.feature.settings.SettingsRoute

/** Central navigation graph. Features expose route composables; the app wires them together. */
object Routes {
    const val SCANNER = "scanner"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val RESULTS = "results/{scanId}"
    fun results(scanId: String) = "results/$scanId"
}

@Composable
fun LifeLensNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SCANNER) {
        composable(Routes.SCANNER) {
            ScannerRoute(
                onScanComplete = { scanId -> navController.navigate(Routes.results(scanId)) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.RESULTS,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType }),
        ) {
            ResultsRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryRoute(
                onScanClick = { scanId -> navController.navigate(Routes.results(scanId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
