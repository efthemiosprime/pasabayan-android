package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult

/**
 * Domain result of cancelling a match.
 * Parity with iOS `CancelMatchResponse` from MatchingModels.swift.
 *
 * `match` reflects the cancelled match returned by the backend (status flipped
 * to cancelled with audit timestamps). `chatConversationId` lets the UI re-route
 * the user to the conversation system message. `refund` surfaces refund
 * processing outcome when the cancellation triggered one.
 */
data class CancelMatchResult(
    val match: DeliveryMatch,
    val chatConversationId: Int?,
    val refund: RefundResult?,
)
