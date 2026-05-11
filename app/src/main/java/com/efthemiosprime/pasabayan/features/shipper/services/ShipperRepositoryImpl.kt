package com.efthemiosprime.pasabayan.features.shipper.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.shipper.ShipperApi
import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarriers
import com.efthemiosprime.pasabayan.features.shipper.model.ShipperMapper
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipperRepositoryImpl @Inject constructor(
    private val api: ShipperApi,
    private val json: Json,
) : ShipperRepository {

    override suspend fun fetchNearbyCarriers(): Result<NearbyCarriers> = try {
        val response = api.getNearbyCarriers()
        if (!response.isSuccessful) {
            throw DomainErrorMapperException(
                ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json),
            )
        }
        val body = response.body()
            ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) {
            throw DomainErrorMapperException(
                DomainError.ServerError(body.message ?: "Failed to load nearby carriers"),
            )
        }
        Result.success(ShipperMapper.toDomain(body.data))
    } catch (e: DomainErrorMapperException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
    }
}
