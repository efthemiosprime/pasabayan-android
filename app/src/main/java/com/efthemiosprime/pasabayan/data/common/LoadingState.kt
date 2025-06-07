package com.efthemiosprime.pasabayan.data.common

/**
 * Functional loading state for UI state management
 * Represents different states of data loading operations
 */
sealed class LoadingState<out T> {
    object Idle : LoadingState<Nothing>()
    object Loading : LoadingState<Nothing>()
    data class Success<out T>(val data: T) : LoadingState<T>()
    data class Error(val error: AppError) : LoadingState<Nothing>()
    
    // Functional map operation
    inline fun <R> map(transform: (T) -> R): LoadingState<R> = when (this) {
        is Idle -> Idle
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    // Functional flatMap for chaining operations
    inline fun <R> flatMap(transform: (T) -> LoadingState<R>): LoadingState<R> = when (this) {
        is Idle -> Idle
        is Loading -> Loading
        is Success -> transform(data)
        is Error -> this
    }
    
    // Handle all cases functionally
    inline fun <R> fold(
        onIdle: () -> R,
        onLoading: () -> R,
        onSuccess: (T) -> R,
        onError: (AppError) -> R
    ): R = when (this) {
        is Idle -> onIdle()
        is Loading -> onLoading()
        is Success -> onSuccess(data)
        is Error -> onError(error)
    }
    
    // State checks
    val isIdle: Boolean get() = this is Idle
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    // Get data or null
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    // Get error or null
    fun getErrorOrNull(): AppError? = when (this) {
        is Error -> error
        else -> null
    }
}

/**
 * Extension functions for creating LoadingState instances
 */
fun <T> T.asLoadingSuccess(): LoadingState<T> = LoadingState.Success(this)
fun AppError.asLoadingError(): LoadingState<Nothing> = LoadingState.Error(this)

/**
 * Convert Result to LoadingState
 */
fun <T> Result<T>.toLoadingState(): LoadingState<T> = when (this) {
    is Result.Success -> LoadingState.Success(data)
    is Result.Failure -> LoadingState.Error(error)
}

/**
 * Functional resource state for combining loading and data
 * Useful for UI components that need to show loading, error, and success states
 */
data class Resource<out T>(
    val data: T? = null,
    val error: AppError? = null,
    val isLoading: Boolean = false
) {
    val isSuccess: Boolean get() = data != null && error == null && !isLoading
    val isError: Boolean get() = error != null && !isLoading
    val isIdle: Boolean get() = data == null && error == null && !isLoading
    
    companion object {
        fun <T> loading(data: T? = null): Resource<T> = Resource(data = data, isLoading = true)
        fun <T> success(data: T): Resource<T> = Resource(data = data)
        fun <T> error(error: AppError, data: T? = null): Resource<T> = Resource(data = data, error = error)
        fun <T> idle(): Resource<T> = Resource()
    }
    
    // Functional transformations
    inline fun <R> map(transform: (T) -> R): Resource<R> = Resource(
        data = data?.let(transform),
        error = error,
        isLoading = isLoading
    )
} 