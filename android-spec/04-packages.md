# 04 — Packages and service requests

**Phase:** 2 | **Feature:** Packages | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Package requests CRUD, available packages browse, multipart image uploads, service requests (grocery, errands), compatible trips; parity with [`PackagesAPIService.swift`](../../Pasabayan/Features/Packages/Services/PackagesAPIService.swift) and all files under [`Features/Packages/`](../../Pasabayan/Features/Packages/).

## Models

### `PackageRequest` (core model — from [`PackageRequest.swift`](../../Pasabayan/Features/Packages/Models/PackageRequest.swift))

#### Core fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `userId` | Int | |
| `pickupAddress` | String | |
| `pickupCity` | String | |
| `pickupCountry` | String | |
| `deliveryAddress` | String | |
| `deliveryCity` | String | |
| `deliveryCountry` | String | |
| `packageWeightKg` | Double? | Flexible decode: Double, Int, or String |
| `packageDimensions` | PackageDimensions? | length, width, height (each flexible decode) |
| `packageType` | String | See PackageType enum |
| `fragile` | Bool? | Flexible decode: Bool, Int (0/1), String ("true"/"false") |
| `packageValue` | Double? | |
| `packageDescription` | String? | |
| `urgencyLevel` | String? | See UrgencyLevel enum |
| `maxPriceBudget` | Double? | |
| `pickupDatePreferred` | Date? | 9 date format parsers — see quirks |
| `pickupTimePreferred` | String? | |
| `pickupDateFlexible` | Bool? | Flexible decode |
| `deliveryDateNeeded` | Date? | |
| `deliveryTimeNeeded` | String? | |
| `specialHandlingRequirements` | String? | |
| `requestStatus` | String | See PackageRequestStatus enum |
| `createdAt` | Date? | |
| `updatedAt` | Date? | |
| `pickupCityId` | Int? | |
| `deliveryCityId` | Int? | |

#### Image fields

| Field | Type | Notes |
|-------|------|-------|
| `images` | [PackageImage]? | Array of uploaded images |
| `imagesProcessing` | Bool? | `true` while backend processes uploads — poll until false |

#### Service request fields (inline on PackageRequest)

| Field | Type | Notes |
|-------|------|-------|
| `serviceType` | String? | See ServiceType enum |
| `shoppingList` | String? | JSON string of shopping items |
| `storeName` | String? | |
| `storeAddress` | String? | |
| `storeLat` | Double? | |
| `storeLng` | Double? | |
| `estimatedCost` | Double? | |
| `receiptRequired` | Bool? | |

#### Computed properties

| Property | Logic |
|----------|-------|
| `isServiceRequest` | `serviceType != nil` |
| `shoppingItems` | Parse `shoppingList` JSON → `[ShoppingItem]` |
| `hasStoreLocation` | `storeLat != nil && storeLng != nil` |
| `parsedShoppingList` | Same as shoppingItems |

### `PackageImage`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `packageRequestId` | Int? | |
| `imagePath` | String | May contain full URL from backend |
| `displayOrder` | Int? | |
| `originalFilename` | String? | |
| `url` | String? | Resolved URL |
| `createdAt` | String? | |
| `updatedAt` | String? | |

### `PackageDimensions`

| Field | Type | Notes |
|-------|------|-------|
| `length` | Double? | Flexible decode: Double, Int, or String |
| `width` | Double? | Same |
| `height` | Double? | Same |

### `ShoppingItem`

| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | Client-generated |
| `item` | String | Item name |
| `quantity` | String | Quantity (flexible — "2", "1 kg", etc.) |
| `notes` | String? | Special instructions |

### `CreatePackageRequest`

| Field | Type | Notes |
|-------|------|-------|
| `pickupAddress` | String | |
| `pickupCity` | String | |
| `pickupCountry` | String | |
| `deliveryAddress` | String | |
| `deliveryCity` | String | |
| `deliveryCountry` | String | |
| `packageWeightKg` | Double | |
| `packageDimensions` | PackageDimensions? | Optional nested object |
| `packageType` | String | PackageType raw value |
| `fragile` | Bool | |
| `packageValue` | Double? | |
| `packageDescription` | String? | |
| `urgencyLevel` | String | UrgencyLevel raw value |
| `maxPriceBudget` | Double? | |
| `pickupDatePreferred` | String | Date string |
| `pickupTimePreferred` | String? | |
| `pickupDateFlexible` | Bool | |
| `deliveryDateNeeded` | String | |
| `deliveryTimeNeeded` | String? | |
| `specialHandlingRequirements` | String? | |
| `pickupCityId` | Int? | |
| `deliveryCityId` | Int? | |

