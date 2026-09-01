package com.allergytrack.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Nahrazuje bývalý provideOkHttpClient + Retrofit.Builder z Hilt NetworkModule.
 *
 * @param logging true = LogLevel.BODY (odpovídá dřívějšímu
 *                HttpLoggingInterceptor.Level.BODY pro Open-Meteo),
 *                false = LogLevel.NONE
 */
fun createHttpClient(
    engine: HttpClientEngineFactory<*>,
    json: Json,
    logging: Boolean
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        level = if (logging) LogLevel.BODY else LogLevel.NONE
    }
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
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        level = if (logging) LogLevel.BODY else LogLevel.NONE
    }
}
