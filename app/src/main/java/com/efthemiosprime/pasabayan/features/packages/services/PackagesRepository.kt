package com.efthemiosprime.pasabayan.features.packages.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

interface PackagesRepository {

    suspend fun loadPackages(): Result<List<PackageRequest>>

    /** Legacy flat-list loader. Prefer [loadAvailablePackagesPage] for browse-tab flows. */
    suspend fun loadAvailablePackages(params: Map<String, String> = emptyMap()): Result<List<AvailablePackage>>

    /**
     * Paginated browse loader — returns the full envelope (page/last/total/nearby)
     * iOS-parity infinite-scroll needs. The VM's pagination state machine drives
     * `page` and bumps it on `loadMore`.
     */
    suspend fun loadAvailablePackagesPage(
        filter: PackageBrowseFilter,
        page: Int,
        perPage: Int = DEFAULT_PER_PAGE,
    ): Result<AvailablePackagesPage>

    suspend fun getPackage(id: Int): Result<PackageRequest>

    suspend fun createPackage(
        request: CreatePackageRequestJson,
        imageUris: List<Uri> = emptyList(),
    ): Result<PackageRequest>

    suspend fun createServiceRequest(request: CreateServiceRequestBodyJson): Result<PackageRequest>

    suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson): Result<PackageRequest>

    /**
     * Multipart `PUT /packages/{id}` mirroring iOS `updatePackageDetailsWithImages`.
     * Only non-null fields in [request] are sent. Images are appended as `images[]`.
     * When the server returns `imagesProcessing = true` the caller should poll
     * `getPackage(id)` until that flag flips. iOS caps at 5 images / 5 MB each.
     */
    suspend fun updatePackageWithImages(
        id: Int,
        request: PackageUpdateRequestJson,
        imageUris: List<Uri>,
    ): Result<PackageRequest>

    suspend fun cancelPackage(id: Int): Result<Unit>

    /**
     * iOS parity: filter currently-active shipper packages with the same
     * pickup and delivery cities to warn the user of likely duplicates
     * before creating a new request. Server has no dedicated endpoint —
     * filtering is client-side over `loadPackages()`.
     */
    suspend fun findSimilarPackages(pickupCity: String, deliveryCity: String): Result<List<PackageRequest>>

    companion object {
        /** iOS-parity default page size — mirrors `Pagination.defaultPerPage`. */
        const val DEFAULT_PER_PAGE: Int = 15
    }
}
