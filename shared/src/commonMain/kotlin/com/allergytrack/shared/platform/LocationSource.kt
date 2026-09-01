package com.allergytrack.shared.platform

/** Souřadnice bez vazby na platformní Location typ. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Zdroj polohy. Implementuje LocationProvider v :app (Play Services Location).
 *
 * Obě metody vrací null, pokud poloha není dostupná — včetně případu,
 * kdy chybí oprávnění. Platformní implementace musí odchytit svoji
 * variantu bezpečnostní výjimky sama; commonMain SecurityException nezná.
 */
interface LocationSource {
    suspend fun getCurrentLocation(): Coordinates?
    suspend fun getLastKnownLocation(): Coordinates?
}