### `PackageUpdateRequest` (partial update — only changed fields)

| Field | Type | Notes |
|-------|------|-------|
| `maxPriceBudget` | Double? | |
| `urgencyLevel` | String? | |
| `pickupDateFlexible` | Bool? | |
| `deliveryDateNeeded` | String? | |
| `specialHandlingRequirements` | String? | |
| `requestStatus` | String? | For cancel: `"cancelled"` |

### `CreateServiceRequestBody`

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `serviceType` | String | Yes | ServiceType raw value |
| `shoppingList` | [[String: String]] | Yes | Array of `{item, quantity, notes}` |
| `deliveryCity` | String | Yes | |
| `deliveryAddress` | String | Yes | |
| `storeName` | String? | No | |
| `storeAddress` | String? | No | |
| `storeLat` | Double? | No | Store GPS |
| `storeLng` | Double? | No | |
| `estimatedCost` | Double? | No | |
| `maxPriceBudget` | Double? | No | |
| `deliveryDateNeeded` | String? | No | |
| `urgencyLevel` | String? | No | |
| `deliveryLat` | Double? | No | Delivery GPS |
| `deliveryLng` | Double? | No | |
| `direction` | String? | No | |
| `recipientName` | String? | No | |
| `recipientPhone` | String? | No | |

### Response types

| Type | Structure |
|------|-----------|
| `PackageRequestResponse` | `{ message: String, data: PackageRequest }` |
| `PackageRequestsResponse` | `{ message: String, data: PaginatedResponse<PackageRequest> }` |
| `AvailablePackagesResponse` | `{ data: PaginatedResponse<AvailablePackage> }` |

### `AvailablePackage` (browse model — separate from `PackageRequest`)

| Field | Type | Notes |
|-------|------|-------|
| All `PackageRequest` core fields | — | Inherits base fields |
| `distanceKm` | Double? | Server-calculated distance from searcher |
| `daysSincePosted` | Int? | |
| `shipper` | AvailablePackageShipper? | Simplified shipper info |
| Service fields | — | `serviceType`, `shoppingList`, `storeName`, etc. |

### Compatible trips models

| Type | Fields | Notes |
|------|--------|-------|
| `CompatibleTrip` | carrierId, origin/dest city+country, dates, weight/space/pricing, tripStatus, transportationMethod, specialNotes, shipperRequestStatus, canRequest, requestMessage, distanceKm | Full trip for matching |
| `CarrierInfo` | id, name, email, avatar, phone, phoneVerified, profileCompleted, userTypes, rating, totalRatings, verificationLevel, isActiveCarrier/Shipper | Extracted carrier details |
| `CompatibilityDetails` | routeMatch, capacitySufficient, dateCompatible, priceCompatible, weightUsagePercentage, spaceUsagePercentage | Match metrics |
| `CompatibilitySummary` | totalCompatible, perfectMatches, goodMatches, fairMatches, averageCompatibilityScore, totalPotentialEarnings, capacityUtilization | Aggregate stats |
| `CompatiblePackagesData` | tripInfo, compatiblePackages (paginated), compatibilitySummary | Full response |

## Enums (all raw values on wire — snake_case)

### `PackageType` (21 cases)

| Case | Raw value | Icon |
|------|-----------|------|
| `general` | `"general"` | 📦 |
| `electronics` | `"electronics"` | 💻 |
| `clothing` | `"clothing"` | 👕 |
| `books` | `"books"` | 📚 |
| `food` | `"food"` | 🍕 |
| `furniture` | `"furniture"` | 🪑 |
| `medical` | `"medical"` | 💊 |
| `documents` | `"documents"` | 📄 |
| `fragile` | `"fragile"` | 🔮 |
| `parcel` | `"parcel"` | 📮 |
| `gifts` | `"gifts"` | 🎁 |
| `automotive` | `"automotive"` | 🚗 |
| `beauty` | `"beauty"` | 💄 |
| `sports` | `"sports"` | ⚽ |
| `toys` | `"toys"` | 🧸 |
| `household` | `"household"` | 🏠 |
| `jewelry` | `"jewelry"` | 💎 |
| `art` | `"art"` | 🎨 |
| `industrial` | `"industrial"` | 🏭 |
| `equipment` | `"equipment"` | 🔧 |
| `other` | `"other"` | 📋 |

Note: iOS also handles `"document"` (singular) → maps to `documents`.

### `UrgencyLevel` (6 cases)

