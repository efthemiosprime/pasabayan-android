# 05 — Bookings, matches, compatibility, receipts

**Phase:** 3 | **Feature:** Bookings | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Largest API surface: [`BookingsAPIService.swift`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift) (2,600+ lines, 39+ methods), models in [`BookingModels.swift`](../../Pasabayan/Features/Bookings/Models/BookingModels.swift), [`MatchingModels.swift`](../../Pasabayan/Features/Bookings/Models/MatchingModels.swift), [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift), [`IncomingRequestContext.swift`](../../Pasabayan/Features/Bookings/Models/IncomingRequestContext.swift), [`ReceiptModels.swift`](../../Pasabayan/Features/Bookings/Models/ReceiptModels.swift), [`ReceiverAccessModels.swift`](../../Pasabayan/Features/Bookings/Models/ReceiverAccessModels.swift). 64 view files, 3 ViewModels.

---

## Android unification strategy (vs iOS)

iOS has **separate** carrier and shipper views for match lists, match details, and card components — resulting in ~70–90% code duplication. Android should **unify** these into role-parameterized composables.

### What to unify on Android (not 1:1 with iOS file structure)

| iOS (separate) | Android (unified) | Strategy |
|----------------|-------------------|----------|
| `BookingListView` + `ShipperMatchesView` | **`MatchListScreen`** | Single screen with `isCarrier: Boolean`; same filter chips, same list, different card actions |
| `CarrierMatchDetailsView` + `ShipperMatchDetailsView` | **`MatchDetailScreen`** | Single screen with `role: UserRole`; conditional sections (timeline = shipper-only, rating/tip = shipper-only, receipt upload = carrier-only) |
| `BookingCard` + `ShipperMatchCard` + `MatchCard` + `CarrierRequestCard` | **`MatchCard`** | Single composable with `role` + `actions: List<MatchAction>` params |
| `BookingStatusBadge` + `MatchStatusBadge` | **`MatchStatusBadge`** | Single composable with `context: StatusContext` (carrier/shipper/neutral) |
| `PriceComparisonSection.forCarrier()` / `.forShipper()` | **`PriceComparisonSection`** | Single composable with `role: UserRole` |

### What stays separate (fundamentally different flows)

| iOS | Android | Why separate |
|-----|---------|-------------|
| `CarrierBrowsePackagesView` | `BrowsePackagesScreen` | Carrier-only: browse available packages |
| `CompatiblePackagesForTripView` | `CompatiblePackagesScreen` | Carrier-only: packages matching a trip |
| `RequestToCarrySheet` | `RequestToCarrySheet` | Carrier-only: propose price for package |
| `DirectBookingSheet` | `DirectBookingSheet` | Carrier-only: direct space/passenger booking |
| `CompatibleTripsView` | `CompatibleTripsScreen` | Shipper-only: find trips for package |
| `ShareWithReceiverSheet` | `ShareWithReceiverSheet` | Shipper-only: share tracking link |
| `RateDeliverySheet` | `RateDeliverySheet` | Shipper-only: rate carrier |

### What is already shared (keep shared)

Counter-offer components, auto-charge confirmation, pickup/delivery code views, booking success, liability banner — all role-agnostic, keep as shared components.

---

## Package layout (feature-first)

```
features/bookings/
├── model/
│   ├── DeliveryMatch.kt
│   ├── Booking.kt
│   ├── MatchStatus.kt
│   ├── BookingStatus.kt
│   ├── BookingStatusContext.kt
│   ├── BookingType.kt
│   ├── BookingAction.kt
│   ├── InitiatedBy.kt
│   ├── TripStatusError.kt
│   ├── AutoChargeInfo.kt
│   ├── MatchTransaction.kt
│   ├── TrackingNote.kt
│   ├── CounterOfferContext.kt
│   ├── IncomingRequestContext.kt
│   ├── CounterOfferNotificationData.kt
│   ├── ReceiverAccessToken.kt
│   ├── PickupCodeData.kt
│   ├── DeliveryCodeData.kt
│   ├── CarrierLocationResponse.kt
│   ├── CompatibilityResult.kt
│   ├── MatchCheckData.kt
│   ├── BookingStats.kt
│   ├── dto/                               # All request/response DTOs
│   │   ├── MatchCreationRequest.kt
│   │   ├── MatchUpdateRequest.kt
│   │   ├── MatchConfirmResponse.kt
│   │   ├── CancelMatchResponse.kt
│   │   ├── AutoChargeRetryResponse.kt
│   │   ├── CreateBookingRequest.kt
│   │   ├── DirectBookingRequest.kt
│   │   ├── DirectBookingResponse.kt
│   │   ├── RequestToCarryRequest.kt
│   │   ├── CarrierRequestBody.kt
│   │   ├── CarrierRequestResponse.kt
│   │   ├── CarrierAcceptRequest.kt
│   │   ├── CarrierDeclineRequest.kt
│   │   ├── ShipperAcceptRequest.kt
│   │   ├── ShipperDeclineRequest.kt
│   │   ├── ShipperTripRequest.kt
│   │   ├── ShipperTripRequestResponse.kt
│   │   ├── AcceptShipperRequestResponse.kt
│   │   ├── ShipperMatchResponse.kt
│   │   ├── PackageAcceptRequest.kt
│   │   ├── PackageAcceptResponse.kt
│   │   ├── PackageRejectRequest.kt
│   │   ├── PackageRejectResponse.kt
│   │   ├── ShipperCounterOfferRequest.kt
│   │   ├── CarrierCounterOfferRequest.kt
│   │   ├── CounterOfferResponse.kt
│   │   ├── ServiceOfferRequest.kt
│   │   ├── ServiceOfferResponse.kt
│   │   ├── PickupCodeResponse.kt
│   │   ├── DeliveryCodeResponse.kt
│   │   ├── PickupConfirmationResponse.kt
│   │   ├── DeliveryConfirmationResponse.kt
│   │   ├── UpdateLocationRequest.kt
│   │   ├── UpdateLocationResponse.kt
│   │   ├── CarrierLocationDataResponse.kt
│   │   ├── CompatibilityResponse.kt
│   │   ├── MatchCheckResponse.kt
│   │   ├── ReceiptUploadResponse.kt
│   │   ├── ReceiptResponse.kt
│   │   ├── ReceiverAccessResponses.kt
│   │   ├── BookingResponse.kt
│   │   ├── BookingStatsResponse.kt
│   │   └── ErrorResponses.kt
│   └── nested/                            # Nested info types from API
│       ├── CarrierTripInfo.kt
│       ├── PackageRequestInfo.kt
│       ├── UserInfo.kt
│       ├── CarrierBasicInfo.kt
│       ├── CurrentLocationData.kt
│       ├── DeliveryAddressData.kt
│       ├── DirectBookingData.kt
│       ├── PackageDimensions.kt
│       ├── RefundResult.kt
│       ├── TripCapacity.kt
│       ├── TripCapacityImpact.kt
│       ├── TripAvailability.kt
│       └── PackageCompatibility.kt
├── services/
│   ├── BookingsRepository.kt             (interface)
│   ├── BookingsRepositoryImpl.kt
│   ├── BookingsApi.kt                    (Retrofit)
│   └── BookingsModule.kt                 (Hilt)
├── viewmodel/
│   ├── MatchingViewModel.kt
│   ├── LiveTrackingViewModel.kt
│   └── AutoChargeConfirmationViewModel.kt
├── ui/
│   ├── MatchListScreen.kt               # UNIFIED (carrier + shipper)
│   ├── MatchDetailScreen.kt             # UNIFIED (carrier + shipper)
│   ├── BrowsePackagesScreen.kt          # Carrier-only
│   ├── CompatiblePackagesScreen.kt      # Carrier-only
│   ├── RequestToCarrySheet.kt           # Carrier-only
│   ├── DirectBookingSheet.kt            # Carrier-only
│   ├── CompatibleTripsScreen.kt         # Shipper-only
│   ├── ShareWithReceiverSheet.kt        # Shipper-only
│   ├── RateDeliverySheet.kt             # Shipper-only
│   ├── BookingSuccessScreen.kt          # Shared
│   ├── CounterOfferPromptSheet.kt       # Shared
│   ├── AutoChargeConfirmationSheet.kt   # Shared
│   ├── GeneratePickupCodeScreen.kt      # Shared
│   ├── GenerateDeliveryCodeScreen.kt    # Shared
│   ├── ConfirmDeliveryCodeScreen.kt     # Shared
│   └── DeliveryHistoryScreen.kt         # Shared
└── components/
    ├── MatchCard.kt                      # UNIFIED (replaces 4 iOS cards)
    ├── MatchStatusBadge.kt               # UNIFIED (replaces 2 iOS badges)
    ├── PriceComparisonSection.kt         # UNIFIED (role-parameterized)
    ├── PriceDisplayView.kt               # Shared
    ├── CounterOfferBanner.kt             # Shared
    ├── CounterOfferSnackbar.kt           # Shared
    ├── CounterOfferStatusBanner.kt       # Shared
    ├── IncomingRequestSnackbar.kt        # Shared (isCarrier flag)
    ├── AutoChargeFailureBanner.kt        # Shared
    ├── LiabilityWarningBanner.kt         # Shared
    ├── BookingLocationRow.kt             # Shared
    ├── UrgencyBadge.kt                   # Shared
    ├── HistoryRowCard.kt                 # Shared
    ├── MatchActionButtons.kt             # UNIFIED (role → actions)
    ├── MatchDetailSections.kt            # UNIFIED sections (header, route, pricing, timeline, etc.)
    └── PickupDeliveryCodeView.kt         # Shared
```

---

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

**Computed properties:**
- `description: String` — human-readable
- `isCancellable: Boolean` — true for pending/confirmed/requested states

### `BookingStatus` (simplified — 6 cases, mapped from MatchStatus)

| Case | Raw value | Maps from MatchStatus |
|------|-----------|----------------------|
| `pending` | `"pending"` | pending, carrierRequested, shipperRequested |
| `confirmed` | `"confirmed"` | confirmed, shipperAccepted, carrierAccepted |
| `pickedUp` | `"picked_up"` | pickedUp |
| `inTransit` | `"in_transit"` | inTransit |
| `delivered` | `"delivered"` | delivered |
| `cancelled` | `"cancelled"` | cancelled, shipperDeclined, carrierDeclined |

**Computed properties:**
- `displayName: String` — "Pending", "Confirmed", etc.
- `shortDisplayName: String` — abbreviated
- `color: Color` — semantic color from tokens
- `icon: ImageVector` — status icon
- `description: String` — longer description

