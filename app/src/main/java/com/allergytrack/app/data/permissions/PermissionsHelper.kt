package com.allergytrack.app.data.permissions

import com.allergytrack.shared.platform.PermissionsState
import com.allergytrack.shared.platform.Permissions
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Centrální správce runtime oprávnění aplikace.
 * Zjišťuje aktuální stav — request provádí UI vrstva přes Compose launcher.
 */
class PermissionsHelper(
    private val context: Context
) : Permissions {
    /** Je uděleno oprávnění k přibližné GPS poloze? */
    override fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /** Je uděleno oprávnění ke kameře? */
    override fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    /** Je uděleno oprávnění k mikrofonu? */
    override fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    /** Je uděleno oprávnění k notifikacím? (Android 13+) */
    override fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pod Android 13 se notifikace povolují automaticky
        }

    /** Stav všech oprávnění najednou */
    override fun getPermissionsState() = PermissionsState(
        location = hasLocationPermission(),
        camera = hasCameraPermission(),
        notifications = hasNotificationPermission()
    )
}

