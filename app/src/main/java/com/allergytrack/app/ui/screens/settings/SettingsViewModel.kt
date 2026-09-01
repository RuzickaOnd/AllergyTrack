package com.allergytrack.app.ui.screens.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.shared.data.local.datastore.OnboardingDataStore
import com.allergytrack.shared.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.permissions.PermissionsState
import com.allergytrack.app.data.worker.WorkManagerScheduler
import com.allergytrack.app.data.remote.ai.AiSummaryService
import com.allergytrack.app.domain.analysis.CorrelationEngine
import com.allergytrack.app.domain.export.MedicalReportExporter
import com.allergytrack.app.ui.util.AppLocaleHelper
import com.allergytrack.shared.domain.premium.PremiumHelper
import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.app.data.notifications.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class FoodLanguageOption(val code: String, val nativeName: String)

val FOOD_LANGUAGE_OPTIONS = listOf(
    FoodLanguageOption("", "Auto"),
    FoodLanguageOption("cs", "Čeština"),
    FoodLanguageOption("en", "English"),
    FoodLanguageOption("de", "Deutsch"),
    FoodLanguageOption("sk", "Slovenčina"),
    FoodLanguageOption("pl", "Polski"),
    FoodLanguageOption("fr", "Français"),
    FoodLanguageOption("es", "Español"),
    FoodLanguageOption("it", "Italiano"),
    FoodLanguageOption("hu", "Magyar"),
    FoodLanguageOption("nl", "Nederlands"),
    FoodLanguageOption("pt", "Português"),
    FoodLanguageOption("ro", "Română")
)

class SettingsViewModel(
    private val appContext: Context,
    private val permissionsHelper: PermissionsHelper,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val workManagerScheduler: WorkManagerScheduler,
    private val foodLogDao: FoodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val environmentLogDao: EnvironmentLogDao,
    private val onboardingDataStore: OnboardingDataStore,
    private val correlationEngine: CorrelationEngine,
    private val aiSummaryService: AiSummaryService,
    private val medicalReportExporter: MedicalReportExporter,
    private val premiumHelper: PremiumHelper,
    private val analyticsManager: com.allergytrack.app.data.analytics.AnalyticsManager,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    init {
        analyticsManager.logScreenView("settings", this::class.java.simpleName)
        fetchFcmToken()
    }

    private fun fetchFcmToken() {
        notificationManager.getFcmToken { token ->
            _fcmToken.value = token ?: ""
        }
    }

    val isExportVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canUseExport: StateFlow<Boolean> =
        premiumHelper.observeAccess(PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showExportBadge: StateFlow<Boolean> =
        premiumHelper.observeShowBadge(PremiumFeature.PDF_EXPORT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canUseExtendedCharts: StateFlow<Boolean> =
        premiumHelper.observeAccess(PremiumFeature.EXTENDED_CHARTS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // --- OPRÁVNĚNÍ ---

    private val _permissionsState = MutableStateFlow(permissionsHelper.getPermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    /** Přenačte skutečný stav oprávnění ze systému — voláno při ON_RESUME. */
    fun refreshPermissions() {
        _permissionsState.update { permissionsHelper.getPermissionsState() }
    }

    // --- OBECNÉ ---

    // Jazyk aplikace — čte aktuální stav z LocaleManager (API 33+) nebo SharedPreferences (starší)
    private val _appLanguage = MutableStateFlow(AppLocaleHelper.getLanguage(appContext))
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // API 33+ spustí recreate automaticky přes LocaleManager; pre-33 signalizuje Screen
    private val _restartAppRequired = MutableStateFlow(false)
    val restartAppRequired: StateFlow<Boolean> = _restartAppRequired.asStateFlow()

    fun setAppLanguage(code: String) {
        AppLocaleHelper.setLanguage(appContext, code)
        _appLanguage.value = code
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            _restartAppRequired.value = true
        }
    }

    fun onRestartHandled() {
        _restartAppRequired.value = false
    }

    /**
     * Tmavý/světlý režim aplikace. Výchozí hodnota = true (tmavý).
     * Změna se projeví okamžitě — MainActivity sleduje tento flow a předává
     * ho přímo do AllergyTrackTheme(darkTheme = ...).
     */
    val darkMode: StateFlow<Boolean> = userPreferencesDataStore.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setDarkMode(enabled) }
    }

    /**
     * Večerní připomínka k logování — viz [WorkManagerScheduler.scheduleReminder].
     * Zapnutí ihned naplánuje WorkManager job; vypnutí ho zruší.
     * Stav přežije restart aplikace (uložen v DataStore + WorkManager).
     */
    val notificationsEnabled: StateFlow<Boolean> = userPreferencesDataStore.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setNotificationsEnabled(enabled)
            if (enabled) workManagerScheduler.scheduleReminder()
            else workManagerScheduler.cancelReminder()
        }
    }

    // --- VYHLEDÁVÁNÍ JÍDLA ---

    /**
     * Kód jazyka pro vyhledávání v Supabase (např. "cs", "en").
     * Prázdný string = automaticky podle systémového locale zařízení.
     * Hodnota se promítne do FoodRepositoryImpl.resolvedLocale().
     */
    val foodSearchLanguage: StateFlow<String> = userPreferencesDataStore.foodSearchLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setFoodSearchLanguage(code: String) {
        viewModelScope.launch { userPreferencesDataStore.setFoodSearchLanguage(code) }
    }

    // --- GRAFY ---

    val syncChartsScroll: StateFlow<Boolean> = userPreferencesDataStore.syncChartsScroll
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setSyncChartsScroll(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setSyncChartsScroll(enabled) }
    }

    // --- DATA A SOUKROMÍ ---

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    /**
     * Vygeneruje PDF lékařský report a uloží ho do Documents/AllergyTrack.
     * Průběh: načte všechna data → spustí korelační analýzu → AI shrnutí →
     * MedicalReportExporter → výsledný File předá do [exportedFile].
     * SettingsScreen sleduje [exportedFile] a po jeho naplnění zobrazí
     * systémový share sheet.
     */
    fun exportData() {
        viewModelScope.launch {
            _isExporting.value = true
            val foodLogs = foodLogDao.getAllLogs().first()
            val symptomLogs = symptomLogDao.getAllLogs().first()
            val environmentLogs = environmentLogDao.getAllLogs().first()
            val allergyTypesRaw = onboardingDataStore.selectedAllergyTypes.first()
            val knownAllergies = allergyTypesRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

            val correlations = correlationEngine.analyze(
                foodLogs = foodLogs,
                symptomLogs = symptomLogs,
                environmentLogs = environmentLogs,
                knownAllergies = knownAllergies
            )
            val aiSummary = aiSummaryService.generateSummary(correlations)
            val file = medicalReportExporter.generatePdfReport(
                correlations = correlations,
                foodTriggers = emptyList(),
                aiSummary = aiSummary
            )
            _isExporting.value = false
            _exportedFile.value = file
        }
    }

    /** Vymaže referenci na exportovaný soubor po jeho sdílení — zabrání opakovanému spuštění share sheetu. */
    fun dismissExport() {
        _exportedFile.value = null
    }

    /**
     * Trvale smaže všechny záznamy jídla, příznaků a prostředí z Room databáze.
     * Operace je nevratná. DataStore preference (jméno, avatar, jazyk) zůstanou zachovány.
     */
    fun deleteAllData() {
        viewModelScope.launch {
            foodLogDao.deleteAll()
            symptomLogDao.deleteAll()
            environmentLogDao.deleteAll()
        }
    }
}
