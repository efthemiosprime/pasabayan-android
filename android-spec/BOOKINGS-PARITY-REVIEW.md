# Bookings — iOS↔Android Parity Review

**Date:** 2026-05-13
**Branch:** `feature/initial-android-spec`
**Scope:** Phase 3 — Bookings, matches, counter-offers, live tracking, codes
**Spec:** [`05-bookings-matches.md`](05-bookings-matches.md)
**iOS reference:** `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Bookings/`
**Android impl:** `app/src/main/java/com/efthemiosprime/pasabayan/features/bookings/`

---

## Executive Summary

Phase 3 (Bookings) is **core-complete, discovery-incomplete**. The match lifecycle — list, details, request-to-carry, counter-offer, auto-charge, pickup/delivery codes, live tracking — is at parity with iOS. The gap is concentrated in **discovery, history, rating, and onboarding-gate UIs** that on iOS allow shippers to browse trips, carriers to browse packages, and either side to view past deliveries or rate after completion.

File counts: **iOS 89 / Android 62** (~70% by file count). The deficit is almost entirely in the View layer; Models, Services, and primary ViewModels are at parity or better (Android extracts more reusable utilities).

Five gap clusters, ranked by user impact:

1. **Discovery surfaces missing** — `CompatibleTripsView` (shipper browses carrier trips), `CarrierBrowsePackagesView` (carrier browses shipper packages), `CompatiblePackagesForTripView`, `DirectBookingSheet`. Without these, neither role can initiate matches by browsing on Android — they depend on inbound notifications / external links.
2. **History view missing** — `DeliveryHistoryView` + `HistoryRowCard`. No way to browse completed deliveries.
3. **Post-delivery rating missing** — `RateDeliverySheet` + flow.
4. **Carrier Stripe onboarding gate missing** — `CarrierOnboardingPrompt` model + `CarrierOnboardingRequiredSheet`. Carriers without Stripe can accept matches in code paths that iOS gates.
5. **Receiver share + secondary banners missing** — `ShareWithReceiverSheet` (+ ViewModel), `RouteInformationCard`, `ChatAccessCard`, `LiabilityWarningBanner`, `UrgencyBadge`, extracted `FilterChip`/`StatusFilterChip`.

`IMPLEMENTATION-STATUS.md` should reflect this as "core lifecycle complete; discovery, history, rating, and onboarding gate pending."

---

## 1. Models / DTOs

### 1a. Feature-level Domain Models — ✅ At parity, with healthy DRY extractions

| iOS File | Android Equivalent | Status | Notes |
|---|---|---|---|
| `Booking.swift` | `model/DeliveryMatch.kt` | ✅ | Core match model, rich nested fields |
| `BookingModels.swift` | scattered across `model/*.kt` | ⚠️ | Request/response types not consolidated; Android splits by responsibility |
| `MatchingModels.swift` | scattered across `model/*.kt` + `model/nested/*` | ⚠️ | Same — split rather than monolithic file |
| `CounterOfferContext.swift` | `model/CounterOfferContext.kt` | ✅ | |
| `IncomingRequestContext.swift` | `model/IncomingRequestContext.kt` | ✅ | |
| `ReceiptModels.swift` | `model/MatchReceipt.kt` | ✅ | |
| `ReceiverAccessModels.swift` | `model/ReceiverAccessToken.kt` | ✅ | |
| `CarrierOnboardingPrompt.swift` | — | ❌ | Stripe onboarding prompt state not modeled |

**Android-only models** (utility / result wrappers — not gaps):
- `BookingMapper.kt` — JSON→domain mapping
- `BookingStats.kt` — aggregated metrics
- `BookingStatusContext.kt` — status-specific UI context
- `TrackingMath.kt` — distance/ETA calculations
- `OverageConfirmationData.kt` — capacity overage state
- `RequestMatchResult.kt`, `ConfirmMatchResult.kt`, `CancelMatchResult.kt` — typed result wrappers
- `NegotiationMetadata.kt` — counter-offer round tracking
- `model/nested/*` (8 files) — `CarrierTripInfo`, `CurrentLocationData`, `DeliveryAddressData`, `DirectBookingData`, `PackageCompatibility`, `PackageRequestInfo`, `RefundResult`, `TripAvailability`, `TripCapacity`

---

## 2. Services / Repositories — ✅ Complete surface

