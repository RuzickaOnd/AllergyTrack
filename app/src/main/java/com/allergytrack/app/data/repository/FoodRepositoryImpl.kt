package com.allergytrack.app.data.repository

import com.allergytrack.app.data.local.database.dao.FoodLogDao
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.app.data.local.recognition.FoodImageRecognizer
import com.allergytrack.app.data.remote.food.SupabaseFoodApiService
import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.shared.data.remote.food.toRecognizedFood
import com.allergytrack.shared.data.remote.food.toRecognizedFood
import com.allergytrack.shared.domain.model.FoodLog
import com.allergytrack.shared.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalUuidApi::class)
class FoodRepositoryImpl(
    private val foodLogDao: FoodLogDao,
    private val supabaseFoodApi: SupabaseFoodApiService,   // ← Supabase místo OFF
    private val json: Json,
    private val foodImageRecognizer: FoodImageRecognizer,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : FoodRepository {

    companion object {
        // Mapování ISO 639-1 jazyka → kód země (zachováno pro budoucí použití)
        private val LANGUAGE_TO_COUNTRY = mapOf(
            "cs" to "cz", "sk" to "sk", "pl" to "pl", "de" to "de",
            "fr" to "fr", "es" to "es", "it" to "it", "hu" to "hu",
            "nl" to "nl", "pt" to "pt", "ro" to "ro", "en" to "gb"
        )
    }

    /**
     * Načte jazyk z UserPreferences nebo použije systémový default.
     * Vrací Pair(languageCode, countryCode) — stejné jako původní implementace.
     *
     * Poznámka: countryCode se u Supabase nepoužívá (databáze filtr nemá),
     * ale zachováváme ho pro konzistenci a případné budoucí použití.
     */
    private suspend fun resolvedLocale(): Pair<String, String> {
        val saved = userPreferencesDataStore.foodSearchLanguage.first()
        return if (saved.isEmpty()) {
            val locale = Locale.getDefault()
            Pair(locale.language.lowercase(), locale.country.lowercase())
        } else {
            Pair(saved, LANGUAGE_TO_COUNTRY.getOrDefault(saved, saved))
        }
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

    // ── DAO operace (beze změny) ───────────────────────────────────────────────

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

    // ── Supabase vyhledávání s lokalizací ─────────────────────────────────────

    override suspend fun searchFoodByName(query: String): List<RecognizedFood> {
        if (query.length < 2) return emptyList()

        return try {
            val (lc, _) = resolvedLocale()
            supabaseFoodApi
                .searchProducts(query = query, limit = 20)
                .map { result ->
                    // Lokalizace probíhá v mapper funkci — výběr správného sloupce
                    result.toRecognizedFood(
                        languageCode = lc,
                        originalQuery = query
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFoodByBarcode(barcode: String): RecognizedFood? {
        return try {
            val (lc, _) = resolvedLocale()
            supabaseFoodApi
                .getProductByBarcode(barcodeFilter = "eq.$barcode")
                .firstOrNull()
                ?.toRecognizedFood(languageCode = lc)
        } catch (e: Exception) {
            null
        }
    }

    // ── Rozpoznávání z fotky (beze změny — interně volá searchFoodByName) ─────

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

    // ── Mapper extension funkce (beze změny) ──────────────────────────────────

    private fun FoodLogEntity.toDomain(json: Json): FoodLog = FoodLog(
        id = Uuid.parse(this.id),
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
