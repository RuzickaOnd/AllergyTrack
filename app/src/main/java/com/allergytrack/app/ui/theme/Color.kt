package com.allergytrack.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
// AllergyTrack — Finální barevná paleta
// Lipový list se stopkou · Forest Green
// ─────────────────────────────────────────────

// ── Primary: Forest Green ──────────────────────
val Forest900 = Color(0xFF1A3324)
val Forest800 = Color(0xFF234530)
val Forest700 = Color(0xFF2D5A3D)
val Forest600 = Color(0xFF3D6E4F)
val Forest500 = Color(0xFF4A7C59)   // ← Primary (light mode)
val Forest400 = Color(0xFF6A9E78)
val Forest300 = Color(0xFF8DC09A)
val Forest200 = Color(0xFFC2DECA)
val Forest100 = Color(0xFFEAF4ED)

// ── Primary Dark mode: Sage ────────────────────
val Sage900 = Color(0xFF1A3324)
val Sage800 = Color(0xFF2E5A3E)
val Sage700 = Color(0xFF3E7050)
val Sage600 = Color(0xFF5E9E6E)
val Sage500 = Color(0xFF7AC48A)     // ← Primary (dark mode)
val Sage400 = Color(0xFF9ED4AA)
val Sage300 = Color(0xFFA8D8B0)
val Sage200 = Color(0xFFCEEDD4)
val Sage100 = Color(0xFFEAF7EC)

// ── Alert / Symptom: Signal Red ───────────────
val Signal900 = Color(0xFF4A1010)
val Signal800 = Color(0xFF7A1E1E)
val Signal700 = Color(0xFFA02828)
val Signal600 = Color(0xFFC43636)   // ← Alert (light mode)
val Signal500 = Color(0xFFD85A5A)
val Signal400 = Color(0xFFE88080)   // ← Alert (dark mode)
val Signal300 = Color(0xFFF0A0A0)
val Signal200 = Color(0xFFF5BABA)
val Signal100 = Color(0xFFFDF0F0)

// ── Warning: Amber ────────────────────────────
val Amber900 = Color(0xFF3C2008)
val Amber800 = Color(0xFF6E4010)
val Amber700 = Color(0xFF9A5C18)
val Amber600 = Color(0xFFC47820)    // ← Warning (light mode)
val Amber500 = Color(0xFFD99840)
val Amber400 = Color(0xFFE8B86A)    // ← Warning (dark mode)
val Amber300 = Color(0xFFF0CCA0)
val Amber200 = Color(0xFFF5D8A0)
val Amber100 = Color(0xFFFDF5E4)

// ── Neutral: Ink / Mist ───────────────────────
// Dark
val Ink900 = Color(0xFF0C0D0A)
val Ink800 = Color(0xFF111210)      // ← Background dark
val Ink700 = Color(0xFF1A1C18)      // ← Surface dark / Text primary dark
val Ink600 = Color(0xFF282B24)      // ← Surface variant dark
val Ink500 = Color(0xFF4A4E44)      // ← Disabled dark
val Ink400 = Color(0xFF6E7268)      // ← Muted text
val Ink300 = Color(0xFF9A9E94)      // ← Placeholder text
val Ink200 = Color(0xFFC8CCB8)      // ← Text light on dark surface
val Ink100 = Color(0xFFE8EAE4)      // ← Divider light

// Light
val Mist100 = Color(0xFFFFFFFF)     // ← Pure white
val Mist200 = Color(0xFFFCFDF9)
val Mist300 = Color(0xFFF9FAF7)
val Mist400 = Color(0xFFF5F7F2)     // ← Background light
val Mist500 = Color(0xFFF0F2EC)     // ← Surface light
val Mist600 = Color(0xFFE8EAE4)     // ← Divider / border light
val Mist700 = Color(0xFFD8DAD0)

// ── Material 3 Role Mapping ───────────────────

// Light scheme
val md_theme_light_primary = Forest500
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Forest100
val md_theme_light_onPrimaryContainer = Forest800

val md_theme_light_secondary = Forest400
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Forest200
val md_theme_light_onSecondaryContainer = Forest700

val md_theme_light_tertiary = Amber600
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Amber100
val md_theme_light_onTertiaryContainer = Amber800

val md_theme_light_error = Signal600
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Signal100
val md_theme_light_onErrorContainer = Signal800

val md_theme_light_background = Mist400
val md_theme_light_onBackground = Ink700
val md_theme_light_surface = Mist100
val md_theme_light_onSurface = Ink700
val md_theme_light_surfaceVariant = Mist500
val md_theme_light_onSurfaceVariant = Ink400
val md_theme_light_outline = Mist600
val md_theme_light_outlineVariant = Ink100
val md_theme_light_inverseSurface = Ink700
val md_theme_light_inverseOnSurface = Mist400
val md_theme_light_inversePrimary = Sage500

// Dark scheme
val md_theme_dark_primary = Sage500
val md_theme_dark_onPrimary = Forest900
val md_theme_dark_primaryContainer = Forest700
val md_theme_dark_onPrimaryContainer = Forest100

val md_theme_dark_secondary = Sage400
val md_theme_dark_onSecondary = Forest900
val md_theme_dark_secondaryContainer = Forest600
val md_theme_dark_onSecondaryContainer = Forest100

val md_theme_dark_tertiary = Amber400
val md_theme_dark_onTertiary = Amber900
val md_theme_dark_tertiaryContainer = Amber700
val md_theme_dark_onTertiaryContainer = Amber100

val md_theme_dark_error = Signal400
val md_theme_dark_onError = Signal900
val md_theme_dark_errorContainer = Signal800
val md_theme_dark_onErrorContainer = Signal200

val md_theme_dark_background = Ink800
val md_theme_dark_onBackground = Ink200
val md_theme_dark_surface = Ink700
val md_theme_dark_onSurface = Ink200
val md_theme_dark_surfaceVariant = Ink600
val md_theme_dark_onSurfaceVariant = Ink300
val md_theme_dark_outline = Ink600
val md_theme_dark_outlineVariant = Ink500
val md_theme_dark_inverseSurface = Mist400
val md_theme_dark_inverseOnSurface = Ink700
val md_theme_dark_inversePrimary = Forest500