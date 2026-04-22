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
| **Last updated** | 2026-04-22 (Trips TDD checklist closeout pass) |
| **Spec audit** | **Complete** — YAML expanded from ~25 to ~100 endpoint rows; all feature specs updated with query params, multipart fields, WebSocket protocol, local storage keys, activity logs, cache policy, GPS services, badge rules, analytics mock structures |

---

## Phases (exit gates)

| Phase | Exit gate | Status |
|-------|-----------|--------|
| **0** — Foundation | See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) § Phase 0 | **Complete** — foundation modules, API-SHAPES (auth), **ErrorAlertPolicy**, TDD Phase 0 backlog ticked; app shell polish remains in later phases |
| **1** — Authentication | Login, token, `/auth/me`, logout | **Complete** — OAuth UI (Google/Facebook → backend); cold-start onboarding → auth; post-login city → consent → dashboard shell; **`UnauthorizedSessionNotifier`** + **`AuthViewModel`** signed-out on **401**; `didJustCompleteConsent` one-shot; auth DTO tests + **`AuthRepositoryIntegrationTest`** + **`TokenClearingHandlerTest`**. Optional: Credential Manager / One Tap, extra `AuthViewModel` tests. |
| **2** — Trips, packages, Explore | Browse/list/create flows + role tabs | **Complete** — `:core:domain` (15 enums, 4 shared models, FlexibleDecoders, DateTimeParsing); 12 new `P*` design-system components; tab shell with role switcher. **Trips** parity additions now wired in Android code: endpoints in `TripsApi` (`/routes/popular-packages`, `/route-activity/summary`, `/trips/{id}/matches`, `/packages/{id}/trip-template`), repository methods in `TripsRepository`/`TripsRepositoryImpl`, create-from-package orchestration (`CreateTripFromPackageUseCase`, `CreateTripFromPackageViewModel`, payload fields `autoRequestPackageId`/`proposedPrice`/`requestMessage`), domain parity in `Trip` + `TripMapper` (passenger fields, `distanceMultiplier`, pickup/dropoff instructions, `formattedDuration`, `priceDisplayString`), ViewModel rules (`BrowseTripsViewModel` load-more pagination, `CarrierTripsViewModel` transition/delete guards, `TripPackageProgressViewModel` state machine, `RouteActivitySummaryViewModel`), and first-time gate store `CarrierPreferencesFormStore`. Decode/repository/VM/store tests and fixtures were expanded accordingly. **Packages**: API/DTO/domain/repository/VM/UI + `PackageFormValidator` tests remain complete. **Explore**: `CarrierExploreContent` + `ShipperExploreContent` now surface route activity summary and popular routes; EN/FR strings updated. |
| **3** — Bookings & matches | Core marketplace loop + counter-offer parity | **Complete** — BookingsApi (20+ endpoints), DeliveryMatch (60+ fields) + BookingAction (10 cases) + BookingMapper + computed props (availableActions, pricing, codes); BookingType enum; 9 nested info types; 10 supporting models (CounterOfferContext, codes, tracking, stats); BookingsRepository (15 methods); MatchingViewModel + LiveTrackingViewModel + AutoChargeConfirmationViewModel; unified MatchCard (replaces 4 iOS cards) + MatchStatusBadge + PriceComparison + CounterOfferBanner + code views; RequestToCarrySheet + CounterOfferPromptSheet + AutoChargeSheet + RateDeliverySheet; unified MatchListScreen wired to tab; BookingSuccessScreen + code screens; 60+ tests; full i18n (EN+FR). |
| **4** — Payments & Stripe | … | **Partial** — closeout check run: PaymentSheet parity ✅, transactions parity ✅, payments test checklist ✅. Exit gate not met yet: Stripe Connect onboarding/dashboard journey still lacks full iOS parity states (loading/not setup/partial/complete, security notice, onboarding/dashboard sheet handling). |
| **5** — Chat & notifications | … | **Pending** — FCM service stub + `firebase-messaging` dependency only; feature implementation not started. |
| **6** — Profile, verification, favorites & ratings | … | **Pending** — feature implementation not started. |
| **7** — Legal, support, misc | … | **Pending** — feature implementation not started. |

---

