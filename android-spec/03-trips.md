# 03 — Trips

**Phase:** 2 | **Feature:** Trips | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Carrier trip CRUD, browse available trips for shippers, trip-package matching, route templates; parity with [`TripsAPIService.swift`](../../Pasabayan/Features/Trips/Services/TripsAPIService.swift) and all files under [`Features/Trips/`](../../Pasabayan/Features/Trips/).

---

## Shared models (from `:core:domain` — see cross-spec note)

The following types are **defined once** in `:core:domain` and **imported** by this feature. Do not redefine them in `features/trips/model/`. Specs 03, 04, and 05 all share these types.

| Shared type | Module | Used here as |
|------------|--------|-------------|
| `UserSummary` | `:core:domain/model/` | Unified user subset — replaces iOS's scattered `CarrierInfo`, `UserInfo`, `AvailablePackageShipper`, `CarrierBasicInfo`. All fields optional except `id` + `name`. Computed: `effectiveVerificationLevel`, `ratingValue`, `formattedRating`, `isVerified`, `isPremium`. Used on `Trip.carrier`. |
| `PackageDimensions` | `:core:domain/model/` | Flexible decode + dual format. Used on `PackageTemplateDetails`. |
| `CompatibilityDetails` | `:core:domain/model/` | Shared with packages + bookings. |
| `Coordinates` | `:core:domain/model/` | `latitude: Double`, `longitude: Double`. |
| `PaginatedResponse<T>` | `:core:network` | Used by all list endpoints. |
| `TripStatus` | `:core:domain/enum/` | 5 cases. Canonical definition here; also used by bookings + packages + UI. |
| `TransportationMethod` | `:core:domain/enum/` | 10 cases + `isLandTransport`, `defaultPricingType`. Canonical here; also used by packages (`CompatibleTrip`). |
| `PricingType` | `:core:domain/enum/` | 2 cases. Canonical here. |
| `PricingMethod` | `:core:domain/enum/` | 3 cases. Canonical here. |
| `MatchStatus` | `:core:domain/enum/` | 12 cases (canonical in 05-bookings). Used here on `TripMatchPackage.matchStatus`. |
| `SortOrder` | `:core:domain/enum/` | `asc`/`desc`. Shared with packages browse. |
| `VerificationLevel` | `:core:domain/enum/` | 3 cases + `normalized()`. Used for carrier display. |
| `FlexibleDecoders` | `:core:domain/util/` | `flexibleDouble`, `flexibleBool`, `flexibleString`. |
| `DateTimeParsing` | `:core:domain/util/` | `parseApiDate`, `parseApiDateTime`, `combineDateAndTime`. |

---

## Component unification strategy

Many UI patterns in Trips are **shared with Packages (04) and Bookings (05)**. Before building trip-specific UI, the following **shared primitives** must land in `:core:designsystem` (or `:core:ui` if stateful). Feature modules compose these — they never duplicate the patterns.

### New shared `P*` components (`:core:designsystem`)

| Component | Purpose | Used by |
|-----------|---------|---------|
| **`PStatusBadge`** | Generic status badge: label + color + icon, 3 variants (standard / compact / iconOnly). Accepts any status enum via a `StatusBadgeConfig` (label, color, lightColor, icon). | Trips (`TripStatus`), Packages (`PackageRequestStatus`), Bookings (`MatchStatus`), Urgency |
| **`PRouteSection`** | Origin → Destination display with optional addresses, landmarks, dotted connector. | Trip cards, package cards, booking route section |
| **`PFilterChip`** | Tappable chip for category/status filtering with optional count badge and selected state. Capsule shape. | Trip browse filters, package browse filters, booking status filters |
| **`PEmptyState`** | Icon + title + description centered in a card. Already spec'd in **14** — implement now. | All list screens (trips, packages, bookings) |
| **`PCardActionFooter`** | Divider + "View Details" link + overflow menu. Already spec'd in **14** — implement now. | Trip card, package card, booking card |
| **`PLinearProgress`** | Themed linear progress bar (Material `LinearProgressIndicator` + tokens). | Trip package progress, delivery tracking |
| **`PExpandableSection`** | Header + expand/collapse content. Used for multi-section forms and detail views. | Trip creation/edit, package creation, booking details |
| **`PDateTimeField`** | Wrapped date/time picker field (tap to open `DatePickerDialog` / `TimePickerDialog`). Displays formatted value in `POutlinedTextField` (readOnly). | Trip creation/edit, package creation, booking pickup date |
| **`PStateSwitcher`** | Generic `when` wrapper for Loading / Error (retry) / Empty / Content states. Reduces boilerplate. | All list/detail screens |

### Reuse of existing `P*`

| Existing component | Trip usage |
|--------------------|------------|
| `PCard` | Trip cards, detail sections, earnings card |
| `PButton` | Create trip, edit, cancel, filter apply/reset, booking actions |
| `POutlinedTextField` | All form text fields |
| `PModalBottomSheet` | Filter sheet, saved routes, edit trip, disclaimer |
| `PScaffold` / `PSnackbar` | Screen shells, success/error feedback |
| `PTopBar` | Screen headers |
| `PDivider` | Section separators |
| `PCircularProgress` | Loading states (within `PStateSwitcher`) |

