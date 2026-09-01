package com.allergytrack.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.app.domain.premium.PremiumHelper
import com.allergytrack.app.data.analytics.AnalyticsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val premiumHelper: PremiumHelper,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    init {
        analyticsManager.logScreenView("profile", this::class.java.simpleName)
    }

    val userName: StateFlow<String> = userPreferencesDataStore.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Anonym")

    val avatarSelection: StateFlow<String> = userPreferencesDataStore.avatarSelection
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "person")

    val isPremiumUser: StateFlow<Boolean> = premiumHelper
        .observeAccess(PremiumFeature.AI_CORRELATION_ANALYSIS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPremiumVisible: StateFlow<Boolean> = premiumHelper
        .observeVisible(PremiumFeature.AI_CORRELATION_ANALYSIS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setUserName(name: String) {
        viewModelScope.launch { userPreferencesDataStore.setUserName(name.trim()) }
    }

    fun setAvatarSelection(selection: String) {
        viewModelScope.launch { userPreferencesDataStore.setAvatarSelection(selection) }
    }
}
