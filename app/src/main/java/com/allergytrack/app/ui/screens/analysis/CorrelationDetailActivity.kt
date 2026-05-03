package com.allergytrack.app.ui.screens.analysis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.allergytrack.app.ui.theme.AllergyTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CorrelationDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!intent.hasExtra(EXTRA_TRIGGER) || !intent.hasExtra(EXTRA_SYMPTOM_TYPE)) {
            finish()
            return
        }

        setContent {
            AllergyTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CorrelationDetailScreenRoot(
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TRIGGER = "trigger"
        private const val EXTRA_TRIGGER_TYPE = "triggerType"
        private const val EXTRA_SYMPTOM_TYPE = "symptomType"

        fun start(context: Context, trigger: String, triggerType: String, symptomType: String) {
            val intent = Intent(context, CorrelationDetailActivity::class.java).apply {
                putExtra(EXTRA_TRIGGER, trigger)
                putExtra(EXTRA_TRIGGER_TYPE, triggerType)
                putExtra(EXTRA_SYMPTOM_TYPE, symptomType)
            }
            context.startActivity(intent)
        }
    }
}
