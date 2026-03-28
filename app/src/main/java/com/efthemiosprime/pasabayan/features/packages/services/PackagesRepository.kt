package com.efthemiosprime.pasabayan.features.packages.services

import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

interface PackagesRepository {

    suspend fun loadPackages(): Result<List<PackageRequest>>

    suspend fun loadAvailablePackages(params: Map<String, String> = emptyMap()): Result<List<AvailablePackage>>

    suspend fun getPackage(id: Int): Result<PackageRequest>

    suspend fun createPackage(request: CreatePackageRequestJson): Result<PackageRequest>

    suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson): Result<PackageRequest>

    suspend fun cancelPackage(id: Int): Result<Unit>
}
