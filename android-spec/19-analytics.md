# 19 — Analytics (mock-only dashboard)

**Phase:** 7 | **Feature:** Analytics | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Role-based analytics dashboard with carrier performance metrics and shipper spending analysis. **All data is mock/hardcoded** — no REST API calls. Parity target: iOS [`AnalyticsView.swift`](../../Pasabayan/Features/Analytics/Views/AnalyticsView.swift), [`AnalyticsViewModel.swift`](../../Pasabayan/Features/Analytics/ViewModels/AnalyticsViewModel.swift), [`AnalyticsModels.swift`](../../Pasabayan/Features/Analytics/Models/AnalyticsModels.swift), and all files under [`Features/Analytics/`](../../Pasabayan/Features/Analytics/).

---

## Package layout

```
features/analytics/
├── model/
│   ├── AnalyticsPeriod.kt
│   ├── CarrierHistoricalStats.kt
│   ├── MonthlyPerformance.kt
│   ├── CarrierTrends.kt
│   ├── RouteAnalytics.kt
│   ├── ShipperDetailedStats.kt
│   ├── MonthlyAnalysis.kt
│   ├── BudgetTracking.kt
│   ├── PreferredCarrier.kt
│   ├── DeliverySuccess.kt
│   ├── CostOptimization.kt
│   └── CarrierPerformanceComparison.kt
├── viewmodel/
│   └── AnalyticsViewModel.kt
├── ui/
│   └── AnalyticsScreen.kt
└── components/
    ├── CarrierAnalyticsContent.kt
    ├── ShipperAnalyticsContent.kt
    ├── CarrierMetricsOverview.kt
    ├── EarningsChartView.kt
    ├── SuccessRateChartView.kt
    ├── SpendingChartView.kt
    ├── BudgetOverviewView.kt
    ├── BudgetEfficiencyChartView.kt
    ├── DeliverySuccessView.kt
    ├── CostOptimizationView.kt
    ├── RouteAnalyticsView.kt
    ├── PreferredCarriersView.kt
    ├── TrendsView.kt
    ├── CarrierComparisonView.kt
    ├── PeriodHeaderView.kt
    └── MetricCard.kt
```

---

## Data models (all `@Serializable` for future API readiness)

### `AnalyticsPeriod`

| Field | Type | JSON key |
|-------|------|----------|
| `months` | `Int` | `months` |
| `startDate` | `String` | `start_date` |
| `endDate` | `String` | `end_date` |

### Carrier models

**`CarrierHistoricalStats`**

| Field | Type |
|-------|------|
| `period` | `AnalyticsPeriod` |
| `monthlyPerformance` | `List<MonthlyPerformance>` |
| `trends` | `CarrierTrends` |
| `routeAnalytics` | `List<RouteAnalytics>` |
| `performanceInsights` | `List<String>` |

**`MonthlyPerformance`**

| Field | Type |
|-------|------|
| `month` | `String` (ISO date) |
| `monthName` | `String` |
| `tripsCreated` | `Int` |
| `matchesReceived` | `Int` |
| `deliveriesCompleted` | `Int` |
| `earnings` | `Double` |
| `successRate` | `Double` |

**`CarrierTrends`**

| Field | Type |
|-------|------|
| `earningsTrend` | `String` |
| `deliveriesTrend` | `String` |
| `earningsGrowthRate` | `Double` |
| `deliveriesGrowthRate` | `Double` |

**`RouteAnalytics`**

| Field | Type |
|-------|------|
| `route` | `String` |
| `totalTrips` | `Int` |
| `avgPricePerKg` | `Double` |
| `monthlyData` | `List<RouteMonthlyData>` |

**`RouteMonthlyData`**: `period: String`, `trips: Int`, `avgPrice: Double`.

### Shipper models

**`ShipperDetailedStats`**

| Field | Type |
|-------|------|
| `period` | `AnalyticsPeriod` |
| `monthlyAnalysis` | `List<MonthlyAnalysis>` |
| `budgetTracking` | `BudgetTracking` |
| `preferredCarriers` | `List<PreferredCarrier>` |
| `deliverySuccess` | `DeliverySuccess` |
| `costOptimization` | `CostOptimization` |
| `carrierPerformanceComparison` | `CarrierPerformanceComparison` |

