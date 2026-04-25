package com.allergytrack.app.data.remote.airquality

import retrofit2.http.GET
import retrofit2.http.Query

// OMEZENÍ POKRYTÍ: Tento service používá Open-Meteo Air Quality API s CAMS modelem
// (Copernicus Atmosphere Monitoring Service), který je provozován EU a pokrývá POUZE Evropu.
//
// Konkrétně:
//  - `european_aqi` → CAMS European AQI index, dostupný POUZE v Evropě (lat 34–72, lon -25–45)
//  - Pylová data (grass_pollen, birch_pollen, …) → EU + sezónně (únor–říjen)
//  - pm2_5, no2, ozone → rovněž z CAMS, primárně EU
//
// Pro non-EU souřadnice API vrátí null pro všechna tato pole. Proto v aplikaci
// nezobrazujeme sekci prostředí mimo Evropu — hodnota 0 by byla zavádějící.
// Pokud by bylo potřeba globální pokrytí, je třeba integrovat jiný zdroj
// (např. WAQI API, IQAir nebo Open-Meteo US-AQI endpoint).
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
