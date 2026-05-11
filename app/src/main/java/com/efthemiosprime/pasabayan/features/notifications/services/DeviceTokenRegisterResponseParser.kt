package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.network.notifications.DeviceTokenResponseJson
import kotlinx.serialization.json.Json

/**
 * Pure parser for `POST /device-tokens` — testable without OkHttp / Retrofit. Mirrors iOS
 * `DeviceTokenRegisterResponseParser.parse(data:statusCode:)`.
 */
object DeviceTokenRegisterResponseParser {

    fun parse(statusCode: Int, body: String, json: Json): DeviceTokenRegisterResult = when (statusCode) {
        200, 201 -> parseSuccess(body, json)
        403 -> parseForbidden(body, json)
        else -> parseGenericError(statusCode, body, json)
    }

    private fun parseSuccess(body: String, json: Json): DeviceTokenRegisterResult {
        val decoded = runCatching {
            json.decodeFromString(DeviceTokenResponseJson.serializer(), body)
        }.getOrElse { return DeviceTokenRegisterResult.DecodingError(it) }

        if (!decoded.success) {
            return DeviceTokenRegisterResult.ServerError(decoded.message)
        }
        val info = decoded.data?.deviceToken
            ?: return DeviceTokenRegisterResult.DecodingError(
                IllegalStateException("Missing data.device_token in success response"),
            )
        return DeviceTokenRegisterResult.Registered(info)
    }

    private fun parseForbidden(body: String, json: Json): DeviceTokenRegisterResult {
        val decoded = runCatching {
            json.decodeFromString(DeviceTokenResponseJson.serializer(), body)
        }.getOrNull()
        val purpose = decoded?.consentRequired?.takeIf { it.isNotBlank() }
        if (purpose != null) {
            return DeviceTokenRegisterResult.ConsentRequired(
                purpose = purpose,
                message = decoded.message,
            )
        }
        return DeviceTokenRegisterResult.ServerError(decoded?.message ?: "Forbidden")
    }

    private fun parseGenericError(statusCode: Int, body: String, json: Json): DeviceTokenRegisterResult {
        val decoded = runCatching {
            json.decodeFromString(DeviceTokenResponseJson.serializer(), body)
        }.getOrNull()
        return if (decoded?.message != null) {
            DeviceTokenRegisterResult.ServerError(decoded.message)
        } else {
            DeviceTokenRegisterResult.HttpError(statusCode)
        }
    }
}
