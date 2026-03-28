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

### Multipart field names — POST `/support/tickets`

| Field name | Type | Notes |
|-----------|------|-------|
| `category` | text | |
| `subject` | text | |
| `email` | text | |
| `priority` | text | |
| `description` | text | |
| `attachments[]` | file | Laravel array format; multiple attachments |

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
| PUT | `/profile` | Update when detected city differs; body: `{homeCityId: <city.id>}` |

### Home city detection flow (iOS parity)

1. Check GPS authorization (needs `ACCESS_FINE_LOCATION` on Android)
2. Check authenticated user exists
3. Parallel fetch: GPS location (10s timeout) + city catalog + profile `home_city_id`
4. Resolve: city name match first, then distance match (max **50 km** radius, closest wins)
5. If detected city differs from profile's `home_city_id`:
   - Update local user object
   - Store `city.id` in `confirmedHomeCityId` preference
   - PUT `/profile` with `homeCityId`

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

[`APIService.swift`](../../Pasabayan/Services/APIService.swift), [`ActivityLogger.swift`](../../Pasabayan/Services/ActivityLogger.swift)

| Method | Path |
|--------|------|
| GET | `/health` |
| POST | `/activity-logs` |

### Activity logs request body — POST `/activity-logs`

From `ActivityLogger.swift`:

```json
{
  "userId": 42,
  "userName": "Jane Doe",
  "userEmail": "jane@example.com",
  "userType": "shipper",
  "action": "create_package",
  "description": "Created package request #123",
  "logType": "package",
  "subjectType": "PackageRequest",
  "subjectId": 123,
  "properties": {"key": "value"},
  "ipAddress": "Android_a1b2c3d4",
  "userAgent": "Pasabayan Android 1.0.0"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `userId` | Int | Required; skip if ≤ 0 |
| `userName` | String | |
| `userEmail` | String | |
| `userType` | String | `"shipper"`, `"carrier"`, `"admin"` |
| `action` | String | e.g. `"create_package"`, `"accept_match"` |
| `description` | String | Human-readable |
| `logType` | String | One of: `user`, `package`, `trip`, `match`, `transaction`, `rating`, `system`, `chat`, `premium`, `auth`, `payment`, `booking` |
| `subjectType` | String? | `"PackageRequest"`, `"CarrierTrip"`, `"DeliveryMatch"`, `"Transaction"`, `"Rating"`, or null |
| `subjectId` | Int? | Entity ID or null |
| `properties` | Map? | Arbitrary key-value pairs or null |
| `ipAddress` | String | iOS: `"iOS_{first8CharsOfVendorUUID}"`; Android: `"Android_{first8CharsOfDeviceId}"` |
| `userAgent` | String | `"Pasabayan Android {versionName}"` |

## TDD checklist

- [ ] `RouteActivitySummaryTests`, `DeliveryCityCatalogTests`, `APIModels`/`ContractSchemaTests` as applicable.
- [ ] Home city: `CitiesResponse` / profile `home_city_id` flow matches `HomeCityDetectionService` behavior.
