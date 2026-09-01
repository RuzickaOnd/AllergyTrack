package com.allergytrack.shared.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.allergytrack.shared.platform.dataStorePath
import kotlinx.coroutines.Dispatchers

actual fun databaseBuilder(): RoomDatabase.Builder<AllergyDatabase> =
    Room.databaseBuilder<AllergyDatabase>(
        name = dataStorePath(AllergyDatabase.DATABASE_NAME)
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
