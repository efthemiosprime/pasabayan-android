package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import com.efthemiosprime.pasabayan.features.payments.model.StripeConnectError
import com.efthemiosprime.pasabayan.features.payments.model.canPayout
import com.efthemiosprime.pasabayan.features.payments.model.hasAccount
import com.efthemiosprime.pasabayan.features.payments.model.isOnboarded
import com.efthemiosprime.pasabayan.features.payments.services.StripeConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class StripeConnectUiState(
    val status: StripeConnectStatusJson? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showOnboarding: Boolean = false,
    val onboardingUrl: String? = null,
    val dashboardUrl: String? = null,
    val showDashboard: Boolean = false,
)

/**
 * iOS parity: `StripeConnectViewModel.swift`.
 *
 * `loadStatus` is **debounced** (2s) and **single-flight** to avoid 429 from rapid status checks
 * (e.g. on screen recomposition or background return). `forceRefresh = true` (used by
 * `handleOnboardingReturn`) bypasses the debounce.
 */
@HiltViewModel
class StripeConnectViewModel @Inject constructor(
    private val connectRepository: StripeConnectRepository,
    private val clock: Clock,
) : ViewModel() {

    /** Test-only secondary constructor with a default system clock. */
    constructor(connectRepository: StripeConnectRepository) : this(connectRepository, SystemClock)

    private val _uiState = MutableStateFlow(StripeConnectUiState())
    val uiState: StateFlow<StripeConnectUiState> = _uiState.asStateFlow()

    private val loadStatusMutex = Mutex()
    private var lastLoadStatusCompletedAt: Long? = null

    fun loadStatus(forceRefresh: Boolean = false) {
        if (loadStatusMutex.isLocked) return
        if (!forceRefresh) {
            val last = lastLoadStatusCompletedAt
            if (last != null && clock.nowMillis() - last < LOAD_STATUS_DEBOUNCE_MS) return
        }
        viewModelScope.launch {
            loadStatusMutex.withLock {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                connectRepository.checkStatus().fold(
                    onSuccess = { status ->
                        _uiState.update {
                            it.copy(
                                status = status,
                                isLoading = false,
                                showOnboarding = false,
                                onboardingUrl = null,
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.userFacingMessage()) }
                    },
                )
                lastLoadStatusCompletedAt = clock.nowMillis()
            }
        }
    }

    fun startOnboarding() {
        if (!shouldStartOnboarding(_uiState.value.status)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = StripeConnectError.AlreadyOnboarded.displayMessage,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showOnboarding = false) }
            connectRepository.startOnboarding().fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(isLoading = false, onboardingUrl = url, showOnboarding = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.userFacingMessage()) }
                },
            )
        }
    }

    fun openDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showDashboard = false) }
            connectRepository.getDashboardUrl().fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(isLoading = false, dashboardUrl = url, showDashboard = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.userFacingMessage()) }
                },
            )
        }
    }

    fun handleOnboardingReturn() {
        _uiState.update { it.copy(showOnboarding = false, onboardingUrl = null) }
        loadStatus(forceRefresh = true)
    }

    fun handleDashboardDismiss() {
        _uiState.update { it.copy(showDashboard = false, dashboardUrl = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun Throwable.userFacingMessage(): String = when (this) {
        is StripeConnectError -> displayMessage
        else -> message ?: "Stripe connect request failed"
    }

    companion object {
        const val LOAD_STATUS_DEBOUNCE_MS: Long = 2_000L

        fun shouldStartOnboarding(status: StripeConnectStatusJson?): Boolean =
            status?.isOnboarded != true

        @Suppress("unused")
        internal fun statusCanPayout(status: StripeConnectStatusJson?): Boolean =
            status?.canPayout == true

        @Suppress("unused")
        internal fun statusHasAccount(status: StripeConnectStatusJson?): Boolean =
            status?.hasAccount == true
    }
}

/** Testable clock seam for the [StripeConnectViewModel] debounce. */
fun interface Clock {
    fun nowMillis(): Long
}

internal object SystemClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
