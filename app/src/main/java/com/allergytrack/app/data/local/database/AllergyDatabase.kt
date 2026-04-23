package com.allergytrack.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allergytrack.app.data.local.database.dao.*
import com.allergytrack.app.data.local.database.entity.*

@Database(
    entities = [
        FoodLogEntity::class,
        SymptomLogEntity::class,
        EnvironmentLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AllergyDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun symptomLogDao(): SymptomLogDao
    abstract fun environmentLogDao(): EnvironmentLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE environment_logs ADD COLUMN no2 REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE environment_logs ADD COLUMN ozone REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE environment_logs ADD COLUMN region TEXT NOT NULL DEFAULT 'UNKNOWN'")
                db.execSQL("ALTER TABLE environment_logs ADD COLUMN pollenAvailable INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
