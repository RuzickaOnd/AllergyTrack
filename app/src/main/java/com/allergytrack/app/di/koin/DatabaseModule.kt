package com.allergytrack.app.di.koin

import com.allergytrack.shared.data.local.database.AllergyDatabase
import com.allergytrack.shared.data.local.database.createAllergyDatabase
import org.koin.dsl.module

/**
 * Room žije celý v :shared. Název DB, verze i migrace jsou v AllergyDatabase
 * a nesmí se změnit — aplikace je živá s reálnými daty.
 */
val databaseModule = module {
    single { createAllergyDatabase() }
    single { get<AllergyDatabase>().foodLogDao() }
    single { get<AllergyDatabase>().symptomLogDao() }
    single { get<AllergyDatabase>().environmentLogDao() }
}
