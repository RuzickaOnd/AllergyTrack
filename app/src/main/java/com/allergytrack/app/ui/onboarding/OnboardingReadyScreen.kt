package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes

@Composable
fun OnboardingReadyContent(
    uiState: OnboardingUiState,
) {
    val permissions = uiState.permissionsState
    val context = LocalContext.current

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
            modifier = Modifier.fillMaxWidth()
        )

        // Settings Summary
        AppCard(borderColor = MaterialTheme.colorScheme.outline) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.ready_settings_title), icon = Icons.Outlined.Tune)

                ReadySummaryItem(
                    label = stringResource(R.string.ready_allergies_label),
                    value = if (uiState.selectedAllergyTypes.isEmpty())
                        stringResource(R.string.ready_allergies_not_set)
                    else
                        uiState.selectedAllergyTypes.joinToString(", ") { context.getString(it.labelRes()) }
                )

                ReadySummaryItem(
                    label = stringResource(R.string.ready_symptoms_label),
                    value = if (uiState.selectedSymptomTypes.isEmpty())
                        stringResource(R.string.ready_symptoms_not_set)
                    else
                        uiState.selectedSymptomTypes.joinToString(", ") { context.getString(it.labelRes()) }
                )

                ReadyPermissionItem(
                    label = stringResource(R.string.ready_location_label),
                    isGranted = permissions.location
                )

                ReadyPermissionItem(
                    label = stringResource(R.string.ready_camera_label),
                    isGranted = permissions.camera
                )

                ReadyPermissionItem(
                    label = stringResource(R.string.ready_notifications_label),
                    isGranted = permissions.notifications
                )
            }
        }

        // Workflow tips
        AppCard(borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.ready_first_day_title), icon = Icons.Outlined.PlayArrow)

                WorkflowTipItem(Icons.Outlined.Restaurant, stringResource(R.string.ready_tip_breakfast))
                WorkflowTipItem(Icons.Outlined.Healing, stringResource(R.string.ready_tip_feeling))
                WorkflowTipItem(Icons.Outlined.BarChart, stringResource(R.string.ready_tip_environment))
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
private fun ReadyPermissionItem(label: String, isGranted: Boolean) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(if (isGranted) R.string.ready_status_granted else R.string.ready_status_denied),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun WorkflowTipItem(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