**Conversion:** `BookingStatus.from(matchStatus: MatchStatus): BookingStatus`

### `BookingStatusContext` (3 cases)

| Case | Purpose |
|------|---------|
| `SHIPPER` | Shipper's perspective display text |
| `CARRIER` | Carrier's perspective display text |
| `NEUTRAL` | Default display text |

### `InitiatedBy` (3 cases)

| Case | Raw value |
|------|-----------|
| `shipper` | `"shipper"` |
| `carrier` | `"carrier"` |
| `unknown` | `"unknown"` |

**Computed:** `displayName: String` (localized)

### `BookingType` (3 cases)

| Case | Raw value | Purpose |
|------|-----------|---------|
| `spaceOnly` | `"space_only"` | Book cargo space on existing trip |
| `fullService` | `"full_service"` | Full pickup/delivery service |
| `passenger` | `"passenger"` | Passenger/ride-sharing |

**Computed:** `displayName`, `description`, `icon`

### `BookingAction` (10 cases — determines available actions per role + status)

| Case | Carrier | Shipper | Condition |
|------|---------|---------|-----------|
| `acceptBooking` | Yes (shipper_requested) | Yes (carrier_requested) | Pending request from other party |
| `counterOffer` | No (in details) | Yes (carrier_requested) | `canCounterOffer == true` |
| `declineBooking` | Yes (shipper_requested) | Yes (carrier_requested) | Pending request |
| `markPickedUp` | Yes (confirmed) | No | Code validated |
| `markInTransit` | Yes (pickedUp) | No | After pickup |
| `enterDeliveryCode` | Yes (inTransit) | No | Carrier enters code |
| `enterPickupCode` | No | Yes (confirmed) | Shipper generates code |
| `markDelivered` | Yes (inTransit) | No | Code validated |
| `cancelBooking` | Yes (pending/confirmed) | Yes (pending/confirmed) | `isCancellable` |
| `trackLive` | No | Yes (inTransit) | Active delivery |

### `TripStatusError` (6 cases — trip unavailability reasons)

| Case | Raw value | userFriendlyTitle | shouldRemoveFromList |
|------|-----------|-------------------|---------------------|
| `planning` | `"planning"` | "Trip Not Ready" | `false` |
| `completed` | `"completed"` | "Trip Completed" | `true` |
| `cancelled` | `"cancelled"` | "Trip Cancelled" | `true` |
| `inTransit` | `"in_transit"` | "Trip In Transit" | `false` |
| `departed` | `"departed"` | "Trip Departed" | `true` |
| `unavailable` | `"unavailable"` | "Trip Unavailable" | `true` |

**Factory:** `from(message: String): TripStatusError?` — parses server error messages

**Computed:** `userFriendlyTitle`, `userFriendlyMessage`, `shouldRemoveFromList`

---

## Models

### `DeliveryMatch` (primary model — ~50+ properties)

#### Core fields

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `id` | `Int` | `id` | |
| `carrierTripId` | `Int?` | `carrier_trip_id` | |
| `packageRequestId` | `Int?` | `package_request_id` | |
| `carrierId` | `Int?` | `carrier_id` | |
| `shipperId` | `Int?` | `shipper_id` | |
| `agreedPrice` | `Double` | `agreed_price` | **Flexible decoder:** API may return `"150.00"` (String) or `150.0` (Number) |
| `status` | `String?` | `status` | |
| `matchStatus` | `String` | `match_status` | Raw status string; parse via `matchStatusEnum` |

#### Status timestamps

| Field | Type | Wire key |
|-------|------|----------|
| `confirmedAt` | `String?` | `confirmed_at` |
| `pickedUpAt` | `String?` | `picked_up_at` |
| `deliveredAt` | `String?` | `delivered_at` |
| `carrierRequestedAt` | `String?` | `carrier_requested_at` |
| `shipperRequestedAt` | `String?` | `shipper_requested_at` |
| `shipperRespondedAt` | `String?` | `shipper_responded_at` |
| `carrierRespondedAt` | `String?` | `carrier_responded_at` |
| `createdAt` | `String?` | `created_at` |
| `updatedAt` | `String?` | `updated_at` |

#### Messages & photos

| Field | Type | Wire key |
|-------|------|----------|
| `carrierMessage` | `String?` | `carrier_message` |
| `shipperMessage` | `String?` | `shipper_message` |
| `declineReason` | `String?` | `decline_reason` |
| `pickupPhoto` | `String?` | `pickup_photo` |
| `deliveryPhoto` | `String?` | `delivery_photo` |
| `receiptPhoto` | `String?` | `receipt_photo` |

#### Pickup codes

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `pickupConfirmationCode` | `String?` | `pickup_confirmation_code` | 6-digit pickup code |
| `deliveryConfirmationCode` | `String?` | `delivery_confirmation_code` | 6-digit delivery code |
| `shipperGeneratedCode` | `String?` | `shipper_generated_code` | |
| `codeExpiresAt` | `String?` | `code_expires_at` | |
| `codeGeneratedAt` | `String?` | `code_generated_at` | |
| `codeUsedAt` | `String?` | `code_used_at` | |

#### Delivery verification codes

| Field | Type | Wire key |
|-------|------|----------|
| `deliveryVerificationCode` | `String?` | `delivery_verification_code` |
| `deliveryCodeExpiresAt` | `String?` | `delivery_code_expires_at` |
| `deliveryCodeGeneratedAt` | `String?` | `delivery_code_generated_at` |
| `deliveryCodeUsedAt` | `String?` | `delivery_code_used_at` |

#### Auto-charge & pricing

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `autoCharge` | `AutoChargeInfo?` | `auto_charge` | Nested |
| `transaction` | `MatchTransaction?` | `transaction` | Payment transaction details |
| `transactionStatus` | `String?` | `transaction_status` | |
| `carrierExpectedPrice` | `Double?` | `carrier_expected_price` | Flexible decoder |
| `priceDifference` | `Double?` | `price_difference` | Flexible decoder |
| `isBelowRate` | `Boolean?` | `is_below_rate` | |
| `platformFeePercent` | `Double?` | `platform_fee_percent` | Can be percentage (10) or decimal (0.10) — normalize |
| `autoCancelAfterDays` | `Int?` | `auto_cancel_after_days` | Default 10 if nil |
| `autoCancelledAt` | `String?` | `auto_cancelled_at` | |
| `autoCancelled` | `Boolean?` | `auto_cancelled` | |

#### Counter-offer fields

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `isCounterOffer` | `Boolean` | `is_counter_offer` | |
| `originalPrice` | `String?` | `original_price` | String from API |
| `counterOffererId` | `Int?` | `counter_offerer_id` | |
| `counterOffererName` | `String?` | `counter_offerer_name` | |
| `counterOfferRound` | `Int?` | `counter_offer_round` | |
| `remainingCounterOffers` | `Int?` | `remaining_counter_offers` | |
| `canCounterOffer` | `Boolean?` | `can_counter_offer` | |
| `initiatedBy` | `InitiatedBy` | `initiated_by` | |

#### Location tracking

| Field | Type | Wire key |
|-------|------|----------|
| `carrierCurrentLat` | `Double?` | `carrier_current_lat` |
| `carrierCurrentLng` | `Double?` | `carrier_current_lng` |
| `locationLastUpdatedAt` | `String?` | `location_last_updated_at` |

#### Related entities

| Field | Type | Wire key |
|-------|------|----------|
| `packageRequest` | `PackageRequest?` | `package_request` |
| `carrierTrip` | `Trip?` | `carrier_trip` |
| `carrier` | `User?` | `carrier` |
| `shipper` | `User?` | `shipper` |
| `chatConversationId` | `Int?` | `chat_conversation_id` |
| `trackingNotes` | `List<TrackingNote>?` | `tracking_notes` |

#### Computed properties (must implement)

| Property | Type | Logic |
|----------|------|-------|
| `matchStatusEnum` | `MatchStatus` | Parse `matchStatus` string; fallback `PENDING` |
| `effectiveStatus` | `MatchStatus` | Normalized status |
| `normalizedStatusString` | `String` | For display |
| `agreedPriceValue` | `Double` | Parse `agreedPrice` (already Double after flexible decode) |
| `originalPriceValue` | `Double?` | Parse `originalPrice` String → Double |
| `platformFeePercentValue` | `Double` | If >1 divide by 100 (normalize % vs decimal) |
| `isPaymentCompleted` | `Boolean` | `transactionStatus` in completed/captured |
| `isServiceMatch` | `Boolean` | Check package type |
| `isPackage` | `Boolean` | Regular package delivery |
| `isService` | `Boolean` | Service errand |
| `hasReceipt` | `Boolean` | `receiptPhoto != null` |
| `autoCancelWindowDays` | `Int` | `max(autoCancelAfterDays ?: 10, 1)` |
| `isCancellationUnderReview` | `Boolean` | Status check |
| `hasActiveCode` | `Boolean` | Shipper-generated code valid and not expired |
| `isCodeExpired` | `Boolean` | Code past expiry |
| `canGenerateCode` | `Boolean` | Can generate shipper pickup code |
| `canPickupWithCode` | `Boolean` | Code exists and valid for pickup |
| `timeUntilCodeExpires` | `Duration?` | Time remaining |
| `hasActiveDeliveryCode` | `Boolean` | Delivery code valid and not expired |
| `deliveryCodeExpired` | `Boolean` | Check expiration |
| `canGenerateDeliveryCode` | `Boolean` | Can create new delivery code |
| `needsDeliveryCode` | `Boolean` | In-transit and no active code |
| `isPriceAcceptable` | `Boolean` | Check if below/above rate |
| `priceComparisonText` | `String` | Display price variance |
| `isCounterOfferInferred` | `Boolean` | Inferred from messages/status |
| `isCounterOfferAllowedByLimit` | `Boolean` | `remainingCounterOffers > 0 && canCounterOffer == true` |
| `canCurrentUserAccept(userId)` | `Boolean` | Permission check based on status + userId |

---

### `Booking` (~40 properties)

