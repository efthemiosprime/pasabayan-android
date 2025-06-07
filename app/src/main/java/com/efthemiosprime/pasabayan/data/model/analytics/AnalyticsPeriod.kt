package com.efthemiosprime.pasabayan.data.model.analytics

/**
 * Represents a time period for analytics data
 */
data class AnalyticsPeriod(
    val months: Int,
    val startDate: String,
    val endDate: String
) 