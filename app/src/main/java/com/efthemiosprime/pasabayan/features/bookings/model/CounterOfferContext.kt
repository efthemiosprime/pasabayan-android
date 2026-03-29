package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy

data class CounterOfferContext(
    val newPrice: Double,
    val originalPrice: Double,
    val counterOffererName: String,
    val counterOffererId: Int,
    val initiatedBy: InitiatedBy,
    val isCounterOffer: Boolean,
    val counterOfferRound: Int? = null,
    val remainingCounterOffers: Int? = null,
    val canCounterOffer: Boolean? = null,
) {
    val priceDifference: Double get() = newPrice - originalPrice

    val direction: String get() = if (priceDifference > 0) "up" else "down"

    val isPriceIncrease: Boolean get() = priceDifference > 0
}
