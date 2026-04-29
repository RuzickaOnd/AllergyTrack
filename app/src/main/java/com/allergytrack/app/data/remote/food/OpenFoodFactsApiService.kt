package com.allergytrack.app.data.remote.food

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.Serializable

interface OpenFoodFactsApiService {

    @GET("api/v0/product/{barcode}.json?fields=product_name,product_name_cs,allergens_tags,ingredients_text,ingredients_text_cs,image_url")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("lc") languageCode: String,
        @Query("cc") countryCode: String
    ): OpenFoodFactsResponse

    @GET("cgi/search.pl?search_simple=1&action=process&json=1&sort_by=unique_scans_n&fields=product_name,product_name_cs,allergens_tags,ingredients_text,ingredients_text_cs,image_url")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("lc") languageCode: String,
        @Query("cc") countryCode: String
    ): OpenFoodSearchResponse
}

@Serializable
data class OpenFoodSearchResponse(
    val count: Int = 0,
    val products: List<OpenFoodProduct> = emptyList()
)
