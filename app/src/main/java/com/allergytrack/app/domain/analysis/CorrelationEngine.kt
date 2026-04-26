package com.allergytrack.app.domain.analysis

import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.AnalysisReadiness
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.domain.model.TriggerType
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt

class CorrelationEngine @Inject constructor(
    private val json: Json
) {
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

    fun canAnalyze(readiness: AnalysisReadiness): Boolean =
        readiness !is AnalysisReadiness.Locked
}