### Trip-specific components (`features/trips/components/`)

Only what **cannot be generalized** stays feature-scoped:

| Component | Notes |
|-----------|-------|
| `TripCard` | Composes: `PCard` + `PStatusBadge(TripStatus)` + `PRouteSection` + `TripPackageProgressWidget` + `PCardActionFooter` |
| `TripCreationForm` | Multi-section form using `PExpandableSection`, `PDateTimeField`, `POutlinedTextField`, city autocomplete |
| `TripPackageProgressWidget` | Trip-specific progress display using `PLinearProgress` + delivery metrics |
| `TripPackageProgressSkeleton` | Shimmer/skeleton for progress widget loading |
| `TripTutorialOverlay` | One-time carrier tutorial (AppStorage/SharedPreferences dismissed) |
| `SavedRoutesSheet` | `PModalBottomSheet` with saved route template list |
| `TripFilterContent` | Content inside `PModalBottomSheet`: search + origin/destination + radius + sort |
| `CarrierTripDisclaimerSheet` | One-time disclaimer with acknowledgment + offline sync |

---

## Models

### `Trip` (core model — from [`Trip.swift`](../../Pasabayan/Features/Trips/Models/Trip.swift))

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `carrierId` | Int | |
| `originCity` | String | |
| `originCountry` | String | |
| `originLat` | Double | |
| `originLng` | Double | |
| `destinationCity` | String | |
| `destinationCountry` | String | |
| `destinationLat` | Double | |
| `destinationLng` | Double | |
| `departureDate` | Date | |
| `arrivalDate` | Date | |
| `availableWeightKg` | Double | |
| `availableSpaceLiters` | Double | |
| `pricePerKg` | Double | Flights only; land uses pricing fields below |
| `tripStatus` | TripStatus | Enum — see below |
| `transportationMethod` | TransportationMethod | Enum — see below |
| `specialNotes` | String? | |
| `carrier` | User? | Nested user model |
| `createdAt` | String? | |
| `updatedAt` | String? | |

#### Computed properties (parity with iOS `Trip.swift`)

| Property | Return type | Logic |
|----------|-------------|-------|
| `route` | String | `"$originCity → $destinationCity"` |
| `routeDistanceKm` | Double | Haversine from origin/dest coordinates; fallback to `distanceKm` API field |
| `formattedDepartureDate` | String | Locale-formatted date string |
| `formattedArrivalDate` | String | Locale-formatted date string |
| `formattedDuration` | String | Duration between departure and arrival (e.g. "2d 5h") |
| `formattedPrice` | String | Formatted by pricing type (per-kg or flat) |
| `formattedCapacity` | String | e.g. "25.0 kg" |
| `isBookable` | Boolean | `tripStatus in [planning, active] && hasCapacity` |
| `hasCapacity` | Boolean | `availableWeightKg > 0` |
| `effectivePricingType` | PricingType | `pricingType ?: if (transportationMethod.isLandTransport) flat else perKg` |
| `effectivePrice` | Double | Resolves flat vs per-kg vs calculated price |
| `priceDisplayString` | String | Full display: "₱150.00/kg" or "₱500.00 flat" |
| `priceDisplayStringCompact` | String | Short display for cards |
| `estimatedPrice(forWeight: Double)` | Double | `effectivePrice * weight` (per-kg) or flat |

#### Land transport pricing fields

| Field | Type | Notes |
|-------|------|-------|
| `pricingType` | PricingType? | `per_kg` or `flat` |
| `pricingMethod` | PricingMethod? | `calculated`, `manual`, or `per_kg` |
| `flatTripPrice` | Double? | For flat pricing |
| `basePrice` | Double? | For calculated pricing |
| `calculatedPrice` | Double? | Server-computed |
| `distanceMultiplier` | Double? | For distance-based calc |

#### Passenger transport fields

| Field | Type | Notes |
|-------|------|-------|
| `passengerCapacity` | Int? | |
| `pricePerPassenger` | Double? | |
| `passengerRequirements` | String? | |
| `ageRestrictions` | String? | |
| `passengerAmenities` | String? | |

#### Address & pickup/dropoff fields

| Field | Type | Notes |
|-------|------|-------|
| `pickupAddress` | String? | |
| `pickupLandmark` | String? | |
| `pickupInstructions` | String? | |
| `dropoffAddress` | String? | |
| `dropoffLandmark` | String? | |
| `dropoffInstructions` | String? | |

#### Earnings fields

| Field | Type | Notes |
|-------|------|-------|
| `tripEarningsTotal` | Double? | |
| `tripEarningsCurrency` | String? | |
| `tripEarningsBreakdown` | TripEarningsBreakdown? | Delivered + pending breakdown |

#### Pending requests

