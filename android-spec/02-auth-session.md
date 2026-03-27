# 02 — Auth and session

**Phase:** 1 | **Feature:** Authentication | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

OAuth/token login, current user, logout, token storage behavior (parity with [`AuthService`](../../Pasabayan/Features/Authentication/Services/AuthService.swift) / Keychain).

## Base URL

[`APIConfiguration.baseURL`](../../Pasabayan/Services/APIConfiguration.swift) — `https://api.pasabayan.com/api` (production); debug may use localhost.

## Endpoints

| Method | Path | Auth header | Request body | Response (iOS type) |
|--------|------|-------------|--------------|---------------------|
| POST | `/auth/{provider}/login` | No | `{ "access_token": string }` | `BackendAuthResponse` → `AuthResponse` ([`AuthResponses.swift`](../../Pasabayan/Features/Authentication/Models/AuthResponses.swift)) |
| GET | `/auth/me` | Yes | — | `UserResponse` → `User` |
| POST | `/auth/logout` | Yes | — | `APIResponse` |

`provider` examples: `google`, `facebook`, `apple` (must match backend routes).

## Session rules

- On **401** from `APIService`, token is cleared (`AuthService.removeToken()`); Android should mirror (clear secure storage and return to auth graph).
- **Login** uses dedicated URLSession path in [`loginWithToken`](../../Pasabayan/Services/APIService.swift); success requires `success == true` and `data` with `token` + `user`.

## UI (iOS reference)

Auth screens and onboarding gates: [`ContentView.swift`](../../Pasabayan/ContentView.swift), [`Features/Authentication/`](../../Pasabayan/Features/Authentication/).

## ROP branches

- Success: `Result<User, DomainError>` with token persisted.
- Failure: server message, validation, network, decode.

## TDD checklist

- [ ] Decode `BackendAuthResponse` / `UserResponse` fixtures (see `PasabayanTests/AuthViewModelTests.swift`).
- [ ] Repository fake: login → store token → getCurrentUser succeeds.
