package com.efthemiosprime.pasabayan.features.bookings.viewmodel

/**
 * State machine for the "Confirm & Pay" sheet a shipper sees when confirming
 * a `MatchStatus.PENDING` match. Parity with iOS
 * `AutoChargeConfirmationViewModel.ConfirmationState`.
 *
 * Transitions:
 * - `Idle` → `CheckingPaymentMethod` (on `prepareConfirmation`)
 * - `CheckingPaymentMethod` → `ReadyToConfirm` (has default PM) or
 *   `NeedsPaymentMethod` (no default PM) or `Error`
 * - `NeedsPaymentMethod` / `NeedsPaymentMethodAfterConfirm` → `AddingPaymentMethod`
 *   (on user tap; UI hosts Stripe PaymentSheet)
 * - `AddingPaymentMethod` → back to `CheckingPaymentMethod` on success
 *   (which then resolves to ReadyToConfirm or, when post-confirm, retries
 *   auto-charge) or returns to its prior `NeedsPaymentMethod*` on cancel
 * - `ReadyToConfirm` → `Confirming` → `Success` |
 *   `NeedsPaymentMethodAfterConfirm` (match confirmed but charge couldn't
 *   queue) | `Error`
 */
sealed interface AutoChargeConfirmationState {
    data object Idle : AutoChargeConfirmationState

    data object CheckingPaymentMethod : AutoChargeConfirmationState

    /** Pre-confirm: shipper hasn't added a card yet. */
    data class NeedsPaymentMethod(val price: Double) : AutoChargeConfirmationState

    /** Default card on file — show price + last-4 and a single "Confirm & Pay" CTA. */
    data class ReadyToConfirm(val price: Double, val cardLast4: String?) : AutoChargeConfirmationState

    /** User tapped "Add Payment Method"; UI is hosting Stripe PaymentSheet. */
    data class AddingPaymentMethod(val afterConfirm: Boolean) : AutoChargeConfirmationState

    /** API call to `PUT /matches/{id}/confirm` in flight. */
    data object Confirming : AutoChargeConfirmationState

    /**
     * Match confirmed but server reported no default payment method, so
     * the charge wasn't queued. UI prompts to add a card and retries via
     * `POST /payments/matches/{id}/auto-charge` on success.
     */
    data object NeedsPaymentMethodAfterConfirm : AutoChargeConfirmationState

    /** Terminal happy state. Includes whether the auto-charge actually queued. */
    data class Success(val autoChargeQueued: Boolean) : AutoChargeConfirmationState

    /** Recoverable error; UI surfaces "Try Again" leading back to ReadyToConfirm. */
    data class Error(val message: String) : AutoChargeConfirmationState
}
