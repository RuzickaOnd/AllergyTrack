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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var workManagerScheduler: WorkManagerScheduler
    @Inject lateinit var onboardingDataStore: OnboardingDataStore
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Synchronní čtení stavu před prvním sestavením UI — záměrné, trvá < 5ms.
        val onboardingCompleted = runBlocking { onboardingDataStore.isCompleted.first() }
        val initialDarkMode = runBlocking { userPreferencesDataStore.darkMode.first() }

        setContent {
            val darkMode by userPreferencesDataStore.darkMode.collectAsState(initial = initialDarkMode)
            AllergyTrackTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(initialOnboardingCompleted = onboardingCompleted)
                }
            }
        }

        workManagerScheduler.scheduleEnvironmentTracking()
    }
}

@Composable
fun AppRoot(initialOnboardingCompleted: Boolean) {
    var onboardingCompleted by remember { mutableStateOf(initialOnboardingCompleted) }

    if (!onboardingCompleted) {
        OnboardingFlow(
            onCompleted = { onboardingCompleted = true }
        )
    } else {
        MainAppScaffold()
    }
}
