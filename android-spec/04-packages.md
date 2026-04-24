# 04 — Packages and service requests

**Phase:** 2 | **Feature:** Packages | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Package requests CRUD, available packages browse, multipart image uploads, service requests (grocery, errands), compatible trips; parity with [`PackagesAPIService.swift`](../../Pasabayan/Features/Packages/Services/PackagesAPIService.swift) and all files under [`Features/Packages/`](../../Pasabayan/Features/Packages/).

## Shared models (from `:core:domain` — see cross-spec note)

The following types are **defined once** in `:core:domain` and **imported** by this feature. Do not redefine them in `features/packages/model/`. Full definitions and canonical field tables live in the shared module; this spec documents feature-specific **usage** only.

| Shared type | Module | Used here as |
|------------|--------|-------------|
| `UserSummary` | `:core:domain/model/` | Replaces iOS `CarrierInfo` (on `CompatibleTrip`) and `AvailablePackageShipper` (on `AvailablePackage`). Single type with all fields optional except `id` + `name`. Computed: `effectiveVerificationLevel`, `ratingValue`, `formattedRating`, `isVerified`, `isPremium`. |
| `PackageDimensions` | `:core:domain/model/` | Flexible decode (Double/Int/String) + dual format (JSON object or stringified JSON). Used on `PackageRequest`, `AvailablePackage`, and in bookings `PackageRequestInfo`. |
| `CompatibilityDetails` | `:core:domain/model/` | `routeMatch`, `capacitySufficient`, `dateCompatible`, `priceCompatible`, `weightUsagePercentage`, `spaceUsagePercentage`. Used on `PackageRequest` compatibility fields and in bookings. |
| `Coordinates` | `:core:domain/model/` | `latitude: Double`, `longitude: Double`. Used in bookings but available to all features. |
| `PaginatedResponse<T>` | `:core:network` | Generic paginated wrapper. Used by all list endpoints. |
| `MatchStatus` | `:core:domain/enum/` | 12-case enum (canonical in 05-bookings). Used here via `CompatibleTrip.shipperRequestStatus` comparison. |
| `PackageRequestStatus` | `:core:domain/enum/` | 9 cases. Defined here, lives in shared module. |
| `PackageType` | `:core:domain/enum/` | 22 cases. Defined here, lives in shared module. |
| `UrgencyLevel` | `:core:domain/enum/` | 6 cases. Defined here, lives in shared module. |
| `ServiceType` | `:core:domain/enum/` | 5 cases. Defined here, lives in shared module. |
| `PackageSize` | `:core:domain/enum/` | 4 cases (computed from weight). |
| `TransportationMethod` | `:core:domain/enum/` | 10 cases. Defined in 03-trips, used here on `CompatibleTrip`. |
| `PricingType` | `:core:domain/enum/` | 2 cases. Defined in 03-trips, used here on `CompatibleTrip`. |
| `PricingMethod` | `:core:domain/enum/` | 3 cases. Defined in 03-trips, used here on `CompatibleTrip`. |
| `SortOrder` | `:core:domain/enum/` | `asc`/`desc`. Shared with trips browse. |
| `VerificationLevel` | `:core:domain/enum/` | 3 cases + `normalized()`. Used for user display across features. |
| `FlexibleDecoders` | `:core:domain/util/` | `flexibleDouble`, `flexibleBool`, `flexibleString` kotlinx.serialization deserializers. |
| `DateTimeParsing` | `:core:domain/util/` | `parseApiDate`, `parseApiDateTime`, `combineDateAndTime`, format helpers. |

## Models

### `PackageRequest` (core model — from [`PackageRequest.swift`](../../Pasabayan/Features/Packages/Models/PackageRequest.swift))

