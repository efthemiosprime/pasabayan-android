package com.efthemiosprime.pasabayan.core.network.verification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumVerificationResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: PremiumVerificationSubmissionDataJson? = null,
)

@Serializable
data class PremiumVerificationSubmissionDataJson(
    val status: String? = null,
    @SerialName("estimated_review_time") val estimatedReviewTime: String? = null,
)

@Serializable
data class PremiumVerificationStatusResponseJson(
    val success: Boolean,
    val data: PremiumVerificationStatusDataJson? = null,
)

@Serializable
data class PremiumVerificationStatusDataJson(
    @SerialName("current_status") val currentStatus: String? = null,
    @SerialName("verification_level") val verificationLevel: String? = null,
    @SerialName("premium_verified_at") val premiumVerifiedAt: String? = null,
    val requests: List<PremiumVerificationRequestJson> = emptyList(),
)

@Serializable
data class PremiumVerificationRequestJson(
    val id: Int? = null,
    val status: String? = null,
    @SerialName("id_type") val idType: String? = null,
    @SerialName("confidence_score") val confidenceScore: Double? = null,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
)

@Serializable
data class PremiumVerificationApplicationResponseJson(
    val success: Boolean = true,
    val data: PremiumVerificationRequestJson? = null,
)