| Case | Raw value | Color | Icon |
|------|-----------|-------|------|
| `low` | `"low"` | green | 🟢 |
| `normal` | `"normal"` | blue | 🔵 |
| `high` | `"high"` | orange | 🟠 |
| `urgent` | `"urgent"` | red | 🔴 |
| `express` | `"express"` | purple | ⚡ |
| `flexible` | `"flexible"` | gray | 🕐 |

### `PackageRequestStatus` (10 cases)

| Case | Raw value | Color | Notes |
|------|-----------|-------|-------|
| `open` | `"open"` | blue | Available for requests |
| `pendingRequest` | `"pending_request"` | orange | Has pending carrier/shipper request |
| `matched` | `"matched"` | purple | Matched with carrier |
| `pickedUp` | `"picked_up"` | indigo | Carrier picked up |
| `delivered` | `"delivered"` | green | Delivery complete |
| `cancelled` | `"cancelled"` | red | Cancelled |
| `pending` | `"pending"` | orange | Legacy |
| `booked` | `"booked"` | purple | Legacy |
| `inTransit` | `"in_transit"` | indigo | Legacy |

### `ServiceType` (5 cases)

| Case | Raw value | Icon | Badge color | Requires car/motorcycle |
|------|-----------|------|-------------|------------------------|
| `delivery` | `"delivery"` | 📦 | blue | No |
| `groceryShopping` | `"grocery_shopping"` | 🛒 | green | No |
| `foodDelivery` | `"food_delivery"` | 🍔 | orange | Yes |
| `pharmacyPickup` | `"pharmacy_pickup"` | 💊 | red | No |
| `generalErrand` | `"general_errand"` | 📋 | purple | No |

### `PackageSize` (4 cases — computed from weight)

| Case | Raw value | Weight range |
|------|-----------|-------------|
| `small` | `"small"` | ≤ 5 kg |
| `medium` | `"medium"` | ≤ 15 kg |
| `large` | `"large"` | ≤ 30 kg |
| `extraLarge` | `"extra_large"` | > 30 kg |

### `DeliveryCardVariant` (client-only UI)

| Case | maxDetailsPerRow | maxShoppingListItems |
|------|-----------------|---------------------|
| `standard` | 3 | 5 |
| `compact` | 2 | 3 |
| `package` | 3 | 4 |

## Endpoints

| Method | Path | Notes |
|--------|------|--------|
| GET | `/packages` | List own packages (paginated) |
| GET | `/packages/available?...` | Browse — see query params below |
| POST | `/packages` | JSON or **multipart** with `images[]` — see multipart fields below |
| GET | `/packages/{id}` | Get detail; also used for **image polling** |
| PUT | `/packages/{id}` | Update (JSON or multipart). Cancel: `{request_status: "cancelled"}` |
| DELETE | `/packages/{id}` | Delete (deprecated — prefer cancel via PUT) |
| POST | `/services/request` | `CreateServiceRequestBody` → `PackageRequestResponse` |
| GET | `/packages/{packageId}/trip-template` | Compatible trip template (in BookingsAPIService) |
| GET | `/packages/{packageRequestId}/compatible-trips` | Compatible trips (in BookingsAPIService) |

### Browse query parameters — GET `/packages/available`

All optional. Snake_case on wire (from `PackagesAPIService.buildAvailablePackagesEndpoint`):

| Param | URL name | Type | Notes |
|-------|----------|------|-------|
| q | `q` | String | Free-text search |
| origin | `origin` | String | Origin city/address |
| destination | `destination` | String | Destination city/address |
| newThisWeek | `new_this_week` | Bool | `"true"` / `"false"` |
| sortBy | `sort_by` | String | Sort field |
| sortOrder | `sort_order` | String | asc/desc |
| destinationCityId | `destination_city_id` | Int | Filter by destination city |
| maxWeight | `max_weight` | Double | Max weight filter (kg) |
| urgency | `urgency` | String | Urgency level |
| minBudget | `min_budget` | Double | Min budget filter |
| maxBudget | `max_budget` | Double | Max budget filter |
| fragile | `fragile` | Bool | Fragile-only filter |
| pickupDateFrom | `pickup_date_from` | String | Date range start |
| pickupDateTo | `pickup_date_to` | String | Date range end |
| cityId | `city_id` | Int | Filter by city ID |
| lat | `lat` | Double | GPS latitude |
| lng | `lng` | Double | GPS longitude |

### Multipart field names — POST/PUT `/packages` with images

From `PackagesAPIService.createPackageRequestWithImages`:

| Field name | Type | Notes |
|-----------|------|-------|
| `pickup_address` | text | |
| `pickup_city` | text | |
| `pickup_country` | text | |
| `delivery_address` | text | |
| `delivery_city` | text | |
| `delivery_country` | text | |
| `package_weight_kg` | text | |
| `package_dimensions[length]` | text | Optional, nested |
| `package_dimensions[width]` | text | Optional, nested |
| `package_dimensions[height]` | text | Optional, nested |
| `package_type` | text | |
| `fragile` | text | `0` or `1` |
| `package_value` | text | Optional |
| `package_description` | text | Optional |
| `urgency_level` | text | |
| `max_price_budget` | text | Optional |
| `pickup_date_preferred` | text | |
| `pickup_time_preferred` | text | Optional |
| `pickup_date_flexible` | text | `0` or `1` |
| `delivery_date_needed` | text | |
| `delivery_time_needed` | text | Optional |
| `special_handling_requirements` | text | Optional |
| `pickup_city_id` | text | Optional |
| `delivery_city_id` | text | Optional |
| `images[]` | file | Laravel array format; max **5** images, max **5 MB** each; filename: `package_image_{index}.jpg`; MIME: `image/jpeg` |

## ViewModel (iOS reference — mirror on Android)

### `PackageViewModel` (from [`PackageViewModel.swift`](../../Pasabayan/Features/Packages/ViewModels/PackageViewModel.swift))

**State:**
- `packageRequests: [PackageRequest]` — own packages list
- `compatibleTrips: [CompatibleTrip]` — trips matching a package
- `loadingTripsForPackageId`, `lastLoadedTripsForPackageId` — loading state
- `isLoading`, `errorMessage`, `successMessage`
- `hasLoadedPackages` — debounce guard

**Actions:**
- `loadPackageRequests(force:)` — with **1.0s debounce** to prevent rapid reloads
- `createPackageRequest(_:completion:)` — JSON creation
- `createPackageRequestWithImages(_:images:completion:)` — multipart creation
- `updatePackageRequest(id:request:)` — full update
- `updatePackageDetails(id:update:completion:)` — partial update (JSON)
- `updatePackageDetailsWithImages(id:update:images:completion:)` — partial update with new images
- `cancelPackageRequest(id:)` — sets status to `cancelled` via PUT
- `deletePackageRequest(id:)` — deprecated, prefer cancel
- `getPackageRequest(id:)` — single detail fetch
- `getCompatibleTrips(for:)` — loads compatible trips for matching
- `bookPackage(packageId:tripId:proposedPrice:)` — legacy booking
- `requestTripForPackage(...)` — service request handling
- `checkSimilarPackages(...)` — duplicate detection
- `pollForProcessedImages(packageId:attempts:)` — see image polling below

**Special behaviors:**
- **Post-creation shipper role auto-enable:** `enableShipperRoleIfNeeded()` — after creating a package, auto-enables shipper role if not already active
- **Activity logging:** calls `ActivityLogger.shared.logPackageAction()` after create/cancel

### Image processing polling

After creating a package with images, backend processes them asynchronously. The ViewModel polls:

1. Wait **2 seconds**
2. GET `/packages/{id}` — check `imagesProcessing` field
3. If `imagesProcessing == true` and attempts < 3, retry from step 1
4. After 3 attempts or `imagesProcessing == false`, update local state with final image URLs

## UI (iOS reference — all views to mirror)

| iOS View | Purpose | Role |
|----------|---------|------|
| `PackageListView` | Lists shipper's own packages with pagination | Shipper |
| `PackageDetailView` (Shipper/) | Full package details, images gallery, status, compatible trips | Shipper |
| `EditPackageSheet` | Limited edits for open/pending packages (budget, urgency, dates, handling) | Shipper |
| `CreateServiceRequestView` | Multi-step service request wizard (type → items → delivery → confirm) | Shipper |
| `ReceiptUploadView` | Upload delivery receipt for completed service requests | Shipper |
| `PackageTutorialOverlay` | One-time tutorial for new shippers (`hasSeenPackageTutorial` flag) | Shipper |

### Reusable components

