package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson

/** iOS parity: computed flags on `StripeConnectStatus`. */
val StripeConnectStatusJson.isOnboarded: Boolean
    get() = onboardingComplete

val StripeConnectStatusJson.canPayout: Boolean
    get() = payoutsEnabled && canReceivePayouts

val StripeConnectStatusJson.hasAccount: Boolean
    get() = hasStripeAccount
