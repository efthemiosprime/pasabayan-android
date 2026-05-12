package com.efthemiosprime.pasabayan.features.packages.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.PackagesApi
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.model.toDomain
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackagesRepositoryImpl @Inject constructor(
    private val packagesApi: PackagesApi,
    private val json: Json,
    private val multipartFormDataFactory: MultipartFormDataFactory,
) : PackagesRepository {

    override suspend fun loadPackages(): Result<List<PackageRequest>> {
        return try {
            val res = packagesApi.getPackages()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val packages = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(packages)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadAvailablePackages(params: Map<String, String>): Result<List<AvailablePackage>> {
        return try {
            val res = packagesApi.getAvailablePackages(params)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val packages = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(packages)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadAvailablePackagesPage(
        filter: PackageBrowseFilter,
        page: Int,
        perPage: Int,
    ): Result<AvailablePackagesPage> {
        val params = buildMap {
            putAll(filter.toQueryMap())
            put("page", page.toString())
            put("per_page", perPage.toString())
        }
        return try {
            val res = packagesApi.getAvailablePackages(params)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val envelope = body.data
                ?: return Result.success(
                    AvailablePackagesPage(
                        packages = emptyList(),
                        currentPage = page,
                        lastPage = page,
                        total = 0,
                        perPage = perPage,
                        nearby = body.nearby,
                    ),
                )
            Result.success(
                AvailablePackagesPage(
                    packages = envelope.data.map { it.toDomain() },
                    currentPage = envelope.currentPage,
                    lastPage = envelope.lastPage,
                    total = envelope.total,
                    perPage = envelope.perPage,
                    nearby = body.nearby,
                ),
            )
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getPackage(id: Int): Result<PackageRequest> {
        return try {
            val res = packagesApi.getPackage(id)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val pkg = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(pkg)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createPackage(
        request: CreatePackageRequestJson,
        imageUris: List<Uri>,
    ): Result<PackageRequest> {
        return try {
            val response = if (imageUris.isEmpty()) {
                packagesApi.createPackage(request)
            } else {
                val fields = multipartFormDataFactory.createPackageFields(request)
                val imageParts = multipartFormDataFactory.createImageParts(imageUris)
                packagesApi.createPackageMultipart(fields = fields, images = imageParts)
            }
            response.toPackageResult()
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createServiceRequest(request: CreateServiceRequestBodyJson): Result<PackageRequest> {
        return try {
            val response = packagesApi.createServiceRequest(request)
            response.toPackageResult()
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson): Result<PackageRequest> {
        return try {
            val res = packagesApi.updatePackage(id, request)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val pkg = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(pkg)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun cancelPackage(id: Int): Result<Unit> {
        return try {
            val res = packagesApi.cancelPackage(id)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun retrofit2.Response<com.efthemiosprime.pasabayan.core.network.packages.PackageRequestResponseJson>.toPackageResult():
        Result<PackageRequest> {
        if (!isSuccessful) {
            return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(code(), errorBody()?.bytes(), json)),
            )
        }
        val pkg = body()?.data?.toDomain()
            ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
        return Result.success(pkg)
    }
}
