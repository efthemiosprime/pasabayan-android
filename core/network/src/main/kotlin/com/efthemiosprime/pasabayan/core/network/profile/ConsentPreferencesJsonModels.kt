package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for `PUT /profile/consent-preferences` (iOS `ConsentOnboardingView`). */
@Serializable
data class ConsentPreferencesUpdateJson(
    @SerialName("push_notifications") val pushNotifications: Boolean,
    @SerialName("location_tracking") val locationTracking: Boolean,
    val analytics: Boolean,
    @SerialName("marketing_communications") val marketingCommunications: Boolean,
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
