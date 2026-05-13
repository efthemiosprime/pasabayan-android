# Trips — iOS↔Android Parity Review

**Date:** 2026-05-13
**Branch:** `feature/initial-android-spec`
**Scope:** Phase 1 — Carrier trips (create, edit, browse, details, status updates), shipper-side trip browsing, popular routes, route activity
**Spec:** [`03-trips.md`](03-trips.md)
**iOS reference:** `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Trips/`
**Android impl:** `app/src/main/java/com/efthemiosprime/pasabayan/features/trips/` + `core/network/.../trips/`

---

## Executive Summary

Trips is the **healthiest of the three reviewed features so far** (vs. [Bookings](BOOKINGS-PARITY-REVIEW.md), [Packages](PACKAGES-PARITY-REVIEW.md)). Models, services, ViewModels, and most screens reach 85–100% parity. Android out-decomposes iOS: 30 model files (vs. iOS' 3) split out form state, validators, mappers, sort/filter/template DTOs, and earnings — a healthy DRY pattern. File counts: **iOS 28 / Android 61** (Android leads on count; iOS leads on LOC because iOS bundles enormous SwiftUI bodies into single files).

Three real gaps and one architectural divergence:

1. **❌ `POST /trips/{id}/activate` endpoint missing on Android.** iOS has a dedicated method with a comment that reads "the only sanctioned way to activate." Android's `TripsApi.kt` exposes no `activate` route, and `TripsRepositoryImpl.kt` does not send a `trip_status` field via `PUT /trips/{id}` as a workaround. **Carriers cannot activate planning trips on Android.** Verified by reading `TripsApi.kt` and grepping the repo.
2. **❌ `EditTripSheet` is severely under-built — 161 LOC vs. iOS' 1,593 (~10×).** Android only edits weight + notes. iOS edits the full route (origin/destination cities + countries), capacity (weight + space), pricing (pricePerKg / flatTripPrice with branch logic), shared pickup/delivery schedule, dates, and exposes activate + cancel-with-blocking-match-detection actions.
3. **❌ `CreateTripFromPackageViewModel` shrinks from 565 LOC to 86 LOC**, missing template-population fields and form-state binding. Some of this is intentional (Compose holds form state in `TripFormState.kt` rather than the VM), but the VM is missing `populateFormFromTemplate`-equivalent behavior and the derived state iOS exposes via `@Published` properties.
4. **⚠️ Architectural divergence (not a gap):** Android puts form state in `model/TripFormState.kt` + composable Screen state, with validation in `model/TripFormValidator.kt`. iOS puts it inline in ViewModels. Both work; Android's is more testable. Documented but not actionable.

The remaining differences in big-file LOC ratios (BrowseTripsView 18%, TripDetailsView 41%, TripCreationView 40%) are largely **Compose terseness + Android-side decomposition**, not feature gaps.

---

## 1. Models / DTOs

### 1a. Feature-level Domain Models

| iOS File | Android Equivalent | Status | Notes |
|---|---|---|---|
| `Models/Trip.swift` (1,480 LOC) | `model/Trip.kt` + `model/TripMapper.kt` + ~15 sibling models | ✅ | Decomposed across many files; iOS' fallback decoder logic moved to mapper |
| `Models/TripModels.swift` (406) | `core/network/.../trips/TripJsonModels.kt` + `TripMatchJsonModels.kt` + `RouteActivityJsonModels.kt` | ✅ | JSON-layer split |
| `Models/TripPackagesFilter.swift` (71) | `model/TripPackagesFilter.kt` | ✅ |
| iOS nested `PendingTripRequest` | `model/PendingTripRequest.kt` | ✅ |
| iOS nested `TripEarningsBreakdown` | `model/TripEarningsBreakdown.kt` | ✅ |
| iOS enums `PricingType`, `PricingMethod`, `TransportationMethod`, `TripStatus` | `core/domain/enum/*` | ✅ |

**Android-only models** (healthy extractions):

- `model/AvailableTripsPage.kt` — paginated browse model
- `model/CarrierExploreDropdownState.kt` — explore dropdown state
- `model/CarrierTripActionPolicy.kt` — action gating policy
- `model/PopularRoute.kt` — popular routes data
- `model/RouteActivitySummary.kt` — route activity model
- `model/TripCityAutocomplete.kt` — city autocomplete suggestions
- `model/TripCompatibilityResult.kt` — compatibility scoring result
- `model/TripCreationFlowState.kt` — multi-step form state
- `model/TripFilter.kt`, `model/TripSortOption.kt` — query/sort models (iOS inlines)
- `model/TripFormState.kt`, `model/TripFormValidator.kt`, `model/TripValidationError.kt` — extracted form state + validation
- `model/TripMapper.kt` — DTO → domain
- `model/TripMatchPackage.kt` — match package model
- `model/TripTemplateData.kt` — template loading state

**No missing models detected.**

---

## 2. Services / Repositories

### 2a. Endpoint parity

| iOS Method (TripsAPIService / RoutesAPIService) | Android Equivalent | Status |
|---|---|---|
| `getTrips()` (carrier) | `TripsApi.getTrips()` / `getCarrierTrips()` | ✅ |
| `getAvailableTripsPage(...)` | `TripsApi.getAvailableTrips(QueryMap)` | ✅ |
| (implicit `getTrip(id)`) | `TripsApi.getTrip(id)` | ✅ |
| (implicit `getTripMatches(id)`) | `TripsApi.getTripMatches(id)` | ✅ |
| `createTrip(request)` | `TripsApi.createTrip(body)` | ✅ |
| `updateTrip(id, request)` | `TripsApi.updateTrip(id, body)` | ✅ |
| **`activateTrip(id)` → `POST /trips/{id}/activate`** | **— MISSING** | ❌ Confirmed missing in `TripsApi.kt`. No `trip_status` workaround in `TripsRepositoryImpl.kt`. iOS comment: "the only sanctioned way to activate." |
| `deleteTrip(id)` / `cancelTrip(id)` | `TripsApi.deleteTrip(id)` | ✅ |
| `RoutesAPIService.getPopularPackageRoutes()` | `TripsApi.getPopularPackageRoutes()` | ✅ |
| `getRouteActivitySummary()` | `TripsApi.getRouteActivitySummary()` | ✅ |
| `getTripTemplate(packageId)` | `TripsApi.getTripTemplate(packageId)` | ✅ |

### 2b. Service files

| iOS File | Android Equivalent | Status |
|---|---|---|
| `Services/TripsAPIService.swift` (461) | `services/TripsRepositoryImpl.kt` (305) + `core/network/.../trips/TripsApi.kt` (51) | ⚠️ Endpoint coverage 11/12 — see §2a |
| `Services/RoutesAPIService.swift` (53) | folded into `TripsRepositoryImpl.kt` | ✅ |
| `Services/CarrierDisclaimerStore.swift` | `services/CarrierDisclaimerStore.kt` | ✅ |
| `Services/CarrierPreferencesFormStore.swift` | `services/CarrierPreferencesFormStore.kt` | ✅ |
| `Services/SavedRouteTemplatesStore.swift` | `services/SavedRouteTemplatesStore.kt` | ✅ |
| `Services/UsualTransportStore.swift` | `services/UsualTransportStore.kt` | ✅ |

**Android-only services** (healthy):

- `services/TripsRepository.kt` — interface
- `services/TripsModule.kt` — Hilt bindings
- `services/CreateTripFromPackageUseCase.kt` — use-case wrapper
- `services/TripsLocalStateCoordinator.kt`, `services/TripsLocalStateUpdater.kt` — local cache coordination
- `services/TripTutorialStore.kt` — tutorial dismissed flag

---

## 3. ViewModels

### 3a. File parity

| iOS File | Android Equivalent | Status |
|---|---|---|
| `ViewModels/BrowseTripsViewModel.swift` (435) | `viewmodel/BrowseTripsViewModel.kt` (392) | ✅ ~90% parity |
| `ViewModels/CreateTripFromPackageViewModel.swift` (565) | `viewmodel/CreateTripFromPackageViewModel.kt` (86) + `model/TripFormState.kt` + `model/TripFormValidator.kt` + `viewmodel/TripCreationViewModel.kt` (117) | ⚠️ 15% direct, ~70% w/ delegated form state — **template-population gap** (see §3b) |
| `ViewModels/PopularRoutesViewModel.swift` (65) | `viewmodel/RouteActivitySummaryViewModel.kt` (47) | ✅ |
| `ViewModels/TripPackageProgressViewModel.swift` (169) | `viewmodel/TripPackageProgressViewModel.kt` (83) | ✅ |
| — | `viewmodel/CarrierTripsViewModel.kt` (259) | ✅ Android-only — carrier my-trips list |
| — | `viewmodel/CarrierPreferencesFormViewModel.kt` (17) | ✅ Android-only |
| — | `viewmodel/TripCreationSavedRoutesViewModel.kt` (33) | ✅ Android-only |
| — | `viewmodel/TripsLocalStateViewModel.kt` (33) | ✅ Android-only — local cache surface |

### 3b. Method-level gap — `CreateTripFromPackageViewModel`

| iOS Method / `@Published` | Android Equivalent | Status |
|---|---|---|
| `loadTemplate()` | `loadTemplate(packageId)` | ✅ |
| `populateFormFromTemplate(tripData)` | — (form state is local to Screen) | ⚠️ Behavior moved to UI; needs verification that all template fields populate correctly |
| `saveTrip()` | `createTrip(request, userId?)` | ✅ |
| `clearFormData()` | `clearCreatedTrip()` | ⚠️ Different semantic — confirm clears all form fields |
| `shouldShowRequestToCarryAlert()` | — | ❌ MISSING |
| `consumeRequiresPhoneVerification()` | `consumeRequiresPhoneVerification()` | ✅ |
| `@Published` form fields (departureDate, arrivalDate, availableWeight, availableSpace, pricePerKg, transportationMethod, specialNotes, pickupAddress, dropoffAddress, sharedPickupDate, sharedDeliveryDate, isLoading, isSaving, errorMessage, showSuccess, showPhoneVerificationBlockedSheet) | Distributed across `TripFormState.kt` + Screen `remember` state + `TripCreationUiState` | ⚠️ Architectural difference |

**Net:** Android moved form state to model + Screen — sound design, but `populateFromTemplate` and `shouldShowRequestToCarryAlert` need an explicit Android implementation or confirmation they're handled elsewhere.

---

## 4. Views / Screens

### 4a. Mapping

| iOS View | Android Equivalent | iOS LOC | Android LOC | Status |
|---|---|---|---|---|
| `Views/BrowseTripsView.swift` | `ui/BrowseTripsScreen.kt` | 798 | 144 | ✅ Compose terseness — VM at 90% (§3a) |
| `Views/TripCreationView.swift` | `ui/TripCreationScreen.kt` + `components/TripCreationBaseScaffold.kt` | 1,715 | 680 | ⚠️ ~90% — passenger-mode conditional UI needs verification |
| `Views/CreateTripFromPackageView.swift` | `ui/CreateTripFromPackageScreen.kt` | 1,217 | 546 | ✅ ~90% w/ form state delegated to model |
| `Views/TripDetailsView.swift` | `ui/TripDetailsScreen.kt` (813) + `ui/TripDetailsSectionsModel.kt` (120) + `ui/TripMatchPackageCard.kt` (304) + `ui/CarrierEarningsSection.kt` (119) | 1,962 | 1,356 combined | ✅ ~70% structurally complete; pricing breakdown viz simplified |
| `Views/EditTripSheet.swift` | `ui/EditTripSheet.kt` | 1,593 | 161 | ❌ **MAJOR GAP** — see §4c |
| `Views/TripStatusUpdateSheet.swift` | `ui/TripStatusUpdateSheet.kt` | 680 | 381 | ✅ ~56% — confirm parity of status options + transitions |
| `Views/SavedRoutesSheet.swift` | `components/SavedRoutesSheet.kt` | 57 | (small) | ✅ |
| `Views/TripUpdateTimeoutCoordinator.swift` | folded into `services/TripsLocalStateCoordinator.kt` | 34 | — | ✅ moved |
| — | `ui/CarrierMyTripsScreen.kt` | — | 261 | ✅ Android-only — dedicated carrier list |
| — | `ui/CarrierEarningsSection.kt` | — | 119 | ✅ Android-only — extracted from details |
| — | `ui/TripFilterSheet.kt` | — | 58 | ✅ |

### 4b. Components

| iOS Components/ | Android components/ | Status |
|---|---|---|
| `TripCard.swift` (388) | `components/TripCard.kt` | ✅ |
| `TripFilterSheet.swift` (147) | `components/TripFilterContent.kt` (+ `ui/TripFilterSheet.kt` wrapper) | ✅ |
| `TripPackageProgressComponents.swift` (219) | `components/TripPackageProgressWidget.kt` + `components/TripCardProgressSection.kt` | ✅ |
| `TripStatusBadge.swift` (107) | `components/TripStatusBadgeConfig.kt` | ✅ |
| `TripTutorialOverlay.swift` (448) | `components/TripTutorialOverlay.kt` | ✅ |

**Android-only components** (healthy):

- `components/TripCreationBaseScaffold.kt` — reusable form scaffold for create + edit + create-from-package
- `components/TripDateTimePicker.kt` — extracted picker
- `components/CarrierTripActions.kt` — carrier action buttons
- `components/CarrierTripDisclaimerSheet.kt` — disclaimer sheet
- `components/SavedRoutesSheet.kt` — moved from `ui/`

### 4c. `EditTripSheet` gap detail

iOS `EditTripSheet.swift` (1,593 LOC) covers:

1. **Trip status display** with conditional editability gating (planning vs. active/completed)
2. **Trip Info section** — origin city w/ autocomplete + country picker, destination city + country, departure date, arrival date
3. **Shared pickup/delivery schedule** (optional — `pickupDate`, `deliveryDate`)
4. **Capacity** — `availableWeight`, `availableSpace`
5. **Pricing** — `pricePerKg` OR `flatTripPrice` (mutually exclusive based on `pricingType`)
6. **Special Notes**
7. **Actions** — Save, Cancel, **Activate** (planning only), **Cancel Trip** (with blocking-match detection alert)
8. **Alert state machine** — `showActivateConfirmation`, `showCancelConfirmation`, `showSuccessAlert`, `showError`, `showActivateErrorAlert`, `showCancelErrorAlert`

Android `ui/EditTripSheet.kt` (161 LOC) covers:

1. Lock icon + explanation if route is locked
2. Weight field (editable if not locked)
3. Notes field (always editable)
4. Save + Cancel buttons

**Missing on Android:** route editing (origin/destination cities + countries), date editing (departure, arrival, shared pickup/delivery), space capacity, pricing edit (pricePerKg / flatTripPrice with branch logic), trip-status picker, activate action, cancel-trip action with blocking-match detection.

This is **the largest gap of the three reviewed features**. Likely intentional MVP scope on Android, but needs product confirmation. If full parity is required, budget ~500 LOC for sections + validation + alerts.

---

## 5. Top Gaps — Ranked

| # | Gap | Severity | Files involved | Slice scope |
|---|---|---|---|---|
| 1 | **`POST /trips/{id}/activate` missing — carriers cannot activate planning trips** | HIGH | `core/network/.../trips/TripsApi.kt`, `TripsRepositoryImpl.kt`, `TripsRepository.kt`, VM | Add `@POST("trips/{id}/activate")` + repo method + VM trigger + tests. Small slice. |
| 2 | **`EditTripSheet` route/dates/capacity/pricing editing + activate + cancel-with-blocking-matches** | HIGH | `ui/EditTripSheet.kt` (extend ~10×); reuse `TripFormState`, `TripFormValidator`, `components/TripCreationBaseScaffold.kt` | Multi-slice. Sections in order: route → dates → capacity → pricing → status picker → activate action → cancel action with blocking-match detection. |
| 3 | **`populateFormFromTemplate` behavior in `CreateTripFromPackageScreen`** | MEDIUM | `viewmodel/CreateTripFromPackageViewModel.kt`, `model/TripTemplateData.kt`, `ui/CreateTripFromPackageScreen.kt` | Verify all template fields (dates, capacity, prices, addresses) populate `TripFormState` on load. Add tests. |
| 4 | **`shouldShowRequestToCarryAlert` — alert gating logic** | MEDIUM | `viewmodel/CreateTripFromPackageViewModel.kt` | Port iOS gating rule + UI alert; tests. |
| 5 | **`TripCreationScreen` passenger-mode conditional UI** | MEDIUM | `ui/TripCreationScreen.kt` | iOS conditionally shows passenger fields (capacity, price-per-passenger, requirements, amenities, age restrictions) when transport method = passenger-capable. Verify Android matches; add if missing. |
| 6 | **`TripStatusUpdateSheet` parity check** | MEDIUM | `ui/TripStatusUpdateSheet.kt` | 56% LOC ratio — confirm all status options + transitions match iOS; add missing transitions/error states. |
| 7 | **`TripDetailsScreen` pricing-breakdown visualization** | LOW | `ui/TripDetailsScreen.kt` or `components/` | iOS shows distance multiplier + base price + calculated price; Android shows raw values. Polish. |
| 8 | **`clearFormData` semantic gap** | LOW | `viewmodel/CreateTripFromPackageViewModel.kt` | Confirm `clearCreatedTrip()` clears all form state, not just the success/result field. |
| 9 | **`TripCreationViewModel` sharedPickupDate/sharedDeliveryDate fields** | LOW | `viewmodel/TripCreationViewModel.kt`, `model/TripFormState.kt` | iOS exposes shared schedule binding; verify Android `TripFormState` carries these and `TripCreationScreen` exposes them. |
| 10 | **Earnings breakdown visualization in details** | LOW | `ui/CarrierEarningsSection.kt` | Polish — match iOS visualization (multiplier, base, calculated). |

---

## 6. Implementation Plan (suggested slice order)

Per [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md) and the project's slice-by-layer convention:

1. **Slice A — `POST /trips/{id}/activate`** *(top-priority bug fix)*
   - Add `@POST("trips/{id}/activate")` to `TripsApi.kt` + JSON contract test
   - `TripsRepository.activateTrip(id)` + Impl + repo unit tests
   - Wire VM trigger (e.g., `EditTripSheet` activate button — but see Slice B for full sheet)
   - For now: minimum-viable activate flow exposed via `CarrierMyTripsScreen` quick action
   - `feat(trips): add POST /trips/{id}/activate endpoint and carrier activate action`

2. **Slice B — `EditTripSheet` expansion (route + dates + capacity + pricing)** *(largest gap)*
   - Sub-slice B1: route editing (origin/destination city + country) using `TripCityAutocomplete`
   - Sub-slice B2: date editing (departure, arrival, shared pickup/delivery) using `components/TripDateTimePicker.kt`
   - Sub-slice B3: capacity editing (`availableWeight`, `availableSpace`)
   - Sub-slice B4: pricing editing (pricePerKg / flatTripPrice with `TripFormValidator` branch)
   - Sub-slice B5: trip-status picker + activate action (reuses Slice A)
   - Sub-slice B6: cancel-trip action + blocking-match-detection alert
   - Each sub-slice: tests + `@Preview` (light + dark) + strings (EN + FR)
   - `feat(trips): expand EditTripSheet to edit route/dates/capacity/pricing/status`

3. **Slice C — `populateFormFromTemplate` + `shouldShowRequestToCarryAlert`**
   - Verify `CreateTripFromPackageScreen` populates `TripFormState` from `TripTemplateData` for all fields
   - Add `shouldShowRequestToCarryAlert` logic to VM + UI alert
   - Tests
   - `feat(trips): port template population and request-to-carry alert from iOS`

4. **Slice D — `TripCreationScreen` passenger-mode conditional UI**
   - Audit transport-method enum cases on Android vs. iOS
   - Add passenger fields + conditional rendering
   - `TripFormValidator` extension for passenger fields
   - `feat(trips): conditionally render passenger-mode fields in trip creation`

5. **Slice E — `TripStatusUpdateSheet` audit**
   - Diff iOS status options + transitions vs. Android
   - Add missing transitions/error states
   - `feat(trips): add missing status transitions and error states to status update sheet` (or `refactor` if no behavior change)

6. **Slice F — Polish** *(low priority)*
   - Pricing breakdown visualization in `TripDetailsScreen`
   - Earnings breakdown polish in `CarrierEarningsSection`
   - `clearFormData` semantic clarification
   - `ui(trips): polish pricing + earnings breakdown visualization`

Each slice ends with a stop for user commit per [`.cursor/rules/phase-commit-workflow.mdc`](../.cursor/rules/phase-commit-workflow.mdc).

---

## 7. Notes on Architectural Divergences (not gaps)

These are documented to prevent future "let's port them back to ViewModel" suggestions:

- **Form state in `model/TripFormState.kt` + Screen state, not in ViewModel.** iOS uses `@Published` properties on the VM; Android uses an immutable `TripFormState` data class held by `TripCreationViewModel`/Screen. Android's pattern is more testable (validator + state are pure) and idiomatic Compose.
- **`TripFormValidator.kt` extracted as a pure object.** iOS scatters validation in VM methods. Don't merge back.
- **`TripsLocalStateCoordinator` + `TripsLocalStateUpdater` cache layer.** iOS implicit; Android explicit. Lets us evict/refresh deterministically.
- **Multiple ViewModels per screen surface.** Android splits `CarrierTripsViewModel`, `CarrierPreferencesFormViewModel`, `TripCreationSavedRoutesViewModel`, `TripsLocalStateViewModel`. iOS uses fewer VMs with more state. Don't consolidate.
- **`CarrierMyTripsScreen.kt` + `CarrierEarningsSection.kt` are Android-only.** iOS folds these into `TripDetailsView`. Both are deliberate.

---

## 8. Cross-references

- Spec: [`03-trips.md`](03-trips.md)
- Phases: [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md)
- Status: [`IMPLEMENTATION-STATUS.md`](IMPLEMENTATION-STATUS.md)
- Coverage matrix: [`FEATURE-COVERAGE-MATRIX.md`](FEATURE-COVERAGE-MATRIX.md)
- Design system: [`14-design-system.md`](14-design-system.md)
- Sibling parity reviews: [`BOOKINGS-PARITY-REVIEW.md`](BOOKINGS-PARITY-REVIEW.md), [`PACKAGES-PARITY-REVIEW.md`](PACKAGES-PARITY-REVIEW.md), [`PAYMENTS-PARITY-REVIEW.md`](PAYMENTS-PARITY-REVIEW.md)
