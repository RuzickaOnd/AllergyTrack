package com.allergytrack.app.ui.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.data.local.datastore.DebugPremiumOverride
import com.allergytrack.app.domain.model.FoodLog
import com.allergytrack.app.domain.model.SymptomLog
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.domain.repository.FoodRepository
import com.allergytrack.app.domain.repository.SymptomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DebugMenuViewModel @Inject constructor(
    private val debugPreferences: DebugPreferences,
    private val onboardingDataStore: OnboardingDataStore,
    private val foodRepository: FoodRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    val premiumOverride: StateFlow<DebugPremiumOverride> = debugPreferences.premiumOverride
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DebugPremiumOverride.USE_APP_STATE
        )

    val symptomCountOverride: StateFlow<Int> = debugPreferences.symptomCountOverride
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )

    fun setPremiumOverride(override: DebugPremiumOverride) {
        viewModelScope.launch {
            debugPreferences.setPremiumOverride(override)
        }
    }

    fun setSymptomCountOverride(count: Int) {
        viewModelScope.launch {
            debugPreferences.setSymptomCountOverride(count)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            debugPreferences.resetAll()
        }
    }

    fun seedTestData() {
        // ... (existing code)
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            onboardingDataStore.reset()
        }
    }
}
