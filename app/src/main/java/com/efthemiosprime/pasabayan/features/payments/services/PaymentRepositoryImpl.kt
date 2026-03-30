package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.payments.CancelRequestJson
import com.efthemiosprime.pasabayan.core.network.payments.ConfirmCaptureRequestJson
import com.efthemiosprime.pasabayan.core.network.payments.CreatePaymentRequestJson
import com.efthemiosprime.pasabayan.core.network.payments.CreatePaymentResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentApi
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import com.efthemiosprime.pasabayan.core.network.payments.TipRequestJson
import com.efthemiosprime.pasabayan.core.network.payments.TipResponseJson
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.model.toDomain
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentApi: PaymentApi,
    private val json: Json,
) : PaymentRepository {

    override suspend fun createPayment(deliveryMatchId: Int, amount: Double, currency: String): Result<CreatePaymentResponseJson> {
        return try {
            val res = paymentApi.createPayment(CreatePaymentRequestJson(deliveryMatchId, amount, currency))
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun listTransactions(role: String?): Result<List<Transaction>> {
        return try {
            val res = paymentApi.listTransactions(role)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(null))
            val transactions = body.data.map { it.toDomain() }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getTransaction(id: Int): Result<Transaction> = txAction { paymentApi.getTransaction(id) }

    override suspend fun captureTransaction(id: Int): Result<Transaction> = txAction { paymentApi.captureTransaction(id) }

    override suspend fun confirmCapture(deliveryMatchId: Int): Result<Transaction> {
        return try {
            val res = paymentApi.confirmCapture(ConfirmCaptureRequestJson(deliveryMatchId))
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val tx = body.data?.toDomain() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun releaseTransaction(id: Int): Result<Transaction> = txAction { paymentApi.releaseTransaction(id) }

    override suspend fun requestRefund(transactionId: Int, amount: Double?, reason: String, description: String?): Result<RefundRequestDataJson> {
        return try {
            val res = paymentApi.requestRefund(transactionId, RefundRequestBodyJson(reason, amount, description))
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val data = body.data ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getRefundStatus(transactionId: Int): Result<RefundRequestDataJson> {
        return try {
            val res = paymentApi.getRefundStatus(transactionId)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val data = body.data ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun cancelTransaction(id: Int, reason: String?): Result<Transaction> {
        return try {
            val res = paymentApi.cancelTransaction(id, CancelRequestJson(reason))
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val tx = body.data?.toDomain() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun addTip(transactionId: Int, amount: Double): Result<TipResponseJson> {
        return try {
            val res = paymentApi.addTip(transactionId, TipRequestJson(amount))
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private suspend fun <T> txAction(call: suspend () -> Response<T>): Result<Transaction>
        where T : Any {
        return try {
            val res = call()
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val body = res.body()
            // Extract Transaction from response data field via reflection-free approach
            val txJson = when (body) {
                is com.efthemiosprime.pasabayan.core.network.payments.TransactionResponseJson -> {
                    if (!body.success) return Result.failure(mapBusinessError(body.message))
                    body.data
                }
                is com.efthemiosprime.pasabayan.core.network.payments.PaymentActionResponseJson -> {
                    if (!body.success) return Result.failure(mapBusinessError(body.message))
                    body.data
                }
                else -> null
            }
            val tx = txJson?.toDomain() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))

    private fun mapBusinessError(message: String?): DomainErrorMapperException =
        DomainErrorMapperException(DomainError.ServerError(message ?: "Payment request failed"))
}
