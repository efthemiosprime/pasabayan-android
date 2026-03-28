# 05 — Bookings, matches, compatibility, receipts

**Phase:** 3 | **Feature:** Bookings | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Largest API surface: [`BookingsAPIService.swift`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift) (2,600+ lines, 39+ methods), models in [`BookingModels.swift`](../../Pasabayan/Features/Bookings/Models/BookingModels.swift), [`MatchingModels.swift`](../../Pasabayan/Features/Bookings/Models/MatchingModels.swift), [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift), [`IncomingRequestContext.swift`](../../Pasabayan/Features/Bookings/Models/IncomingRequestContext.swift), [`ReceiptModels.swift`](../../Pasabayan/Features/Bookings/Models/ReceiptModels.swift), [`ReceiverAccessModels.swift`](../../Pasabayan/Features/Bookings/Models/ReceiverAccessModels.swift). 64 view files, 3 ViewModels.

## Enums

### `MatchStatus` (primary state machine — 12 cases)

| Case | Raw value | Meaning |
|------|-----------|---------|
| `pending` | `"pending"` | Match created, awaiting response |
| `confirmed` | `"confirmed"` | Both parties accepted |
| `pickedUp` | `"picked_up"` | Package picked up |
| `inTransit` | `"in_transit"` | In delivery (carrier sharing location) |
| `delivered` | `"delivered"` | Delivery confirmed |
| `cancelled` | `"cancelled"` | Cancelled by either party |
| `carrierRequested` | `"carrier_requested"` | Carrier initiated request to shipper |
| `shipperRequested` | `"shipper_requested"` | Shipper initiated request to carrier |
| `shipperAccepted` | `"shipper_accepted"` | Shipper accepted carrier's request |
| `shipperDeclined` | `"shipper_declined"` | Shipper declined |
| `carrierAccepted` | `"carrier_accepted"` | Carrier accepted shipper's request |
| `carrierDeclined` | `"carrier_declined"` | Carrier declined |

### `BookingStatus` (simplified — 6 cases, mapped from MatchStatus)

| Case | Raw value | Maps from MatchStatus |
|------|-----------|----------------------|
| `pending` | `"pending"` | pending, carrierRequested, shipperRequested |
| `confirmed` | `"confirmed"` | confirmed, shipperAccepted, carrierAccepted |
| `pickedUp` | `"picked_up"` | pickedUp |
| `inTransit` | `"in_transit"` | inTransit |
| `delivered` | `"delivered"` | delivered |
| `cancelled` | `"cancelled"` | cancelled, shipperDeclined, carrierDeclined |

### `InitiatedBy` (3 cases)

| Case | Raw value |
|------|-----------|
| `shipper` | `"shipper"` |
| `carrier` | `"carrier"` |
| `unknown` | `"unknown"` |

### `BookingType` (3 cases)

| Case | Raw value | Purpose |
|------|-----------|---------|
| `spaceOnly` | `"space_only"` | Book cargo space on existing trip |
| `fullService` | `"full_service"` | Full pickup/delivery service |
| `passenger` | `"passenger"` | Passenger/ride-sharing |

### `TripStatusError` (6 cases — trip unavailability reasons)

| Case | Raw value |
|------|-----------|
| `planning` | `"planning"` |
| `completed` | `"completed"` |
| `cancelled` | `"cancelled"` |
| `inTransit` | `"in_transit"` |
| `departed` | `"departed"` |
| `unavailable` | `"unavailable"` |

## Models

### `DeliveryMatch` (primary model — ~50+ properties)

#### Core fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `carrierTripId` | Int | |
| `packageRequestId` | Int | |
| `carrierId` | Int? | |
| `shipperId` | Int? | |
| `agreedPrice` | String | **API returns as string** `"150.00"` — compute `agreedPriceValue: Double` |
| `status` | String? | |
| `matchStatus` | String | Raw status string |

#### Status timestamps

| Field | Type |
|-------|------|
| `confirmedAt` | String? |
| `pickedUpAt` | String? |
| `deliveredAt` | String? |
| `carrierRequestedAt` | String? |
| `shipperRequestedAt` | String? |
| `shipperRespondedAt` | String? |
| `carrierRespondedAt` | String? |
| `createdAt` | String? |
| `updatedAt` | String? |

