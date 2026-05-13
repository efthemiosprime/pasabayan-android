package com.efthemiosprime.pasabayan.core.network

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.dto.ApiErrorResponseDto
import com.efthemiosprime.pasabayan.core.network.dto.GenericJsonObjectDto
import com.efthemiosprime.pasabayan.core.network.dto.PaymentRequiredBodyDto
import com.efthemiosprime.pasabayan.core.network.dto.ValidationErrorResponseDto
import com.efthemiosprime.pasabayan.core.network.dto.booleanAt
import com.efthemiosprime.pasabayan.core.network.dto.doubleAt
import com.efthemiosprime.pasabayan.core.network.dto.stringAt
import com.efthemiosprime.pasabayan.core.network.dto.toFieldErrorsMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Maps HTTP status + JSON error body to [DomainError], following iOS `APIService.makeRequest` rules
 * in `android-spec/01-error-taxonomy.md`.
 */
object ApiErrorMapper {

    fun map(statusCode: Int, body: ByteArray?, json: Json): DomainError {
        val raw = body?.decodeToString() ?: ""
        return when (statusCode) {
            401 -> DomainError.Unauthorized
            404 -> map404(raw, json)
            402 -> map402(raw, json)
            409 -> map409(raw, json)
            400 -> map400(raw, json)
            422 -> map422(raw, json)
            429 -> DomainError.RateLimited(null)
            in 403..499 -> mapClientOther(statusCode, raw, json)
            in 500..599 -> map5xx(raw, json)
            else -> DomainError.Unknown
        }
    }

    private fun map404(raw: String, json: Json): DomainError {
        if (raw.isBlank()) return DomainError.NotFound
        return try {
            val dto = json.decodeFromString(ApiErrorResponseDto.serializer(), raw)
            val normalized = dto.message.lowercase()
            if (normalized.contains("trip") || normalized.contains("carriertrip")) {
                DomainError.TripNotFound(message = dto.message, tripId = null)
            } else {
                DomainError.NotFound
            }
        } catch (_: Exception) {
            DomainError.NotFound
        }
    }

    private fun map402(raw: String, json: Json): DomainError {
        if (raw.isBlank()) return DomainError.ServerError("Payment required")
        return try {
            val dto = json.decodeFromString(PaymentRequiredBodyDto.serializer(), raw)
            if (dto.error == "payment_required") {
                DomainError.PaymentRequired(dto.message ?: "Payment required")
            } else {
                DomainError.ServerError(dto.message ?: "Payment required")
            }
        } catch (_: Exception) {
            DomainError.ServerError("Payment required")
        }
    }

    private fun map409(raw: String, json: Json): DomainError {
        if (raw.isBlank()) {
            return DomainError.Conflict(message = "This action cannot be completed", expiresAt = null)
        }
        return try {
            val dto = json.decodeFromString(GenericJsonObjectDto.serializer(), raw)
            val msg = dto.message
            when {
                msg != null && isTripOvercommittedMessage(msg) -> DomainError.TripOvercommitted(msg)
                else -> DomainError.Conflict(
                    message = msg ?: "This action cannot be completed",
                    expiresAt = dto.expiresAt,
                )
            }
        } catch (_: Exception) {
            DomainError.Conflict(message = "This action cannot be completed", expiresAt = null)
        }
    }

    private fun isTripOvercommittedMessage(msg: String): Boolean {
        val n = msg.lowercase()
        return n.contains("fully booked") || n.contains("no remaining capacity")
    }

    private fun map400(raw: String, json: Json): DomainError {
        val validation = tryDecodeValidation(raw, json)
        if (validation != null) return validation
        return genericMessageOrServer(raw, json, "The request was invalid. Please check your input.")
    }

    private fun map422(raw: String, json: Json): DomainError {
        if (raw.isNotBlank()) {
            try {
                val root = json.parseToJsonElement(raw).jsonObject
                if (root.stringAt("error") == "capacity_acknowledgment_required") {
                    val compat = root["compatibility"] as? JsonObject
                    return DomainError.CapacityAcknowledgmentRequired(
                        message = root.stringAt("message"),
                        packageWeightKg = compat?.doubleAt("package_weight_kg"),
                        tripAvailableWeightKg = compat?.doubleAt("trip_available_weight_kg"),
                        overageKg = compat?.doubleAt("overage_kg"),
                    )
                }
                if (root.booleanAt("carrier_onboarding_required") == true) {
                    val msg = root.stringAt("message") ?: "Carrier onboarding required"
                    return DomainError.CarrierOnboardingRequired(msg)
                }
            } catch (_: Exception) { /* fall through */ }
        }
        val validation = tryDecodeValidation(raw, json)
        if (validation != null) return validation
        return genericMessageOrServer(raw, json, "Validation error")
    }

    private fun tryDecodeValidation(raw: String, json: Json): DomainError? {
        if (raw.isBlank()) return null
        return try {
            val dto = json.decodeFromString(ValidationErrorResponseDto.serializer(), raw)
            val fields = dto.toFieldErrorsMap()
            if (fields.isNotEmpty() || dto.message.isNotBlank()) {
                DomainError.ValidationError(message = dto.message.ifBlank { "Validation error" }, fieldErrors = fields)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun mapClientOther(statusCode: Int, raw: String, json: Json): DomainError {
        val msg = extractMessage(raw, json) ?: "Client error occurred"
        return DomainError.ServerError(msg)
    }

    private fun map5xx(raw: String, json: Json): DomainError {
        val msg = extractMessage(raw, json) ?: "Internal server error"
        return DomainError.ServerError(msg)
    }

    private fun genericMessageOrServer(raw: String, json: Json, fallback: String): DomainError {
        val msg = extractMessage(raw, json) ?: fallback
        return DomainError.ServerError(msg)
    }

    private fun extractMessage(raw: String, json: Json): String? {
        if (raw.isBlank()) return null
        return try {
            val dto = json.decodeFromString(ApiErrorResponseDto.serializer(), raw)
            dto.message.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            try {
                val root = json.parseToJsonElement(raw)
                if (root is JsonObject) root.stringAt("message") else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
