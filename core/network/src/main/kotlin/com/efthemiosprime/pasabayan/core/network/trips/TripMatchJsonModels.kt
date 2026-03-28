package com.efthemiosprime.pasabayan.core.network.trips

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripMatchesResponseJson(
    val trip: TripJson? = null,
    val matches: List<TripMatchPackageJson> = emptyList(),
)

@Serializable
data class TripMatchPackageJson(
    val id: Int,
    @SerialName("match_status") val matchStatus: MatchStatus = MatchStatus.PENDING,
    @SerialName("agreed_price") @Serializable(with = FlexibleDoubleSerializer::class) val agreedPrice: Double? = null,
    @SerialName("package") val packageInfo: PackageSummaryJson? = null,
    val shipper: UserSummary? = null,
    @SerialName("chat_conversation_id") val chatConversationId: Int? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("picked_up_at") val pickedUpAt: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PackageSummaryJson(
    val id: Int = 0,
    val description: String? = null,
    @SerialName("package_weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val weightKg: Double? = null,
    @SerialName("package_dimensions") val dimensions: String? = null,
)

@Serializable
data class PendingTripRequestJson(
    val id: Int,
    @SerialName("shipper_id") val shipperId: Int = 0,
    @SerialName("shipper_name") val shipperName: String? = null,
    @SerialName("shipper_avatar") val shipperAvatar: String? = null,
    @SerialName("package_id") val packageId: Int? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("proposed_price") @Serializable(with = FlexibleDoubleSerializer::class) val proposedPrice: Double? = null,
    val message: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class TripEarningsBreakdownJson(
    @SerialName("delivered_amount") @Serializable(with = FlexibleDoubleSerializer::class) val deliveredAmount: Double? = null,
    @SerialName("delivered_currency") val deliveredCurrency: String? = null,
    @SerialName("pending_amount") @Serializable(with = FlexibleDoubleSerializer::class) val pendingAmount: Double? = null,
    @SerialName("pending_currency") val pendingCurrency: String? = null,
)

@Serializable
data class TripTemplateResponseJson(
    val message: String = "",
    val data: TripTemplateJson? = null,
)

@Serializable
data class TripTemplateJson(
    @SerialName("origin_city") val originCity: String = "",
    @SerialName("origin_country") val originCountry: String = "",
    @SerialName("destination_city") val destinationCity: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("suggested_departure_date") val suggestedDepartureDate: String? = null,
    @SerialName("suggested_arrival_date") val suggestedArrivalDate: String? = null,
    @SerialName("suggested_weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val suggestedWeightKg: Double? = null,
    @SerialName("suggested_space_liters") @Serializable(with = FlexibleDoubleSerializer::class) val suggestedSpaceLiters: Double? = null,
    @SerialName("package_details") val packageDetails: PackageTemplateDetailsJson? = null,
)

@Serializable
data class PackageTemplateDetailsJson(
    val id: Int = 0,
    val description: String = "",
    @SerialName("weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val weightKg: Double? = null,
    @SerialName("length_cm") @Serializable(with = FlexibleDoubleSerializer::class) val lengthCm: Double? = null,
    @SerialName("width_cm") @Serializable(with = FlexibleDoubleSerializer::class) val widthCm: Double? = null,
    @SerialName("height_cm") @Serializable(with = FlexibleDoubleSerializer::class) val heightCm: Double? = null,
    @SerialName("is_fragile") val isFragile: Boolean = false,
    @SerialName("urgency_level") val urgencyLevel: String? = null,
)
