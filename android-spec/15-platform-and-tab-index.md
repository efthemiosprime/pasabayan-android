# 15 — Platform cross-check: tabs, docs, and iOS features

**Purpose:** Re-scan checklist so the Android specs stay aligned with **`docs/tabs/`** and the **Pasabayan** codebase. Use this when auditing for missing flows.

**Complete feature inventory (no gaps):** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md) — every `Pasabayan/Features/*` folder + core `Services/`.

**First-time setup:** [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) — read order, workflow, pitfalls.

---

## `docs/tabs/` — tab documentation index

Files on disk (see also [docs/tabs/README.md](../../pasabayan-ios/docs/tabs/README.md); that README was corrected to match these names):

| File | Role | Tab index | Label (UI) | Root view(s) |
|------|------|-----------|------------|----------------|
| [SHIPPER_TAB_EXPLORE.md](../../pasabayan-ios/docs/tabs/SHIPPER_TAB_EXPLORE.md) | Shipper | 0 | Explore | `ShipperHomeContent` |
| [SHIPPER_TAB_MATCHES.md](../../pasabayan-ios/docs/tabs/SHIPPER_TAB_MATCHES.md) | Shipper | 1 | Matches | `ShipperMatchesView` |
| [SHIPPER_TAB_PACKAGES.md](../../pasabayan-ios/docs/tabs/SHIPPER_TAB_PACKAGES.md) | Shipper | 2 | My Packages | `ShipperPackagesContent` |
| [CARRIER_TAB_EXPLORE.md](../../pasabayan-ios/docs/tabs/CARRIER_TAB_EXPLORE.md) | Carrier | 0 | Explore | `CarrierHomeContent` |
| [CARRIER_TAB_MATCHES.md](../../pasabayan-ios/docs/tabs/CARRIER_TAB_MATCHES.md) | Carrier | 1 | Matches | `CarrierBookingsContent` |
| [CARRIER_TAB_MYTRIPS.md](../../pasabayan-ios/docs/tabs/CARRIER_TAB_MYTRIPS.md) | Carrier | 2 | My Trips | `CarrierTripsContent` |
| [SHARED_TAB_MESSAGES.md](../../pasabayan-ios/docs/tabs/SHARED_TAB_MESSAGES.md) | Both | 3 | Messages | `ConversationsView` |
| [SHARED_TAB_PROFILE.md](../../pasabayan-ios/docs/tabs/SHARED_TAB_PROFILE.md) | Both | 4 | Profile | `ProfileView` |

**Related docs (outside `tabs/`):** [TAB_UI_FLOW_DIAGRAMS.md](../../pasabayan-ios/docs/TAB_UI_FLOW_DIAGRAMS.md), [DASHBOARD_VIEW_STRUCTURE_CARRIER.md](../../pasabayan-ios/docs/DASHBOARD_VIEW_STRUCTURE_CARRIER.md), [DASHBOARD_VIEW_STRUCTURE_SHIPPER.md](../../pasabayan-ios/docs/DASHBOARD_VIEW_STRUCTURE_SHIPPER.md), [MATCH_CONFIRMATION_FLOW.md](../../pasabayan-ios/docs/MATCH_CONFIRMATION_FLOW.md), [BRANDING_DESIGN_GUIDE.md](../../pasabayan-ios/docs/BRANDING_DESIGN_GUIDE.md).

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
| **Analytics** | **Mock-only** — see [**19-analytics.md**](19-analytics.md) for full screen/component/model spec |
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

---

## Tab parity audit (2026-05-13)

Cross-checked every tab in `docs/tabs/*.md` against the current Android implementation. Findings filtered to **verified** gaps only — already-documented intentional deviations (single-source role switching, header consolidations, deleted UserHeaderCard) are **not** gaps and excluded.

### Major (flow-level)

| Tab | Gap | iOS source |
|---|---|---|
| Shipper Matches | **Counter Offers filter chip** missing — show only when `counterOfferCount > 0` | `SHIPPER_TAB_MATCHES.md:18` |
| Shipper Matches | **Pickup/Delivery code sheets not wired** — `GeneratePickupCodeScreen` + `ConfirmDeliveryCodeScreen` standalone but no details-sheet `onGenerateCode` / `onEnterCode` callback. Strings `bookings_action_generate_code` + `bookings_action_enter_code` already exist, unused. | `SHIPPER_TAB_MATCHES.md:91+` |
| Shipper Matches | **Share with Receiver sheet** missing (PIN + receiver link for package delivery) | `SHIPPER_TAB_MATCHES.md:259` |
| Shipper Matches | **Live Delivery Tracking sheet** missing — `LiveTrackingViewModel` exists, `bookings_action_track_live` string exists, no UI sheet | `SHIPPER_TAB_MATCHES.md:107,152` |
| Carrier My Trips | **CompatiblePackagesForTripView ("Find Packages") sheet** missing | `CARRIER_TAB_MYTRIPS.md:98,257` |
| Shipper Packages | **Status filter chips** missing entirely (iOS has 6: All, Open, Matched, InTransit, Delivered, Cancelled) | `SHIPPER_TAB_PACKAGES.md:21-26` |

