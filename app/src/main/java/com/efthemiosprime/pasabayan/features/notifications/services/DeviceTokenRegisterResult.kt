package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.network.notifications.DeviceTokenInfoJson

/**
 * Outcome of `POST /device-tokens`. Mirrors the cases handled by iOS
 * `DeviceTokenRegisterResponseParser`.
 */
sealed interface DeviceTokenRegisterResult {
    data class Registered(val info: DeviceTokenInfoJson) : DeviceTokenRegisterResult
    data class ConsentRequired(val purpose: String, val message: String?) : DeviceTokenRegisterResult
    data class ServerError(val message: String?) : DeviceTokenRegisterResult
    data class HttpError(val statusCode: Int) : DeviceTokenRegisterResult
    data class DecodingError(val cause: Throwable) : DeviceTokenRegisterResult
}