Full field table — similar to `DeliveryMatch` but uses `BookingStatus` and adds location/scheduling fields.

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `id` | `Int` | `id` | |
| `packageRequestId` | `Int?` | `package_request_id` | |
| `tripId` | `Int?` | `trip_id` | |
| `carrierId` | `Int?` | `carrier_id` | |
| `shipperId` | `Int?` | `shipper_id` | |
| `pickupLocation` | `String?` | `pickup_location` | |
| `deliveryLocation` | `String?` | `delivery_location` | |
| `pickupCoordinates` | `Coordinates?` | `pickup_coordinates` | |
| `deliveryCoordinates` | `Coordinates?` | `delivery_coordinates` | |
| `scheduledPickupDate` | `String?` | `scheduled_pickup_date` | |
| `scheduledPickupTime` | `String?` | `scheduled_pickup_time` | |
| `actualPickupTime` | `String?` | `actual_pickup_time` | |
| `estimatedDeliveryTime` | `String?` | `estimated_delivery_time` | |
| `actualDeliveryTime` | `String?` | `actual_delivery_time` | |
| `agreedPrice` | `Double` | `agreed_price` | **Flexible decoder** (String or Number) |
| `status` | `BookingStatus` | `status` | |
| `notes` | `String?` | `notes` | |
| `trackingNumber` | `String?` | `tracking_number` | |
| `createdAt` | `String` | `created_at` | |
| `updatedAt` | `String` | `updated_at` | |
| `autoCancelAfterDays` | `Int?` | `auto_cancel_after_days` | |
| `autoCancelledAt` | `String?` | `auto_cancelled_at` | |
| `autoCancelled` | `Boolean?` | `auto_cancelled` | |
| `platformFeePercent` | `Double?` | `platform_fee_percent` | |
| `rawMatchStatus` | `String?` | `raw_match_status` | For permission logic |
| `chatConversationId` | `Int?` | `chat_conversation_id` | |
| `packageRequest` | `PackageRequest?` | `package_request` | |
| `trip` | `Trip?` | `trip` | |
| `carrier` | `User?` | `carrier` | |
| `shipper` | `User?` | `shipper` | |
| `deliveryVerificationCode` | `String?` | `delivery_verification_code` | |
| `deliveryCodeExpiresAt` | `String?` | `delivery_code_expires_at` | |
| `deliveryCodeGeneratedAt` | `String?` | `delivery_code_generated_at` | |
| `deliveryCodeUsedAt` | `String?` | `delivery_code_used_at` | |
| `isCounterOffer` | `Boolean?` | `is_counter_offer` | |
| `originalPrice` | `Double?` | `original_price` | |
| `counterOffererId` | `Int?` | `counter_offerer_id` | |
| `counterOffererName` | `String?` | `counter_offerer_name` | |
| `counterOfferRound` | `Int?` | `counter_offer_round` | |
| `remainingCounterOffers` | `Int?` | `remaining_counter_offers` | |
| `canCounterOffer` | `Boolean?` | `can_counter_offer` | |
| `initiatedBy` | `String?` | `initiated_by` | |

**Conversion:** `Booking.from(match: DeliveryMatch)` — convenience constructor.

**Computed properties:**
- `canAccept(currentUserId: Int): Boolean` — checks `rawMatchStatus` vs userId to determine if user can accept
- `shouldShowWaitingMessage(currentUserId: Int): Boolean` — should show "Waiting for response"
- `waitingMessage(currentUserId: Int): String?` — descriptive message about who needs to act
- `statusDisplay(context: BookingStatusContext = NEUTRAL): String` — context-aware display text based on raw match status
- `hasActiveDeliveryCode`, `canGenerateDeliveryCode` — delivery code state
- `autoCancelWindowDays`, `isCancellationUnderReview` — auto-cancel state
- `platformFeePercentValue` — normalize % vs decimal
- `matchStatusEnum` — parse `rawMatchStatus` to `MatchStatus`
- `contextAwareStatusDisplay` — default context status text

---

### `AutoChargeInfo`

| Field | Type | Wire key |
|-------|------|----------|
| `chargeAmount` | `Double?` | `charge_amount` |
| `chargeStatus` | `String?` | `charge_status` |
| `chargeTimestamp` | `String?` | `charge_timestamp` |
| `failureReason` | `String?` | `failure_reason` |

**Note:** The confirm-match response returns `auto_charge` at root level with different shape: `{queued, shipper_has_default_payment_method}`. Handle both shapes.

### `MatchTransaction`

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `Int` | `id` |
| `matchId` | `Int` | `match_id` |
| `amount` | `Double` | `amount` |
| `currency` | `String` | `currency` |
| `status` | `String` | `status` |
| `type` | `String` | `type` |
| `createdAt` | `String` | `created_at` |
| `updatedAt` | `String` | `updated_at` |

### `TrackingNote`

| Field | Type | Wire key |
|-------|------|----------|
| `timestamp` | `String` | `timestamp` |
| `note` | `String` | `note` |

### `CounterOfferContext` (UI state)

| Field | Type |
|-------|------|
| `newPrice` | `Double` |
| `originalPrice` | `Double` |
| `priceDifference` | `Double` |
| `direction` | `String` (`"up"` / `"down"`) |
| `counterOffererName` | `String` |
| `counterOffererId` | `Int` |
| `counterOfferRound` | `Int?` |
| `initiatedBy` | `InitiatedBy` |
| `remainingCounterOffers` | `Int?` |
| `canCounterOffer` | `Boolean?` |
| `isCounterOffer` | `Boolean` |

**Computed properties:**
- `isLowerOffer: Boolean` — `direction == "down"`
- `isHigherOffer: Boolean` — `direction == "up"`
- `formattedDifference: String` — "$5.00 more/less"
- `formattedNewPrice: String` — "$150.00"
- `formattedOriginalPrice: String` — "$145.00"
- `summaryText: String` — "Counter-offer: $150.00 (was $145.00)"

**Factory methods:** `from(notificationData)`, `from(match: DeliveryMatch)`, `from(booking: Booking)`

### `IncomingRequestContext`

| Field | Type |
|-------|------|
| `matchId` | `Int` |
| `requesterName` | `String` |

**Logic:** For carrier → incoming = `shipper_requested`; for shipper → incoming = `carrier_requested`. Excludes counter-offers.

**Factory methods:**
- `from(match: DeliveryMatch, isCarrier: Boolean): IncomingRequestContext?`
- `incomingRequestSnackbarItems(matches, isCarrier, reviewedIds): List<IncomingRequestContext>` — max 3 items
- `dismissedIdsAfterReview(matches, isCarrier): Set<Int>`

### `CounterOfferNotificationData`

| Field | Type | Wire key |
|-------|------|----------|
| `matchId` | `Int` | `match_id` |
| `counterOffererId` | `Int` | `counter_offerer_id` |
| `counterOffererName` | `String` | `counter_offerer_name` |
| `newPrice` | `Double` | `new_price` |
| `originalPrice` | `Double` | `original_price` |
| `priceDifference` | `Double` | `price_difference` |
| `direction` | `String` | `direction` |

**Computed:** `isPriceIncrease: Boolean`, `priceChangeDescription: String`

### `BookingStats`

| Field | Type | Wire key |
|-------|------|----------|
| `totalBookings` | `Int` | `total_bookings` |
| `pendingBookings` | `Int` | `pending_bookings` |
| `confirmedBookings` | `Int` | `confirmed_bookings` |
| `activeBookings` | `Int` | `active_bookings` |
| `completedBookings` | `Int` | `completed_bookings` |
| `cancelledBookings` | `Int` | `cancelled_bookings` |
| `totalEarnings` | `Double` | `total_earnings` |
| `averageRating` | `Double?` | `average_rating` |

---

### Pickup/delivery code models

**`PickupCodeData`:**

| Field | Type | Wire key |
|-------|------|----------|
| `confirmationCode` | `String` | `confirmation_code` |
| `expiresAt` | `String` | `expires_at` |
| `matchId` | `Int` | `match_id` |
| `shipperId` | `Int` | `shipper_id` |
| `carrierId` | `Int` | `carrier_id` |
| `sentToChat` | `Boolean` | `sent_to_chat` |

**`DeliveryCodeData`:**

| Field | Type | Wire key |
|-------|------|----------|
| `verificationCode` | `String` | `verification_code` |
| `expiresAt` | `String` | `expires_at` |
| `matchId` | `Int` | `match_id` |
| `shipperId` | `Int` | `shipper_id` |
| `carrierId` | `Int` | `carrier_id` |
| `sentToChat` | `Boolean` | `sent_to_chat` |
| `packageOrService` | `String` | `package_or_service` |

### Receiver access models

**`ReceiverAccessToken`:**

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `Int` | `id` |
| `shortCode` | `String` | `short_code` |
| `shortUrl` | `String` | `short_url` |
| `hasPin` | `Boolean` | `has_pin` |
| `pin` | `String?` | `pin` |
| `pinNotice` | `String?` | `pin_notice` |
| `isActive` | `Boolean` | `is_active` |
| `accessCount` | `Int` | `access_count` |
| `trackingUrl` | `String` | `tracking_url` |
| `firstAccessedAt` | `String?` | `first_accessed_at` |
| `lastAccessedAt` | `String?` | `last_accessed_at` |
| `createdAt` | `String` | `created_at` |

**`CreateReceiverAccessRequest`:** `generatePin: Boolean`, `receiverName: String?`

### Location tracking models

**`UpdateLocationRequest`:** `latitude: Double`, `longitude: Double`, `accuracy: Double?`

**`CarrierLocationResponse`:**

| Field | Type | Wire key |
|-------|------|----------|
| `matchId` | `Int` | `match_id` |
| `carrier` | `CarrierBasicInfo?` | `carrier` |
| `currentLocation` | `CurrentLocationData?` | `current_location` |
| `deliveryAddress` | `DeliveryAddressData?` | `delivery_address` |
| `matchStatus` | `String?` | `match_status` |

**Computed:** `carrierLatitude`, `carrierLongitude`, `lastUpdatedAt`, `isStale`

**`CarrierBasicInfo`:** `id: Int`, `name: String`, `avatar: String?`

**`CurrentLocationData`:** `latitude: Double`, `longitude: Double`, `lastUpdatedAt: String`, `isStale: Boolean`

**`DeliveryAddressData`:** `address: String?`, `city: String?`, `latitude: String`, `longitude: String` — note: **coordinates are Strings!**

**Computed:** `latitudeDouble: Double?`, `longitudeDouble: Double?`

### Compatibility models

**`CompatibilityResult`:**

| Field | Type | Wire key |
|-------|------|----------|
| `isCompatible` | `Boolean` | `is_compatible` |
| `estimatedPrice` | `Double` | `estimated_price` |
| `availableCapacity` | `TripCapacity` | `available_capacity` |
| `reasons` | `List<String>?` | `reasons` |

**`TripCapacity`:** `weightKg: Double`, `spaceLiters: Double`

**`MatchCheckData`:**

