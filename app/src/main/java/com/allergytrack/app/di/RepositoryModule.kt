package com.allergytrack.app.di

import com.allergytrack.app.data.repository.EnvironmentRepositoryImpl
import com.allergytrack.app.data.repository.FoodRepositoryImpl
import com.allergytrack.app.data.repository.SymptomRepositoryImpl
import com.allergytrack.app.domain.repository.EnvironmentRepository
import com.allergytrack.app.domain.repository.FoodRepository
import com.allergytrack.app.domain.repository.SymptomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindSymptomRepository(
        impl: SymptomRepositoryImpl
    ): SymptomRepository

    @Binds
    @Singleton
    abstract fun bindEnvironmentRepository(
        impl: EnvironmentRepositoryImpl
    ): EnvironmentRepository

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: com.allergytrack.app.data.repository.PremiumRepositoryImpl
    ): com.allergytrack.app.domain.repository.PremiumRepository
}
