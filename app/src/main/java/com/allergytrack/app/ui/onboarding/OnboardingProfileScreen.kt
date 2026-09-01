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
import androidx.compose.ui.unit.dp
import com.allergytrack.app.R
import com.allergytrack.shared.domain.model.AllergyType
import com.allergytrack.shared.domain.model.SymptomTypeOnboarding
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes

@Composable
fun OnboardingProfileContent(
    uiState: OnboardingUiState,
    onAllergyTypeToggled: (AllergyType) -> Unit,
    onSymptomTypeToggled: (SymptomTypeOnboarding) -> Unit,
) {
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
            text = stringResource(R.string.onboarding_profile_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        // Allergy Types
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.onboarding_allergies_heading),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            val rows = AllergyType.entries.chunked(3)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { type ->
                        OnboardingSelectCard(
                            modifier = Modifier.weight(1f),
                            icon = type.icon(),
                            label = stringResource(type.labelRes()),
                            isSelected = type in uiState.selectedAllergyTypes,
                            onClick = { onAllergyTypeToggled(type) }
                        )
                    }
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
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
                            icon = type.icon(),
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
}
