package com.allergytrack.shared.data.remote.food

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Charakterizační testy lokalizačních mapperů.
 * Přesunuty do commonTest spolu s FoodDto (fáze 3).
 */
class FoodDtoMapperCharacterizationTest {

    private val plnyProdukt = SupabaseFoodProduct(
        code = "8595000000001",
        productName = "Strawberry Yogurt",
        productNameCs = "Jahodový jogurt",
        allergensTags = listOf("en:milk", "en:gluten"),
        ingredientsText = "milk, sugar; strawberries",
        ingredientsTextCs = "mléko, cukr; jahody",
        brands = "Madeta",
        nutriscoreGrade = "c",
        imageUrl = "https://example.test/img.jpg"
    )

    @Test
    fun `cestina preferuje product_name_cs`() {
        assertEquals("Jahodový jogurt", plnyProdukt.localizedName("cs", "fallback"))
    }

    @Test
    fun `jiny jazyk preferuje product_name`() {
        assertEquals("Strawberry Yogurt", plnyProdukt.localizedName("de", "fallback"))
        assertEquals("Strawberry Yogurt", plnyProdukt.localizedName("en", "fallback"))
    }

    @Test
    fun `chybejici cesky nazev spadne zpet na product_name`() {
        val bezCestiny = plnyProdukt.copy(productNameCs = null)
        assertEquals("Strawberry Yogurt", bezCestiny.localizedName("cs", "fallback"))
    }

    @Test
    fun `prazdny retezec se chova jako chybejici hodnota`() {
        val prazdnyCesky = plnyProdukt.copy(productNameCs = "   ")
        assertEquals("Strawberry Yogurt", prazdnyCesky.localizedName("cs", "fallback"))
    }

    @Test
    fun `bez jakehokoli nazvu se pouzije fallback`() {
        val bezNazvu = plnyProdukt.copy(productName = null, productNameCs = null)
        assertEquals("fallback", bezNazvu.localizedName("cs", "fallback"))
        assertEquals("fallback", bezNazvu.localizedName("en", "fallback"))
    }

    @Test
    fun `ingredience se deli carkou i strednikem a trimuji`() {
        assertEquals(
            listOf("mléko", "cukr", "jahody"),
            plnyProdukt.localizedIngredients("cs")
        )
        assertEquals(
            listOf("milk", "sugar", "strawberries"),
            plnyProdukt.localizedIngredients("en")
        )
    }

    @Test
    fun `chybejici ingredience vrati prazdny seznam`() {
        val bezIngredienci = plnyProdukt.copy(
            ingredientsText = null,
            ingredientsTextCs = null
        )
        assertEquals(emptyList<String>(), bezIngredienci.localizedIngredients("cs"))
    }

    @Test
    fun `toRecognizedFood ocisti prefix en a zvetsi prvni pismeno`() {
        val result = plnyProdukt.toRecognizedFood("cs")
        assertEquals(listOf("Milk", "Gluten"), result.allergens)
    }

    @Test
    fun `toRecognizedFood prenese barcode znacku a nutriscore`() {
        val result = plnyProdukt.toRecognizedFood("cs")
        assertEquals("8595000000001", result.barcode)
        assertEquals("Madeta", result.brand)
        assertEquals("c", result.nutriscoreGrade)
        assertEquals("https://example.test/img.jpg", result.imageUrl)
    }

    @Test
    fun `nameIsLocalized je true kdyz pozadovany jazyk existuje`() {
        assertTrue(plnyProdukt.toRecognizedFood("cs").nameIsLocalized)
    }

    @Test
    fun `nameIsLocalized je false kdyz preklad chybi`() {
        val bezCestiny = plnyProdukt.copy(productNameCs = null)
        assertFalse(bezCestiny.toRecognizedFood("cs").nameIsLocalized)
    }

    @Test
    fun `SearchResult lokalizuje nazev stejne jako plny produkt`() {
        val vysledek = SupabaseSearchResult(
            code = "8595000000002",
            productName = "Rye Bread",
            productNameCs = "Žitný chléb",
            allergensTags = listOf("en:gluten"),
            rank = 0.9f
        )
        assertEquals("Žitný chléb", vysledek.localizedName("cs", "fallback"))
        assertEquals("Rye Bread", vysledek.localizedName("en", "fallback"))
    }
}
