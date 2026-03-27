# 12 — Legal, support, location catalog, shipper, routes, activity

**Phase:** 7 | **Features:** Legal, Support, Locations, Shipper, Routes, Route activity (+ health/activity logs) | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Legal

[`LegalAgreementService.swift`](../../Pasabayan/Features/Legal/Services/LegalAgreementService.swift)

| Method | Path |
|--------|------|
| GET | `/legal/status` |
| POST | `/legal/agree` |
| POST | `/legal/withdraw` |

## Support

[`SupportTicketService.swift`](../../Pasabayan/Features/Support/Services/SupportTicketService.swift)

| Method | Path |
|--------|------|
| POST | `/support/tickets` (multipart) |

## Location catalog

[`LocationCatalogService.swift`](../../Pasabayan/Services/LocationCatalogService.swift)

| Method | Path |
|--------|------|
| GET | `/locations/catalog` |

## Home city detection (Explore / profile)

[`HomeCityDetectionService.swift`](../../Pasabayan/Services/HomeCityDetectionService.swift) — runs once per session from Explore `onAppear`; uses GPS + profile.

| Method | Path | Notes |
|--------|------|--------|
| GET | `/locations/countries/CA/cities` | City list (`CitiesResponse`); used to resolve GPS → `home_city_id` |
| GET | `/profile` | Read `home_city_id` via [`ProfileAPIService.getHomeCityId`](../../Pasabayan/Features/Profile/Services/ProfileAPIService.swift) |
| PUT | `/profile` | Update when detected city differs (same profile update as rest of app) |

## Shipper

[`ShipperAPIService.swift`](../../Pasabayan/Features/Shipper/Services/ShipperAPIService.swift)

| Method | Path |
|--------|------|
| GET | `/shipper/nearby-carriers` |

## Routes

[`RoutesAPIService.swift`](../../Pasabayan/Features/Trips/Services/RoutesAPIService.swift)

| Method | Path |
|--------|------|
| GET | `/routes/popular-packages` |

## Route activity

[`RouteActivityAPIService.swift`](../../Pasabayan/Features/RouteActivity/Services/RouteActivityAPIService.swift)

| Method | Path |
|--------|------|
| GET | `/route-activity/summary` |

## Health / activity logs (core)

[`APIService.swift`](../../Pasabayan/Services/APIService.swift)

| Method | Path |
|--------|------|
| GET | `/health` |
| POST | `/activity-logs` |

## TDD checklist

- [ ] `RouteActivitySummaryTests`, `DeliveryCityCatalogTests`, `APIModels`/`ContractSchemaTests` as applicable.
- [ ] Home city: `CitiesResponse` / profile `home_city_id` flow matches `HomeCityDetectionService` behavior.
