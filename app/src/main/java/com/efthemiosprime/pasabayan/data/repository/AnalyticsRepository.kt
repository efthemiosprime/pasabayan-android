package com.efthemiosprime.pasabayan.data.repository

import com.efthemiosprime.pasabayan.data.model.analytics.CarrierHistoricalStats
import com.efthemiosprime.pasabayan.data.model.analytics.ShipperDetailedStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for analytics data
 */
interface AnalyticsRepository {
    suspend fun getCarrierAnalytics(): CarrierHistoricalStats
    suspend fun getShipperAnalytics(): ShipperDetailedStats
    fun getAnalyticsStream(): Flow<Pair<CarrierHistoricalStats?, ShipperDetailedStats?>>
} 