## Phase 0 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| Gradle **Version Catalog** (`gradle/libs.versions.toml`) | Complete | AGP 8.8.2, Gradle 8.10.2, Kotlin 2.2.10, KSP, Compose |
| **`:core:designsystem`** | Complete | Core primitives: `PasabayanTheme`, tokens (`PasabayanBorder`, `PasabayanLayout`, `PasabayanMotion`, `PasabayanTextStyles`), `ds*` modifiers, `PButton`, `PCard`, `POutlinedTextField`, `PScaffold`, `PSnackbar`/`PSnackbarHost`, `PModalBottomSheet`, `PTopBar`, `PDivider`, `PCircularProgress`; JVM + androidTest. Spec **14** full color inventory / domain composites (e.g. `EmptyStateView`) still optional as features land. |
| **`:core:domain-error`** | Complete | `DomainError` + `ValidationError`; [01-error-taxonomy.md](01-error-taxonomy.md); **`userMessage()`**; **`ErrorAlertPolicy`** / `ErrorAlertContext` (foreground vs background, iOS parity) + JVM tests |
| **`:core:network`** | Complete | OkHttp + Retrofit + kotlinx-serialization; Hilt `NetworkModule`; `AuthInterceptor` (Bearer except `/auth/*` without `/auth/me`); **`ApiErrorMapper`** + error DTOs; **`Response.toDomainResult` / `foldDomainResult`**; `BuildConfig.API_BASE_URL` = production; `consumer-rules.pro` |
| **`:app`** | Partial | `@HiltAndroidApp` `PasabayanApplication`; `@AndroidEntryPoint` `MainActivity`; Compose shell showing API base URL |
| **Hilt** | Complete | App + `:core:network` (KSP) |
| **Google Services / Firebase** | Partial | `com.google.gms.google-services` plugin; `app/google-services.json`; Firebase BOM + **firebase-messaging**; **`PasabayanFirebaseMessagingService`** stub (token POST + routing **TODO** Phase 5) |
| **Google Sign-In** | Partial | `GoogleSignInHelper` + `AuthScreen` / `AuthViewModel`; Credential Manager / One Tap **not** wired (optional) |
| **Facebook Login** | Partial | `FacebookLoginStarter` + `LoginManager`; `MainActivity` forwards `onActivityResult` to `CallbackManager` |
| **Manifest & security** | Complete | `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `WAKE_LOCK`; `network_security_config` (HTTPS; debug cleartext for localhost); backup / data-extraction XML |
| **Tests** | Complete | `AuthInterceptorTest` + **`ApiErrorMapperTest`**; **`ErrorAlertPolicyTest`** (`:core:domain-error`); **`AuthRepositoryIntegrationTest`** (MockWebServer + Retrofit, login / me / logout) in `:core:session`; auth JSON decode tests in `:core:network`; design system JVM + androidTest |

---

## Phase 1 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| **`:core:session`** | Complete | `TokenStore`, `EncryptedTokenStore`; `StoredAuthTokenProvider`; `TokenClearingHandler` → `SessionInvalidationHandler` + **`UnauthorizedSessionNotifier`** (401 → UI); `AuthRepository` / `AuthRepositoryImpl`; `SessionModule` (Hilt); **`TokenClearingHandlerTest`** |
| **`app` dependency** | Complete | `implementation(project(":core:session"))` |
| **OAuth UI** | Complete | `AuthScreen` + `AuthRoute` + `AuthViewModel`; Google + Facebook → backend; **`DashboardScreen`** placeholder; **401** + `error_unauthorized`; **`didJustCompleteConsent`** via dashboard |
| **Onboarding (17)** | Complete | `RootViewModel` / `AppEntryContent`; `OnboardingRoute`; city + consent gates; prefs keys; consent failure still advances |

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
| [03-trips.md](03-trips.md) | [ ] | Partial — core Trips scope and major parity slices landed (extra APIs, repository wiring, domain fields/helpers, pagination, guards, create/cancel rules, progress state machine, sheets, store wiring, expanded tests). Remaining open scope is now primarily non-TDD UI/flow parity from **03-trips remaining blockers**. |
| [04-packages.md](04-packages.md) | [x] | API + DTOs, domain models, repository, ViewModel, UI screens, form validation (14 tests) |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | [x] | Tab shell, role switcher, carrier + shipper explore content, stats grids; Matches/Messages/Profile stubs |
| [05-bookings-matches.md](05-bookings-matches.md) | [x] | Unified MatchCard + MatchListScreen, 3 ViewModels, counter-offer flows, code screens, 60+ tests |
| [06-payments-stripe.md](06-payments-stripe.md) | [ ] | Partial — closeout check confirms most scope complete (PaymentSheet + transactions + tests). Remaining for phase gate: finalize `PayoutSetupScreen` parity per spec states and onboarding/dashboard sheet UX, then re-run Phase 4 verification. |
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | [ ] | Pending — implementation not started. |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | [ ] | Pending — implementation not started. |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | [ ] | Pending — implementation not started. |
| [10-verification.md](10-verification.md) | [ ] | Pending — implementation not started. |
| [11-favorites-ratings.md](11-favorites-ratings.md) | [ ] | Pending — implementation not started. |
| [12-legal-support-misc.md](12-legal-support-misc.md) | [ ] | Pending — implementation not started. |
| [15-platform-and-tab-index.md](15-platform-and-tab-index.md) | [ ] | Pending — full parity audit not completed yet. |

---

## 03-trips remaining blockers (mapped to spec sections/checklist)

- **Endpoints → POST `/trips` special error handling (`03-trips.md` “POST `/trips` — special error handling”, checklist item: POST error handling)**  
  Android `createTrip()` still maps non-2xx via generic `ApiErrorMapper`; it does not implement the spec-specific domain mapping for `MixedTransportTypes`, `NoTransportTypeSpecified`, `Unauthenticated`, and `UserNotCarrier`.

- **Services contract parity (`03-trips.md` “RouteActivitySummary”, “PopularRoute”)**  
  Android route-activity and popular-route DTO/domain shapes currently follow the iOS carrier-near-home summary variant (`package_delivery_near_home`, etc.) and destination trip-count route objects, not the exact `RouteActivitySummary`/`PopularRoute` fields documented in `03-trips.md` (`totalTrips`, `activeTrips`, `completedTrips`, `totalEarnings`, `currency`, `originCity`, `packageCount`, `averagePrice`).

- **ViewModel parity (`03-trips.md` ViewModels section, checklist items for Browse/Carrier/Progress VMs)**  
  `BrowseTripsViewModel` still lacks several spec-listed booking/compatibility actions/state (`selectTrip`, `selectPackage`, compatibility checks, booking state/action set).  
  `CarrierTripsViewModel` lacks explicit `cancelTrip()` and match-status-based cancel blocking (`confirmed` / `picked_up` / `in_transit`).  
  `TripPackageProgressViewModel` does not include the spec `Idle` state and does not expose full spec metrics fields (`status`, explicit label model).

- **UI parity (`03-trips.md` UI screens + trip details sections + creation UX detail)**  
  `EditTripSheet`, `CreateTripFromPackageScreen`, and `TripFilterSheet` are still missing.  
  `TripDetailsScreen` is missing spec-listed carrier sections (trip earnings overview, accepted packages list with `TripPackagesFilter`, richer carrier action set).  
  `TripCreationScreen` remains scaffold-level and does not yet implement the full spec flow (wizard/full-review modes, city autocomplete, saved-routes apply flow integration, keyboard focus chain, success flow).

- **Local/client behavior parity (`03-trips.md` Local state + checklist item for stores)**  
  `UsualTransportStore` exists but is not yet wired to auto-save on successful trip creation.  
  `CarrierDisclaimerStore` keys exist, but offline sync retry behavior is not integrated into startup/flow logic.  
  `SavedRoutesSheet` and `TripTutorialOverlay` exist but are not yet integrated into the primary trips flows.

- **TDD checklist parity (`03-trips.md` TDD checklist)**  
  **Resolved in closeout slices** — added/expanded tests now cover the previously listed gaps: POST `/trips` spec error mapping matrix, `TripUpdateRequest` planning vs non-planning encode constraints, `TripPackageProgressMetrics` status/labels derivation, and store integration behavior (`UsualTransportStore`, disclaimer pending-sync retry hook, local-state coordinator/use-case wiring).

---

## iOS feature folders → Android (from [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md))

- [x] `Authentication/` — Complete — `:core:session` + **AuthScreen** / **AuthRoute** / **AuthViewModel**; **401 → signed-out**  
- [ ] `Analytics/` — Pending — [19-analytics.md](19-analytics.md) not started.  
- [x] `Bookings/` — Complete — Phase 3 scope landed.  
- [ ] `Chat/` — Pending — implementation not started.  
- [ ] `Favorites/` — Pending — implementation not started.  
- [ ] `Legal/` — Pending — implementation not started.  
- [ ] `Notifications/` — Pending — FCM dependency + stub service only.  
- [x] `Onboarding/` — Complete — Phase 1 scope landed.  
- [x] `Packages/` — Complete — Phase 2 scope landed.  
- [x] `Payments/` — Partial — Phase 4 slice in progress; exit gate not met yet.  
- [ ] `Profile/` — Pending — implementation not started.  
- [ ] `Ratings/` — Pending — implementation not started.  
- [ ] `RouteActivity/` — Partial — summary endpoint + `RouteActivitySummaryViewModel` + carrier dashboard summary surface implemented via Trips parity slice; remaining Phase 7 RouteActivity scope pending.  
- [ ] `Shipper/` — Pending — implementation not started.  
- [ ] `Support/` — Pending — implementation not started.  
- [ ] `Trips/` — Partial — core Phase 2 scope plus major parity slice landed; remaining blockers are listed in **03-trips remaining blockers** above.  
- [ ] `Verification/` — Pending — implementation not started.  

**Cross-cutting**

- [x] Shared networking stack in `:core:network` — Complete — interceptor policy + prod base URL.  
- [x] Design tokens in `:core:designsystem` — Complete.  
- [x] App shell / tabs / Explore — Complete — built in Phase 2 (`MainTabScreen`, role switcher, carrier + shipper explore); tab-specific feature depth continues in later phases.  
- [x] `google-services.json` + Firebase project linkage for FCM — Partial — linkage done; runtime registration still TODO.  
- [x] Google / Facebook **sign-in flows** — Complete — Compose + backend done; Credential Manager / extra tests optional.
