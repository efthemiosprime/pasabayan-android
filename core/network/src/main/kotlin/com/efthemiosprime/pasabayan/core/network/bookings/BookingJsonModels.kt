package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.BookingStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingJson(
    val id: Int,
    @SerialName("package_request_id") val packageRequestId: Int? = null,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("carrier_id") val carrierId: Int? = null,
    @SerialName("shipper_id") val shipperId: Int? = null,
    @SerialName("pickup_location") val pickupLocation: String? = null,
    @SerialName("delivery_location") val deliveryLocation: String? = null,
    @SerialName("scheduled_pickup_date") val scheduledPickupDate: String? = null,
    @SerialName("scheduled_pickup_time") val scheduledPickupTime: String? = null,
    @SerialName("actual_pickup_time") val actualPickupTime: String? = null,
    @SerialName("estimated_delivery_time") val estimatedDeliveryTime: String? = null,
    @SerialName("actual_delivery_time") val actualDeliveryTime: String? = null,
    @SerialName("agreed_price") @Serializable(with = FlexibleDoubleSerializer::class) val agreedPrice: Double? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val notes: String? = null,
    @SerialName("tracking_number") val trackingNumber: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("auto_cancel_after_days") val autoCancelAfterDays: Int? = null,
    @SerialName("auto_cancelled_at") val autoCancelledAt: String? = null,
    @SerialName("auto_cancelled") @Serializable(with = FlexibleBoolSerializer::class) val autoCancelled: Boolean? = null,
    @SerialName("platform_fee_percent") val platformFeePercent: Int? = null,
    @SerialName("raw_match_status") val rawMatchStatus: String? = null,
    @SerialName("chat_conversation_id") val chatConversationId: Int? = null,
    val carrier: UserSummary? = null,
    val shipper: UserSummary? = null,
    // Counter-offer fields
    @SerialName("is_counter_offer") @Serializable(with = FlexibleBoolSerializer::class) val isCounterOffer: Boolean? = null,
    @SerialName("original_price") @Serializable(with = FlexibleDoubleSerializer::class) val originalPrice: Double? = null,
    @SerialName("counter_offerer_id") val counterOffererId: Int? = null,
    @SerialName("counter_offerer_name") val counterOffererName: String? = null,
    @SerialName("counter_offer_round") val counterOfferRound: Int? = null,
    @SerialName("remaining_counter_offers") val remainingCounterOffers: Int? = null,
    @SerialName("can_counter_offer") @Serializable(with = FlexibleBoolSerializer::class) val canCounterOffer: Boolean? = null,
    @SerialName("initiated_by") val initiatedBy: String? = null,
    // Delivery codes
    @SerialName("delivery_verification_code") val deliveryVerificationCode: String? = null,
    @SerialName("delivery_code_expires_at") val deliveryCodeExpiresAt: String? = null,
)

@Serializable
data class CreateBookingRequestJson(
    @SerialName("trip_id") val tripId: Int,
    @SerialName("package_request_id") val packageRequestId: Int,
    @SerialName("pickup_date") val pickupDate: String? = null,
    val notes: String? = null,
)

@Serializable
data class BookingResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: BookingJson? = null,
)
