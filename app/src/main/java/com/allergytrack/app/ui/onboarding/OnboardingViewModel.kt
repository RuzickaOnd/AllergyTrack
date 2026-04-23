package com.allergytrack.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.permissions.PermissionsState
import com.allergytrack.app.domain.model.AllergyType
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.domain.model.SymptomTypeOnboarding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedAllergyTypes: Set<AllergyType> = emptySet(),
    val selectedSymptomTypes: Set<SymptomTypeOnboarding> = emptySet(),
    val permissionsState: PermissionsState = PermissionsState(
        location = false,
        camera = false,
        notifications = false
    ),
    val isCompleting: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore,
    private val permissionsHelper: PermissionsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun onAllergyTypeToggled(type: AllergyType) {
        _uiState.update { state ->
            val current = state.selectedAllergyTypes.toMutableSet()
            if (type in current) current.remove(type) else current.add(type)
            state.copy(selectedAllergyTypes = current)
        }
    }

    fun onSymptomTypeToggled(type: SymptomTypeOnboarding) {
        _uiState.update { state ->
            val current = state.selectedSymptomTypes.toMutableSet()
            if (type in current) current.remove(type) else current.add(type)
            state.copy(selectedSymptomTypes = current)
        }
    }

    fun refreshPermissions() {
        _uiState.update { it.copy(permissionsState = permissionsHelper.getPermissionsState()) }
    }

    fun navigateToNext() {
        val current = _uiState.value.currentStep
        val next = when (current) {
            OnboardingStep.WELCOME -> OnboardingStep.ALLERGY_PROFILE
            OnboardingStep.ALLERGY_PROFILE -> OnboardingStep.PERMISSIONS
            OnboardingStep.PERMISSIONS -> OnboardingStep.READY
            OnboardingStep.READY -> null
        }
        if (next != null) {
            _uiState.update { it.copy(currentStep = next) }
        }
    }

    fun navigateToPrevious() {
        val current = _uiState.value.currentStep
        val previous = when (current) {
            OnboardingStep.WELCOME -> null
            OnboardingStep.ALLERGY_PROFILE -> OnboardingStep.WELCOME
            OnboardingStep.PERMISSIONS -> OnboardingStep.ALLERGY_PROFILE
            OnboardingStep.READY -> OnboardingStep.PERMISSIONS
        }
        if (previous != null) {
            _uiState.update { it.copy(currentStep = previous) }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }
            val state = _uiState.value
            onboardingDataStore.saveAllergyProfile(
                allergyTypes = state.selectedAllergyTypes.map { it.name }.toSet(),
                symptomTypes = state.selectedSymptomTypes.map { it.name }.toSet()
            )
            onboardingDataStore.setCompleted()
            _uiState.update { it.copy(isCompleting = false, isCompleted = true) }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            onboardingDataStore.setCompleted()
            _uiState.update { it.copy(isCompleted = true) }
        }
    }
}
