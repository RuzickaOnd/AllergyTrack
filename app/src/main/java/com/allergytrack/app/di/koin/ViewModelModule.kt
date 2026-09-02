package com.allergytrack.app.di.koin

import com.allergytrack.shared.presentation.*
import com.allergytrack.shared.presentation.OnboardingViewModel
import com.allergytrack.shared.presentation.AnalysisViewModel
import com.allergytrack.shared.presentation.CorrelationDetailViewModel
import com.allergytrack.shared.presentation.FoodTriggerDetailViewModel
import com.allergytrack.shared.presentation.ChartsViewModel
import com.allergytrack.shared.presentation.SymptomDetailViewModel
import com.allergytrack.shared.presentation.DashboardViewModel
import com.allergytrack.app.ui.screens.debug.DebugMenuViewModel
import com.allergytrack.shared.presentation.FoodLogViewModel
import com.allergytrack.shared.presentation.PremiumViewModel
import com.allergytrack.shared.presentation.ProfileViewModel
import com.allergytrack.shared.presentation.SettingsViewModel
import com.allergytrack.shared.presentation.SymptomLogViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Všech 13 ViewModelů.
 *
 * SavedStateHandle se uvnitř viewModel {} bloku resolvuje přes get() —
 * Koin ho dodá z ViewModelStoreOwner stejně, jako to dělal Hilt.
 */
val viewModelModule = module {

    viewModel {
        DashboardViewModel(
            foodLogDao = get(),
            symptomLogDao = get(),
            environmentRepository = get(),
            permissionsHelper = get(),
            premiumHelper = get(),
            userPreferences = get(),
            analyticsManager = get(),
            correlationEngine = get(),
            onboardingDataStore = get()
        )
    }

    viewModel {
        FoodLogViewModel(
            saveFoodLogUseCase = get(),
            searchFoodUseCase = get(),
            recognizeFoodFromImageUseCase = get(),
            foodRepository = get(),
            premiumHelper = get(),
            analyticsManager = get()
        )
    }

    viewModel {
        SymptomLogViewModel(
            saveSymptomLogUseCase = get(),
            analyticsManager = get()
        )
    }

    viewModel {
        ChartsViewModel(
            symptomLogDao = get(),
            environmentLogDao = get(),
            premiumHelper = get(),
            permissionsHelper = get(),
            userPreferencesDataStore = get(),
            analyticsManager = get()
        )
    }

    viewModel {
        SymptomDetailViewModel(
            symptomLogDao = get(),
            premiumHelper = get(),
            savedStateHandle = get()
        )
    }

    viewModel {
        AnalysisViewModel(
            symptomLogDao = get(),
            foodLogDao = get(),
            environmentLogDao = get(),
            correlationEngine = get(),
            aiSummaryService = get(),
            onboardingDataStore = get(),
            medicalReportExporter = get(),
            premiumHelper = get(),
            debugPreferences = get(),
            analyticsManager = get(),
            appConfig = get()
        )
    }

    viewModel {
        CorrelationDetailViewModel(
            savedStateHandle = get(),
            symptomLogDao = get(),
            foodLogDao = get(),
            environmentLogDao = get(),
            onboardingDataStore = get(),
            correlationEngine = get()
        )
    }

    viewModel {
        FoodTriggerDetailViewModel(
            savedStateHandle = get(),
            foodLogDao = get(),
            symptomLogDao = get(),
            onboardingDataStore = get(),
            correlationEngine = get(),
            json = get()
        )
    }

    viewModel {
        SettingsViewModel(
            deviceInfo = get(),
            permissionsHelper = get(),
            userPreferencesDataStore = get(),
            workManagerScheduler = get(),
            foodLogDao = get(),
            symptomLogDao = get(),
            environmentLogDao = get(),
            onboardingDataStore = get(),
            correlationEngine = get(),
            aiSummaryService = get(),
            medicalReportExporter = get(),
            premiumHelper = get(),
            analyticsManager = get(),
            notificationManager = get()
        )
    }

    viewModel {
        ProfileViewModel(
            userPreferencesDataStore = get(),
            premiumHelper = get(),
            analyticsManager = get()
        )
    }

    viewModel {
        PremiumViewModel(
            premiumHelper = get(),
            analyticsManager = get()
        )
    }

    viewModel {
        OnboardingViewModel(
            onboardingDataStore = get(),
            permissionsHelper = get(),
            premiumHelper = get(),
            analyticsManager = get(),
            userPreferencesDataStore = get(),
            workManagerScheduler = get()
        )
    }

    viewModel {
        DebugMenuViewModel(
            debugPreferences = get(),
            onboardingDataStore = get(),
            foodRepository = get(),
            symptomRepository = get(),
            environmentRepository = get(),
            foodLogDao = get(),
            symptomLogDao = get(),
            environmentLogDao = get(),
            premiumHelper = get()
        )
    }
}
