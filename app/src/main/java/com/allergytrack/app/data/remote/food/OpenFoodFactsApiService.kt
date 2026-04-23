package com.allergytrack.app.data.remote.food

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.Serializable

interface OpenFoodFactsApiService {

    // Vyhledání produktu podle čárového kódu
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse

    // Vyhledání produktu podle názvu
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&fields=product_name,allergens_tags,ingredients_text,image_url")
    suspend fun searchProducts(
        @Query("search_terms") query: String
    ): OpenFoodSearchResponse
}

@Serializable
data class OpenFoodSearchResponse(
    val count: Int = 0,
    val products: List<OpenFoodProduct> = emptyList()
)