| Field | Type | Wire key |
|-------|------|----------|
| `matchExists` | `Boolean` | `match_exists` |
| `match` | `DeliveryMatch?` | `match` |
| `canCreateNew` | `Boolean` | `can_create_new` |
| `reason` | `String` | `reason` |
| `tripCapacityImpact` | `TripCapacityImpact?` | `trip_capacity_impact` |
| `tripAvailability` | `TripAvailability?` | `trip_availability` |
| `packageCompatibility` | `PackageCompatibility?` | `package_compatibility` |
| `tripStatus` | `String?` | `trip_status` |
| `issues` | `List<String>?` | `issues` |

**`TripCapacityImpact`:** `weightUsed: String`, `weightRemaining: String`, `spaceUsed: String`, `spaceRemaining: String`

**`TripAvailability`:** `tripStatus: String`, `departureDate: String`, `availableWeightKg: String`, `availableSpaceLiters: String`, `pricePerKg: String`

**`PackageCompatibility`:** `packageWeightKg: String`, `packageVolumeLiters: String`, `routeCompatible: Boolean`, `capacitySufficient: Boolean`, `datesCompatible: Boolean`

### Receipt models

**`ReceiptUploadResponse`:** `success: Boolean`, `message: String`, `data: { receiptPhoto: String, receiptUrl: String }`

**`ReceiptResponse`:** `success: Boolean`, `data: { receiptPhoto: String, receiptUrl: String, uploadedAt: String }`

---

## Request/Response DTOs

### Match creation & status transitions

**`MatchCreationRequest`:**

| Field | Type | Wire key |
|-------|------|----------|
| `carrierTripId` | `Int` | `carrier_trip_id` |
| `packageRequestId` | `Int` | `package_request_id` |
| `agreedPrice` | `Double` | `agreed_price` |
| `message` | `String?` | `message` |

**`MatchUpdateRequest`:**

| Field | Type | Wire key |
|-------|------|----------|
| `matchId` | `Int` | `match_id` |
| `photo` | `String?` | `photo` |
| `notes` | `String?` | `notes` |

**`MatchConfirmResponse`:**

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `message` | `String` | `message` | |
| `data` | `DeliveryMatch` | `data` | |
| `autoCharge` | `AutoChargeInfo?` | `auto_charge` | **At root level, not nested under data** |

**`CancelMatchResponse`:**

| Field | Type | Wire key |
|-------|------|----------|
| `message` | `String` | `message` |
| `data` | `DeliveryMatch` | `data` |
| `refund` | `RefundResult?` | `refund` |

**`RefundResult`:** `refundAmount: Double`, `refundStatus: String`, `refundedAt: String`

**`AutoChargeRetryResponse`:**

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `message` | `String` | `message` |
| `data` | `DeliveryMatch` | `data` |
| `transaction` | `MatchTransaction?` | `transaction` |

### Accept/decline flows

**`CarrierAcceptRequest`:** `message: String?`, `manualConfirmation: Boolean?`

**`CarrierDeclineRequest`:** `reason: String?`

**`ShipperAcceptRequest`:** `message: String?`

**`ShipperDeclineRequest`:** `reason: String?`

**`CarrierResponseResult`:** `success: Boolean`, `message: String`, `data: DeliveryMatch`

**`AcceptShipperRequestResponse`:**

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean?` | `success` |
| `message` | `String` | `message` |
| `data` | `AcceptShipperRequestData` | `data` |
| `chatConversationId` | `Int?` | `chat_conversation_id` |
| `autoConfirmed` | `Boolean?` | `auto_confirmed` |

**`AcceptShipperRequestData`:** `id: Int`, `matchStatus: String`, `agreedPrice: Double`

**`ShipperMatchResponse`:**

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean?` | `success` |
| `message` | `String` | `message` |
| `data` | `DeliveryMatch` | `data` |
| `chatConversationId` | `Int?` | `chat_conversation_id` |
| `autoConfirmed` | `Boolean?` | `auto_confirmed` |

### Booking creation

**`CreateBookingRequest`:**

| Field | Type | Wire key |
|-------|------|----------|
| `packageRequestId` | `Int` | `package_request_id` |
| `tripId` | `Int` | `trip_id` |
| `pickupLocation` | `String` | `pickup_location` |
| `deliveryLocation` | `String` | `delivery_location` |
| `pickupCoordinates` | `Coordinates?` | `pickup_coordinates` |
| `deliveryCoordinates` | `Coordinates?` | `delivery_coordinates` |
| `scheduledPickupDate` | `String` | `scheduled_pickup_date` |
| `scheduledPickupTime` | `String?` | `scheduled_pickup_time` |
| `agreedPrice` | `Double` | `agreed_price` |
| `notes` | `String?` | `notes` |

**`BookingResponse`:** `message: String`, `data: Booking`

**`BookingsResponse`:** `message: String`, `data: PaginatedResponse<Booking>`

**`BookingStatsResponse`:** `success: Boolean`, `message: String`, `data: BookingStats`

### Direct booking

**`DirectBookingRequest`:**

| Field | Type | Wire key |
|-------|------|----------|
| `bookingType` | `BookingType` | `booking_type` |
| `spaceNeededLiters` | `Double?` | `space_needed_liters` |
| `weightNeededKg` | `Double?` | `weight_needed_kg` |
| `passengerCount` | `Int?` | `passenger_count` |
| `pickupLocation` | `String?` | `pickup_location` |
| `deliveryLocation` | `String?` | `delivery_location` |
| `pickupLat` | `Double?` | `pickup_lat` |
| `pickupLng` | `Double?` | `pickup_lng` |
| `deliveryLat` | `Double?` | `delivery_lat` |
| `deliveryLng` | `Double?` | `delivery_lng` |
| `priceAgreed` | `Double` | `price_agreed` |
| `serviceFee` | `Double?` | `service_fee` |
| `specialRequirements` | `String?` | `special_requirements` |
| `packageRequestId` | `Int?` | `package_request_id` |
| `message` | `String?` | `message` |

**`DirectBookingResponse`:** `success: Boolean`, `message: String`, `data: DirectBookingData`

**`DirectBookingData`:** `bookingId: Int`, `tripId: Int`, `status: String`, `priceAgreed: Double`, `serviceFee: Double?`, `totalAmount: Double`, `bookingReference: String`, `estimatedPickupTime: String?`, `estimatedDeliveryTime: String?`

### Package accept/reject

**`PackageAcceptRequest`:** `agreedPrice: Double`

**`PackageAcceptResponse`:** `message: String`, `data: DeliveryMatch`, `autoConfirmed: Boolean?`

**`PackageRejectRequest`:** `reason: String?`

**`PackageRejectResponse`:** `message: String`, `data: PackageRejectionResponse`

**`PackageRejectionResponse`:** `tripId: Int`, `packageId: Int`, `reason: String?`, `rejectedAt: String`

### Request to carry

**`RequestToCarryRequest`:** `carrierTripId: Int`, `agreedPrice: Double`, `message: String?`

**`CarrierRequestBody`:**

| Field | Type | Wire key |
|-------|------|----------|
| `proposedPrice` | `Double?` | `proposed_price` |
| `message` | `String?` | `message` |
| `isCounterOffer` | `Boolean?` | `is_counter_offer` |
| `originalMatchId` | `Int?` | `original_match_id` |
| `originalPrice` | `Double?` | `original_price` |
| `counterOfferRound` | `Int?` | `counter_offer_round` |

**`CarrierRequestResponse`:** `success: Boolean?`, `message: String`, `data: CarrierRequestData`, `warnings: List<String>?`, `negotiationNeeded: Boolean?`, `isCounterOffer: Boolean?`

**`CarrierRequestData`** — 30+ fields: full match data including `carrierTrip: CarrierTripInfo`, `packageRequest: PackageRequestInfo`, `carrier: UserInfo`, `shipper: UserInfo`

### Shipper trip request

**`ShipperTripRequest`:** `offeredPrice: Double`, `message: String?`

**`ShipperTripRequestResponse`:** `success: Boolean`, `message: String`, `data: ShipperTripRequestData`, `warnings: List<String>?`, `negotiationNeeded: Boolean?`, `isCounterOffer: Boolean?`

**`ShipperTripRequestData`** — 25+ fields: full match data with counter-offer fields, `carrierTrip: CarrierTripInfo`, `packageRequest: PackageRequestInfo`, `carrier: UserInfo`, `shipper: UserInfo?`

### Service offer

**`ServiceOfferRequest`:** `proposedPrice: Double`, `message: String?`, `preferredTransportationMethod: String?`

**`ServiceOfferResponse`:** `success: Boolean?`, `message: String`, `data: ServiceOfferData`, `trip: ServiceOfferTrip?`, `autoCreatedTrip: Boolean?`

**`ServiceOfferData`:** `id: Int`, `matchStatus: String`, `agreedPrice: String`

**`ServiceOfferTrip`:** `id: Int`, `transportationMethod: String?`, `tripStatus: String?`

### Counter-offer

**`ShipperCounterOfferRequest`:** `offeredPrice: Double`, `message: String?`, `isCounterOffer: Boolean` (always true), `originalMatchId: Int`, `originalPrice: Double?`, `counterOfferRound: Int?`

**`CarrierCounterOfferRequest`:** `proposedPrice: Double`, `message: String?`, `isCounterOffer: Boolean` (always true), `originalMatchId: Int`, `originalPrice: Double?`, `counterOfferRound: Int?`

**`CounterOfferResponse`:** `success: Boolean?`, `message: String`, `data: DeliveryMatch`, `warnings: List<String>?`, `negotiationNeeded: Boolean?`, `isCounterOffer: Boolean?`

### Code generation

**`PickupCodeResponse`:** `success: Boolean`, `message: String`, `data: PickupCodeData`

**`PickupConfirmationResponse`:** `success: Boolean`, `message: String`, `data: DeliveryMatch`

**`DeliveryCodeResponse`:** `success: Boolean`, `message: String`, `data: DeliveryCodeData`

**`DeliveryConfirmationResponse`:** `success: Boolean`, `message: String`, `data: DeliveryMatch`, `chatConversationClosed: Boolean?`

### Location tracking

**`UpdateLocationResponse`:** `success: Boolean`, `message: String`, `data: LocationUpdateData`

**`LocationUpdateData`:** `matchId: Int`, `latitude: Double`, `longitude: Double`, `updatedAt: String`

**`CarrierLocationDataResponse`:** `success: Boolean`, `data: CarrierLocationResponse`

### Compatibility

**`CompatibilityResponse`:** `success: Boolean`, `message: String`, `data: CompatibilityResult`

**`MatchCheckResponse`:** `message: String`, `data: MatchCheckData`

### Receiver access

**`ReceiverAccessListResponse`:** `success: Boolean`, `data: List<ReceiverAccessToken>`

**`CreateReceiverAccessResponse`:** `success: Boolean`, `data: ReceiverAccessToken`

