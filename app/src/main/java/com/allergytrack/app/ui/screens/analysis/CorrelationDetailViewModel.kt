package com.allergytrack.app.ui.screens.analysis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.domain.analysis.CorrelationEngine
import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.shared.domain.model.TriggerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CorrelationDetailUiState(
    val correlation: Correlation? = null,
    val isLoading: Boolean = true
)

class CorrelationDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val symptomLogDao: SymptomLogDao,
    private val foodLogDao: FoodLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    private val onboardingDataStore: OnboardingDataStore,
    private val correlationEngine: CorrelationEngine
) : ViewModel() {

    private val trigger: String? = savedStateHandle["trigger"]
    private val triggerType: String? = savedStateHandle["triggerType"]
    private val symptomType: String? = savedStateHandle["symptomType"]

    private val _uiState = MutableStateFlow(CorrelationDetailUiState())
    val uiState: StateFlow<CorrelationDetailUiState> = _uiState.asStateFlow()

    init {
        loadCorrelation()
    }

    private fun loadCorrelation() {
        if (trigger == null || triggerType == null || symptomType == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                symptomLogDao.getAllLogs(),
                foodLogDao.getAllLogs(),
                environmentLogDao.getAllLogs(),
                onboardingDataStore.selectedAllergyTypes,
                onboardingDataStore.selectedSymptomTypes
            ) { symptoms, foods, env, allergies, userSymptoms ->
                val allergySet = allergies.split(",").filter { it.isNotEmpty() }.toSet()
                val symptomSet = userSymptoms.split(",").filter { it.isNotEmpty() }.toSet()

                val correlations = correlationEngine.analyze(
                    foodLogs = foods,
                    symptomLogs = symptoms,
                    environmentLogs = env,
                    knownAllergies = allergySet,
                    knownSymptoms = symptomSet
                )

                correlations.find { 
                    it.trigger == trigger && 
                    it.triggerType.name == triggerType && 
                    it.symptomType.name == symptomType 
                }
            }.collect { correlation ->
                _uiState.update { 
                    it.copy(correlation = correlation, isLoading = false) 
                }
            }
        }
    }
}
