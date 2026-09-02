package com.allergytrack.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.allergytrack.shared.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.shared.data.local.database.dao.FoodLogDao
import com.allergytrack.shared.data.local.database.dao.SymptomLogDao
import com.allergytrack.shared.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.shared.data.local.database.entity.FoodLogEntity
import com.allergytrack.shared.data.local.database.entity.SymptomLogEntity

@Database(
    entities = [
        FoodLogEntity::class,
        SymptomLogEntity::class,
        EnvironmentLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@ConstructedBy(AllergyDatabaseConstructor::class)
abstract class AllergyDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun symptomLogDao(): SymptomLogDao
    abstract fun environmentLogDao(): EnvironmentLogDao

    companion object {
        const val DATABASE_NAME = "allergy_track.db"

        /**
         * Migrace 1→2: přidává no2, ozone, region a pollenAvailable
         * do environment_logs.
         *
         * SQL je doslova stejné jako v Android-only verzi — mění se jen typ
         * parametru z SupportSQLiteDatabase na SQLiteConnection (Room KMP API).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE environment_logs ADD COLUMN no2 REAL NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE environment_logs ADD COLUMN ozone REAL NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE environment_logs ADD COLUMN region TEXT NOT NULL DEFAULT 'UNKNOWN'")
                connection.execSQL("ALTER TABLE environment_logs ADD COLUMN pollenAvailable INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

/**
 * Tělo generuje KSP. "expect" bez "actual" je zde záměr —
 * Room plugin doplní actual pro každý target.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AllergyDatabaseConstructor : RoomDatabaseConstructor<AllergyDatabase> {
    override fun initialize(): AllergyDatabase
}
