package com.allergytrack.shared.platform

/** Aktuální čas v milisekundách od epochy. Náhrada za System.currentTimeMillis(). */
expect fun currentTimeMillis(): Long

/** ISO 639-1 kód jazyka zařízení, malými písmeny (např. "cs"). */
expect fun deviceLanguage(): String

/** ISO 3166-1 kód země zařízení, malými písmeny (např. "cz"). Prázdný, pokud není známa. */
expect fun deviceCountry(): String
