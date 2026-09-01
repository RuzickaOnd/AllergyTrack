package com.allergytrack.app.di.koin

import com.allergytrack.shared.domain.analysis.CorrelationEngine
import com.allergytrack.shared.domain.premium.PremiumHelper
import com.allergytrack.shared.domain.usecase.CheckPremiumAccessUseCase
import com.allergytrack.app.platform.RecognizeFoodFromImageUseCase
import com.allergytrack.shared.domain.usecase.RecognizeFoodFromImage
import com.allergytrack.shared.domain.usecase.SaveFoodLogUseCase
import com.allergytrack.shared.domain.usecase.SaveSymptomLogUseCase
import com.allergytrack.shared.domain.usecase.SearchFoodUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * PremiumHelper a CorrelationEngine byly @Singleton — zůstávají single.
 * Use case byly bez scope anotace (nový objekt při každé injekci) — proto factory.
 */
val domainModule = module {

    single { PremiumHelper(premiumRepository = get(), debugPreferences = get(), appConfig = get()) }
    single { CorrelationEngine(json = get()) }

    factory { SaveFoodLogUseCase(repository = get()) }
    factory { SaveSymptomLogUseCase(repository = get()) }
    factory { SearchFoodUseCase(repository = get()) }
    factory { CheckPremiumAccessUseCase(premiumHelper = get()) }
    factory<RecognizeFoodFromImage> { RecognizeFoodFromImageUseCase(context = androidContext(), repository = get()) }
}