| Field | Type | Notes |
|-------|------|-------|
| `hasPendingRequests` | Bool? | |
| `pendingRequestCount` | Int? | |
| `pendingRequests` | [PendingTripRequest]? | Array of pending shipper requests |
| `distanceKm` | Double? | Location-aware filtering (shipper proximity) |

### `CreateTripRequest`

| Field | Type | Notes |
|-------|------|-------|
| `originCity` | String | |
| `originCountry` | String | |
| `destinationCity` | String | |
| `destinationCountry` | String | |
| `departureDate` | String | API datetime string |
| `arrivalDate` | String | |
| `availableWeightKg` | Double | |
| `availableSpaceLiters` | Double | |
| `pricePerKg` | Double? | Flights only |
| `transportationMethod` | String | Enum raw value |
| `specialNotes` | String? | |
| `pricingMethod` | String? | `"calculated"` or `"manual"` |
| `flatTripPrice` | Double? | |
| `basePrice` | Double? | |
| `originCityId` | Int? | |
| `destinationCityId` | Int? | |
| `pickupAddress` | String? | |
| `dropoffAddress` | String? | |
| `autoRequestPackageId` | Int? | Auto-request a specific package on creation |
| `proposedPrice` | Double? | For auto-request workflow |
| `requestMessage` | String? | For auto-request workflow |

### `TripUpdateRequest`

All fields optional — only send changed values:

| Field | Type | Notes |
|-------|------|-------|
| `tripStatus` | String? | |
| `availableWeightKg` | Double? | |
| `availableSpaceLiters` | Double? | |
| `pricePerKg` | Double? | |
| `specialNotes` | String? | |
| `flatTripPrice` | Double? | |
| `originCity` | String? | Editable only when status = `planning` |
| `originCountry` | String? | |
| `originLat` | Double? | |
| `originLng` | Double? | |
| `destinationCity` | String? | Editable only when status = `planning` |
| `destinationCountry` | String? | |
| `destinationLat` | Double? | |
| `destinationLng` | Double? | |
| `departureDate` | String? | Editable only when status = `planning` |
| `arrivalDate` | String? | Editable only when status = `planning` |
| `transportationMethod` | String? | **Not** editable after `planning` |
| `pickupAddress` | String? | |
| `dropoffAddress` | String? | |

### Response types

| Type | Structure |
|------|-----------|
| `TripResponse` | `{ message: String, data: Trip }` |
| `TripsResponse` | `{ message: String, data: PaginatedResponse<Trip> }` |
| `TripsSuccessResponse` | `{ success: Bool, data: PaginatedResponse<Trip> }` |

### Trip matching models (from [`TripModels.swift`](../../Pasabayan/Features/Trips/Models/TripModels.swift))

#### `TripMatchesResponse`

| Field | Type | Notes |
|-------|------|-------|
| `trip` | Trip | The trip |
| `matches` | List<TripMatchPackage> | Matched packages |

#### `TripMatchPackage`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | Match ID |
| `matchStatus` | MatchStatus | See enum below |
| `agreedPrice` | Double? | Negotiated price |
| `package` | PackageSummary? | Package details (id, description, weight, dimensions) |
| `shipper` | UserSummary? | Shipper info (id, name, avatar, rating) |
| `chatConversationId` | Int? | For navigating to chat |
| `confirmedAt` | String? | ISO 8601 |
| `pickedUpAt` | String? | ISO 8601 |
| `deliveredAt` | String? | ISO 8601 |
| `createdAt` | String? | ISO 8601 |
| `updatedAt` | String? | ISO 8601 |

#### `PendingTripRequest`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | Request ID |
| `shipperId` | Int | |
| `shipperName` | String? | |
| `shipperAvatar` | String? | URL |
| `packageId` | Int? | |
| `packageDescription` | String? | |
| `proposedPrice` | Double? | |
| `message` | String? | |
| `createdAt` | String? | |

#### `TripTemplateResponse`

| Field | Type | Notes |
|-------|------|-------|
| `message` | String | |
| `data` | TripTemplate | |

#### `TripTemplate`

| Field | Type | Notes |
|-------|------|-------|
| `originCity` | String | Prefill from package origin |
| `originCountry` | String | |
| `destinationCity` | String | |
| `destinationCountry` | String | |
| `suggestedDepartureDate` | String? | May be past — clamp to future |
| `suggestedArrivalDate` | String? | |
| `suggestedWeightKg` | Double? | |
| `suggestedSpaceLiters` | Double? | |
| `packageDetails` | PackageTemplateDetails | |

#### `PackageTemplateDetails`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | Package ID |
| `description` | String | |
| `weightKg` | Double | |
| `lengthCm` | Double? | |
| `widthCm` | Double? | |
| `heightCm` | Double? | |
| `isFragile` | Boolean | |
| `urgencyLevel` | String? | |

#### `TripEarningsBreakdown`

| Field | Type | Notes |
|-------|------|-------|
| `deliveredAmount` | Double | Earnings from delivered packages |
| `deliveredCurrency` | String | |
| `pendingAmount` | Double | Earnings from in-progress packages |
| `pendingCurrency` | String | |

---

## Enums (all raw values on wire — snake_case)

