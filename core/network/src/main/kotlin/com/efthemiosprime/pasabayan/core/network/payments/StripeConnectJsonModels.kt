package com.efthemiosprime.pasabayan.core.network.payments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeConnectStatusJson(
    @SerialName("has_stripe_account") val hasStripeAccount: Boolean = false,
    @SerialName("stripe_account_id") val stripeAccountId: String? = null,
    @SerialName("onboarding_complete") val onboardingComplete: Boolean = false,
    @SerialName("onboarded_at") val onboardedAt: String? = null,
    @SerialName("charges_enabled") val chargesEnabled: Boolean = false,
    @SerialName("payouts_enabled") val payoutsEnabled: Boolean = false,
    @SerialName("can_receive_payouts") val canReceivePayouts: Boolean = false,
)

@Serializable
data class StripeStatusResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: StripeConnectStatusJson? = null,
)

@Serializable
data class StripeOnboardingResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: OnboardingDataJson? = null,
)

@Serializable
data class OnboardingDataJson(
    @SerialName("onboarding_url") val onboardingUrl: String = "",
    @SerialName("stripe_account_id") val stripeAccountId: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class StripeDashboardResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: DashboardDataJson? = null,
)

@Serializable
data class DashboardDataJson(
    @SerialName("dashboard_url") val dashboardUrl: String = "",
)
