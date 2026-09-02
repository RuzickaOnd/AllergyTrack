package com.allergytrack.shared.platform

import com.allergytrack.shared.data.local.database.AllergyDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Hlídá, že se cesta k databázi nestaví přes dataStorePath().
 *
 * Ta bezpodmínečně připojuje příponu .preferences_pb, takže SQLite soubor
 * skončil na disku jako allergy_track.db.preferences_pb. Na iOS to zatím
 * nikdo nevidí (targety jsou canary), o to snáz by se to vrátilo zpátky.
 */
class IosPathsTest {

    @Test
    fun cestaKDatabaziNemaPriponuDataStore() {
        val path = databasePath(AllergyDatabase.DATABASE_NAME)
        assertFalse(
            path.endsWith(".preferences_pb"),
            "Databáze nesmí mít příponu DataStore, byla: $path"
        )
        assertTrue(
            path.endsWith("/${AllergyDatabase.DATABASE_NAME}"),
            "Očekáván soubor ${AllergyDatabase.DATABASE_NAME}, byla: $path"
        )
    }

    @Test
    fun cestaKDataStorePriponuMa() {
        assertTrue(dataStorePath("user_preferences").endsWith("/user_preferences.preferences_pb"))
    }

    @Test
    fun obeCestyLeziVeStejnemAdresari() {
        val dbDir = databasePath("a").substringBeforeLast('/')
        val dsDir = dataStorePath("a").substringBeforeLast('/')
        assertEquals(dsDir, dbDir)
    }

    @Test
    fun cestaJeAbsolutni() {
        assertTrue(databasePath("a").startsWith("/"), "Očekávána absolutní cesta")
    }
}