> **All enums below are defined in `:core:domain/enum/`**, not in `features/trips/model/`. This spec documents their cases and display properties for reference. Implementation lives in the shared module.

### `TripStatus`

| Case | Raw value | Display name | Color token | Icon |
|------|-----------|-------------|-------------|------|
| `planning` | `"planning"` | `R.string.trips_status_planning` | `tripScheduled` (Blue) | `Icons.Default.Edit` |
| `active` | `"active"` | `R.string.trips_status_active` | `tripActive` (Indigo) | `Icons.Default.CheckCircle` |
| `inTransit` | `"in_transit"` | `R.string.trips_status_in_transit` | `statusInTransit` (Orange) | `Icons.Default.LocalShipping` |
| `completed` | `"completed"` | `R.string.trips_status_completed` | `tripCompleted` (Green) | `Icons.Default.Done` |
| `cancelled` | `"cancelled"` | `R.string.trips_status_cancelled` | `tripCancelled` (Red) | `Icons.Default.Cancel` |

Each case provides: `displayName` (localized string resource), `color` (from `PasabayanColors`), `icon` (Material icon). Feed these into **`PStatusBadge`** via a `StatusBadgeConfig`.

### `TransportationMethod`

| Case | Raw value | Display name | Icon (emoji) | `isLandTransport` | `defaultPricingType` |
|------|-----------|-------------|--------------|-------------------|---------------------|
| `none` | `"none"` | — | — | false | `perKg` |
| `flight` | `"flight"` | `R.string.trips_transport_flight` | ✈️ | false | `perKg` |
| `bus` | `"bus"` | `R.string.trips_transport_bus` | 🚌 | true | `flat` |
| `car` | `"car"` | `R.string.trips_transport_car` | 🚗 | true | `flat` |
| `truck` | `"truck"` | `R.string.trips_transport_truck` | 🚛 | true | `flat` |
| `van` | `"van"` | `R.string.trips_transport_van` | 🚐 | true | `flat` |
| `motorcycle` | `"motorcycle"` | `R.string.trips_transport_motorcycle` | 🏍️ | true | `flat` |
| `ship` | `"ship"` | `R.string.trips_transport_ship` | 🚢 | false | `perKg` |
| `train` | `"train"` | `R.string.trips_transport_train` | 🚂 | true | `flat` |
| `other` | `"other"` | `R.string.trips_transport_other` | 📦 | false | `perKg` |

`isLandTransport` drives pricing form toggle (flat vs per-kg). `defaultPricingType` sets initial form state.

### `PricingType`

| Case | Raw value |
|------|-----------|
| `perKg` | `"per_kg"` |
| `flat` | `"flat"` |

### `PricingMethod`

| Case | Raw value |
|------|-----------|
| `calculated` | `"calculated"` |
| `manual` | `"manual"` |
| `perKg` | `"per_kg"` |

### `MatchStatus` — **shared** (`:core:domain/enum/MatchStatus.kt`, canonical in [05-bookings-matches.md](05-bookings-matches.md))

The full 12-case enum is defined in spec 05. This feature uses a **9-case subset** (the cases below). The shared enum has all 12 cases; trips code simply doesn't encounter `shipperDeclined`, `carrierAccepted`, or `carrierDeclined` in its `TripMatchPackage` responses.

| Case | Raw value | Notes |
|------|-----------|-------|
| `pending` | `"pending"` | Initial request state |
| `carrierRequested` | `"carrier_requested"` | Carrier initiated |
| `shipperRequested` | `"shipper_requested"` | Shipper initiated |
| `shipperAccepted` | `"shipper_accepted"` | Shipper accepted carrier's request |
| `confirmed` | `"confirmed"` | Both parties agreed |
| `pickedUp` | `"picked_up"` | Package collected |
| `inTransit` | `"in_transit"` | En route |
| `delivered` | `"delivered"` | Completed |
| `cancelled` | `"cancelled"` | Either party cancelled |

### `TripSortOption` (browse query)

| Case | Raw value |
|------|-----------|
| `departureDate` | `"departure_date"` |
| `pricePerKg` | `"price_per_kg"` |
| `availableCapacity` | `"available_capacity"` |
| `distance` | `"distance"` |

### `SortOrder` — **shared** (`:core:domain/enum/SortOrder.kt`)

| Case | Raw value |
|------|-----------|
| `ascending` | `"asc"` |
| `descending` | `"desc"` |

Shared with Packages browse (04-packages). Defined once in `:core:domain`.

### `TripPackagesFilter` (client-only, carrier UI)

| Case | Display name | Icon | Empty state message |
|------|-------------|------|---------------------|
| `all` | `R.string.trips_filter_all` | list icon | `R.string.trips_empty_no_packages` |
| `remaining` | `R.string.trips_filter_remaining` | pending icon | `R.string.trips_empty_no_remaining` |
| `delivered` | `R.string.trips_filter_delivered` | check icon | `R.string.trips_empty_no_delivered` |

---

## Endpoints

