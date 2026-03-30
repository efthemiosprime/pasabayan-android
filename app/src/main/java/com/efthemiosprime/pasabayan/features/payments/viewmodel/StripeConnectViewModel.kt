package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import com.efthemiosprime.pasabayan.features.payments.services.StripeConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

@HiltViewModel
class StripeConnectViewModel @Inject constructor(
    private val connectRepository: StripeConnectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StripeConnectUiState())
    val uiState: StateFlow<StripeConnectUiState> = _uiState.asStateFlow()

    fun loadStatus(forceRefresh: Boolean = false) {
        viewModelScope.launch {
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
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load status") }
                },
            )
        }
    }

    fun startOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showOnboarding = false) }
            connectRepository.startOnboarding().fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(isLoading = false, onboardingUrl = url, showOnboarding = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Onboarding failed") }
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
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load dashboard") }
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
}