#### Core fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `shipperId` | Int? | `shipper_id` on wire — iOS also extracts from nested `shipper.id` if direct field missing |
| `pickupAddress` | String? | Optional — some partial-update endpoints omit it |
| `pickupCity` | String? | |
| `pickupCountry` | String? | |
| `deliveryAddress` | String? | |
| `deliveryCity` | String? | |
| `deliveryCountry` | String? | |
| `packageWeightKg` | Double? | Flexible decode: Double, Int, or String |
| `packageDimensions` | PackageDimensions? | length, width, height (each flexible decode); can arrive as JSON object **or JSON string** — see quirks |
| `packageType` | PackageType? | See PackageType enum; iOS decodes flexibly (trim, lowercase, fallback) |
| `fragile` | Bool? | Flexible decode: Bool, Int (0/1), String ("true"/"false"/"yes"/"no"/"1"/"0") |
| `packageValue` | Double? | Flexible decode: Double or String |
| `packageDescription` | String? | |
| `urgencyLevel` | UrgencyLevel? | See UrgencyLevel enum; iOS decodes flexibly |
| `maxPriceBudget` | Double? | Flexible decode: Double or String |
| `pickupDatePreferred` | String? | Date string — see date parsing in quirks |
| `pickupTimePreferred` | String? | |
| `pickupDateFlexible` | Bool? | Flexible decode |
| `deliveryDateNeeded` | String? | |
| `deliveryTimeNeeded` | String? | |
| `specialHandlingRequirements` | String? | |
| `requestStatus` | PackageRequestStatus? | See PackageRequestStatus enum |
| `createdAt` | String? | ISO8601 string |
| `updatedAt` | String? | ISO8601 string |
| `pickupCityId` | Int? | |
| `deliveryCityId` | Int? | |

#### Location coordinate fields

| Field | Type | Notes |
|-------|------|-------|
| `pickupLat` | String? | Backend sends as String; iOS decodes from Double or String → stores as String |
| `pickupLng` | String? | Same |
| `deliveryLat` | String? | Same |
| `deliveryLng` | String? | Same |

#### Image fields

| Field | Type | Notes |
|-------|------|-------|
| `images` | [PackageImage]? | Array of uploaded images |
| `imagesProcessing` | Bool? | Flexible decode; `true` while backend processes uploads — poll until false |

#### Service request fields (inline on PackageRequest)

| Field | Type | Notes |
|-------|------|-------|
| `serviceType` | String? | See ServiceType enum |
| `shoppingList` | String? | JSON string of shopping items; **can arrive as `[ShoppingItem]` array or String** — see quirks |
| `storeName` | String? | |
| `storeAddress` | String? | |
| `storeLat` | String? | Flexible: String, Double, or Int from backend |
| `storeLng` | String? | Same |
| `estimatedCost` | String? | Flexible: String, Double, or Int from backend |
| `receiptRequired` | Bool? | Flexible decode |

#### Related / nested data

| Field | Type | Notes |
|-------|------|-------|
| `shipper` | User? | Nested shipper object; also used to extract `shipperId` as fallback |
| `compatibleTripsCount` | Int? | `compatible_trips_count` — number of matching trips |

#### Compatibility fields (from compatible-packages endpoint)

These fields appear on `PackageRequest` when returned by the `/trips/{tripId}/compatible-packages` endpoint:

| Field | Type | Notes |
|-------|------|-------|
| `compatibilityScore` | Int? | Flexible decode: Int, String, or Double |
| `compatibilityDetails` | CompatibilityDetails? | Nested object |
| `estimatedEarnings` | String? | Flexible: String, Double, or Int |
| `distanceFromTripOrigin` | String? | Same |
| `distanceToTripDestination` | String? | Same |
| `distanceKm` | Double? | Flexible: Double or String |

#### Computed properties

| Property | Logic |
|----------|-------|
| `title` | `packageDescription ?? "{packageType.displayName} Package"` |
| `pickupLocation` | `[pickupAddress, pickupCity].filter { !empty }.joinedWith(", ")` |
| `deliveryLocation` | `[deliveryAddress, deliveryCity].filter { !empty }.joinedWith(", ")` |
| `isFragile` | `fragile ?? false` |
| `status` | `requestStatus ?? .open` |
| `packageSize` | Computed from weight — see PackageSize enum |
| `isServiceRequest` | `serviceType != nil && serviceType != "delivery"` |
| `shoppingItems` / `parsedShoppingList` | Parse `shoppingList` JSON string → `[ShoppingItem]` |
| `hasStoreLocation` | `storeLat != nil && storeLng != nil` |
| `pickupDatePreferredDate` | Parse via `parseAPIDate()` |
| `pickupDateTimePreferred` | Combine date + time |
| `deliveryDateNeededDate` | Parse via `parseAPIDate()` |
| `deliveryDateTimeNeeded` | Combine date + time |

### `PackageImage`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `packageRequestId` | Int | Non-optional in iOS |
| `imagePath` | String | May contain full URL from backend |
| `displayOrder` | Int | Non-optional in iOS |
| `originalFilename` | String? | |
| `url` | String | **Non-optional** — if `url` is missing in JSON, falls back to `imagePath` |
| `createdAt` | String? | |
| `updatedAt` | String? | |

### `PackageDimensions` — **shared** (`:core:domain/model/PackageDimensions.kt`)

