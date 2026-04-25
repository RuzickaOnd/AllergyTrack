package com.allergytrack.app.domain.model

data class DataAvailability(
    val hasAqi: Boolean = false,
    val hasPollen: Boolean = false,
    val hasGps: Boolean = false,
    val region: EnvironmentRegion = EnvironmentRegion.UNKNOWN
)

// POZNÁMKA K REGIONŮM: Aktuálně používané API (Open-Meteo CAMS) pokrývá POUZE Evropu.
// Hodnoty european_aqi a pylová data jsou pro non-EU souřadnice null.
// GLOBAL je rezervováno pro budoucí integraci globálního AQI zdroje.
// Do té doby se sekce prostředí v UI zobrazuje POUZE pro region EUROPE.
enum class EnvironmentRegion {
    EUROPE,   // Plná data: european_aqi + pyl (CAMS model, EU bounding box)
    GLOBAL,   // Rezervováno — žádný aktuální zdroj dat; API vrací null pro non-EU souřadnice
    UNKNOWN   // Ještě nenačteno / GPS nedostupné
}

// Extension pro určení regionu z GPS souřadnic
// Jednoduchý bounding box pro Evropu (včetně Turecka, Islandu, Kypru)
fun isEurope(latitude: Double, longitude: Double): Boolean =
    latitude in 34.0..72.0 && longitude in -25.0..45.0
