package com.allergytrack.app.di

import com.allergytrack.app.BuildConfig
import com.allergytrack.app.data.remote.airquality.AirQualityApiService
import com.allergytrack.app.data.remote.food.SupabaseFoodApiService
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
    fun provideAirQualityService(@Named("openmeteo") retrofit: Retrofit): AirQualityApiService =
        retrofit.create(AirQualityApiService::class.java)

    /**
     * Dedikovaný OkHttpClient + Retrofit pro Supabase.
     *
     * Supabase vyžaduje hlavičky apikey a Authorization u každého requestu.
     * Používáme @Named("supabase") aby Hilt rozlišil od případných jiných
     * Retrofit instancí v projektu.
     */
    @Provides
    @Singleton
    @Named("supabase")
    fun provideSupabaseRetrofit(json: Json): Retrofit {
        val supabaseClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                .build()
            chain.proceed(request)
        }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BASIC
                else
                    HttpLoggingInterceptor.Level.NONE
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/")
            .client(supabaseClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseFoodApiService(
        @Named("supabase") retrofit: Retrofit
    ): SupabaseFoodApiService = retrofit.create(SupabaseFoodApiService::class.java)
}
