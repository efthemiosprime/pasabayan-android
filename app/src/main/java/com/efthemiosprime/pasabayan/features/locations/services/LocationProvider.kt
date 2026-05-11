package com.efthemiosprime.pasabayan.features.locations.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.efthemiosprime.pasabayan.features.locations.model.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * One-shot GPS lookup wrapped behind an interface so the service layer can be tested with a
 * stub. The Play Services impl uses `FusedLocationProviderClient.getCurrentLocation()` with
 * the [timeoutMillis] enforced via `withTimeoutOrNull`.
 */
interface LocationProvider {
    /**
     * Returns the current device coordinates, or `null` when the permission is missing, GPS
     * is disabled, or the lookup times out.
     */
    suspend fun currentLocation(timeoutMillis: Long = DEFAULT_TIMEOUT_MS): GeoPoint?

    companion object {
        const val DEFAULT_TIMEOUT_MS: Long = 10_000L
    }
}

@Singleton
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(timeoutMillis: Long): GeoPoint? {
        if (!hasLocationPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val tokenSource = CancellationTokenSource()
        return withTimeoutOrNull(timeoutMillis) {
            suspendCancellableCoroutine<GeoPoint?> { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                    .addOnSuccessListener { location ->
                        cont.resume(
                            location?.let { GeoPoint(it.latitude, it.longitude) },
                        )
                    }
                    .addOnFailureListener { cont.resume(null) }
                cont.invokeOnCancellation { tokenSource.cancel() }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}
