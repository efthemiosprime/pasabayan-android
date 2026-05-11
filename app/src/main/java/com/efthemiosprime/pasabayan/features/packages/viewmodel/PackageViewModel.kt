package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitRequestMapper
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.PackagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageUiState(
    val packageRequests: List<PackageRequest> = emptyList(),
    val availablePackages: List<AvailablePackage> = emptyList(),
    val isLoadingAvailablePackages: Boolean = false,
    val hasLoadedAvailablePackages: Boolean = false,
    val selectedPackageDetail: PackageRequest? = null,
    val isLoadingPackageDetail: Boolean = false,
    val isUpdatingPackage: Boolean = false,
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
    @ApplicationContext private val context: Context,
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
                            hasLoadedPackages = false,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_load_packages),
                        )
                    }
                },
            )
        }
    }

    fun loadAvailablePackages(
        params: Map<String, String> = emptyMap(),
        force: Boolean = false,
    ) {
        if (!force && _uiState.value.hasLoadedAvailablePackages) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAvailablePackages = true, errorMessage = null) }
            packagesRepository.loadAvailablePackages(params).fold(
                onSuccess = { packages ->
                    _uiState.update {
                        it.copy(
                            availablePackages = packages,
                            isLoadingAvailablePackages = false,
                            hasLoadedAvailablePackages = true,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingAvailablePackages = false,
                            hasLoadedAvailablePackages = true,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_load_available_packages),
                        )
                    }
                },
            )
        }
    }

    fun refreshPackages() = loadPackages(force = true)

    fun refreshAvailablePackages(params: Map<String, String> = emptyMap()) =
        loadAvailablePackages(params = params, force = true)

    fun loadPackageDetail(packageId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPackageDetail = true, errorMessage = null) }
            packagesRepository.getPackage(packageId).fold(
                onSuccess = { pkg ->
                    _uiState.update {
                        it.copy(
                            selectedPackageDetail = pkg,
                            isLoadingPackageDetail = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingPackageDetail = false,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_load_package_detail),
                        )
                    }
                },
            )
        }
    }

    fun clearPackageDetail() {
        _uiState.update { it.copy(selectedPackageDetail = null, isLoadingPackageDetail = false) }
    }

    fun updatePackage(
        packageId: Int,
        request: com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPackage = true, errorMessage = null, successMessage = null) }
            packagesRepository.updatePackage(packageId, request).fold(
                onSuccess = { updatedPackage ->
                    _uiState.update { state ->
                        state.copy(
                            isUpdatingPackage = false,
                            selectedPackageDetail = updatedPackage,
                            packageRequests = state.packageRequests.map { existing ->
                                if (existing.id == updatedPackage.id) updatedPackage else existing
                            },
                            successMessage = context.getString(R.string.packages_success_update_package),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isUpdatingPackage = false,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_update_package),
                        )
                    }
                },
            )
        }
    }

    fun cancelPackage(packageId: Int) {
        viewModelScope.launch {
            packagesRepository.cancelPackage(packageId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            packageRequests = state.packageRequests.filter { it.id != packageId },
                            selectedPackageDetail = state.selectedPackageDetail?.takeIf { it.id != packageId },
                            successMessage = context.getString(R.string.packages_success_cancel_package),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_cancel_package),
                        )
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
                            packageRequestSuccessMessage = context.getString(R.string.packages_success_submit_package_request),
                            packageRequests = listOf(created) + it.packageRequests,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingPackageRequest = false,
                            packageRequestErrorMessage = e.message
                                ?: context.getString(R.string.packages_error_submit_package_request),
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
                            serviceRequestSuccessMessage = context.getString(R.string.packages_success_submit_service_request),
                            packageRequests = listOf(created) + it.packageRequests,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingServiceRequest = false,
                            serviceRequestErrorMessage = e.message
                                ?: context.getString(R.string.packages_error_submit_service_request),
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
            val flatResult: Result<DeliveryMatch> = result.map { it.match }
            flatResult.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingTripRequest = false,
                            tripRequestSuccessMessage = context.getString(R.string.packages_success_trip_request_sent),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingTripRequest = false,
                            tripRequestErrorMessage = e.message
                                ?: context.getString(R.string.packages_error_trip_request_send),
                        )
                    }
                },
            )
            onResult(flatResult)
        }
    }

    fun clearTripRequestState() {
        _uiState.update { it.copy(tripRequestErrorMessage = null, tripRequestSuccessMessage = null) }
    }
}
