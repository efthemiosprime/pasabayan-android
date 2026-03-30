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
| **Current phase** | Phase 4 — Payments & Stripe (Phase 3 complete) |
| **Last updated** | 2026-03-30 |
| **Spec audit** | **Complete** — YAML expanded from ~25 to ~100 endpoint rows; all feature specs updated with query params, multipart fields, WebSocket protocol, local storage keys, activity logs, cache policy, GPS services, badge rules, analytics mock structures |

---

## Phases (exit gates)

| Phase | Exit gate | Status |
|-------|-----------|--------|
| **0** — Foundation | See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) § Phase 0 | **Complete** — foundation modules, API-SHAPES (auth), **ErrorAlertPolicy**, TDD Phase 0 backlog ticked; app shell polish remains in later phases |
| **1** — Authentication | Login, token, `/auth/me`, logout | **Complete** — OAuth UI (Google/Facebook → backend); cold-start onboarding → auth; post-login city → consent → dashboard shell; **`UnauthorizedSessionNotifier`** + **`AuthViewModel`** signed-out on **401**; `didJustCompleteConsent` one-shot; auth DTO tests + **`AuthRepositoryIntegrationTest`** + **`TokenClearingHandlerTest`**. Optional: Credential Manager / One Tap, extra `AuthViewModel` tests. |
| **2** — Trips, packages, Explore | Browse/list/create flows + role tabs | **Complete** — `:core:domain` (15 enums, 4 shared models, FlexibleDecoders, DateTimeParsing); 12 new `P*` design system components; tab shell with role switcher; **Trips**: TripsApi + DTOs, Trip domain model + mapper, TripsRepository + local stores, CarrierTripsVM + BrowseTripsVM, TripCard + progress widget + filter + disclaimer + tutorial, CarrierMyTripsScreen + BrowseTripsScreen + TripDetailsScreen + TripCreationScreen, TripFormValidator (24 tests); **Packages**: PackagesApi + DTOs, PackageRequest + AvailablePackage + mapper, PackagesRepository + local stores, PackageViewModel, PackageRequestCard + details + tutorial, PackageListScreen + PackageDetailScreen + PackageRequestScreen, PackageFormValidator (14 tests); **Explore**: CarrierExploreContent + ShipperExploreContent + UserHeaderCard + StatCard, with carrier empty-state content aligned to iOS structure; full i18n (EN+FR). |
| **3** — Bookings & matches | Core marketplace loop + counter-offer parity | **Complete** — BookingsApi (20+ endpoints), DeliveryMatch (60+ fields) + BookingAction (10 cases) + BookingMapper + computed props (availableActions, pricing, codes); BookingType enum; 9 nested info types; 10 supporting models (CounterOfferContext, codes, tracking, stats); BookingsRepository (15 methods); MatchingViewModel + LiveTrackingViewModel + AutoChargeConfirmationViewModel; unified MatchCard (replaces 4 iOS cards) + MatchStatusBadge + PriceComparison + CounterOfferBanner + code views; RequestToCarrySheet + CounterOfferPromptSheet + AutoChargeSheet + RateDeliverySheet; unified MatchListScreen wired to tab; BookingSuccessScreen + code screens; 60+ tests; full i18n (EN+FR). |
| **4** — Payments & Stripe | … | In progress — contract layer aligned in `:core:network` (Payments/Stripe APIs + DTO updates + decode fixtures/tests) |
| **5** — Chat & notifications | … | Not started (FCM **service stub** + **firebase-messaging** dep only) |
| **6** — Profile, verification, favorites & ratings | … | Not started |
| **7** — Legal, support, misc | … | Not started |

---

