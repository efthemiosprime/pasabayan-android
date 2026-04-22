package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitRequestMapper
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestSubmitPayload
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
    val isSubmittingPackageRequest: Boolean = false,
    val packageRequestErrorMessage: String? = null,
    val packageRequestSuccessMessage: String? = null,
    val isSubmittingServiceRequest: Boolean = false,
    val serviceRequestErrorMessage: String? = null,
    val serviceRequestSuccessMessage: String? = null,
    val isSubmittingTripRequest: Boolean = false,
    val tripRequestErrorMessage: String? = null,
    val tripRequestSuccessMessage: String? = null,
)

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val packagesRepository: PackagesRepository,
    private val bookingsRepository: BookingsRepository,
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

    fun createPackageRequest(
        payload: PackageSubmitPayload,
        imageUris: List<Uri>,
        onResult: (Result<PackageRequest>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingPackageRequest = true,
                    packageRequestErrorMessage = null,
                    packageRequestSuccessMessage = null,
                )
            }
            val request = PackageSubmitRequestMapper.toCreatePackageRequestJson(payload)
            val result = packagesRepository.createPackage(
                request = request,
                imageUris = imageUris,
            )
            result.fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isSubmittingPackageRequest = false,
                            packageRequestSuccessMessage = "Package request submitted",
                            packageRequests = listOf(created) + it.packageRequests,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingPackageRequest = false,
                            packageRequestErrorMessage = e.message ?: "Failed to submit package request",
                        )
                    }
                },
            )
            onResult(result)
        }
    }

    fun createServiceRequest(
        payload: ServiceRequestSubmitPayload,
        onResult: (Result<PackageRequest>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingServiceRequest = true,
                    serviceRequestErrorMessage = null,
                    serviceRequestSuccessMessage = null,
                )
            }
            val request = PackageSubmitRequestMapper.toCreateServiceRequestBodyJson(payload)
            val result = packagesRepository.createServiceRequest(request)
            result.fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isSubmittingServiceRequest = false,
                            serviceRequestSuccessMessage = "Service request submitted",
                            packageRequests = listOf(created) + it.packageRequests,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingServiceRequest = false,
                            serviceRequestErrorMessage = e.message ?: "Failed to submit service request",
                        )
                    }
                },
            )
            onResult(result)
        }
    }

    fun clearCreateRequestState() {
        _uiState.update {
            it.copy(
                packageRequestErrorMessage = null,
                packageRequestSuccessMessage = null,
                serviceRequestErrorMessage = null,
                serviceRequestSuccessMessage = null,
            )
        }
    }

    fun getPendingRequests(): List<PackageRequest> = _uiState.value.packageRequests.filter {
        it.status == PackageRequestStatus.OPEN ||
            it.status == PackageRequestStatus.PENDING ||
            it.status == PackageRequestStatus.PENDING_REQUEST
    }

    fun requestTripForPackage(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
        onResult: (Result<DeliveryMatch>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingTripRequest = true,
                    tripRequestErrorMessage = null,
                    tripRequestSuccessMessage = null,
                )
            }
            val result = bookingsRepository.shipperRequestTrip(
                packageId = packageId,
                tripId = tripId,
                offeredPrice = offeredPrice,
                message = message,
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingTripRequest = false,
                            tripRequestSuccessMessage = "Request sent",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingTripRequest = false,
                            tripRequestErrorMessage = e.message ?: "Failed to send request",
                        )
                    }
                },
            )
            onResult(result)
        }
    }

    fun clearTripRequestState() {
        _uiState.update { it.copy(tripRequestErrorMessage = null, tripRequestSuccessMessage = null) }
    }
}
