package com.efthemiosprime.pasabayan.core.domain.error

/**
 * English fallback for JVM / non-UI use. **Android UI** should resolve copy with string resources
 * (e.g. `DomainError.localizedMessage` in the app module) so `en` / `fr` match [android-spec/18-localization.md].
 * Aligned with iOS `APIError.userFriendlyMessage` semantics.
 */
fun DomainError.userMessage(): String = when (this) {
    is DomainError.InvalidUrl -> "Invalid request URL"
    is DomainError.NetworkError -> {
        val d = cause?.localizedMessage?.takeIf { it.isNotBlank() }
        if (d != null) "Network error: $d" else "Network error"
    }
    is DomainError.DecodingError -> "Unable to process server response"
    is DomainError.EncodingError -> "Unable to encode request"
    is DomainError.ServerError -> "Server error: ${message ?: "Unknown"}"
    is DomainError.PaymentRequired -> message ?: "Payment required"
    is DomainError.Unauthorized -> "Unauthorized - Please log in again"
    is DomainError.AuthenticationError -> "Authentication failed - Please log in again"
    is DomainError.HttpError -> "HTTP error (Code: $statusCode)"
    is DomainError.InvalidResponse -> "Invalid server response"
    is DomainError.NotFound -> "Resource not found"
    is DomainError.TripNotFound -> message ?: "Trip not found"
    is DomainError.Unauthenticated -> "Please sign in to continue"
    is DomainError.UserNotCarrier -> "You need to register as a carrier to perform this action"
    is DomainError.ValidationError -> formattedValidationMessage()
    is DomainError.MixedTransportTypes -> "Trip must be either cargo-only or passenger-only, not both"
    is DomainError.NoTransportTypeSpecified -> "Trip must specify either cargo transport or passenger transport"
    is DomainError.Conflict -> message ?: "This action cannot be completed"
    is DomainError.RateLimited -> "Too many requests. Please wait a moment and try again."
    is DomainError.CarrierOnboardingRequired -> message ?: "Carrier onboarding required"
    is DomainError.ConsentRequired -> message?.takeIf { it.isNotBlank() }
        ?: "Additional consent is required to continue"
    is DomainError.Unknown -> "An unknown error occurred"
}

private fun DomainError.ValidationError.formattedValidationMessage(): String {
    if (fieldErrors.isEmpty()) return message
    val lines = fieldErrors.map { (field, msgs) ->
        val label = field.replace('_', ' ').split(' ').filter { it.isNotEmpty() }
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercaseChar() } }
        "$label: ${msgs.joinToString(", ")}"
    }
    return if (lines.isEmpty()) message else "$message\n\n${lines.joinToString("\n")}"
}
