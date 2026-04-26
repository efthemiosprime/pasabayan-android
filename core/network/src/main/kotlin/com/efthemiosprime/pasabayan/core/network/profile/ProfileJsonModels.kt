package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: ProfileDataJson,
)

@Serializable
data class ProfileDataJson(
    val profile: UserProfileJson? = null,
    @SerialName("home_city_id") val homeCityId: Int? = null,
    @SerialName("is_complete") val isComplete: Boolean = false,
)

/**
 * `PUT /profile` body per `09-profile-carrier-consent` — only non-null fields should be sent for partial updates.
 * For home city only, pass [homeCityId] alone.
 */
@Serializable
data class UpdateProfileRequestJson(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("profile_picture") val profilePicture: String? = null,
    @SerialName("preferred_contact_method") val preferredContactMethod: String? = null,
    @SerialName("additional_info") val additionalInfo: Map<String, String>? = null,
    @SerialName("home_city_id") val homeCityId: Int? = null,
)

@Serializable
data class SimpleApiMessageResponseJson(
    val success: Boolean,
    val message: String? = null,
)

@Serializable
data class AccountDeletionRequestJson(
    val confirm: Boolean,
    val reason: String? = null,
)

@Serializable
data class AccountDeletionResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: AccountDeletionDataJson? = null,
)

@Serializable
data class AccountDeletionDataJson(
    @SerialName("request_id") val requestId: String? = null,
    val status: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
)

@Serializable
data class DisclaimerAcknowledgmentsListResponseJson(
    val success: Boolean = true,
    val data: DisclaimerAcknowledgmentsDataJson? = null,
)

@Serializable
data class DisclaimerAcknowledgmentsDataJson(
    @SerialName("carrier_trip") val carrierTrip: String? = null,
    val shipper: String? = null,
)

@Serializable
data class DisclaimerAcknowledgmentRequestJson(
    @SerialName("disclaimer_type") val disclaimerType: String,
)

@Serializable
data class DisclaimerAcknowledgmentResponseJson(
    val success: Boolean = true,
    val data: DisclaimerAcknowledgmentDataJson? = null,
)

@Serializable
data class DisclaimerAcknowledgmentDataJson(
    @SerialName("disclaimer_type") val disclaimerType: String? = null,
    @SerialName("acknowledged_at") val acknowledgedAt: String? = null,
)
