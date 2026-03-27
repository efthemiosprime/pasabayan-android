# 03 — Trips

**Phase:** 2 | **Feature:** Trips | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Carrier trip CRUD, browse available trips for shippers; parity with [`TripsAPIService.swift`](../../Pasabayan/Features/Trips/Services/TripsAPIService.swift).

## Models

[`Trip.swift`](../../Pasabayan/Features/Trips/Models/Trip.swift), `CreateTripRequest`, `TripUpdateRequest`, `TripResponse`, `TripsResponse`.

## Endpoints

| Method | Path | Notes |
|--------|------|--------|
| GET | `/trips` | List; on failure message containing “not registered as both carrier and shipper”, iOS **retries** `/carrier/trips` |
| GET | `/carrier/trips` | Fallback list |
| POST | `/trips` | **Custom URLSession** in iOS (not `makeRequest`) — stricter timeout; special errors: `mixedTransportTypes`, `noTransportTypeSpecified`, `unauthenticated`, `userNotCarrier`, `validationError` |
| PUT | `/trips/{id}` | Update via `makeRequest` |
| DELETE | `/trips/{id}` | Cancel/delete; `APIResponse` |
| GET | `/trips/available?...` | Query params for browse |

## UI (iOS reference)

[`EditTripSheet.swift`](../../Pasabayan/Features/Trips/Views/EditTripSheet.swift), trip lists in dashboard components.

## Local / client-only state

- [`UsualTransportStore`](../../Pasabayan/Features/Trips/Services/UsualTransportStore.swift) — persisted usual transport preferences (UserDefaults-style); mirror on Android if UX depends on it.

## Quirks

- **Create trip** bypasses shared `APIService` success path — Android should still map HTTP codes to the **same** domain errors for parity.

## ROP / TDD

- [ ] Unit tests for trip JSON decode (`TripDecodingTests`, `CreateTripRequestTests`).
- [ ] Integration: create trip error messages for 400/401/403/422.
