package com.allergytrack.shared.platform

import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun deviceLanguage(): String = Locale.getDefault().language.lowercase()

actual fun deviceCountry(): String = Locale.getDefault().country.lowercase()

actual fun randomUuidString(): String = java.util.UUID.randomUUID().toString()

actual fun startOfToday(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
