package com.allergytrack.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.allergytrack.shared.platform.Coordinates
import com.allergytrack.shared.platform.LocationSource
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(
    private val context: Context
) : LocationSource {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Vrací aktuální polohu s přiměřenou přesností. Může spustit GPS/síťový fix — pomalejší.
    @SuppressLint("MissingPermission")
    /**
     * SecurityException se chytá zde. Dřív ji chytal EnvironmentRepositoryImpl,
     * ale ten je teď v commonMain, kde tenhle typ neexistuje. Pozice odchycení
     * se změnila, chování ne — v obou případech výsledkem je null.
     */
    override suspend fun getCurrentLocation(): Coordinates? = try {
        rawCurrentLocation()?.let { Coordinates(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null
    }

    override suspend fun getLastKnownLocation(): Coordinates? =
        rawLastKnownLocation()?.let { Coordinates(it.latitude, it.longitude) }

    private suspend fun rawCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            continuation.invokeOnCancellation { cts.cancel() }
        }

    // Vrací poslední kešovanou polohu ze systému — okamžité, bez čekání na GPS/síť.
    // Může vrátit null pokud oprávnění není uděleno nebo systém nemá žádnou kešovanou pozici.
    @SuppressLint("MissingPermission")
    private suspend fun rawLastKnownLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
}