#### Messages & photos

| Field | Type |
|-------|------|
| `carrierMessage` | String? |
| `shipperMessage` | String? |
| `declineReason` | String? |
| `pickupPhoto` | String? |
| `deliveryPhoto` | String? |
| `receiptPhoto` | String? |

#### Codes

| Field | Type | Notes |
|-------|------|-------|
| `pickupConfirmationCode` | String? | 6-digit pickup code |
| `deliveryConfirmationCode` | String? | 6-digit delivery code |
| `shipperGeneratedCode` | String? | |
| `deliveryVerificationCode` | String? | |
| `deliveryCodeExpiresAt` | String? | |
| `deliveryCodeGeneratedAt` | String? | |
| `deliveryCodeUsedAt` | String? | |

#### Auto-charge & pricing

| Field | Type | Notes |
|-------|------|-------|
| `autoCharge` | AutoChargeInfo? | `{queued, shipperHasDefaultPaymentMethod}` |
| `transaction` | MatchTransaction? | Payment transaction details |
| `transactionStatus` | String? | |
| `carrierExpectedPrice` | String? | |
| `priceDifference` | String? | |
| `isBelowRate` | Bool? | |
| `platformFeePercent` | Double? | Can be percentage (10) or decimal (0.10) — normalize |
| `autoCancelAfterDays` | Int? | Default 10 if nil |
| `autoCancelledAt` | String? | |
| `autoCancelled` | Bool? | |

#### Counter-offer fields

| Field | Type | Notes |
|-------|------|-------|
| `isCounterOffer` | Bool | |
| `originalPrice` | String? | |
| `counterOffererId` | Int? | |
| `counterOffererName` | String? | |
| `counterOfferRound` | Int? | |
| `remainingCounterOffers` | Int? | |
| `canCounterOffer` | Bool? | |
| `initiatedBy` | InitiatedBy | |

#### Location tracking

| Field | Type |
|-------|------|
| `carrierCurrentLat` | Double? |
| `carrierCurrentLng` | Double? |
| `locationLastUpdatedAt` | String? |

#### Related entities

| Field | Type |
|-------|------|
| `packageRequest` | PackageRequest? |
| `carrierTrip` | Trip? |
| `carrier` | User? |
| `shipper` | User? |
| `chatConversationId` | Int? |
| `trackingNotes` | [TrackingNote]? |

### `Booking` (~40 properties)

Similar to `DeliveryMatch` but with `BookingStatus` enum. Key additional fields:

| Field | Type | Notes |
|-------|------|-------|
| `pickupLocation` | String? | |
| `deliveryLocation` | String? | |
| `pickupCoordinates` | Coordinates? | |
| `deliveryCoordinates` | Coordinates? | |
| `scheduledPickupDate/Time` | String? | |
| `actualPickupTime` | String? | |
| `estimatedDeliveryTime` | String? | |
| `actualDeliveryTime` | String? | |
| `trackingNumber` | String? | |
| `rawMatchStatus` | String? | For permission logic |

**Permission helpers:**
- `canAccept(currentUserId:) → Bool` — checks rawMatchStatus vs userId
- `statusDisplay(for context: BookingStatusContext) → String` — context-aware (shipper/carrier/neutral) display text

### `AutoChargeInfo`

| Field | Type |
|-------|------|
| `queued` | Bool |
| `shipperHasDefaultPaymentMethod` | Bool |

### `MatchTransaction`

| Field | Type |
|-------|------|
| `id` | Int |
| `status` | String |
| `currency` | String |
| `totalAmount` | Double |
| `platformFee` | Double |
| `carrierAmount` | Double |
| `requiresActionAt` | String? |
| `errorCode` | String? |
| `errorMessage` | String? |
| `createdAt` | String? |

### `TrackingNote`

| Field | Type |
|-------|------|
| `id` | Int? |
| `matchId` | Int |
| `note` | String |
| `timestamp` | String? |

### Counter-offer request/response types

