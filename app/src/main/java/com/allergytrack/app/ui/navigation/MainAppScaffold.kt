package com.allergytrack.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.allergytrack.app.ui.components.BottomNavBar

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        AllergyNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
