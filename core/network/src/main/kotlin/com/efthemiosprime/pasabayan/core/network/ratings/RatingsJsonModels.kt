package com.efthemiosprime.pasabayan.core.network.ratings

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingUserInfoJson(
    val id: Int,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
    val rating: String? = null,
)

/** Received-ratings entry from `GET /users/{userId}/ratings`. */
@Serializable
data class RatingWithRaterJson(
    val id: Int,
    @SerialName("booking_id") val bookingId: Int? = null,
    @SerialName("rater_id") val raterId: Int? = null,
    @SerialName("rated_user_id") val ratedUserId: Int? = null,
    val rating: Int = 0,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val rater: RatingUserInfoJson? = null,
)

/** Given-ratings entry from `GET /ratings/given`. */
@Serializable
data class RatingWithRatedJson(
    val id: Int,
    @SerialName("booking_id") val bookingId: Int? = null,
    @SerialName("rater_id") val raterId: Int? = null,
    @SerialName("rated_user_id") val ratedUserId: Int? = null,
    val rating: Int = 0,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val rated: RatingUserInfoJson? = null,
)

@Serializable
data class PaginatedRatingsJson(
    @SerialName("current_page") val currentPage: Int = 1,
    val data: List<RatingWithRaterJson> = emptyList(),
    @SerialName("per_page") val perPage: Int = 15,
    val total: Int = 0,
    @SerialName("last_page") val lastPage: Int = 1,
)

@Serializable
data class UserRatingSummaryJson(
    val id: Int,
    val name: String,
    val rating: String? = null,
    @SerialName("total_ratings") val totalRatings: Int = 0,
)

@Serializable
data class UserReceivedRatingsDataJson(
    val user: UserRatingSummaryJson? = null,
    val ratings: PaginatedRatingsJson = PaginatedRatingsJson(),
)

@Serializable
data class UserReceivedRatingsResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: UserReceivedRatingsDataJson? = null,
)

@Serializable
data class UserGivenRatingsResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: List<RatingWithRatedJson> = emptyList(),
)

@Serializable
data class PendingReviewOtherUserJson(
    val id: Int,
    val name: String,
    val avatar: String? = null,
    val rating: String? = null,
    @SerialName("total_ratings") val totalRatings: Int = 0,
    @SerialName("verification_level") val verificationLevel: String? = null,
)

@Serializable
data class PendingReviewJson(
    @SerialName("match_id") val matchId: Int,
    @SerialName("other_user") val otherUser: PendingReviewOtherUserJson,
    @SerialName("delivery_date") val deliveryDate: String? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    val route: String? = null,
    @SerialName("days_since_delivery")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val daysSinceDelivery: Double? = null,
    @SerialName("can_rate") val canRate: Boolean = true,
    @SerialName("user_role") val userRole: String? = null,
    @SerialName("agreed_price") val agreedPrice: String? = null,
    @SerialName("transportation_method") val transportationMethod: String? = null,
)

@Serializable
data class PendingReviewsResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: List<PendingReviewJson> = emptyList(),
)

@Serializable
data class CommentUpdateRequestJson(
    @SerialName("review_text") val reviewText: String,
)

@Serializable
data class CommentUpdateResponseJson(
    val success: Boolean,
    val message: String? = null,
)
