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

actual fun currentHourOfDay(): Int =
    platform.Foundation.NSCalendar.currentCalendar
        .component(platform.Foundation.NSCalendarUnitHour, platform.Foundation.NSDate())
        .toInt()

actual fun dayAndMonth(epochMillis: Long): Pair<Int, Int> {
    // NSDate počítá od 1. 1. 2001; 978307200 s je odstup od unixové epochy.
    val date = platform.Foundation.NSDate(
        timeIntervalSinceReferenceDate = epochMillis / 1000.0 - 978_307_200.0
    )
    val cal = platform.Foundation.NSCalendar.currentCalendar
    val day = cal.component(platform.Foundation.NSCalendarUnitDay, date).toInt()
    val month = cal.component(platform.Foundation.NSCalendarUnitMonth, date).toInt()
    return day to month
}
