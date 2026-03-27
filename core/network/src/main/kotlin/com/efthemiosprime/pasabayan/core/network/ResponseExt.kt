package com.efthemiosprime.pasabayan.core.network

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import kotlinx.serialization.json.Json
import retrofit2.Response

/**
 * Thrown by [toDomainResult] so [kotlin.Result.exceptionOrNull] carries a [DomainError].
 */
class DomainErrorMapperException(val domainError: DomainError) : Exception(domainError.toString())

/**
 * Converts this HTTP response to [kotlin.Result]: success with body, or failure with [DomainErrorMapperException].
 * For null body on success, failure is [DomainError.InvalidResponse].
 */
fun <T> Response<T>.toDomainResult(json: Json): Result<T> {
    if (isSuccessful) {
        val b = body()
        return if (b != null) {
            Result.success(b)
        } else {
            Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
        }
    }
    val err = ApiErrorMapper.map(code(), errorBody()?.bytes(), json)
    return Result.failure(DomainErrorMapperException(err))
}

fun <T> Response<T>.foldDomainResult(json: Json, onSuccess: (T) -> Unit, onError: (DomainError) -> Unit) {
    when {
        isSuccessful -> {
            val b = body()
            if (b != null) onSuccess(b) else onError(DomainError.InvalidResponse)
        }
        else -> onError(ApiErrorMapper.map(code(), errorBody()?.bytes(), json))
    }
}
