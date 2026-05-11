package com.efthemiosprime.pasabayan.core.network.shipper

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

@Serializable
data class NearbyCarrierJson(
    val id: Int = 0,
    val name: String = "",
    val avatar: String? = null,
    @SerialName("completed_deliveries") val completedDeliveries: Int = 0,
    @SerialName("distance_km") val distanceKm: Double? = null,
)

@Serializable
data class NearbyCarriersPayloadJson(
    @SerialName("home_city_id") val homeCityId: Int? = null,
    @SerialName("home_city_name") val homeCityName: String? = null,
    @SerialName("radius_km") val radiusKm: Double? = null,
    val carriers: List<NearbyCarrierJson> = emptyList(),
)

/**
 * iOS parity: `NearbyCarriersResponse`. The backend returns one of:
 *   - object: `{ "success": true, "message": "...", "data": {payload} }`
 *   - empty array (when home city is not set): `{ "success": true, "data": [] }`
 *   - null (rare; treated as missing payload)
 *
 * The custom serializer collapses all variants into a nullable [NearbyCarriersPayloadJson].
 */
@Serializable(with = NearbyCarriersResponseSerializer::class)
data class NearbyCarriersResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: NearbyCarriersPayloadJson? = null,
)

private object NearbyCarriersResponseSerializer : KSerializer<NearbyCarriersResponseJson> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("NearbyCarriersResponseJson")

    private val payloadSerializer = NearbyCarriersPayloadJson.serializer()
    private val arraySerializer = ListSerializer(NearbyCarrierJson.serializer())

    override fun deserialize(decoder: Decoder): NearbyCarriersResponseJson {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("NearbyCarriersResponseJson requires the kotlinx JSON decoder")
        val root = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: return NearbyCarriersResponseJson(success = false)

        val success = (root["success"]?.toString() ?: "false")
            .trim('"').equals("true", ignoreCase = true)
        val message = root["message"]?.let { el ->
            if (el is kotlinx.serialization.json.JsonPrimitive && el.isString) el.content else el.toString().trim('"')
        }

        val data = when (val rawData = root["data"]) {
            null -> null
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(payloadSerializer, rawData)
            is JsonArray -> {
                // iOS treats the array-shape (no home city set) as an empty carriers payload
                val carriers = jsonDecoder.json.decodeFromJsonElement(arraySerializer, rawData)
                NearbyCarriersPayloadJson(carriers = carriers)
            }
            else -> null
        }

        return NearbyCarriersResponseJson(success = success, message = message, data = data)
    }

    override fun serialize(encoder: Encoder, value: NearbyCarriersResponseJson) {
        error("NearbyCarriersResponseJson is decode-only")
    }
}
