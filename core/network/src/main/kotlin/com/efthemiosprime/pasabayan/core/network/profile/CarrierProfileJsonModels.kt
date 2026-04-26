package com.efthemiosprime.pasabayan.core.network.profile

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleNotNullSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class CarrierProfileResponseJson(
    val message: String = "",
    val data: CarrierProfileJson? = null,
)

@Serializable
data class AvailableRouteJson(
    @SerialName("from") val origin: String? = null,
    @SerialName("to") val destination: String? = null,
)

@Serializable
data class PreferredPickupCityJson(
    val id: Int = 0,
    val name: String = "",
    @SerialName("state_code") val stateCode: String = "",
    @Serializable(with = FlexibleDoubleSerializer::class) val lat: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val lng: Double? = null,
)

/**
 * Tolerant: [availableRoutes] may be an array, a string, or empty/missing.
 */
@Serializable
data class CarrierProfileJson(
    val id: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("max_weight_capacity_kg")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val maxWeightCapacityKg: Double? = null,
    @SerialName("max_space_capacity_liters")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val maxSpaceCapacityLiters: Double? = null,
    @SerialName("preferred_pickup_city_id") val preferredPickupCityId: Int? = null,
    @SerialName("preferred_pickup_city") val preferredPickupCity: PreferredPickupCityJson? = null,
    @SerialName("preferred_package_types") val preferredPackageTypes: List<String>? = null,
    @SerialName("restricted_items") val restrictedItems: List<String>? = null,
    @SerialName("default_price_per_kg")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val defaultPricePerKg: Double? = null,
    @SerialName("available_routes") @Serializable(with = AvailableRoutesJsonSerializer::class)
    val availableRoutes: List<AvailableRouteJson>? = null,
    @SerialName("carrier_status") val carrierStatus: String? = null,
    @SerialName("insurance_coverage_amount")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val insuranceCoverageAmount: Double? = null,
    @SerialName("government_id_verified") val governmentIdVerified: Boolean? = null,
    val bio: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val rating: Double? = null,
    @SerialName("total_trips") val totalTrips: Int? = null,
    @SerialName("total_earnings")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val totalEarnings: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("setup_required") val setupRequired: Boolean? = null,
    @SerialName("is_detailed_profile") val isDetailedProfile: Boolean? = null,
)

/**
 * POST/PUT [carrier/profile] per `09-profile-carrier-consent`.
 */
@Serializable
data class CreateCarrierProfileRequestJson(
    @SerialName("preferred_pickup_city_id") val preferredPickupCityId: Int? = null,
    @SerialName("max_weight_capacity_kg")
    @Serializable(with = FlexibleDoubleNotNullSerializer::class)
    val maxWeightCapacityKg: Double,
    @SerialName("max_space_capacity_liters")
    @Serializable(with = FlexibleDoubleNotNullSerializer::class)
    val maxSpaceCapacityLiters: Double = 0.0,
    @SerialName("preferred_package_types") val preferredPackageTypes: List<String>? = null,
    @SerialName("restricted_items") val restrictedItems: List<String>? = null,
    @SerialName("default_price_per_kg")
    @Serializable(with = FlexibleDoubleNotNullSerializer::class)
    val defaultPricePerKg: Double,
    @SerialName("available_routes") @Serializable(with = AvailableRoutesJsonSerializer::class)
    val availableRoutes: List<AvailableRouteJson>? = null,
    @SerialName("insurance_coverage_amount")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val insuranceCoverageAmount: Double? = null,
    val bio: String? = null,
)

/** Decodes `available_routes` as a JSON array of `{from,to}` or a comma/arrow string fallback. */
object AvailableRoutesJsonSerializer :
    kotlinx.serialization.KSerializer<List<AvailableRouteJson>?> {

    override val descriptor =
        kotlinx.serialization.descriptors.buildClassSerialDescriptor("AvailableRoutesTolerant")

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): List<AvailableRouteJson>? {
        val jsonDecoder = decoder as? kotlinx.serialization.json.JsonDecoder ?: return null
        return when (val el = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> el.flatMap { item ->
                when (item) {
                    is JsonObject -> {
                        val o = item
                        val from = o["from"]?.jsonPrimitive?.content
                        val to = o["to"]?.jsonPrimitive?.content
                        if (from == null && to == null) emptyList()
                        else listOf(AvailableRouteJson(origin = from, destination = to))
                    }
                    is JsonPrimitive -> parseRouteString(item.content)
                    else -> emptyList()
                }
            }
            is JsonPrimitive -> parseRouteString(el.content)
            else -> null
        }
    }

    private fun parseRouteString(s: String): List<AvailableRouteJson> {
        val trimmed = s.trim()
        if (trimmed.isEmpty()) return emptyList()
        for (sep in listOf("→", "->", "—", ",")) {
            if (trimmed.contains(sep)) {
                val p = trimmed.split(sep, limit = 2).map { it.trim() }
                if (p.size == 2) {
                    return listOf(AvailableRouteJson(origin = p[0], destination = p[1]))
                }
            }
        }
        return emptyList()
    }

    override fun serialize(
        encoder: kotlinx.serialization.encoding.Encoder,
        value: List<AvailableRouteJson>?,
    ) {
        if (value == null) {
            (encoder as? kotlinx.serialization.json.JsonEncoder)
                ?.encodeJsonElement(JsonArray(emptyList())) ?: return
        } else {
            kotlinx.serialization.builtins.ListSerializer(AvailableRouteJson.serializer())
            encoder.encodeSerializableValue(
                kotlinx.serialization.builtins.ListSerializer(AvailableRouteJson.serializer()),
                value,
            )
        }
    }
}
