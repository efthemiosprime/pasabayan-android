package com.efthemiosprime.pasabayan.data.repository

import com.efthemiosprime.pasabayan.data.model.analytics.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implementation of AnalyticsRepository with mock data
 */
class AnalyticsRepositoryImpl : AnalyticsRepository {
    
    override suspend fun getCarrierAnalytics(): CarrierHistoricalStats {
        return createMockCarrierStats()
    }
    
    override suspend fun getShipperAnalytics(): ShipperDetailedStats {
        return createMockShipperStats()
    }
    
    override fun getAnalyticsStream(): Flow<Pair<CarrierHistoricalStats?, ShipperDetailedStats?>> {
        return flowOf(Pair(createMockCarrierStats(), createMockShipperStats()))
    }
    
    private fun createMockCarrierStats(): CarrierHistoricalStats {
        val period = AnalyticsPeriod(
            months = 6,
            startDate = "2024-08-01",
            endDate = "2025-01-31"
        )
        
        val monthlyPerformance = listOf(
            MonthlyPerformance(
                month = "2024-08",
                monthName = "Aug 2024",
                tripsCreated = 3,
                matchesReceived = 5,
                deliveriesCompleted = 4,
                earnings = 30.63, // CAD converted from ₱1,250.00
                successRate = 80.0
            ),
            MonthlyPerformance(
                month = "2024-09",
                monthName = "Sep 2024",
                tripsCreated = 4,
                matchesReceived = 7,
                deliveriesCompleted = 6,
                earnings = 43.61, // CAD converted from ₱1,780.00
                successRate = 85.7
            ),
            MonthlyPerformance(
                month = "2024-10",
                monthName = "Oct 2024",
                tripsCreated = 5,
                matchesReceived = 8,
                deliveriesCompleted = 7,
                earnings = 51.45, // CAD converted from ₱2,100.00
                successRate = 87.5
            ),
            MonthlyPerformance(
                month = "2024-11",
                monthName = "Nov 2024",
                tripsCreated = 4,
                matchesReceived = 6,
                deliveriesCompleted = 5,
                earnings = 40.43, // CAD converted from ₱1,650.00
                successRate = 83.3
            ),
            MonthlyPerformance(
                month = "2024-12",
                monthName = "Dec 2024",
                tripsCreated = 6,
                matchesReceived = 9,
                deliveriesCompleted = 8,
                earnings = 58.80, // CAD converted from ₱2,400.00
                successRate = 88.9
            ),
            MonthlyPerformance(
                month = "2025-01",
                monthName = "Jan 2025",
                tripsCreated = 2,
                matchesReceived = 3,
                deliveriesCompleted = 3,
                earnings = 23.28, // CAD converted from ₱950.00
                successRate = 100.0
            )
        )
        
        val trends = CarrierTrends(
            earningsTrend = "increasing",
            deliveriesTrend = "stable",
            earningsGrowthRate = 15.2,
            deliveriesGrowthRate = 8.7
        )
        
        val routeAnalytics = listOf(
            RouteAnalytics(
                route = "Manila → Cebu City",
                totalTrips = 8,
                avgPricePerKg = 0.62, // CAD converted from ₱25.50
                monthlyData = listOf(
                    RouteMonthlyData(period = "2024-08", trips = 2, avgPrice = 0.59), // CAD
                    RouteMonthlyData(period = "2024-09", trips = 2, avgPrice = 0.61), // CAD
                    RouteMonthlyData(period = "2024-10", trips = 2, avgPrice = 0.64), // CAD
                    RouteMonthlyData(period = "2024-11", trips = 1, avgPrice = 0.65), // CAD
                    RouteMonthlyData(period = "2024-12", trips = 1, avgPrice = 0.66)  // CAD
                )
            ),
            RouteAnalytics(
                route = "Makati City → BGC, Taguig",
                totalTrips = 6,
                avgPricePerKg = 0.21, // CAD converted from ₱8.75
                monthlyData = listOf(
                    RouteMonthlyData(period = "2024-08", trips = 1, avgPrice = 0.20), // CAD
                    RouteMonthlyData(period = "2024-09", trips = 2, avgPrice = 0.21), // CAD
                    RouteMonthlyData(period = "2024-10", trips = 2, avgPrice = 0.22), // CAD
                    RouteMonthlyData(period = "2024-11", trips = 1, avgPrice = 0.23)  // CAD
                )
            )
        )
        
        val insights = listOf(
            "Best performing month: Dec 2024 with $58.80 earnings", // CAD values
            "Most active route: Manila → Cebu City with 8 trips",
            "Earnings have grown consistently over the past 6 months",
            "Success rate improvement of 20% since August 2024",
            "December shows seasonal peak in demand - consider increasing capacity"
        )
        
        return CarrierHistoricalStats(
            period = period,
            monthlyPerformance = monthlyPerformance,
            trends = trends,
            routeAnalytics = routeAnalytics,
            performanceInsights = insights
        )
    }
    
