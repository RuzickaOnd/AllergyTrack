package com.allergytrack.app.ui.screens.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.domain.model.SymptomType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "7 dní"),
    TWO_WEEKS(14, "14 dní"),
    MONTH(30, "30 dní")
}

data class ChartsUiState(
    val isLoading: Boolean = true,
    val selectedRange: TimeRange = TimeRange.WEEK,
    val selectedSymptomFilter: SymptomType? = null,
    val chartData: ChartDataSet? = null,
    val totalSymptomEvents: Int = 0,
    val mostFrequentSymptom: String = "",
    val avgDailyIntensity: Float = 0f
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val symptomLogDao: SymptomLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    private val foodLogDao: FoodLogDao,
    private val premiumHelper: com.allergytrack.app.domain.premium.PremiumHelper
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)
    private val _selectedFilter = MutableStateFlow<SymptomType?>(null)

    val isExtendedChartsVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.app.domain.model.PremiumFeature.EXTENDED_CHARTS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canUseExtendedCharts: StateFlow<Boolean> =
        premiumHelper.observeAccess(com.allergytrack.app.domain.model.PremiumFeature.EXTENDED_CHARTS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showExtendedChartsBadge: StateFlow<Boolean> =
        premiumHelper.observeShowBadge(com.allergytrack.app.domain.model.PremiumFeature.EXTENDED_CHARTS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isPollenVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.app.domain.model.PremiumFeature.POLLEN_DATA)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canSeePollen: StateFlow<Boolean> =
        premiumHelper.observeAccess(com.allergytrack.app.domain.model.PremiumFeature.POLLEN_DATA)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showPollenUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.app.domain.model.PremiumFeature.POLLEN_DATA)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val uiState: StateFlow<ChartsUiState> = combine(
        _selectedRange,
        _selectedFilter,
        symptomLogDao.getAllLogs(),
        environmentLogDao.getAllLogs()
    ) { range, filter, symptomLogs, envLogs ->

        val from = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(range.days.toLong())
        val filteredSymptoms = symptomLogs.filter { it.timestamp >= from }
        val filteredEnv = envLogs.filter { it.timestamp >= from }

        val chartData = ChartDataMapper.mapToChartData(
            symptomLogs = filteredSymptoms,
            environmentLogs = filteredEnv,
            filterType = filter,
            days = range.days
        )

        val mostFrequent = filteredSymptoms
            .groupBy { it.type }
            .maxByOrNull { it.value.size }
            ?.let { SymptomType.valueOf(it.key).label }
            ?: "—"

        val avgIntensity = if (filteredSymptoms.isEmpty()) 0f
        else filteredSymptoms.map { it.intensity }.average().toFloat()

        ChartsUiState(
            isLoading = false,
            selectedRange = range,
            selectedSymptomFilter = filter,
            chartData = chartData,
            totalSymptomEvents = filteredSymptoms.size,
            mostFrequentSymptom = mostFrequent,
            avgDailyIntensity = avgIntensity
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartsUiState()
    )

    fun onRangeSelected(range: TimeRange) {
        _selectedRange.value = range
    }

    fun onFilterSelected(type: SymptomType?) {
        _selectedFilter.value = type
    }
}
