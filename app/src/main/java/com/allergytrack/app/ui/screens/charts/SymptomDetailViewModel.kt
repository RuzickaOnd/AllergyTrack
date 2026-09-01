package com.allergytrack.app.ui.screens.charts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.app.domain.premium.PremiumHelper
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

data class SymptomTypeStat(
    val type: SymptomType,
    val count: Int,
    val avgIntensity: Float,
    val maxIntensity: Int,
    val lastOccurrence: Long?
)

data class SymptomDetailUiState(
    val isLoading: Boolean = true,
    val selectedRange: TimeRange = TimeRange.WEEK,
    val stats: List<SymptomTypeStat> = emptyList(),
    val maxCount: Int = 0
)

class SymptomDetailViewModel constructor(
    private val symptomLogDao: SymptomLogDao,
    private val premiumHelper: PremiumHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialRange = savedStateHandle.get<String>("range")
        ?.let { runCatching { TimeRange.valueOf(it) }.getOrNull() }
        ?: TimeRange.WEEK

    private val _selectedRange = MutableStateFlow(initialRange)

    val canUseExtendedCharts: StateFlow<Boolean> =
        premiumHelper.observeAccess(PremiumFeature.EXTENDED_CHARTS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val uiState: StateFlow<SymptomDetailUiState> = combine(
        _selectedRange,
        canUseExtendedCharts,
        symptomLogDao.getAllLogs()
    ) { requestedRange, canUse, logs ->
        val range = if (canUse) requestedRange else TimeRange.WEEK
        val from = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(range.days.toLong())
        val filtered = logs.filter { it.timestamp >= from }

        val stats = SymptomType.entries.mapNotNull { type ->
            val typeLogs = filtered.filter { it.type == type.name }
            if (typeLogs.isEmpty()) null
            else SymptomTypeStat(
                type = type,
                count = typeLogs.size,
                avgIntensity = typeLogs.map { it.intensity }.average().toFloat(),
                maxIntensity = typeLogs.maxOf { it.intensity },
                lastOccurrence = typeLogs.maxOfOrNull { it.timestamp }
            )
        }.sortedByDescending { it.count }

        SymptomDetailUiState(
            isLoading = false,
            selectedRange = range,
            stats = stats,
            maxCount = stats.firstOrNull()?.count ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SymptomDetailUiState()
    )

    fun onRangeSelected(range: TimeRange) {
        _selectedRange.value = range
    }
}
