package com.allergytrack.app.domain.analysis

import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.domain.model.AnalysisReadiness
import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.CorrelationSeverity
import com.allergytrack.shared.domain.model.FoodReactionEvent
import com.allergytrack.shared.domain.model.FoodTrigger
import com.allergytrack.shared.domain.model.SymptomInWindow
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.shared.domain.model.TriggerType
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class CorrelationEngine constructor(
    private val json: Json
) {
    // ─── Konstanty pro potravinovou korelaci ──────────────────────────────────────

    companion object {
        // Maximální prodleva mezi konzumací a symptomem (potravinové alergie: 2 min–72h)
        const val FOOD_REACTION_WINDOW_MS = 72L * 60 * 60 * 1000

        // Minimální počet konzumací pro statisticky relevantní výsledek
        const val MIN_CONSUMPTION_COUNT = 2

        // Práh pro zobrazení v TOP TRIGGERS (nízké skóre = nezobrazovat)
        const val MIN_DISPLAY_SCORE = 0.15f
    }

    // Časová okna pro hledání korelace (ms)
    private val TIME_WINDOWS = listOf(
        2L, 4L, 8L, 12L, 24L, 48L, 72L
    ).map { TimeUnit.HOURS.toMillis(it) }

    fun analyze(
        foodLogs: List<FoodLogEntity>,
        symptomLogs: List<SymptomLogEntity>,
        environmentLogs: List<EnvironmentLogEntity>,
        knownAllergies: Set<String> = emptySet(),
        knownSymptoms: Set<String> = emptySet()
    ): List<Correlation> {
        if (symptomLogs.size < AnalysisReadiness.MIN_SYMPTOMS) return emptyList() // Min. dat pro analýzu

        val results = mutableListOf<Correlation>()

        // 1. Korelace: Jídlo → Symptom
        results.addAll(analyzeFoodCorrelations(foodLogs, symptomLogs, knownAllergies, knownSymptoms))

        // 2. Korelace: Prostředí → Symptom
        results.addAll(analyzeEnvironmentCorrelations(environmentLogs, symptomLogs, knownAllergies, knownSymptoms))

        // 3. Seřaď podle skóre (se započítáním isExpected prioritizace)
        return results
            .filter { it.score > 0.3f }  // Minimální práh relevance
            .sortedWith(compareByDescending<Correlation> { it.isExpected }.thenByDescending { it.score })
            .take(10)
    }

    private fun analyzeFoodCorrelations(
        foodLogs: List<FoodLogEntity>,
        symptomLogs: List<SymptomLogEntity>,
        knownAllergies: Set<String>,
        knownSymptoms: Set<String>
    ): List<Correlation> {
        // Extrahuj unikátní alergeny ze všech záznamů jídla
        val allergens = foodLogs
            .flatMap { log ->
                if (log.allergens.isNotEmpty()) {
                    try { json.decodeFromString<List<String>>(log.allergens) }
                    catch (e: Exception) { emptyList() }
                } else emptyList()
            }
            .groupBy { it }
            .filter { it.value.size >= 2 } // Musí se objevit alespoň 2x
            .keys

        return allergens.flatMap { allergen ->
            SymptomType.entries.mapNotNull { symptomType ->
                val correlation = calculateFoodSymptomCorrelation(
                    allergen = allergen,
                    symptomType = symptomType,
                    foodLogs = foodLogs,
                    symptomLogs = symptomLogs,
                    isExpected = "FOOD" in knownAllergies || symptomType.name in knownSymptoms
                )
                correlation
            }
        }
    }

    private fun calculateFoodSymptomCorrelation(
        allergen: String,
        symptomType: SymptomType,
        foodLogs: List<FoodLogEntity>,
        symptomLogs: List<SymptomLogEntity>,
        isExpected: Boolean
    ): Correlation? {
        val symptomLogsOfType = symptomLogs.filter { it.type == symptomType.name }
        if (symptomLogsOfType.isEmpty()) return null

        // Pro každý výskyt alergenu: kolik symptomů následovalo v časovém okně?
        val foodEventsWithAllergen = foodLogs.filter { log ->
            try {
                val allergens = json.decodeFromString<List<String>>(log.allergens)
                allergen in allergens
            } catch (e: Exception) { false }
        }

        if (foodEventsWithAllergen.isEmpty()) return null

        var matchCount = 0
        var totalDelayMs = 0L

        foodEventsWithAllergen.forEach { foodLog ->
            val followingSymptoms = symptomLogsOfType.filter { symptom ->
                val delay = symptom.timestamp - foodLog.timestamp
                delay in (0L..TIME_WINDOWS.last())
            }
            if (followingSymptoms.isNotEmpty()) {
                matchCount++
                val minDelay = followingSymptoms.minOf { it.timestamp - foodLog.timestamp }
                totalDelayMs += minDelay
            }
        }

        if (matchCount < 2) return null

        var score = matchCount.toFloat() / foodEventsWithAllergen.size.toFloat()
        val avgDelayHours = if (matchCount > 0)
            TimeUnit.MILLISECONDS.toHours(totalDelayMs / matchCount).toFloat()
        else 0f

        // Lehký boost pro očekávané korelace, aby se dostaly výše
        if (isExpected) score = (score * 1.1f).coerceAtMost(1.0f)

        return Correlation(
            trigger = allergen,
            triggerType = TriggerType.FOOD,
            symptomType = symptomType,
            score = score.coerceIn(0f, 1f),
            occurrences = matchCount,
            totalEvents = foodEventsWithAllergen.size,
            avgDelayHours = avgDelayHours,
            severity = when {
                score >= 0.7f -> CorrelationSeverity.HIGH
                score >= 0.4f -> CorrelationSeverity.MEDIUM
                else -> CorrelationSeverity.LOW
            },
            isExpected = isExpected
        )
    }

    private fun analyzeEnvironmentCorrelations(
        environmentLogs: List<EnvironmentLogEntity>,
        symptomLogs: List<SymptomLogEntity>,
        knownAllergies: Set<String>,
        knownSymptoms: Set<String>
    ): List<Correlation> {
        val correlations = mutableListOf<Correlation>()
        val symptoms = symptomLogs.map { it.intensity.toFloat() }

        // AQI korelace — globální, vždy dostupné
        val aqiValues = environmentLogs.map { it.aqi.toFloat() }
        if (aqiValues.size >= AnalysisReadiness.MIN_ENV_LOGS && symptoms.size >= AnalysisReadiness.MIN_SYMPTOMS) {
            val pearson = pearsonCorrelation(
                aqiValues.take(symptoms.size),
                symptoms.take(aqiValues.size)
            )
            if (pearson > 0.25f) {
                correlations.add(buildAqiCorrelation(pearson, environmentLogs, knownSymptoms))
            }
        }

        // Pyl korelace — pouze pokud máme alespoň 5 záznamů s dostupným pylem
        val pollenLogs = environmentLogs.filter { it.pollenAvailable }
        if (pollenLogs.size >= AnalysisReadiness.MIN_ENV_LOGS && symptoms.size >= AnalysisReadiness.MIN_SYMPTOMS) {
            val grassValues = pollenLogs.map { it.pollenGrassIndex?.toFloat() ?: 0f }
            val pearson = pearsonCorrelation(
                grassValues.take(symptoms.size),
                symptoms.take(grassValues.size)
            )
            if (pearson > 0.3f) {
                correlations.add(buildPollenCorrelation(pearson, pollenLogs, knownAllergies, knownSymptoms))
            }
        }

        return correlations
    }

    private fun buildAqiCorrelation(
        pearson: Float,
        logs: List<EnvironmentLogEntity>,
        knownSymptoms: Set<String>
    ) = Correlation(
        trigger = "Kvalita vzduchu (AQI)",
        triggerType = TriggerType.ENVIRONMENT,
        symptomType = SymptomType.ASTHMA,
        score = pearson.coerceIn(0f, 1f),
        occurrences = logs.count { it.aqi >= 50 },
        totalEvents = logs.size,
        avgDelayHours = 1f,
        severity = if (pearson >= 0.7f) CorrelationSeverity.HIGH else CorrelationSeverity.MEDIUM,
        isExpected = SymptomType.ASTHMA.name in knownSymptoms
    )

    private fun buildPollenCorrelation(
        pearson: Float,
        pollenLogs: List<EnvironmentLogEntity>,
        knownAllergies: Set<String>,
        knownSymptoms: Set<String>
    ) = Correlation(
        trigger = "Pyl trávy",
        triggerType = TriggerType.ENVIRONMENT,
        symptomType = SymptomType.RHINITIS,
        score = pearson.coerceIn(0f, 1f),
        occurrences = pollenLogs.count { (it.pollenGrassIndex ?: 0) >= 3 },
        totalEvents = pollenLogs.size,
        avgDelayHours = 2f,
        severity = when {
            pearson >= 0.7f -> CorrelationSeverity.HIGH
            pearson >= 0.4f -> CorrelationSeverity.MEDIUM
            else            -> CorrelationSeverity.LOW
        },
        isExpected = "POLLEN" in knownAllergies || SymptomType.RHINITIS.name in knownSymptoms
    )

    // Pearsonova korelace dvou časových řad stejné délky
    private fun pearsonCorrelation(x: List<Float>, y: List<Float>): Float {
        if (x.size != y.size || x.size < 2) return 0f
        val meanX = x.average().toFloat()
        val meanY = y.average().toFloat()
        val numerator = x.zip(y).sumOf { (xi, yi) ->
            ((xi - meanX) * (yi - meanY)).toDouble()
        }
        val denomX = sqrt(x.sumOf { ((it - meanX) * (it - meanX)).toDouble() })
        val denomY = sqrt(y.sumOf { ((it - meanY) * (it - meanY)).toDouble() })
        val denom = denomX * denomY
        return if (denom == 0.0) 0f else (numerator / denom).toFloat().coerceIn(-1f, 1f)
    }

    fun evaluateReadiness(
        symptomCount: Int,
        environmentLogCount: Int
    ): AnalysisReadiness = AnalysisReadiness.evaluate(
        symptomCount = symptomCount,
        environmentLogCount = environmentLogCount
    )

    fun evaluateReadiness(
        symptomLogs: List<SymptomLogEntity>,
        environmentLogs: List<EnvironmentLogEntity>
    ): AnalysisReadiness = evaluateReadiness(
        symptomCount = symptomLogs.size,
        environmentLogCount = environmentLogs.size
    )

    // ─── Hlavní metoda pro potravinové korelace ───────────────────────────────────

    /**
     * Analyzuje korelaci mezi zaznamenaným jídlem/alergeny a symptomy.
     *
     * Algoritmus:
     * 1. Pro každé jedinečné jídlo/alergen najde všechny záznamy konzumace
     * 2. Ke každé konzumaci přiřadí symptomy zaznamenané do 72h po ní
     * 3. Spočítá skóre: reactionCount / totalCount s váhou intenzity
     * 4. Seřadí sestupně podle skóre a odfiltruje pod MIN_DISPLAY_SCORE
     * 5. Označí shody s alergickým profilem uživatele (isExpected)
     *
     * @param foodLogs         Všechny záznamy jídla
     * @param symptomLogs      Všechny záznamy symptomů
     * @param knownAllergens   Alergeny z onboarding profilu (pro isExpected)
     */
    fun analyzeFoodTriggers(
        foodLogs: List<FoodLogEntity>,
        symptomLogs: List<SymptomLogEntity>,
        knownAllergens: Set<String> = emptySet()
    ): List<FoodTrigger> {

        if (foodLogs.isEmpty() || symptomLogs.isEmpty()) return emptyList()

        // Krok 1: Seskup konzumace podle alergenu (nebo jídla pokud alergen neznámý)
        val groupedByTrigger = mutableMapOf<String, MutableList<FoodLogEntity>>()

        foodLogs.forEach { log ->
            val allergens = try {
                if (log.allergens.isNotEmpty()) {
                    json.decodeFromString<List<String>>(log.allergens)
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            if (allergens.isNotEmpty()) {
                // Skupina per alergen
                allergens.forEach { allergen ->
                    groupedByTrigger.getOrPut(allergen) { mutableListOf() }.add(log)
                }
            } else {
                // Jídlo bez alergenu — skupinuj pod názvem jídla
                val key = "__food__${log.foodName}"
                groupedByTrigger.getOrPut(key) { mutableListOf() }.add(log)
            }
        }

        // Krok 2: Pro každý trigger spočítej korelaci
        val triggers = mutableListOf<FoodTrigger>()

        groupedByTrigger.forEach { (triggerKey, logs) ->
            if (logs.size < MIN_CONSUMPTION_COUNT) return@forEach

            val events = logs.map { foodLog ->
                val reactionsInWindow = symptomLogs.filter { symptom ->
                    val delay = symptom.timestamp - foodLog.timestamp
                    delay in 0..FOOD_REACTION_WINDOW_MS
                }.map { symptom ->
                    SymptomInWindow(
                        symptomName = symptom.type,
                        recordedAt = symptom.timestamp,
                        delayHours = (symptom.timestamp - foodLog.timestamp)
                            .toFloat() / (60 * 60 * 1000),
                        intensity = symptom.intensity
                    )
                }.sortedBy { it.recordedAt }

                FoodReactionEvent(
                    foodLogId = foodLog.id,
                    foodName = foodLog.foodName,
                    consumedAt = foodLog.timestamp,
                    reactions = reactionsInWindow,
                    hadReaction = reactionsInWindow.isNotEmpty(),
                    maxIntensity = reactionsInWindow.maxOfOrNull { it.intensity }
                )
            }

            val reactionCount = events.count { it.hadReaction }
            val totalCount = events.size

            // Skóre = základní frekvence + bonus za intenzitu
            val baseScore = reactionCount.toFloat() / totalCount
            val intensityBonus = events
                .filter { it.hadReaction }
                .mapNotNull { it.maxIntensity }
                .average()
                .let { if (it.isNaN()) 0.0 else (it - 1) / 4.0 * 0.15 }  // max +15%
            val finalScore = (baseScore + intensityBonus).toFloat().coerceIn(0f, 1f)

            if (finalScore < MIN_DISPLAY_SCORE) return@forEach

            // Dominantní symptomy (top 3 nejčastější)
            val dominantSymptoms = events
                .flatMap { it.reactions }
                .groupingBy { it.symptomName }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            val isAllergen = !triggerKey.startsWith("__food__")
            val displayName = if (isAllergen) triggerKey else triggerKey.removePrefix("__food__")

            // Shoda s profilem
            val isExpected = isAllergen && triggerKey in knownAllergens

            triggers.add(
                FoodTrigger(
                    triggerName = displayName,
                    allergenKey = triggerKey,
                    correlationScore = finalScore,
                    severity = when {
                        finalScore >= 0.70f -> CorrelationSeverity.HIGH
                        finalScore >= 0.40f -> CorrelationSeverity.MEDIUM
                        else -> CorrelationSeverity.LOW
                    },
                    reactionCount = reactionCount,
                    totalCount = totalCount,
                    dominantSymptoms = dominantSymptoms,
                    isExpected = isExpected,
                    reactionEvents = events.sortedByDescending { it.consumedAt }
                )
            )
        }

        return triggers.sortedByDescending { it.correlationScore }
    }

    fun canAnalyze(readiness: AnalysisReadiness): Boolean =
        readiness !is AnalysisReadiness.Locked
}
