package com.efthemiosprime.pasabayan.core.domain.error

/**
 * Parity with iOS `Pasabayan/Utilities/ErrorAlertPolicy.swift` (`ErrorAlertContext`):
 * whether a [DomainError] should surface in UI for a given context.
 *
 * - **ForegroundAction:** user-initiated call — always show (matches iOS `foregroundAction`).
 * - **BackgroundRefresh:** silent refresh / sync — only “actionable” auth/payment/consent errors surface.
 */
enum class ErrorAlertContext {
    ForegroundAction,
    BackgroundRefresh,
}

object ErrorAlertPolicy {

    fun shouldPresentToUser(error: DomainError, context: ErrorAlertContext): Boolean {
        return when (context) {
            ErrorAlertContext.ForegroundAction -> true
            ErrorAlertContext.BackgroundRefresh -> backgroundShouldPresent(error)
        }
    }

    private fun backgroundShouldPresent(error: DomainError): Boolean = when (error) {
        DomainError.Unauthorized,
        DomainError.AuthenticationError,
        DomainError.Unauthenticated,
        is DomainError.PaymentRequired,
        is DomainError.CarrierOnboardingRequired,
        is DomainError.ConsentRequired,
        -> true
        DomainError.InvalidUrl,
        is DomainError.NetworkError,
        DomainError.DecodingError,
        DomainError.EncodingError,
        is DomainError.ServerError,
        is DomainError.HttpError,
        DomainError.InvalidResponse,
        DomainError.NotFound,
        is DomainError.TripNotFound,
        is DomainError.Conflict,
        is DomainError.TripOvercommitted,
        is DomainError.TripHasBlockingMatch,
        is DomainError.CapacityAcknowledgmentRequired,
        is DomainError.RateLimited,
        DomainError.UserNotCarrier,
        DomainError.MixedTransportTypes,
        DomainError.NoTransportTypeSpecified,
        DomainError.Unknown,
        is DomainError.ValidationError,
        -> false
    }
}
