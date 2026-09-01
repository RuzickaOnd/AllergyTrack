package com.allergytrack.app.di.koin

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.datastore.core.DataStore
import androidx.work.WorkerParameters
import io.ktor.client.HttpClient
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Ověřuje, že Koin graf jde rozřešit — každý konstruktorový parametr
 * každé definice má odpovídající definici.
 *
 * Bez tohoto testu by se chybějící `single { }` projevila až za běhu
 * na zařízení jako NoDefinitionFoundException, tedy nejdřív při Tasku 7.
 *
 * Ověřuje se celá sestava naráz přes includes(), protože moduly na sebe
 * navzájem odkazují (coreModule potřebuje UserPreferencesDataStore
 * z dataStoreModule) — verify() jednoho modulu je vidět nemůže.
 *
 * Seznam modulů musí zůstat shodný s tím v AllergyTrackApplication.startKoin.
 *
 * extraTypes = typy, které Koin dodává sám z běhového prostředí:
 *   Context           — z androidContext() v startKoin
 *   SavedStateHandle  — z ViewModelStoreOwner
 *   WorkerParameters  — z WorkerFactory
 *   String, Boolean   — konstruktor AppConfig bere literály z lambdy;
 *                       verify() čte konstruktor reflexí a lambdu nevidí
 *   HttpClient        — vzniká uvnitř továren v :shared, ne jako definice
 *   DataStore         — totéž, vzniká v createDataStore()
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModuleGraphTest {

    private val vsechnyModuly = module {
        includes(
            coreModule,
            dataStoreModule,
            databaseModule,
            networkModule,
            repositoryModule,
            domainModule,
            viewModelModule,
            workerModule
        )
    }

    @Test
    fun celyGrafZavislostiJdeRozresit() {
        vsechnyModuly.verify(
            extraTypes = listOf(
                Context::class,
                SavedStateHandle::class,
                WorkerParameters::class,
                String::class,
                Boolean::class,
                HttpClient::class,
                DataStore::class
            )
        )
    }
}
