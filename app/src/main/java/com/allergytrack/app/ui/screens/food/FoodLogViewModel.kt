package com.allergytrack.app.ui.screens.food

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.shared.data.remote.food.FoodAllergenHelper
import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.shared.domain.model.FoodLog
import com.allergytrack.shared.data.remote.ApiHttpException
import com.allergytrack.shared.domain.repository.FoodRepository
import com.allergytrack.app.domain.usecase.RecognizeFoodFromImageUseCase
import com.allergytrack.shared.domain.usecase.SaveFoodLogUseCase
import com.allergytrack.shared.domain.usecase.SearchFoodUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.allergytrack.app.data.analytics.AnalyticsManager

// ---- UI State ----

sealed interface VoiceInputState {
    object Idle : VoiceInputState
    object Listening : VoiceInputState
}

sealed interface FoodLogUiState {
    object Idle : FoodLogUiState
    object CameraReady : FoodLogUiState
    object Processing : FoodLogUiState
    data class RecognitionResult(val foods: List<RecognizedFood>) : FoodLogUiState
    data class Confirmed(val food: RecognizedFood) : FoodLogUiState
    object Saved : FoodLogUiState
    data class Error(val message: String) : FoodLogUiState
}

data class FoodSearchState(
    val query: String = "",
    val results: List<RecognizedFood> = emptyList(),
    val isSearching: Boolean = false,
    val isServiceUnavailable: Boolean = false
)

// ---- ViewModel ----

