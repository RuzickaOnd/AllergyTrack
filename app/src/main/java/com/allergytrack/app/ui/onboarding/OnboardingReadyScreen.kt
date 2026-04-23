package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingReadyScreen(
    uiState: OnboardingUiState,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val permissions = uiState.permissionsState

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Vše je připraveno!", fontFamily = SerifFontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Subtitle
                Text(
                    text = "Během 14 dnů logování AI odhalí tvé první vzorce a potenciální spouštěče alergií.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    textAlign = TextAlign.Center
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
                        CardTitle("TVÉ NASTAVENÍ")

                        ReadySummaryItem(
                            label = "Typy alergií",
                            value = if (uiState.selectedAllergyTypes.isEmpty())
                                "Nenastaveno"
                            else
                                uiState.selectedAllergyTypes.joinToString(" ") { it.emoji }
                        )

                        ReadySummaryItem(
                            label = "Poloha",
                            value = if (permissions.location) "✓ Povoleno" else "✗ Zakázáno"
                        )

                        ReadySummaryItem(
                            label = "Fotoaparát",
                            value = if (permissions.camera) "✓ Povoleno" else "✗ Zakázáno"
                        )

                        ReadySummaryItem(
                            label = "Oznámení",
                            value = if (permissions.notifications) "✓ Povoleno" else "✗ Zakázáno"
                        )
                    }
                }

                // First Day Recommendations
                AppCard(borderColor = AccentBlue.copy(alpha = 0.3f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CardTitle("TVŮJ PRVNÍ DEN")
                        
                        FirstDayItem("📸", "Vyfoť nebo zapiš svou snídani")
                        FirstDayItem("😊", "Zaznamenej, jak se cítíš")
                        FirstDayItem("📊", "Prostředí se sleduje automaticky")
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
                    nextLabel = "PŘEJÍT NA PŘEHLED →",
                )
            }
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