**`RevokeReceiverAccessResponse`:** `success: Boolean`, `message: String`

### Error responses

**`ShipperRequestErrorResponse`:** `success: Boolean?`, `message: String`, `currentStatus: String?`, `existingMatchStatus: String?`, `tripStatus: String?`

**`CarrierRequestErrorResponse`:** same shape as above

**`ValidationErrorResponse`:** `message: String`, `errors: Map<String, List<String>>`
Computed: `formattedMessage: String` — joins all error lists

**`ConflictErrorResponse`:** `success: Boolean?`, `message: String`, `conflictType: String?`, `existingMatchId: Int?`

### Nested info types (from API responses)

**`CarrierTripInfo`:** `id`, `carrierId`, `originCity`, `originCountry`, `destinationCity`, `destinationCountry`, `departureDate`, `arrivalDate`, `availableWeightKg: String`, `availableSpaceLiters: String`, `pricePerKg: String`, `tripStatus`, `transportationMethod`, `specialNotes`

**`PackageRequestInfo`:** `id`, `shipperId`, `pickupAddress`, `pickupCity`, `pickupCountry`, `deliveryAddress`, `deliveryCity`, `deliveryCountry`, `packageWeightKg: String`, `packageDimensions: PackageDimensions` (custom decoder for stringified JSON), `packageType`, `packageDescription`, `fragile`, `packageValue: String`, `urgencyLevel`, `maxPriceBudget: String`, `pickupDatePreferred`, `pickupDateFlexible`, `deliveryDateNeeded`, `specialHandlingRequirements`, `requestStatus`
Computed: `title: String`

**`UserInfo`:** `id`, `name`, `email`, `avatar`, `phone`, `userTypes: List<String>`, `rating: String?`, `verificationLevel: String?`
Computed: `effectiveVerificationLevel: String`

**`PackageDimensions`:** Custom decoder — API may return as stringified JSON within `package_dimensions` field.

---

## Endpoints (full inventory — 39+ methods)

### Match listing

| Method | Path | Query params | Response | Notes |
|--------|------|-------------|----------|-------|
| GET | `/matches` | `?role=shipper` | `PaginatedResponse<DeliveryMatch>` | Shipper's matches |
| GET | `/matches` | `?role=carrier&status={status}` | `PaginatedResponse<DeliveryMatch>` | Carrier's matches; optional status filter |
| GET | `/matches` | — | `PaginatedResponse<DeliveryMatch>` | All matches |
| GET | `/matches/pending-requests` | — | `PaginatedResponse<DeliveryMatch>` | Pending shipper requests |
| GET | `/matches/{matchId}` | — | `DeliveryMatch` | Single match detail |

### Match creation & status transitions

| Method | Path | Request body | Response | Notes |
|--------|------|-------------|----------|-------|
| POST | `/matches` | `MatchCreationRequest` | `DeliveryMatch` | Create match |
| PUT | `/matches/{matchId}/confirm` | — | `MatchConfirmResponse` | **Returns `auto_charge` at root level** |
| PUT | `/matches/{matchId}/pickup` | `{ confirmation_code?, pickup_photo? }` | `DeliveryMatch` | |
| PUT | `/matches/{matchId}/transit` | — | `DeliveryMatch` | |
| PUT | `/matches/{matchId}/deliver` | `{ verification_code?, delivery_photo?, receipt_photo? }` | `DeliveryMatch` | |
| DELETE | `/matches/{matchId}` | — | `CancelMatchResponse` | Returns refund info |
| PUT | `/matches/{matchId}/confirm-pickup/{code}` | — | `PickupConfirmationResponse` | Confirm pickup with code |
| PUT | `/matches/{matchId}/delivery/{code}` | — | `DeliveryConfirmationResponse` | Deliver with verification code |

### Accept/decline flows

| Method | Path | Request body | Response | Notes |
|--------|------|-------------|----------|-------|
| POST | `/matches/{matchId}/accept` | `ShipperAcceptRequest` | `ShipperMatchResponse` | Shipper accepts carrier's request |
| POST | `/matches/{matchId}/decline` | `ShipperDeclineRequest` | `ShipperMatchResponse` | Shipper declines carrier's request |
| PUT | `/matches/{matchId}/accept-shipper-request` | `CarrierAcceptRequest` | `AcceptShipperRequestResponse` | Carrier accepts shipper's request |
| PUT | `/matches/{matchId}/decline-shipper-request` | `CarrierDeclineRequest` | `CarrierResponseResult` | Carrier declines shipper's request |

### Code generation

| Method | Path | Response |
|--------|------|----------|
| POST | `/matches/{matchId}/generate-pickup-code` | `PickupCodeResponse` |
| POST | `/matches/{matchId}/generate-delivery-code` | `DeliveryCodeResponse` |

### Location tracking

| Method | Path | Request body | Notes |
|--------|------|-------------|-------|
| GET | `/matches/{matchId}/carrier-location` | — | Shipper polls carrier position |
| POST | `/matches/{matchId}/location` | `UpdateLocationRequest` | Carrier posts `{latitude, longitude, accuracy}` every 30s |

### Rating

| Method | Path |
|--------|------|
| POST | `/matches/{matchId}/rate` |
| GET | `/matches/{matchId}/rating-status` |

### Receiver access

| Method | Path | Request body | Response | Notes |
|--------|------|-------------|----------|-------|
| GET | `/matches/{matchId}/receiver-access` | — | `ReceiverAccessListResponse` | List tokens |
| POST | `/matches/{matchId}/receiver-access` | `CreateReceiverAccessRequest` | `CreateReceiverAccessResponse` | |
| DELETE | `/matches/{matchId}/receiver-access/{tokenId}` | — | `RevokeReceiverAccessResponse` | |

### Receipts

| Method | Path | Notes |
|--------|------|-------|
| POST | `/services/matches/{matchId}/receipt` | Multipart image upload |
| GET | `/services/matches/{matchId}/receipt` | Retrieve receipt |

### Auto-charge

| Method | Path | Response |
|--------|------|----------|
| POST | `/payments/matches/{matchId}/auto-charge` | `AutoChargeRetryResponse` |

### Trips ↔ packages

| Method | Path | Request body | Response | Notes |
|--------|------|-------------|----------|-------|
| POST | `/trips/{tripId}/book` | `BookTripRequest` | `BookingResponse` | Shipper books trip directly |
| POST | `/bookings` | `CreateBookingRequest` | `BookingResponse` | Direct booking |
| GET | `/trips/{tripId}/matches` | — | `List<DeliveryMatch>` | All matches for trip |
| GET | `/trips/{tripId}/compatibility/{packageId}` | — | `CompatibilityResponse` | Compatibility check |
| GET | `/trips/{tripId}/compatible-packages` | — | List | Packages matching trip |
| POST | `/trips/{tripId}/packages/{packageId}/request` | `CarrierRequestBody` | `CarrierRequestResponse` | Carrier requests package (or counter-offer with `is_counter_offer: true`) |
| POST | `/trips/{tripId}/packages/{packageId}/accept` | `AcceptPackageRequest` | `PackageAcceptResponse` | Carrier accepts package |
| POST | `/trips/{tripId}/packages/{packageId}/reject` | `PackageRejectRequest` | `PackageRejectResponse` | Carrier rejects package |
| GET | `/packages/{packageId}/compatible-trips` | — | List | Compatible trips for package |
| GET | `/packages/{packageId}/trip-template` | — | `TripTemplateResponse` | Trip template from package |
| POST | `/packages/{packageId}/request-trip/{tripId}` | `ShipperTripRequest` | `ShipperTripRequestResponse` | Shipper requests trip (or counter-offer) |
| POST | `/packages/{packageId}/service-offer` | `ServiceOfferRequest` | `ServiceOfferResponse` | Generic service offer |

### Counter-offer (reuse same paths)

| Role | Method | Path | Request type |
|------|--------|------|-------------|
| Shipper | POST | `/packages/{packageId}/request-trip/{tripId}` | `ShipperCounterOfferRequest` (with `is_counter_offer: true`) |
| Carrier | POST | `/trips/{tripId}/packages/{packageId}/request` | `CarrierCounterOfferRequest` (with `is_counter_offer: true`) |

---

## Status transition diagram

```
INITIATION:
  Carrier → Shipper:  POST /trips/{}/packages/{}/request  →  carrier_requested
  Shipper → Carrier:  POST /packages/{}/request-trip/{}    →  shipper_requested

ACCEPTANCE:
  Shipper accepts:    POST /matches/{}/accept              →  confirmed
  Carrier accepts:    PUT /matches/{}/accept-shipper-req    →  confirmed

CONFIRMATION (payment):
  PUT /matches/{}/confirm  →  confirmed (returns auto_charge)

DELIVERY:
  PUT /matches/{}/pickup   →  picked_up
  PUT /matches/{}/transit  →  in_transit  (carrier broadcasts GPS every 30s)
  PUT /matches/{}/deliver  →  delivered

CANCELLATION (any time):
  POST /matches/{}/decline                →  cancelled
  PUT /matches/{}/decline-shipper-req     →  cancelled
  DELETE /matches/{}                      →  cancelled (with refund)

COUNTER-OFFER (from any non-confirmed state):
  Same endpoints as initiation + is_counter_offer: true + original_match_id
  Loop until accepted, declined, or remaining_counter_offers exhausted
```

---

## Repository interface

