package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptApi
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.toDomain
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    private val receiptApi: ReceiptApi,
    private val json: Json,
) : ReceiptRepository {

    override suspend fun fetchReceipts(page: Int, perPage: Int): Result<Triple<List<PaymentReceipt>, Boolean, Int>> {
        return try {
            val res = receiptApi.getReceipts(page, perPage)
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val receipts = body.data.map { it.toDomain() }
            val meta = body.meta
            val hasMore = meta != null && meta.currentPage < meta.lastPage
            val total = meta?.total ?: receipts.size
            Result.success(Triple(receipts, hasMore, total))
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchReceipt(transactionId: Int): Result<PaymentReceipt> {
        return try {
            val res = receiptApi.getReceipt(transactionId)
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val receipt = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(receipt)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
