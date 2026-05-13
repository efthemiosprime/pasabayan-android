package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Service-match receipt uploaded by the carrier and viewable by the shipper.
 * Mirrors iOS `ReceiptResponse.data` projection.
 *
 * - [receiptPhoto] — backend storage key (private).
 * - [receiptUrl] — public CDN URL for inline rendering.
 * - [uploadedAt] — ISO-8601 timestamp; null on the immediate upload response
 *   and populated by the subsequent fetch.
 */
data class MatchReceipt(
    val receiptPhoto: String,
    val receiptUrl: String,
    val uploadedAt: String? = null,
)
