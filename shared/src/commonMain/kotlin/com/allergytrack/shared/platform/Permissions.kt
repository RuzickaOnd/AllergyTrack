package com.allergytrack.shared.platform

/** Implementuje PermissionsHelper v :app (Android runtime permissions). */
interface Permissions {
    fun hasLocationPermission(): Boolean
    fun hasCameraPermission(): Boolean
    fun hasMicrophonePermission(): Boolean
    fun hasNotificationPermission(): Boolean
    fun getPermissionsState(): PermissionsState
}

data class PermissionsState(
    val location: Boolean,
    val camera: Boolean,
    val notifications: Boolean
) {
    val allGranted: Boolean get() = location && camera && notifications
    val locationOrCameraGranted: Boolean get() = location || camera
}
