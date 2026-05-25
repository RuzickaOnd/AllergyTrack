package com.allergytrack.app.ui.screens.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.app.ui.theme.AllergyTrackTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PremiumActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Záznam zobrazení Premium obrazovky
        analyticsManager.logScreenView("premium_screen", this::class.java.simpleName)
        analyticsManager.logPremiumInteraction("premium_page", "view_upsell")

        setContent {
            AllergyTrackTheme {
                PremiumScreen(
                    onBack = { finish() },
                    onBuyClick = {
                        analyticsManager.logPremiumInteraction("premium_page", "click_buy")
                    }
                )
            }
        }
    }
}
