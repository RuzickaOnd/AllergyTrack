package com.allergytrack.shared.data.remote.food

import com.allergytrack.shared.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * Supabase REST API nad tabulkou food_products.
 *
 * Autentizace: hlavičky apikey + Authorization s anon klíčem. Dřív je přidával
 * OkHttp interceptor v NetworkModule; teď se přidávají explicitně u každého
 * requestu, protože Ktor klient je sdílený.
 *
 * Poznámka k lokalizaci: Supabase neakceptuje parametr jazyka. Oba jazykové
 * sloupce jsou vždy v response, výběr probíhá v mapper funkcích ve FoodDto.
 */
class SupabaseFoodApi(
    private val client: HttpClient,
    private val config: AppConfig
) {
    /**
     * Lookup podle čárového kódu (EAN). Prázdný seznam = produkt není v databázi.
     * Supabase REST filtr má tvar code=eq.{hodnota}.
     */
    suspend fun getProductByBarcode(barcode: String): List<SupabaseFoodProduct> =
        client.get("${config.supabaseUrl}/rest/v1/food_products") {
            supabaseHeaders()
            parameter("code", "eq.$barcode")
            parameter("select", PRODUCT_SELECT)
            parameter("limit", 1)
        }.body()

    /**
     * Full-text vyhledávání přes PostgreSQL funkci search_products().
     * Prohledává oba jazykové sloupce současně.
     */
    suspend fun searchProducts(
        query: String,
        limit: Int = 20
    ): List<SupabaseSearchResult> =
        client.get("${config.supabaseUrl}/rest/v1/rpc/search_products") {
            supabaseHeaders()
            parameter("query", query)
            parameter("result_limit", limit)
        }.body()

    private fun HttpRequestBuilder.supabaseHeaders() {
        header("apikey", config.supabaseKey)
        header("Authorization", "Bearer ${config.supabaseKey}")
    }

    companion object {
        /** Sloupce pro barcode lookup — oba jazykové sloupce vždy. */
        const val PRODUCT_SELECT =
            "code," +
            "product_name," +
            "product_name_cs," +
            "allergens_tags," +
            "ingredients_text," +
            "ingredients_text_cs," +
            "brands," +
            "nutriscore_grade," +
            "nova_group," +
            "image_url"
    }
}
