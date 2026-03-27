package com.efthemiosprime.pasabayan.core.domain.error

/**
 * Domain-level errors aligned with the API contract (parity with iOS APIError).
 * Map HTTP/transport failures to these at the repository boundary.
 */
sealed class DomainError {
    data object InvalidUrl : DomainError()

    data class NetworkError(val cause: Throwable?) : DomainError()

    data object DecodingError : DomainError()

    data object EncodingError : DomainError()

    data class ServerError(val message: String?) : DomainError()

    data class PaymentRequired(val message: String?) : DomainError()

    data object Unauthorized : DomainError()

    data object AuthenticationError : DomainError()

    data class HttpError(val statusCode: Int) : DomainError()

    data object InvalidResponse : DomainError()

    data object NotFound : DomainError()

    data class TripNotFound(val message: String?, val tripId: String?) : DomainError()

    data class Conflict(val message: String?, val expiresAt: String?) : DomainError()

    data class RateLimited(val retryAfterSeconds: Long?) : DomainError()

    data class CarrierOnboardingRequired(val message: String?) : DomainError()

    data class ConsentRequired(val purpose: String?, val message: String?) : DomainError()

    data object Unauthenticated : DomainError()

    data object UserNotCarrier : DomainError()

    data object MixedTransportTypes : DomainError()

    data object NoTransportTypeSpecified : DomainError()

    data object Unknown : DomainError()
}
