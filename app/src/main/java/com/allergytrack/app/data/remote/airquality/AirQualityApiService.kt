package com.allergytrack.app.data.remote.airquality

import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApiService {

    // Aktuální hodnoty — AQI + pyl v jednom volání
    // Bez API klíče, zdarma, pokrytí EU včetně ČR
    @GET("v1/air-quality")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "european_aqi,pm2_5,pm10,nitrogen_dioxide,ozone," +
            "grass_pollen,birch_pollen,alder_pollen,mugwort_pollen,ragweed_pollen",
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse

    // Hodinová historická data — pro grafy (posledních 5 dní)
    @GET("v1/air-quality")
    suspend fun getHourlyHistory(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String =
            "european_aqi,pm2_5,grass_pollen,birch_pollen," +
            "alder_pollen,mugwort_pollen,ragweed_pollen",
        @Query("past_days") pastDays: Int = 5,
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse
}
