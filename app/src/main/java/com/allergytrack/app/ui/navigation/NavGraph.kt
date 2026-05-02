package com.allergytrack.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.ui.components.PermissionDeniedScreen
import com.allergytrack.app.ui.components.RequestCameraPermission
import com.allergytrack.app.ui.screens.analysis.AnalysisScreen
import com.allergytrack.app.ui.screens.charts.ChartsScreen
import com.allergytrack.app.ui.screens.dashboard.DashboardScreen
import com.allergytrack.app.ui.screens.food.CameraScreen
import com.allergytrack.app.ui.screens.food.FoodConfirmScreen
import com.allergytrack.app.ui.screens.food.FoodLogScreen
import com.allergytrack.app.ui.screens.food.FoodManualEntryScreen
import com.allergytrack.app.ui.screens.food.FoodManualSearchScreen
import com.allergytrack.app.ui.screens.profile.AboutScreen
import com.allergytrack.app.ui.screens.profile.ProfileScreen
import com.allergytrack.app.ui.screens.debug.DebugMenuScreen
import com.allergytrack.app.ui.screens.settings.SettingsScreen
import com.allergytrack.app.ui.screens.symptom.SymptomLogScreen

@Composable
fun AllergyNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.FoodLog.route) {
            FoodLogScreen(navController = navController)
        }
        composable(
            route = Screen.Camera.route,
            arguments = listOf(navArgument(Screen.Camera.ARG_MODE) { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(Screen.Camera.ARG_MODE) ?: "photo"
            RequestCameraPermission(
                onGranted = { CameraScreen(navController = navController, mode = mode) },
                onDenied = {
                    PermissionDeniedScreen(
                        message = "Kamera je potřeba pro skenování",
                        onBack = { navController.popBackStack() }
                    )
                }
            )
        }
        composable(
            route = Screen.FoodConfirm.route,
            arguments = listOf(
                navArgument("foodName") { type = NavType.StringType },
                navArgument("allergens") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            FoodConfirmScreen(
                navController = navController,
                foodName = backStackEntry.arguments?.getString("foodName") ?: "",
                allergens = backStackEntry.arguments?.getString("allergens") ?: ""
            )
        }
        composable(
            route = Screen.FoodManualSearch.route,
            arguments = listOf(navArgument(Screen.FoodManualSearch.ARG_QUERY) {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            FoodManualSearchScreen(
                navController = navController,
                initialQuery = backStackEntry.arguments?.getString(Screen.FoodManualSearch.ARG_QUERY) ?: ""
            )
        }
        composable(
            route = Screen.FoodManualEntry.route,
            arguments = listOf(navArgument("foodName") { type = NavType.StringType })
        ) { backStackEntry ->
            FoodManualEntryScreen(
                navController = navController,
                initialName = backStackEntry.arguments?.getString("foodName") ?: ""
            )
        }
        composable(Screen.SymptomLog.route) {
            SymptomLogScreen(navController = navController)
        }
        composable(Screen.Charts.route) {
            ChartsScreen(navController = navController)
        }
        composable(Screen.Analysis.route) {
            AnalysisScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        if (BuildConfig.DEBUG) {
            composable(Screen.DebugMenu.route) {
                DebugMenuScreen(navController = navController)
            }
        }
    }
}
