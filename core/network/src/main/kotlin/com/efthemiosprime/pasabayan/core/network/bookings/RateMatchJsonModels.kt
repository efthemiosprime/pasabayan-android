package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for `POST /matches/{id}/rate`. Mirrors iOS' inline rating payload sent
 * from `RatingViewModel.submitRating` — integer star value (1–5) and optional
 * review text.
 */
@Serializable
data class RateMatchRequestJson(
    val rating: Int,
    @SerialName("review_text") val reviewText: String? = null,
)
