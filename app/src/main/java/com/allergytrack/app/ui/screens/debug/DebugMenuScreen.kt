package com.allergytrack.app.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.R
import com.allergytrack.app.data.local.datastore.DebugPremiumOverride
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun DebugMenuScreen(
    navController: NavController,
    viewModel: DebugMenuViewModel = hiltViewModel()
) {
    val premiumOverride by viewModel.premiumOverride.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.debug_premium_override_title),
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

        val symptomCountOverride by viewModel.symptomCountOverride.collectAsState()

        if (BuildConfig.DEBUG) {
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.debug_analysis_sim_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.debug_analysis_sim_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val analysisOptions = listOf(
                -1 to stringResource(R.string.debug_analysis_real_data),
                0  to stringResource(R.string.debug_analysis_0),
                3  to stringResource(R.string.debug_analysis_3),
                5  to stringResource(R.string.debug_analysis_5),
                8  to stringResource(R.string.debug_analysis_8),
                10 to stringResource(R.string.debug_analysis_10)
            )
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column {
                    analysisOptions.forEachIndexed { index, (count, label) ->
                        if (index > 0) HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = symptomCountOverride == count,
                                onClick = { viewModel.setSymptomCountOverride(count) }
                            )
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        HorizontalDivider()

        Text(
            stringResource(R.string.debug_other_options),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = { viewModel.seedTestData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.debug_seed_test_data))
        }

        Button(
            onClick = { throw RuntimeException("Manuální pád aplikace") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.debug_crash_app))
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.debug_onboarding_section),
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
            Text(stringResource(R.string.debug_reset_onboarding_btn))
        }

        Text(
            stringResource(R.string.debug_reset_onboarding_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