| Field | Type | Notes |
|-------|------|-------|
| `length` | Double | Non-optional; Flexible decode: Double, Int, or String |
| `width` | Double | Same |
| `height` | Double | Same |

Also used by bookings (`PackageRequestInfo`). Dual format: JSON object or stringified JSON string.

### `ShoppingItem`

| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | Client-generated; **not** included in JSON encoding |
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
| `packageType` | PackageType | PackageType raw value |
| `fragile` | Bool | |
| `packageValue` | Double? | |
| `packageDescription` | String? | |
| `urgencyLevel` | UrgencyLevel | UrgencyLevel raw value |
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
| `deliveryDateNeeded` | String? | Date-only YYYY-MM-DD; must be today or future |
| `specialHandlingRequirements` | String? | |
| `clearSpecialHandlingRequirements` | Bool | **Not sent on wire** — controls encoding: when `true`, encodes `special_handling_requirements` as `null` so backend clears the value. Default `false`. |
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
| `PackageRequestResponse` | `{ success?: Bool, message: String, data: PackageRequest }` — `success` is optional (some endpoints omit it) |
| `PackageRequestsResponse` | `{ message: String, data: PaginatedPackageRequests }` |
| `AvailablePackagesResponse` | `{ success: Bool, message: String, nearby?: Bool, data: PaginatedResponse<AvailablePackage> }` |
| `CompatibleTripsAPIResponse` | `{ success?: Bool, message: String, data: PaginatedResponse<CompatibleTrip> }` |
| `CompatiblePackagesResponse` | `{ success?: Bool, message: String, data: CompatiblePackagesData }` — see flexible decoding note |

### `AvailablePackage` (browse model — **separate struct** from `PackageRequest`)

`AvailablePackage` is **not** a subclass of `PackageRequest` — it is its own struct with a subset of fields plus browse-specific fields. iOS provides a `toPackageRequest()` converter for UI reuse.

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | May differ from actual package request ID |
| `packageRequestId` | Int? | The actual `PackageRequest.id` — use this for API calls |
| `pickupCity` | String | |
| `pickupCountry` | String? | |
| `deliveryCity` | String | |
| `deliveryCountry` | String? | |
| `packageWeightKg` | Double | Flexible decode: Double or String |
| `packageDimensions` | PackageDimensions? | Can be JSON object or string |
| `volumeLiters` | Double? | Backend-calculated volume; flexible: Double or String |
| `urgencyLevel` | UrgencyLevel | Non-optional on browse |
| `urgencyDisplay` | String? | Backend-provided display string |
| `maxPriceBudget` | Double? | Flexible: Double or String |
| `pickupDatePreferred` | String | Defaults to `""` if missing |
| `pickupTimePreferred` | String? | |
| `pickupDateFlexible` | Bool | Defaults to `false` |
| `deliveryDateNeeded` | String | Defaults to `""` if missing |
| `deliveryTimeNeeded` | String? | |
| `fragile` | Bool | Non-optional on browse |
| `packageType` | PackageType | Non-optional on browse |
| `packageDescription` | String? | |
| `specialHandlingRequirements` | String? | |
| `createdAt` | String | |
| `daysSincePosted` | Double? | Flexible: Double or String |
| `shipper` | AvailablePackageShipper? | Simplified shipper info |
| `serviceType` | String? | |
| `shoppingList` | String? | Can be array or string — same as PackageRequest |
| `storeName` | String? | |
| `storeAddress` | String? | |
| `storeLat` | String? | Flexible: String, Double, or Int |
| `storeLng` | String? | Same |
| `estimatedCost` | String? | Same |
| `receiptRequired` | Bool? | |
| `distanceKm` | Double? | Flexible: Double or String |

**Computed:**
- `shoppingItems` / `parsedShoppingList` — same as PackageRequest
- `hasStoreLocation` — same as PackageRequest
- `isServiceRequest` — same as PackageRequest
- `toPackageRequest()` — converter for UI component reuse

### `AvailablePackageShipper` → **use `UserSummary`** from `:core:domain`

**Do not create a separate `AvailablePackageShipper` class.** Use the shared `UserSummary` type which has all fields optional except `id` + `name`. The API response fields map directly:

| API field | `UserSummary` field |
|-----------|-------------------|
| `id` | `id` |
| `name` | `name` |
| `email` | `email` |
| `avatar` | `avatar` |
| `rating` | `rating` (String) |
| `total_ratings` | `totalRatings` |
| `verification_level` | `verificationLevel` |
| `phone_verified` | `phoneVerified` |

