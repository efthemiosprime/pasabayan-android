@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.efthemiosprime.pasabayan.core.network.trips

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class TripMatchesResponseJson(
    val trip: TripJson? = null,
    // iOS parity: `/trips/{id}/matches` returns the array under `packages`
    // (`TripMatchesResponse.swift:15`). `@JsonNames` keeps backwards-compat with
    // any path that still emits `matches`, so the progress widget renders
    // "X of Y delivered" instead of falling through to the Empty state.
    @JsonNames("packages")
    val matches: List<TripMatchPackageJson> = emptyList(),
)

@Serializable
data class TripMatchPackageJson(
    // iOS uses `match_id` (`TripMatchPackage.id` = `match_id`, `TripModels.swift:31`).
    // Accept both so legacy `id`-keyed fixtures keep decoding.
    @JsonNames("match_id")
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
    // iOS `TripPackageInfo` (`TripModels.swift:55`) sends weight under `weight_kg`.
    // `package_weight_kg` is the Android-internal alias; accept both so the
    // per-match card row renders weight against the iOS-parity backend.
    @SerialName("package_weight_kg")
    @JsonNames("weight_kg")
    @Serializable(with = FlexibleDoubleSerializer::class) val weightKg: Double? = null,
    @SerialName("package_dimensions") val dimensions: String? = null,
    // iOS parity (`TripPackageInfo`): the trip-matches endpoint hangs pickup/delivery cities,
    // fragility, and package type under the embedded `package` object. iOS keys are `pickup_city`,
    // `delivery_city`, `fragile`, `package_type` (`TripModels.swift`).
    @SerialName("pickup_city") val pickupCity: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    val fragile: Boolean? = null,
    @SerialName("package_type") val packageType: String? = null,
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
    @SerialName("delivered_count") val deliveredCount: Int? = null,
    @SerialName("pending_amount") @Serializable(with = FlexibleDoubleSerializer::class) val pendingAmount: Double? = null,
    @SerialName("pending_currency") val pendingCurrency: String? = null,
    @SerialName("pending_count") val pendingCount: Int? = null,
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
    // iOS parity (`PackageTemplateDetails.packageType`): the trip-template endpoint also returns
    // the package's classification so the create-from-package screen can render a "shippingbox"
    // chip alongside weight + urgency.
    @SerialName("package_type") val packageType: String? = null,
)
