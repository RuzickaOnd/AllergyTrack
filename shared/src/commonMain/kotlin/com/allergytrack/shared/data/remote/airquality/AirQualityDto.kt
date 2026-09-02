package com.allergytrack.shared.data.remote.airquality

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Open-Meteo Air Quality API — zdarma, bez API klíče
// Dokumentace: https://open-meteo.com/en/docs/air-quality-api
// Pokrývá: AQI, PM, plyny + pylová data pro Evropu (sezónně)
@Serializable
data class AirQualityResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    @SerialName("hourly") val hourly: AirQualityHourly? = null,
    @SerialName("current") val current: AirQualityCurrent? = null
)

// Aktuální hodnoty (current endpoint)
@Serializable
data class AirQualityCurrent(
    val time: String = "",
    @SerialName("european_aqi") val europeanAqi: Int? = null,
    @SerialName("pm2_5") val pm25: Float? = null,
    @SerialName("pm10") val pm10: Float? = null,
    @SerialName("nitrogen_dioxide") val no2: Float? = null,
    @SerialName("ozone") val ozone: Float? = null,
    @SerialName("grass_pollen") val grassPollen: Float? = null,
    @SerialName("birch_pollen") val birchPollen: Float? = null,
    @SerialName("alder_pollen") val alderPollen: Float? = null,
    @SerialName("mugwort_pollen") val mugwortPollen: Float? = null,
    @SerialName("ragweed_pollen") val ragweedPollen: Float? = null
)

// Hodinová data (hourly endpoint) — pro historické grafy
@Serializable
data class AirQualityHourly(
    val time: List<String> = emptyList(),
    @SerialName("european_aqi") val europeanAqi: List<Int?> = emptyList(),
    @SerialName("pm2_5") val pm25: List<Float?> = emptyList(),
    @SerialName("grass_pollen") val grassPollen: List<Float?> = emptyList(),
    @SerialName("birch_pollen") val birchPollen: List<Float?> = emptyList(),
    @SerialName("alder_pollen") val alderPollen: List<Float?> = emptyList(),
    @SerialName("mugwort_pollen") val mugwortPollen: List<Float?> = emptyList(),
    @SerialName("ragweed_pollen") val ragweedPollen: List<Float?> = emptyList()
)

// Helper: převod µg/m³ na index 0–5 (kompatibilní s původním Google Pollen formátem)
// Prahové hodnoty dle CAMS / EEA doporučení pro trávu:
// 0 = 0, 1 = <10, 2 = <30, 3 = <80, 4 = <200, 5 = 200+
fun Float?.toPollenIndex(): Int = when {
    this == null -> 0
    this < 1f   -> 0
    this < 10f  -> 1
    this < 30f  -> 2
    this < 80f  -> 3
    this < 200f -> 4
    else        -> 5
}
