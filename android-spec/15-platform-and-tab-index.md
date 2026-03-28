# 15 — Platform cross-check: tabs, docs, and iOS features

**Purpose:** Re-scan checklist so the Android specs stay aligned with **`docs/tabs/`** and the **Pasabayan** codebase. Use this when auditing for missing flows.

**Complete feature inventory (no gaps):** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md) — every `Pasabayan/Features/*` folder + core `Services/`.

**First-time setup:** [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) — read order, workflow, pitfalls.

---

## `docs/tabs/` — tab documentation index

Files on disk (see also [docs/tabs/README.md](../tabs/README.md); that README was corrected to match these names):

| File | Role | Tab index | Label (UI) | Root view(s) |
|------|------|-----------|------------|----------------|
| [SHIPPER_TAB_EXPLORE.md](../tabs/SHIPPER_TAB_EXPLORE.md) | Shipper | 0 | Explore | `ShipperHomeContent` |
| [SHIPPER_TAB_MATCHES.md](../tabs/SHIPPER_TAB_MATCHES.md) | Shipper | 1 | Matches | `ShipperMatchesView` |
| [SHIPPER_TAB_PACKAGES.md](../tabs/SHIPPER_TAB_PACKAGES.md) | Shipper | 2 | My Packages | `ShipperPackagesContent` |
| [CARRIER_TAB_EXPLORE.md](../tabs/CARRIER_TAB_EXPLORE.md) | Carrier | 0 | Explore | `CarrierHomeContent` |
| [CARRIER_TAB_MATCHES.md](../tabs/CARRIER_TAB_MATCHES.md) | Carrier | 1 | Matches | `CarrierBookingsContent` |
| [CARRIER_TAB_MYTRIPS.md](../tabs/CARRIER_TAB_MYTRIPS.md) | Carrier | 2 | My Trips | `CarrierTripsContent` |
| [SHARED_TAB_MESSAGES.md](../tabs/SHARED_TAB_MESSAGES.md) | Both | 3 | Messages | `ConversationsView` |
| [SHARED_TAB_PROFILE.md](../tabs/SHARED_TAB_PROFILE.md) | Both | 4 | Profile | `ProfileView` |

**Related docs (outside `tabs/`):** [TAB_UI_FLOW_DIAGRAMS.md](../TAB_UI_FLOW_DIAGRAMS.md), [DASHBOARD_VIEW_STRUCTURE_CARRIER.md](../DASHBOARD_VIEW_STRUCTURE_CARRIER.md), [DASHBOARD_VIEW_STRUCTURE_SHIPPER.md](../DASHBOARD_VIEW_STRUCTURE_SHIPPER.md), [MATCH_CONFIRMATION_FLOW.md](../MATCH_CONFIRMATION_FLOW.md), [BRANDING_DESIGN_GUIDE.md](../BRANDING_DESIGN_GUIDE.md).

**Spec mapping:** Tab structure + Explore parity → [13-ui-tab-explore.md](13-ui-tab-explore.md); tokens → [14-design-system.md](14-design-system.md).

---

## Programmatic navigation (parity)

iOS uses `NotificationCenter` for tab switches and deep links (see `docs/tabs/README.md`). Android should implement **equivalent** typed navigation/events:

- `SwitchToTab` / `tabIndex`
- `openMatchDetail` / `matchId`
- `openConversation` / `conversationId`
- `setBookingFilter` / `filter`

Document behavior in feature specs and [00-architecture.md](00-architecture.md).

---

## iOS features vs spec files

See the full **audit table** (all 16 `Features/` modules + cross-cutting services) in [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md). Highlights:

| Topic | Where |
|-------|--------|
| **Onboarding** | [**17-onboarding.md**](17-onboarding.md) — `PasabayanApp` / `ContentView` order, `OnboardingScreen`, city, consent |
| **Analytics** | **Mock-only** — `AnalyticsViewModel.loadMockData()`; no REST in folder; see mock data structures below |
| **Home city** | `HomeCityDetectionService` → [12-legal-support-misc.md](12-legal-support-misc.md) |
| **Live carrier GPS** | `LiveTrackingViewModel`, `CarrierLocationService` → [05-bookings-matches.md](05-bookings-matches.md) |

---

## Analytics mock data structures (no API — client-only)

From [`AnalyticsViewModel.swift`](../../Pasabayan/Features/Analytics/ViewModels/AnalyticsViewModel.swift) and [`AnalyticsModels.swift`](../../Pasabayan/Features/Analytics/Models/AnalyticsModels.swift). All data is **mock/hardcoded** — no REST API calls.

### Carrier analytics

| Field | Type | Notes |
|-------|------|-------|
| Period | months, startDate, endDate | Time range |
| Monthly performance | Array of `{month, monthName, tripsCreated, matchesReceived, deliveriesCompleted, earnings, successRate}` | Per-month stats |
| Trends | `earningsTrend`, `deliveriesTrend` (strings), growth rates | Trend indicators |
| Route analytics | Array of `{route, totalTrips, avgPricePerKg, monthlyData}` | Per-route breakdown |
| Performance insights | String array | Text insights |

### Shipper analytics

| Field | Type | Notes |
|-------|------|-------|
| Monthly analysis | Similar structure to carrier | Per-month stats |
| Budget tracking | `{totalBudgetAllocated, totalAmountSpent, totalSavings, overallEfficiency, spendingTrend, efficiencyByMonth}` | Spending analysis |
| Preferred carriers | Array of `{carrierId, carrierName, carrierRating, totalDeliveries, averagePrice, successRate, reliabilityScore}` | Carrier comparison |
| Delivery success | `{overallSuccessRate, successTrend, monthlySuccessRates, improvementSuggestions}` | Success metrics |
| Cost optimization | `{recommendations, budgetAlerts}` | Suggestions |

### Implementation note

`AnalyticsViewModel.refreshData()` simulates a 1-second delay then reloads mock data. Mirror this on Android with `delay(1000)` + hardcoded data. Charts use the same data structures — implement with a Compose charting library or simple custom composables.

---

## TDD / parity notes

- Tab-level UI: cross-reference `PasabayanUITests/Flows/…` with `docs/tabs/*.md` and [13-ui-tab-explore.md](13-ui-tab-explore.md).
- Shipper Matches tab documents **Counter Offers** filter chip — see [05-bookings-matches.md](05-bookings-matches.md).