### Medium (sub-flows / buttons)

| Tab | Gap | iOS source |
|---|---|---|
| Shipper Explore | Search-focus header hide-and-restore (0.3 s) | `SHIPPER_TAB_EXPLORE.md:140` |
| Shipper Matches | Counter-offer snackbar stacks up to 3 + "+N more" pill + dismiss | `SHIPPER_TAB_MATCHES.md:34-37` |
| Shipper Matches | Auto-charge failure "Retry" button on confirmed-but-unpaid badge | `SHIPPER_TAB_MATCHES.md:30` |
| Shipper Matches | "Already Rated" badge on delivered matches (pre-flight disable) | `SHIPPER_TAB_MATCHES.md:215` |
| Carrier Matches | "All" filter hides cancelled by default | `CARRIER_TAB_MATCHES.md:217` |
| Carrier Matches | `dismissedCounterOfferIds` session-level snackbar suppression | `CARRIER_TAB_MATCHES.md:218` |
| Carrier My Trips | "Update Status" entry point — `TripStatusUpdateSheet.kt` exists but no card-overflow / details-toolbar trigger | `CARRIER_TAB_MYTRIPS.md:107,114,228` |
| Carrier My Trips | TripCard inline "Cancel Trip" confirmation alert | `CARRIER_TAB_MYTRIPS.md:127` |
| Carrier My Trips | `TripTutorialOverlay` on first-empty state (separate from creation tutorial) | `CARRIER_TAB_MYTRIPS.md:68` |
| Carrier My Trips | EditTripSheet scope — iOS edits status + capacity + pricing + weight + notes; Android only weight + notes | `CARRIER_TAB_MYTRIPS.md:206-224` |
| Shipper Packages | Package tutorial overlay on first-empty state | `SHIPPER_TAB_PACKAGES.md:32` |
| Messages | Manual refresh button in header (iOS arrow.clockwise) — Android has pull-to-refresh only | `SHARED_TAB_MESSAGES.md:20,125` |
| Messages | "Match Info" button on conversation thread header (opens `MatchDetailsSheet`) | `SHARED_TAB_MESSAGES.md:43,90` |

### Minor (copy / polish)

- Shipper Explore empty-state CTA copy: iOS "Post shipment request" vs Android "Create Package Request"
- Carrier My Trips FAB visibility gate — iOS hides FAB on empty (own CTA); Android shows both
- Messages empty-state copy is more verbose on iOS
- Counter-offer detection fallback: iOS has a `DeliveryMatch.isCounterOfferInferred` heuristic; Android relies on the explicit flag only

### Intentional deviations (NOT gaps — kept here so the next audit doesn't re-raise them)

| iOS doc claim | Android disposition | Where decided |
|---|---|---|
| `RoleSwitcherSection` between header and stats | Removed — top-bar `SwapHoriz` is the single source | `20-profile-tab.md` § Role switching |
| Standalone `CarrierStatusCard` | Folded into header `CarrierActiveBadge` | `20-profile-tab.md` § Section order |
| Messages `conversation.userRole` filtering | Removed — iOS code itself dropped this; the doc page is stale | Earlier session note in `IMPLEMENTATION-STATUS.md` |
| `BookingsMenuSection.My Matches → MatchListView` row | Skipped — duplicate of the primary Matches tab | `20-profile-tab.md` |
| `UserHeaderCard` on shipper / carrier explore | Removed — top-bar shows name + role + badge | `20-profile-tab.md`, recent session decision |
| `ShipperRecentSearchesSectionView` | Tracked as separate Recent Searches follow-up | Shipper feature row in `IMPLEMENTATION-STATUS.md` |
| Profile badge source (notifications vs `attention.total`) | Wired to `attention.total` per `/me/attention` | `20-profile-tab.md` § Attention signals |

### Suggested priority for follow-up PRs

1. **Shipper Packages filter chips (A6)** — Android has zero filter UI; biggest perceived gap.
2. **Pickup/Delivery code sheet wiring (A2)** — code sheets exist; just need callback plumbing.
3. **TripStatusUpdateSheet entry point (B7)** — sheet exists, just needs trigger.
4. **Counter Offers chip + snackbar stacking (A1, B2)** — same area, single PR.
5. **Live Delivery Tracking + Share with Receiver + Find Packages sheets (A3, A4, A5)** — three new sheets, larger scope.
6. **Smaller polish (B1, B3, B4, B5, B6, B8, B9, B10, B11, B12, B13)** — bundle by area.
