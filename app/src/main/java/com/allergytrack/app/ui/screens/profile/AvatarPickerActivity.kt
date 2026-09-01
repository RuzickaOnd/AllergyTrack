package com.allergytrack.app.ui.screens.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.allergytrack.app.ui.theme.AllergyTrackTheme

class AvatarPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllergyTrackTheme {
                AvatarPickerScreen(onBack = { finish() })
            }
        }
    }
}
