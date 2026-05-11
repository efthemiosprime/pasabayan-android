package com.efthemiosprime.pasabayan.features.shipper.services

import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarriers

interface ShipperRepository {
    suspend fun fetchNearbyCarriers(): Result<NearbyCarriers>
}
