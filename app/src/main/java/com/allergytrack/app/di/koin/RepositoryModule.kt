package com.allergytrack.app.di.koin

import com.allergytrack.app.data.repository.AppUpdateRepositoryImpl
import com.allergytrack.shared.data.repository.EnvironmentRepositoryImpl
import com.allergytrack.shared.data.repository.FoodRepositoryImpl
import com.allergytrack.app.data.repository.PremiumRepositoryImpl
import com.allergytrack.shared.data.repository.SymptomRepositoryImpl
import com.allergytrack.shared.domain.repository.AppUpdateRepository
import com.allergytrack.shared.domain.repository.EnvironmentRepository
import com.allergytrack.shared.domain.repository.FoodRepository
import com.allergytrack.shared.domain.repository.PremiumRepository
import com.allergytrack.shared.domain.repository.SymptomRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<FoodRepository> {
        FoodRepositoryImpl(
            foodLogDao = get(),
            supabaseFoodApi = get(),
            json = get(),
            foodImageRecognizer = get(),
            userPreferencesDataStore = get()
        )
    }

    single<SymptomRepository> {
        SymptomRepositoryImpl(symptomLogDao = get(), json = get())
    }

    single<EnvironmentRepository> {
        EnvironmentRepositoryImpl(
            environmentLogDao = get(),
            airQualityApi = get(),
            locationProvider = get()
        )
    }

    single<PremiumRepository> { PremiumRepositoryImpl() }
    single<AppUpdateRepository> { AppUpdateRepositoryImpl() }
}