## Phase 0 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| Gradle **Version Catalog** (`gradle/libs.versions.toml`) | Done | AGP 8.8.2, Gradle 8.10.2, Kotlin 2.2.10, KSP, Compose |
| **`:core:designsystem`** | Done | Core primitives: `PasabayanTheme`, tokens (`PasabayanBorder`, `PasabayanLayout`, `PasabayanMotion`, `PasabayanTextStyles`), `ds*` modifiers, `PButton`, `PCard`, `POutlinedTextField`, `PScaffold`, `PSnackbar`/`PSnackbarHost`, `PModalBottomSheet`, `PTopBar`, `PDivider`, `PCircularProgress`; JVM + androidTest. Spec **14** full color inventory / domain composites (e.g. `EmptyStateView`) still optional as features land. |
| **`:core:domain-error`** | Done | `DomainError` + `ValidationError`; [01-error-taxonomy.md](01-error-taxonomy.md); **`userMessage()`**; **`ErrorAlertPolicy`** / `ErrorAlertContext` (foreground vs background, iOS parity) + JVM tests |
| **`:core:network`** | Done | OkHttp + Retrofit + kotlinx-serialization; Hilt `NetworkModule`; `AuthInterceptor` (Bearer except `/auth/*` without `/auth/me`); **`ApiErrorMapper`** + error DTOs; **`Response.toDomainResult` / `foldDomainResult`**; `BuildConfig.API_BASE_URL` = production; `consumer-rules.pro` |
| **`:app`** | Partial | `@HiltAndroidApp` `PasabayanApplication`; `@AndroidEntryPoint` `MainActivity`; Compose shell showing API base URL |
| **Hilt** | Done | App + `:core:network` (KSP) |
| **Google Services / Firebase** | Partial | `com.google.gms.google-services` plugin; `app/google-services.json`; Firebase BOM + **firebase-messaging**; **`PasabayanFirebaseMessagingService`** stub (token POST + routing **TODO** Phase 5) |
| **Google Sign-In** | Partial | `GoogleSignInHelper` + `AuthScreen` / `AuthViewModel`; Credential Manager / One Tap **not** wired (optional) |
| **Facebook Login** | Partial | `FacebookLoginStarter` + `LoginManager`; `MainActivity` forwards `onActivityResult` to `CallbackManager` |
| **Manifest & security** | Done | `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `WAKE_LOCK`; `network_security_config` (HTTPS; debug cleartext for localhost); backup / data-extraction XML |
| **Tests** | Done (Phase 0 scope) | `AuthInterceptorTest` + **`ApiErrorMapperTest`**; **`ErrorAlertPolicyTest`** (`:core:domain-error`); **`AuthRepositoryIntegrationTest`** (MockWebServer + Retrofit, login / me / logout) in `:core:session`; auth JSON decode tests in `:core:network`; design system JVM + androidTest |

---

## Phase 1 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| **`:core:session`** | Done | `TokenStore`, `EncryptedTokenStore`; `StoredAuthTokenProvider`; `TokenClearingHandler` → `SessionInvalidationHandler` + **`UnauthorizedSessionNotifier`** (401 → UI); `AuthRepository` / `AuthRepositoryImpl`; `SessionModule` (Hilt); **`TokenClearingHandlerTest`** |
| **`app` dependency** | Done | `implementation(project(":core:session"))` |
| **OAuth UI** | Done | `AuthScreen` + `AuthRoute` + `AuthViewModel`; Google + Facebook → backend; **`DashboardScreen`** placeholder; **401** + `error_unauthorized`; **`didJustCompleteConsent`** via dashboard |
| **Onboarding (17)** | Done | `RootViewModel` / `AppEntryContent`; `OnboardingRoute`; city + consent gates; prefs keys; consent failure still advances |

---

## Phase 0 — Spec / artifact checklist

| Spec / artifact | Done | Notes |
|-----------------|------|-------|
| [16-project-bootstrap.md](16-project-bootstrap.md) | [x] | Modules, Hilt, Retrofit, theme, `assembleDebug` |
| [00-architecture.md](00-architecture.md) | [x] | Core module split; lean stack documented in Gradle |
| [01-error-taxonomy.md](01-error-taxonomy.md) | [x] | **HTTP status → `DomainError`** via `ApiErrorMapper`; **`userMessage()`**; **`ErrorAlertPolicy`** + tests |
| [14-design-system.md](14-design-system.md) | [x] | Core `P*` set + `ds*` + tokens + JVM/androidTest; DS-0–DS-7 done in TDD table. Large **14** inventory (every semantic color row, domain composites) grows with features. |
| [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) | [x] | **Implemented auth DTOs** audited: golden JSON fixtures + `AuthJsonModelsDecodeTest` in `:core:network` (`src/test/resources/api-fixtures/auth/`). Re-audit when new Retrofit/DTO surfaces land (trips, packages, …). |
| [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) | [x] | **Expanded** from ~25 to ~100 endpoints; 6 new groups (favorites, ratings, verification, stripe_connect, stripe_payment_methods, support) + expanded all existing groups |
| [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) | — | Process doc; no “done” checkbox |
| [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) | [x] | Phase 0 rows ticked (interceptor, mapper + ErrorAlertPolicy, design system); expand per backlog when features land |

**Phase 0 exit gate (from [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)):** **Error mapping + user-facing strings** are in place; **contract YAML audit complete** (expanded to ~100 endpoints); **[API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) verified for implemented Android DTOs** (auth — golden JSON tests in `:core:network`); extend fixtures as new DTO modules ship. **ErrorAlertPolicy** (foreground vs background) implemented in `:core:domain-error`. [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) Phase 0 complete.

**Phase 1 exit gate:** Login → token persist → **`/auth/me`** → logout; auth DTO tests; onboarding order per [17-onboarding.md](17-onboarding.md). **Met** for shipped scope (see phase table).

---

## Feature specs (remaining phases)

| Spec | Done |
|------|------|
| [02-auth-session.md](02-auth-session.md) | [x] | App + `:core:session` meets exit gate; optional extra VM/repo tests later |
| [17-onboarding.md](17-onboarding.md) | [x] | Flows + keys + gates; carrier consent flash uses `didJustCompleteConsent` when carrier UI ships |
| [03-trips.md](03-trips.md) | [x] | API + DTOs, domain models, repository, ViewModels, UI screens, form validation (24 tests) |
| [04-packages.md](04-packages.md) | [x] | API + DTOs, domain models, repository, ViewModel, UI screens, form validation (14 tests) |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | [x] | Tab shell, role switcher, carrier + shipper explore content, stats grids; Matches/Messages/Profile stubs |
| [05-bookings-matches.md](05-bookings-matches.md) | [x] | Unified MatchCard + MatchListScreen, 3 ViewModels, counter-offer flows, code screens, 60+ tests |
| [06-payments-stripe.md](06-payments-stripe.md) | [ ] | Phase 4 slice 1 in progress: contract layer (Retrofit + DTO + decode tests/fixtures) aligned; repositories/ViewModels/UI remain for next slices |
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | [ ] |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | [ ] |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | [ ] |
| [10-verification.md](10-verification.md) | [ ] |
| [11-favorites-ratings.md](11-favorites-ratings.md) | [ ] |
| [12-legal-support-misc.md](12-legal-support-misc.md) | [ ] |
| [15-platform-and-tab-index.md](15-platform-and-tab-index.md) | [ ] |

---

## iOS feature folders → Android (from [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md))

- [x] `Authentication/` — `:core:session` + **AuthScreen** / **AuthRoute** / **AuthViewModel**; **401 → signed-out**  
- [ ] `Analytics/` — [19-analytics.md](19-analytics.md)  
- [ ] `Bookings/`  
- [ ] `Chat/`  
- [ ] `Favorites/`  
- [ ] `Legal/`  
- [ ] `Notifications/` — FCM dependency + stub service only  
- [x] `Onboarding/` — Phase 1 scope  
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
- [x] Google / Facebook **sign-in flows** — Compose + backend **done**; Credential Manager / extra tests **optional**
