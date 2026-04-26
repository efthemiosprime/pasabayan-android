package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for `PUT /profile/consent-preferences`.
 * All keys optional so callers may send a scoped map (iOS partial update); omit keys with `null`.
 */
@Serializable
data class ConsentPreferencesUpdateJson(
    @SerialName("push_notifications") val pushNotifications: Boolean? = null,
    @SerialName("location_tracking") val locationTracking: Boolean? = null,
    val analytics: Boolean? = null,
    @SerialName("marketing_communications") val marketingCommunications: Boolean? = null,
)

@Serializable
data class ConsentPreferencesResponseJson(
    val success: Boolean,
    val data: ConsentPreferencesDataJson? = null,
)

@Serializable
data class ConsentPreferencesDataJson(
    @SerialName("push_notifications") val pushNotifications: Boolean,
    @SerialName("location_tracking") val locationTracking: Boolean,
    val analytics: Boolean,
    @SerialName("marketing_communications") val marketingCommunications: Boolean,
)