| iOS File | Android Equivalent | Status | Notes |
|---|---|---|---|
| `BookingsAPIService.swift` | `services/BookingsRepositoryImpl.kt` | ✅ | API orchestration |
| `BookingsAPIServicing.swift` | `services/BookingsRepository.kt` | ✅ | Interface |
| `CounterOfferErrorMapper.swift` (root) | `services/CounterOfferErrorMapper.kt` | ✅ | Error → domain mapping |

**Android-only services** (reasonable additions):
- `BookingsModule.kt` — Hilt DI bindings
- `MatchReceiptRepository.kt` + `MatchReceiptRepositoryImpl.kt` — receipt persistence
- `CounterOfferErrorMessages.kt` — localized error message helper
- `CounterOfferPromptValidator.kt` — counter-offer submission validation

---

## 3. ViewModels

| iOS File | Android Equivalent | Status |
|---|---|---|
| `MatchingViewModel.swift` | `viewmodel/MatchingViewModel.kt` | ✅ |
| `LiveTrackingViewModel.swift` | `viewmodel/LiveTrackingViewModel.kt` | ✅ |
| `AutoChargeConfirmationViewModel.swift` | `viewmodel/AutoChargeConfirmationViewModel.kt` (+ `AutoChargeConfirmationState.kt`) | ✅ |
| `CarrierBrowsePackagesViewModel.swift` | — | ❌ MISSING |
| `ShareWithReceiverViewModel.swift` | — | ❌ MISSING |

---

## 4. Views / Screens

### 4a. Match lifecycle (✅ at parity)

| iOS | Android | Status |
|---|---|---|
| `MatchListView` / `BookingListView` | `ui/MatchListScreen.kt` | ✅ |
| `ShipperMatchesView` | `ui/MatchListScreen.kt` (shipper role) | ✅ |
| `CarrierMatchRequestsView` | `ui/MatchListScreen.kt` (carrier role) | ✅ |
| `ShipperMatchDetailsView` | `ui/ShipperMatchDetailsSheet.kt` | ✅ |
| `CarrierMatchDetailsView` | `ui/ShipperMatchDetailsSheet.kt` | ⚠️ Sheet, not full screen |
| `MatchCreationView` / `RequestToCarrySheet` | `ui/RequestToCarrySheet.kt` + `ui/ShipperMatchCreationSheet.kt` | ✅ |
| `BookingSuccessView` | `ui/BookingSuccessScreen.kt` | ✅ |
| `AutoChargeConfirmationSheet` | `ui/AutoChargeConfirmationSheet.kt` | ✅ |
| `CounterOfferPromptView` | `ui/CounterOfferPromptSheet.kt` | ✅ |
| `GeneratePickupCodeView` | `ui/GeneratePickupCodeScreen.kt` | ✅ |
| `ConfirmDeliveryCodeView` | `ui/ConfirmDeliveryCodeScreen.kt` | ✅ |
| `GenerateDeliveryCodeView` | folded into `LiveTrackingViewModel` flow | ⚠️ Partial |
| `BookingDetailsView` / `BookingDetailsContentView` | folded into `ShipperMatchDetailsSheet` | ⚠️ Partial |

### 4b. Discovery / browsing (❌ MISSING — top gap)

| iOS | Android | Notes |
|---|---|---|
| `CompatibleTripsView` (+ `CompatibleTripsPricingPolicy`) | — | Shipper browses carrier trips compatible with a package |
| `CarrierBrowsePackagesView` (+ ViewModel) | — | Carrier browses available shipper packages |
| `CompatiblePackagesForTripView` | — | Carrier views packages compatible with a specific trip |
| `DirectBookingSheet` | — | Space-only direct booking flow |
| `RequestToCarryBudgetReminder` | — | Budget alert banner during request creation |

### 4c. History (❌ MISSING)

| iOS | Android |
|---|---|
| `DeliveryHistoryView` | — |
| `HistoryRowCard` | — |

### 4d. Rating (❌ MISSING)

| iOS | Android |
|---|---|
| `RateDeliverySheet` | — |

### 4e. Receiver sharing (❌ MISSING)

| iOS | Android |
|---|---|
| `ShareWithReceiverSheet` | — |
| `ShareWithReceiverViewModel` | — |

### 4f. Carrier onboarding gate (❌ MISSING)

| iOS | Android |
|---|---|
| `CarrierOnboardingRequiredSheet` | — |
| `CarrierOnboardingPrompt` model | — |

