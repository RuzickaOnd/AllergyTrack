package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.theme.*

@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip button
        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "SKIP",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextDim,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.weight(0.4f))

        // Large Decorative Icon
        Text(
            text = "🌿",
            fontSize = 96.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Title
        Text(
            text = "Identify Your\nTriggers",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = TextPrimary,
            lineHeight = 44.sp
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text(
            text = "AllergyTrack automatically connects your food, environment, and symptoms to help you discover what's behind your allergies.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.weight(0.6f))

        // CTA Section
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgressDots(
                currentStep = OnboardingStep.WELCOME,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PrimaryButton(
                text = "GET STARTED →",
                onClick = onNext,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "No registration required",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                letterSpacing = 0.5.sp
            )
        }
    }
}