| Component | Purpose |
|-----------|---------|
| `PackageRequestCard` | Package summary card for lists |
| `DeliveryCard` | Complex card with variant layouts (standard/compact/package) |
| `RequestStatusBadge` | Status badge with colors per PackageRequestStatus |
| `ShipperRequestStatusBadge` | Shipper-specific request status display |
| `PackageImagesSection` | Image gallery (up to 5 images) |
| `PackageImagePreview` | Single image preview with zoom |
| `PackageDetailsSection` | Weight, dimensions, type, fragile display |
| `DeliveryInformationSection` | Delivery address, date, time |
| `PickupInformationSection` | Pickup details |
| `SavedPackageRouteSheet` | Display/reuse saved pickup + handoff templates |
| `ServiceTypePicker` | Service type selection with icons |
| `ShoppingListForm` | Add/edit shopping items |
| `DeliveryCityCatalog` | City selection for delivery |
| `ItemCatalog` | Browse items for shopping list |
| `InteractiveMapView` | Store/delivery location picker |

## Local / client-only state

- [`ShipperDisclaimerStore`](../../Pasabayan/Features/Packages/Services/ShipperDisclaimerStore.swift) — per-user disclaimer ack with **offline-first** flow: local save → async POST → pending sync flag → background retry
- [`SavedPackageDescriptionsStore`](../../Pasabayan/Features/Packages/Services/SavedPackageDescriptionsStore.swift) — recent package descriptions
- [`SavedPackageRouteTemplatesStore`](../../Pasabayan/Features/Packages/Services/SavedPackageRouteTemplatesStore.swift) — pickup + handoff route templates

### Local storage keys (SharedPreferences / DataStore)

| Key | Type | Max | Logic |
|-----|------|-----|-------|
| `shipper_disclaimer_ack_v1_{userId}` | Bool | — | Per-user disclaimer acknowledgment |
| `shipper_disclaimer_pending_sync_v1_{userId}` | Bool | — | Offline sync tracking for failed ack POST |
| `saved_package_descriptions_v1` | JSON `[String]` | 15 | FIFO, most recent first (prepended); case-insensitive dedup; trim whitespace; skip empty |
| `saved_pickup_templates_v1` | JSON array of `{pickupCountryCode, pickupCity, pickupAddress, createdAt}` | 5 | Sorted by `createdAt` desc; ID: `"{countryCode}-{city}-{timestamp}"` |
| `saved_handoff_templates_v1` | JSON array of `{deliveryCountryCode, deliveryCity, deliveryAddress, createdAt}` | 5 | Sorted by `createdAt` desc; ID: `"{countryCode}-{city}-{timestamp}"` |
| `hasSeenPackageTutorial` | Bool | — | One-time tutorial overlay flag |

## Quirks

- **Flexible type decoding:** `packageWeightKg` decodes from Double, Int, or String. `fragile` decodes from Bool, Int (0/1), or String ("true"/"false"/"yes"/"no"). Android kotlinx.serialization custom deserializers needed.
- **9 date format parsers** on iOS — try in order: ISO8601 with fractional, without fractional, backend microseconds (`yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'`), date-only (`yyyy-MM-dd`), plus timezone variants.
- **Image polling** after creation — see ViewModel section above.
- **`deletePackageRequest`** is deprecated in iOS — prefer `cancelPackageRequest` (PUT with `request_status: cancelled`).
- **Partial PUT updates** (`updatePackageDetailsWithImages`) sends only changed fields in multipart, not the full object.
- **Activity logging** — ViewModel calls `ActivityLogger.shared.logPackageAction()` after create/cancel (see [12-legal-support-misc.md](12-legal-support-misc.md) for payload).
- **Shipper role auto-enable** — after first package creation, `enableShipperRoleIfNeeded()` is called.
- **`documents` enum** — iOS handles both `"document"` (singular) and `"documents"` (plural) raw values; map both.

## TDD checklist

- [ ] `PackageRequest` JSON decode — all fields including images, service fields, flexible types.
- [ ] `CreatePackageRequest` encode — all fields.
- [ ] `PackageUpdateRequest` partial update — verify only changed fields sent.
- [ ] `CreateServiceRequestBody` encode — all fields including shopping list.
- [ ] All enums decode from raw string values (especially `PackageType` 21 cases, `PackageRequestStatus` 10 cases).
- [ ] `AvailablePackage` decode with `distanceKm`, `daysSincePosted`, shipper.
- [ ] `CompatibleTrip` + `CarrierInfo` + `CompatibilityDetails` decode.
- [ ] Multipart field names match iOS (22 text fields + images array).
- [ ] Browse query params: verify all 17 params serialize correctly as snake_case.
- [ ] Image polling: mock 3-attempt poll until `imagesProcessing == false`.
- [ ] Flexible type decoding: Double/Int/String for weight; Bool/Int/String for fragile.
- [ ] Local stores: descriptions (max 15, FIFO, dedup), templates (max 5), disclaimer (offline sync).
- [ ] Service request ViewModel: type selection, shopping list, store location, validation.
