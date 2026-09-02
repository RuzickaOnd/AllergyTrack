package com.allergytrack.shared.data.local.database

import androidx.room.RoomDatabase

expect fun databaseBuilder(): RoomDatabase.Builder<AllergyDatabase>

/**
 * Sestaví databázi včetně migrací.
 *
 * Existuje proto, aby Room zůstal celý uvnitř :shared — platformní aplikace
 * s typy RoomDatabase.Builder ani Migration vůbec nepřijde do styku.
 * Stejný princip jako u createAirQualityApi() pro Ktor.
 */
fun createAllergyDatabase(): AllergyDatabase =
    databaseBuilder()
        .addMigrations(AllergyDatabase.MIGRATION_1_2)
        .build()
