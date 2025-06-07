package com.efthemiosprime.pasabayan.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.efthemiosprime.pasabayan.data.common.LoadingState
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError

/**
 * Functional ViewModel base class following functional programming principles
 * Provides pure reducer functions and immutable state management
 */
abstract class FunctionalViewModel<State, Action, Effect>(
    initialState: State
) : ViewModel() {
    
    // Immutable state management
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    
    // Side effects (one-time events)
    private val _effects = MutableStateFlow<Effect?>(null)
    val effects: StateFlow<Effect?> = _effects.asStateFlow()
    
    /**
     * Pure reducer function - must be implemented by subclasses
     * Takes current state and action, returns new state (pure function)
     */
    protected abstract fun reduce(currentState: State, action: Action): State
    
    /**
     * Handle side effects (optional override)
     * For operations that need to interact with external systems
     */
    protected open suspend fun handleSideEffect(action: Action, currentState: State): Effect? = null
    
    /**
     * Dispatch action to update state (functional approach)
     */
    fun dispatch(action: Action) {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Pure state reduction
            val newState = reduce(currentState, action)
            _state.value = newState
            
            // Handle side effects separately
            val effect = handleSideEffect(action, newState)
            if (effect != null) {
                _effects.value = effect
            }
        }
    }
    
    /**
     * Clear effects after consumption
     */
    fun clearEffects() {
        _effects.value = null
    }
    
    /**
     * Update state with pure function
     */
    protected fun updateState(reducer: (State) -> State) {
        _state.value = reducer(_state.value)
    }
    
    /**
     * Emit side effect
     */
    protected fun emitEffect(effect: Effect) {
        _effects.value = effect
    }
}

/**
 * Functional state wrapper for common UI patterns
 */
data class UiState<T>(
    val data: T? = null,
    val loadingState: LoadingState<T> = LoadingState.Idle,
    val error: AppError? = null,
    val isRefreshing: Boolean = false
) {
    val isLoading: Boolean get() = loadingState.isLoading
    val isSuccess: Boolean get() = loadingState.isSuccess
    val isError: Boolean get() = error != null
    val isIdle: Boolean get() = loadingState.isIdle
    
    companion object {
        fun <T> idle(): UiState<T> = UiState()
        fun <T> loading(currentData: T? = null): UiState<T> = UiState(
            data = currentData,
            loadingState = LoadingState.Loading
        )
        fun <T> success(data: T): UiState<T> = UiState(
            data = data,
            loadingState = LoadingState.Success(data)
        )
        fun <T> error(error: AppError, currentData: T? = null): UiState<T> = UiState(
            data = currentData,
            error = error
        )
    }
    
    // Functional transformations
    inline fun <R> map(transform: (T) -> R): UiState<R> = UiState(
        data = data?.let(transform),
        loadingState = loadingState.map(transform),
        error = error,
        isRefreshing = isRefreshing
    )
}

/**
 * Common UI actions for functional ViewModels
 */
sealed class CommonAction {
    object Refresh : CommonAction()
    object LoadData : CommonAction()
    object ClearError : CommonAction()
    data class ShowError(val error: AppError) : CommonAction()
}

/**
 * Common UI effects for functional ViewModels
 */
sealed class CommonEffect {
    data class ShowToast(val message: String) : CommonEffect()
    data class ShowSnackbar(val message: String) : CommonEffect()
    data class NavigateTo(val route: String) : CommonEffect()
    object NavigateBack : CommonEffect()
} 