**`MonthlyAnalysis`**

| Field | Type |
|-------|------|
| `month` | `String` |
| `monthName` | `String` |
| `requestsCreated` | `Int` |
| `packagesDelivered` | `Int` |
| `amountSpent` | `Double` |
| `budgetAllocated` | `Double` |
| `budgetEfficiency` | `Double` |
| `successRate` | `Double` |

**`BudgetTracking`**

| Field | Type |
|-------|------|
| `totalBudgetAllocated` | `Double` |
| `totalAmountSpent` | `Double` |
| `totalSavings` | `Double` |
| `overallEfficiency` | `Double` |
| `spendingTrend` | `String` |
| `efficiencyByMonth` | `List<Double>` |

**`PreferredCarrier`**

| Field | Type |
|-------|------|
| `carrierId` | `Int` |
| `carrierName` | `String` |
| `carrierRating` | `Double` |
| `totalDeliveries` | `Int` |
| `averagePrice` | `Double` |
| `successRate` | `Double` |
| `reliabilityScore` | `Double` |

**`DeliverySuccess`**

| Field | Type |
|-------|------|
| `overallSuccessRate` | `Double` |
| `successTrend` | `String` |
| `monthlySuccessRates` | `List<Double>` |
| `improvementSuggestions` | `List<String>` |

**`CostOptimization`**: `recommendations: List<OptimizationRecommendation>`, `budgetAlerts: List<BudgetAlert>`.

**`OptimizationRecommendation`**: `type: String`, `priority: String` (high/medium/low), `suggestion: String`, `potentialSavings: String`.

**`BudgetAlert`**: `type: String`, `message: String`, `severity: String` (medium/high).

**`CarrierPerformanceComparison`**: `byPrice: PriceComparison`, `byReliability: ReliabilityComparison`, `valueScore: List<ValueScore>`.

**`PriceComparison`**: `mostAffordable: String`, `mostExpensive: String`, `priceVariance: String`.

**`ReliabilityComparison`**: `mostReliable: List<String>`, `leastReliable: String`, `reliabilityVariance: String`.

**`ValueScore`**: `carrierName: String`, `score: Double`, `reason: String`.

---

## ViewModel

### `AnalyticsViewModel`

**State:**
```kotlin
data class AnalyticsUiState(
    val carrierStats: CarrierHistoricalStats? = null,
    val shipperStats: ShipperDetailedStats? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
```

**Behavior:**
- `init {}` → calls `loadMockData()`.
- `refreshData()` → sets `isLoading = true`, `delay(1000)`, reloads mock data, sets `isLoading = false`.
- Mock data: 6-month period (Aug 2024 – Jan 2025), realistic values matching iOS mock data exactly.

---

## UI screens and components

### `AnalyticsScreen` (entry point)

**Layout:**
1. `PScaffold` with `PTopBar` (title: "Analytics", inline, refresh button trailing)
2. If `isLoading`: `PCircularProgress` centered
3. Else: `LazyColumn` with 20 dp spacing → role-based content:
   - Carrier → `CarrierAnalyticsContent`
   - Shipper → `ShipperAnalyticsContent`

---

### `CarrierAnalyticsContent`

**Sections (order):**

| # | Component | Data | Notes |
|---|-----------|------|-------|
| 1 | `PeriodHeaderView` | period | "Aug 2024 – Jan 2025" |
| 2 | `CarrierMetricsOverview` | monthlyPerformance | 3 MetricCards: Total Earnings (green), Deliveries (blue), Avg Success Rate (orange) |
| 3 | `EarningsChartView` | monthlyPerformance | Green bar chart, 200 dp height, month abbreviations |
| 4 | `SuccessRateChartView` | monthlyPerformance | Blue dot/line chart, 150 dp height |
| 5 | `TrendsView` | trends | Trend rows: name, value, growth rate (green/red) |
| 6 | `RouteAnalyticsView` | routeAnalytics | Route cards: route name, trip count, avg price/kg |
| 7 | `InsightsView` | performanceInsights | Bullet-point text insights |

### `ShipperAnalyticsContent`

**Sections (order):**