    private fun createMockShipperStats(): ShipperDetailedStats {
        val period = AnalyticsPeriod(
            months = 6,
            startDate = "2024-08-01",
            endDate = "2025-01-31"
        )
        
        val monthlyAnalysis = listOf(
            MonthlyAnalysis(
                month = "2024-08",
                monthName = "Aug 2024",
                requestsCreated = 4,
                packagesDelivered = 3,
                amountSpent = 10.29, // CAD converted from ₱420.00
                budgetAllocated = 14.70, // CAD converted from ₱600.00
                budgetEfficiency = 30.0,
                successRate = 75.0
            ),
            MonthlyAnalysis(
                month = "2024-09",
                monthName = "Sep 2024",
                requestsCreated = 5,
                packagesDelivered = 4,
                amountSpent = 14.21, // CAD converted from ₱580.00
                budgetAllocated = 18.38, // CAD converted from ₱750.00
                budgetEfficiency = 22.7,
                successRate = 80.0
            ),
            MonthlyAnalysis(
                month = "2024-10",
                monthName = "Oct 2024",
                requestsCreated = 3,
                packagesDelivered = 3,
                amountSpent = 9.56, // CAD converted from ₱390.00
                budgetAllocated = 12.25, // CAD converted from ₱500.00
                budgetEfficiency = 22.0,
                successRate = 100.0
            ),
            MonthlyAnalysis(
                month = "2024-11",
                monthName = "Nov 2024",
                requestsCreated = 6,
                packagesDelivered = 5,
                amountSpent = 17.64, // CAD converted from ₱720.00
                budgetAllocated = 22.05, // CAD converted from ₱900.00
                budgetEfficiency = 20.0,
                successRate = 83.3
            ),
            MonthlyAnalysis(
                month = "2024-12",
                monthName = "Dec 2024",
                requestsCreated = 4,
                packagesDelivered = 4,
                amountSpent = 12.74, // CAD converted from ₱520.00
                budgetAllocated = 15.68, // CAD converted from ₱640.00
                budgetEfficiency = 18.8,
                successRate = 100.0
            ),
            MonthlyAnalysis(
                month = "2025-01",
                monthName = "Jan 2025",
                requestsCreated = 2,
                packagesDelivered = 2,
                amountSpent = 6.86, // CAD converted from ₱280.00
                budgetAllocated = 8.58, // CAD converted from ₱350.00
                budgetEfficiency = 20.0,
                successRate = 100.0
            )
        )
        
        val budgetTracking = BudgetTracking(
            totalBudgetAllocated = 91.63, // CAD converted from ₱3,740.00
            totalAmountSpent = 71.30, // CAD converted from ₱2,910.00
            totalSavings = 20.34, // CAD converted from ₱830.00
            overallEfficiency = 22.2,
            spendingTrend = "stable",
            efficiencyByMonth = listOf(30.0, 22.7, 22.0, 20.0, 18.8, 20.0)
        )
        
        val preferredCarriers = listOf(
            PreferredCarrier(
                carrierId = 15,
                carrierName = "Maria Santos",
                carrierRating = 4.8,
                totalDeliveries = 8,
                averagePrice = 6.01, // CAD converted from ₱245.50
                successRate = 100.0,
                reliabilityScore = 86.4
            ),
            PreferredCarrier(
                carrierId = 23,
                carrierName = "Juan Dela Cruz",
                carrierRating = 4.6,
                totalDeliveries = 6,
                averagePrice = 6.86, // CAD converted from ₱280.00
                successRate = 83.3,
                reliabilityScore = 81.3
            ),
            PreferredCarrier(
                carrierId = 31,
                carrierName = "Ana Reyes",
                carrierRating = 4.9,
                totalDeliveries = 4,
                averagePrice = 4.67, // CAD converted from ₱190.75
                successRate = 100.0,
                reliabilityScore = 85.0
            ),
            PreferredCarrier(
                carrierId = 42,
                carrierName = "Carlos Rodriguez",
                carrierRating = 4.3,
                totalDeliveries = 3,
                averagePrice = 7.84, // CAD converted from ₱320.00
                successRate = 66.7,
                reliabilityScore = 75.7
            )
        )
        
        val deliverySuccess = DeliverySuccess(
            overallSuccessRate = 88.2,
            successTrend = "increasing",
            monthlySuccessRates = listOf(75.0, 80.0, 100.0, 83.3, 100.0, 100.0),
            improvementSuggestions = listOf(
                "Excellent budget management - saving an average of 22.2% on deliveries",
                "Most reliable carrier: Maria Santos with 100% success rate",
                "Consider using Ana Reyes more often - lowest average price with 100% success",
                "Success rate has improved significantly since August 2024"
            )
        )
        
        val costOptimization = CostOptimization(
            recommendations = listOf(
                OptimizationRecommendation(
                    type = "carrier_selection",
                    priority = "high",
                    suggestion = "Use Ana Reyes for budget-conscious deliveries ($4.67 avg price)", // CAD
                    potentialSavings = "$1.34 per delivery vs current average" // CAD
                ),
                OptimizationRecommendation(
                    type = "route_optimization",
                    priority = "medium",
                    suggestion = "Bundle packages to Manila-Cebu route for better rates",
                    potentialSavings = "15-20% on long-distance deliveries"
                ),
                OptimizationRecommendation(
                    type = "timing",
                    priority = "low",
                    suggestion = "October showed best success rates - consider planning deliveries during similar periods",
                    potentialSavings = "Reduced failed delivery costs"
                )
            ),
            budgetAlerts = listOf(
                BudgetAlert(
                    type = "efficiency_decline",
                    message = "Budget efficiency has decreased from 30% to 20% - consider stricter budget controls",
                    severity = "medium"
                )
            )
        )
        
        val carrierComparison = CarrierPerformanceComparison(
            byPrice = PriceComparison(
                mostAffordable = "Ana Reyes ($4.67 avg)", // CAD
                mostExpensive = "Carlos Rodriguez ($7.84 avg)", // CAD
                priceVariance = "67.8%"
            ),
            byReliability = ReliabilityComparison(
                mostReliable = listOf("Maria Santos (100%)", "Ana Reyes (100%)"),
                leastReliable = "Carlos Rodriguez (66.7%)",
                reliabilityVariance = "33.3%"
            ),
            valueScore = listOf(
                ValueScore(carrierName = "Ana Reyes", score = 92.5, reason = "Best price-to-reliability ratio"),
                ValueScore(carrierName = "Maria Santos", score = 86.4, reason = "Highest reliability, moderate pricing"),
                ValueScore(carrierName = "Juan Dela Cruz", score = 81.3, reason = "Good balance of price and reliability"),
                ValueScore(carrierName = "Carlos Rodriguez", score = 75.7, reason = "Higher pricing with lower reliability")
            )
        )
        
        return ShipperDetailedStats(
            period = period,
            monthlyAnalysis = monthlyAnalysis,
            budgetTracking = budgetTracking,
            preferredCarriers = preferredCarriers,
            deliverySuccess = deliverySuccess,
            costOptimization = costOptimization,
            carrierPerformanceComparison = carrierComparison
        )
    }
} 