package com.efthemiosprime.pasabayan.features.shipper.model

/** Domain twin of [com.efthemiosprime.pasabayan.core.network.shipper.NearbyCarrierJson]. */
data class NearbyCarrier(
    val id: Int,
    val name: String,
    val avatar: String?,
    val completedDeliveries: Int,
    val distanceKm: Double?,
)

/**
 * Result of `GET /shipper/nearby-carriers`. When the user has not set a home city, the
 * backend returns an empty list and [homeCityId] / [homeCityName] are null.
 */
data class NearbyCarriers(
    val homeCityId: Int?,
    val homeCityName: String?,
    val radiusKm: Double?,
    val carriers: List<NearbyCarrier>,
) {
    val isHomeCitySet: Boolean get() = homeCityId != null
}