```kotlin
interface BookingsRepository {
    // Match listing
    suspend fun getShipperMatches(): Result<PaginatedResponse<DeliveryMatch>>
    suspend fun getCarrierMatches(status: MatchStatus? = null): Result<PaginatedResponse<DeliveryMatch>>
    suspend fun getAllMatches(): Result<PaginatedResponse<DeliveryMatch>>
    suspend fun getCarrierPendingRequests(): Result<PaginatedResponse<DeliveryMatch>>
    suspend fun getMatch(matchId: Int): Result<DeliveryMatch>

    // Match creation & transitions
    suspend fun createMatch(request: MatchCreationRequest): Result<DeliveryMatch>
    suspend fun confirmMatch(matchId: Int): Result<MatchConfirmResponse>
    suspend fun pickupMatch(matchId: Int, updateRequest: MatchUpdateRequest?): Result<DeliveryMatch>
    suspend fun transitMatch(matchId: Int): Result<DeliveryMatch>
    suspend fun deliverMatch(matchId: Int, updateRequest: MatchUpdateRequest?): Result<DeliveryMatch>
    suspend fun cancelMatch(matchId: Int): Result<CancelMatchResponse>

    // Accept/decline (carrier responds to shipper)
    suspend fun acceptShipperRequest(matchId: Int, message: String): Result<AcceptShipperRequestResponse>
    suspend fun declineShipperRequest(matchId: Int, reason: String): Result<CarrierResponseResult>

    // Accept/decline (shipper responds to carrier)
    suspend fun acceptCarrierRequest(matchId: Int, message: String?): Result<ShipperMatchResponse>
    suspend fun declineCarrierRequest(matchId: Int, reason: String?): Result<ShipperMatchResponse>

    // Auto-charge
    suspend fun retryAutoCharge(matchId: Int): Result<AutoChargeRetryResponse>

    // Counter-offers
    suspend fun submitShipperCounterOffer(packageId: Int, tripId: Int, request: ShipperCounterOfferRequest): Result<CounterOfferResponse>
    suspend fun submitCarrierCounterOffer(tripId: Int, packageId: Int, request: CarrierCounterOfferRequest): Result<CounterOfferResponse>

    // Code generation
    suspend fun generatePickupCode(matchId: Int): Result<PickupCodeData>
    suspend fun generateDeliveryCode(matchId: Int): Result<DeliveryCodeData>
    suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch>
    suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch>

    // Location tracking
    suspend fun getCarrierLocation(matchId: Int): Result<CarrierLocationResponse>
    suspend fun updateLocation(matchId: Int, request: UpdateLocationRequest): Result<Unit>

    // Receiver access
    suspend fun getReceiverAccess(matchId: Int): Result<List<ReceiverAccessToken>>
    suspend fun createReceiverAccess(matchId: Int, request: CreateReceiverAccessRequest): Result<ReceiverAccessToken>
    suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit>

    // Receipts
    suspend fun uploadReceipt(matchId: Int, image: ByteArray): Result<ReceiptUploadResponse>
    suspend fun getReceipt(matchId: Int): Result<ReceiptResponse>

    // Trips ↔ packages
    suspend fun bookTrip(tripId: Int, request: BookTripRequest): Result<BookingResponse>
    suspend fun createBooking(request: CreateBookingRequest): Result<BookingResponse>
    suspend fun getCompatibility(tripId: Int, packageId: Int): Result<CompatibilityResult>
    suspend fun getCompatiblePackages(tripId: Int): Result<List<PackageRequestInfo>>
    suspend fun getCompatibleTrips(packageId: Int): Result<List<Trip>>
    suspend fun carrierRequestPackage(tripId: Int, packageId: Int, request: CarrierRequestBody): Result<CarrierRequestResponse>
    suspend fun carrierAcceptPackage(tripId: Int, packageId: Int, request: AcceptPackageRequest): Result<PackageAcceptResponse>
    suspend fun carrierRejectPackage(tripId: Int, packageId: Int, request: PackageRejectRequest): Result<PackageRejectResponse>
    suspend fun shipperRequestTrip(packageId: Int, tripId: Int, request: ShipperTripRequest): Result<ShipperTripRequestResponse>
    suspend fun submitServiceOffer(packageId: Int, request: ServiceOfferRequest): Result<ServiceOfferResponse>

    // Rating
    suspend fun rateMatch(matchId: Int, rating: Int, comment: String?): Result<Unit>
    suspend fun getRatingStatus(matchId: Int): Result<Unit>
}
```

---

## ViewModels

### `MatchingViewModel` (core — ~1,000+ lines iOS equivalent)

**State:**
```kotlin
data class MatchingUiState(
    val matches: List<DeliveryMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val matchAccepted: Boolean = false,
    val lastAutoChargeInfo: AutoChargeInfo? = null,
    val reviewedIncomingRequestIds: Set<Int> = emptySet(),
)
```

**Methods:**
- `loadShipperMatches()`
- `loadCarrierMatches(status: MatchStatus? = null)`
- `loadCarrierPendingRequests()`
- `loadAllMatches()`
- `fetchMatch(matchId: Int)` — single match refresh
- `createMatch(request: MatchCreationRequest)`
- `confirmMatch(matchId: Int)`
- `pickupMatch(matchId: Int, updateRequest: MatchUpdateRequest?)`
- `transitMatch(matchId: Int)`
- `deliverMatch(matchId: Int, updateRequest: MatchUpdateRequest?)`
- `cancelMatch(matchId: Int)`
- `acceptShipperRequest(matchId: Int, message: String)` — carrier accepts
- `declineShipperRequest(matchId: Int, reason: String)` — carrier declines
- `acceptCarrierRequest(matchId: Int, message: String?)` — shipper accepts
- `declineCarrierRequest(matchId: Int, reason: String?)` — shipper declines
- `submitCounterOffer(...)` — delegates to shipper or carrier endpoint based on role
- `retryAutoCharge(matchId: Int, isCarrier: Boolean)`
- `markIncomingRequestReviewed(matchId: Int)` — adds to `reviewedIncomingRequestIds`
- Receiver access CRUD methods

**Badge logic:** `unseenIncomingRequestBadgeCount(matches, isCarrier, reviewedIds): Int`

**Throttling:** Prevent duplicate API calls on rapid taps.

### `LiveTrackingViewModel` (GPS tracking)

**State:**
```kotlin
data class LiveTrackingUiState(
    val carrierLatitude: Double? = null,
    val carrierLongitude: Double? = null,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val remainingDistance: Double? = null,  // km
    val totalDistance: Double? = null,      // km
    val deliveryProgress: Float = 0f,      // 0..1
    val estimatedMinutes: Int? = null,
    val lastUpdateTime: String? = null,
    val isStale: Boolean = false,
)
```

**Behavior:** Polls once on `startTracking(matchId)`, calculates distance/ETA (40 km/h average), centers map between both markers. Stale if >10 minutes old.

### `AutoChargeConfirmationViewModel` (payment state machine)

**States:** `IDLE → CHECKING_PAYMENT_METHOD → NEEDS_PAYMENT_METHOD | READY_TO_CONFIRM → CONFIRMING → SUCCESS | ERROR`

```kotlin
enum class AutoChargeState {
    IDLE, CHECKING_PAYMENT_METHOD, NEEDS_PAYMENT_METHOD,
    READY_TO_CONFIRM, CONFIRMING, SUCCESS, ERROR
}

data class AutoChargeUiState(
    val state: AutoChargeState = AutoChargeState.IDLE,
    val errorMessage: String? = null,
    val autoChargeInfo: AutoChargeInfo? = null,
)
```

**Flow:** Check default payment method → if none show "Add Payment" → if ready show "Confirm" → call confirm → handle auto_charge response.

---

## UI screens

### `MatchListScreen` — UNIFIED (replaces iOS `BookingListView` + `ShipperMatchesView`)

**Parameters:** `isCarrier: Boolean`, `viewModel: MatchingViewModel`

**Layout:**
1. **Subtitle banner** — informational text (both roles, different copy)
2. **Filter section** — horizontal scrollable `FilterChip` row (All, counter-offers, status-based). Counter-offer chip visible only if `counterOfferCount > 0`
3. **Snackbar section** — max 3 `IncomingRequestSnackbar` items (role-parameterized via `isCarrier`)
4. **Match list** — `LazyColumn` of `MatchCard` items (unified card, role-parameterized)
5. **Empty state** — when no matches after filtering
6. **Loading state** — `PCircularProgress`
7. **Pull-to-refresh**

**Actions:** Filter by status, dismiss snackbar, tap card → `MatchDetailScreen`

### `MatchDetailScreen` — UNIFIED (replaces iOS `CarrierMatchDetailsView` + `ShipperMatchDetailsView`)

**Parameters:** `matchId: Int`, `role: UserRole` (CARRIER / SHIPPER)

**Sections (order):**

| # | Section | Carrier | Shipper | Condition |
|---|---------|---------|---------|-----------|
| 1 | `CounterOfferBanner` | Yes | Yes | Counter-offer context detected |
| 2 | **Match header** — ID, price, status badge | Yes | Yes | Always |
| 3 | `AutoChargeFailureBanner` | No | Yes | `transaction.status == failed` |
| 4 | **Other party info** — carrier shows shipper, shipper shows carrier (avatar, name, rating, verification) | Yes (shipper info) | Yes (carrier info) | Always |
| 5 | **Package info section** | Yes | Yes | Always |
| 6 | **Trip info section** | Yes | Yes | Always |
| 7 | **Pricing summary** — simple for carrier, detailed breakdown for shipper (base + fee + total, "Carrier receives X") | Simple | Detailed | Always |
| 8 | **Status timeline** — Created→Confirmed→PickedUp→InTransit→Delivered | No | Yes | Shipper-only |
| 9 | **Auto-cancel policy** | Yes | Yes | `status != delivered && !isCancellationUnderReview` |
| 10 | **Cancellation review** | Yes | Yes | `isCancellationUnderReview` |
| 11 | **`MatchActionButtons`** — role-specific actions | Role-specific | Role-specific | Always |

**Carrier-only actions:** Mark picked up, mark in transit, enter delivery code, upload receipt (service)

**Shipper-only actions:** Generate pickup/delivery codes, share with receiver (package type), pay, rate carrier, add tip, counter-offer

**Shared actions:** Accept, decline, cancel, view receipt

### `BrowsePackagesScreen` (carrier-only)

Browse available packages to carry.

**Layout:**
1. Search bar — filter by delivery city
2. Active filters display — urgency, package type, max weight, max price
3. Package list — `CarrierPackageCard` items sorted by urgency (highest first) then creation date
4. Empty state, loading state

**Actions:** Search, filter, clear filters, tap → `RequestToCarrySheet`

### `CompatiblePackagesScreen` (carrier-only)

Packages matching a specific carrier trip.

**Layout:**
1. Trip summary header (route, status, available weight, pricing)
2. Counter-offer status banner (if applicable)
3. Compatible packages list — shipper info, route, package type/weight, pickup date + flexibility, budget, estimated earnings

**Actions:** Request to carry (proposes price), refresh

### `RequestToCarrySheet` (carrier-only bottom sheet)

Carrier makes an offer on a shipper's package.

**Sections:**
1. Shipper info (if available)
2. Package summary (route, weight, type, dates, budget)
3. Trip selection — scrollable list (hidden for service errands); filtered by route/schedule/capacity compatibility
4. Pricing section — auto-calculated for packages (trip rate × weight), manual entry for services; budget limit reminder (`RequestToCarryBudgetReminder`)
5. Optional message
6. Counter-offer status banner (if negotiation detected in response)
7. Submit button

**Actions:** Select trip, enter price, enter message, create new trip from package, submit

### `DirectBookingSheet` (carrier-only bottom sheet)

Direct space/passenger booking without negotiation.

