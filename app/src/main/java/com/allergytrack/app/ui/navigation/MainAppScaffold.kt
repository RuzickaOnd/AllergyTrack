package com.allergytrack.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allergytrack.app.ui.components.BottomNavBar
import com.allergytrack.app.ui.screens.analysis.AnalysisViewModel
import com.allergytrack.app.ui.screens.dashboard.DashboardViewModel
import com.allergytrack.app.ui.screens.debug.DebugMenuViewModel
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Sdílené ViewModely pro akce v TopAppBar (pokud jsou potřeba)
    // Poznámka: V čisté architektuře by TopAppBar mohl mít vlastní ViewModel, 
    // ale pro jednoduchost a efektivitu zde přistupujeme k těm, které potřebují interakci z Top Baru.
    
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    
    val analysisViewModel: AnalysisViewModel = hiltViewModel()
    val analysisState by analysisViewModel.uiState.collectAsStateWithLifecycle()
    val isExportVisible by analysisViewModel.isExportVisible.collectAsStateWithLifecycle()
    
    val debugViewModel: DebugMenuViewModel = hiltViewModel()

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
        containerColor = Background,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Column {
                            val title = when {
                                currentRoute == Screen.Dashboard.route -> dashboardState.greeting
                                currentRoute == Screen.Charts.route -> "Analytika"
                                currentRoute == Screen.Analysis.route -> "AI Analýza"
                                currentRoute == Screen.Profile.route -> "Profil"
                                currentRoute == Screen.FoodLog.route -> "Zalogovat jídlo"
                                currentRoute == Screen.SymptomLog.route -> "Zalogovat příznak"
                                currentRoute == Screen.Settings.route -> "Nastavení"
                                currentRoute == Screen.About.route -> "O aplikaci"
                                currentRoute == Screen.DebugMenu.route -> "Debug Menu"
                                currentRoute?.startsWith("food_confirm") == true -> "Potvrdit jídlo"
                                currentRoute == Screen.FoodManualSearch.route -> "Hledat potravinu"
                                else -> ""
                            }
                            Text(title, fontFamily = SerifFontFamily)
                            
                            if (currentRoute == Screen.Charts.route) {
                                Text(
                                    "Trendy a environmentální korelace",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDim,
                                    fontFamily = MonoFontFamily
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (!showBottomBar) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                            }
                        }
                    },
                    actions = {
                        when (currentRoute) {
                            Screen.Analysis.route -> {
                                if (isExportVisible && analysisState.hasEnoughData) {
                                    IconButton(onClick = analysisViewModel::exportReport) {
                                        if (analysisState.isExporting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = AccentGreen
                                            )
                                        } else {
                                            Icon(Icons.Default.Share, "Exportovat report", tint = TextPrimary)
                                        }
                                    }
                                }
                            }
                            Screen.DebugMenu.route -> {
                                IconButton(onClick = { debugViewModel.resetAll() }) {
                                    Icon(Icons.Default.Refresh, "Resetovat vše")
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Background,
                        titleContentColor = TextPrimary,
                        navigationIconContentColor = TextPrimary
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