### 4g. iOS-only (test harnesses + previews — not gaps)

`CompatibleTripsFlowUITestHarnessView`, `RequestToCarryFlowUITestHarnessView`, `BookingActionsUITestHarnessView`, `CounterOfferBannerUITestHarnessView`, `AutoChargeConfirmationUITestHarnessView`, `CarrierBrowsePackagesViewPreviews`, `RequestToCarrySheetPreviews`, `ShipperMatchesViewPreviews`. These are SwiftUI dev-only artifacts; Android equivalents are `@Preview` composables in the same files.

---

## 5. Components

### 5a. At parity (✅)

| iOS | Android |
|---|---|
| `MatchCard` / `CarrierRequestCard` / `ShipperMatchCard` | `components/MatchCard.kt` (consolidated, role-agnostic) |
| `BookingStatusBadge` / `MatchStatusBadge` | `components/MatchStatusBadgeConfig.kt` |
| `CounterOfferStatusBanner` | `components/CounterOfferBanner.kt` |
| `CounterOfferSnackbar` | `components/CounterOfferSnackbar.kt` |
| `IncomingRequestSnackbar` | `components/IncomingRequestSnackbar.kt` |
| `BookingPickupCodeView` / `BookingDeliveryCodeView` | `components/PickupDeliveryCodeView.kt` |
| `PriceComparisonSection` | `components/PriceComparisonSection.kt` |

### 5b. Missing or partial (❌ / ⚠️)

| iOS | Android | Status | Notes |
|---|---|---|---|
| `BookingActions` | — | ⚠️ | Action buttons inlined in sheets/screens; not extracted as a reusable component |
| `BookingCard` + 6 subsections (`BookingCardHeader`, `BookingCardRouteSection`, `BookingCardShipperSection`, `BookingCardQuickActions`, `BookingCardDetailsSection`, `BookingLocationRow`) | — | ❌ | iOS decomposes the card; Android uses a single `MatchCard.kt`. Decomposition only becomes valuable once discovery screens land. |
| `TripWithRequestsCard` | — | ❌ | Shipper-side card aggregating requests for a trip |
| `ChatAccessCard` | — | ❌ | Inline chat shortcut on detail surfaces |
| `RouteInformationCard` | — | ❌ | Origin/destination route summary |
| `LiabilityWarningBanner` | — | ❌ | Liability disclaimer banner |
| `AutoChargeFailureBanner` | — | ⚠️ | Failure state surfaced via error path, not dedicated banner |
| `UrgencyBadge` | — | ❌ | Visual urgency indicator on cards |
| `FilterChip` / `StatusFilterChip` | — | ⚠️ | Filtering exists in list logic; chip UI not extracted |
| `CarrierOnboardingRequiredSheet` | — | ❌ | (Also listed under Views §4f) |

### 5c. Android-only (✅ additions, not gaps)

- `OverageConfirmationDialog.kt` — capacity overage confirmation modal
- `OverCapacityBanner.kt` — banner for over-capacity state

---

## 6. Top Gaps — Ranked

| # | Gap | Severity | Files involved | Slice scope |
|---|---|---|---|---|
| 1 | Shipper trip discovery | HIGH | `CompatibleTripsView`, `CompatibleTripsPricingPolicy` | New screen + reuses existing ViewModel surface; pricing policy may need new mapper |
| 2 | Carrier package discovery | HIGH | `CarrierBrowsePackagesView`, `CarrierBrowsePackagesViewModel`, `CompatiblePackagesForTripView` | New screen + ViewModel + repo method |
| 3 | Delivery history view | HIGH | `DeliveryHistoryView`, `HistoryRowCard` | New screen, likely reuses `BookingsRepository.list*` with status filter |
| 4 | Carrier Stripe onboarding gate | MEDIUM | `CarrierOnboardingPrompt`, `CarrierOnboardingRequiredSheet` | Cross-cuts payments — depends on Phase 4 Stripe Connect status |
| 5 | Post-delivery rating flow | MEDIUM | `RateDeliverySheet` | New sheet + ViewModel; depends on `:ratings` (Phase 6 / spec `11-favorites-ratings.md`) |
| 6 | Receiver share link | MEDIUM | `ShareWithReceiverSheet`, `ShareWithReceiverViewModel` | New sheet + ViewModel; uses existing `ReceiverAccessToken` model |
| 7 | Direct booking sheet | MEDIUM | `DirectBookingSheet` | Space-only flow, distinct from request-to-carry |
| 8 | `BookingActions` extraction | LOW | refactor inline action buttons into shared component | Refactor — unblocks consistency across discovery + history surfaces |
| 9 | `BookingCard` decomposition | LOW | split `MatchCard.kt` into header/route/shipper/quickactions/details/location subsections | Refactor — value increases once discovery screens exist |
| 10 | Secondary banners/badges | LOW | `RouteInformationCard`, `ChatAccessCard`, `LiabilityWarningBanner`, `UrgencyBadge`, `FilterChip` extraction | Each is a small `:core:designsystem` or feature `components/` add |

