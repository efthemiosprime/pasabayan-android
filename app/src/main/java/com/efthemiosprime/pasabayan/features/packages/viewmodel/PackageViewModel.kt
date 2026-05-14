package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitRequestMapper
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.PackagesRepository
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
    // -- Carrier-explore browse state (iOS-parity infinite-scroll) --
    /** User-controlled filter sent on every fetch. */
    val availablePackagesFilter: PackageBrowseFilter = PackageBrowseFilter(),
    /** Page number of the last successful fetch; 0 before any page lands. */
    val availablePackagesCurrentPage: Int = 0,
    /** Drives the auto-loader and the "Load more" affordance. */
    val availablePackagesHasMore: Boolean = true,
    /** In-flight append (page > 1). Independent of [isLoadingAvailablePackages]. */
    val availablePackagesIsLoadingMore: Boolean = false,
    /** Top-level `nearby` flag from the response envelope (parity §4). */
    val availablePackagesNearby: Boolean? = null,
    /** Surfaced only for failed `loadMore` calls; reload errors use [errorMessage]. */
    val availablePackagesLoadMoreError: String? = null,
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
    /**
     * One-shot signal: a creation/request action was blocked because the user's phone
     * is not verified. UI shows [VerifyPhonePromptSheet] for this reason and calls
     * [PackageViewModel.consumeRequiresPhoneVerification] to clear it.
     */
    val requiresPhoneVerification: VerifyPhoneReason? = null,
    /**
     * iOS parity: existing active packages with the same pickup/delivery cities
     * as the request the user is composing. Surfaced inline in the create flow
     * to warn shippers of likely duplicates. Cleared via
     * [PackageViewModel.clearSimilarPackages] when the user dismisses the warning
     * or proceeds anyway.
     */
    val similarPackages: List<PackageRequest> = emptyList(),
) {
    /**
     * iOS parity: `packageType` filter is applied client-side after the
     * server returns the page (the backend has no equivalent param yet).
     * UI should bind to this rather than [availablePackages] when the
     * filter sheet is in scope.
     */
    val visibleAvailablePackages: List<AvailablePackage>
        get() = availablePackagesFilter.packageType?.let { type ->
            availablePackages.filter { it.packageType == type }
        } ?: availablePackages
}