| # | Component | Data | Notes |
|---|-----------|------|-------|
| 1 | `PeriodHeaderView` | period | Same as carrier |
| 2 | `BudgetOverviewView` | budgetTracking | 3 MetricCards: Allocated (blue), Spent (orange), Saved (green) + efficiency % |
| 3 | `SpendingChartView` | monthlyAnalysis | Orange bar chart, 180 dp height |
| 4 | `BudgetEfficiencyChartView` | budgetTracking.efficiencyByMonth | Green dot chart, 100 dp height |
| 5 | `PreferredCarriersView` | preferredCarriers | Carrier name + delivery count list |
| 6 | `CostOptimizationView` | costOptimization | Recommendations (first 2) + alerts (first 2) |
| 7 | `DeliverySuccessView` | deliverySuccess | Success rate (bold green) + trend text |
| 8 | `CarrierComparisonView` | carrierPerformanceComparison | Most affordable + most reliable |

---

### Shared sub-components

**`PeriodHeaderView`** — Displays date range with role icon.

**`MetricCard`** — Single stat card:
- Icon + label (caption) + value (h4 bold) + accent color
- Uses `PCard` with `dsCardStyle()`
- Colors: green (earnings/savings), blue (deliveries/allocated), orange (spending/rate)

**Chart components** — Custom Compose composables:
- Bar charts: vertical bars normalized to max height, month labels below
- Dot/line charts: dots positioned by value, thin vertical lines
- Use `Canvas` composable or Compose drawing APIs — no external charting library required
- Gradient fills where iOS uses `LinearGradient` (e.g. `StatusTransitCard`)

**`InsightsView`** — `LazyColumn` of text items with bullet icon prefix. Reusable from shared components.

---

## Navigation

Analytics is accessed from **Profile tab → Analytics menu item** (or as a sub-screen within Profile). Not a standalone tab.

---

## Localization

All strings in `res/values/strings_analytics.xml` and `res/values-fr/strings_analytics.xml`:

```xml
<string name="analytics_title">Analytics</string>
<string name="analytics_loading">Loading analytics…</string>
<string name="analytics_period_header">%1$s – %2$s</string>
<string name="analytics_carrier_total_earnings">Total Earnings</string>
<string name="analytics_carrier_deliveries">Deliveries</string>
<string name="analytics_carrier_avg_success">Avg Success Rate</string>
<string name="analytics_carrier_earnings_chart">Monthly Earnings</string>
<string name="analytics_carrier_success_chart">Success Rate</string>
<string name="analytics_carrier_trends">Trends</string>
<string name="analytics_carrier_routes">Route Analytics</string>
<string name="analytics_carrier_insights">Performance Insights</string>
<string name="analytics_shipper_budget_overview">Budget Overview</string>
<string name="analytics_shipper_allocated">Allocated</string>
<string name="analytics_shipper_spent">Spent</string>
<string name="analytics_shipper_saved">Saved</string>
<string name="analytics_shipper_efficiency">Overall Efficiency</string>
<string name="analytics_shipper_spending_chart">Monthly Spending</string>
<string name="analytics_shipper_efficiency_chart">Budget Efficiency</string>
<string name="analytics_shipper_preferred_carriers">Preferred Carriers</string>
<string name="analytics_shipper_cost_optimization">Cost Optimization</string>
<string name="analytics_shipper_delivery_success">Delivery Success</string>
<string name="analytics_shipper_comparison">Carrier Comparison</string>
<string name="analytics_shipper_most_affordable">Most Affordable</string>
<string name="analytics_shipper_most_reliable">Most Reliable</string>
```

---

## TDD checklist

- [ ] All analytics model classes serialize/deserialize correctly (future API readiness)
- [ ] `AnalyticsViewModel`: mock data loads on init, `refreshData()` delays 1s then reloads
- [ ] `AnalyticsViewModel`: loading state transitions (idle → loading → loaded)
- [ ] `MetricCard` renders with correct accent color per metric type
- [ ] Bar chart normalizes to max value, handles empty data
- [ ] Carrier analytics shows all 7 sections with correct data
- [ ] Shipper analytics shows all 8 sections with correct data
- [ ] Role switching shows correct content (carrier vs shipper)
