package com.efthemiosprime.pasabayan.features.bookings.model.nested

/** Note: API sends coordinates as Strings. */
data class DeliveryAddressData(
    val address: String?,
    val city: String?,
    val latitude: String,
    val longitude: String,
) {
    val latitudeDouble: Double? get() = latitude.toDoubleOrNull()
    val longitudeDouble: Double? get() = longitude.toDoubleOrNull()
}