| Method | Path | Notes |
|--------|------|--------|
| GET | `/trips` | List; on failure message containing "not registered as both carrier and shipper", iOS **retries** `/carrier/trips` |
| GET | `/carrier/trips` | Fallback list |
| POST | `/trips` | **Custom URLSession** in iOS (not `makeRequest`) — 8s timeout; special errors below |
| PUT | `/trips/{id}` | Update via `makeRequest` |
| DELETE | `/trips/{id}` | Cancel/delete; `APIResponse` |
| GET | `/trips/{id}` | Get trip detail (spec-defined; not in `TripsAPIService` — may be in booking flows) |
| GET | `/trips/available?...` | Browse — see query params below |

### POST `/trips` — special error handling

| HTTP status | Condition | DomainError |
|-------------|-----------|-------------|
| 400 | Message contains "cargo-only or passenger-only, not both" | `MixedTransportTypes` |
| 400 | Message contains "must specify either cargo transport or passenger transport" | `NoTransportTypeSpecified` |
| 401 | Message contains "Unauthenticated" | `Unauthenticated` |
| 403 | Message contains "not registered as a carrier" | `UserNotCarrier` |
| 422 | Validation errors object | `ValidationError(fields)` |
| timeout | 8s exceeded | `NetworkTimeout` |
| network | No internet / connection lost / cannot connect | `NetworkError` |
| decode | Empty body / malformed JSON | `DecodingError` |

### Browse query parameters — GET `/trips/available`

All optional. Snake_case on wire (from `TripsAPIService.getAvailableTrips`):

| Param | URL name | Type | Notes |
|-------|----------|------|-------|
| q | `q` | String | Free-text search |
| origin | `origin` | String | Origin city/address |
| destination | `destination` | String | Destination city/address |
| lat | `lat` | Double | GPS latitude |
| lng | `lng` | Double | GPS longitude |
| radius | `radius` | Double | Search radius (km) |
| cityId | `city_id` | Int | Filter by city ID |
| sortBy | `sort_by` | String | `TripSortOption.rawValue` |
| sortOrder | `sort_order` | String | `SortOrder.rawValue` (asc/desc) |
| page | `page` | Int | Pagination |

---

## Form validation rules (parity with iOS `TripCreationFormState`)

### Trip creation

| Rule | Constraint |
|------|-----------|
| Start / end location | Required, non-empty |
| Start ≠ end | Origin city must differ from destination city |
| Weight capacity | Required, must be > 0 |
| Space capacity | Optional; if provided must be > 0 |
| Pricing (flight / non-land) | `pricePerKg` required, > 0 |
| Pricing (land transport) | `flatTripPrice` required, > 0 |
| Departure date | Must be ≥ now + 5 minutes |
| Arrival date | Must be > departure date |
| Min trip duration (land) | arrival − departure ≥ 20 minutes |
| Min trip duration (flight) | arrival − departure ≥ 3 hours |
| Special notes | Optional; max 1000 characters |
| Transportation method | Required; must not be `none` |

### Trip edit (additional constraints)

| Rule | Constraint |
|------|-----------|
| Route fields | Editable **only** when `tripStatus == planning` |
| Date fields | Editable **only** when `tripStatus == planning` |
| `transportationMethod` | **Never** editable after creation |
| Status transitions | `planning → active → inTransit → completed` or `→ cancelled` at any stage |
| Cancel blocked | Cannot cancel if any match has status `confirmed`, `picked_up`, or `in_transit` |

### Validation error display

Show `validationErrorText` in a `PSnackbar` or inline below the relevant field. Map per-field 422 errors from API to field-level error states in the form.

---

## ViewModels (iOS reference — mirror on Android)

### `BrowseTripsViewModel` (shipper-facing)

**State:**
- `availableTrips`, `filteredTrips` — trip lists
- `isLoading`, `errorMessage` — loading/error
- `hasLoadedTrips` — prevents stale empty state on first load
- `filter` — `TripFilter(searchText, origin, destination, radius, sortBy, sortOrder)`
- `selectedTrip`, `selectedPackage` — booking selection
- `compatibilityResult`, `isCheckingCompatibility` — trip-package compat check
- `isBookingSheetPresented`, `isBookingTrip`, `bookingSuccessMessage` — booking flow

**Actions:** `loadAvailableTrips()`, `loadAvailableTripsWithFilter()`, `submitSearch()`, `clearSearch()`, `refreshTrips()`, `clearFilters()`, `applyFilterAndFetch()`, `selectTrip()`, `selectPackage()`, `checkTripCompatibility()`, `showBookingSheet()`, `bookTrip(pickupDate, notes)`, `bookTripDirectly()`, `clearBookingState()`

**Client-side filtering:** After API fetch, remove trips where status ∉ {`planning`, `active`} (server may include stale).

### `CreateTripFromPackageViewModel` (shipper request → carrier creates trip)

**State:**
- `templateData` — prefilled from API template
- Form fields: `departureDate`, `departureTime`, `arrivalDate`, `arrivalTime`, `availableWeight`, `availableSpace`, `pricePerKg`, `transportationMethod`, `specialNotes`, `pickupAddress`, `dropoffAddress`
- `isLoading`, `isSaving`, `errorMessage`, `showSuccess`

