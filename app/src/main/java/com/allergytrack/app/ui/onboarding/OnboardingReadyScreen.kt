package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*

@Composable
fun OnboardingReadyContent(
    uiState: OnboardingUiState,
) {
    val permissions = uiState.permissionsState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Subtitle
        Text(
            text = stringResource(R.string.onboarding_ready_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Large Success Icon
        Text(
            text = "✨",
            fontSize = 80.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Settings Summary
        AppCard(borderColor = MaterialTheme.colorScheme.outline) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.ready_settings_title))

                ReadySummaryItem(
                    label = stringResource(R.string.ready_allergies_label),
                    value = if (uiState.selectedAllergyTypes.isEmpty())
                        stringResource(R.string.ready_allergies_not_set)
                    else
                        uiState.selectedAllergyTypes.joinToString(" ") { it.emoji }
                )

                ReadySummaryItem(
                    label = stringResource(R.string.ready_location_label),
                    value = if (permissions.location) stringResource(R.string.ready_status_granted) else stringResource(R.string.ready_status_denied)
                )

                ReadySummaryItem(
                    label = stringResource(R.string.ready_camera_label),
                    value = if (permissions.camera) stringResource(R.string.ready_status_granted) else stringResource(R.string.ready_status_denied)
                )

                ReadySummaryItem(
                    label = stringResource(R.string.ready_notifications_label),
                    value = if (permissions.notifications) stringResource(R.string.ready_status_granted) else stringResource(R.string.ready_status_denied)
                )
            }
        }

        // First Day Recommendations
        AppCard(borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.ready_first_day_title))

                FirstDayItem("📸", stringResource(R.string.ready_tip_breakfast))
                FirstDayItem("😊", stringResource(R.string.ready_tip_feeling))
                FirstDayItem("📊", stringResource(R.string.ready_tip_environment))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReadySummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FirstDayItem(emoji: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
