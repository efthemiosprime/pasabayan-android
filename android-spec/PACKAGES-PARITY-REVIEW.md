# Packages — iOS↔Android Parity Review

**Date:** 2026-05-13
**Branch:** `feature/initial-android-spec`
**Scope:** Phase 2 — Packages (shipper requests + service requests/errands), package browsing, multipart create/update
**Spec:** [`04-packages.md`](04-packages.md)
**iOS reference:** `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Packages/`
**Android impl:** `app/src/main/java/com/efthemiosprime/pasabayan/features/packages/` + `core/network/.../packages/`

## Gap implementation status (2026-05-13)

| # | Gap | Status |
|---|-----|--------|
| 1 | Multipart `PUT /packages/{id}` | ✅ Implemented — `PackagesApi.updatePackageMultipart`, `PackagesRepository.updatePackageWithImages`, `PackageViewModel.updatePackageWithImages`, `EditPackageSheet` image picker, JVM tests for serialization. |
| 2 | `pollForProcessedImages` | ✅ Implemented — 3-attempt 2 s back-off poll in `PackageViewModel` (mirrors iOS), unit test using fake API. |
| 3 | Shipper `PackageDetailScreen` expansion | ✅ Implemented — added `PackageImagesCarousel`, `PackageShipperInfoCard`, `PackageCompatibleTripsSummary` components; screen now renders shipper info, image carousel (with processing spinner), compatible-trips summary, full action set, light + dark previews. |
| 4 | Service request — task-mode fields + advanced options | ✅ Implemented — new `Task` direction option for `general_errand`; `taskName` / `taskDescription` fields; payload, mapper, and DTO updated to send `task_name`, `task_description`, `store_lat/lng`, `delivery_lat/lng`. |
| 5 | Interactive map + geocoding | ✅ Implemented — added `com.google.maps.android:maps-compose` + `play-services-maps`; `MAPS_API_KEY` plumbed from `local.properties` via manifest placeholder; new `PackageLocationMap` reusable component (light + dark previews use a stub tile so previews don't require Play Services); new `AddressGeocoder` service wraps the framework `Geocoder` for forward/reverse lookups (no external API key required); `PackageErrandRequestScreen` wires store + delivery map sections with "Use address" / "Clear pin" affordances; `PackageViewModel` exposes `geocodeAddress` / `reverseGeocode`. Tap-to-pin updates coordinates and back-fills the address field via reverse geocode. Caller wires the geocoder via `onGeocodeAddress` / `onReverseGeocode` callbacks. |
| 6 | `checkSimilarPackages` duplicate detection | ✅ Implemented — `PackagesRepository.findSimilarPackages` (client-side filter on `/packages`), `PackageViewModel.checkSimilarPackages`, banner surfaced on `PackageRequestScreen` review step, light + dark previews, VM tests. |
| 7 | `enableShipperRoleIfNeeded` | ✅ Implemented — `PackageViewModel` now consults `AuthRepository`; after a successful package or service-request create, it calls `loadCurrentUser()` so the session reflects the backend-side role activation. Best-effort, silent on failure. VM tests cover both branches. |
| 8 | `PackageRequestScreen` modularization (refactor) | ✅ Implemented — split the 900-line monolith into eight focused `components/` files mirroring the iOS `Views/Components/PackageRequest/` structure: `PickupInformationSection`, `DeliveryInformationSection`, `PackageImagesSection`, `PackageReviewFormSection`, `PackageRequestFormActions`, `SimilarPackagesWarning`, plus shared helpers `CountryAndAddressFields` / `PackageRequestDropdownField` / `PackageRequestDateTimeRow`. Screen down to 511 lines (orchestration + state only). All sections expose `modifier` slots, are state-hoisted, and ship light + dark previews. Behavior-preserving — no functional change. |
| 9 | Package history view | ✅ Implemented — packages-feature owns the screen (shipper-only delivered history). New `PackageHistoryViewModel` calls `bookingsRepository.loadMatches(role="shipper", status="delivered")` and exposes `matches` + `totalSpent`. New `ui/PackageHistoryScreen.kt` renders summary card (count + total spent), reused `MatchCard` list, plus loading / error / empty states. New `components/PackageHistorySummaryCard`. Strings localized en + fr; light + dark previews for all states; 5 VM tests covering happy path, defensive status filter, error, cached-load, and force-refresh. |

---

## Executive Summary

Packages is **list/create-complete, detail/service-request-incomplete**. The shipper package-create flow, package list, browse with filters, edit sheet, and tutorial overlay are at parity. Three significant gaps:

1. **Shipper `PackageDetailScreen` is severely under-built** — 174 lines vs iOS' 1,073-line `PackageDetailView` (+ Helpers / + Modifiers). Missing shipper info card, compatible-trips list, image carousel, full action set.
2. **Service-request (errand) flow is ~30% complete** — Android `PackageErrandRequestScreen.kt` covers shopping list and basic fields, but iOS' `CreateServiceRequestView.swift` (1,319 lines) plus 6 component files in `Components/CreateServiceRequest/` add interactive map, store/delivery location pickers, geocoding, address suggestions, task-specific branching, and timezone-aware date handling.
3. **Package multipart-update + image polling missing** — `PackagesApi.kt` exposes `createPackageMultipart` (POST) but no `@Multipart @PUT` for `updatePackage`. iOS has both, plus a 3-attempt async poll for processed images. Users cannot add or replace images on an existing package.

Two smaller gaps and three "moved-elsewhere" items (not strictly missing):
- iOS `ViewModel` has `checkSimilarPackages` (duplicate detection) and `enableShipperRoleIfNeeded` — both absent on Android.
- iOS `PackageHistoryView` — no Android counterpart.
- iOS `ReceiptUploadView` lives in **packages** on iOS but in **chat** on Android (`features/chat/ui/ChatReceiptUploadSheet.kt`). Architectural choice, not a parity gap, but worth noting for discoverability.
- iOS `DeliveryCardModels` + `DeliveryCard.swift` are a polymorphic unified-card abstraction (variants: standard / compact / package; status types: trip / booking / urgency / service). Android opts for separate per-domain cards (`MatchCard`, `PackageRequestCard`, `CarrierExplorePackageCard`). Architectural divergence, not a missing UI.

File counts: **iOS 38 / Android 39** — Android leads on file count because it extracts validators, mappers, payload models, and filter state; iOS bundles those concerns into larger files. **LOC ratio is the truer signal: ~14k iOS vs ~6k Android.**

---

## 1. Models / DTOs

### 1a. Feature-level Domain Models

| iOS File | Android Equivalent | Status | Notes |
|---|---|---|---|
| `Models/PackageRequest.swift` (2,263 LOC — many nested types) | `model/PackageRequest.kt` (76) + `core/domain` enums + `model/nested` (none here) + `model/PackageImage.kt` + `model/ShoppingItem.kt` | ⚠️ Partial | Android leans on `core/domain/enum/*` for `PackageRequestStatus`, `PackageType`, `UrgencyLevel`. Missing nested types: `CompatibilityDetails` (compatibility scoring for carrier browse), task-specific fields (`taskName`, `taskDescription`), expanded location fields (`pickupLat/Lng`, `compatibilityScore`, `estimatedEarnings`). |
| `Models/DeliveryCardModels.swift` (101 LOC) | — | ❌ MISSING by name; ⚠️ design diverges | iOS unifies booking/trip/package/service into one polymorphic `DeliveryCardData`. Android uses domain-specific cards. **Treat as architectural divergence, not a gap** — but note in the spec if cross-role unified cards are desired. |

**Android-only models** (healthy extractions, not gaps):

- `model/AvailablePackage.kt`, `model/AvailablePackagesPage.kt` — paginated browse model
- `model/PackageBrowseFilter.kt` — explicit filter state (iOS keeps filter state inline in ViewModel)
- `model/PackageFormState.kt`, `model/PackageFormValidator.kt`, `model/PackageValidationError.kt` — extracted form state + validation (iOS inlines)
- `model/PackageMapper.kt` — DTO → domain
- `model/PackageSubmitPayload.kt`, `model/PackageSubmitRequestMapper.kt` — explicit create/update payload
- `model/ServiceRequestSubmitPayload.kt` — service request payload
- `model/ShoppingItem.kt` — shopping list item

---

## 2. Services / Repositories

### 2a. Endpoint parity

| iOS Method | Android Equivalent | Status |
|---|---|---|
| `getPackageRequests()` | `PackagesApi.getPackages()` | ✅ |
| `getPackageRequest(id)` | `PackagesApi.getPackage(id)` | ✅ |
| `getAvailablePackageRequests(...)` / `getAvailablePackageRequestsPage(...)` | `PackagesApi.getAvailablePackages(QueryMap)` | ✅ |
| `createPackageRequest()` | `PackagesApi.createPackage(body)` | ✅ |
| `createPackageRequestWithImages()` | `PackagesApi.createPackageMultipart(parts, images)` | ✅ |
| `updatePackageRequest()` / `updatePackageDetails()` | `PackagesApi.updatePackage(id, body)` | ✅ |
| **`updatePackageDetailsWithImages()`** | **— MISSING** | ❌ No `@Multipart @PUT` variant on `PackagesApi.kt`. Confirmed by reading file. |
| `cancelPackageRequest()` | `PackagesApi.cancelPackage(id)` | ✅ |
| `deletePackageRequest()` | `PackagesApi.deletePackage(id)` | ✅ (deprecated on iOS but exposed on Android) |
| `createServiceRequest()` | `PackagesApi.createServiceRequest(body)` | ✅ JSON path only — no multipart variant for receipt images on service requests |

### 2b. Service files

| iOS File | Android Equivalent | Status | Notes |
|---|---|---|---|
| `Services/PackagesAPIService.swift` (1,174) | `services/PackagesRepositoryImpl.kt` (186) + `core/network/.../packages/PackagesApi.kt` | ✅ | Android delegates HTTP to Retrofit + core network; iOS bundles URLSession + multipart builders + validation |
| `Services/SavedPackageDescriptionsStore.swift` | `services/SavedPackageDescriptionsStore.kt` | ✅ |
| `Services/SavedPackageRouteTemplatesStore.swift` | `services/SavedPackageRouteTemplatesStore.kt` | ✅ |
| `Services/ShipperDisclaimerStore.swift` | `services/ShipperDisclaimerStore.kt` | ✅ |

**Android-only services** (healthy):

- `services/PackagesRepository.kt` — interface (iOS uses concrete service)
- `services/PackagesModule.kt` — Hilt bindings
- `services/PackageTutorialStore.kt` — tutorial dismissed flag persistence
- `services/MultipartFormDataFactory.kt` — extracted multipart builder (iOS inlines)

---

## 3. ViewModels

### 3a. File parity

| iOS File | Android Equivalent | Status |
|---|---|---|
| `ViewModels/PackageViewModel.swift` (694) | `viewmodel/PackageViewModel.kt` (555) + `viewmodel/PackageCreationAssistViewModel.kt` (92) | ⚠️ Mostly parity, three methods missing (see 3b) |

### 3b. Method-level parity (PackageViewModel)

| iOS Method | Android Equivalent | Status |
|---|---|---|
| `loadPackageRequests(force:)` | `loadPackages()` | ✅ |
| `createPackageRequest(...)` / `createPackageRequestWithImages(...)` | `createPackageRequest(payload, uris)` (unified) | ✅ |
| `updatePackageRequest(id, ...)` / `updatePackageDetails(id, ...)` / `updatePackageDetailsWithImages(...)` | `updatePackage(id, request)` | ⚠️ Image variant blocked by missing API endpoint (§2a) |
| `cancelPackageRequest(id)` | `cancelPackage(id)` | ✅ |
| `getPackageRequest(id)` | `getPackage(id)` | ✅ |
| `requestTripForPackage(packageId, tripId, price, message)` | `requestTripForPackage(...)` | ✅ |
| `getCompatibleTrips(...)` / `checkExistingShipperRequest(...)` / `checkMatchForTripPackage(...)` | Delegated to `BookingsRepository` | ⚠️ Different layering — acceptable |
| `loadAvailablePackages(...)` / `getAvailablePackageRequestsPage(...)` | `loadAvailablePackages(params)` / `loadAvailablePackagesPage(filter, page)` | ✅ Android pagination state machine more explicit |
| `packagesByStatus(status)` / `getPendingRequests()` | `packagesByStatus(status)` / `getPendingRequests()` | ✅ |
| `getMatchedRequests()` / `getBookedRequests()` | — (UI filters from `uiState`) | ⚠️ Not exposed; UI uses state filters |
| **`pollForProcessedImages(...)`** | **— MISSING** | ❌ iOS retries 3× to refresh processed image URLs after async server processing. Confirmed absent (grepped). |
| **`checkSimilarPackages(originCity, destinationCity)`** | **— MISSING** | ❌ Duplicate package detection. Confirmed absent. |
| **`enableShipperRoleIfNeeded()`** | **— MISSING** | ❌ Auto-enable shipper role on first package creation. Confirmed absent. |
| `createServiceRequest(type, list, city, ...)` | `createServiceRequest(payload)` | ✅ |

**Android-only ViewModel:** `PackageCreationAssistViewModel.kt` — extracted assist UX state.

---

## 4. Views / Screens

### 4a. Mapping

| iOS Screen | Android Equivalent | Status |
|---|---|---|
| `Views/PackageListView.swift` (107) | `ui/PackageListScreen.kt` (152) | ✅ |
| `Views/Shipper/PackageDetailView.swift` (1,073) + `+Helpers.swift` + `+Modifiers.swift` | `ui/PackageDetailScreen.kt` (174) | ❌ **MAJOR GAP** — missing shipper info card, compatible-trips list, image carousel, receipt section, full action set, animation modifiers |
| `Views/Shipper/PackageRequestView.swift` (1,041) | `ui/PackageRequestScreen.kt` (829) | ⚠️ Mostly parity — Android monolithic vs iOS' modular `Components/PackageRequest/` (11 files). Functional gap small; structural gap real. |
| `Views/CreateServiceRequestView.swift` (1,319) | `ui/PackageErrandRequestScreen.kt` | ❌ **MAJOR GAP** — see §4c |
| `Views/EditPackageSheet.swift` | `ui/EditPackageSheet.kt` | ✅ |
| `Views/PackageHistoryView.swift` | — | ❌ MISSING |
| `Views/PackageTutorialOverlay.swift` | `components/PackageTutorialOverlay.kt` | ✅ |
| `Views/ReceiptUploadView.swift` | `features/chat/ui/ChatReceiptUploadSheet.kt` | ⚠️ Lives in **chat** feature on Android, not packages. Architectural placement — not strictly missing. |
| — | `ui/CarrierPackageDetailSheet.kt` | ✅ Android-only — carrier role view |
| — | `ui/PackageFilterSheet.kt` | ✅ Android-only |

### 4b. Component subfolders

#### iOS `Views/Components/PackageRequest/` (11 files) → Android

| iOS Component | Android Equivalent | Status |
|---|---|---|
| `DeliveryInformationSection.swift` | folded into `ui/PackageRequestScreen.kt` | ⚠️ inlined |
| `PackageDetailsSection.swift` | `components/PackageDetailsSection.kt` | ✅ |
| `PackageDetailCards.swift` | folded into `PackageRequestScreen.kt` | ⚠️ inlined |
| `PackageDetailComponents.swift` | atomized into `:core:designsystem` | ⚠️ |
| `PackageImagePreview.swift` | folded into picker / screen | ⚠️ inlined |
| `PackageImagesSection.swift` | folded into `PackageRequestScreen.kt` | ⚠️ inlined |
| `PackageRequestFormActions.swift` | folded into `PackageRequestScreen.kt` action buttons | ⚠️ inlined |
| `PackageRequestFormState.swift` | `model/PackageFormState.kt` + `model/PackageFormValidator.kt` | ✅ extracted to model layer |
| `PickupInformationSection.swift` | folded into `PackageRequestScreen.kt` | ⚠️ inlined |
| `SavedPackageRouteSheet.swift` | handoff template logic in screen | ⚠️ partial |
| `ChatLoadingView.swift` | progress indicators inline | ⚠️ |
| `DebugToolsView.swift` | not in packages module | N/A |

**Net:** functional coverage is ~80%, structural decomposition is the real divergence. Promote inlined sections to `components/` once a second consumer appears (per project's reuse-before-create rule).

#### iOS `Views/Components/CreateServiceRequest/` (6 files) → Android

| iOS Component | Android Equivalent | Status |
|---|---|---|
| `ItemCatalog.swift` | shopping list UI in `PackageErrandRequestScreen.kt` | ✅ |
| `ShoppingListForm.swift` | item-input UI in `PackageErrandRequestScreen.kt` | ✅ |
| `ServiceTypePicker.swift` | dropdown in `PackageErrandRequestScreen.kt` | ✅ |
| `CreateServiceRequestRequirements.swift` | `PackageRequirementChipUi` (existing) | ✅ |
| `DeliveryCityCatalog.swift` | uses shared `CityCatalog` | ⚠️ partial |
| **`InteractiveMapView.swift`** | **— MISSING** | ❌ MapKit store + delivery picker. No Compose Maps equivalent in packages. |

#### iOS `Views/Components/DeliveryCard/` (2 files) → Android

| iOS Component | Android Equivalent | Status |
|---|---|---|
| `DeliveryCard.swift` + `DeliveryCardSubComponents.swift` | — (separate per-domain cards) | ⚠️ Architectural divergence (see Executive Summary) |

#### iOS `Views/Components/` (root, 3 files) → Android

| iOS Component | Android Equivalent | Status |
|---|---|---|
| `PackageRequestCard.swift` | `components/PackageRequestCard.kt` | ✅ |
| `RequestStatusBadge.swift` | `components/PackageStatusBadgeConfig.kt` | ✅ |
| `ShipperRequestStatusBadge.swift` | `components/PackageStatusBadgeConfig.kt` | ✅ consolidated |

### 4c. Service-request (errand) flow gap detail

iOS `CreateServiceRequestView.swift` (1,319 LOC) covers:

- Service type selection + branching UI per type (errand, food, shopping, task, document)
- Shopping-list management with item catalog
- Store picker with location + map
- **Interactive map** (`InteractiveMapView.swift`) for store and delivery location
- Delivery address + city as separate fields, with city catalog
- Address geocoding + suggestion (`addressGeocodeToken`)
- Delivery date picker with timezone awareness
- Max budget field
- Expandable sections w/ requirement chips
- Advanced options toggle
- Direction picker (general errands)
- Task-specific fields (taskName, taskDescription) for custom-task type
- Two-level focus state machine

Android `PackageErrandRequestScreen.kt` covers:

- Service type dropdown (5 types)
- Shopping list add/edit
- Delivery city + address (single field)
- Delivery date picker
- Budget field
- Requirement chips

**Missing on Android:** interactive map, store location picker, address geocoding/suggestions, task-specific UI branching, timezone-aware date handling, advanced options, distinct city/address inputs.

### 4d. Android-only UI (✅ healthy additions)

- `components/CarrierExplorePackageCard.kt` — carrier browse
- `components/CreatePackageOptionsSheet.kt` — package vs service-request type chooser
- `components/NearbyBanner.kt` — nearby flag
- `components/PackageFilterContent.kt` — filter sheet body
- `components/PackageRequestBaseScaffold.kt` — shared scaffold for create/edit
- `components/PackageTextResolvers.kt` — string resolvers
- `components/UrgencyBadgeConfig.kt` — urgency badge config
- `ui/PackageFilterSheet.kt`, `ui/CarrierPackageDetailSheet.kt`

---

## 5. Top Gaps — Ranked

| # | Gap | Severity | Files involved | Slice scope |
|---|---|---|---|---|
| 1 | **Multipart `PUT /packages/{id}` for image update** | HIGH | `core/network/.../packages/PackagesApi.kt`, `PackagesRepositoryImpl.kt`, `PackageViewModel.kt`, `EditPackageSheet.kt`/`PackageRequestScreen.kt` (image picker wiring) | Add `@Multipart @PUT("packages/{id}")` overload + repo method + VM call. Tests for multipart serialization. |
| 2 | **Shipper `PackageDetailScreen` expansion** | HIGH | `ui/PackageDetailScreen.kt` (extend ~6×); add components for shipper info card, compatible-trips list, image carousel, action set | New components in `components/`; reuses `BookingsRepository.getCompatibleTrips`. Decompose into sections (header, route, details, shipper, trips, actions, images). |
| 3 | **Service-request (errand) flow completion** | HIGH | `ui/PackageErrandRequestScreen.kt` extension; new components for store picker, interactive map, address-suggest, task-specific fields | Multi-slice. Maps integration is a discrete sub-slice. Likely extends `core/domain` or pulls from `:core:designsystem`. |
| 4 | **Interactive map for store + delivery location** | HIGH | new `components/PackageLocationMap.kt` (Compose Maps), wire into `PackageErrandRequestScreen.kt` | Add Google Maps Compose dependency to `:app` (or `:core:designsystem` if reused). Place + geocode autocomplete. |
| 5 | **`pollForProcessedImages` — async image processing poll** | MEDIUM | `viewmodel/PackageViewModel.kt`, `services/PackagesRepository.kt` | Add 3-attempt poll loop after create/update; refresh image URLs on completion. Tests via fake API. |
| 6 | **`checkSimilarPackages` — duplicate detection** | MEDIUM | `services/PackagesRepository.kt` (new endpoint), `viewmodel/PackageViewModel.kt`, surface in `PackageRequestScreen.kt` as warning | New endpoint check during create; show inline banner if duplicates found. |
| 7 | **`enableShipperRoleIfNeeded` — auto-enable shipper role** | MEDIUM | `viewmodel/PackageViewModel.kt`, `core/session` role flag, `features/profile` repo | Trigger after successful first package create. Cross-feature touch. |
| 8 | **`PackageHistoryView` equivalent** | MEDIUM | new `ui/PackageHistoryScreen.kt` + reuse `BookingsRepository` w/ status filter, or `PackagesRepository` history endpoint if present | Likely overlaps with bookings history (see [`BOOKINGS-PARITY-REVIEW.md`](BOOKINGS-PARITY-REVIEW.md) gap #3). Decide single-feature ownership before slicing. |
| 9 | **`PackageRequestScreen.kt` modularization (refactor)** | LOW | `ui/PackageRequestScreen.kt` 829-line file → split into `components/PickupInformationSection.kt`, `DeliveryInformationSection.kt`, `PackageImagesSection.kt`, `PackageRequestFormActions.kt`, etc. | Refactor — reduces 829-line monolith to a screen + 5–6 components matching iOS structure. |
| 10 | **Service-request multipart for receipt images** | LOW | `core/network/.../packages/PackagesApi.kt`, `PackagesRepositoryImpl.kt` | If service requests need receipt-image upload at creation time. Confirm need before slicing. |

---

## 6. Implementation Plan (suggested slice order)

Per [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md) and the project's slice-by-layer convention (DTO → repo → ViewModel → UI):

1. **Slice A — Multipart `PUT /packages/{id}`**
   - DTO + `@Multipart @PUT` on `PackagesApi.kt` + tests for serialization
   - Repo method + Impl + tests
   - VM `updatePackageWithImages(id, request, uris)` + tests
   - Wire into `EditPackageSheet.kt` image picker
   - `feat(packages): add multipart update endpoint for package image edits`

2. **Slice B — `pollForProcessedImages`**
   - Repo method (or reuse `getPackage`) + VM 3-attempt poll loop + tests
   - UI: trigger from create/update success path; refresh image URLs
   - `feat(packages): poll for processed images after create/update`

3. **Slice C — `PackageDetailScreen` expansion**
   - Decompose into sections: extract `components/PackageHeaderSection.kt`, `components/PackageRouteSection.kt`, `components/PackageImageCarousel.kt`, `components/PackageShipperInfoSection.kt`, `components/PackageActionsSection.kt`, `components/PackageCompatibleTripsSection.kt`
   - Wire VM state for compatible trips (delegated to `BookingsRepository`)
   - Previews (light + dark) for each section
   - Strings localized
   - `feat(packages): expand shipper package detail screen with shipper, trips, images, actions`

4. **Slice D — Service-request distinct city/address + advanced options + task-specific fields**
   - Form-state + validator updates
   - UI sections + previews + strings
   - `feat(packages): add advanced options + task-specific fields to errand request`

5. **Slice E — Interactive map + geocoding for service request**
   - Add Compose Maps dependency (decide `:app` vs `:core:designsystem`)
   - `components/PackageLocationMap.kt` (reusable: store + delivery)
   - Geocoding via Places API (or chosen provider) — repo + VM
   - Wire into `PackageErrandRequestScreen.kt`
   - Previews (light + dark) — fake state, no live map in preview
   - `feat(packages): add interactive map + geocoding for service request locations`

6. **Slice F — `checkSimilarPackages` duplicate detection**
   - Endpoint + repo + VM + tests
   - UI banner in `PackageRequestScreen.kt` create flow
   - `feat(packages): warn shipper of similar existing packages on create`

7. **Slice G — `enableShipperRoleIfNeeded`**
   - Cross-feature: depends on `core/session` role flag and `features/profile` role enable endpoint
   - Trigger from VM after first successful package create
   - `feat(packages): auto-enable shipper role on first package creation`

8. **Slice H — `PackageRequestScreen.kt` modularization (refactor)**
   - Split monolith into 5–6 `components/` matching iOS structure
   - `refactor(packages): split package request screen into reusable section components`

9. **Slice I — Package history view** (decide ownership with bookings first)
   - If owned by packages: new screen + repo wiring + previews + strings
   - If overlaps with bookings: link to `BOOKINGS-PARITY-REVIEW.md` gap #3 instead
   - `feat(packages): add package history view`

Each slice ends with a stop for user commit per [`.cursor/rules/phase-commit-workflow.mdc`](../.cursor/rules/phase-commit-workflow.mdc).

---

## 7. Cross-references

- Spec: [`04-packages.md`](04-packages.md)
- Phases: [`PHASES-AND-FEATURES.md`](PHASES-AND-FEATURES.md)
- Status: [`IMPLEMENTATION-STATUS.md`](IMPLEMENTATION-STATUS.md)
- Coverage matrix: [`FEATURE-COVERAGE-MATRIX.md`](FEATURE-COVERAGE-MATRIX.md)
- Design system: [`14-design-system.md`](14-design-system.md)
- Sibling parity reviews: [`BOOKINGS-PARITY-REVIEW.md`](BOOKINGS-PARITY-REVIEW.md), [`PAYMENTS-PARITY-REVIEW.md`](PAYMENTS-PARITY-REVIEW.md)
