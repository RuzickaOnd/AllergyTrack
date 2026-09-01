package com.allergytrack.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.app.data.worker.WorkManagerScheduler
import com.allergytrack.app.ui.navigation.MainAppScaffold
import com.allergytrack.app.ui.onboarding.OnboardingFlow
import com.allergytrack.app.ui.theme.AllergyTrackTheme
import com.allergytrack.app.ui.util.AppLocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val workManagerScheduler: WorkManagerScheduler by inject()
    private val onboardingDataStore: OnboardingDataStore by inject()
    private val userPreferencesDataStore: UserPreferencesDataStore by inject()
    private val appUpdateRepository: com.allergytrack.app.domain.repository.AppUpdateRepository by inject()

    private lateinit var appUpdateHelper: com.allergytrack.app.ui.util.AppUpdateHelper

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        appUpdateHelper = com.allergytrack.app.ui.util.AppUpdateHelper(this, appUpdateRepository)
        appUpdateHelper.checkForUpdates()

        // Synchronní čtení stavu před prvním sestavením UI — záměrné, trvá < 5ms.
        val onboardingCompleted = runBlocking { onboardingDataStore.isCompleted.first() }
        val initialDarkMode = runBlocking { userPreferencesDataStore.darkMode.first() }

        setContent {
            val darkMode by userPreferencesDataStore.darkMode.collectAsState(initial = initialDarkMode)
            
            // Dynamicky uzamknout orientaci na portrét pro kompaktní šířku (telefony)
            @OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
            val windowSize = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(this)
            if (windowSize.widthSizeClass == androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact) {
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

            AllergyTrackTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        initialOnboardingCompleted = onboardingCompleted,
                        appUpdateRepository = appUpdateRepository
                    )
                }
            }
        }

        workManagerScheduler.scheduleEnvironmentTracking()
    }
}

@Composable
fun AppRoot(
    initialOnboardingCompleted: Boolean,
    appUpdateRepository: com.allergytrack.app.domain.repository.AppUpdateRepository
) {
    var onboardingCompleted by remember { mutableStateOf(initialOnboardingCompleted) }

    if (!onboardingCompleted) {
        OnboardingFlow(
            onCompleted = { onboardingCompleted = true }
        )
    } else {
        MainAppScaffold(appUpdateRepository = appUpdateRepository)
    }
}
