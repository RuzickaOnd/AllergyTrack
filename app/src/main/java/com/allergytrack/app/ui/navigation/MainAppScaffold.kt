package com.allergytrack.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allergytrack.app.domain.model.AnalysisReadiness
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.BottomNavBar
import com.allergytrack.app.ui.screens.analysis.AnalysisViewModel
import com.allergytrack.app.ui.screens.dashboard.DashboardViewModel
import com.allergytrack.app.ui.screens.debug.DebugMenuViewModel
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.AnalysisExportHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val analysisViewModel: AnalysisViewModel = hiltViewModel()
    val analysisState by analysisViewModel.uiState.collectAsStateWithLifecycle()
    val isExportVisible by analysisViewModel.isExportVisible.collectAsStateWithLifecycle()

    val debugViewModel: DebugMenuViewModel = hiltViewModel()

    // Unifikované zpracování exportu (funguje pro TopBar i AnalysisScreen)
    AnalysisExportHandler(viewModel = analysisViewModel)

    val showBottomBar = when (currentRoute) {
        Screen.Dashboard.route,
        Screen.Charts.route,
        Screen.Analysis.route,
        Screen.Profile.route -> true
        else -> false
    }

    val showTopBar = currentRoute != Screen.Camera.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Column {
                            val title = when {
                                currentRoute == Screen.Dashboard.route -> stringResource(dashboardState.greetingRes)
                                currentRoute == Screen.Charts.route -> stringResource(R.string.screen_analytics)
                                currentRoute == Screen.Analysis.route -> stringResource(R.string.screen_ai_analysis)
                                currentRoute == Screen.Profile.route -> stringResource(R.string.screen_profile)
                                currentRoute == Screen.FoodLog.route -> stringResource(R.string.screen_log_food)
                                currentRoute == Screen.SymptomLog.route -> stringResource(R.string.screen_log_symptom)
                                currentRoute == Screen.Settings.route -> stringResource(R.string.screen_settings)
                                currentRoute == Screen.About.route -> stringResource(R.string.screen_about)
                                currentRoute == Screen.DebugMenu.route -> stringResource(R.string.screen_debug_menu)
                                currentRoute?.startsWith("food_confirm") == true -> stringResource(R.string.screen_confirm_food)
                                currentRoute == Screen.FoodManualSearch.route -> stringResource(R.string.screen_search_food)
                                else -> ""
                            }
                            Text(title, fontFamily = SerifFontFamily)

                            if (currentRoute == Screen.Charts.route) {
                                Text(
                                    stringResource(R.string.subtitle_charts),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = MonoFontFamily
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (!showBottomBar) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                            }
                        }
                    },
                    actions = {
                        when (currentRoute) {
                            Screen.Analysis.route -> {
                                if (isExportVisible && analysisState.readiness !is AnalysisReadiness.Locked) {
                                    IconButton(onClick = analysisViewModel::exportReport) {
                                        if (analysisState.isExporting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(Icons.Default.Share, stringResource(R.string.cd_export_report), tint = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                            Screen.DebugMenu.route -> {
                                IconButton(onClick = { debugViewModel.resetAll() }) {
                                    Icon(Icons.Default.Refresh, stringResource(R.string.cd_reset_all))
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        AllergyNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
