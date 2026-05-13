package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson

/**
 * Maps the wire-level [StripeConnectStatusJson] into the domain [StripeConnectStatus].
 * iOS-parity computeds (`isOnboarded`, `hasAccount`, `canPayout`) live on the domain class.
 */
fun StripeConnectStatusJson.toDomain(): StripeConnectStatus = StripeConnectStatus(
    hasStripeAccount = hasStripeAccount,
    stripeAccountId = stripeAccountId,
    onboardingComplete = onboardingComplete,
    onboardedAt = onboardedAt,
    chargesEnabled = chargesEnabled,
    payoutsEnabled = payoutsEnabled,
    canReceivePayouts = canReceivePayouts,
)
