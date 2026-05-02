package com.allergytrack.app.data.remote.food

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface pro Supabase REST API tabulky food_products.
 *
 * Base URL: BuildConfig.SUPABASE_URL (nastaveno v NetworkModule)
 * Autentizace: OkHttp interceptor přidá hlavičky apikey + Authorization.
 *
 * Poznámka k lokalizaci:
 *   Na rozdíl od OFF API Supabase neakceptuje parametr jazyka.
 *   Oba jazykové sloupce (product_name + product_name_cs) jsou vždy
 *   součástí response — výběr správného probíhá v mapper funkci.
 */
interface SupabaseFoodApiService {

    /**
     * Lookup produktu podle čárového kódu (EAN).
     * Vrátí prázdné pole pokud produkt v databázi není.
     *
     * Supabase REST filtr: code=eq.{hodnota}
     */
    @GET("rest/v1/food_products")
    suspend fun getProductByBarcode(
        @Query("code")   barcodeFilter: String,  // formát: "eq.{barcode}"
        @Query("select") select: String = PRODUCT_SELECT,
        @Query("limit")  limit: Int = 1
    ): List<SupabaseFoodProduct>

    /**
     * Full-text vyhledávání podle názvu produktu.
     * Volá PostgreSQL funkci search_products() ze Supabase schématu.
     * Funkce prohledává oba jazykové sloupce současně.
     */
    @GET("rest/v1/rpc/search_products")
    suspend fun searchProducts(
        @Query("query")        query: String,
        @Query("result_limit") limit: Int = 20
    ): List<SupabaseSearchResult>

    companion object {
        // Sloupce pro barcode lookup — oba jazykové sloupce vždy
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
