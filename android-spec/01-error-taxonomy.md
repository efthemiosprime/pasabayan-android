# 01 — Error taxonomy and HTTP mapping (iOS parity)

**Phase:** 0 (foundation). **Scope:** cross-cutting errors for all features. See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md).

## Source files

- [`Pasabayan/Services/APIService.swift`](../../Pasabayan/Services/APIService.swift) — `APIError`, `makeRequest` status handling
- [`Pasabayan/Utilities/APIErrorFormatter.swift`](../../Pasabayan/Utilities/APIErrorFormatter.swift) — `userFriendlyMessage`
- [`Pasabayan/Utilities/ErrorAlertPolicy.swift`](../../Pasabayan/Utilities/ErrorAlertPolicy.swift) — foreground vs background alerts
- [`Pasabayan/Features/Payments/Models/PaymentModels.swift`](../../Pasabayan/Features/Payments/Models/PaymentModels.swift) — `PaymentError` where applicable

## `APIError` cases (mirror in `DomainError` / sealed class)

| Case | Typical trigger |
|------|-----------------|
| `invalidURL` | Malformed URL |
| `networkError(Error)` | Transport failure |
| `decodingError` | JSON decode failure |
| `encodingError` | Request body encode failure |
| `serverError(String)` | Parsed `message` or generic client/server text |
| `paymentRequired(message)` | HTTP 402 + `error == "payment_required"` |
| `unauthorized` | HTTP 401 (also clears token on iOS) |
| `authenticationError` | Auth-specific |
| `httpError(statusCode)` | Generic HTTP |
| `invalidResponse` | Non-HTTPURLResponse |
| `notFound` | HTTP 404 (non-trip message) |
| `tripNotFound(message, tripId?)` | HTTP 404 + message contains `trip` or `carriertrip` |
| `conflict(message, expiresAt?)` | HTTP 409 (e.g. code generation cooldown) |
| `rateLimited(retryAfterSeconds?)` | Defined on enum; confirm HTTP branch in client if used |
| `carrierOnboardingRequired(message)` | HTTP 422 + `carrier_onboarding_required == true` |
| `consentRequired(purpose, message)` | Laravel consent middleware |
| `validationError(ValidationErrorResponse)` | HTTP 400/422 + Laravel `errors` |
| `unauthenticated` | Trip create custom client (401 + message) |
| `userNotCarrier` | Trip create 403 |
| `mixedTransportTypes` / `noTransportTypeSpecified` | Trip create 400 message match |
| `unknown` | Fallback |

## HTTP status handling (`APIService.makeRequest`)

- **200–299**: Decode body as `T`.
- **401**: Remove token, `unauthorized`.
- **404**: Parse `APIErrorResponse.message`; if contains `trip`/`carriertrip` → `tripNotFound`, else `notFound`.
- **402**: JSON `error` / `message`; `payment_required` → `paymentRequired`.
- **409**: `message`, optional `expires_at` → `conflict`.
- **400**: `ValidationErrorResponse` or `message` → `validationError` or `serverError`.
- **422**: Carrier onboarding flag, then validation, then message.
- **400–499** (other): `message` if present.
- **500–599**: `message` or generic server error.

## Auth header rule

Endpoints containing `/auth/` **except** `/auth/me` are called **without** Bearer token. All other authenticated calls send `Authorization: Bearer <token>` when token exists.

## User-facing strings

- Prefer mapping through the same rules as `APIError.userFriendlyMessage` for parity with iOS UI.

## ROP

- Map transport + `APIError` → `Result<Success, DomainError>` at repository boundary; map `DomainError` → UI string in presentation using the same policy as `userFriendlyMessage`.

## TDD checklist

- [ ] Unit tests: 401/404/402/409/422 branches produce expected domain errors.
- [ ] Unit test: auth header omitted only for `/auth/*` excluding `/auth/me`.
- [ ] `ErrorAlertPolicy`-equivalent: background sync vs foreground errors.
