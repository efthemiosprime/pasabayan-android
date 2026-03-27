# Android implementation status (living document)

**Purpose:** Track what is **done**, **in progress**, or **not started** in this repo relative to `android-spec/`. Update this file when milestones land.

**Roadmap:** [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) · **Coverage:** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md)

---

## How to update

1. When a spec slice or phase gate is satisfied in code, tick **`[x]`** and add a short **Note** (PR, area, or follow-up).
2. Deferred work: mark **Deferred** in Notes with a link or reason.
3. Set **Current phase** and **Last updated** when you edit this file.

---

## Current focus

| Field | Value |
|-------|--------|
| **Current phase** | Phase 0 — Foundation (HTTP→domain mapping + tests landed; contract YAML audit remains) |
| **Last updated** | 2026-03-27 |

---

## Phases (exit gates)

| Phase | Exit gate | Status |
|-------|-----------|--------|
| **0** — Foundation | See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) § Phase 0 | **In progress** — `ApiErrorMapper` + `DomainError.userMessage()` + unit tests; **api-contract-matrix** + **API-SHAPES** row audit still TODO |
| **1** — Authentication | Login, token, `/auth/me`, logout | Not started (OAuth client libs present; flows not wired) |
| **2** — Trips, packages, Explore | … | Not started |
| **3** — Bookings & matches | … | Not started |
| **4** — Payments & Stripe | … | Not started |
| **5** — Chat & notifications | … | Not started (FCM **service stub** + **firebase-messaging** dep only) |
| **6** — Profile, verification, favorites & ratings | … | Not started |
| **7** — Legal, support, misc | … | Not started |

---

## Phase 0 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| Gradle **Version Catalog** (`gradle/libs.versions.toml`) | Done | AGP 8.8.2, Gradle 8.10.2, Kotlin 2.2.10, KSP, Compose |
| **`:core:designsystem`** | Done | `PasabayanTheme`, spacing / radius / colors / typography per [14-design-system.md](14-design-system.md); Material3 DayNight XML theme in `:app` |
| **`:core:domain-error`** | Done | `DomainError` + `ValidationError`; [01-error-taxonomy.md](01-error-taxonomy.md); **`userMessage()`** (iOS `userFriendlyMessage` parity) |
| **`:core:network`** | Done | OkHttp + Retrofit + kotlinx-serialization; Hilt `NetworkModule`; `AuthInterceptor` (Bearer except `/auth/*` without `/auth/me`); **`ApiErrorMapper`** + error DTOs; **`Response.toDomainResult` / `foldDomainResult`**; `BuildConfig.API_BASE_URL` = production; `consumer-rules.pro` |
| **`:app`** | Partial | `@HiltAndroidApp` `PasabayanApplication`; `@AndroidEntryPoint` `MainActivity`; Compose shell showing API base URL |
| **Hilt** | Done | App + `:core:network` (KSP) |
| **Google Services / Firebase** | Partial | `com.google.gms.google-services` plugin; `app/google-services.json`; Firebase BOM + **firebase-messaging**; **`PasabayanFirebaseMessagingService`** stub (token POST + routing **TODO** Phase 5) |
| **Google Sign-In (prep)** | Partial | `play-services-auth`, `play-services-identity` — **no UI / `GoogleSignInClient` flow yet** (Phase 1) |
| **Facebook Login (prep)** | Partial | `facebook-login` SDK; manifest meta-data + `FacebookActivity` / `CustomTabActivity`; strings from legacy `pasabayan-android-develop` — **no `LoginManager` flow yet** (Phase 1) |
| **Manifest & security** | Done | `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `WAKE_LOCK`; `network_security_config` (HTTPS; debug cleartext for localhost); backup / data-extraction XML |
| **Tests** | Partial | `AuthInterceptorTest` + **`ApiErrorMapperTest`** (401/404/402/409/400/422/403/500/429); repository-level integration tests **TODO** |

---

## Phase 0 — Spec / artifact checklist

| Spec / artifact | Done | Notes |
|-----------------|------|-------|
| [16-project-bootstrap.md](16-project-bootstrap.md) | [x] | Modules, Hilt, Retrofit, theme, `assembleDebug` |
| [00-architecture.md](00-architecture.md) | [x] | Core module split; lean stack documented in Gradle |
| [01-error-taxonomy.md](01-error-taxonomy.md) | [x] | **HTTP status → `DomainError`** via `ApiErrorMapper`; **`userMessage()`** for UI copy; foreground/background **ErrorAlertPolicy** still TODO |
| [14-design-system.md](14-design-system.md) | [x] | Baseline tokens + theme; iterate vs iOS as screens ship |
| [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) | [ ] | Not audited for Android DTOs yet |
| [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) | [ ] | Not reviewed row-by-row for Android |
| [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) | — | Process doc; no “done” checkbox |
| [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) | [ ] | Auth interceptor + **API error branch** tests; expand per backlog when features land |

**Phase 0 exit gate (from [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)):** **Error mapping + user-facing strings** are in place; gate **not fully met** until [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) and [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) are reviewed for Android DTOs; **ErrorAlertPolicy**-style presentation (foreground vs background) is optional for the gate but still TODO.

---

## Phase 1+ — Feature specs (not started)

| Spec | Done |
|------|------|
| [02-auth-session.md](02-auth-session.md) | [ ] |
| [17-onboarding.md](17-onboarding.md) | [ ] |
| [03-trips.md](03-trips.md) | [ ] |
| [04-packages.md](04-packages.md) | [ ] |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | [ ] |
| [05-bookings-matches.md](05-bookings-matches.md) | [ ] |
| [06-payments-stripe.md](06-payments-stripe.md) | [ ] |
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | [ ] |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | [ ] |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | [ ] |
| [10-verification.md](10-verification.md) | [ ] |
| [11-favorites-ratings.md](11-favorites-ratings.md) | [ ] |
| [12-legal-support-misc.md](12-legal-support-misc.md) | [ ] |
| [15-platform-and-tab-index.md](15-platform-and-tab-index.md) | [ ] |

---

## iOS feature folders → Android (from [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md))

- [ ] `Authentication/` — OAuth deps only; no feature module yet  
- [ ] `Analytics/`  
- [ ] `Bookings/`  
- [ ] `Chat/`  
- [ ] `Favorites/`  
- [ ] `Legal/`  
- [ ] `Notifications/` — FCM dependency + stub service only  
- [ ] `Onboarding/`  
- [ ] `Packages/`  
- [ ] `Payments/`  
- [ ] `Profile/`  
- [ ] `Ratings/`  
- [ ] `RouteActivity/`  
- [ ] `Shipper/`  
- [ ] `Support/`  
- [ ] `Trips/`  
- [ ] `Verification/`  

**Cross-cutting**

- [x] Shared networking stack in `:core:network` (interceptor policy + prod base URL)  
- [x] Design tokens in `:core:designsystem`  
- [ ] App shell / tabs / Explore — not built  
- [x] `google-services.json` + Firebase project linkage for FCM (runtime registration **TODO**)  
- [ ] Google / Facebook **sign-in flows** end-to-end (Phase 1)
