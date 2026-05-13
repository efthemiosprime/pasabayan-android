package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.features.bookings.model.MatchReceipt

/**
 * Service-match receipt repository. Mirrors iOS `BookingsAPIService.uploadReceipt`
 * + `getReceipt`. Distinct from [com.efthemiosprime.pasabayan.features.payments.services.ReceiptRepository]
 * (payments hub transaction receipts) — different endpoints, different DTOs.
 */
interface MatchReceiptRepository {

    /**
     * Carrier uploads a receipt photo for a delivered service match. The
     * impl compresses the image (1024 px / JPEG quality 80, matching iOS)
     * before sending the multipart payload.
     */
    suspend fun uploadReceipt(matchId: Int, photoBytes: ByteArray): Result<MatchReceipt>

    /**
     * Shipper fetches the receipt for a match. Returns `Result.success(null)`
     * on 404 (no receipt uploaded yet) — matches iOS, which surfaces "no
     * receipt yet" rather than an error in that case.
     */
    suspend fun fetchReceipt(matchId: Int): Result<MatchReceipt?>
}
