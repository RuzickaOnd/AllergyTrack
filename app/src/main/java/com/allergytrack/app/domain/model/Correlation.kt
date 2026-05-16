package com.allergytrack.app.domain.model

data class Correlation(
    val trigger: String,
    val triggerType: TriggerType,
    val symptomType: SymptomType,
    val score: Float, // 0.0 - 1.0
    val occurrences: Int,
    val totalEvents: Int,
    val avgDelayHours: Float,
    val severity: CorrelationSeverity,
    val isExpected: Boolean = false
)

enum class TriggerType { FOOD, ENVIRONMENT, COMBINED }

enum class CorrelationSeverity { HIGH, MEDIUM, LOW }

/**
 * Výsledek korelační analýzy pro jedno jídlo nebo alergen.
 *
 * @param triggerName    Zobrazovaný název spouštěče (např. "Mléčné výrobky", "Lepek")
 * @param allergenKey    Klíč alergenu z EU_14 (např. "Milk", "Gluten") nebo název jídla pokud
 *                       není alergen znám ("__food__Jogurt")
 * @param correlationScore   0.0–1.0 — síla korelace (zobrazuje se jako %)
 * @param severity       HIGH / MEDIUM / LOW — viz CorrelationSeverity
 * @param reactionCount  Počet konzumací kde následoval symptom do 72h
 * @param totalCount     Celkový počet zaznamenaných konzumací
 * @param dominantSymptoms  Nejčastější typy symptomů po konzumaci (max 3)
 * @param isExpected     True pokud odpovídá alergickému profilu uživatele z onboardingu
 * @param reactionEvents Seřazený seznam událostí pro detail obrazovku
 */
data class FoodTrigger(
    val triggerName: String,
    val allergenKey: String,
    val correlationScore: Float,           // 0.0–1.0
    val severity: CorrelationSeverity,
    val reactionCount: Int,
    val totalCount: Int,
    val dominantSymptoms: List<String>,    // max 3 typy symptomů
    val isExpected: Boolean = false,
    val reactionEvents: List<FoodReactionEvent> = emptyList()
)

/**
 * Jedna konzumace jídla a co se stalo v 72h okně po ní.
 *
 * @param foodLogId       ID záznamu jídla pro případné propojení
 * @param foodName        Název jídla jak byl zaznamenán
 * @param consumedAt      Unix timestamp konzumace
 * @param reactions       Symptomy zaznamenané do 72h po konzumaci (seřazené časově)
 * @param hadReaction     True pokud reactions není prázdný
 * @param maxIntensity    Maximální intenzita reakce (1–5), null pokud žádná
 */
data class FoodReactionEvent(
    val foodLogId: String,
    val foodName: String,
    val consumedAt: Long,
    val reactions: List<SymptomInWindow>,
    val hadReaction: Boolean,
    val maxIntensity: Int?
)

/**
 * Symptom zaznamenaný v časovém okně po konzumaci jídla.
 *
 * @param symptomName   Zobrazovaný název symptomu
 * @param recordedAt    Unix timestamp záznamu symptomu
 * @param delayHours    Odstup od konzumace v hodinách (zaokrouhleno na 1 des. místo)
 * @param intensity     Intenzita 1–5
 */
data class SymptomInWindow(
    val symptomName: String,
    val recordedAt: Long,
    val delayHours: Float,
    val intensity: Int
)
