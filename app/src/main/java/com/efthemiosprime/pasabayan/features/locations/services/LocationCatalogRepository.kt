package com.efthemiosprime.pasabayan.features.locations.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.location.LocationCatalogApi
import com.efthemiosprime.pasabayan.features.locations.model.LocationCatalogSnapshot
import com.efthemiosprime.pasabayan.features.locations.model.LocationCatalogTransformer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface LocationCatalogRepository {
    /** Latest snapshot (loaded from disk on first access, refreshed via [refreshIfNeeded]). */
    val snapshot: StateFlow<LocationCatalogSnapshot>

    /** True once the in-memory snapshot has been populated this process. */
    val isLoaded: Boolean

    /**
     * Fetches the catalog from the API if not already loaded this process. No-op when the
     * snapshot is already present unless [forceRefresh] is true; only writes to disk when
     * the server returns a new [LocationCatalogResponseJson.version].
     */
    suspend fun refreshIfNeeded(forceRefresh: Boolean = false): Result<LocationCatalogSnapshot>
}

@Singleton
class LocationCatalogRepositoryImpl @Inject constructor(
    private val api: LocationCatalogApi,
    private val store: LocationCatalogStore,
    private val json: Json,
) : LocationCatalogRepository {

    private val _snapshot = MutableStateFlow(store.load() ?: LocationCatalogSnapshot.Empty)
    override val snapshot: StateFlow<LocationCatalogSnapshot> = _snapshot.asStateFlow()

    @Volatile
    private var loadedThisProcess: Boolean = _snapshot.value !== LocationCatalogSnapshot.Empty

    override val isLoaded: Boolean get() = loadedThisProcess

    override suspend fun refreshIfNeeded(forceRefresh: Boolean): Result<LocationCatalogSnapshot> {
        if (!forceRefresh && loadedThisProcess) return Result.success(_snapshot.value)
        return try {
            val response = api.getCatalog()
            if (!response.isSuccessful) {
                throw DomainErrorMapperException(
                    ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json),
                )
            }
            val body = response.body()
                ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
            if (!body.success) {
                throw DomainErrorMapperException(
                    DomainError.ServerError("Location catalog returned success=false"),
                )
            }
            val current = _snapshot.value
            if (current.version.isNotBlank() && current.version == body.version) {
                loadedThisProcess = true
                return Result.success(current)
            }
            val next = LocationCatalogTransformer.fromResponse(body)
            _snapshot.value = next
            store.save(next)
            loadedThisProcess = true
            Result.success(next)
        } catch (e: DomainErrorMapperException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
