# 05 — Bookings, matches, compatibility, receipts

**Phase:** 3 | **Feature:** Bookings | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Largest API surface: [`BookingsAPIService.swift`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift), models in [`BookingModels.swift`](../../Pasabayan/Features/Bookings/Models/BookingModels.swift) and related files.

## Endpoints (inventory)

Paths are under `APIConfiguration.baseURL` (prefix `/api` in full URL).

### Matches core

| Method | Path |
|--------|------|
| GET | `/matches` |
| GET | `/matches?role=carrier` |
| GET | `/matches?role=shipper` |
| GET | `/matches/pending-requests` |
| GET | `/matches/{matchId}` |
| POST | `/matches` |
| PUT | `/matches/{id}/confirm` |
| PUT | `/matches/{id}/pickup` |
| PUT | `/matches/{id}/transit` |
| PUT | `/matches/{id}/deliver` |
| DELETE | `/matches/{id}` |
| POST | `/matches/{matchId}/generate-pickup-code` |
| POST | `/matches/{matchId}/generate-delivery-code` |
| GET | `/matches/{matchId}/carrier-location` |
| POST | `/matches/{matchId}/location` |
| POST | `/matches/{matchId}/rate` |
| GET | `/matches/{matchId}/rating-status` |
| POST | `/payments/matches/{matchId}/auto-charge` |
| POST | `/services/matches/{matchId}/receipt` (multipart) |
| GET | `/services/matches/{matchId}/receipt` |
| GET | `/matches/{matchId}/receiver-access` |
| POST | `/matches/{matchId}/receiver-access` |
| DELETE | `/matches/{matchId}/receiver-access/{tokenId}` |
| POST | `/matches/{matchId}/accept-shipper-request` |
| POST | `/matches/{matchId}/decline-shipper-request` |
| POST | `/matches/{matchId}/accept` |
| POST | `/matches/{matchId}/decline` |

### Trips ↔ packages

| Method | Path |
|--------|------|
| POST | `/trips/{tripId}/book` |
| POST | `/bookings` |
| GET | `/trips/{tripId}/matches` |
| GET | `/trips/{tripId}/compatibility/{packageId}` |
| GET | `/trips/{tripId}/compatible-packages` |
| POST | `/trips/{tripId}/packages/{packageId}/request` |
| POST | `/trips/{tripId}/packages/{packageId}/accept` |
| POST | `/trips/{tripId}/packages/{packageId}/reject` |
| GET | `/packages/{packageId}/compatible-trips` |
| POST | `/packages/{packageId}/request-trip/{tripId}` |

### Misc

| Method | Path |
|--------|------|
| GET | `/packages/{packageId}/trip-template` |

### Counter-offer (negotiation)

Counter-offers reuse the **same paths** as the initial “request” flows, with JSON bodies that set `is_counter_offer: true` and reference `original_match_id`. Implementations: [`submitShipperCounterOffer`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift), [`submitCarrierCounterOffer`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift).

| Role | Method | Path | Request type | Response |
|------|--------|------|--------------|----------|
| Shipper | POST | `/packages/{packageId}/request-trip/{tripId}` | `ShipperCounterOfferRequest` | `CounterOfferResponse` |
| Carrier | POST | `/trips/{tripId}/packages/{packageId}/request` | `CarrierCounterOfferRequest` | `CounterOfferResponse` |

**Payload keys (snake_case on wire):**

- **Shipper:** `offered_price`, `message`, `is_counter_offer`, `original_match_id`, `original_price`, `counter_offer_round` (optional).
- **Carrier:** `proposed_price`, `message`, `is_counter_offer`, `original_match_id`, `original_price`, `counter_offer_round` (optional).

**Response:** `CounterOfferResponse` — `message`, `data` (`DeliveryMatch`), optional `warnings`, `negotiation_needed`, `is_counter_offer`.

**Related models:** [`MatchingModels.swift`](../../Pasabayan/Features/Bookings/Models/MatchingModels.swift) (`ShipperCounterOfferRequest`, `CarrierCounterOfferRequest`, `CounterOfferResponse`, `CounterOfferNotificationData`).

**UI (iOS):** [`CounterOfferPromptView.swift`](../../Pasabayan/Features/Bookings/Views/Shared/CounterOfferPromptView.swift), [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift), [`CounterOfferBanner.swift`](../../Pasabayan/Features/Bookings/Views/Components/CounterOfferBanner.swift), pricing copy in [`PriceComparisonSection.swift`](../../Pasabayan/Features/Bookings/Views/Components/PriceComparisonSection.swift). Deep links from push: see [08-notifications-device-tokens.md](08-notifications-device-tokens.md) (`NotificationType.counterOffer`).

## Request/response types

Use iOS method signatures in `BookingsAPIService` for exact `Codable` types (`DataResponse<DeliveryMatch>`, `BookingResponse`, `CarrierResponseResult`, etc.).

## UI (iOS reference)

Core: [`MatchingViewModel.swift`](../../Pasabayan/Features/Bookings/ViewModels/MatchingViewModel.swift), [`CompatibleTripsView.swift`](../../Pasabayan/Features/Bookings/Views/Shipper/CompatibleTripsView.swift), [`RequestToCarrySheet.swift`](../../Pasabayan/Features/Bookings/Views/Carrier/RequestToCarrySheet.swift), [`ConfirmDeliveryCodeView.swift`](../../Pasabayan/Features/Bookings/Views/Delivery/ConfirmDeliveryCodeView.swift). Also: [`DeliveryHistoryView`](../../Pasabayan/Features/Bookings/Views/History/DeliveryHistoryView.swift), [`ShareWithReceiverSheet`](../../Pasabayan/Features/Bookings/Views/Shipper/ShareWithReceiverSheet.swift), [`LiveTrackingViewModel`](../../Pasabayan/Features/Bookings/ViewModels/LiveTrackingViewModel.swift), generate pickup/delivery code views under `Bookings/Views/`.

## Ambiguity

Multiple methods may hit overlapping paths with different wrapper types — Android should follow **one** mapping per call site matching the iOS method used in production.

## TDD checklist

- [ ] `MatchingViewModelTests`, `MatchingAPITests`, `BookingDecoderTests`, `DeliveryCardTests`, etc. from `PasabayanTests/`.
- [ ] Counter-offer: `CounterOfferTests`, `CompatibleTripsCounterOfferTests`, `CounterOfferContextNotificationDataTests`, `CounterOfferNotificationTests`, `PickupDeliveryCodeModelsTests` / decode paths as applicable.
