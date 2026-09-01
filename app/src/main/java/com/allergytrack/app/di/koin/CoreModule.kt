package com.allergytrack.app.di.koin

import com.allergytrack.app.BuildConfig
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.app.data.local.recognition.FoodImageRecognizer
import com.allergytrack.app.data.location.LocationProvider
import com.allergytrack.app.data.notifications.NotificationManager
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.remote.ai.AiSummaryService
import com.allergytrack.app.data.worker.WorkManagerScheduler
import com.allergytrack.app.platform.MedicalReportExporter
import com.allergytrack.shared.config.AppConfig
import com.allergytrack.shared.domain.export.ReportExporter
import com.allergytrack.shared.platform.AiSummaryGenerator
import com.allergytrack.shared.platform.Analytics
import com.allergytrack.shared.platform.AppNotifications
import com.allergytrack.app.platform.AndroidDeviceInfo
import com.allergytrack.shared.platform.BackgroundScheduler
import com.allergytrack.shared.platform.DeviceInfo
import com.allergytrack.shared.platform.ImageRecognizer
import com.allergytrack.shared.platform.Permissions
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

    single<Permissions> { PermissionsHelper(androidContext()) }
    single<DeviceInfo> { AndroidDeviceInfo(androidContext()) }
    single<LocationSource> { LocationProvider(androidContext()) }
    single<BackgroundScheduler> { WorkManagerScheduler(androidContext()) }
    single<Analytics> { AnalyticsManager(androidContext()) }
    // Application volá initialize(), což na rozhraní není — proto i konkrétní typ.
    single { get<Analytics>() as AnalyticsManager }
    single<AiSummaryGenerator> { AiSummaryService(androidContext()) }
    single<ReportExporter> { MedicalReportExporter(androidContext()) }
    single<ImageRecognizer> { FoodImageRecognizer() }
    single<AppNotifications> { NotificationManager(androidContext(), get()) }
    // FCM služba volá showPushNotification(), které na rozhraní není — proto
    // i vazba na konkrétní typ. Stejný důvod jako u AnalyticsManager.initialize().
    single { get<AppNotifications>() as NotificationManager }
}
