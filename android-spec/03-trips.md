# 03 — Trips

**Phase:** 2 | **Feature:** Trips | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Carrier trip CRUD, browse available trips for shippers, trip-package matching, route templates; parity with [`TripsAPIService.swift`](../../Pasabayan/Features/Trips/Services/TripsAPIService.swift) and all files under [`Features/Trips/`](../../Pasabayan/Features/Trips/).

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

| Type | Purpose |
|------|---------|
| `TripMatchesResponse` | Trip with matched packages |
| `TripMatchPackage` | Individual match with status |
| `TripTemplateResponse` | Template for "create trip from package" |
| `TripTemplate` | Template data (prefilled form fields) |
| `PackageTemplateDetails` | Package details within template |
| `TripEarningsBreakdown` | Earnings: delivered + pending amounts |
| `PendingTripRequest` | Shipper request on a trip |

## Enums (all raw values on wire — snake_case)

### `TripStatus`

| Case | Raw value |
|------|-----------|
| `planning` | `"planning"` |
| `active` | `"active"` |
| `inTransit` | `"in_transit"` |
| `completed` | `"completed"` |
| `cancelled` | `"cancelled"` |

### `TransportationMethod`

| Case | Raw value |
|------|-----------|
| `none` | `"none"` |
| `flight` | `"flight"` |
| `bus` | `"bus"` |
| `car` | `"car"` |
| `truck` | `"truck"` |
| `van` | `"van"` |
| `motorcycle` | `"motorcycle"` |
| `ship` | `"ship"` |
| `train` | `"train"` |
| `other` | `"other"` |

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

### `TripSortOption` (browse query)

| Case | Raw value |
|------|-----------|
| `departureDate` | `"departure_date"` |
| `pricePerKg` | `"price_per_kg"` |
| `availableCapacity` | `"available_capacity"` |
| `distance` | `"distance"` |

### `SortOrder`

| Case | Raw value |
|------|-----------|
| `ascending` | `"asc"` |
| `descending` | `"desc"` |

### `TripPackagesFilter` (client-only, carrier UI)

| Case | Use |
|------|-----|
| `all` | Show all packages in trip |
| `remaining` | Not yet delivered |
| `delivered` | Completed deliveries |

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

## ViewModels (iOS reference — mirror on Android)

### `BrowseTripsViewModel` (shipper-facing)

**State:**
- `availableTrips`, `filteredTrips` — trip lists
- `isLoading`, `errorMessage` — loading/error
- `filter` (origin, destination, radius, sort) — search criteria
- `selectedTrip`, `selectedPackage` — booking selection
- `compatibilityResult`, `isCheckingCompatibility` — trip-package compat check
- `isBookingSheetPresented`, `isBookingTrip`, `bookingSuccessMessage` — booking flow

**Actions:** `loadAvailableTrips()`, `submitSearch()`, `clearSearch()`, `refreshTrips()`, `clearFilters()`, `applyFilterAndFetch()`, `selectTrip()`, `selectPackage()`, `showBookingSheet()`, `bookTrip()`, `bookTripDirectly()`, `clearBookingState()`

### `CreateTripFromPackageViewModel` (shipper request → carrier creates trip)

**State:**
- `templateData` — prefilled from API template
- Form fields: `departureDate`, `departureTime`, `arrivalDate`, `arrivalTime`, `availableWeight`, `availableSpace`, `pricePerKg`, `transportationMethod`, `specialNotes`, `pickupAddress`, `dropoffAddress`
- `isLoading`, `isSaving`, `errorMessage`, `showSuccess`

**Actions:** `loadTemplate()`, `applySavedRoute()`, `createTrip()`, `validateTripSchedule()`

### `PopularRoutesViewModel` (shipper-facing — popular package routes)

### `TripPackageProgressViewModel` (carrier — delivery progress tracking)

## UI (iOS reference — all views to mirror)

