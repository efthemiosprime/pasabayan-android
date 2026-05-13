package com.efthemiosprime.pasabayan.features.payments.viewmodel

/**
 * Outcome of a Stripe PaymentSheet presentation, translated from the SDK's
 * `PaymentSheetResult` into a VM-friendly enum that can be persisted in state.
 */
enum class PaymentSheetResultKind {
    COMPLETED,
    CANCELED,
    FAILED,
}
