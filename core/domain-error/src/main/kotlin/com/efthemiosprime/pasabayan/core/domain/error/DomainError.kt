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

    /**
     * HTTP 409 returned when the carrier's trip has been clamped to zero
     * remaining capacity after a successful over-capacity accept. Surface
     * a distinct "trip is full" UI rather than the generic conflict copy.
     */
    data class TripOvercommitted(val message: String?) : DomainError()

    /**
     * HTTP 422 with `error: "capacity_acknowledgment_required"` — server-side
     * fallback when the accept call didn't carry `acknowledge_overage: true`
     * on an over-capacity match. UI should re-surface the same confirmation
     * sheet as the local pre-flight check and retry on confirm.
     */
    data class CapacityAcknowledgmentRequired(
        val message: String?,
        val packageWeightKg: Double?,
        val tripAvailableWeightKg: Double?,
        val overageKg: Double?,
    ) : DomainError()

    data class RateLimited(val retryAfterSeconds: Long?) : DomainError()

    data class CarrierOnboardingRequired(val message: String?) : DomainError()

    data class ConsentRequired(val purpose: String?, val message: String?) : DomainError()

    data object Unauthenticated : DomainError()

    data object UserNotCarrier : DomainError()

    data object MixedTransportTypes : DomainError()

    data object NoTransportTypeSpecified : DomainError()

    data object Unknown : DomainError()

    /** HTTP 400/422 + Laravel-style `errors` map (parity with iOS `ValidationErrorResponse`). */
    data class ValidationError(
        val message: String,
        val fieldErrors: Map<String, List<String>>,
    ) : DomainError()
}
