package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileJson(
    val id: Int? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("profile_picture") val profilePicture: String? = null,
    @SerialName("preferred_contact_method") val preferredContactMethod: String? = null,
    @SerialName("additional_info") val additionalInfo: Map<String, String>? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("verification_level") val verificationLevel: String? = null,
)
