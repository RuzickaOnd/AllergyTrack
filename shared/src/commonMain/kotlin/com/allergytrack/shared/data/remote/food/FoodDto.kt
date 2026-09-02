package com.allergytrack.shared.data.remote.food

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response z Supabase REST API — řádek z tabulky food_products.
 * Supabase vždy vrátí oba jazykové sloupce (cs + fallback en/default).
 * Výběr správného sloupce podle jazyka probíhá v mapper funkci.
 */
@Serializable
data class SupabaseFoodProduct(
    val code: String = "",
    @SerialName("product_name")        val productName: String? = null,
    @SerialName("product_name_cs")     val productNameCs: String? = null,
    @SerialName("allergens_tags")      val allergensTags: List<String> = emptyList(),
    @SerialName("ingredients_text")    val ingredientsText: String? = null,
    @SerialName("ingredients_text_cs") val ingredientsTextCs: String? = null,
    val brands: String? = null,
    @SerialName("categories_tags")     val categoriesTags: List<String> = emptyList(),
    @SerialName("nutriscore_grade")    val nutriscoreGrade: String? = null,
    @SerialName("nova_group")          val novaGroup: Int? = null,
    @SerialName("image_url")           val imageUrl: String? = null
)

/**
 * Response z Supabase RPC funkce search_products().
 * Obsahuje `rank` pro řazení výsledků full-text vyhledávání.
 */
@Serializable
data class SupabaseSearchResult(
    val code: String = "",
    @SerialName("product_name")    val productName: String? = null,
    @SerialName("product_name_cs") val productNameCs: String? = null,
    @SerialName("allergens_tags")  val allergensTags: List<String> = emptyList(),
    val brands: String? = null,
    @SerialName("nutriscore_grade") val nutriscoreGrade: String? = null,
    @SerialName("image_url")       val imageUrl: String? = null,
    val rank: Float = 0f
)

/**
 * Doménový model pro rozpoznané jídlo.
 * Sdílený výstup pro barcode lookup i textové vyhledávání.
 */
data class RecognizedFood(
    val name: String,
    val confidence: Float = 1.0f,
    val allergens: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val imageUrl: String? = null,
    val barcode: String? = null,
    val brand: String? = null,
    val nutriscoreGrade: String? = null,
    /** false = název je ve fallback jazyce (preferovaný překlad chybí v databázi) */
    val nameIsLocalized: Boolean = true
)

// ── Lokalizované mapper funkce ─────────────────────────────────────────────────

/**
 * Vrátí lokalizovaný název produktu podle jazyka uživatele.
 *
 * Logika priorit:
 *   cs → preferuj product_name_cs, fallback na product_name
 *   ostatní → preferuj product_name, fallback na product_name_cs
 *
 * @param languageCode ISO 639-1 kód jazyka ("cs", "de", "en", ...)
 * @param fallback Název použitý pokud nejsou dostupné žádné překlady
 */
fun SupabaseFoodProduct.localizedName(languageCode: String, fallback: String): String =
    if (languageCode == "cs") {
        productNameCs?.takeIf { it.isNotBlank() }
            ?: productName?.takeIf { it.isNotBlank() }
            ?: fallback
    } else {
        productName?.takeIf { it.isNotBlank() }
            ?: productNameCs?.takeIf { it.isNotBlank() }
            ?: fallback
    }

/**
 * Vrátí lokalizovaný seznam ingrediencí.
 * Databáze obsahuje ingredience v češtině (cs) a defaultním jazyce.
 */
fun SupabaseFoodProduct.localizedIngredients(languageCode: String): List<String> {
    val text = if (languageCode == "cs") {
        ingredientsTextCs?.takeIf { it.isNotBlank() } ?: ingredientsText
    } else {
        ingredientsText?.takeIf { it.isNotBlank() } ?: ingredientsTextCs
    }
    return text?.split(",", ";")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
}

/**
 * Vrátí lokalizovaný název z výsledku vyhledávání.
 * SearchResult má méně polí než plný produkt — bez ingrediencí.
 */
fun SupabaseSearchResult.localizedName(languageCode: String, fallback: String): String =
    if (languageCode == "cs") {
        productNameCs?.takeIf { it.isNotBlank() }
            ?: productName?.takeIf { it.isNotBlank() }
            ?: fallback
    } else {
        productName?.takeIf { it.isNotBlank() }
            ?: productNameCs?.takeIf { it.isNotBlank() }
            ?: fallback
    }

// ── Mapper do doménového modelu ────────────────────────────────────────────────

/**
 * Převod Supabase produktu na doménový model s lokalizací.
 * @param languageCode Jazyk z UserPreferences ("cs", "de", "en", ...)
 */
fun SupabaseFoodProduct.toRecognizedFood(languageCode: String): RecognizedFood {
    val cleanedAllergens = allergensTags.map { tag ->
        tag.removePrefix("en:").replaceFirstChar { it.uppercase() }
    }
    val nameLocalized = if (languageCode == "cs") !productNameCs.isNullOrBlank() else !productName.isNullOrBlank()
    return RecognizedFood(
        name = localizedName(languageCode, "Neznámý produkt"),
        allergens = cleanedAllergens,
        ingredients = localizedIngredients(languageCode),
        imageUrl = imageUrl,
        barcode = code,
        brand = brands,
        nutriscoreGrade = nutriscoreGrade,
        nameIsLocalized = nameLocalized
    )
}

/**
 * Převod výsledku vyhledávání na doménový model s lokalizací.
 * @param languageCode Jazyk z UserPreferences
 * @param originalQuery Původní vyhledávací dotaz — použit jako fallback název
 */
fun SupabaseSearchResult.toRecognizedFood(
    languageCode: String,
    originalQuery: String = ""
): RecognizedFood {
    val cleanedAllergens = allergensTags.map { tag ->
        tag.removePrefix("en:").replaceFirstChar { it.uppercase() }
    }
    val nameLocalized = if (languageCode == "cs") !productNameCs.isNullOrBlank() else !productName.isNullOrBlank()
    return RecognizedFood(
        name = localizedName(languageCode, originalQuery.ifBlank { "Neznámý produkt" }),
        confidence = rank,
        allergens = cleanedAllergens,
        imageUrl = imageUrl,
        barcode = code,
        brand = brands,
        nutriscoreGrade = nutriscoreGrade,
        nameIsLocalized = nameLocalized
    )
}