**Sections:**
1. Trip summary header
2. Booking type selection — `BookingType` picker (space-only, full-service, passenger)
3. Requirements — passenger count (passenger type), space/weight (cargo types)
4. Location inputs — pickup/delivery
5. Pricing — base + service fee = total
6. Special requirements (optional text)
7. Confirm button

### `CompatibleTripsScreen` (shipper-only)

Find trips for a shipper's package.

**Layout:**
1. Package summary header (route, weight, type, urgency, dates, budget)
2. Counter-offer status banner (if applicable)
3. Compatible trips list — carrier name + rating, trip route + status, departure date, available space/weight, price per kg, estimated price, compatibility score (%), existing request status

**Actions:** Send booking request, enter manual price, counter-offer existing match, refresh

### `ShareWithReceiverSheet` (shipper-only bottom sheet)

Generate and share tracking link with package receiver.

**Sections:**
1. Share method selection (SMS, email, link)
2. Receiver contact entry
3. PIN generation option
4. Share / copy link buttons

### `RateDeliverySheet` (shipper-only bottom sheet)

Post-delivery carrier rating.

**Sections:**
1. Carrier info (avatar, name, current rating, review count)
2. Star rating selector (1–5)
3. Comment text field
4. Submit button

### `BookingSuccessScreen` (shared)

Confirmation after successful booking/match.

**Content:** Confirmation message, order details summary, next steps guidance, navigation to bookings list.

### `CounterOfferPromptSheet` (shared bottom sheet)

Make or respond to counter-offer.

**Sections:**
1. Original price display
2. New price input
3. Price comparison (difference, direction indicator)
4. Optional message
5. Remaining counter-offers indicator
6. Submit / cancel buttons

**Variants:** `forCarrier()` shows carrier rate + budget; `forShipper()` shows shipper budget + price difference. Use `role: UserRole` parameter.

### `AutoChargeConfirmationSheet` (shared bottom sheet)

Confirm payment on match confirmation.

**State machine UI (6 states):**
- IDLE → loading
- CHECKING_PAYMENT_METHOD → checking default card
- NEEDS_PAYMENT_METHOD → prompt to add card, link to payment methods
- READY_TO_CONFIRM → match summary, pricing breakdown, payment method display, "Confirm" button
- CONFIRMING → loading spinner
- SUCCESS → success message + dismiss
- ERROR → error message + retry

### Pickup/delivery code screens (shared)

**`GeneratePickupCodeScreen`** — shipper generates 6-digit pickup code; displays code + expiry + "sent to chat" status

**`GenerateDeliveryCodeScreen`** — carrier generates 6-digit delivery code; displays code + expiry + "sent to chat" + `packageOrService` label

**`ConfirmDeliveryCodeScreen`** — 6-digit code entry with validation

### `DeliveryHistoryScreen` (shared)

Past deliveries and earnings. `LazyColumn` of `HistoryRowCard` items.

---

## Components

### `MatchCard` — UNIFIED (replaces iOS `BookingCard` + `ShipperMatchCard` + `MatchCard` + `CarrierRequestCard`)

**Parameters:** `match: DeliveryMatch`, `role: UserRole`, `actions: List<MatchAction>`, `onAction: (MatchAction) -> Unit`

**Sub-sections (all role-parameterized):**

| Sub-section | Content | Role behavior |
|-------------|---------|---------------|
| **Header** | Match #ID, status badge, created date | Same |
| **Other party** | Avatar, name, rating, verification — carrier sees shipper, shipper sees carrier | Swapped entity |
| **Route** | Pickup city → delivery city | Same |
| **Details** | Package type, weight, price, dates | Same |
| **Quick actions** | Accept/decline/counter-offer chips | Role-specific: different actions based on `BookingAction` |
| **Counter-offer indicator** | Price change badge if counter-offer | Same |

**Reuse gain:** Replaces 4 separate iOS card files with 1 composable + sub-composable sections.

### `MatchStatusBadge` — UNIFIED

**Parameters:** `status: MatchStatus`, `context: BookingStatusContext`

Display text changes based on context (carrier/shipper/neutral). Colors and icons from `MatchStatus` / `BookingStatus`.

### `MatchActionButtons` — UNIFIED

**Parameters:** `match: DeliveryMatch`, `role: UserRole`, `onAction: (BookingAction) -> Unit`

Computes available `BookingAction` list from match status + role, renders as a vertical stack of `PButton` items. Handles payment-gating for shipper (e.g., pickup code requires payment).

### `MatchDetailSections` — UNIFIED section composables

Individual composables for each detail section, all role-parameterized:

- `MatchHeaderSection(match, role)`
- `OtherPartySection(match, role)` — shows carrier info for shipper, shipper info for carrier
- `PackageInfoSection(packageRequest)`
- `TripInfoSection(trip)`
- `PricingSummarySection(match, role)` — simple for carrier, detailed for shipper
- `StatusTimelineSection(match)` — shipper-only; 6-step timeline
- `AutoCancelPolicySection(match)`
- `CancellationReviewSection(match)`

### `PriceComparisonSection` — UNIFIED

**Parameters:** `originalPrice: Double`, `newPrice: Double`, `role: UserRole`

Shows price difference with direction indicator. For carrier: shows carrier rate + budget context. For shipper: shows budget + fee context.

### `PriceDisplayView`

Simple price display with optional platform fee breakdown.

### Counter-offer components (shared — already role-agnostic in iOS)

- **`CounterOfferBanner`** — displays counter-offer context with price change
- **`CounterOfferSnackbar`** — push-notification style overlay, max 3
- **`CounterOfferStatusBanner`** — shows negotiation round status

### Other shared components

- **`IncomingRequestSnackbar`** — `isCarrier` flag controls which requests shown
- **`AutoChargeFailureBanner`** — payment failure with retry action
- **`LiabilityWarningBanner`** — legal disclaimer (shown in match details)
- **`BookingLocationRow`** — single pickup/delivery location display
- **`UrgencyBadge`** — urgency level with color coding
- **`HistoryRowCard`** — past delivery summary card
- **`PickupDeliveryCodeView`** — displays 6-digit code with expiry countdown

---

## Carrier location tracking (live delivery)

During active delivery, the carrier's device posts GPS coordinates every 30 seconds via a **foreground service** (Android equivalent of iOS `CarrierLocationService`). See [00-architecture.md](00-architecture.md#carrier-location-service-gps-tracking) for full config.

| Direction | Method | Path | Notes |
|-----------|--------|------|-------|
| Carrier → server | POST | `/matches/{matchId}/location` | Body: `{latitude, longitude, accuracy}` every 30s |
| Shipper ← server | GET | `/matches/{matchId}/carrier-location` | Polling-based; shipper reads carrier position |

Android: use a **foreground service** with notification during active delivery.

---

## Quirks

- **`agreedPrice` is a String** — API returns `"150.00"` not `150.0`. Use flexible decoder: accept both String and Number.
- **`platformFeePercent` normalization** — can be percentage (10) or decimal (0.10). If >1, divide by 100.
- **Delivery address coordinates are Strings** in `CarrierLocationResponse` — `latitude: "14.5998"` not Double.
- **`auto_charge` in confirm response** — PUT `/matches/{id}/confirm` returns `auto_charge` object at **root level**, not nested inside `data`.
- **`package_dimensions` may be stringified JSON** — `PackageRequestInfo.packageDimensions` can arrive as a JSON string inside a string field. Custom deserializer needed.
- **Counter-offer regex** — iOS extracts original price from chat messages via pattern `"was $150.00"`. Mirror if showing price in messages.
- **Auto-cancel default** — `autoCancelAfterDays ?: 10`, minimum 1 day.
- **Incoming request detection** — for carrier: `match.status == "shipper_requested"`; for shipper: `match.status == "carrier_requested"`. Exclude counter-offers.
- **`autoConfirmed` in accept responses** — when carrier/shipper accepts, response may include `autoConfirmed: true` indicating the match was automatically confirmed.

---

## Localization keys

All display strings must use `stringResource(R.string.key)`. Add to `res/values/strings_bookings.xml` (English) and `res/values-fr/strings_bookings.xml` (French).