**Actions:** `loadTemplate()`, `applySavedRoute()`, `createTrip()`, `validateTripSchedule()`

**Date handling quirk:** Template dates may be in the past — clamp to future (departure ≥ tomorrow, arrival ≥ departure + min duration). iOS uses ISO 8601 with fractional seconds + date-only fallback parser.

### `CarrierTripsViewModel` (carrier dashboard — trip management)

**State:**
- `trips` — carrier's trip list
- `isLoading`, `errorMessage`
- `statusFilter` — selected `TripStatus?` (nil = all; default hides cancelled)
- `filteredTrips` — computed from `trips` + `statusFilter`
- `statusCounts` — `Map<TripStatus, Int>` for filter chip badges

**Actions:** `loadTrips()`, `refreshTrips()`, `setStatusFilter(status)`, `updateTripStatus(tripId, newStatus)`, `cancelTrip(tripId)`, `deleteTrip(tripId)`

**Fallback:** If `/trips` returns "not registered as both carrier and shipper", retry with `/carrier/trips`.

### `TripPackageProgressViewModel` (carrier — delivery progress tracking)

**State machine:**

| State | Description |
|-------|-------------|
| `Idle` | Not yet loaded |
| `Loading` | Fetching matches |
| `Loaded(metrics)` | Data available |
| `Empty` | No packages assigned |
| `Error(message)` | Fetch failed |

**`TripPackageProgressMetrics`:**

| Field | Type | Notes |
|-------|------|-------|
| `totalMatches` | Int | Total matched packages |
| `deliveredMatches` | Int | Delivered count |
| `deliveredRatio` | Float | `delivered / total` (0.0–1.0) |
| `deliveredLabel` | String | e.g. "3/5 delivered" |
| `status` | ProgressStatus | `notStarted`, `inProgress`, `complete` |
| `nextActionLabel` | String | e.g. "Pick up 2 packages" |
| `arrivalDateText` | String | Formatted arrival date |

**Actions:** `loadMatches(tripId)`, `refresh()`

### `PopularRoutesViewModel` (shipper-facing — popular package routes)

**State:**
- `routes` — list of popular route objects
- `isLoading`, `errorMessage`

**Actions:** `loadPopularRoutes()`

---

## UI (iOS reference — all views to mirror)

### Screens

| iOS View | Android composable | Purpose | Role |
|----------|--------------------|---------|------|
| [`BrowseTripsView`](../../Pasabayan/Features/Trips/Views/BrowseTripsView.swift) | `BrowseTripsScreen` | Shipper trip browsing + search + booking | Shipper |
| [`TripCreationView`](../../Pasabayan/Features/Trips/Views/TripCreationView.swift) | `TripCreationScreen` | Multi-step wizard: route → schedule → capacity → pricing → notes | Carrier |
| [`EditTripSheet`](../../Pasabayan/Features/Trips/Views/EditTripSheet.swift) | `EditTripSheet` | Edit existing trip via `PModalBottomSheet` | Carrier |
| [`TripDetailsView`](../../Pasabayan/Features/Trips/Views/TripDetailsView.swift) | `TripDetailsScreen` | Trip info + package list + status actions | Carrier / Shipper |
| [`CreateTripFromPackageView`](../../Pasabayan/Features/Trips/Views/CreateTripFromPackageView.swift) | `CreateTripFromPackageScreen` | Create trip prefilled from package template | Carrier |
| [`SavedRoutesSheet`](../../Pasabayan/Features/Trips/Views/SavedRoutesSheet.swift) | `SavedRoutesSheet` | Select from max 5 saved route templates | Carrier |
| [`TripFilterSheet`](../../Pasabayan/Features/Trips/Views/TripFilterSheet.swift) | `TripFilterSheet` | Browse filter UI | Shipper |

### Dashboard integration (carrier tab)

| iOS View | Android composable | Purpose |
|----------|--------------------|---------|
| `CarrierTripsContent` | `CarrierTripsContent` | Main container: status filter + trip list + FAB "Create Trip" |
| `TripsListSection` | `TripsListSection` | Loading / error / empty / tutorial overlay / trip list |
| `TripFilterSection` | `TripStatusFilterRow` | Horizontal row of `PFilterChip` per status with count badges |
| `CarrierTripDisclaimerSheet` | `CarrierTripDisclaimerSheet` | Carrier disclaimer modal (first-time) |
| `RecentTripsSection` | `RecentTripsSection` | Dashboard home recent trips summary |

### Trip creation UX detail

iOS `TripCreationView` has **two modes** — implement both:

1. **Wizard mode** (step-by-step): expandable sections, one open at a time. Sections:
   - Route (origin + destination + countries + city suggestions)
   - Pickup/dropoff addresses (optional, with map picker trigger)
   - Transportation method selector
   - Schedule (departure date/time + arrival date/time via `PDateTimeField`)
   - Capacity (weight required + space optional)
   - Pricing (per-kg for flights, flat for land — toggled by `transportationMethod.isLandTransport`)
   - Special notes (optional, max 1000 chars)
