package com.efthemiosprime.pasabayan.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/** Mirrors iOS `APIErrorResponse`. */
@Serializable
data class ApiErrorResponseDto(
    val message: String = "",
    val errors: Map<String, JsonElement>? = null,
)

/** Mirrors iOS `ValidationErrorResponse` (`errors` required there; default for lenient decode). */
@Serializable
data class ValidationErrorResponseDto(
    val message: String = "",
    val errors: Map<String, JsonElement> = emptyMap(),
)

fun ValidationErrorResponseDto.toFieldErrorsMap(): Map<String, List<String>> =
    errors.mapValues { (_, el) ->
        when (el) {
            is JsonPrimitive -> listOfNotNull(el.contentOrNull)
            is JsonArray -> el.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            else -> emptyList()
        }
    }.filterValues { it.isNotEmpty() }

@Serializable
data class PaymentRequiredBodyDto(
    val message: String? = null,
    val error: String? = null,
)

@Serializable
data class GenericJsonObjectDto(
    val message: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("carrier_onboarding_required") val carrierOnboardingRequired: Boolean? = null,
)

/** Best-effort parse when structure is loosely typed. */
fun JsonObject.booleanAt(key: String): Boolean? =
    (get(key) as? JsonPrimitive)?.booleanOrNull

fun JsonObject.stringAt(key: String): String? =
    (get(key) as? JsonPrimitive)?.contentOrNull
