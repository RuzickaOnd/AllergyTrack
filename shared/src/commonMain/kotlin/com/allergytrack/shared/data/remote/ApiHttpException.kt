package com.allergytrack.shared.data.remote

/**
 * Chyba HTTP volání se status kódem.
 *
 * Existuje proto, aby platformní vrstva mohla reagovat na HTTP stavy,
 * aniž by potřebovala Ktor na classpath — síťová knihovna zůstává
 * uzavřená uvnitř :shared.
 *
 * POZOR: aktuálně ji nic nevyhazuje. FoodRepositoryImpl.searchFoodByName()
 * chytá Exception a vrací emptyList(), takže se k volajícímu žádná síťová
 * výjimka nedostane. Platilo to i před KMP migrací (tehdy s
 * retrofit2.HttpException) — viz docs/kmp-stav.md, seznam zděděných chyb.
 */
class ApiHttpException(
    val statusCode: Int,
    cause: Throwable? = null
) : Exception("HTTP $statusCode", cause)