---

## 7. Implementation Plan (suggested slice order)

Following [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md) and the project's slice-by-layer convention (DTO → repo → ViewModel → UI), the recommended order:

1. **Slice A — Carrier package discovery** (highest carrier-side impact, fewest cross-feature deps)
   - Verify `BookingsRepository` exposes the package-browse endpoint; add if missing (DTO + tests)
   - Add `CarrierBrowsePackagesViewModel` + state (tests for filtering, pagination)
   - Add `CarrierBrowsePackagesScreen.kt` + `@Preview` (light + dark)
   - Add `CompatiblePackagesForTripScreen.kt`
   - Localized strings in `strings_bookings.xml` + `values-fr/`

2. **Slice B — Shipper trip discovery**
   - Extend repo with compatible-trips endpoint + `CompatibleTripsPricingPolicy` mapper
   - ViewModel
   - `CompatibleTripsScreen.kt` + previews + strings

3. **Slice C — Delivery history**
   - Reuse existing `BookingsRepository` with status filter (verify endpoint parity)
   - `DeliveryHistoryViewModel`
   - `DeliveryHistoryScreen.kt` + `HistoryRowCard.kt` (reusable component) + previews + strings

4. **Slice D — Component decomposition + refactor** (do before E/F so they consume shared components)
   - Extract `BookingActions` composable
   - Decompose `MatchCard.kt` into subsection components (header, route, shipper, quick actions, details, location)
   - Extract `FilterChip` / `StatusFilterChip` (candidate for `:core:designsystem` if reused outside bookings)

5. **Slice E — Receiver share**
   - `ShareWithReceiverViewModel` (uses existing `ReceiverAccessToken`)
   - `ShareWithReceiverSheet.kt` + previews + strings

6. **Slice F — Carrier onboarding gate** (cross-feature — depends on Stripe Connect status from Phase 4)
   - `CarrierOnboardingPrompt` model
   - `CarrierOnboardingRequiredSheet.kt` + previews + strings
   - Wire into match acceptance paths (`AutoChargeConfirmation`, `RequestToCarrySheet`)

7. **Slice G — Post-delivery rating** (depends on Phase 6 ratings spec `11-favorites-ratings.md`)
   - `RateDeliverySheet.kt` + ViewModel + repo wiring
   - Trigger on delivery confirmation success path

8. **Slice H — Direct booking**
   - `DirectBookingSheet.kt` + ViewModel + repo
   - Distinct from request-to-carry (space-only, no negotiation)

9. **Slice I — Secondary banners/badges**
   - `RouteInformationCard`, `ChatAccessCard`, `LiabilityWarningBanner`, `AutoChargeFailureBanner`, `UrgencyBadge`
   - Each gets `@Preview` (light + dark) and goes into feature `components/` (or `:core:designsystem` if reused elsewhere)

Each slice ends with a stop for user commit per [`.cursor/rules/phase-commit-workflow.mdc`](../.cursor/rules/phase-commit-workflow.mdc). Suggested commit type: `feat(bookings): …` for new screens/flows, `refactor(bookings): …` for slices D/I extractions.

---

## 8. Cross-references

- Spec: [`05-bookings-matches.md`](05-bookings-matches.md)
- Phases: [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md)
- Status: [`IMPLEMENTATION-STATUS.md`](IMPLEMENTATION-STATUS.md)
- Coverage matrix: [`FEATURE-COVERAGE-MATRIX.md`](FEATURE-COVERAGE-MATRIX.md)
- Design system: [`14-design-system.md`](14-design-system.md)
- Sibling parity reviews: [`PAYMENTS-PARITY-REVIEW.md`](PAYMENTS-PARITY-REVIEW.md)
