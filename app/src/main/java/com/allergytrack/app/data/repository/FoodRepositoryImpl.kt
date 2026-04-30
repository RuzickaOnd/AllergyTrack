package com.allergytrack.app.data.repository

import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.app.data.local.recognition.FoodImageRecognizer
import com.allergytrack.app.data.remote.food.OpenFoodFactsApiService
import com.allergytrack.app.data.remote.food.OpenFoodProduct
import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.domain.model.FoodLog
import com.allergytrack.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodLogDao: FoodLogDao,
    private val openFoodFactsApi: OpenFoodFactsApiService,
    private val json: Json,
    private val foodImageRecognizer: FoodImageRecognizer,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : FoodRepository {

    companion object {
        private val LANGUAGE_TO_COUNTRY = mapOf(
            "cs" to "cz", "sk" to "sk", "pl" to "pl", "de" to "de",
            "fr" to "fr", "es" to "es", "it" to "it", "hu" to "hu",
            "nl" to "nl", "pt" to "pt", "ro" to "ro", "en" to "gb"
        )
    }

    private suspend fun resolvedLocale(): Pair<String, String> {
        val saved = userPreferencesDataStore.foodSearchLanguage.first()
        return if (saved.isEmpty()) {
            val locale = Locale.getDefault()
            Pair(locale.language.lowercase(), locale.country.lowercase())
        } else {
            Pair(saved, LANGUAGE_TO_COUNTRY.getOrDefault(saved, saved))
        }
    }

    private fun OpenFoodProduct.localizedName(lc: String, fallback: String): String =
        if (lc == "cs") productNameCs ?: productName ?: fallback
        else productName ?: fallback

    private fun OpenFoodProduct.localizedIngredients(lc: String): List<String> {
        val text = if (lc == "cs") ingredientsTextCs ?: ingredientsText else ingredientsText
        return text?.split(",")?.map { it.trim() } ?: emptyList()
    }

    private val startOfDay: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    override fun getAllLogs(): Flow<List<FoodLog>> =
        foodLogDao.getAllLogs().map { entities ->
            entities.map { it.toDomain(json) }
        }

    override fun getTodayLogs(): Flow<List<FoodLog>> =
        foodLogDao.getTodayLogs(startOfDay).map { entities ->
            entities.map { it.toDomain(json) }
        }

    override suspend fun saveLog(log: FoodLog) {
        foodLogDao.insert(log.toEntity(json))
    }

    override suspend fun deleteLog(id: String) {
        foodLogDao.deleteById(id)
    }

    override suspend fun searchFoodByName(query: String): List<RecognizedFood> {
        return try {
            val (lc, cc) = resolvedLocale()
            val response = openFoodFactsApi.searchProducts(query, lc, cc)
            response.products.take(10).map { product ->
                RecognizedFood(
                    name = product.localizedName(lc, query),
                    allergens = product.allergensTags.map { tag ->
                        tag.removePrefix("en:").replaceFirstChar { it.uppercase() }
                    },
                    ingredients = product.localizedIngredients(lc)
                )
            }
        } catch (e: HttpException) {
            throw e  // HTTP chyby (včetně 503) propagujeme do ViewModelu
        } catch (e: Exception) {
            emptyList()  // síťové/IO chyby vracejí prázdný seznam
        }
    }

    override suspend fun getFoodByBarcode(barcode: String): RecognizedFood? {
        return try {
            val (lc, cc) = resolvedLocale()
            val response = openFoodFactsApi.getProductByBarcode(barcode, lc, cc)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                RecognizedFood(
                    name = product.localizedName(lc, "Neznámý produkt"),
                    allergens = product.allergensTags.map { tag ->
                        tag.removePrefix("en:").replaceFirstChar { it.uppercase() }
                    },
                    ingredients = product.localizedIngredients(lc),
                    imageUrl = product.imageUrl,
                    barcode = barcode
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun recognizeFoodFromImage(imageBytes: ByteArray): List<RecognizedFood> {
        val labels = foodImageRecognizer.recognize(imageBytes)
        if (labels.isEmpty()) return emptyList()

        val topLabel = labels.first()
        val searchResults = searchFoodByName(topLabel.text)

        return if (searchResults.isNotEmpty()) {
            listOf(searchResults.first().copy(confidence = topLabel.confidence))
        } else {
            listOf(RecognizedFood(name = topLabel.text, confidence = topLabel.confidence))
        }
    }

    // Mapper extension functions
    private fun FoodLogEntity.toDomain(json: Json): FoodLog = FoodLog(
        id = java.util.UUID.fromString(this.id),
        timestamp = this.timestamp,
        foodName = this.foodName,
        allergens = if (this.allergens.isNotEmpty())
            json.decodeFromString(this.allergens) else emptyList(),
        ingredients = if (this.ingredients.isNotEmpty())
            json.decodeFromString(this.ingredients) else emptyList(),
        latitude = this.latitude,
        longitude = this.longitude,
        imageUri = this.imageUri,
        notes = this.notes
    )

    private fun FoodLog.toEntity(json: Json): FoodLogEntity = FoodLogEntity(
        id = this.id.toString(),
        timestamp = this.timestamp,
        foodName = this.foodName,
        allergens = json.encodeToString(this.allergens),
        ingredients = json.encodeToString(this.ingredients),
        latitude = this.latitude,
        longitude = this.longitude,
        imageUri = this.imageUri,
        notes = this.notes
    )
}
