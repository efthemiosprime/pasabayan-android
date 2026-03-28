package com.efthemiosprime.pasabayan.error

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.error.DomainError

/**
 * User-visible [DomainError] copy from string resources (en + fr per `values*`).
 * Non-Android / tests can keep using [com.efthemiosprime.pasabayan.core.domain.error.userMessage].
 */
fun DomainError.localizedMessage(context: Context): String {
    val c = context.applicationContext
    return when (this) {
        is DomainError.InvalidUrl -> c.getString(R.string.error_invalid_url)
        is DomainError.NetworkError -> {
            val d = cause?.localizedMessage?.takeIf { it.isNotBlank() }
            if (d != null) c.getString(R.string.error_network_with_detail, d)
            else c.getString(R.string.error_network)
        }
        is DomainError.DecodingError -> c.getString(R.string.error_decoding)
        is DomainError.EncodingError -> c.getString(R.string.error_encoding)
        is DomainError.ServerError -> {
            val m = message?.takeIf { it.isNotBlank() } ?: c.getString(R.string.error_unknown_placeholder)
            c.getString(R.string.error_server, m)
        }
        is DomainError.PaymentRequired -> {
            val m = message?.takeIf { it.isNotBlank() }
            if (m != null) c.getString(R.string.error_payment_required_message, m)
            else c.getString(R.string.error_payment_required)
        }
        is DomainError.Unauthorized -> c.getString(R.string.error_unauthorized)
        is DomainError.AuthenticationError -> c.getString(R.string.error_authentication)
        is DomainError.HttpError -> c.getString(R.string.error_http, statusCode)
        is DomainError.InvalidResponse -> c.getString(R.string.error_invalid_response)
        is DomainError.NotFound -> c.getString(R.string.error_not_found)
        is DomainError.TripNotFound -> {
            val m = message?.takeIf { it.isNotBlank() }
            if (m != null) c.getString(R.string.error_trip_not_found_message, m)
            else c.getString(R.string.error_trip_not_found)
        }
        is DomainError.Unauthenticated -> c.getString(R.string.error_unauthenticated)
        is DomainError.UserNotCarrier -> c.getString(R.string.error_user_not_carrier)
        is DomainError.ValidationError -> formatValidationError(this, c)
        is DomainError.MixedTransportTypes -> c.getString(R.string.error_mixed_transport)
        is DomainError.NoTransportTypeSpecified -> c.getString(R.string.error_no_transport_type)
        is DomainError.Conflict -> {
            val m = message?.takeIf { it.isNotBlank() }
            if (m != null) c.getString(R.string.error_conflict_message, m)
            else c.getString(R.string.error_conflict)
        }
        is DomainError.RateLimited -> c.getString(R.string.error_rate_limited)
        is DomainError.CarrierOnboardingRequired -> {
            val m = message?.takeIf { it.isNotBlank() }
            if (m != null) c.getString(R.string.error_carrier_onboarding_message, m)
            else c.getString(R.string.error_carrier_onboarding)
        }
        is DomainError.ConsentRequired -> {
            val m = message?.takeIf { it.isNotBlank() }
            if (m != null) c.getString(R.string.error_consent_message, m)
            else c.getString(R.string.error_consent_required)
        }
        is DomainError.Unknown -> c.getString(R.string.error_generic)
    }
}

private fun formatValidationError(error: DomainError.ValidationError, context: Context): String {
    if (error.fieldErrors.isEmpty()) return error.message
    val lines = error.fieldErrors.map { (field, msgs) ->
        val label = field.replace('_', ' ').split(' ').filter { it.isNotEmpty() }
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercaseChar() } }
        context.getString(R.string.error_validation_line, label, msgs.joinToString(", "))
    }
    val body = lines.joinToString("\n")
    return if (error.message.isBlank()) {
        body
    } else {
        context.getString(R.string.error_validation_with_lines, error.message, body)
    }
}
