package com.allergytrack.shared.platform

import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun deviceLanguage(): String =
    NSLocale.currentLocale.languageCode.lowercase()

actual fun deviceCountry(): String =
    NSLocale.currentLocale.countryCode?.lowercase() ?: ""

actual fun randomUuidString(): String = platform.Foundation.NSUUID().UUIDString()

actual fun startOfToday(): Long {
    val cal = platform.Foundation.NSCalendar.currentCalendar
    val start = cal.startOfDayForDate(platform.Foundation.NSDate())
    return (start.timeIntervalSince1970 * 1000).toLong()
}
