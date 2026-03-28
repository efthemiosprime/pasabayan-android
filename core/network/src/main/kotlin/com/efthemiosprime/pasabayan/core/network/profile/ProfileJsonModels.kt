package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileHomeCityRequestJson(
    @SerialName("home_city_id") val homeCityId: Int,
)

@Serializable
data class ProfileResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: ProfileDataJson,
)

@Serializable
data class ProfileDataJson(
    @SerialName("home_city_id") val homeCityId: Int? = null,
    @SerialName("is_complete") val isComplete: Boolean = false,
)
