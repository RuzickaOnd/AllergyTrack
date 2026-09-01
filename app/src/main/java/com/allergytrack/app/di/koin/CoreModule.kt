package com.allergytrack.app.di.koin

import com.allergytrack.app.BuildConfig
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.app.data.local.recognition.FoodImageRecognizer
import com.allergytrack.app.data.location.LocationProvider
import com.allergytrack.app.data.notifications.NotificationManager
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.remote.ai.AiSummaryService
import com.allergytrack.app.data.worker.WorkManagerScheduler
import com.allergytrack.app.domain.export.MedicalReportExporter
import com.allergytrack.shared.config.AppConfig
import com.allergytrack.shared.platform.ImageRecognizer
import com.allergytrack.shared.platform.LocationSource
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Platformní služby a sdílené nástroje.
 *
 * Odpovídá bývalému Hilt SingletonComponent bez vlastního @Module —
 * třídy se dřív injektovaly konstruktorem s @ApplicationContext.
 */
val coreModule = module {

    /** Nahrazuje BuildConfig pro kód mířící do commonMain. */
    single {
        AppConfig(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY,
            isDebug = BuildConfig.DEBUG,
            versionName = BuildConfig.VERSION_NAME
        )
    }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single { PermissionsHelper(androidContext()) }
    single<LocationSource> { LocationProvider(androidContext()) }
    single { WorkManagerScheduler(androidContext()) }
    single { AnalyticsManager(androidContext()) }
    single { AiSummaryService(androidContext()) }
    single { MedicalReportExporter(androidContext()) }
    single<ImageRecognizer> { FoodImageRecognizer() }
    single { NotificationManager(androidContext(), get()) }
}
