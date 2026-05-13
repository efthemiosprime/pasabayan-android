package com.efthemiosprime.pasabayan.features.payments.model

/**
 * Domain model for Stripe Connect onboarding state. Mirrors iOS `StripeConnectStatus` and
 * replaces the previous practice of leaking `StripeConnectStatusJson` directly into VMs / UI.
 */
data class StripeConnectStatus(
    val hasStripeAccount: Boolean = false,
    val stripeAccountId: String? = null,
    val onboardingComplete: Boolean = false,
    val onboardedAt: String? = null,
    val chargesEnabled: Boolean = false,
    val payoutsEnabled: Boolean = false,
    val canReceivePayouts: Boolean = false,
) {
    val isOnboarded: Boolean get() = onboardingComplete
    val hasAccount: Boolean get() = hasStripeAccount
    val canPayout: Boolean get() = payoutsEnabled && canReceivePayouts
}
