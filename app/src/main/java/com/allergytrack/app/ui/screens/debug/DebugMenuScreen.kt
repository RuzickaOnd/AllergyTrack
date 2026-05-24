package com.allergytrack.app.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
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
import com.allergytrack.app.domain.model.PremiumVisibilityMode
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun DebugMenuScreen(
    navController: NavController,
    viewModel: DebugMenuViewModel = hiltViewModel()
) {
    val premiumOverride by viewModel.premiumOverride.collectAsState()
    val currentMode = viewModel.currentVisibilityMode

    DisposableEffect(Unit) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        onDispose {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

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

        // Info chip: aktuální mód nastavený v PremiumHelper.kt
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (currentMode == PremiumVisibilityMode.HIDDEN)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (currentMode == PremiumVisibilityMode.HIDDEN)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
                val modeLabel = if (currentMode == PremiumVisibilityMode.HIDDEN) "HIDDEN" else "VISIBLE"
                Text(
                    "visibilityMode v PremiumHelper.kt: $modeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (currentMode == PremiumVisibilityMode.HIDDEN)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                analysisOptions.forEach { (count, label) ->
                    val isSelected = symptomCountOverride == count
                    Surface(
                        onClick = { viewModel.setSymptomCountOverride(count) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setSymptomCountOverride(count) }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

        Text(
            stringResource(R.string.debug_seed_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val seedingInProgress by viewModel.seedingInProgress.collectAsState()

        if (seedingInProgress) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.debug_seed_in_progress),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.seedTestData(czech = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.debug_seed_cz))
                }
                OutlinedButton(
                    onClick = { viewModel.seedTestData(czech = false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.debug_seed_en))
                }
            }
        }

        Button(
            onClick = { throw RuntimeException("Test Crash") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.debug_crash_app))
        }

        OutlinedButton(
            onClick = {
                FirebaseCrashlytics.getInstance().recordException(
                    RuntimeException("Test non-fatal exception z Debug menu")
                )
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.debug_nonfatal_test))
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
