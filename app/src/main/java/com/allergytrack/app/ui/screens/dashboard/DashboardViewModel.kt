package com.allergytrack.app.ui.screens.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.R
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

sealed interface DashboardInsight {
    data class HighPollen(val maxLevel: Int) : DashboardInsight
    data class HighAqi(val aqi: Int) : DashboardInsight
    data object GoodDay : DashboardInsight
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val todayFoodLogs: List<FoodLogEntity> = emptyList(),
    val todaySymptomLogs: List<SymptomLogEntity> = emptyList(),
    val latestEnvironment: EnvironmentLog? = null,
    val isRefreshingEnvironment: Boolean = false,
    val showEnvironmentSection: Boolean = false,
    @get:StringRes val greetingRes: Int = R.string.greeting_morning,
    val insight: DashboardInsight? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val environmentRepository: EnvironmentRepository
) : ViewModel() {

    // Začíná jako false — loading se zobrazí až po potvrzení EU regionu v refreshEnvironment()
    private val _isRefreshingEnv = MutableStateFlow(false)
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
        // DŮLEŽITÉ: Sekce prostředí se zobrazuje POUZE pro uživatele v Evropě.
        // Používáme Open-Meteo Air Quality API s modelem CAMS (Copernicus), který
        // pokrývá pouze Evropu. Pole `european_aqi` vrací null mimo EU — uložilo by se
        // jako 0, což je zavádějící hodnota ("dobrá kvalita vzduchu" vs "data nejsou k dispozici").
        // Pyl je také EU-only a sezónní (únor–říjen). Pro globální podporu by bylo
        // nutné integrovat jiný datový zdroj (např. IQAir, AirVisual nebo WAQI API).
        val isEurope = latest?.region == EnvironmentRegion.EUROPE
        DashboardUiState(
            isLoading = false,
            todayFoodLogs = foodLogs,
            todaySymptomLogs = symptomLogs,
            // Null pro non-EU: AQI by byl uložen jako 0 (chybějící data, ne skutečná hodnota).
            latestEnvironment = if (isEurope) latest else null,
            isRefreshingEnvironment = isRefreshing,
            // Zobrazit sekci: dokud načítáme (region ještě neznáme) NEBO jsme v EU.
            showEnvironmentSection = (latest == null && isRefreshing) || isEurope,
            greetingRes = getGreeting(),
            insight = computeInsight(if (isEurope) latest else null)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun refreshEnvironment() {
        if (!isFetchingEnv.compareAndSet(false, true)) return
        viewModelScope.launch {
            // Rychlý pre-check z kešované polohy (žádný GPS fix, okamžité).
            // Pokud jsme prokazatelně mimo EU, fetch vůbec nespouštíme — API
            // (Open-Meteo CAMS) stejně nevrátí žádná užitečná data a loading
            // by byl zavádějící. Pro UNKNOWN (žádná kešovaná poloha, první spuštění)
            // pokračujeme — nechceme zbytečně skrýt sekci EU uživatelům při prvním startu.
            val knownRegion = environmentRepository.getLastKnownRegion()
            if (knownRegion == EnvironmentRegion.GLOBAL) {
                isFetchingEnv.set(false)
                return@launch
            }
            // EU nebo neznámý region → zobrazit loading a spustit fetch
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

    @StringRes
    private fun getGreeting(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> R.string.greeting_morning
            hour < 18 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    private fun computeInsight(env: EnvironmentLog?): DashboardInsight? {
        env ?: return null
        if (env.pollenAvailable) {
            val maxPollen = maxOf(
                env.pollenGrassIndex ?: 0,
                env.pollenTreeIndex ?: 0,
                env.pollenWeedIndex ?: 0
            )
            if (maxPollen >= 3) return DashboardInsight.HighPollen(maxPollen)
        }
        if (env.aqi >= 50) return DashboardInsight.HighAqi(env.aqi)
        if (env.aqi > 0) return DashboardInsight.GoodDay
        return null
    }
}