All computed properties (`effectiveVerificationLevel`, `ratingValue`, `formattedRating`, `isVerified`, `isPremium`) are on `UserSummary`.

### `AvailablePackagesResult` (client-side wrapper)

| Field | Type | Notes |
|-------|------|-------|
| `packages` | [AvailablePackage] | Extracted from paginated response |
| `nearby` | Bool? | Response-level flag indicating GPS proximity filtering |

### Compatible trips models

| Type | Fields | Notes |
|------|--------|-------|
| `CompatibleTrip` | id, carrierId, originCity, originCountry, destinationCity, destinationCountry, departureDate, arrivalDate, availableWeightKg (String), availableSpaceLiters (String), pricePerKg (String), **flatTripPrice** (String?), **calculatedPrice** (String?), **pricingType** (String?), **pricingMethod** (String?), tripStatus, transportationMethod, specialNotes, **createdAt**, **updatedAt**, carrier (CarrierInfo), shipperRequestStatus, canRequest, requestMessage, **requestedAt**, distanceKm | Full trip for matching |
| `CarrierInfo` | id, name, email, avatar, phone, phoneVerified?, profileCompleted?, userTypes (array or dict), rating (String), totalRatings?, verificationLevel (normalized), isActiveCarrier?, isActiveShipper? | Extracted carrier details — handles `user_types` as both array and dictionary |
| `CompatibilityDetails` | routeMatch (String), capacitySufficient, dateCompatible, priceCompatible, weightUsagePercentage, spaceUsagePercentage | Match metrics |
| `CapacityUtilization` | weightKg (String), spaceLiters (String), weightPercentage (Double), spacePercentage (Double) | Nested in CompatibilitySummary |
| `CompatibilitySummary` | totalCompatible, perfectMatches, goodMatches, fairMatches, averageCompatibilityScore, totalPotentialEarnings (String), capacityUtilization (CapacityUtilization) | Aggregate stats |
| `CompatiblePackagesData` | tripInfo (CarrierTripInfo?), compatiblePackages (PaginatedResponse), compatibilitySummary? | Full response — **dual decode**: nested `compatible_packages` key OR direct pagination at data level |

**CompatibleTrip computed properties:**
- `usesFlatPricing: Bool` — checks `pricingType == "flat"` or fallback to `TransportationMethod.isLandTransport`
- `effectiveFlatPrice: Double?` — prefers `calculatedPrice`, falls back to `flatTripPrice`
- `canRequestTrip: Bool` — `canRequest ?? true`
- `hasActiveRequest: Bool` — `shipperRequestStatus == "shipper_requested"`

**`CarrierInfo` → use `UserSummary`** from `:core:domain`. The `carrier` field on `CompatibleTrip` deserializes into `UserSummary`. iOS's `CarrierInfo` fields (`id`, `name`, `email`, `avatar`, `phone`, `phoneVerified?`, `profileCompleted?`, `userTypes`, `rating` (String), `totalRatings?`, `verificationLevel`, `isActiveCarrier?`, `isActiveShipper?`) all map to `UserSummary` fields. Computed properties (`isVerified`, `isPremium`, `ratingValue`, `formattedRating`) live on `UserSummary`.

Note: `userTypes` may arrive as `["shipper"]` (array) or `{"0": "shipper"}` (dictionary). The `UserSummary` deserializer must handle both — see `FlexibleDecoders`.

## Enums (all raw values on wire — snake_case)

> **All enums below are defined in `:core:domain/enum/`**, not in `features/packages/model/`. This spec documents their cases and display properties for reference. Implementation lives in the shared module.

### `PackageType` (21 cases + 1 alias)

| Case | Raw value | Icon | Notes |
|------|-----------|------|-------|
| `general` | `"general"` | 📦 | |
| `electronics` | `"electronics"` | 💻 | |
| `clothing` | `"clothing"` | 👕 | |
| `books` | `"books"` | 📚 | |
| `food` | `"food"` | 🍕 | |
| `furniture` | `"furniture"` | 🪑 | |
| `medical` | `"medical"` | 💊 | |
| `documents` | `"documents"` | 📄 | |
| `fragile` | `"fragile"` | ⚠️ | iOS icon is ⚠️ not 🔮 |
| `parcel` | `"parcel"` | 📦 | |
| `gifts` | `"gifts"` | 🎁 | |
| `automotive` | `"automotive"` | 🚗 | |
| `beauty` | `"beauty"` | 💄 | |
| `sports` | `"sports"` | ⚽ | |
| `toys` | `"toys"` | 🧸 | |
| `household` | `"household"` | 🏠 | |
| `jewelry` | `"jewelry"` | 💎 | |
| `art` | `"art"` | 🎨 | |
| `industrial` | `"industrial"` | 🏭 | |
| `equipment` | `"equipment"` | 🔧 | |
| `other` | `"other"` | 📦 | iOS icon is 📦 not 📋 |

