package com.efthemiosprime.pasabayan.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)