2. **Full review mode**: all sections expanded for final review before submit

**Additional creation UX:**
- **City autocomplete**: text input with filtered suggestions dropdown
- **Saved routes picker**: `SavedRoutesSheet` — apply fills origin/destination/addresses
- **Auto-save usual transport**: on successful creation, persist `transportationMethod` to `UsualTransportStore`
- **Requirements prompt**: info banner at top of form explaining carrier requirements
- **Keyboard toolbar**: prev/next field navigation (use `FocusRequester` chain on Android)
- **Success flow**: success alert → option to save route → dismiss

### Components detail

#### `TripCard`

Sections (top to bottom):
1. **Header**: route text (`PRouteSection`), transportation method emoji + name, `PStatusBadge(tripStatus)`
2. **Package progress** (if matches loaded): `TripPackageProgressWidget` — delivered count, progress bar, next action, arrival date
3. **Schedule**: departure + arrival formatted dates, duration
4. **Capacity & price**: weight available, formatted price
5. **Pending requests** (if any): aggregate badge with count + message
6. **Special notes** (if present): truncated text
7. **Footer**: `PCardActionFooter` — "View Details" + overflow menu (edit, update status, cancel, view compatible packages)
8. Reduced opacity for cancelled trips

#### `TripStatusBadge` → replaced by `PStatusBadge`

Three variants via `PStatusBadge`:
- **Standard**: colored background + icon + label text
- **Compact**: smaller, no icon
- **IconOnly**: just the colored icon

Color-coded per `TripStatus` enum display properties → `StatusBadgeConfig`.

#### `TripPackageProgressWidget`

| Sub-component | Purpose |
|---------------|---------|
| `TripPackageProgressWidget` | Status chip + progress bar (`PLinearProgress`) + delivered label + next action + arrival |
| `TripPackageProgressSkeleton` | Shimmer/skeleton while loading |
| `TripPackageProgressEmpty` | "No packages assigned" message |
| `TripPackageProgressError` | Error message + retry button |

#### `TripTutorialOverlay`

- Shown **once** for new carriers (first trip creation)
- Dismissed state stored in SharedPreferences: `trip_tutorial_shown_v1_{userId}`
- Content: explains trip creation flow, what carriers do
- Overlay with semi-transparent background + instructional card

### Trip details screen sections

**Carrier view:**
1. Trip header with route + status badge
2. Route information section
3. Schedule information section
4. Capacity information section
5. Pricing information section
6. Special notes (if present)
7. **Trip earnings overview** (total, breakdown delivered/pending) — shown when matches exist
8. **Package progress card** — `TripPackageProgressWidget`
9. **Accepted packages list** — filterable by `TripPackagesFilter` (all / remaining / delivered)
10. **Actions section**: edit trip, update status, cancel trip (with confirmation)

**Shipper view:**
1. Same info sections (read-only)
2. "Request to Book" button (if `isBookable`)
3. Carrier profile link

### Edit trip sheet sections

Uses `PExpandableSection` for each group. Lock icon shown for non-planning trips:
- Capacity: weight + space
- Pricing: per-kg or flat (based on transport method)
- Status: dropdown with confirmation dialog for status changes
- Notes: text area
- Route: origin/destination + countries + addresses (**locked** unless planning)
- Schedule: dates (**locked** unless planning)
- Edit info banner when trip is not in planning status

---

## Services (beyond API)

### `RoutesAPIService` (from [`RoutesAPIService.swift`](../../Pasabayan/Features/Trips/Services/RoutesAPIService.swift))

| Method | Path | Response | Notes |
|--------|------|----------|-------|
| GET | `/routes/popular-packages` | `PopularRoutesResponse` | Shipper-facing popular routes |

#### `PopularRoutesResponse`

| Field | Type | Notes |
|-------|------|-------|
| `message` | String | |
| `data` | List<PopularRoute> | |

#### `PopularRoute`

| Field | Type | Notes |
|-------|------|-------|
| `originCity` | String | |
| `destinationCity` | String | |
| `packageCount` | Int | Number of packages on this route |
| `averagePrice` | Double? | |

### `RouteActivityAPIService` (from [`RouteActivityAPIService.swift`](../../Pasabayan/Features/RouteActivity/Services/RouteActivityAPIService.swift))

| Method | Path | Response | Notes |
|--------|------|----------|-------|
| GET | `/route-activity/summary` | `RouteActivitySummaryResponse` | Carrier trip activity tracking |

#### `RouteActivitySummaryResponse`

| Field | Type | Notes |
|-------|------|-------|
| `message` | String | |
| `data` | RouteActivitySummary | |

#### `RouteActivitySummary`

| Field | Type | Notes |
|-------|------|-------|
| `totalTrips` | Int | |
| `activeTrips` | Int | |
| `completedTrips` | Int | |
| `totalEarnings` | Double? | |
| `currency` | String? | |

---