Note: iOS also handles `"document"` (singular) as a separate enum case mapping to the same icon as `documents`. Android should map both.

### `UrgencyLevel` (6 cases)

| Case | Raw value | Color | Icon |
|------|-----------|-------|------|
| `low` | `"low"` | **gray** | 🐌 |
| `normal` | `"normal"` | blue | 📦 |
| `high` | `"high"` | orange | ⚡ |
| `urgent` | `"urgent"` | red | 🚨 |
| `express` | `"express"` | purple | 🚀 |
| `flexible` | `"flexible"` | **teal** | 🔄 |

### `PackageRequestStatus` (9 cases)

| Case | Raw value | Color | Notes |
|------|-----------|-------|-------|
| `open` | `"open"` | blue | Available for requests |
| `pendingRequest` | `"pending_request"` | orange | Has pending carrier/shipper request |
| `matched` | `"matched"` | purple | Matched with carrier |
| `pickedUp` | `"picked_up"` | orange | Carrier picked up |
| `delivered` | `"delivered"` | green | Delivery complete |
| `cancelled` | `"cancelled"` | red | Cancelled |
| `pending` | `"pending"` | orange | Legacy |
| `booked` | `"booked"` | purple | Legacy |
| `inTransit` | `"in_transit"` | yellow | Legacy — iOS uses yellow not indigo |

### `ServiceType` (5 cases)

| Case | Raw value | SF Symbol | Badge color | `requiresCarOrMotorcycle` |
|------|-----------|-----------|-------------|--------------------------|
| `delivery` | `"delivery"` | shippingbox.fill | blue | **No** (the only false case) |
| `groceryShopping` | `"grocery_shopping"` | cart.fill | green | **Yes** |
| `foodDelivery` | `"food_delivery"` | takeoutbag.and.cup.and.straw.fill | orange | **Yes** |
| `pharmacyPickup` | `"pharmacy_pickup"` | cross.case.fill | red | **Yes** |
| `generalErrand` | `"general_errand"` | bag.fill | purple | **Yes** |

**Note:** iOS logic for `requiresCarOrMotorcycle` is `self != .delivery` — i.e. **all** non-delivery service types require it, not only `foodDelivery`.

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
| `standard` | 2 | 3 |
| `compact` | 2 | 2 |
| `package` | 2 | 3 |

Note: iOS has `maxDetailsPerRow = 2` for all variants (not 3 as previously documented).

## Endpoints

| Method | Path | Notes |
|--------|------|--------|
| GET | `/packages` | List own packages (paginated) |
| GET | `/packages/available?...` | Browse — see query params below |
| POST | `/packages` | JSON or **multipart** with `images[]` — see multipart fields below |
| GET | `/packages/{id}` | Get detail; also used for **image polling** |
| PUT | `/packages/{id}` | Update (JSON or multipart). Cancel: `{request_status: "cancelled"}` |
| POST | `/packages/{id}/cancel` | **Dedicated cancel endpoint** (iOS uses this) |
| DELETE | `/packages/{id}` | Delete (deprecated — prefer cancel via POST) |
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
- `cancelPackageRequest(id:)` — uses `POST /packages/{id}/cancel`
- `deletePackageRequest(id:)` — deprecated, prefer cancel
- `getPackageRequest(id:)` — single detail fetch
- `getCompatibleTrips(for:)` — loads compatible trips for matching
- `bookPackage(packageId:tripId:proposedPrice:)` — legacy booking
- `requestTripForPackage(...)` — service request handling
- `checkSimilarPackages(...)` — duplicate detection
- `checkExistingShipperRequest(packageId:tripId:completion:)` — checks if shipper already requested a trip
- `checkMatchForTripPackage(...)` — match compatibility check
- `pollForProcessedImages(packageId:attempts:)` — see image polling below
- `refreshData()` — refresh package data

