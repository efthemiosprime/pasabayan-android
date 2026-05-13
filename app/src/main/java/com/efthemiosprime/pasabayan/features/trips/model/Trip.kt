package com.efthemiosprime.pasabayan.features.trips.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.PricingType
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Trip domain model — rich computed properties over the DTO.
 * Parity with iOS `Trip.swift`.
 */
data class Trip(
    val id: Int,
    val carrierId: Int,
    val originCity: String,
    val originCountry: String,
    val originLat: Double?,
    val originLng: Double?,
    val destinationCity: String,
    val destinationCountry: String,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val departureDate: String?,
    val arrivalDate: String?,
    /** Optional shared pickup window at origin. Strictly before [departureDate] when set. */
    val pickupDate: String? = null,
    /** Optional shared delivery window at destination. On/after [arrivalDate] when set. */
    val deliveryDate: String? = null,
    val availableWeightKg: Double?,
    val availableSpaceLiters: Double?,
    val pricePerKg: Double?,
    val tripStatus: TripStatus,
    val transportationMethod: TransportationMethod,
    val specialNotes: String?,
    val carrier: UserSummary?,
    val createdAt: String?,
    val updatedAt: String?,
    // Land pricing
    val pricingType: String?,
    val pricingMethod: String?,
    val flatTripPrice: Double?,
    val basePrice: Double?,
    val calculatedPrice: Double?,
    val distanceMultiplier: Double? = null,
    // Passenger transport
    val passengerCapacity: Int? = null,
    val pricePerPassenger: Double? = null,
    val passengerRequirements: String? = null,
    val ageRestrictions: String? = null,
    val passengerAmenities: String? = null,
    // Addresses
    val pickupAddress: String?,
    val pickupLandmark: String?,
    val pickupInstructions: String? = null,
    val dropoffAddress: String?,
    val dropoffLandmark: String?,
    val dropoffInstructions: String? = null,
    // Earnings
    val tripEarningsTotal: Double?,
    val tripEarningsCurrency: String?,
    val tripEarningsBreakdown: TripEarningsBreakdown?,
    // Pending
    val hasPendingRequests: Boolean?,
    val pendingRequestCount: Int?,
    val pendingRequests: List<PendingTripRequest>?,
    val distanceKm: Double?,
) {
    val route: String
        get() = "$originCity → $destinationCity"

    val hasCapacity: Boolean
        get() = (availableWeightKg ?: 0.0) > 0.0

    val isBookable: Boolean
        get() = tripStatus in listOf(TripStatus.PLANNING, TripStatus.ACTIVE) && hasCapacity

    val effectivePricingType: PricingType
        get() {
            if (pricingType == "flat") return PricingType.FLAT
            if (pricingType == "per_kg") return PricingType.PER_KG
            return transportationMethod.defaultPricingType
        }

    val effectivePrice: Double
        get() = when (effectivePricingType) {
            PricingType.FLAT -> calculatedPrice ?: flatTripPrice ?: 0.0
            PricingType.PER_KG -> pricePerKg ?: 0.0
        }

    val formattedPrice: String
        get() = when (effectivePricingType) {
            PricingType.PER_KG -> String.format("$%.2f/kg", effectivePrice)
            PricingType.FLAT -> String.format("$%.2f flat", effectivePrice)
        }

    val formattedPriceCompact: String
        get() = when (effectivePricingType) {
            PricingType.PER_KG -> String.format("$%.2f/kg", effectivePrice)
            PricingType.FLAT -> String.format("$%.2f", effectivePrice)
        }

    // Alias used by iOS naming in some parity docs.
    val priceDisplayString: String
        get() = formattedPrice

    val formattedCapacity: String
        get() = String.format("%.1f kg", availableWeightKg ?: 0.0)

    val formattedDepartureDate: String
        get() = DateTimeParsing.parseApiDateTime(departureDate)
            ?.let { DateTimeParsing.formatDateTime(it) } ?: ""

    val formattedArrivalDate: String
        get() = DateTimeParsing.parseApiDateTime(arrivalDate)
            ?.let { DateTimeParsing.formatDateTime(it) } ?: ""

    /** Date-only (e.g. "Mar 1, 2026") for the progress widget's "Arrives" line. */
    val formattedArrivalDateShort: String
        get() = DateTimeParsing.parseApiDateTime(arrivalDate)
            ?.let { DateTimeParsing.formatDateOnly(it) } ?: ""

    /**
     * iOS parity (`TripDetailsView.scheduleInformationCard`): the shared collection window
     * `pickupDate` falls back to `departureDate` for display. Null when neither is set.
     */
    val formattedPickupOrDepartureDate: String?
        get() = (pickupDate ?: departureDate)?.let { raw ->
            DateTimeParsing.parseApiDateTime(raw)
                ?.let { DateTimeParsing.formatDateTime(it) }
        }

    /**
     * iOS parity: the shared handoff window `deliveryDate` falls back to `arrivalDate` for
     * display. Null when neither is set.
     */
    val formattedDeliveryOrArrivalDate: String?
        get() = (deliveryDate ?: arrivalDate)?.let { raw ->
            DateTimeParsing.parseApiDateTime(raw)
                ?.let { DateTimeParsing.formatDateTime(it) }
        }

    val formattedDuration: String
        get() {
            val departure = DateTimeParsing.parseApiDateTime(departureDate) ?: return ""
            val arrival = DateTimeParsing.parseApiDateTime(arrivalDate) ?: return ""
            val durationMinutes = abs(arrival - departure) / (60 * 1000)
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    // iOS parity: Carrier "My Trips" TripCard left/right schedule columns
    // (TripCard.swift:97-115, Trip.swift:637-664). Left shows Pickup when the
    // shared pickup window is set, else falls back to Departure. Right shows
    // Delivery when the shared delivery window is set, else trip Duration.

    @get:StringRes
    val tripCardLeftLabelRes: Int
        get() = if (pickupDate != null) R.string.trips_detail_pickup else R.string.trips_detail_departure

    val tripCardLeftValue: String
        get() = formattedPickupOrDepartureDate ?: ""

    @get:StringRes
    val tripCardRightLabelRes: Int
        get() = if (deliveryDate != null) R.string.trips_detail_delivery else R.string.trips_card_duration

    val tripCardRightValue: String
        get() = if (deliveryDate != null) formattedDeliveryOrArrivalDate ?: "" else formattedDuration

    /** iOS parity (`TripCard.swift:284-291`): hide progress for planning/cancelled trips. */
    val shouldShowPackageProgress: Boolean
        get() = tripStatus != TripStatus.PLANNING && tripStatus != TripStatus.CANCELLED

    val routeDistanceKm: Double
        get() {
            if (distanceKm != null && distanceKm > 0) return distanceKm
            val oLat = originLat ?: return 0.0
            val oLng = originLng ?: return 0.0
            val dLat = destinationLat ?: return 0.0
            val dLng = destinationLng ?: return 0.0
            return haversineKm(oLat, oLng, dLat, dLng)
        }

    fun estimatedPrice(forWeightKg: Double): Double = when (effectivePricingType) {
        PricingType.PER_KG -> effectivePrice * forWeightKg
        PricingType.FLAT -> effectivePrice
    }
}

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
