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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.AllergyType
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.domain.model.SymptomTypeOnboarding
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.labelRes

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingProfileScreen(
    uiState: OnboardingUiState,
    onAllergyTypeToggled: (AllergyType) -> Unit,
    onSymptomTypeToggled: (SymptomTypeOnboarding) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_profile_title), fontFamily = SerifFontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Subtitle
                Text(
                    text = stringResource(R.string.onboarding_profile_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Allergy Types
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_allergies_heading),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    label = stringResource(type.labelRes()),
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
                        text = stringResource(R.string.onboarding_symptoms_heading),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    label = stringResource(type.labelRes()),
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
                        text = if (isAnySelected) stringResource(R.string.btn_continue) else stringResource(R.string.btn_skip),
                        onClick = onNext
                    )
                }
            }
        }
    }
}
