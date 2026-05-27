package com.allergytrack.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
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
import com.allergytrack.app.ui.screens.analysis.AnalysisViewModel
import com.allergytrack.app.ui.screens.dashboard.DashboardViewModel
import com.allergytrack.app.ui.screens.debug.DebugMenuViewModel
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.AnalysisExportHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(appUpdateRepository: com.allergytrack.app.domain.repository.AppUpdateRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val analysisViewModel: AnalysisViewModel = hiltViewModel()
    val analysisState by analysisViewModel.uiState.collectAsStateWithLifecycle()
    val isExportVisible by analysisViewModel.isExportVisible.collectAsStateWithLifecycle()
    val showExportUpsell by analysisViewModel.showExportUpsell.collectAsStateWithLifecycle()

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

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = if (showBottomBar) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
    } else {
        NavigationSuiteType.None
    }

    val navSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationRailContainerColor = MaterialTheme.colorScheme.surface
        ),
        navigationSuiteItems = {
            val items = listOf(
                Triple(Screen.Dashboard, R.string.nav_dashboard, Icons.Outlined.Home to Icons.Filled.Home),
                Triple(Screen.Charts,    R.string.nav_charts,    Icons.Outlined.Analytics to Icons.Filled.Analytics),
                Triple(Screen.Analysis,  R.string.nav_analysis,  Icons.Outlined.AutoAwesome to Icons.Filled.AutoAwesome),
                Triple(Screen.Profile,   R.string.nav_profile,   Icons.Outlined.Person to Icons.Filled.Person),
            )
            items.forEach { (screen, labelRes, icons) ->
                val (outlinedIcon, filledIcon) = icons
                val selected = currentRoute == screen.route
                item(
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) filledIcon else outlinedIcon,
                            contentDescription = stringResource(labelRes)
                        )
                    },
                    label = {
                        Text(stringResource(labelRes))
                    },
                    colors = navSuiteItemColors
                )
            }
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val isUpdateDownloaded by appUpdateRepository.isUpdateDownloaded.collectAsStateWithLifecycle(initialValue = false)
        val updateMessage = stringResource(R.string.update_downloaded_message)
        val updateActionLabel = stringResource(R.string.update_action_restart)

        LaunchedEffect(isUpdateDownloaded) {
            if (isUpdateDownloaded) {
                val result = snackbarHostState.showSnackbar(
                    message = updateMessage,
                    actionLabel = updateActionLabel,
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    appUpdateRepository.completeUpdate()
                }
            }
        }

        val contentWindowInsets = if (showBottomBar) {
            WindowInsets.safeDrawing.exclude(WindowInsets.navigationBars)
        } else {
            WindowInsets.safeDrawing
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = contentWindowInsets,
            snackbarHost = {
                com.allergytrack.app.ui.components.AppSnackbarHost(
                    hostState = snackbarHostState
                )
            },
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
                                    currentRoute == Screen.Faq.route -> stringResource(R.string.screen_faq)
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
                                    if (isExportVisible && !showExportUpsell && analysisState.readiness !is AnalysisReadiness.Locked) {
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
            }
        ) { innerPadding ->
            AllergyNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
