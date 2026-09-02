package com.allergytrack.shared.data.remote.airquality

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Open-Meteo Air Quality API. Bez klíče, zdarma.
 *
 * OMEZENÍ POKRYTÍ: Tento service používá CAMS model (Copernicus Atmosphere
 * Monitoring Service), který je provozován EU a pokrývá POUZE Evropu.
 *
 *  - `european_aqi` → CAMS European AQI, dostupný POUZE v Evropě
 *    (lat 34–72, lon -25–45)
 *  - pylová data (grass_pollen, birch_pollen, …) → EU + sezónně (únor–říjen)
 *  - pm2_5, no2, ozone → rovněž z CAMS, primárně EU
 *
 * Pro non-EU souřadnice API vrátí null pro všechna tato pole. Proto v aplikaci
 * nezobrazujeme sekci prostředí mimo Evropu — hodnota 0 by byla zavádějící.
 * Pro globální pokrytí by bylo třeba integrovat jiný zdroj (WAQI, IQAir
 * nebo Open-Meteo US-AQI endpoint).
 */
class AirQualityApi(
    private val client: HttpClient
) {
    /** Aktuální hodnoty — AQI i pyl v jednom volání. */
    suspend fun getCurrentAirQuality(
        latitude: Double,
        longitude: Double
    ): AirQualityResponse =
        client.get("$BASE_URL/v1/air-quality") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", CURRENT_FIELDS)
            parameter("timezone", "auto")
        }.body()


    companion object {
        private const val BASE_URL = "https://air-quality-api.open-meteo.com"

        private const val CURRENT_FIELDS =
            "european_aqi,pm2_5,pm10,nitrogen_dioxide,ozone," +
            "grass_pollen,birch_pollen,alder_pollen,mugwort_pollen,ragweed_pollen"

    }
}
