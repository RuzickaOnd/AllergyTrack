package com.allergytrack.app.ui.screens.analysis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.shared.data.local.database.dao.FoodLogDao
import com.allergytrack.shared.data.local.database.dao.SymptomLogDao
import com.allergytrack.shared.data.local.datastore.OnboardingDataStore
import com.allergytrack.shared.domain.analysis.CorrelationEngine
import com.allergytrack.shared.domain.model.FoodReactionEvent
import com.allergytrack.shared.domain.model.FoodTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class FoodTriggerDetailUiState(
    val trigger: FoodTrigger? = null,
    val selectedEvent: FoodReactionEvent? = null,
    val isLoading: Boolean = true
)

class FoodTriggerDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val onboardingDataStore: OnboardingDataStore,
    private val correlationEngine: CorrelationEngine,
    private val json: Json
) : ViewModel() {

    // Trigger key předaný přes navigation argument nebo Intent extra
    private val triggerKey: String? = savedStateHandle["triggerKey"]

    private val _uiState = MutableStateFlow(FoodTriggerDetailUiState())
    val uiState: StateFlow<FoodTriggerDetailUiState> = _uiState.asStateFlow()

    init {
        loadTrigger()
    }

    private fun loadTrigger() {
        val key = triggerKey ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                foodLogDao.getAllLogs(),
                symptomLogDao.getAllLogs(),
                onboardingDataStore.selectedAllergyTypes
            ) { foods, symptoms, allergyTypesRaw ->
                val knownAllergens = allergyTypesRaw
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                val triggers = correlationEngine.analyzeFoodTriggers(
                    foodLogs = foods,
                    symptomLogs = symptoms,
                    knownAllergens = knownAllergens
                )
                
                triggers.find { it.allergenKey == key }
            }.collect { trigger ->
                _uiState.update { 
                    it.copy(trigger = trigger, isLoading = false) 
                }
            }
        }
    }

    /**
     * Umožňuje nastavit trigger přímo (např. ze sdíleného ViewModelu).
     */
    fun setTrigger(trigger: FoodTrigger) {
        _uiState.update {
            it.copy(trigger = trigger, isLoading = false)
        }
    }

    fun selectEvent(event: FoodReactionEvent?) {
        _uiState.update { it.copy(selectedEvent = event) }
    }
}
