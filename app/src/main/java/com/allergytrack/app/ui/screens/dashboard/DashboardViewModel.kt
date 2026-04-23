package com.allergytrack.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.domain.repository.EnvironmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val todayFoodLogs: List<FoodLogEntity> = emptyList(),
    val todaySymptomLogs: List<SymptomLogEntity> = emptyList(),
    val latestEnvironment: EnvironmentLog? = null,
    val greeting: String = "Good morning",
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val environmentRepository: EnvironmentRepository
) : ViewModel() {

    private val startOfDay: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        foodLogDao.getTodayLogs(startOfDay),
        symptomLogDao.getTodayLogs(startOfDay),
        environmentRepository.getAllLogs()
    ) { foodLogs, symptomLogs, envLogs ->
        DashboardUiState(
            isLoading = false,
            todayFoodLogs = foodLogs,
            todaySymptomLogs = symptomLogs,
            latestEnvironment = envLogs.maxByOrNull { it.timestamp },
            greeting = getGreeting()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
