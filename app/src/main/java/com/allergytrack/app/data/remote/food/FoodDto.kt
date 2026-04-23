package com.allergytrack.app.data.remote.food

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Open Food Facts API — bezplatné, bez API klíče, globální databáze
@Serializable
data class OpenFoodFactsResponse(
    val status: Int = 0,
    val product: OpenFoodProduct? = null
)

@Serializable
data class OpenFoodProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_cs") val productNameCs: String? = null,
    @SerialName("allergens_tags") val allergensTags: List<String> = emptyList(),
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("ingredients_text_cs") val ingredientsTextCs: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val nutriments: Nutriments? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal_100g") val energyKcal: Float? = null,
    @SerialName("proteins_100g") val proteins: Float? = null,
    @SerialName("carbohydrates_100g") val carbohydrates: Float? = null,
    @SerialName("fat_100g") val fat: Float? = null
)

// Model pro výsledek rozpoznání jídla
data class RecognizedFood(
    val name: String,
    val confidence: Float = 1.0f,
    val allergens: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val imageUrl: String? = null,
    val barcode: String? = null
)