**`ShipperCounterOfferRequest`** (shipper negotiates on carrier's trip):

| Field | Type | Notes |
|-------|------|-------|
| `offeredPrice` | Double | New price |
| `message` | String? | |
| `isCounterOffer` | Bool | Always `true` |
| `originalMatchId` | Int | Previous match ID |
| `originalPrice` | Double | |
| `counterOfferRound` | Int? | |

**`CarrierCounterOfferRequest`** (carrier negotiates for shipper's package):

| Field | Type | Notes |
|-------|------|-------|
| `proposedPrice` | Double | New price |
| `message` | String? | |
| `isCounterOffer` | Bool | Always `true` |
| `originalMatchId` | Int | |
| `originalPrice` | Double | |
| `counterOfferRound` | Int? | |

**`CounterOfferResponse`:**

| Field | Type |
|-------|------|
| `message` | String |
| `data` | DeliveryMatch |
| `warnings` | [String]? |
| `negotiationNeeded` | Bool? |
| `isCounterOffer` | Bool? |

### `CounterOfferContext` (UI state — from [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift))

| Field | Type |
|-------|------|
| `newPrice` | Double |
| `originalPrice` | Double |
| `priceDifference` | Double |
| `direction` | String (`"up"` / `"down"`) |
| `counterOffererName` | String |
| `counterOffererId` | Int |
| `counterOfferRound` | Int? |
| `initiatedBy` | InitiatedBy |
| `remainingCounterOffers` | Int? |
| `canCounterOffer` | Bool? |

**Factory methods:** `from(notificationData:)`, `from(match: DeliveryMatch)`, `from(booking: Booking)`

### `IncomingRequestContext` (from [`IncomingRequestContext.swift`](../../Pasabayan/Features/Bookings/Models/IncomingRequestContext.swift))

| Field | Type |
|-------|------|
| `matchId` | Int |
| `requesterName` | String |

**Logic:** For carrier → incoming = `shipper_requested`; for shipper → incoming = `carrier_requested`. Excludes counter-offers.

### Pickup/delivery code models

**`PickupCodeData`:** `confirmationCode`, `expiresAt`, `matchId`, `shipperId`, `carrierId`, `sentToChat`

**`DeliveryCodeData`:** `verificationCode`, `expiresAt`, `matchId`, `shipperId`, `carrierId`, `sentToChat`, `packageOrService`

### Receiver access models

**`ReceiverAccessToken`:** `id`, `shortCode`, `shortUrl`, `hasPin`, `pin`, `pinNotice`, `isActive`, `accessCount`, `trackingUrl`, `firstAccessedAt`, `lastAccessedAt`, `createdAt`

**`CreateReceiverAccessRequest`:** `generatePin: Bool`, `receiverName: String?`

### Location tracking models

**`UpdateLocationRequest`:** `latitude: Double`, `longitude: Double`, `accuracy: Double?`

**`CarrierLocationResponse`:** `matchId`, `carrier` (basic info), `currentLocation` (`{latitude, longitude, lastUpdatedAt, isStale}`), `deliveryAddress` (`{address, city, latitude: String, longitude: String}` — note: strings!), `matchStatus`

### Receipt models

**`ReceiptUploadResponse`:** `success`, `message`, `data: {receiptPhoto, receiptUrl}`

**`ReceiptResponse`:** `success`, `data: {receiptPhoto, receiptUrl, uploadedAt}`

## Endpoints (full inventory — 39+ methods)

### Match listing

| Method | Path | Notes |
|--------|------|-------|
| GET | `/matches?role=shipper` | Shipper's matches |
| GET | `/matches?role=carrier` | Carrier's matches; optional `&status={status}` filter |
| GET | `/matches` | All matches |
| GET | `/matches/pending-requests` | Pending shipper requests |
| GET | `/matches/{matchId}` | Single match detail |

### Match creation & status transitions

| Method | Path | Response | Notes |
|--------|------|----------|-------|
| POST | `/matches` | `DeliveryMatch` | Create match |
| PUT | `/matches/{matchId}/confirm` | `MatchConfirmResponse` | **Returns `auto_charge` at root level** |
| PUT | `/matches/{matchId}/pickup` | `DeliveryMatch` | Body: `{confirmation_code?, pickup_photo?}` |
| PUT | `/matches/{matchId}/transit` | `DeliveryMatch` | |
| PUT | `/matches/{matchId}/deliver` | `DeliveryMatch` | Body: `{verification_code?, delivery_photo?, receipt_photo?}` |
| DELETE | `/matches/{matchId}` | `CancelMatchResponse` | Returns refund info |

### Accept/decline flows

| Method | Path | Notes |
|--------|------|-------|
| POST | `/matches/{matchId}/accept` | Shipper accepts carrier's request |
| POST | `/matches/{matchId}/decline` | Shipper declines carrier's request |
| POST | `/matches/{matchId}/accept-shipper-request` | Carrier accepts shipper's request |
| POST | `/matches/{matchId}/decline-shipper-request` | Carrier declines shipper's request |

### Code generation

| Method | Path | Response |
|--------|------|----------|
| POST | `/matches/{matchId}/generate-pickup-code` | `PickupCodeResponse` |
| POST | `/matches/{matchId}/generate-delivery-code` | `DeliveryCodeResponse` |

### Location tracking

| Method | Path | Notes |
|--------|------|-------|
| GET | `/matches/{matchId}/carrier-location` | Shipper polls carrier position |
| POST | `/matches/{matchId}/location` | Carrier posts `{latitude, longitude, accuracy}` every 30s |

### Rating

| Method | Path |
|--------|------|
| POST | `/matches/{matchId}/rate` |
| GET | `/matches/{matchId}/rating-status` |

### Receiver access

| Method | Path | Notes |
|--------|------|-------|
| GET | `/matches/{matchId}/receiver-access` | List tokens |
| POST | `/matches/{matchId}/receiver-access` | Create: `{generate_pin, receiver_name?}` → `ReceiverAccessToken` |
| DELETE | `/matches/{matchId}/receiver-access/{tokenId}` | Revoke |

### Receipts

| Method | Path | Notes |
|--------|------|-------|
| POST | `/services/matches/{matchId}/receipt` | Multipart image upload |
| GET | `/services/matches/{matchId}/receipt` | Retrieve receipt |

### Auto-charge

| Method | Path |
|--------|------|
| POST | `/payments/matches/{matchId}/auto-charge` | Retry failed payment |

### Trips ↔ packages

| Method | Path | Notes |
|--------|------|-------|
| POST | `/trips/{tripId}/book` | Shipper books trip directly |
| POST | `/bookings` | Direct booking |
| GET | `/trips/{tripId}/matches` | All matches for trip |
| GET | `/trips/{tripId}/compatibility/{packageId}` | Compatibility check |
| GET | `/trips/{tripId}/compatible-packages` | Packages matching trip |
| POST | `/trips/{tripId}/packages/{packageId}/request` | Carrier requests package (or counter-offer with `is_counter_offer: true`) |
| POST | `/trips/{tripId}/packages/{packageId}/accept` | Carrier accepts package |
| POST | `/trips/{tripId}/packages/{packageId}/reject` | Carrier rejects package |
| GET | `/packages/{packageId}/compatible-trips` | Compatible trips for package |
| POST | `/packages/{packageId}/request-trip/{tripId}` | Shipper requests trip (or counter-offer with `is_counter_offer: true`) |
| POST | `/packages/{packageId}/service-offer` | Generic service offer |

### Counter-offer (reuse same paths)

| Role | Method | Path | Request type |
|------|--------|------|-------------|
| Shipper | POST | `/packages/{packageId}/request-trip/{tripId}` | `ShipperCounterOfferRequest` (with `is_counter_offer: true`) |
| Carrier | POST | `/trips/{tripId}/packages/{packageId}/request` | `CarrierCounterOfferRequest` (with `is_counter_offer: true`) |

## Status transition diagram

```
INITIATION:
  Carrier → Shipper:  POST /trips/{}/packages/{}/request  →  carrier_requested
  Shipper → Carrier:  POST /packages/{}/request-trip/{}    →  shipper_requested

ACCEPTANCE:
  Shipper accepts:    POST /matches/{}/accept              →  confirmed
  Carrier accepts:    POST /matches/{}/accept-shipper-req   →  confirmed

CONFIRMATION (payment):
  PUT /matches/{}/confirm  →  confirmed (returns auto_charge)

DELIVERY:
  PUT /matches/{}/pickup   →  picked_up
  PUT /matches/{}/transit   →  in_transit  (carrier broadcasts GPS every 30s)
  PUT /matches/{}/deliver   →  delivered

CANCELLATION (any time):
  POST /matches/{}/decline                →  cancelled
  POST /matches/{}/decline-shipper-req    →  cancelled
  DELETE /matches/{}                      →  cancelled (with refund)

COUNTER-OFFER (from any non-confirmed state):
  Same endpoints as initiation + is_counter_offer: true + original_match_id
  Loop until accepted, declined, or remaining_counter_offers exhausted
```

## ViewModels (iOS reference — mirror on Android)

### `MatchingViewModel` (core — ~1,000+ lines)

**State:**
- `matches: [DeliveryMatch]` — current matches
- `isLoading`, `errorMessage`
- `matchAccepted: Bool`
- `reviewedIncomingRequestIds: Set<Int>` — dismissed snackbars
- `lastAutoChargeInfo: AutoChargeInfo?`

**Actions:** All match listing (by role, status, pending), all status transitions, accept/decline (both roles), counter-offer submission, auto-charge retry, receiver access CRUD.

**Badge logic:** `unseenIncomingRequestBadgeCountForShipper(matches:, reviewedRequestIds:) → Int`

### `LiveTrackingViewModel` (GPS tracking)

**State:**
- `carrierLocation: CLLocationCoordinate2D?`
- `deliveryLocation`, `mapRegion`
- `remainingDistance`, `totalDistance`, `deliveryProgress`
- `estimatedMinutes`, `lastUpdateTime`

**Behavior:** Polls once on `startTracking()`, calculates distance/ETA (40 km/h average), centers map between both markers. Stale if >10 minutes old.

### `AutoChargeConfirmationViewModel` (payment state machine)

**States:** `idle → checkingPaymentMethod → needsPaymentMethod | readyToConfirm → confirming → success | error`

**Flow:** Check default payment method → if none show "Add Payment" → if ready show "Confirm" → call confirm → handle auto_charge response.

## UI (iOS reference — 64 view files)

### Carrier screens (11 files)

| View | Purpose |
|------|---------|
| `BookingListView` | Main carrier hub — active bookings + incoming requests + counter-offers |
| `CarrierBrowsePackagesView` | Browse available packages to carry |
| `CarrierMatchDetailsView` | Match details with accept/decline/counter-offer actions |
| `CarrierMatchRequestsView` | Incoming shipper requests |
| `CompatiblePackagesForTripView` | Packages matching carrier's trip |
| `RequestToCarrySheet` | Modal: carrier makes offer on package |
| `DirectBookingSheet` | Modal: direct booking without negotiation |
| `MatchCreationView` | Create match from trip/package |
| `MatchListView` | All matches with filtering |
| `RequestToCarryBudgetReminder` | Budget warning banner |

### Shipper screens (7 files)

| View | Purpose |
|------|---------|
| `ShipperMatchesView` | Main shipper hub — matches + incoming requests + counter-offers |
| `ShipperMatchDetailsView` | Match details with actions |
| `CompatibleTripsView` | Browse compatible trips for package (with pricing policy) |
| `ShareWithReceiverSheet` | Modal: generate receiver tracking link |
| `RateDeliverySheet` | Modal: post-delivery rating |

### Delivery code screens (3 files)

| View | Purpose |
|------|---------|
| `GeneratePickupCodeView` | Shipper generates 6-digit pickup code |
| `GenerateDeliveryCodeView` | Carrier generates 6-digit delivery code |
| `ConfirmDeliveryCodeView` | Verify delivery code entry |

### Shared screens (5 files)

| View | Purpose |
|------|---------|
| `BookingDetailsView` / `BookingDetailsContentView` | Full booking detail (both roles) |
| `BookingSuccessView` | Completion confirmation |
| `CounterOfferPromptView` | Modal: make/accept counter-offer |
| `AutoChargeConfirmationSheet` | Modal: confirm payment |

### History (2 files)

| View | Purpose |
|------|---------|
| `DeliveryHistoryView` | Past deliveries and earnings |
| `HistoryRowCard` | History item card |

### Reusable components (25+ files)

**Cards:** `BookingCard` (with sub-components: Header, RouteSection, DetailsSection, QuickActions, ShipperSection), `MatchCard`, `CarrierRequestCard`, `ShipperMatchCard`, `TripWithRequestsCard`, `ChatAccessCard`, `RouteInformationCard`, `CarrierPackageCard`

**Status:** `BookingStatusBadge`, `MatchStatusBadge`, `UrgencyBadge`

**Counter-offer:** `CounterOfferBanner`, `CounterOfferSnackbar`, `CounterOfferStatusBanner`, `PriceComparisonSection`, `PriceDisplayView`

**Other:** `IncomingRequestSnackbar`, `AutoChargeFailureBanner`, `LiabilityWarningBanner`, `BookingLocationRow`, `FilterChip`, `StatusFilterChip`, `BookingDeliveryCodeView`, `BookingPickupCodeView`

## Carrier location tracking (live delivery)

During active delivery, the carrier's device posts GPS coordinates every 30 seconds via [`CarrierLocationService.swift`](../../Pasabayan/Services/CarrierLocationService.swift). See [00-architecture.md](00-architecture.md#carrier-location-service-gps-tracking) for full config.

| Direction | Method | Path | Notes |
|-----------|--------|------|-------|
| Carrier → server | POST | `/matches/{matchId}/location` | Body: `{latitude, longitude, accuracy}` every 30s |
| Shipper ← server | GET | `/matches/{matchId}/carrier-location` | Polling-based; shipper reads carrier position |

Android: use a **foreground service** with notification during active delivery.

## Quirks

- **`agreedPrice` is a String** — API returns `"150.00"` not `150.0`. Compute `Double(agreedPrice)` on decode.
- **`platformFeePercent` normalization** — can be percentage (10) or decimal (0.10). If >1, divide by 100.
- **Delivery address coordinates are Strings** in `CarrierLocationResponse` — `latitude: "14.5998"` not Double.
- **`auto_charge` in confirm response** — PUT `/matches/{id}/confirm` returns `auto_charge` object at **root level**, not nested inside `data`.
- **Counter-offer regex** — iOS extracts original price from chat messages via pattern `"was $150.00"`. Mirror if showing price in messages.
- **Auto-cancel default** — `autoCancelAfterDays ?? 10`, minimum 1 day.
- **Incoming request detection** — for carrier: `match.status == "shipper_requested"`; for shipper: `match.status == "carrier_requested"`. Exclude counter-offers.

## TDD checklist

- [ ] `DeliveryMatch` JSON decode — all 50+ fields including string price, counter-offer, auto-charge.
- [ ] `Booking` JSON decode with permission helpers (`canAccept`, `statusDisplay`).
- [ ] All 12 `MatchStatus` cases and 6 `BookingStatus` mapping.
- [ ] Counter-offer: `ShipperCounterOfferRequest` / `CarrierCounterOfferRequest` encode; `CounterOfferResponse` decode.
- [ ] `CounterOfferContext` factory methods from match, booking, and notification data.
- [ ] Pickup/delivery code models decode with expiration logic.
- [ ] `ReceiverAccessToken` decode with PIN and access tracking.
- [ ] `CarrierLocationResponse` decode with String coordinates.
- [ ] `AutoChargeInfo` from confirm response at root level.
- [ ] `MatchTransaction` decode with error fields.
- [ ] Status transition tests: all valid transitions per diagram.
- [ ] `MatchingViewModel`: load by role, accept/decline both roles, counter-offer flow.
- [ ] `LiveTrackingViewModel`: distance calc, ETA, stale detection.
- [ ] `AutoChargeConfirmationViewModel`: state machine transitions (6 states).
- [ ] Incoming request badge count with dismissal set.
- [ ] Receipt upload (multipart) and retrieval.