| iOS View | Purpose | Role |
|----------|---------|------|
| [`BrowseTripsView`](../../Pasabayan/Features/Trips/Views/BrowseTripsView.swift) | Shipper trip browsing + search + booking | Shipper |
| [`TripCardView`](../../Pasabayan/Features/Trips/Views/TripCardView.swift) | Reusable card: carrier info, route, capacity, price, status badge | Both |
| [`TripCreationView`](../../Pasabayan/Features/Trips/Views/TripCreationView.swift) | Multi-step wizard: route → schedule → capacity → pricing → notes | Carrier |
| [`EditTripSheet`](../../Pasabayan/Features/Trips/Views/EditTripSheet.swift) | Edit existing trip (expandable sections); route editable only when `planning` | Carrier |
| [`TripDetailsView`](../../Pasabayan/Features/Trips/Views/TripDetailsView.swift) | Trip info + package list (filter: all/remaining/delivered) + status actions | Carrier |
| [`CreateTripFromPackageView`](../../Pasabayan/Features/Trips/Views/CreateTripFromPackageView.swift) | Create trip prefilled from package template; saved route support | Carrier |
| [`SavedRoutesSheet`](../../Pasabayan/Features/Trips/Views/SavedRoutesSheet.swift) | Select from max 5 saved route templates | Carrier |
| [`TripFilterSheet`](../../Pasabayan/Features/Trips/Views/TripFilterSheet.swift) | Browse filter UI: search, origin, destination, radius, sort | Shipper |
| `TripStatusBadge` | Status badge styling per `TripStatus` | Both |
| `TripTutorialOverlay` | One-time tutorial for new carriers | Carrier |
| `TripPackageProgressComponents` | Delivery progress bar within trip | Carrier |

## Services (beyond API)

### `RoutesAPIService` (from [`RoutesAPIService.swift`](../../Pasabayan/Features/Trips/Services/RoutesAPIService.swift))

| Method | Path | Notes |
|--------|------|-------|
| GET | `/routes/popular-packages` | Shipper-facing popular routes |

### `RouteActivityAPIService` (from [`RouteActivityAPIService.swift`](../../Pasabayan/Features/RouteActivity/Services/RouteActivityAPIService.swift))

| Method | Path | Notes |
|--------|------|-------|
| GET | `/route-activity/summary` | Carrier trip activity tracking |

## Local / client-only state

- [`UsualTransportStore`](../../Pasabayan/Features/Trips/Services/UsualTransportStore.swift) — persisted usual transport preferences
- [`SavedRouteTemplatesStore`](../../Pasabayan/Features/Trips/Services/SavedRouteTemplatesStore.swift) — max 5 saved route templates
- [`CarrierDisclaimerStore`](../../Pasabayan/Features/Trips/Services/CarrierDisclaimerStore.swift) — per-user disclaimer acknowledgment with offline sync
- [`CarrierPreferencesFormStore`](../../Pasabayan/Features/Trips/Services/CarrierPreferencesFormStore.swift) — tracks carrier preferences form display

### Local storage keys (SharedPreferences / DataStore)

| Key | Type | Max | Notes |
|-----|------|-----|-------|
| `carrier_usual_transport_v1_{userId}` | String | — | `TransportationMethod.rawValue`; per-user |
| `saved_route_templates_v1` | JSON array | 5 | Route templates; sorted by date desc; drops oldest |
| `carrier_disclaimer_ack_v1_{userId}` | Bool | — | Per-user disclaimer acknowledgment |
| `carrier_disclaimer_pending_sync_v1_{userId}` | Bool | — | Offline sync tracking for failed ack POST |
| `carrier_preferences_form_shown_v1_{userId}` | Bool | — | Tracks if carrier preferences form was shown |

## Quirks

- **Create trip** bypasses shared `APIService` success path — Android should still map HTTP codes to the **same** domain errors for parity.
- **Route editing** (city, country, coordinates, dates) only allowed when `tripStatus == planning`; `transportationMethod` not editable after `planning`.
- **`BrowseTripsViewModel`** filters API results client-side (removes non-planning/active trips).
- **`CreateTripFromPackageViewModel`** has special date parsing (ISO8601 with fractional seconds, date-only fallback).
- iOS has two identical methods: `cancelTrip(id:)` and `deleteTrip(id:)` — both call DELETE. Android needs only one.

## TDD checklist

- [ ] Unit tests for `Trip` JSON decode — all fields including pricing, passenger, earnings, addresses.
- [ ] `CreateTripRequest` encode with all fields including `autoRequestPackageId`, `proposedPrice`, `requestMessage`.
- [ ] `TripUpdateRequest` encode — verify route fields only sent when status = planning.
- [ ] All enums decode correctly from raw string values (especially `TransportationMethod.none`).
- [ ] POST `/trips` error handling: 400 (mixed/no transport), 401, 403, 422.
- [ ] `BrowseTripsViewModel`: search, filter, pagination, booking flow.
- [ ] `TripMatchesResponse` and `TripTemplateResponse` decode tests.
- [ ] Local stores: usual transport, route templates (max 5), disclaimer ack with sync.
