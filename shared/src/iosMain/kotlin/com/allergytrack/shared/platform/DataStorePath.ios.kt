package com.allergytrack.shared.platform

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/** Adresář Documents aplikace — společný základ pro DataStore i databázi. */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal fun documentsDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(requireNotNull(documentDirectory).path)
}

actual fun dataStorePath(fileName: String): String =
    documentsDirectory() + "/$fileName.preferences_pb"

/**
 * Absolutní cesta k SQLite souboru databáze. Záměrně nepoužívá dataStorePath —
 * ta připojuje příponu .preferences_pb, která na databázi nepatří.
 */
internal fun databasePath(fileName: String): String =
    documentsDirectory() + "/$fileName"
