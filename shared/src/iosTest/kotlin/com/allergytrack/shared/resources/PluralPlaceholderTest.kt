package com.allergytrack.shared.resources

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getPluralString
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Hlídá, že plurály používají poziční placeholdery.
 *
 * Compose Resources substituuje jen tvar %N$d / %N$s. Android styl %d
 * projde překladem i buildem a vypíše se doslova — "%d výskytů" místo
 * "7 výskytů". Migrace stringů to zvládla, plurals.xml se přehlédl.
 *
 * Test schválně netvrdí nic o samotném slově: locale je při běhu testu
 * neznámé a zajímá nás jen dosazení čísla.
 */
class PluralPlaceholderTest {

    @Test
    fun pluralDosadiPocet() = runTest {
        val text = getPluralString(Res.plurals.symptom_occurrences, 7, 7)
        assertTrue("7" in text, "Očekáváno dosazené číslo, bylo: $text")
        assertFalse("%" in text, "V textu zůstal nedosazený placeholder: $text")
    }

    @Test
    fun pluralDosadiPocetIProJednotneCislo() = runTest {
        val text = getPluralString(Res.plurals.symptom_occurrences, 1, 1)
        assertTrue("1" in text, "Očekáváno dosazené číslo, bylo: $text")
        assertFalse("%" in text, "V textu zůstal nedosazený placeholder: $text")
    }
}