```xml
<!-- Match status -->
<string name="bookings_status_pending">Pending</string>
<string name="bookings_status_confirmed">Confirmed</string>
<string name="bookings_status_picked_up">Picked Up</string>
<string name="bookings_status_in_transit">In Transit</string>
<string name="bookings_status_delivered">Delivered</string>
<string name="bookings_status_cancelled">Cancelled</string>
<string name="bookings_status_carrier_requested">Carrier Requested</string>
<string name="bookings_status_shipper_requested">Shipper Requested</string>
<string name="bookings_status_shipper_accepted">Shipper Accepted</string>
<string name="bookings_status_shipper_declined">Shipper Declined</string>
<string name="bookings_status_carrier_accepted">Carrier Accepted</string>
<string name="bookings_status_carrier_declined">Carrier Declined</string>

<!-- Booking status (simplified) -->
<string name="bookings_simple_pending">Pending</string>
<string name="bookings_simple_confirmed">Confirmed</string>
<string name="bookings_simple_picked_up">Picked Up</string>
<string name="bookings_simple_in_transit">In Transit</string>
<string name="bookings_simple_delivered">Delivered</string>
<string name="bookings_simple_cancelled">Cancelled</string>

<!-- Booking type -->
<string name="bookings_type_space_only">Space Only</string>
<string name="bookings_type_full_service">Full Service</string>
<string name="bookings_type_passenger">Passenger</string>

<!-- Initiated by -->
<string name="bookings_initiated_shipper">Shipper</string>
<string name="bookings_initiated_carrier">Carrier</string>
<string name="bookings_initiated_unknown">Unknown</string>

<!-- Actions -->
<string name="bookings_action_accept">Accept</string>
<string name="bookings_action_decline">Decline</string>
<string name="bookings_action_counter_offer">Counter Offer</string>
<string name="bookings_action_confirm">Confirm Match</string>
<string name="bookings_action_pickup">Mark as Picked Up</string>
<string name="bookings_action_transit">Mark as In Transit</string>
<string name="bookings_action_deliver">Mark as Delivered</string>
<string name="bookings_action_cancel">Cancel Booking</string>
<string name="bookings_action_track_live">Track Live</string>
<string name="bookings_action_generate_pickup_code">Generate Pickup Code</string>
<string name="bookings_action_generate_delivery_code">Generate Delivery Code</string>
<string name="bookings_action_enter_code">Enter Code</string>
<string name="bookings_action_share_receiver">Share with Receiver</string>
<string name="bookings_action_rate_carrier">Rate Carrier</string>
<string name="bookings_action_add_tip">Add Tip</string>
<string name="bookings_action_upload_receipt">Upload Receipt</string>
<string name="bookings_action_view_receipt">View Receipt</string>
<string name="bookings_action_retry_payment">Retry Payment</string>
<string name="bookings_action_support">Contact Support</string>

<!-- Match detail sections -->
<string name="bookings_detail_match_id">Match #%d</string>
<string name="bookings_detail_agreed_price">Agreed Price</string>
<string name="bookings_detail_package_info">Package Information</string>
<string name="bookings_detail_trip_info">Trip Information</string>
<string name="bookings_detail_carrier_info">Carrier</string>
<string name="bookings_detail_shipper_info">Shipper</string>
<string name="bookings_detail_pricing">Pricing</string>
<string name="bookings_detail_timeline">Timeline</string>
<string name="bookings_detail_auto_cancel">Auto-Cancel Policy</string>
<string name="bookings_detail_auto_cancel_days">This booking will be auto-cancelled in %d days if not confirmed.</string>
<string name="bookings_detail_cancellation_review">Cancellation Under Review</string>

<!-- Counter-offer -->
<string name="bookings_counter_offer_title">Counter Offer</string>
<string name="bookings_counter_offer_new_price">New Price</string>
<string name="bookings_counter_offer_original_price">Original Price</string>
<string name="bookings_counter_offer_difference">Difference: %s</string>
<string name="bookings_counter_offer_remaining">%d counter-offers remaining</string>
<string name="bookings_counter_offer_round">Round %d</string>
<string name="bookings_counter_offer_submit">Submit Counter Offer</string>
<string name="bookings_counter_offer_message_hint">Add a message (optional)</string>

<!-- Incoming requests -->
<string name="bookings_incoming_request">%s wants to %s</string>
<string name="bookings_incoming_carry">carry your package</string>
<string name="bookings_incoming_book">book space on your trip</string>

<!-- Auto-charge -->
<string name="bookings_auto_charge_failed">Payment failed. Please retry or update your payment method.</string>
<string name="bookings_auto_charge_confirm_title">Confirm Payment</string>
<string name="bookings_auto_charge_no_payment_method">Please add a payment method to continue.</string>
<string name="bookings_auto_charge_ready">Ready to confirm. Total: %s</string>

<!-- Pickup/delivery codes -->
<string name="bookings_code_pickup_title">Pickup Code</string>
<string name="bookings_code_delivery_title">Delivery Code</string>
<string name="bookings_code_expires_in">Expires in %s</string>
<string name="bookings_code_expired">Code expired</string>
<string name="bookings_code_sent_to_chat">Code sent to chat</string>
<string name="bookings_code_enter_hint">Enter 6-digit code</string>

<!-- Receiver access -->
<string name="bookings_receiver_share_title">Share with Receiver</string>
<string name="bookings_receiver_generate_pin">Generate PIN</string>
<string name="bookings_receiver_copy_link">Copy Link</string>
<string name="bookings_receiver_revoke">Revoke Access</string>

<!-- Request to carry -->
<string name="bookings_request_carry_title">Request to Carry</string>
<string name="bookings_request_carry_select_trip">Select Trip</string>
<string name="bookings_request_carry_proposed_price">Proposed Price</string>
<string name="bookings_request_carry_budget_warning">Shipper\'s budget is %s</string>
<string name="bookings_request_carry_submit">Send Request</string>

<!-- Direct booking -->
<string name="bookings_direct_title">Direct Booking</string>
<string name="bookings_direct_type">Booking Type</string>
<string name="bookings_direct_space">Space Needed (L)</string>
<string name="bookings_direct_weight">Weight Needed (kg)</string>
<string name="bookings_direct_passengers">Passengers</string>
<string name="bookings_direct_confirm">Confirm Booking</string>

<!-- Compatible trips/packages -->
<string name="bookings_compatible_trips_title">Compatible Trips</string>
<string name="bookings_compatible_packages_title">Compatible Packages</string>
<string name="bookings_compatibility_score">%d%% compatible</string>
<string name="bookings_estimated_price">Estimated: %s</string>
<string name="bookings_no_compatible">No compatible matches found.</string>

<!-- Rating -->
<string name="bookings_rate_title">Rate Delivery</string>
<string name="bookings_rate_comment_hint">Leave a comment (optional)</string>
<string name="bookings_rate_submit">Submit Rating</string>

<!-- History -->
<string name="bookings_history_title">Delivery History</string>
<string name="bookings_history_empty">No past deliveries.</string>

<!-- Booking success -->
<string name="bookings_success_title">Booking Confirmed!</string>
<string name="bookings_success_message">Your booking has been created successfully.</string>
<string name="bookings_success_next_steps">Next Steps</string>

<!-- Trip status errors -->
<string name="bookings_trip_error_planning">Trip Not Ready</string>
<string name="bookings_trip_error_completed">Trip Completed</string>
<string name="bookings_trip_error_cancelled">Trip Cancelled</string>
<string name="bookings_trip_error_in_transit">Trip In Transit</string>
<string name="bookings_trip_error_departed">Trip Departed</string>
<string name="bookings_trip_error_unavailable">Trip Unavailable</string>

<!-- Waiting messages -->
<string name="bookings_waiting_shipper">Waiting for shipper to respond</string>
<string name="bookings_waiting_carrier">Waiting for carrier to respond</string>

<!-- Liability -->
<string name="bookings_liability_warning">By proceeding, you acknowledge the terms and conditions regarding liability for items in transit.</string>

<!-- Filters -->
<string name="bookings_filter_all">All</string>
<string name="bookings_filter_counter_offers">Counter Offers</string>
<string name="bookings_filter_empty">No bookings match this filter.</string>

<!-- Errors -->
<string name="bookings_error_generic">Something went wrong. Please try again.</string>
<string name="bookings_error_conflict">A match already exists for this trip and package.</string>
<string name="bookings_error_trip_unavailable">This trip is no longer available.</string>

<!-- Pricing -->
<string name="bookings_price_carrier_receives">Carrier receives %s</string>
<string name="bookings_price_platform_fee">Platform fee (%s%%)</string>
<string name="bookings_price_total">Total</string>
<string name="bookings_price_below_rate">Below your rate</string>
<string name="bookings_price_above_rate">Above your rate</string>
```

---

## TDD checklist

### Models & decode
- [ ] `DeliveryMatch` JSON decode — all 50+ fields including string price, counter-offer, auto-charge, flexible decoder (String/Number for `agreedPrice`)
- [ ] `DeliveryMatch` computed properties — `matchStatusEnum`, `agreedPriceValue`, `platformFeePercentValue`, `autoCancelWindowDays`, code state booleans, counter-offer checks, `canCurrentUserAccept`
- [ ] `Booking` JSON decode with permission helpers (`canAccept`, `shouldShowWaitingMessage`, `waitingMessage`, `statusDisplay`)
- [ ] `Booking.from(match: DeliveryMatch)` conversion
- [ ] All 12 `MatchStatus` cases parsing from raw string
- [ ] `MatchStatus.isCancellable` for all cases
- [ ] All 6 `BookingStatus` cases and mapping from `MatchStatus`
- [ ] `BookingStatus` computed properties (displayName, color, icon, description)
- [ ] `BookingAction` — correct actions for each role + status combination
- [ ] `BookingType` — 3 cases with displayName, description, icon
- [ ] `TripStatusError` — parsing from server message, `userFriendlyTitle`, `shouldRemoveFromList`
- [ ] Counter-offer: `ShipperCounterOfferRequest` / `CarrierCounterOfferRequest` encode; `CounterOfferResponse` decode
- [ ] `CounterOfferContext` factory methods from match, booking, and notification data; computed properties
- [ ] `CounterOfferNotificationData` — `isPriceIncrease`, `priceChangeDescription`
- [ ] `IncomingRequestContext` — factory, snackbar items with max 3, dismissal set
- [ ] Pickup/delivery code models decode with expiration logic
- [ ] `ReceiverAccessToken` decode with PIN and access tracking
- [ ] `CarrierLocationResponse` decode with String coordinates, `latitudeDouble`/`longitudeDouble` computed
- [ ] `DeliveryAddressData` — String→Double coordinate conversion
- [ ] `AutoChargeInfo` decode — both shapes (chargeAmount/status and queued/shipperHasDefaultPaymentMethod)
- [ ] `MatchTransaction` decode
- [ ] `MatchConfirmResponse` — `auto_charge` at root level
- [ ] `CancelMatchResponse` — `RefundResult` decode
- [ ] `AcceptShipperRequestResponse` — `autoConfirmed`, `chatConversationId`
- [ ] `ShipperMatchResponse` — `autoConfirmed`, `chatConversationId`
- [ ] `DirectBookingResponse` / `DirectBookingData` decode
- [ ] `CarrierRequestResponse` / `CarrierRequestData` — 30+ fields with nested info types
- [ ] `ShipperTripRequestResponse` / `ShipperTripRequestData` — flexible `agreedPrice` String→Double
- [ ] `ServiceOfferResponse` — `autoCreatedTrip`
- [ ] `PackageRequestInfo` — custom decoder for stringified `package_dimensions`
- [ ] `UserInfo` — `effectiveVerificationLevel` computed
- [ ] `CompatibilityResult` / `MatchCheckData` decode
- [ ] `ValidationErrorResponse.formattedMessage` — joins all error lists
- [ ] `BookingStats` decode

### Repository
- [ ] Match listing — shipper, carrier, all, pending, single
- [ ] Match creation and all status transitions (confirm, pickup, transit, deliver, cancel)
- [ ] Accept/decline — both roles (4 endpoints)
- [ ] Counter-offer submission — both roles
- [ ] Auto-charge retry
- [ ] Code generation and confirmation (pickup + delivery)
- [ ] Location tracking — update + get
- [ ] Receiver access CRUD
- [ ] Receipt upload (multipart) and retrieval
- [ ] Trip booking, direct booking, compatibility check
- [ ] Carrier request/accept/reject package
- [ ] Shipper request trip, service offer
- [ ] Error response parsing (`ShipperRequestErrorResponse`, `ConflictErrorResponse`, `ValidationErrorResponse`)

### ViewModels
- [ ] `MatchingViewModel`: load by role, accept/decline both roles, counter-offer flow, throttling
- [ ] `MatchingViewModel`: receiver access CRUD, auto-charge retry
- [ ] `MatchingViewModel`: badge count with dismissal set
- [ ] `LiveTrackingViewModel`: distance calc, ETA (40 km/h), stale detection (>10 min)
- [ ] `AutoChargeConfirmationViewModel`: all 6 state machine transitions
- [ ] Status transition tests: all valid transitions per diagram
