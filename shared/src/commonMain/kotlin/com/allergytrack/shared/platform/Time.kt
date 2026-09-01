package com.allergytrack.shared.platform

/** Aktuální čas v milisekundách od epochy. Náhrada za System.currentTimeMillis(). */
expect fun currentTimeMillis(): Long

/** ISO 639-1 kód jazyka zařízení, malými písmeny (např. "cs"). */
expect fun deviceLanguage(): String

/** ISO 3166-1 kód země zařízení, malými písmeny (např. "cz"). Prázdný, pokud není známa. */
expect fun deviceCountry(): String

/** Náhodné UUID jako řetězec — pro primární klíče Room entit. */
expect fun randomUuidString(): String

/**
 * Půlnoc dnešního dne v místní zóně, v milisekundách od epochy.
 * Náhrada za Calendar.getInstance() s vynulovanými poli.
 */
expect fun startOfToday(): Long
