package com.efthemiosprime.pasabayan.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Package dimensions with flexible deserialization.
 *
 * Backend sends as either a JSON object `{"length":10,"width":5,"height":3}`
 * or a stringified JSON `"{\"length\":10,\"width\":5,\"height\":3}"`.
 * Each dimension field may be Double, Int, or String.
 */
@Serializable(with = PackageDimensionsSerializer::class)
data class PackageDimensions(
    val length: Double,
    val width: Double,
    val height: Double,
) {
    val formatted: String
        get() = "${length}×${width}×${height} cm"
}

object PackageDimensionsSerializer : KSerializer<PackageDimensions> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PackageDimensions") {
        element<Double>("length")
        element<Double>("width")
        element<Double>("height")
    }

    override fun deserialize(decoder: Decoder): PackageDimensions {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("PackageDimensions requires JSON decoder")
        val element = jsonDecoder.decodeJsonElement()

        val jsonObject = when (element) {
            is JsonObject -> element
            is JsonPrimitive -> {
                // Stringified JSON — parse the string content as JSON
                val content = element.content
                if (content.isBlank()) {
                    throw SerializationException("Empty PackageDimensions string")
                }
                jsonDecoder.json.parseToJsonElement(content).jsonObject
            }
            else -> throw SerializationException("Unexpected PackageDimensions format: $element")
        }

        return PackageDimensions(
            length = jsonObject.flexibleDouble("length"),
            width = jsonObject.flexibleDouble("width"),
            height = jsonObject.flexibleDouble("height"),
        )
    }

    override fun serialize(encoder: Encoder, value: PackageDimensions) {
        val jsonEncoder = encoder as? kotlinx.serialization.json.JsonEncoder
        if (jsonEncoder != null) {
            val obj = kotlinx.serialization.json.buildJsonObject {
                put("length", JsonPrimitive(value.length))
                put("width", JsonPrimitive(value.width))
                put("height", JsonPrimitive(value.height))
            }
            jsonEncoder.encodeJsonElement(obj)
        }
    }

    private fun JsonObject.flexibleDouble(key: String): Double {
        val prim = this[key]?.jsonPrimitive ?: return 0.0
        return prim.doubleOrNull
            ?: prim.content.toDoubleOrNull()
            ?: 0.0
    }
}
