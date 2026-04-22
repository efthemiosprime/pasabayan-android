package com.efthemiosprime.pasabayan.features.packages.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

interface PackagesRepository {

    suspend fun loadPackages(): Result<List<PackageRequest>>

    suspend fun loadAvailablePackages(params: Map<String, String> = emptyMap()): Result<List<AvailablePackage>>

    suspend fun getPackage(id: Int): Result<PackageRequest>

    suspend fun createPackage(
        request: CreatePackageRequestJson,
        imageUris: List<Uri> = emptyList(),
    ): Result<PackageRequest>

    suspend fun createServiceRequest(request: CreateServiceRequestBodyJson): Result<PackageRequest>

    suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson): Result<PackageRequest>

    suspend fun cancelPackage(id: Int): Result<Unit>
}