**Status filter methods:**
- `packageRequests(with status:)` — filter by status
- `getPendingRequests()`, `getMatchedRequests()`, `getBookedRequests()`, `getInTransitRequests()`, `getPickedUpRequests()`, `getDeliveredRequests()`

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
| `PackageRequestView` (Shipper/) | Multi-step form for creating new package delivery requests with image upload and map | Shipper |
| `PackageHistoryView` | Delivered packages with spending history and timeline | Shipper |
| `EditPackageSheet` | Limited edits for open/pending packages (budget, urgency, dates, handling) | Shipper |
| `CreateServiceRequestView` | Multi-step service request wizard (type → items → delivery → confirm) | Shipper |
| `ReceiptUploadView` | Upload delivery receipt for completed service requests | Shipper |
| `PackageTutorialOverlay` | One-time tutorial for new shippers (`hasSeenPackageTutorial` flag) | Shipper |

### Reusable components

| Component | Purpose |
|-----------|---------|
| `PackageRequestCard` | Package summary card for lists |
| `DeliveryCard` | Complex card with variant layouts (standard/compact/package) |
| `DeliveryCardSubComponents` | CardHeader, UserInfoSection, RouteSection, DetailsGrid, SpecialContentSection, ActionFooter |
| `RequestStatusBadge` | Status badge with colors per PackageRequestStatus (standard, compact, icon-only variants) |
| `ShipperRequestStatusBadge` | Shipper-specific request status display |
| `PackageImagesSection` | Image gallery (up to 5 images) |
| `PackageImagePreview` | Single image preview with zoom |
| `PackageDetailsSection` | Weight, dimensions, type, fragile display |
| `DeliveryInformationSection` | Delivery address, date, time |
| `PickupInformationSection` | Pickup details |
| `PackageDetailCards` | MatchInfoCard, CarrierDetailsCard, DeliveryRouteCard, TripDetailsCard, RouteRow, InfoRow |
| `PackageDetailComponents` | Reusable detail component building blocks |
| `PackageRequestFormState` | State management for package creation form |
| `PackageRequestFormActions` | Submit/cancel action buttons |
| `SavedPackageRouteSheet` | Display/reuse saved pickup + handoff templates |
| `ServiceTypePicker` | Service type selection with icons |
| `ShoppingListForm` | Add/edit shopping items |
| `DeliveryCityCatalog` | City selection for delivery |
| `ItemCatalog` | Browse items for shopping list |
| `InteractiveMapView` | Store/delivery location picker |

## Android component unification plan

iOS has significant component duplication that Android should **avoid** by design. The following patterns consolidate iOS's scattered implementations.

### 1. Unified status badge (Priority: Critical)

iOS duplicates status → color/text mapping across `RequestStatusBadge` (3 variants × full switch), `ShipperRequestStatusBadge`, and inline switches. **Android approach:**

```
:core:designsystem  →  PStatusBadge(config: StatusDisplayConfig, variant: BadgeVariant)

interface StatusDisplayConfig {
    val displayText: String       // from stringResource
    val backgroundColor: Color
    val textColor: Color
    val icon: String?
}
```

All status enums (`PackageRequestStatus`, `ShipperRequestStatus`, `BookingStatus`, `TripStatus`, `MatchStatus`) implement `StatusDisplayConfig`. Single `PStatusBadge` composable with `BadgeVariant.Standard | Compact | IconOnly`.

### 2. Unified card components (Priority: High)

iOS has `DeliveryCard`, `PackageRequestCard`, and `PackageDetailCards` with overlapping row/section patterns. **Android approach:**

| Unified component | Replaces iOS... | Location |
|-------------------|-----------------|----------|
| `PDetailRow(label, value, color?)` | `InfoRow`, inline HStack label+value patterns | `:core:designsystem` |
| `PRouteSection(origin, destination, icons?)` | `RouteSection` in DeliveryCard, `RouteRow` in DetailCards | `:core:designsystem` |
| `PUserInfoSection(avatar, name, rating?, verificationLevel?)` | `DeliveryCardUserInfoSection`, `CarrierDetailsCard` user block | `:core:designsystem` |
| `PDetailsGrid(items, columns)` | `DetailsGrid` in DeliveryCard, inline grids in detail views | `:core:designsystem` |

Feature-level cards (`PackageRequestCard`, `DeliveryCard`) compose from these shared primitives.

### 3. Centralized date/time formatting (Priority: High)

iOS has 5+ date formatting implementations scattered across form state, detail components, and card components. **Android approach:**

```
:core:common → DateTimeFormatting object
  - parseApiDate(value: String): LocalDate?
  - parseApiDateTime(value: String): LocalDateTime?
  - combineDateAndTime(date: LocalDate, time: String?): LocalDateTime?
  - formatDateOnly(date: LocalDate): String
  - formatDateTime(dateTime: LocalDateTime): String
  - parseTimeString(value: String): LocalTime?
```

