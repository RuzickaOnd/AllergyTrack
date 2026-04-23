package com.allergytrack.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// We focus on Dark Mode as per the wireframe
private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Background,
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    onTertiary = Background,
    
    background = Background,
    onBackground = TextPrimary,
    
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = Border,
    error = DotRed
)

@Composable
fun AllergyTrackTheme(
    content: @Composable () -> Unit
) {
    // We enforce the dark wireframe theme for the MVP
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