class FoodLogViewModel(
    private val saveFoodLogUseCase: SaveFoodLogUseCase,
    private val searchFoodUseCase: SearchFoodUseCase,
    private val recognizeFoodFromImageUseCase: RecognizeFoodFromImageUseCase,
    private val foodRepository: FoodRepository,
    private val premiumHelper: com.allergytrack.shared.domain.premium.PremiumHelper,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    init {
        analyticsManager.logScreenView("food_log", this::class.java.simpleName)
    }

    private val _uiState = MutableStateFlow<FoodLogUiState>(FoodLogUiState.Idle)
    val uiState: StateFlow<FoodLogUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow(FoodSearchState())
    val searchState: StateFlow<FoodSearchState> = _searchState.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

    val isPhotoVisible: StateFlow<Boolean> =
        premiumHelper.observeVisible(com.allergytrack.shared.domain.model.PremiumFeature.FOOD_PHOTO_RECOGNITION)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showPhotoUpsell: StateFlow<Boolean> =
        premiumHelper.observeShowUpsell(com.allergytrack.shared.domain.model.PremiumFeature.FOOD_PHOTO_RECOGNITION)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showPhotoBadge: StateFlow<Boolean> =
        premiumHelper.observeShowBadge(com.allergytrack.shared.domain.model.PremiumFeature.FOOD_PHOTO_RECOGNITION)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val recentFoodLogs: StateFlow<List<FoodLog>> =
        foodRepository.getAllLogs()
            .map { it.take(10) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _voiceState = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val voiceState: StateFlow<VoiceInputState> = _voiceState.asStateFlow()

    private val _navigateToSearch = MutableStateFlow(false)
    val navigateToSearch: StateFlow<Boolean> = _navigateToSearch.asStateFlow()

    private val _customTimestamp = MutableStateFlow(System.currentTimeMillis())
    val customTimestamp: StateFlow<Long> = _customTimestamp.asStateFlow()

    fun onTimestampChanged(timestamp: Long) {
        _customTimestamp.value = timestamp
    }

    fun startVoiceInput() { _voiceState.value = VoiceInputState.Listening }

    fun onVoiceResult(text: String) {
        _voiceState.value = VoiceInputState.Idle
        onSearchQueryChange(text)
        _navigateToSearch.value = true
    }

    fun dismissVoiceInput() { _voiceState.value = VoiceInputState.Idle }

    fun onSearchNavigated() { _navigateToSearch.value = false }

    // ---- Allergen suggestion (manual entry) ----

    private val _suggestedAllergens = MutableStateFlow<Set<String>>(emptySet())
    val suggestedAllergens: StateFlow<Set<String>> = _suggestedAllergens.asStateFlow()

    private val _isSuggesting = MutableStateFlow(false)
    val isSuggesting: StateFlow<Boolean> = _isSuggesting.asStateFlow()

    fun suggestAllergens(foodName: String) {
        if (foodName.isBlank()) return
        viewModelScope.launch {
            _isSuggesting.value = true
            // A: lokální heuristika (okamžitá)
            val local = FoodAllergenHelper.suggestFromName(foodName)
            _suggestedAllergens.value = local
            // B: Open Food Facts (síťové dohledání)
            try {
                val results = searchFoodUseCase(foodName)
                if (results.isNotEmpty()) {
                    _suggestedAllergens.value = local + results.first().allergens.toSet()
                }
            } catch (_: Exception) { /* ponechat lokální */ }
            _isSuggesting.value = false
        }
    }

    fun clearAllergenSuggestions() {
        _suggestedAllergens.value = emptySet()
        _isSuggesting.value = false
    }

    // Debounced search
    @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults = _searchState
        .map { it.query }
        .debounce(400)
        .filter { it.length >= 2 }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                _searchState.update { it.copy(isSearching = true, isServiceUnavailable = false) }
                try {
                    val results = searchFoodUseCase(query)
                    emit(results)
                    _searchState.update { it.copy(isSearching = false, results = results) }
                } catch (e: ApiHttpException) {
                    // POZOR: tento blok je nedosažitelný — FoodRepositoryImpl
                    // .searchFoodByName() chytá Exception a vrací emptyList(),
                    // takže sem výjimka nikdy nedoteče a isServiceUnavailable
                    // se nikdy nenastaví. Platilo to i před KMP migrací
                    // (tehdy s retrofit2.HttpException). Zachováno záměrně,
                    // aby migrace nezměnila chování — oprava je samostatná změna.
                    // ApiHttpException je typ z :shared; díky němu nepotřebuje
                    // :app mít na classpath Ktor.
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            isServiceUnavailable = e.statusCode == 503
                        )
                    }
                    emit(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchState.update { it.copy(query = query, isServiceUnavailable = false) }
    }

    fun onImageCaptured(uri: Uri) {
        viewModelScope.launch {
            if (!premiumHelper.hasAccess(com.allergytrack.shared.domain.model.PremiumFeature.FOOD_PHOTO_RECOGNITION)) {
                _uiState.value = FoodLogUiState.Error("Foto rozpoznávání vyžaduje Premium")
                return@launch
            }
            _capturedImageUri.value = uri
            _uiState.value = FoodLogUiState.Processing
            processImage(uri)
        }
    }

    private fun processImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val results = recognizeFoodFromImageUseCase(uri)
                _uiState.value = if (results.isNotEmpty()) {
                    FoodLogUiState.RecognitionResult(results)
                } else {
                    FoodLogUiState.Error("Jídlo se nepodařilo rozpoznat, zkus jiný záběr")
                }
            } catch (e: Exception) {
                _uiState.value = FoodLogUiState.Error("Rozpoznání selhalo: ${e.message}")
            }
        }
    }

    fun onBarcodeScanned(barcode: String) {
        if (_uiState.value !is FoodLogUiState.Idle) return
        viewModelScope.launch {
            _uiState.value = FoodLogUiState.Processing
            try {
                val food = foodRepository.getFoodByBarcode(barcode)
                if (food != null) {
                    _uiState.value = FoodLogUiState.RecognitionResult(listOf(food))
                } else {
                    _uiState.value = FoodLogUiState.Error("Produkt nenalezen v databázi")
                }
            } catch (e: Exception) {
                _uiState.value = FoodLogUiState.Error("Chyba při vyhledávání: ${e.message}")
            }
        }
    }

    fun onFoodSelected(food: RecognizedFood) {
        _uiState.value = FoodLogUiState.Confirmed(food)
    }

    fun saveFood(food: RecognizedFood, notes: String = "") {
        viewModelScope.launch {
            try {
                val log = FoodLog(
                    foodName = food.name,
                    allergens = food.allergens,
                    ingredients = food.ingredients,
                    imageUri = _capturedImageUri.value?.toString(),
                    notes = notes,
                    timestamp = _customTimestamp.value
                )
                saveFoodLogUseCase(log)
                analyticsManager.logFoodLogged(food.name, food.allergens.size)
                _uiState.value = FoodLogUiState.Saved
            } catch (e: Exception) {
                _uiState.value = FoodLogUiState.Error("Uložení selhalo: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = FoodLogUiState.Idle
        _capturedImageUri.value = null
        _searchState.value = FoodSearchState()
        _customTimestamp.value = System.currentTimeMillis()
    }
}
