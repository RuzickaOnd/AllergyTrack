package com.allergytrack.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.permissions.PermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val permissionsHelper: PermissionsHelper,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _permissionsState = MutableStateFlow(permissionsHelper.getPermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    val foodSearchLanguage: StateFlow<String> = userPreferencesDataStore.foodSearchLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun refreshPermissions() {
        _permissionsState.update { permissionsHelper.getPermissionsState() }
    }

    fun setFoodSearchLanguage(code: String) {
        viewModelScope.launch { userPreferencesDataStore.setFoodSearchLanguage(code) }
    }
}