## Local / client-only state

- [`UsualTransportStore`](../../Pasabayan/Features/Trips/Services/UsualTransportStore.swift) — persisted usual transport preferences; **auto-saved** on successful trip creation
- [`SavedRouteTemplatesStore`](../../Pasabayan/Features/Trips/Services/SavedRouteTemplatesStore.swift) — max 5 saved route templates; sorted by date desc; drops oldest when 6th added
- [`CarrierDisclaimerStore`](../../Pasabayan/Features/Trips/Services/CarrierDisclaimerStore.swift) — per-user disclaimer acknowledgment with **offline sync** (if POST fails, sets `pending_sync` flag; retries on next launch)
- [`CarrierPreferencesFormStore`](../../Pasabayan/Features/Trips/Services/CarrierPreferencesFormStore.swift) — tracks whether carrier preferences form was shown to user (first-time gate before trip creation)

### `SavedRouteTemplate` model

| Field | Type | Notes |
|-------|------|-------|
| `startCountryCode` | String | |
| `startLocation` | String | City name |
| `pickupAddress` | String? | |
| `endCountryCode` | String | |
| `endLocation` | String | City name |
| `dropoffAddress` | String? | |
| `createdAt` | Long | Epoch ms |

Display label: `"$startLocation → $endLocation"` (e.g. "Toronto, ON → Vancouver, BC").

### Local storage keys (SharedPreferences / DataStore)

| Key | Type | Max | Notes |
|-----|------|-----|-------|
| `carrier_usual_transport_v1_{userId}` | String | — | `TransportationMethod.rawValue`; per-user; auto-saved on trip creation |
| `saved_route_templates_v1` | JSON array | 5 | Route templates; sorted by date desc; drops oldest |
| `carrier_disclaimer_ack_v1_{userId}` | Bool | — | Per-user disclaimer acknowledgment |
| `carrier_disclaimer_pending_sync_v1_{userId}` | Bool | — | Offline sync tracking for failed ack POST; retry on next app launch |
| `carrier_preferences_form_shown_v1_{userId}` | Bool | — | Tracks if carrier preferences form was shown; gate before first trip creation |
| `trip_tutorial_shown_v1_{userId}` | Bool | — | Tutorial overlay dismissed state |

---

## Quirks

- **Create trip** bypasses shared `APIService` success path — Android should still map HTTP codes to the **same** domain errors for parity.
- **Route editing** (city, country, coordinates, dates) only allowed when `tripStatus == planning`; `transportationMethod` not editable after `planning`.
- **`BrowseTripsViewModel`** filters API results client-side (removes non-planning/active trips).
- **`CreateTripFromPackageViewModel`** has special date parsing (ISO8601 with fractional seconds, date-only fallback).
- iOS has two identical methods: `cancelTrip(id:)` and `deleteTrip(id:)` — both call DELETE. Android needs only one.
- **City autocomplete** is text-based with local filtering against a city catalog — not a Google Places API call.
- **Keyboard toolbar** (prev/next field): iOS uses `InputAccessoryView`; Android uses `FocusRequester` chain with `imeAction = ImeAction.Next`.

---

## TDD checklist

- [ ] Unit tests for `Trip` JSON decode — all fields including pricing, passenger, earnings, addresses.
- [ ] `Trip` computed properties: `route`, `isBookable`, `hasCapacity`, `effectivePricingType`, `effectivePrice`, `estimatedPrice`.
- [ ] `CreateTripRequest` encode with all fields including `autoRequestPackageId`, `proposedPrice`, `requestMessage`.
- [ ] `TripUpdateRequest` encode — verify route fields only sent when status = planning.
- [ ] All enums decode correctly from raw string values (especially `TransportationMethod.none`).
- [ ] `TransportationMethod.isLandTransport` and `defaultPricingType` correctness.
- [ ] `MatchStatus` enum decode (shared with Bookings).
- [ ] POST `/trips` error handling: 400 (mixed/no transport), 401, 403, 422, timeout, network, decode.
- [ ] `TripMatchesResponse`, `TripMatchPackage`, `TripTemplateResponse` decode tests.
- [ ] `TripEarningsBreakdown` and `PendingTripRequest` decode tests.
- [ ] `BrowseTripsViewModel`: search, filter, pagination, client-side status filtering, booking flow.
- [ ] `CarrierTripsViewModel`: load, status filter, status counts, cancel blocking logic.
- [ ] `TripPackageProgressViewModel`: state machine transitions (idle → loading → loaded/empty/error).
- [ ] `TripPackageProgressMetrics`: ratio calculation, status derivation, label generation.
- [ ] Form validation: all rules from validation table (dates, capacity, pricing, notes length).
- [ ] Local stores: usual transport (auto-save), route templates (max 5 + drop oldest), disclaimer ack (offline sync), preferences form gate.
- [ ] `PStatusBadge` renders all 3 variants with correct colors (shared component test).
- [ ] `PRouteSection` displays origin → destination with optional addresses (shared component test).
- [ ] `PExpandableSection` expand/collapse behavior (shared component test).
