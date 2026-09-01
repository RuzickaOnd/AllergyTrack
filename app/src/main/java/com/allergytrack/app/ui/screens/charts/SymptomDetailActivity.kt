package com.allergytrack.app.ui.screens.charts

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
import com.allergytrack.app.ui.theme.AllergyTrackTheme

class SymptomDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllergyTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SymptomDetailScreen(onBack = { finish() })
                }
            }
        }
    }

    companion object {
        private const val EXTRA_RANGE = "range"

        fun start(context: Context, range: TimeRange) {
            context.startActivity(
                Intent(context, SymptomDetailActivity::class.java).apply {
                    putExtra(EXTRA_RANGE, range.name)
                }
            )
        }
    }
}
