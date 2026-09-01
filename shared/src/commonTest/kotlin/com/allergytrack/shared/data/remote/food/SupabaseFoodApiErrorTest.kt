package com.allergytrack.shared.data.remote.food

import com.allergytrack.shared.config.AppConfig
import com.allergytrack.shared.data.remote.ApiHttpException
import com.allergytrack.shared.data.remote.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Ověřuje, že HTTP chyby ze Supabase dorazí k volajícímu jako ApiHttpException
 * se status kódem.
 *
 * Dřív se výjimka ztratila: FoodRepositoryImpl chytal Exception a vracel
 * prázdný seznam, takže ošetření stavu 503 ve FoodLogViewModelu bylo
 * nedosažitelné. Viz docs/kmp-stav.md, seznam zděděných chyb.
 */
class SupabaseFoodApiErrorTest {

    private val config = AppConfig(
        supabaseUrl = "https://test.supabase.co",
        supabaseKey = "test-key",
        isDebug = true,
        versionName = "1.0.0"
    )

    /** Stejná konfigurace, jakou dostane produkční klient. */
    private fun newClient(engine: MockEngine) =
        createHttpClient(engine, Json { ignoreUnknownKeys = true }, logging = false)

    private fun apiReturning(status: HttpStatusCode): SupabaseFoodApi {
        val engine = MockEngine { respondError(status) }
        return SupabaseFoodApi(newClient(engine), config)
    }

    @Test
    fun searchVyhodiApiHttpExceptionSeStatusem503() = runTest {
        val api = apiReturning(HttpStatusCode.ServiceUnavailable)
        val e = assertFailsWith<ApiHttpException> { api.searchProducts("jogurt") }
        assertEquals(503, e.statusCode)
    }

    @Test
    fun searchVyhodiApiHttpExceptionIProJineChyby() = runTest {
        val api = apiReturning(HttpStatusCode.InternalServerError)
        val e = assertFailsWith<ApiHttpException> { api.searchProducts("jogurt") }
        assertEquals(500, e.statusCode)
    }

    @Test
    fun barcodeLookupVyhodiApiHttpException() = runTest {
        val api = apiReturning(HttpStatusCode.ServiceUnavailable)
        val e = assertFailsWith<ApiHttpException> { api.getProductByBarcode("123") }
        assertEquals(503, e.statusCode)
    }

    @Test
    fun uspesnaOdpovedSeNaparsuje() = runTest {
        val body = """[{"code":"123","product_name":"Yogurt","product_name_cs":"Jogurt"}]"""
        val engine = MockEngine {
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val result = SupabaseFoodApi(newClient(engine), config).getProductByBarcode("123")
        assertEquals(1, result.size)
        assertEquals("Jogurt", result.first().productNameCs)
    }

    @Test
    fun hlavickyObsahujiApiKlic() = runTest {
        var seenApiKey: String? = null
        val engine = MockEngine { request ->
            seenApiKey = request.headers["apikey"]
            respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        SupabaseFoodApi(newClient(engine), config).searchProducts("test")
        assertTrue(seenApiKey == "test-key", "Očekáván apikey header, byl: $seenApiKey")
    }
}
