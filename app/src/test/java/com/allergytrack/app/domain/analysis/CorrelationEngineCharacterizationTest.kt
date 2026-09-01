package com.allergytrack.app.domain.analysis

import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.AnalysisReadiness
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.domain.model.TriggerType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue("Očekávána korelace pro Milk, dostal jsem: $result", milk != null)
        assertEquals(TriggerType.FOOD, milk!!.triggerType)
        assertEquals(SymptomType.ECZEMA, milk.symptomType)
        assertTrue("Skóre má být > 0.3, je ${milk.score}", milk.score > 0.3f)
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
        assertTrue("isExpected mělo být true pro knownAllergies=[FOOD]", milk.isExpected)
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
            "isExpected má být false — engine porovnává s \"FOOD\", ne s klíčem alergenu",
            !milk.isExpected
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
        assertTrue("isExpected mělo být true pro knownSymptoms=[ECZEMA]", milk.isExpected)
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

        assertTrue("Nejvýše 10 výsledků, dostal jsem ${result.size}", result.size <= 10)
        val scores = result.map { it.score }
        assertEquals(
            "Výsledky musí být seřazené sestupně",
            scores.sortedDescending(), scores
        )
    }

    @Test
    fun `jidlo bez naslednych symptomu nevygeneruje korelaci`() {
        val foods = (0 until 6).map { food("Rýže", listOf("Gluten"), 1000L + it * 48L) }
        val symptoms = (0 until 6).map { symptom(SymptomType.ASTHMA, 3, it * 48L) }

        val result = engine.analyze(foods, symptoms, emptyList())

        assertTrue(
            "Očekáván prázdný nebo bezglutenový výsledek, dostal jsem: $result",
            result.none { it.trigger == "Gluten" }
        )
    }

    @Test
    fun `evaluateReadiness pod prahem vrati Locked`() {
        val readiness = engine.evaluateReadiness(
            symptomCount = 2,
            environmentLogCount = 0
        )
        assertTrue(
            "Pod 5 symptomy má být Locked, je $readiness",
            readiness is AnalysisReadiness.Locked
        )
        assertTrue("canAnalyze mělo být false", !engine.canAnalyze(readiness))
    }

    @Test
    fun `evaluateReadiness na 5 az 9 symptomech vrati Partial`() {
        val readiness = engine.evaluateReadiness(symptomCount = 6, environmentLogCount = 0)
        assertTrue(
            "Při 6 symptomech má být Partial, je $readiness",
            readiness is AnalysisReadiness.Partial
        )
    }

    @Test
    fun `evaluateReadiness od 10 symptomu vrati Full`() {
        val readiness = engine.evaluateReadiness(symptomCount = 12, environmentLogCount = 7)
        assertTrue(
            "Při 12 symptomech má být Full, je $readiness",
            readiness is AnalysisReadiness.Full
        )
    }
}
