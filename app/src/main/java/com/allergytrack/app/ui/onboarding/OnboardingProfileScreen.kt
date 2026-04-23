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
import androidx.compose.ui.unit.dp
import com.allergytrack.app.domain.model.AllergyType
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.domain.model.SymptomTypeOnboarding
import com.allergytrack.app.ui.components.SectionHeading
import com.allergytrack.app.ui.theme.Background
import com.allergytrack.app.ui.theme.TextDim

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileScreen(
    uiState: OnboardingUiState,
    onAllergyTypeToggled: (AllergyType) -> Unit,
    onSymptomTypeToggled: (SymptomTypeOnboarding) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            SectionHeading(
                title = "Allergy Profile",
                subtitle = "This helps us tailor your insights and focus on what matters most for you."
            )

            // Allergy Types
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "WHICH ALLERGIES DO YOU HAVE?",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    fontWeight = FontWeight.Bold
                )
                
                // We use a Grid for OnboardingSelectCard
                val rows = AllergyType.entries.chunked(3)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { type ->
                            OnboardingSelectCard(
                                modifier = Modifier.weight(1f),
                                emoji = type.emoji,
                                label = type.label,
                                isSelected = type in uiState.selectedAllergyTypes,
                                onClick = { onAllergyTypeToggled(type) }
                            )
                        }
                        // Fill empty spots if row is not full
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Symptoms
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "WHAT ARE YOUR USUAL SYMPTOMS?",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    fontWeight = FontWeight.Bold
                )
                
                val rows = SymptomTypeOnboarding.entries.chunked(3)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { type ->
                            OnboardingSelectCard(
                                modifier = Modifier.weight(1f),
                                emoji = type.emoji,
                                label = type.label,
                                isSelected = type in uiState.selectedSymptomTypes,
                                onClick = { onSymptomTypeToggled(type) }
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardingProgressDots(
                    currentStep = OnboardingStep.ALLERGY_PROFILE,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                val isAnySelected = uiState.selectedAllergyTypes.isNotEmpty() || uiState.selectedSymptomTypes.isNotEmpty()
                
                com.allergytrack.app.ui.components.PrimaryButton(
                    text = if (isAnySelected) "CONTINUE" else "SKIP FOR NOW",
                    onClick = onNext
                )
            }
        }
    }
}
