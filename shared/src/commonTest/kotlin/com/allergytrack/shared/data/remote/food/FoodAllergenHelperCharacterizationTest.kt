package com.allergytrack.shared.data.remote.food

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Charakterizační testy heuristiky návrhu alergenů.
 * Přesunuty do commonTest ve fázi 7 spolu se stringy.
 */
class FoodAllergenHelperCharacterizationTest {

    /** Dvojice (název jídla → alergen, který MUSÍ být navržen). */
    private val expectedMappings = listOf(
        "Jogurt" to "Milk",
        "Chléb kmínový" to "Gluten",
        "Arašídové máslo" to "Peanuts",
        "Sójový nápoj" to "Soybeans",
        "Vejce natvrdo" to "Eggs"
    )

    @Test
    fun `klicova slova navrhnou ocekavany alergen`() {
        expectedMappings.forEach { (name, allergen) ->
            val result = FoodAllergenHelper.suggestFromName(name)
            assertTrue(
                allergen in result,
                "Pro '$name' byl očekáván alergen '$allergen', dostal jsem: $result"
            )
        }
    }

    /**
     * Heuristika hledá podřetězec, takže jeden název může trefit víc alergenů.
     * "Arašídové máslo" obsahuje jak "arašíd" (Peanuts), tak "máslo" (Milk).
     * Zapsáno záměrně — je to současné chování.
     */
    @Test
    fun `nazev muze trefit vice alergenu najednou`() {
        val result = FoodAllergenHelper.suggestFromName("Arašídové máslo")
        assertTrue("Peanuts" in result, "Očekáván Peanuts v $result")
        assertTrue("Milk" in result, "Očekáván i Milk (kvůli 'máslo') v $result")
    }

    @Test
    fun `heuristika je case insensitive`() {
        val lower = FoodAllergenHelper.suggestFromName("jogurt")
        val upper = FoodAllergenHelper.suggestFromName("JOGURT")
        val mixed = FoodAllergenHelper.suggestFromName("JoGuRt")
        assertEquals(lower, upper)
        assertEquals(lower, mixed)
    }

    @Test
    fun `neznamy nazev nevrati zadny alergen`() {
        assertEquals(emptySet<String>(), FoodAllergenHelper.suggestFromName("xyzqwerty"))
    }

    @Test
    fun `prazdny nazev nevrati zadny alergen`() {
        assertEquals(emptySet<String>(), FoodAllergenHelper.suggestFromName(""))
    }

    @Test
    fun `EU_14 obsahuje presne 14 alergenu s unikatnimi klici`() {
        assertEquals(14, FoodAllergenHelper.EU_14.size)
        val keys = FoodAllergenHelper.EU_14.map { it.key }
        assertEquals(keys.size, keys.toSet().size, "Klíče musí být unikátní")
    }

    @Test
    fun `EU_14 obsahuje vsech 14 povinnych klicu`() {
        val expected = setOf(
            "Gluten", "Milk", "Eggs", "Fish", "Crustaceans", "Peanuts",
            "Soybeans", "Nuts", "Celery", "Mustard", "Sesame-seeds",
            "Sulphur-dioxide-and-sulphites", "Lupin", "Molluscs"
        )
        assertEquals(expected, FoodAllergenHelper.EU_14.map { it.key }.toSet())
    }
}
