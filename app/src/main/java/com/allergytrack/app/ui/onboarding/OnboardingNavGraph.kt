package com.allergytrack.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allergytrack.app.domain.model.OnboardingStep

/**
 * Onboarding jako jeden Composable se step-based navigací.
 * Nepoužíváme NavHost — kroky jsou jednoduché a animujeme je ručně.
 *
 * @param onCompleted Callback volaný po dokončení onboardingu → přejde na hlavní app
 */
@Composable
fun OnboardingFlow(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Přejdi na hlavní app po dokončení
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onCompleted()
    }

    // Animovaný přechod mezi kroky
    AnimatedContent(
        targetState = uiState.currentStep,
        transitionSpec = {
            // Vpřed: slide zleva, vzad: slide zprava
            val goingForward = targetState.ordinal > initialState.ordinal
            if (goingForward) {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            }
        },
        contentAlignment = Alignment.Center,
        label = "onboarding_step"
    ) { step ->
        when (step) {
            OnboardingStep.WELCOME -> OnboardingWelcomeScreen(
                onNext = viewModel::navigateToNext,
                onSkip = viewModel::skipOnboarding
            )
            OnboardingStep.ALLERGY_PROFILE -> OnboardingProfileScreen(
                uiState = uiState,
                onAllergyTypeToggled = viewModel::onAllergyTypeToggled,
                onSymptomTypeToggled = viewModel::onSymptomTypeToggled,
                onNext = viewModel::navigateToNext,
                onBack = viewModel::navigateToPrevious
            )
            OnboardingStep.PERMISSIONS -> OnboardingPermissionsScreen(
                uiState = uiState,
                onPermissionsRefreshed = viewModel::refreshPermissions,
                onNext = viewModel::navigateToNext,
                onBack = viewModel::navigateToPrevious
            )
            OnboardingStep.READY -> OnboardingReadyScreen(
                uiState = uiState,
                onComplete = viewModel::completeOnboarding,
                onBack = viewModel::navigateToPrevious
            )
        }
    }
}
