package com.allergytrack.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.theme.SerifFontFamily

/**
 * Onboarding jako jeden Composable se statickým rámcem a animovaným obsahem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlow(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onCompleted()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val title = when (uiState.currentStep) {
                OnboardingStep.WELCOME -> ""
                OnboardingStep.ALLERGY_PROFILE -> stringResource(R.string.onboarding_profile_title)
                OnboardingStep.PERMISSIONS -> stringResource(R.string.onboarding_permissions_title)
                OnboardingStep.READY -> stringResource(R.string.onboarding_ready_title)
            }

            TopAppBar(
                title = { Text(title, fontFamily = SerifFontFamily) },
                actions = {
                    if (uiState.currentStep != OnboardingStep.READY) {
                        TextButton(
                            onClick = viewModel::skipOnboarding,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_skip),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        ) {
            // Animovaný obsah uprostřed
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(400)) { it / 2 } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { -it / 2 } + fadeOut(animationSpec = tween(400)))
                    },
                    label = "onboarding_content"
                ) { step ->
                    when (step) {
                        OnboardingStep.WELCOME -> OnboardingWelcomeContent()
                        OnboardingStep.ALLERGY_PROFILE -> OnboardingProfileContent(
                            uiState = uiState,
                            onAllergyTypeToggled = viewModel::onAllergyTypeToggled,
                            onSymptomTypeToggled = viewModel::onSymptomTypeToggled
                        )
                        OnboardingStep.PERMISSIONS -> OnboardingPermissionsContent(
                            uiState = uiState,
                            onPermissionsRefreshed = viewModel::refreshPermissions
                        )
                        OnboardingStep.READY -> OnboardingReadyContent(
                            uiState = uiState
                        )
                    }
                }
            }

            // Statická spodní část
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnboardingProgressDots(
                    currentStep = uiState.currentStep,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val buttonText = when (uiState.currentStep) {
                    OnboardingStep.WELCOME -> stringResource(R.string.onboarding_cta)
                    OnboardingStep.ALLERGY_PROFILE -> {
                        val isAnySelected = uiState.selectedAllergyTypes.isNotEmpty() || uiState.selectedSymptomTypes.isNotEmpty()
                        if (isAnySelected) stringResource(R.string.btn_continue) else stringResource(R.string.btn_skip)
                    }
                    OnboardingStep.PERMISSIONS -> {
                        if (uiState.permissionsState.location) stringResource(R.string.btn_continue_arrow) 
                        else stringResource(R.string.btn_continue_no_location)
                    }
                    OnboardingStep.READY -> stringResource(R.string.btn_go_to_dashboard)
                }

                val onButtonClick = when (uiState.currentStep) {
                    OnboardingStep.READY -> viewModel::completeOnboarding
                    else -> viewModel::navigateToNext
                }

                PrimaryButton(
                    text = buttonText.uppercase(),
                    onClick = onButtonClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
