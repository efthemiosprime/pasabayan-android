package com.efthemiosprime.pasabayan.data.model.analytics

/**
 * Detailed statistics for shippers
 */
data class ShipperDetailedStats(
    val period: AnalyticsPeriod,
    val monthlyAnalysis: List<MonthlyAnalysis>,
    val budgetTracking: BudgetTracking,
    val preferredCarriers: List<PreferredCarrier>,
    val deliverySuccess: DeliverySuccess,
    val costOptimization: CostOptimization,
    val carrierPerformanceComparison: CarrierPerformanceComparison
)

/**
 * Monthly analysis data for shippers
 */
data class MonthlyAnalysis(
    val month: String,
    val monthName: String,
    val requestsCreated: Int,
    val packagesDelivered: Int,
    val amountSpent: Double, // CAD values
    val budgetAllocated: Double, // CAD values
    val budgetEfficiency: Double,
    val successRate: Double
)

/**
 * Budget tracking data
 */
data class BudgetTracking(
    val totalBudgetAllocated: Double, // CAD values
    val totalAmountSpent: Double, // CAD values
    val totalSavings: Double, // CAD values
    val overallEfficiency: Double,
    val spendingTrend: String,
    val efficiencyByMonth: List<Double>
)

/**
 * Preferred carrier data
 */
data class PreferredCarrier(
    val carrierId: Int,
    val carrierName: String,
    val carrierRating: Double,
    val totalDeliveries: Int,
    val averagePrice: Double, // CAD values
    val successRate: Double,
    val reliabilityScore: Double
)

/**
 * Delivery success metrics
 */
data class DeliverySuccess(
    val overallSuccessRate: Double,
    val successTrend: String,
    val monthlySuccessRates: List<Double>,
    val improvementSuggestions: List<String>
)

/**
 * Cost optimization data
 */
data class CostOptimization(
    val recommendations: List<OptimizationRecommendation>,
    val budgetAlerts: List<BudgetAlert>
)

/**
 * Optimization recommendation
 */
data class OptimizationRecommendation(
    val type: String,
    val priority: String,
    val suggestion: String,
    val potentialSavings: String
)

/**
 * Budget alert
 */
data class BudgetAlert(
    val type: String,
    val message: String,
    val severity: String
)

/**
 * Carrier performance comparison data
 */
data class CarrierPerformanceComparison(
    val byPrice: PriceComparison,
    val byReliability: ReliabilityComparison,
    val valueScore: List<ValueScore>
)

/**
 * Price comparison data
 */
data class PriceComparison(
    val mostAffordable: String,
    val mostExpensive: String,
    val priceVariance: String
)

/**
 * Reliability comparison data
 */
data class ReliabilityComparison(
    val mostReliable: List<String>,
    val leastReliable: String,
    val reliabilityVariance: String
)

/**
 * Value score for carriers
 */
data class ValueScore(
    val carrierName: String,
    val score: Double,
    val reason: String
) 