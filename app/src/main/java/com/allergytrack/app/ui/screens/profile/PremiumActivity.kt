package com.allergytrack.app.ui.screens.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.app.domain.model.PremiumStatus
import com.allergytrack.app.ui.theme.AllergyTrackTheme
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter
import org.koin.android.ext.android.inject

class PremiumActivity : ComponentActivity() {

    private val analyticsManager: AnalyticsManager by inject()

    private val viewModel: PremiumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Záznam zobrazení Premium obrazovky
        analyticsManager.logScreenView("premium_screen", this::class.java.simpleName)
        analyticsManager.logPremiumInteraction("premium_page", "view_upsell")

        setContent {
            AllergyTrackTheme {
                val premiumStatus by viewModel.premiumStatus.collectAsState()

                if (premiumStatus == PremiumStatus.PREMIUM || premiumStatus == PremiumStatus.TRIAL) {
                    // Pokud je uživatel Premium, zobrazíme Customer Center pro správu/zrušení předplatného
                    CustomerCenter(
                        modifier = Modifier.fillMaxSize(),
                        onDismiss = { finish() }
                    )
                } else {
                    // Pokud je uživatel Free, zobrazíme RevenueCat Paywall pro nákup
                    Paywall(
                        options = PaywallOptions.Builder(dismissRequest = { finish() })
                            .setListener(object : PaywallListener {
                                override fun onPurchaseStarted(rcPackage: Package) {
                                    viewModel.logPurchaseStarted(rcPackage.identifier)
                                }

                                override fun onPurchaseCompleted(
                                    customerInfo: CustomerInfo,
                                    storeTransaction: StoreTransaction
                                ) {
                                    viewModel.logPurchaseCompleted()
                                    Toast.makeText(
                                        this@PremiumActivity,
                                        "Předplatné bylo úspěšně zakoupeno!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    finish()
                                }

                                override fun onPurchaseError(error: PurchasesError) {
                                    viewModel.logPurchaseError(error.code.name, error.message)
                                    Toast.makeText(
                                        this@PremiumActivity,
                                        "Chyba při nákupu: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                override fun onPurchaseCancelled() {
                                    viewModel.logPurchaseCancelled()
                                }

                                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                    viewModel.logRestoreCompleted()
                                    Toast.makeText(
                                        this@PremiumActivity,
                                        "Nákupy byly úspěšně obnoveny!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                            .build()
                    )
                }
            }
        }
    }
}
