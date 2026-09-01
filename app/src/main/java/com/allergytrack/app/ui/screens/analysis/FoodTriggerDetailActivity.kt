package com.allergytrack.app.ui.screens.analysis

import com.allergytrack.shared.presentation.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.allergytrack.app.ui.theme.AllergyTrackTheme

class FoodTriggerDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!intent.hasExtra(EXTRA_TRIGGER_KEY)) {
            finish()
            return
        }

        setContent {
            AllergyTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Použijeme NavController jen pro vnitřní navigaci pokud by byla potřeba, 
                    // ale pro zavření aktivity použijeme finish()
                    val navController = rememberNavController()
                    
                    // Získáme AnalysisViewModel (který přepočítá data)
                    // nebo lépe FoodTriggerDetailViewModel který si data načte sám
                    
                    // Prozatím předpokládáme že FoodTriggerDetailScreen si poradí 
                    // s tím, že dostane triggerKey a načte si data přes svůj ViewModel
                    
                    // Upravíme FoodTriggerDetailScreen aby přijímal triggerKey 
                    // a sám si vytáhl data z ViewModelu
                    FoodTriggerDetailScreenRoot(
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TRIGGER_KEY = "triggerKey"

        fun start(context: Context, triggerKey: String) {
            val intent = Intent(context, FoodTriggerDetailActivity::class.java).apply {
                putExtra(EXTRA_TRIGGER_KEY, triggerKey)
            }
            context.startActivity(intent)
        }
    }
}
