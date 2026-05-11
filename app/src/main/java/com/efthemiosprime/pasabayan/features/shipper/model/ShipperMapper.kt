package com.efthemiosprime.pasabayan.features.shipper.model

import com.efthemiosprime.pasabayan.core.network.shipper.NearbyCarrierJson
import com.efthemiosprime.pasabayan.core.network.shipper.NearbyCarriersPayloadJson

object ShipperMapper {

    fun toDomain(dto: NearbyCarrierJson): NearbyCarrier = NearbyCarrier(
        id = dto.id,
        name = dto.name,
        avatar = dto.avatar,
        completedDeliveries = dto.completedDeliveries,
        distanceKm = dto.distanceKm,
    )

    fun toDomain(dto: NearbyCarriersPayloadJson?): NearbyCarriers = NearbyCarriers(
        homeCityId = dto?.homeCityId,
        homeCityName = dto?.homeCityName,
        radiusKm = dto?.radiusKm,
        carriers = dto?.carriers.orEmpty().map(::toDomain),
    )
}
