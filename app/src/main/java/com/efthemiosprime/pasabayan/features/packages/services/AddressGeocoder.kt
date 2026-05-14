package com.efthemiosprime.pasabayan.features.packages.services

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.efthemiosprime.pasabayan.core.domain.model.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves free-text addresses to coordinates and vice versa, using the
 * Android framework [Geocoder] (no API key required).
 *
 * iOS parity: `CreateServiceRequestView` uses CoreLocation `CLGeocoder` to
 * convert the typed store / delivery address into a map region and back-fill
 * the address from a map tap. This service mirrors that minimal surface area.
 *
 * All entry points run off the main thread — the legacy blocking API can take
 * several seconds, and the new `Geocoder.GeocodeListener` callback should not
 * run on Main per the docs.
 */
@Singleton
class AddressGeocoder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Forward geocode an address (e.g. "1455 Boul. de Maisonneuve O., Montreal")
     * to a [Coordinates]. Returns `null` when the address is blank, the system
     * has no geocoding backend, or no match is found. Never throws.
     */
    suspend fun forward(address: String): Coordinates? {
        val trimmed = address.trim()
        if (trimmed.isEmpty()) return null
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                forwardListener(geocoder, trimmed)
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(trimmed, 1)?.firstOrNull()?.let {
                        Coordinates(latitude = it.latitude, longitude = it.longitude)
                    }
                }
            }
        }.getOrNull()
    }

    /**
     * Reverse geocode a coordinate to a single-line street address. Returns
     * `null` when no result is available. Used to back-fill the address field
     * after the shipper taps the map.
     */
    suspend fun reverse(coordinates: Coordinates): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                reverseListener(geocoder, coordinates)
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                        ?.firstOrNull()
                        ?.let(::formatAddress)
                }
            }
        }.getOrNull()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun forwardListener(geocoder: Geocoder, address: String): Coordinates? =
        suspendCoroutine { cont ->
            geocoder.getFromLocationName(address, 1) { addresses ->
                val result = addresses.firstOrNull()?.let {
                    Coordinates(latitude = it.latitude, longitude = it.longitude)
                }
                cont.resume(result)
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun reverseListener(geocoder: Geocoder, coordinates: Coordinates): String? =
        suspendCoroutine { cont ->
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1) { addresses ->
                cont.resume(addresses.firstOrNull()?.let(::formatAddress))
            }
        }

    private fun formatAddress(address: android.location.Address): String? {
        // Compose the first available street + locality so the user gets an
        // intuitive label, not a sparse JSON-looking fragment.
        val lineCount = address.maxAddressLineIndex
        if (lineCount >= 0) {
            return (0..lineCount).joinToString(separator = ", ") { address.getAddressLine(it) }
        }
        return listOfNotNull(
            address.thoroughfare,
            address.locality,
            address.adminArea,
            address.countryName,
        ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null }
    }
}
