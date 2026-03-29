package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt

interface ReceiptRepository {
    /** Returns (receipts, hasMore, total). */
    suspend fun fetchReceipts(page: Int, perPage: Int = 20): Result<Triple<List<PaymentReceipt>, Boolean, Int>>
    suspend fun fetchReceipt(transactionId: Int): Result<PaymentReceipt>
}
