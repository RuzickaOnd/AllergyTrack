package com.allergytrack.app.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.remote.ai.AiSummaryService
import com.allergytrack.app.domain.analysis.CorrelationEngine
import com.allergytrack.app.domain.model.Correlation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val hasEnoughData: Boolean = false,
    val symptomCount: Int = 0,
    val daysUntilAnalysis: Int = 14,
    val correlations: List<Correlation> = emptyList(),
    val aiSummary: String = "",
    val selectedCorrelation: Correlation? = null,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val symptomLogDao: SymptomLogDao,
    private val foodLogDao: FoodLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    private val correlationEngine: CorrelationEngine,
    private val aiSummaryService: AiSummaryService,
    private val medicalReportExporter: com.allergytrack.app.domain.export.MedicalReportExporter,
    private val premiumHelper: com.allergytrack.app.domain.premium.PremiumHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    val isAnalysisVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.app.domain.model.PremiumFeature.AI_CORRELATION_ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canUseAnalysis: StateFlow<Boolean> =
        premiumHelper.observeAccess(com.allergytrack.app.domain.model.PremiumFeature.AI_CORRELATION_ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showAnalysisUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.app.domain.model.PremiumFeature.AI_CORRELATION_ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isExportVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.app.domain.model.PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canExportPdf: StateFlow<Boolean> =
        premiumHelper.observeAccess(com.allergytrack.app.domain.model.PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showExportUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.app.domain.model.PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadAnalysis()
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            combine(
                symptomLogDao.getAllLogs(),
                foodLogDao.getAllLogs(),
                environmentLogDao.getAllLogs()
            ) { symptoms, foods, env ->
                Triple(symptoms, foods, env)
            }.collect { (symptoms, foods, env) ->

                val hasEnough = correlationEngine.hasEnoughData(symptoms)
                val daysLeft = correlationEngine.daysUntilAnalysis(symptoms)

                if (hasEnough) {
                    val correlations = correlationEngine.analyze(foods, symptoms, env)
                    val summary = aiSummaryService.generateSummary(correlations)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasEnoughData = true,
                            symptomCount = symptoms.size,
                            correlations = correlations,
                            aiSummary = summary
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasEnoughData = false,
                            symptomCount = symptoms.size,
                            daysUntilAnalysis = daysLeft
                        )
                    }
                }
            }
        }
    }

    fun onCorrelationSelected(correlation: Correlation?) {
        _uiState.update { it.copy(selectedCorrelation = correlation) }
    }

    fun exportReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val state = _uiState.value
            val file = medicalReportExporter.generatePdfReport(
                correlations = state.correlations,
                aiSummary = state.aiSummary
            )
            _uiState.update { it.copy(isExporting = false, exportSuccess = file != null) }
        }
    }

    fun dismissExportSuccess() {
        _uiState.update { it.copy(exportSuccess = false) }
    }
}
