package com.allergytrack.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────
// AllergyTrack — Material 3 Color Schemes
// ─────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary                = md_theme_light_primary,
    onPrimary              = md_theme_light_onPrimary,
    primaryContainer       = md_theme_light_primaryContainer,
    onPrimaryContainer     = md_theme_light_onPrimaryContainer,
    secondary              = md_theme_light_secondary,
    onSecondary            = md_theme_light_onSecondary,
    secondaryContainer     = md_theme_light_secondaryContainer,
    onSecondaryContainer   = md_theme_light_onSecondaryContainer,
    tertiary               = md_theme_light_tertiary,
    onTertiary             = md_theme_light_onTertiary,
    tertiaryContainer      = md_theme_light_tertiaryContainer,
    onTertiaryContainer    = md_theme_light_onTertiaryContainer,
    error                  = md_theme_light_error,
    onError                = md_theme_light_onError,
    errorContainer         = md_theme_light_errorContainer,
    onErrorContainer       = md_theme_light_onErrorContainer,
    background             = md_theme_light_background,
    onBackground           = md_theme_light_onBackground,
    surface                = md_theme_light_surface,
    onSurface              = md_theme_light_onSurface,
    surfaceVariant         = md_theme_light_surfaceVariant,
    onSurfaceVariant       = md_theme_light_onSurfaceVariant,
    outline                = md_theme_light_outline,
    outlineVariant         = md_theme_light_outlineVariant,
    inverseSurface         = md_theme_light_inverseSurface,
    inverseOnSurface       = md_theme_light_inverseOnSurface,
    inversePrimary         = md_theme_light_inversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary                = md_theme_dark_primary,
    onPrimary              = md_theme_dark_onPrimary,
    primaryContainer       = md_theme_dark_primaryContainer,
    onPrimaryContainer     = md_theme_dark_onPrimaryContainer,
    secondary              = md_theme_dark_secondary,
    onSecondary            = md_theme_dark_onSecondary,
    secondaryContainer     = md_theme_dark_secondaryContainer,
    onSecondaryContainer   = md_theme_dark_onSecondaryContainer,
    tertiary               = md_theme_dark_tertiary,
    onTertiary             = md_theme_dark_onTertiary,
    tertiaryContainer      = md_theme_dark_tertiaryContainer,
    onTertiaryContainer    = md_theme_dark_onTertiaryContainer,
    error                  = md_theme_dark_error,
    onError                = md_theme_dark_onError,
    errorContainer         = md_theme_dark_errorContainer,
    onErrorContainer       = md_theme_dark_onErrorContainer,
    background             = md_theme_dark_background,
    onBackground           = md_theme_dark_onBackground,
    surface                = md_theme_dark_surface,
    onSurface              = md_theme_dark_onSurface,
    surfaceVariant         = md_theme_dark_surfaceVariant,
    onSurfaceVariant       = md_theme_dark_onSurfaceVariant,
    outline                = md_theme_dark_outline,
    outlineVariant         = md_theme_dark_outlineVariant,
    inverseSurface         = md_theme_dark_inverseSurface,
    inverseOnSurface       = md_theme_dark_inverseOnSurface,
    inversePrimary         = md_theme_dark_inversePrimary,
)

// ─────────────────────────────────────────────
// AllergyTrack Theme Composable
// ─────────────────────────────────────────────

/**
 * Hlavní téma aplikace AllergyTrack.
 *
 * @param darkTheme Přepnutí dark/light módu. Výchozí = systémové nastavení.
 * @param dynamicColor Povolení Material You dynamických barev (Android 12+).
 *        Pro zachování identity brandu je výchozí FALSE.
 * @param content Obsah aplikace.
 */
@Composable
fun AllergyTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,      // ← záměrně false: chceme svoji paletu
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Nastavení barvy status baru a navigační lišty — edge-to-edge design
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.background.toArgb()
            }
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

// ─────────────────────────────────────────────
// Extension helpers pro sémantické použití barev
// Použití: MaterialTheme.colorScheme.symptomColor
// ─────────────────────────────────────────────

/**
 * Barva pro zobrazení alergického symptomu nebo varování.
 * = error barva v M3 schématu (Signal Red)
 */
val androidx.compose.material3.ColorScheme.symptomColor
    get() = error

/**
 * Barva pro kontejner symptomu (světlé pozadí za varováním).
 */
val androidx.compose.material3.ColorScheme.symptomContainer
    get() = errorContainer

/**
 * Barva pro středně vysoké hodnoty pylu (Amber / Warning).
 * = tertiary barva v M3 schématu
 */
val androidx.compose.material3.ColorScheme.warningColor
    get() = tertiary

/**
 * Barva pro nízké/normální hodnoty prostředí.
 * = primary barva
 */
val androidx.compose.material3.ColorScheme.okColor
    get() = primary

/**
 * Barva karty prostředí podle pylového indexu.
 *
 * @param pollenIndex Hodnota 0–5
 */
fun androidx.compose.material3.ColorScheme.environmentCardColor(pollenIndex: Int?) =
    when {
        pollenIndex == null  -> surfaceVariant       // data nedostupná
        pollenIndex >= 4     -> errorContainer       // vysoký pyl
        pollenIndex >= 2     -> tertiaryContainer    // střední pyl
        else                 -> primaryContainer     // nízký pyl
    }