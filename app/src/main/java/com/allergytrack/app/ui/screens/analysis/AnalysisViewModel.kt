package com.allergytrack.app.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.shared.config.AppConfig
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.data.local.datastore.DebugPreferences
import com.allergytrack.shared.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.data.remote.ai.AiSummaryService
import com.allergytrack.app.domain.analysis.CorrelationEngine
import com.allergytrack.app.domain.export.MedicalReportExporter
import com.allergytrack.shared.domain.model.AnalysisReadiness
import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.FoodTrigger
import com.allergytrack.shared.domain.premium.PremiumHelper
import com.allergytrack.app.data.analytics.AnalyticsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val readiness: AnalysisReadiness = AnalysisReadiness.Locked(symptomCount = 0),
    val correlations: List<Correlation> = emptyList(),
    val foodTriggers: List<FoodTrigger> = emptyList(),
    val foodTriggersLoading: Boolean = false,
    val selectedTrigger: FoodTrigger? = null,
    val aiSummary: String = "",
    val showDisclaimer: Boolean = false,
    val disclaimer: String = "",
    val isExporting: Boolean = false,
    val exportedFile: File? = null
)

class AnalysisViewModel(
    private val symptomLogDao: SymptomLogDao,
    private val foodLogDao: FoodLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    private val correlationEngine: CorrelationEngine,
    private val aiSummaryService: AiSummaryService,
    private val onboardingDataStore: OnboardingDataStore,
    private val medicalReportExporter: MedicalReportExporter,
    private val premiumHelper: PremiumHelper,
    private val debugPreferences: DebugPreferences,
    private val analyticsManager: AnalyticsManager,
    private val appConfig: AppConfig
) : ViewModel() {

    init {
        analyticsManager.logScreenView("analysis", this::class.java.simpleName)
    }

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    val isAnalysisVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.shared.domain.model.PremiumFeature.AI_CORRELATION_ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showAnalysisUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.shared.domain.model.PremiumFeature.AI_CORRELATION_ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isExportVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.shared.domain.model.PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showExportUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.shared.domain.model.PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadAnalysis()
        loadFoodTriggers()
    }

    private fun loadFoodTriggers() {
        viewModelScope.launch {
            _uiState.update { it.copy(foodTriggersLoading = true) }

            combine(
                foodLogDao.getAllLogs(),
                symptomLogDao.getAllLogs(),
                onboardingDataStore.selectedAllergyTypes
            ) { foods, symptoms, allergyTypesRaw ->
                Triple(foods, symptoms, allergyTypesRaw)
            }.collect { (foods, symptoms, allergyTypesRaw) ->

                // Načti alergeny z onboarding profilu pro označení "SHODA"
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

                _uiState.update {
                    it.copy(
                        foodTriggers = triggers,
                        foodTriggersLoading = false
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadAnalysis() {
        viewModelScope.launch {
            combine(
                listOf(
                    symptomLogDao.getAllLogs(),
                    foodLogDao.getAllLogs(),
                    environmentLogDao.getAllLogs(),
                    onboardingDataStore.selectedAllergyTypes,
                    onboardingDataStore.selectedSymptomTypes,
                    debugPreferences.symptomCountOverride
                )
            ) { array ->
                val symptoms = array[0] as List<SymptomLogEntity>
                val foods = array[1] as List<FoodLogEntity>
                val env = array[2] as List<EnvironmentLogEntity>
                val allergies = array[3] as String
                val userSymptoms = array[4] as String
                val countOverride = array[5] as Int

                val allergySet = allergies.split(",").filter { it.isNotEmpty() }.toSet()
                val symptomSet = userSymptoms.split(",").filter { it.isNotEmpty() }.toSet()

                val effectiveSymptomCount = if (appConfig.isDebug && countOverride >= 0) {
                    countOverride
                } else {
                    symptoms.size
                }

                val readiness = correlationEngine.evaluateReadiness(
                    symptomCount = effectiveSymptomCount,
                    environmentLogCount = env.size
                )

                when (readiness) {
                    is AnalysisReadiness.Locked -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                readiness = readiness,
                                correlations = emptyList(),
                                aiSummary = "",
                                showDisclaimer = false
                            )
                        }
                    }
                    is AnalysisReadiness.Partial -> {
                        val correlations = correlationEngine.analyze(
                            foodLogs = foods,
                            symptomLogs = symptoms,
                            environmentLogs = env,
                            knownAllergies = allergySet,
                            knownSymptoms = symptomSet
                        )
                        val summary = aiSummaryService.generateSummary(correlations)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                readiness = readiness,
                                correlations = correlations,
                                aiSummary = summary,
                                showDisclaimer = true,
                                disclaimer = readiness.disclaimer
                            )
                        }
                    }
                    is AnalysisReadiness.Full -> {
                        val correlations = correlationEngine.analyze(
                            foodLogs = foods,
                            symptomLogs = symptoms,
                            environmentLogs = env,
                            knownAllergies = allergySet,
                            knownSymptoms = symptomSet
                        )
                        val summary = aiSummaryService.generateSummary(correlations)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                readiness = readiness,
                                correlations = correlations,
                                aiSummary = summary,
                                showDisclaimer = false
                            )
                        }
                    }
                }
            }.collect()
        }
    }

    fun selectTrigger(trigger: FoodTrigger) {
        _uiState.update { it.copy(selectedTrigger = trigger) }
    }

    fun exportReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val state = _uiState.value
            val file = medicalReportExporter.generatePdfReport(
                correlations = state.correlations,
                foodTriggers = state.foodTriggers,
                aiSummary = state.aiSummary
            )
            _uiState.update { it.copy(isExporting = false, exportedFile = file) }
        }
    }

    fun dismissExport() {
        _uiState.update { it.copy(exportedFile = null) }
    }
}
