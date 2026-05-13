package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.network.payments.CreatePaymentResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.TipResponseJson
import com.efthemiosprime.pasabayan.features.payments.model.RefundRequest
import com.efthemiosprime.pasabayan.features.payments.model.Transaction

interface PaymentRepository {
    suspend fun createPayment(deliveryMatchId: Int, amount: Double, currency: String): Result<CreatePaymentResponseJson>
    suspend fun listTransactions(role: String? = null): Result<List<Transaction>>
    suspend fun getTransaction(id: Int): Result<Transaction>
    suspend fun captureTransaction(id: Int): Result<Transaction>
    suspend fun confirmCapture(deliveryMatchId: Int): Result<Transaction>
    suspend fun releaseTransaction(id: Int): Result<Transaction>
    suspend fun requestRefund(transactionId: Int, amount: Double?, reason: String, description: String?): Result<RefundRequest>
    suspend fun getRefundStatus(transactionId: Int): Result<RefundRequest>
    suspend fun cancelTransaction(id: Int, reason: String? = null): Result<Transaction>
    suspend fun addTip(transactionId: Int, amount: Double): Result<TipResponseJson>
}