Supports the same format fallback chain as iOS (ISO8601 with/without fractional, microseconds, date-only, timezone variants).

### 4. Shared verification display logic (Priority: Medium)

iOS repeats verification icon/color logic in `CarrierDetailsCard`, `CarrierInfo`, and `AvailablePackageShipper`. **Android approach:**

```
:core:designsystem → VerificationDisplay
  - VerificationLevel.normalized(raw: String?): String  // "basic" | "verified" | "premium"
  - VerificationLevel.icon: ImageVector
  - VerificationLevel.color: Color
```

### 5. Shared transportation icon mapping (Priority: Low)

iOS duplicates transport icon mapping in `DeliveryRouteCard` and `TripDetailsCard`. **Android approach:**

```
:core:common → TransportationMethod.icon: ImageVector
```

### 6. Service type badge colors (Priority: Low)

Duplicated in `PackageRequestCard` and inline views. Belongs on the `ServiceType` enum's `badgeColor` property.

### Component file structure (Android)

```
# Shared types (NOT in features/packages/) — imported via :core:domain
# :core:domain/model/   → UserSummary, PackageDimensions, CompatibilityDetails, Coordinates
# :core:domain/enum/    → PackageType, UrgencyLevel, PackageRequestStatus, ServiceType,
#                         PackageSize, MatchStatus, TransportationMethod, PricingType,
#                         PricingMethod, SortOrder, VerificationLevel
# :core:domain/util/    → FlexibleDecoders, DateTimeParsing
# :core:network         → PaginatedResponse<T>

features/packages/
├── model/
│   ├── PackageRequest.kt          # Core model (uses shared enums + PackageDimensions)
│   ├── AvailablePackage.kt        # Browse model + toPackageRequest() (shipper = UserSummary)
│   ├── CompatibleTrip.kt          # carrier = UserSummary (no separate CarrierInfo)
│   ├── CompatibilitySummary.kt    # Summary + CapacityUtilization (feature-specific)
│   ├── CreatePackageRequest.kt
│   ├── PackageUpdateRequest.kt
│   ├── CreateServiceRequestBody.kt
│   ├── PackageImage.kt
│   ├── ShoppingItem.kt
│   ├── DeliveryCardModels.kt      # UI-only: DeliveryCardData, variants, etc.
│   └── PackageResponses.kt        # All response wrappers
├── services/
│   ├── PackagesApiService.kt      # Retrofit interface
│   ├── PackagesRepository.kt      # Repository with caching
│   ├── ShipperDisclaimerStore.kt
│   ├── SavedPackageDescriptionsStore.kt
│   └── SavedPackageRouteTemplatesStore.kt
├── viewmodel/
│   └── PackageViewModel.kt
├── ui/
│   ├── PackageListScreen.kt
│   ├── PackageDetailScreen.kt
│   ├── PackageRequestScreen.kt    # Create form
│   ├── PackageHistoryScreen.kt
│   ├── EditPackageSheet.kt
│   ├── CreateServiceRequestScreen.kt
│   ├── ReceiptUploadScreen.kt
│   └── PackageTutorialOverlay.kt
└── components/
    ├── PackageRequestCard.kt       # Composes PDetailRow, PRouteSection, PStatusBadge
    ├── DeliveryCard.kt             # Composes shared primitives per variant
    ├── PackageImagesSection.kt
    ├── PackageImagePreview.kt
    ├── PackageDetailsSection.kt    # Form section
    ├── PickupInformationSection.kt
    ├── DeliveryInformationSection.kt
    ├── PackageDetailCards.kt       # MatchInfoCard, CarrierDetailsCard, etc.
    ├── SavedPackageRouteSheet.kt
    ├── ServiceTypePicker.kt
    ├── ShoppingListForm.kt
    ├── DeliveryCityCatalog.kt
    ├── ItemCatalog.kt
    └── InteractiveMapView.kt
```

**Key difference from iOS:** No `DeliveryCardSubComponents` file — those become shared primitives in `:core:designsystem`. No duplicated `PackageDetailComponents` — use `PDetailRow` etc. directly.

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

