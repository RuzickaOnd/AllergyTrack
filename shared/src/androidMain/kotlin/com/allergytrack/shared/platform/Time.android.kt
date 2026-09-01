package com.allergytrack.shared.platform

import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun deviceLanguage(): String = Locale.getDefault().language.lowercase()

actual fun deviceCountry(): String = Locale.getDefault().country.lowercase()
