package com.allergytrack.app.di.koin

import com.allergytrack.app.domain.analysis.CorrelationEngine
import com.allergytrack.app.domain.premium.PremiumHelper
import com.allergytrack.app.domain.usecase.CheckPremiumAccessUseCase
import com.allergytrack.app.domain.usecase.RecognizeFoodFromImageUseCase
import com.allergytrack.app.domain.usecase.SaveFoodLogUseCase
import com.allergytrack.app.domain.usecase.SaveSymptomLogUseCase
import com.allergytrack.app.domain.usecase.SearchFoodUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * PremiumHelper a CorrelationEngine byly @Singleton — zůstávají single.
 * Use case byly bez scope anotace (nový objekt při každé injekci) — proto factory.
 */
val domainModule = module {

    single { PremiumHelper(premiumRepository = get(), debugPreferences = get()) }
    single { CorrelationEngine(json = get()) }

    factory { SaveFoodLogUseCase(repository = get()) }
    factory { SaveSymptomLogUseCase(repository = get()) }
    factory { SearchFoodUseCase(repository = get()) }
    factory { CheckPremiumAccessUseCase(premiumHelper = get()) }
    factory { RecognizeFoodFromImageUseCase(context = androidContext(), repository = get()) }
}
