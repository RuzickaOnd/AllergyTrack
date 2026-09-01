package com.allergytrack.shared.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.allergytrack.shared.platform.applicationContext
import kotlinx.coroutines.Dispatchers

/**
 * POZOR na volbu driveru: záměrně AndroidSQLiteDriver, NE BundledSQLiteDriver.
 *
 * AndroidSQLiteDriver používá frameworkový SQLite — tentýž, který aplikace
 * používala před migrací. Bundled driver by přinesl vlastní implementaci
 * a tím riziko u databází existujících uživatelů. Aplikace je živá na Play,
 * takže tohle riziko nechceme. Bundled driver se používá až na iOS.
 */
actual fun databaseBuilder(): RoomDatabase.Builder<AllergyDatabase> {
    val context = applicationContext()
    val dbFile = context.getDatabasePath(AllergyDatabase.DATABASE_NAME)
    return Room.databaseBuilder<AllergyDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
        .setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}
