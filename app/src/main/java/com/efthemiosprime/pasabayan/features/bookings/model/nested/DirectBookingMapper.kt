package com.efthemiosprime.pasabayan.features.bookings.model.nested

import com.efthemiosprime.pasabayan.core.network.bookings.DirectBookingInfoJson

/**
 * Map the wire `data.booking` payload of `POST /trips/{id}/book` to the
 * domain projection. Direct booking has no package request, so the existing
 * [DirectBookingData.packageRequestId] stays null.
 */
fun DirectBookingInfoJson.toDomain(): DirectBookingData = DirectBookingData(
    bookingId = id,
    tripId = tripId,
    packageRequestId = null,
    agreedPrice = priceAgreed ?: 0.0,
    status = status.orEmpty(),
)
