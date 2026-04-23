package com.allergytrack.app.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.data.local.datastore.DebugPremiumOverride

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    navController: NavController,
    viewModel: DebugMenuViewModel = hiltViewModel()
) {
    val premiumOverride by viewModel.premiumOverride.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetAll() }) {
                        Icon(Icons.Default.Refresh, "Reset All")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Premium Override Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DebugPremiumOverride.values().forEach { option ->
                val isSelected = option == premiumOverride
                
                Surface(
                    onClick = { viewModel.setPremiumOverride(option) },
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setPremiumOverride(option) }
                        )
                        Column {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Text(
                "Other Debug Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { viewModel.seedTestData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seed Test Data (Food & Symptoms)")
            }

            Button(
                onClick = { throw RuntimeException("Debug manual crash") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulate App Crash")
            }

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                "Onboarding",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(
                onClick = { viewModel.resetOnboarding() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Reset onboardingu (restart app)")
            }

            Text(
                "Po resetu se onboarding zobrazí při dalším spuštění aplikace.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
