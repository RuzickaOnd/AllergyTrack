package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.4f))

            // Large Decorative Icon
            Text(
                text = "🌿",
                fontSize = 96.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 44.sp,
                fontFamily = SerifFontFamily
            )

            Spacer(Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.weight(0.6f))

            // CTA Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnboardingProgressDots(
                    currentStep = OnboardingStep.WELCOME,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                PrimaryButton(
                    text = stringResource(R.string.onboarding_cta),
                    onClick = onNext,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = stringResource(R.string.onboarding_no_registration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
