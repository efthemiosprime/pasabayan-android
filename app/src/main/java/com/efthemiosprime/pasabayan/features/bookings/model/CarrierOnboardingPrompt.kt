package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Structured prompt shown to a carrier when the server responds with
 * `carrier_onboarding_required: true` (HTTP 422) on a match-confirm or
 * accept-shipper-request call. Mirrors iOS `CarrierOnboardingPrompt.swift`.
 *
 * Carries the server message *and* the originating action so the UI can offer
 * a "Complete Payout Setup" CTA and retry the original call once Stripe
 * onboarding finishes — instead of swallowing the failure into a generic alert.
 */
data class CarrierOnboardingPrompt(
    val message: String,
    val action: Action,
) {
    /**
     * Action that triggered the onboarding-required response. The view layer
     * uses this to retry the right API call after the carrier finishes onboarding.
     */
    sealed interface Action {
        val matchId: Int

        data class ConfirmMatch(override val matchId: Int) : Action

        data class AcceptShipperRequest(
            override val matchId: Int,
            val acknowledgeOverage: Boolean? = null,
        ) : Action
    }

    /** Stable identifier for state keys / preview hosts. Mirrors iOS' `Identifiable.id`. */
    val id: String
        get() = when (val a = action) {
            is Action.ConfirmMatch -> "confirmMatch-${a.matchId}"
            is Action.AcceptShipperRequest -> "acceptShipperRequest-${a.matchId}"
        }
}
