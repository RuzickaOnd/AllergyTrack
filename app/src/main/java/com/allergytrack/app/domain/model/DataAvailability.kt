package com.allergytrack.app.domain.model

data class DataAvailability(
    val hasAqi: Boolean = false,
    val hasPollen: Boolean = false,
    val hasGps: Boolean = false,
    val region: EnvironmentRegion = EnvironmentRegion.UNKNOWN
)

enum class EnvironmentRegion {
    EUROPE,      // Plná data: AQI + pyl
    GLOBAL,      // Částečná data: pouze AQI
    UNKNOWN      // Ještě nenačteno
}

// Extension pro určení regionu z GPS souřadnic
// Jednoduchý bounding box pro Evropu (včetně Turecka, Islandu, Kypru)
fun isEurope(latitude: Double, longitude: Double): Boolean =
    latitude in 34.0..72.0 && longitude in -25.0..45.0