- **Flexible type decoding:** `packageWeightKg` decodes from Double, Int, or String. `fragile` decodes from Bool, Int (0/1), or String ("true"/"false"/"yes"/"no"). `packageValue` and `maxPriceBudget` decode from Double or String. Android kotlinx.serialization custom deserializers needed.
- **`packageDimensions` dual format:** Backend sends as JSON object **or** JSON string. If string, parse the string as JSON into the `PackageDimensions` struct. Android needs a custom serializer that tries object first, then string parse.
- **`shoppingList` dual format:** Backend sends as `[ShoppingItem]` array **or** JSON string. iOS converts array to string on decode. Android should handle both.
- **`storeLat`/`storeLng`/`estimatedCost` flexible types:** Backend sends as String, Double, or Int. iOS stores as String after conversion. Android should normalize to String.
- **`shipperId` fallback:** If `shipper_id` field is missing, extract from nested `shipper.id` object.
- **Date parsing (multiple formats):** try in order: date-only (`yyyy-MM-dd`), ISO8601 with fractional seconds, ISO8601 without fractional, microseconds (`yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'`), then extract date prefix from longer strings.
- **Time string parsing:** supports `HH:mm`, `H:mm`, `HH:mm:ss`, `H:mm:ss`, `HH:mm:ss.SSSSSS`, `h:mm a`, `h:mm:ss a`, plus manual colon-split fallback.
- **Image polling** after creation — see ViewModel section above.
- **Cancel endpoint:** iOS uses `POST /packages/{id}/cancel` as dedicated endpoint. PUT with `request_status: cancelled` also works but prefer the dedicated endpoint.
- **`deletePackageRequest`** is deprecated in iOS — prefer `cancelPackageRequest`.
- **Partial PUT updates** (`updatePackageDetailsWithImages`) sends only changed fields in multipart, not the full object.
- **`clearSpecialHandlingRequirements` encoding:** When `true` on `PackageUpdateRequest`, `special_handling_requirements` is encoded as explicit `null` (not omitted) so backend clears the value.
- **Activity logging** — ViewModel calls `ActivityLogger.shared.logPackageAction()` after create/cancel (see [12-legal-support-misc.md](12-legal-support-misc.md) for payload).
- **Shipper role auto-enable** — after first package creation, `enableShipperRoleIfNeeded()` is called.
- **`documents` enum** — iOS handles both `"document"` (singular) and `"documents"` (plural) raw values; map both.
- **`AvailablePackage.id` vs `packageRequestId`** — the `id` field on `AvailablePackage` may differ from the actual package request ID. Always use `packageRequestId ?? id` for API calls.
- **`AvailablePackagesResponse.nearby`** — optional Bool flag indicating results are filtered by GPS proximity. Pass to UI for display context.
- **`CompatiblePackagesData` dual decode** — backend may return pagination nested under `compatible_packages` key, or directly at the data level. Handle both shapes.
- **`user_types` dual format** — `CarrierInfo.userTypes` can be `["shipper"]` (array) or `{"0": "shipper"}` (dictionary from backend). Parse both.

## TDD checklist

- [x] `PackageRequest` JSON decode — all fields including images, service fields, flexible types, coordinate fields, compatibility fields.
- [x] `PackageRequest` — `shipperId` fallback from nested `shipper.id`.
- [x] `CreatePackageRequest` encode — all fields.
- [x] `PackageUpdateRequest` partial update — verify only changed fields sent; verify `clearSpecialHandlingRequirements` encodes null.
- [x] `CreateServiceRequestBody` encode — all fields including shopping list.
- [x] All enums decode from raw string values (especially `PackageType` 21+1 cases, `PackageRequestStatus` 9 cases, `ServiceType` 5 cases).
- [x] `AvailablePackage` decode with `distanceKm`, `daysSincePosted`, shipper, `packageRequestId`, `volumeLiters`, service fields.
- [x] `AvailablePackage.toPackageRequest()` converter — all fields mapped correctly.
- [x] `AvailablePackageShipper` decode with computed properties (ratingValue, effectiveVerificationLevel).
- [x] `CompatibleTrip` + `CarrierInfo` + `CompatibilityDetails` decode — including pricing fields and `user_types` dual format.
- [x] `CapacityUtilization` decode.
- [x] `CompatiblePackagesData` — test both nested and direct pagination shapes.
- [x] Multipart field names match iOS (22 text fields + images array).
- [x] Browse query params: verify all 17 params serialize correctly as snake_case.
- [x] Image polling: mock 3-attempt poll until `imagesProcessing == false`.
- [x] Flexible type decoding: Double/Int/String for weight; Bool/Int/String for fragile; object/string for dimensions; array/string for shoppingList.
- [x] Local stores: descriptions (max 15, FIFO, dedup), templates (max 5), disclaimer (offline sync).
- [x] Service request ViewModel: type selection, shopping list, store location, validation.
- [x] Date parsing: all format variants (date-only, ISO8601, fractional, microseconds).
- [x] `AvailablePackagesResult.nearby` flag preserved through response parsing.
