package com.allergytrack.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Nahrazuje bývalý provideOkHttpClient + Retrofit.Builder z Hilt NetworkModule.
 *
 * Konfiguraci sdílí obě přetížení, aby test s MockEngine nemohl ověřovat
 * jinou konfiguraci, než jakou dostane produkční klient.
 *
 * @param logging true = LogLevel.BODY (odpovídá dřívějšímu
 *                HttpLoggingInterceptor.Level.BODY pro Open-Meteo),
 *                false = LogLevel.NONE
 */
private fun HttpClientConfig<*>.applyCommonConfig(json: Json, logging: Boolean) {
    // Bez expectSuccess Ktor nehází ResponseException na non-2xx odpovědi
    // a ošetření HTTP chyb v API vrstvě by bylo nedosažitelné.
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        level = if (logging) LogLevel.BODY else LogLevel.NONE
        // Anon klíč Supabase nesmí skončit v logu ani v debug buildu.
        sanitizeHeader { header ->
            header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                header.equals("apikey", ignoreCase = true)
        }
    }
}

fun createHttpClient(
    engine: HttpClientEngineFactory<*>,
    json: Json,
    logging: Boolean
): HttpClient = HttpClient(engine) {
    applyCommonConfig(json, logging)
}

/**
 * Varianta pro hotovou instanci enginu — používá ji test s MockEngine,
 * aby ověřoval tutéž konfiguraci, jakou dostane produkční klient.
 */
fun createHttpClient(
    engine: HttpClientEngine,
    json: Json,
    logging: Boolean
): HttpClient = HttpClient(engine) {
    applyCommonConfig(json, logging)
}
