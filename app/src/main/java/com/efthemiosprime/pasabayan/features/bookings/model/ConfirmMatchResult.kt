package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Domain result of `PUT /matches/{id}/confirm`. Carries the resulting match
 * plus the envelope-level `auto_charge` payload and `chat_conversation_id`
 * the backend opens on confirmation.
 *
 * The auto-charge sheet (`AutoChargeConfirmationViewModel`) reads
 * [autoCharge] to decide whether to dismiss with success, prompt for a
 * payment method, or retry via `POST /payments/matches/{id}/auto-charge`.
 */
data class ConfirmMatchResult(
    val match: DeliveryMatch,
    val autoCharge: AutoChargeInfo?,
    val chatConversationId: Int?,
)
