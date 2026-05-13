package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response body for `POST /services/matches/{matchId}/receipt`. Mirrors
 * iOS `ReceiptUploadResponse` — distinct from `PaymentReceiptJson`, which
 * lives under `core/network/.../payments/` and covers transaction-level
 * receipts (`/receipts`, `/receipts/{transactionId}`).
 */
@Serializable
data class MatchReceiptUploadResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: MatchReceiptDataJson? = null,
)

/**
 * Response body for `GET /services/matches/{matchId}/receipt`. iOS
 * `ReceiptResponse`. 404 from the backend is mapped at the repository
 * boundary to "no receipt yet" (returns `null` to callers) — see iOS
 * `BookingsAPIService.getReceipt`.
 */
@Serializable
data class MatchReceiptResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: MatchReceiptDetailDataJson? = null,
)

@Serializable
data class MatchReceiptDataJson(
    @SerialName("receipt_photo") val receiptPhoto: String = "",
    @SerialName("receipt_url") val receiptUrl: String = "",
)

@Serializable
data class MatchReceiptDetailDataJson(
    @SerialName("receipt_photo") val receiptPhoto: String = "",
    @SerialName("receipt_url") val receiptUrl: String = "",
    @SerialName("uploaded_at") val uploadedAt: String? = null,
)
