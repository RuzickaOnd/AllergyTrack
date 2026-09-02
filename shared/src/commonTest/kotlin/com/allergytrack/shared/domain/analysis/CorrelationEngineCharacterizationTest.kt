package com.allergytrack.shared.domain.analysis

import com.allergytrack.shared.data.local.database.entity.FoodLogEntity
import com.allergytrack.shared.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.domain.model.AnalysisReadiness
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.shared.domain.model.TriggerType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Charakterizační testy — zapisují SOUČASNÉ chování CorrelationEngine,
 * aby se odhalila tichá změna při migraci na KMP (fáze 6).
 *
 * Tyto testy se ve fázi 6 přesunou do shared/src/commonTest/ a musí tam
 * dát identické výsledky. Když test selže, je chybná ASERCE, ne produkční kód.
 */
class CorrelationEngineCharacterizationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val engine = CorrelationEngine(json)

    /** Pevný základ času, aby testy nebyly závislé na System.currentTimeMillis(). */
    private val base = 1_700_000_000_000L
    private val hour = 60L * 60 * 1000

    private fun food(name: String, allergens: List<String>, atHour: Long) =
        FoodLogEntity(
            id = "food-$name-$atHour",
            timestamp = base + atHour * hour,
            foodName = name,
            ingredients = "[]",
            allergens = json.encodeToString(allergens)
        )

    private fun symptom(type: SymptomType, intensity: Int, atHour: Long) =
        SymptomLogEntity(
            id = "sym-${type.name}-$atHour",
            timestamp = base + atHour * hour,
            type = type.name,
            intensity = intensity,
            bodyParts = "[]"
        )

    @Test
    fun `pod prahem MIN_SYMPTOMS vrati prazdny seznam`() {
        val symptoms = (1..4).map { symptom(SymptomType.ECZEMA, 3, it * 10L) }

        val result = engine.analyze(
            foodLogs = listOf(food("Jogurt", listOf("Milk"), 0)),
            symptomLogs = symptoms,
            environmentLogs = emptyList()
        )

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `silna vazba mleko na ekzem se objevi jako FOOD korelace`() {
        val foods = (0 until 6).map { food("Jogurt", listOf("Milk"), it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ECZEMA, 4, it * 48L + 4) }

        val result = engine.analyze(
            foodLogs = foods,
            symptomLogs = symptoms,
            environmentLogs = emptyList()
        )

        val milk = result.firstOrNull { it.trigger == "Milk" }
        assertTrue(milk != null, "Očekávána korelace pro Milk, dostal jsem: $result")
        assertEquals(TriggerType.FOOD, milk!!.triggerType)
        assertEquals(SymptomType.ECZEMA, milk.symptomType)
        assertTrue(milk.score > 0.3f, "Skóre má být > 0.3, je ${milk.score}")
    }

    /**
     * POZOR na sémantiku: knownAllergies se testuje na název enumu AllergyType
     * (tedy "FOOD"), NE na konkrétní klíč alergenu. Viz CorrelationEngine:89:
     *     isExpected = "FOOD" in knownAllergies || symptomType.name in knownSymptoms
     * Příznak tedy dostanou VŠECHNY potravinové korelace, jakmile uživatel
     * v onboardingu uvedl potravinovou alergii — ne jen ta odpovídající.
     */
    @Test
    fun `typ alergie FOOD z profilu oznaci potravinove korelace jako ocekavane`() {
        val foods = (0 until 6).map { food("Jogurt", listOf("Milk"), it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ECZEMA, 4, it * 48L + 4) }

        val result = engine.analyze(
            foodLogs = foods,
            symptomLogs = symptoms,
            environmentLogs = emptyList(),
            knownAllergies = setOf("FOOD")
        )

        val milk = result.first { it.trigger == "Milk" }
        assertTrue(milk.isExpected, "isExpected mělo být true pro knownAllergies=[FOOD]")
    }

    @Test
    fun `konkretni klic alergenu v profilu isExpected NEnastavi`() {
        val foods = (0 until 6).map { food("Jogurt", listOf("Milk"), it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ECZEMA, 4, it * 48L + 4) }

        val result = engine.analyze(
            foodLogs = foods,
            symptomLogs = symptoms,
            environmentLogs = emptyList(),
            knownAllergies = setOf("Milk")
        )

        val milk = result.first { it.trigger == "Milk" }
        assertTrue(
            !milk.isExpected,
            "isExpected má být false — engine porovnává s \"FOOD\", ne s klíčem alergenu"
        )
    }

    @Test
    fun `znamy typ symptomu z profilu take nastavi isExpected`() {
        val foods = (0 until 6).map { food("Jogurt", listOf("Milk"), it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ECZEMA, 4, it * 48L + 4) }

        val result = engine.analyze(
            foodLogs = foods,
            symptomLogs = symptoms,
            environmentLogs = emptyList(),
            knownSymptoms = setOf("ECZEMA")
        )

        val milk = result.first { it.trigger == "Milk" }
        assertTrue(milk.isExpected, "isExpected mělo být true pro knownSymptoms=[ECZEMA]")
    }

    @Test
    fun `vysledky jsou serazene sestupne se seznamem max 10 polozek`() {
        val foods = (0 until 8).flatMap { i ->
            listOf(
                food("Jogurt", listOf("Milk"), i * 48L),
                food("Chleba", listOf("Gluten"), i * 48L + 1)
            )
        }
        val symptoms = (0 until 8).map { symptom(SymptomType.ECZEMA, 4, it * 48L + 4) }

        val result = engine.analyze(foods, symptoms, emptyList())

        assertTrue(result.size <= 10, "Nejvýše 10 výsledků, dostal jsem ${result.size}")
        val scores = result.map { it.score }
        assertEquals(
            scores.sortedDescending(), scores,
            "Výsledky musí být seřazené sestupně"
        )
    }

    @Test
    fun `jidlo bez naslednych symptomu nevygeneruje korelaci`() {
        val foods = (0 until 6).map { food("Rýže", listOf("Gluten"), 1000L + it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ASTHMA, 3, it * 48L) }

        val result = engine.analyze(foods, symptoms, emptyList())

        assertTrue(
            result.none { it.trigger == "Gluten" },
            "Očekáván prázdný nebo bezglutenový výsledek, dostal jsem: $result"
        )
    }

    @Test
    fun `evaluateReadiness pod prahem vrati Locked`() {
        val readiness = engine.evaluateReadiness(
            symptomCount = 2,
            environmentLogCount = 0
        )
        assertTrue(
            readiness is AnalysisReadiness.Locked,
            "Pod 5 symptomy má být Locked, je $readiness"
        )
        assertTrue(!engine.canAnalyze(readiness), "canAnalyze mělo být false")
    }

    @Test
    fun `evaluateReadiness na 5 az 9 symptomech vrati Partial`() {
        val readiness = engine.evaluateReadiness(symptomCount = 6, environmentLogCount = 0)
        assertTrue(
            readiness is AnalysisReadiness.Partial,
            "Při 6 symptomech má být Partial, je $readiness"
        )
    }

    @Test
    fun `evaluateReadiness od 10 symptomu vrati Full`() {
        val readiness = engine.evaluateReadiness(symptomCount = 12, environmentLogCount = 7)
        assertTrue(
            readiness is AnalysisReadiness.Full,
            "Při 12 symptomech má být Full, je $readiness"
        )
    }
}
