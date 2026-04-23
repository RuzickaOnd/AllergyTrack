package com.allergytrack.app.di

import com.allergytrack.app.data.remote.airquality.AirQualityApiService
import com.allergytrack.app.data.remote.food.OpenFoodFactsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    @Named("openmeteo")
    fun provideOpenMeteoRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @Named("openfoodfacts")
    fun provideOpenFoodFactsRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAirQualityService(@Named("openmeteo") retrofit: Retrofit): AirQualityApiService =
        retrofit.create(AirQualityApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenFoodFactsService(
        @Named("openfoodfacts") retrofit: Retrofit
    ): OpenFoodFactsApiService =
        retrofit.create(OpenFoodFactsApiService::class.java)
}
