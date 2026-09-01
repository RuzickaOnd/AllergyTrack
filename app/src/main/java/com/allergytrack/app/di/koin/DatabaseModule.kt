package com.allergytrack.app.di.koin

import androidx.room.Room
import com.allergytrack.app.data.local.database.AllergyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Doslovný překlad bývalého Hilt DatabaseModule.
 * Název DB a migrace se NESMÍ změnit — aplikace je živá s reálnými daty.
 */
val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AllergyDatabase::class.java,
            "allergy_track.db"
        )
            .addMigrations(AllergyDatabase.MIGRATION_1_2)
            .build()
    }

    single { get<AllergyDatabase>().foodLogDao() }
    single { get<AllergyDatabase>().symptomLogDao() }
    single { get<AllergyDatabase>().environmentLogDao() }
}
