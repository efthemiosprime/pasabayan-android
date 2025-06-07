package com.efthemiosprime.pasabayan.data.common

/**
 * Functional Result type for error handling
 * Replaces exceptions with explicit success/failure states
 * Based on functional programming principles
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
    
    // Functional map operation
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }
    
    // Functional flatMap for chaining operations
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }
    
    // Handle success and failure cases
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (AppError) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }
    
    // Get data or null (for optional chaining)
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
    
    // Get data or default value
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> default
    }
    
    // Check if result is success
    val isSuccess: Boolean get() = this is Success
    
    // Check if result is failure
    val isFailure: Boolean get() = this is Failure
}

/**
 * Application-specific error types
 * Provides type-safe error handling
 */
sealed class AppError(val message: String, val cause: Throwable? = null) {
    data class NetworkError(val errorMessage: String, val statusCode: Int? = null) : AppError(errorMessage)
    data class ValidationError(val errorMessage: String, val field: String? = null) : AppError(errorMessage)
    data class AuthenticationError(val errorMessage: String) : AppError(errorMessage)
    data class NotFoundError(val errorMessage: String) : AppError(errorMessage)
    data class UnknownError(val errorMessage: String, val throwable: Throwable? = null) : AppError(errorMessage, throwable)
}

/**
 * Extension functions for creating Result instances
 */
fun <T> T.asSuccess(): Result<T> = Result.Success(this)
fun AppError.asFailure(): Result<Nothing> = Result.Failure(this)

/**
 * Catch exceptions and convert to Result
 */
inline fun <T> resultOf(action: () -> T): Result<T> = try {
    Result.Success(action())
} catch (e: Exception) {
    Result.Failure(AppError.UnknownError(e.message ?: "Unknown error", e))
}

/**
 * Validation result type for form validation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
    val isInvalid: Boolean get() = this is Invalid
    
    fun errorList(): List<String> = when (this) {
        is Valid -> emptyList()
        is Invalid -> errors
    }
}

/**
 * Extension function to combine validation results
 */
fun List<ValidationResult>.combine(): ValidationResult {
    val errors = flatMap { it.errorList() }
    return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
} 