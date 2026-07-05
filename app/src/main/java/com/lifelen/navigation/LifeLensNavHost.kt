package com.lifelen.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifelen.feature.history.LibraryRoute
import com.lifelen.feature.prices.PricesRoute
import com.lifelen.feature.results.ResultRoute
import com.lifelen.feature.scanner.ScannerRoute
import com.lifelen.feature.settings.SettingsRoute

/**
 * Navigation graph. Camera is home; result/prices/library/settings are pushed. The result screen
 * doubles as the fresh-scan sheet (scanId = "current", read from the in-memory ScanSession) and the
 * saved-item detail (scanId = a library id). Back always returns toward the camera (Spec §5).
 */
object Routes {
    const val CAMERA = "camera"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val RESULT = "result?scanId={scanId}"
    const val PRICES = "prices?scanId={scanId}"

    fun result(scanId: String = "current") = "result?scanId=$scanId"
    fun prices(scanId: String) = "prices?scanId=$scanId"
}

@Composable
fun LifeLensNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.CAMERA) {
        composable(Routes.CAMERA) {
            ScannerRoute(
                onNavigateToResult = { navController.navigate(Routes.result()) },
                onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType; defaultValue = "current" },
            ),
        ) {
            ResultRoute(
                onBack = { navController.popBackStack() },
                onOpenPrices = { scanId -> navController.navigate(Routes.prices(scanId)) },
            )
        }
        composable(
            route = Routes.PRICES,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType; defaultValue = "current" },
            ),
        ) {
            PricesRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.LIBRARY) {
            LibraryRoute(
                onBack = { navController.popBackStack() },
                onOpenScan = { scanId -> navController.navigate(Routes.result(scanId)) },
                onNewScan = { navController.popBackStack(Routes.CAMERA, inclusive = false) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
