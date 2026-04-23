package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*

@Composable
fun OnboardingReadyScreen(
    uiState: OnboardingUiState,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val permissions = uiState.permissionsState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            SectionHeading(
                title = "You're All Set!",
                subtitle = "In 14 days of logging, AI will reveal your first patterns and potential allergy triggers."
            )

            // Large Success Icon
            Text(
                text = "✨",
                fontSize = 80.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Settings Summary
            AppCard(borderColor = Border) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTitle("YOUR SETUP")

                    ReadySummaryItem(
                        label = "Allergy Types",
                        value = if (uiState.selectedAllergyTypes.isEmpty())
                            "Not set"
                        else
                            uiState.selectedAllergyTypes.joinToString(" ") { it.emoji }
                    )

                    ReadySummaryItem(
                        label = "Location",
                        value = if (permissions.location) "✓ Enabled" else "✗ Disabled"
                    )

                    ReadySummaryItem(
                        label = "Camera",
                        value = if (permissions.camera) "✓ Enabled" else "✗ Disabled"
                    )

                    ReadySummaryItem(
                        label = "Notifications",
                        value = if (permissions.notifications) "✓ Enabled" else "✗ Disabled"
                    )
                }
            }

            // First Day Recommendations
            AppCard(borderColor = AccentBlue.copy(alpha = 0.3f)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTitle("YOUR FIRST DAY")
                    
                    FirstDayItem("📸", "Photograph or log your breakfast")
                    FirstDayItem("😊", "Record how you're feeling")
                    FirstDayItem("📊", "Environment is tracked automatically")
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingProgressDots(
                currentStep = OnboardingStep.READY,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OnboardingBottomBar(
                onNext = onComplete,
                onBack = onBack,
                nextLabel = "GO TO DASHBOARD →",
            )
        }
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
            color = TextSecondary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
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
            color = TextPrimary
        )
    }
}
