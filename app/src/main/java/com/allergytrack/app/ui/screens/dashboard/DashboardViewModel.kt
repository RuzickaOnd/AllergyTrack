package com.allergytrack.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.domain.model.EnvironmentRegion
import com.allergytrack.app.domain.repository.EnvironmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val todayFoodLogs: List<FoodLogEntity> = emptyList(),
    val todaySymptomLogs: List<SymptomLogEntity> = emptyList(),
    val latestEnvironment: EnvironmentLog? = null,
    val isRefreshingEnvironment: Boolean = true,
    // true jen pokud: načítáme a region ještě neznáme, nebo jsme v Evropě
    val showEnvironmentSection: Boolean = true,
    val greeting: String = "",
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val environmentRepository: EnvironmentRepository
) : ViewModel() {

    private val _isRefreshingEnv = MutableStateFlow(true)
    private val isFetchingEnv = AtomicBoolean(false)

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
        environmentRepository.getAllLogs(),
        _isRefreshingEnv
    ) { foodLogs, symptomLogs, envLogs, isRefreshing ->
        val latest = envLogs.maxByOrNull { it.timestamp }
        val isEurope = latest?.region == EnvironmentRegion.EUROPE
        DashboardUiState(
            isLoading = false,
            todayFoodLogs = foodLogs,
            todaySymptomLogs = symptomLogs,
            latestEnvironment = if (isEurope) latest else null,
            isRefreshingEnvironment = isRefreshing,
            // Sekci zobrazíme jen pokud ještě načítáme (region neznámý) nebo jsme v EU
            showEnvironmentSection = (latest == null && isRefreshing) || isEurope,
            greeting = getGreeting()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    // Voláno ze Screenu při každém RESUMED — bezpečné pro souběžná volání
    fun refreshEnvironment() {
        if (!isFetchingEnv.compareAndSet(false, true)) return
        viewModelScope.launch {
            _isRefreshingEnv.value = true
            try {
                environmentRepository.fetchAndSaveCurrentEnvironment()
            } catch (_: Exception) {
                // Tichá chyba — WorkManager provede retry
            } finally {
                _isRefreshingEnv.value = false
                isFetchingEnv.set(false)
            }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Dobré ráno"
            hour < 18 -> "Dobré odpoledne"
            else -> "Dobrý večer"
        }
    }
}