@HiltViewModel
class PackageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packagesRepository: PackagesRepository,
    private val bookingsRepository: BookingsRepository,
    private val requirePhoneVerification: RequirePhoneVerificationUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageUiState())
    val uiState: StateFlow<PackageUiState> = _uiState.asStateFlow()

    /**
     * Bumped on every browse reload. Each in-flight fetch captures the
     * snapshot; results from stale generations are dropped so a fast
     * filter-toggle followed by a slow first response can't replay onto
     * the freshly reset list. iOS parity: `loadGeneration` in
     * `CarrierBrowsePackagesViewModel`.
     */
    private var availablePackagesLoadGeneration: Int = 0

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

    // -- iOS-parity browse pagination state machine --

    /**
     * Update the filter without fetching. Use when the user is typing /
     * tapping chips inside a filter sheet; call [applyBrowseFilter] when
     * they hit Apply.
     */
    fun setBrowseFilter(filter: PackageBrowseFilter) {
        _uiState.update { it.copy(availablePackagesFilter = filter) }
    }

    /**
     * Reset pagination and fetch page 1 with the current filter. Safe to
     * call from `LaunchedEffect(Unit)`, pull-to-refresh, search submit,
     * and filter-apply.
     */
    fun applyBrowseFilter() {
        val generation = ++availablePackagesLoadGeneration
        _uiState.update {
            it.copy(
                isLoadingAvailablePackages = true,
                availablePackagesIsLoadingMore = false,
                availablePackagesLoadMoreError = null,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            val filter = _uiState.value.availablePackagesFilter
            val result = packagesRepository.loadAvailablePackagesPage(filter = filter, page = 1)
            if (generation != availablePackagesLoadGeneration) return@launch
            result.fold(
                onSuccess = { page -> applyFirstPage(page) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingAvailablePackages = false,
                            hasLoadedAvailablePackages = true,
                            availablePackages = emptyList(),
                            availablePackagesCurrentPage = 0,
                            availablePackagesHasMore = false,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_error_load_available_packages),
                        )
                    }
                },
            )
        }
    }

    /** Clears the filter back to empty then re-fetches page 1. */
    fun clearBrowseFilter() {
        setBrowseFilter(PackageBrowseFilter())
        applyBrowseFilter()
    }

    /**
     * Fetch the next page and append. No-op when already loading more,
     * when no more pages exist, or when the first page hasn't landed yet.
     * The race guard on [availablePackagesLoadGeneration] ensures a stale
     * append can't follow a reload.
     */
    fun loadMoreAvailablePackages() {
        val snapshot = _uiState.value
        if (snapshot.availablePackagesIsLoadingMore) return
        if (!snapshot.availablePackagesHasMore) return
        if (snapshot.availablePackagesCurrentPage < 1) return
        if (snapshot.isLoadingAvailablePackages) return

        val generation = availablePackagesLoadGeneration
        val nextPage = snapshot.availablePackagesCurrentPage + 1
        _uiState.update {
            it.copy(
                availablePackagesIsLoadingMore = true,
                availablePackagesLoadMoreError = null,
            )
        }
        viewModelScope.launch {
            val result = packagesRepository.loadAvailablePackagesPage(
                filter = snapshot.availablePackagesFilter,
                page = nextPage,
            )
            if (generation != availablePackagesLoadGeneration) return@launch
            result.fold(
                onSuccess = { page -> appendPage(page) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            availablePackagesIsLoadingMore = false,
                            availablePackagesLoadMoreError = e.message
                                ?: context.getString(R.string.packages_error_load_available_packages),
                        )
                    }
                },
            )
        }
    }

    private fun applyFirstPage(page: AvailablePackagesPage) {
        _uiState.update {
            it.copy(
                availablePackages = page.packages,
                availablePackagesCurrentPage = page.currentPage,
                availablePackagesHasMore = page.hasMore,
                availablePackagesNearby = page.nearby,
                isLoadingAvailablePackages = false,
                availablePackagesIsLoadingMore = false,
                availablePackagesLoadMoreError = null,
                hasLoadedAvailablePackages = true,
                errorMessage = null,
            )
        }
    }

    private fun appendPage(page: AvailablePackagesPage) {
        _uiState.update { state ->
            // Dedupe by effectiveId — the server may overlap pages when items shift between
            // pages between requests (insertions are rare but possible).
            val existing = state.availablePackages.map { it.effectiveId }.toHashSet()
            val merged = state.availablePackages + page.packages.filter { it.effectiveId !in existing }
            state.copy(
                availablePackages = merged,
                availablePackagesCurrentPage = page.currentPage,
                availablePackagesHasMore = page.hasMore,
                // nearby reflects the *server's* current decision; keep the latest.
                availablePackagesNearby = page.nearby ?: state.availablePackagesNearby,
                availablePackagesIsLoadingMore = false,
                availablePackagesLoadMoreError = null,
            )
        }
    }

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
                    applyUpdatedPackage(updatedPackage)
                    if (updatedPackage.imagesProcessing == true) {
                        pollForProcessedImages(packageId)
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

    /**
     * Multipart update — sends only non-null fields plus new image URIs.
     * On success, kicks off [pollForProcessedImages] when the backend
     * reports `imagesProcessing = true`. Mirrors iOS
     * `updatePackageDetailsWithImages` + `pollForProcessedImages`.
     */
    fun updatePackageWithImages(
        packageId: Int,
        request: com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson,
        imageUris: List<Uri>,
        onResult: (Result<PackageRequest>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPackage = true, errorMessage = null, successMessage = null) }
            val result = packagesRepository.updatePackageWithImages(packageId, request, imageUris)
            result.fold(
                onSuccess = { updatedPackage ->
                    applyUpdatedPackage(updatedPackage)
                    if (updatedPackage.imagesProcessing == true) {
                        pollForProcessedImages(packageId)
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
            onResult(result)
        }
    }

    private fun applyUpdatedPackage(updatedPackage: PackageRequest) {
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
    }

    /**
     * Polls `getPackage(id)` to refresh image URLs after async server-side
     * image processing completes. Mirrors iOS `pollForProcessedImages`: up
     * to 3 attempts, 2-second backoff. Stops early when the server clears
     * `imagesProcessing`.
     */
    private fun pollForProcessedImages(packageId: Int, attempt: Int = 0) {
        if (attempt >= POLL_PROCESSED_IMAGES_MAX_ATTEMPTS) return
        viewModelScope.launch {
            delay(POLL_PROCESSED_IMAGES_INTERVAL_MS)
            packagesRepository.getPackage(packageId).onSuccess { refreshed ->
                _uiState.update { state ->
                    state.copy(
                        selectedPackageDetail = state.selectedPackageDetail
                            ?.takeIf { it.id == refreshed.id }?.let { refreshed }
                            ?: state.selectedPackageDetail,
                        packageRequests = state.packageRequests.map { existing ->
                            if (existing.id == refreshed.id) refreshed else existing
                        },
                    )
                }
                if (refreshed.imagesProcessing == true) {
                    pollForProcessedImages(packageId, attempt + 1)
                }
            }
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
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.CreatePackage) }
            return
        }
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
                    enableShipperRoleIfNeeded()
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
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.CreatePackage) }
            return
        }
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
                    enableShipperRoleIfNeeded()
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

    fun consumeRequiresPhoneVerification() {
        _uiState.update { it.copy(requiresPhoneVerification = null) }
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
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.BookTrip) }
            return
        }
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

    /**
     * iOS-parity duplicate-detection helper. Looks up active packages with
     * the same pickup/delivery cities. UI binds to
     * [PackageUiState.similarPackages] and shows a warning banner if non-empty.
     * Failures are treated as "no duplicates" — duplicate detection is
     * advisory, not blocking.
     */
    fun checkSimilarPackages(pickupCity: String, deliveryCity: String) {
        viewModelScope.launch {
            packagesRepository.findSimilarPackages(pickupCity, deliveryCity).fold(
                onSuccess = { similar ->
                    _uiState.update { it.copy(similarPackages = similar) }
                },
                onFailure = {
                    _uiState.update { it.copy(similarPackages = emptyList()) }
                },
            )
        }
    }

    fun clearSimilarPackages() {
        _uiState.update { it.copy(similarPackages = emptyList()) }
    }

    /**
     * iOS parity: after a successful package or service-request creation,
     * refresh `/auth/me` so the session picks up the backend-side shipper
     * role activation. No-op when the user is already active. Failures
     * are silent — role enablement is best-effort and not user-blocking.
     */
    private fun enableShipperRoleIfNeeded() {
        if (authRepository.currentUser().value?.isActiveShipper == true) return
        viewModelScope.launch {
            authRepository.loadCurrentUser()
        }
    }

    private companion object {
        /** iOS parity: `pollForProcessedImages` retries up to 3 times. */
        const val POLL_PROCESSED_IMAGES_MAX_ATTEMPTS = 3
        /** iOS parity: 2-second back-off between processed-image poll attempts. */
        const val POLL_PROCESSED_IMAGES_INTERVAL_MS = 2_000L
    }
}
