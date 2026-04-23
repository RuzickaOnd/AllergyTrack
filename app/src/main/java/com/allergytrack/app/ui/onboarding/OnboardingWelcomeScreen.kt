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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "PŘESKOČIT",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextDim,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
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
                text = "Najdi své\nspouštěče",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = TextPrimary,
                lineHeight = 44.sp,
                fontFamily = SerifFontFamily
            )

            Spacer(Modifier.height(16.dp))

            // Description
            Text(
                text = "AllergyTrack automaticky propojuje tvé jídlo, prostředí a symptomy, aby ti pomohl odhalit, co stojí za tvými alergiemi.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = TextSecondary,
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
                    text = "ZAČÍT →",
                    onClick = onNext,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "Není vyžadována žádná registrace",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
