package com.efthemiosprime.pasabayan.features.packages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.services.PackagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageUiState(
    val packageRequests: List<PackageRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasLoadedPackages: Boolean = false,
)

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val packagesRepository: PackagesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageUiState())
    val uiState: StateFlow<PackageUiState> = _uiState.asStateFlow()

    fun loadPackages(force: Boolean = false) {
        if (!force && _uiState.value.hasLoadedPackages) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            packagesRepository.loadPackages().fold(
                onSuccess = { packages ->
                    _uiState.update {
                        it.copy(
                            packageRequests = packages,
                            isLoading = false,
                            hasLoadedPackages = true,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedPackages = true,
                            errorMessage = e.message ?: "Failed to load packages",
                        )
                    }
                },
            )
        }
    }

    fun refreshPackages() = loadPackages(force = true)

    fun cancelPackage(packageId: Int) {
        viewModelScope.launch {
            packagesRepository.cancelPackage(packageId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            packageRequests = state.packageRequests.filter { it.id != packageId },
                            successMessage = "Package cancelled",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to cancel package")
                    }
                },
            )
        }
    }

    fun packagesByStatus(status: PackageRequestStatus): List<PackageRequest> =
        _uiState.value.packageRequests.filter { it.status == status }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
