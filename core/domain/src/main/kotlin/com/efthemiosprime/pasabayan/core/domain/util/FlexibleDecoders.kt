package com.efthemiosprime.pasabayan.core.domain.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Custom kotlinx.serialization deserializers for backend fields that may
 * arrive as Double, Int, or String interchangeably.
 *
 * Mirrors iOS flexible decoding helpers in Trip.swift / PackageRequest.swift.
 */
@OptIn(ExperimentalSerializationApi::class)
object FlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive) return null
        return element.doubleOrNull
            ?: element.intOrNull?.toDouble()
            ?: element.content.toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value != null) encoder.encodeDouble(value) else encoder.encodeNull()
    }
}

@OptIn(ExperimentalSerializationApi::class)
object FlexibleBoolSerializer : KSerializer<Boolean?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBool", PrimitiveKind.STRING)

    private val truthyStrings = setOf("true", "yes", "1")
    private val falsyStrings = setOf("false", "no", "0")

    override fun deserialize(decoder: Decoder): Boolean? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive) return null
        element.booleanOrNull?.let { return it }
        element.intOrNull?.let { return it != 0 }
        val str = element.content.lowercase().trim()
        return when (str) {
            in truthyStrings -> true
            in falsyStrings -> false
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean?) {
        if (value != null) encoder.encodeBoolean(value) else encoder.encodeNull()
    }
}

@OptIn(ExperimentalSerializationApi::class)
object FlexibleStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive) return null
        val content = element.content
        return content.ifBlank { null }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }
}

/**
 * Non-nullable variant — decodes to 0.0 when value is null or unparseable.
 */
object FlexibleDoubleNotNullSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDoubleNotNull", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Double {
        return FlexibleDoubleSerializer.deserialize(decoder) ?: 0.0
    }

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }
}

/**
 * Non-nullable variant — decodes to false when value is null or unparseable.
 */
object FlexibleBoolNotNullSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBoolNotNull", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        return FlexibleBoolSerializer.deserialize(decoder) ?: false
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}
