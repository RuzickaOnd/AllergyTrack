package com.allergytrack.app.ui.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.data.local.datastore.DebugPremiumOverride
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.shared.domain.model.EnvironmentLog
import com.allergytrack.shared.domain.model.EnvironmentRegion
import com.allergytrack.shared.domain.model.FoodLog
import com.allergytrack.shared.domain.model.PremiumVisibilityMode
import com.allergytrack.shared.domain.model.SymptomLog
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.app.domain.premium.PremiumHelper
import com.allergytrack.shared.domain.repository.EnvironmentRepository
import com.allergytrack.shared.domain.repository.FoodRepository
import com.allergytrack.shared.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DebugMenuViewModel(
    private val debugPreferences: DebugPreferences,
    private val onboardingDataStore: OnboardingDataStore,
    private val foodRepository: FoodRepository,
    private val symptomRepository: SymptomRepository,
    private val environmentRepository: EnvironmentRepository,
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    premiumHelper: PremiumHelper
) : ViewModel() {

    val currentVisibilityMode: PremiumVisibilityMode = premiumHelper.currentVisibilityMode

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

    val analyticsEnabled: StateFlow<Boolean> = debugPreferences.analyticsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val crashlyticsEnabled: StateFlow<Boolean> = debugPreferences.crashlyticsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _seedingInProgress = MutableStateFlow(false)
    val seedingInProgress: StateFlow<Boolean> = _seedingInProgress.asStateFlow()

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

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            debugPreferences.setAnalyticsEnabled(enabled)
        }
    }

    fun setCrashlyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            debugPreferences.setCrashlyticsEnabled(enabled)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            debugPreferences.resetAll()
        }
    }

    fun seedTestData(czech: Boolean) {
        viewModelScope.launch {
            _seedingInProgress.value = true

            foodLogDao.deleteAll()
            symptomLogDao.deleteAll()
            environmentLogDao.deleteAll()

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayMidnight = cal.timeInMillis
            val dayMs = 86_400_000L
            val hourMs = 3_600_000L
            val lat = 50.0755
            val lng = 14.4378

            data class FoodItem(val cz: String, val en: String, val allergens: List<String>)
            data class SymptomItem(val type: SymptomType, val intensity: Int, val hourOfDay: Int)

            val breakfasts = listOf(
                FoodItem("Jogurt s jahodami", "Strawberry yogurt", listOf("Milk")),
                FoodItem("Ovesná kaše s medem", "Honey oatmeal", listOf("Gluten")),
                FoodItem("Müsli s mlékem", "Muesli with milk", listOf("Gluten", "Milk")),
                FoodItem("Celozrnný chléb s máslem", "Whole grain toast with butter", listOf("Gluten", "Milk")),
                FoodItem("Tvarohový dezert s ovocem", "Cottage cheese with fruit", listOf("Milk")),
                FoodItem("Croissant s marmeládou", "Croissant with jam", listOf("Gluten", "Milk", "Eggs")),
                FoodItem("Banán s ořechovým máslem", "Banana with peanut butter", listOf("Peanuts")),
            )

            val lunches = listOf(
                FoodItem("Pizza Margherita", "Margherita pizza", listOf("Gluten", "Milk")),
                FoodItem("Těstoviny s parmezánem", "Pasta with parmesan", listOf("Gluten", "Milk", "Eggs")),
                FoodItem("Kuřecí salát", "Chicken salad", emptyList()),
                FoodItem("Lasagne", "Lasagna", listOf("Gluten", "Milk", "Eggs")),
                FoodItem("Fazolová polévka", "Bean soup", emptyList()),
                FoodItem("Čočková polévka", "Lentil soup", emptyList()),
                FoodItem("Svíčková s knedlíky", "Beef sirloin with dumplings", listOf("Gluten", "Milk")),
                FoodItem("Ryba s bramborovým salátem", "Fish with potato salad", listOf("Fish", "Milk", "Eggs")),
                FoodItem("Špenátová polévka", "Spinach soup", listOf("Milk")),
                FoodItem("Kuřecí vývar", "Chicken broth", emptyList()),
            )

            val snacks = listOf(
                FoodItem("Sýrový sendvič", "Cheese sandwich", listOf("Gluten", "Milk")),
                FoodItem("Řecký jogurt s ořechy", "Greek yogurt with nuts", listOf("Milk", "Nuts")),
                FoodItem("Sušenky", "Cookies", listOf("Gluten", "Milk", "Eggs")),
                FoodItem("Jablko", "Apple", emptyList()),
                FoodItem("Ovocný salát", "Fruit salad", emptyList()),
                FoodItem("Hroznové víno", "Grapes", emptyList()),
                FoodItem("Müsli tyčinka", "Muesli bar", listOf("Gluten", "Milk", "Nuts")),
            )

            // env: aqi, pm25, no2, ozone, pollenGrass, pollenTree, pollenWeed, temp, humidity
            val envValues = listOf(
                intArrayOf(35, 8, 15, 65, 1, 2, 0, 16, 60),   // day 20
                intArrayOf(45, 12, 22, 72, 2, 3, 0, 17, 58),   // day 19
                intArrayOf(55, 15, 28, 78, 2, 2, 1, 18, 62),   // day 18
                intArrayOf(42, 10, 18, 68, 1, 1, 0, 15, 70),   // day 17
                intArrayOf(38, 9, 16, 64, 0, 1, 0, 14, 72),    // day 16
                intArrayOf(60, 18, 32, 85, 3, 2, 1, 19, 55),   // day 15
                intArrayOf(75, 22, 40, 92, 3, 3, 1, 21, 50),   // day 14
                intArrayOf(80, 25, 42, 95, 3, 2, 2, 22, 48),   // day 13
                intArrayOf(68, 20, 35, 88, 2, 1, 1, 20, 52),   // day 12
                intArrayOf(45, 13, 20, 70, 1, 0, 0, 18, 63),   // day 11
                intArrayOf(40, 11, 18, 67, 2, 1, 0, 17, 65),   // day 10
                intArrayOf(35, 8, 14, 62, 1, 1, 0, 16, 68),    // day 9
                intArrayOf(50, 15, 25, 75, 3, 2, 1, 20, 57),   // day 8
                intArrayOf(65, 19, 35, 88, 3, 2, 2, 22, 52),   // day 7
                intArrayOf(72, 21, 38, 90, 2, 1, 1, 23, 49),   // day 6
                intArrayOf(58, 17, 30, 82, 2, 2, 1, 21, 54),   // day 5
                intArrayOf(48, 14, 23, 73, 1, 1, 0, 19, 60),   // day 4
                intArrayOf(42, 11, 19, 68, 2, 2, 0, 18, 63),   // day 3
                intArrayOf(55, 16, 27, 78, 3, 2, 1, 20, 58),   // day 2
                intArrayOf(62, 18, 32, 84, 3, 3, 1, 22, 52),   // day 1
                intArrayOf(50, 14, 24, 75, 2, 2, 1, 21, 56),   // day 0 (today)
            )

            // Symptom pattern designed to produce visible correlations with Milk/Gluten and high AQI
            val symptomPattern: Map<Int, List<SymptomItem>> = mapOf(
                19 to listOf(SymptomItem(SymptomType.RHINITIS, 3, 10)),
                18 to listOf(SymptomItem(SymptomType.RHINITIS, 2, 8)),
                16 to listOf(SymptomItem(SymptomType.DIGESTIVE, 3, 15)),
                14 to listOf(
                    SymptomItem(SymptomType.EYE_ITCHING, 4, 11),
                    SymptomItem(SymptomType.RHINITIS, 3, 19)
                ),
                13 to listOf(SymptomItem(SymptomType.RHINITIS, 2, 9)),
                11 to listOf(
                    SymptomItem(SymptomType.RHINITIS, 3, 11),
                    SymptomItem(SymptomType.ECZEMA, 2, 20)
                ),
                8 to listOf(
                    SymptomItem(SymptomType.EYE_ITCHING, 4, 10),
                    SymptomItem(SymptomType.RHINITIS, 4, 14)
                ),
                7 to listOf(SymptomItem(SymptomType.RHINITIS, 3, 8)),
                6 to listOf(
                    SymptomItem(SymptomType.DIGESTIVE, 4, 10),
                    SymptomItem(SymptomType.RHINITIS, 3, 15)
                ),
                5 to listOf(SymptomItem(SymptomType.ECZEMA, 3, 12)),
                3 to listOf(SymptomItem(SymptomType.RHINITIS, 3, 9)),
                2 to listOf(SymptomItem(SymptomType.RHINITIS, 2, 11)),
                1 to listOf(SymptomItem(SymptomType.EYE_ITCHING, 2, 16)),
                0 to listOf(SymptomItem(SymptomType.RHINITIS, 2, 12)),
            )

            for (dayOffset in 20 downTo 0) {
                val midnight = todayMidnight - dayOffset * dayMs
                val envIdx = 20 - dayOffset
                val e = envValues[envIdx]

                environmentRepository.saveLog(
                    EnvironmentLog(
                        timestamp = midnight + 2 * hourMs,
                        latitude = lat,
                        longitude = lng,
                        aqi = e[0],
                        pm25 = e[1].toFloat(),
                        no2 = e[2].toFloat(),
                        ozone = e[3].toFloat(),
                        pollenGrassIndex = e[4],
                        pollenTreeIndex = e[5],
                        pollenWeedIndex = e[6],
                        region = EnvironmentRegion.EUROPE,
                        pollenAvailable = true,
                        temperature = e[7].toFloat(),
                        humidity = e[8].toFloat()
                    )
                )

                val bf = breakfasts[dayOffset % breakfasts.size]
                foodRepository.saveLog(
                    FoodLog(
                        timestamp = midnight + 8 * hourMs,
                        foodName = if (czech) bf.cz else bf.en,
                        allergens = bf.allergens
                    )
                )

                val ln = lunches[dayOffset % lunches.size]
                foodRepository.saveLog(
                    FoodLog(
                        timestamp = midnight + 13 * hourMs,
                        foodName = if (czech) ln.cz else ln.en,
                        allergens = ln.allergens
                    )
                )

                if (dayOffset % 2 == 0) {
                    val sn = snacks[(dayOffset / 2) % snacks.size]
                    foodRepository.saveLog(
                        FoodLog(
                            timestamp = midnight + 16 * hourMs,
                            foodName = if (czech) sn.cz else sn.en,
                            allergens = sn.allergens
                        )
                    )
                }

                symptomPattern[dayOffset]?.forEach { symptom ->
                    symptomRepository.saveLog(
                        SymptomLog(
                            timestamp = midnight + symptom.hourOfDay * hourMs,
                            type = symptom.type,
                            intensity = symptom.intensity
                        )
                    )
                }
            }

            _seedingInProgress.value = false
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            onboardingDataStore.reset()
        }
    }
}
