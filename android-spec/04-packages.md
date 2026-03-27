# 04 — Packages and service requests

**Phase:** 2 | **Feature:** Packages | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`PackagesAPIService.swift`](../../Pasabayan/Features/Packages/Services/PackagesAPIService.swift) — package requests CRUD, available packages, multipart images, service request endpoint.

## Models

[`PackageRequest.swift`](../../Pasabayan/Features/Packages/Models/PackageRequest.swift), `CreatePackageRequest`, `PackageUpdateRequest`, `CreateServiceRequestBody`, responses `PackageRequestResponse`, `PackageRequestsResponse`, `AvailablePackagesResponse`.

## Endpoints (summary)

| Method | Path | Notes |
|--------|------|--------|
| GET | `/packages` | List |
| GET | `/packages/available?...` | Browse |
| POST | `/packages` | JSON or **multipart** with `images[]` |
| GET|PUT|DELETE | `/packages/{id}` | Get, update (JSON or multipart), delete |
| POST | `/services/request` | `CreateServiceRequestBody` → `PackageRequestResponse` |

Cancel flows may use `PUT` with `request_status: cancelled` (see service implementation).

## UI (iOS reference)

Package creation and lists under `Features/Packages/Views/`, shipper packages tab.

## Local / client-only state

Shipper UX may depend on local stores (not server): [`ShipperDisclaimerStore`](../../Pasabayan/Features/Packages/Services/ShipperDisclaimerStore.swift), [`SavedPackageDescriptionsStore`](../../Pasabayan/Features/Packages/Services/SavedPackageDescriptionsStore.swift), [`SavedPackageRouteTemplatesStore`](../../Pasabayan/Features/Packages/Services/SavedPackageRouteTemplatesStore.swift) — mirror persistence behavior for parity.

## TDD checklist

- [ ] `PackageRequestDecoderTests`, `PackagesAPIServiceAvailableParamsTests` parity.
- [ ] Multipart field names match iOS.
