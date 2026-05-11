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
| **Current phase** | **Phases 4 / 6 — Complete**; **Phase 5 — Code surface complete** for spec [08](08-notifications-device-tokens.md) (dashboard bell + push-tap routing remain). **Phase 7 — Code surface complete** for spec [12](12-legal-support-misc.md) (legal API + sheet, support ticket multipart + form, location catalog + nearby carriers, home-city GPS detection with Haversine matcher, activity logger + health check); UI hookup into Profile/Help menu is the remaining integration step. |
| **Last updated** | 2026-05-11 — Spec [12](12-legal-support-misc.md) full surface landed across 6 slices (98 JVM tests). **Slice 1** Legal: `LegalApi` (status/agree/withdraw) + DTOs + domain (with `LegalDocument.localFilename` mapping `terms_of_service` / `privacy_policy` / `service_agreement` / `liability_waiver` aliases + FR fallback) + `LegalRepository` + `LegalAgreementViewModel` (optimistic state, drop-agreed-ids on partial agreement). **Slice 2** Legal UI: `LegalAssetCatalog` (snapshots `assets/articles/*-fr.html`), `AssetArticleWebView` (no-JS, blocks external nav), `LegalAgreementSheet` (linear progress + horizontal tabs + auto-advance + sticky agree footer), 27 HTML articles copied from iOS bundle (4 legal × EN/FR + support articles), EN/FR `strings_legal.xml`. **Slice 3** Support: `SupportApi` multipart + DTOs, `SupportCategory` (9 icons + `@StringRes`) + `SupportPriority` (3 colors) + `SupportTicketDraft` (subject 3-120 / email lenient / description ≥10 / 5 attachment cap), `AttachmentReader` (`ContentResolver` + `OpenableColumns.DISPLAY_NAME`), repository assembles `attachments[]` Laravel-array parts, `SupportTicketFormScreen` (category grid + filter chips + PickVisualMedia attachments + success dialog), EN/FR `strings_support.xml`. **Slice 4** Location/Shipper: `LocationCatalogApi`, `ShipperApi` (custom `KSerializer` for `data` object-or-array polymorphism), `LocationCatalogTransformer` (flattens countries → "City, ST" sorted/deduped + aliases + metro areas), `LocationCatalogRepository` (`StateFlow<LocationCatalogSnapshot>` with version-aware persistence + in-memory single-load), `ShipperRepository`, kotlin-serialization plugin enabled on `:app` module. **Slice 5** Home-city detection: `HomeCityMatcher` (Haversine within 50 km default radius + name-match override + tie-break by id), `LocationProvider` (`FusedLocationProviderClient.getCurrentLocation` + 10s timeout + permission gate), `ConfirmedHomeCityStore`, `DefaultHomeCityDetectionService` (parallel GPS + cities + profile, single-shot per session, NoChange when server PUT fails), manifest `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION`, `play-services-location:21.3.0`. **Slice 6** Activity logger + health: `SystemApi` (`GET /health` + `POST /activity-logs`), `ActivityLogJson` (camelCase wire), `ActivityLogger` (bounded `Channel(64)` queue on `NotificationAppScope`, no-op when user id ≤ 0, swallows failures), `ActivityLogDeviceContext` (`Android_${first8(ANDROID_ID)}` + `Pasabayan Android ${versionName}`), `HealthCheckService`. **Remaining for Phase 7 gate**: Profile menu deep-links into `LegalAgreementSheet` / `SupportTicketFormScreen`, post-login bootstrap call to `HomeCityDetectionService.detectIfNeeded()`, wire `SessionActivityLogUserProvider.update(authUser, role)` from auth flows. Phase 4 (spec [06](06-payments-stripe.md)), Phase 5 (specs [07](07-chat-broadcasting.md) + [08](08-notifications-device-tokens.md) code surface), and Phase 6 (specs [09](09-profile-carrier-consent.md) / [10](10-verification.md) / [11](11-favorites-ratings.md) / [20](20-profile-tab.md)) already covered in prior status updates. |
| **Spec audit** | **Complete** — YAML expanded from ~25 to ~100 endpoint rows; all feature specs updated with query params, multipart fields, WebSocket protocol, local storage keys, activity logs, cache policy, GPS services, badge rules, analytics mock structures |

---

## Phases (exit gates)

| Phase | Exit gate | Status |
|-------|-----------|--------|
| **0** — Foundation | See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) § Phase 0 | **Complete** — foundation modules, API-SHAPES (auth), **ErrorAlertPolicy**, TDD Phase 0 backlog ticked; app shell polish remains in later phases |
| **1** — Authentication | Login, token, `/auth/me`, logout | **Complete** — OAuth UI (Google/Facebook → backend); cold-start onboarding → auth; post-login city → consent → dashboard shell; **`UnauthorizedSessionNotifier`** + **`AuthViewModel`** signed-out on **401**; `didJustCompleteConsent` one-shot; auth DTO tests + **`AuthRepositoryIntegrationTest`** + **`TokenClearingHandlerTest`**. Optional: Credential Manager / One Tap, extra `AuthViewModel` tests. |
| **2** — Trips, packages, Explore | Browse/list/create flows + role tabs | **Complete** — `:core:domain` (15 enums, 4 shared models, FlexibleDecoders, DateTimeParsing); 12 new `P*` design-system components; tab shell with role switcher. **Trips** parity slices landed across API/repository/VM/UI/store/TDD: endpoints in `TripsApi` (`/routes/popular-packages`, `/route-activity/summary`, `/trips/{id}/matches`, `/packages/{id}/trip-template`), create-trip special error taxonomy, `PopularRoute` + `RouteActivitySummary` contract alignment, booking/compatibility state in `BrowseTripsViewModel`, explicit `cancelTrip()` blocking in `CarrierTripsViewModel`, `TripPackageProgressViewModel` idle + status labels, new UI surfaces (`TripFilterSheet`, `EditTripSheet`, `CreateTripFromPackageScreen`), carrier `TripDetailsScreen` sections, and local-state coordinator wiring (`UsualTransportStore`, saved-route autosave, disclaimer pending-sync retry hook). Decode/repository/VM/store tests were expanded, including `TripUpdateRequestJson` planning/non-planning encode constraints. **Packages**: API/DTO/domain/repository/VM/UI + `PackageFormValidator` tests remain complete. **Explore**: `CarrierExploreContent` + `ShipperExploreContent` surface route activity/popular routes; EN/FR strings updated. |
| **3** — Bookings & matches | Core marketplace loop + counter-offer parity | **Complete** — BookingsApi (20+ endpoints), DeliveryMatch (60+ fields) + BookingAction (10 cases) + BookingMapper + computed props (availableActions, pricing, codes); BookingType enum; 9 nested info types; 10 supporting models (CounterOfferContext, codes, tracking, stats); BookingsRepository (15 methods); MatchingViewModel + LiveTrackingViewModel + AutoChargeConfirmationViewModel; unified MatchCard (replaces 4 iOS cards) + MatchStatusBadge + PriceComparison + CounterOfferBanner + code views; RequestToCarrySheet + CounterOfferPromptSheet + AutoChargeSheet + RateDeliverySheet; unified MatchListScreen wired to tab; BookingSuccessScreen + code screens; 60+ tests; full i18n (EN+FR). |
| **4** — Payments & Stripe | PaymentSheet, transactions, Stripe Connect onboarding + dashboard | **Complete** — PaymentSheet parity ✅, transactions parity ✅, **Stripe Connect parity ✅**: debounce + single-flight `loadStatus` (2 s window, Mutex), typed `StripeConnectError` (NotCarrier / AlreadyOnboarded / NotOnboarded / ApiError) mapped from server messages, dedicated `PayoutSetupScreen` with all 4 iOS-parity states (loading / not setup / partial / complete) plus security notice + status checkmarks, Chrome Custom Tabs (`androidx.browser`) onboarding + dashboard with `handleOnboardingReturn` forced-refresh, profile menu deep-link, EN/FR strings. New tests: VM debounce / forceRefresh / `shouldStartOnboarding` guard, repository business-message mapping. |
| **5** — Chat & notifications | Realtime chat + FCM device-token routing + deep-link parity | **Partial — code surfaces ready** — `07-chat-broadcasting` hardening slices landed (realtime protocol/parser robustness, reconnect/polling state handling, thread + conversations parity upgrades, expanded tests). **`08-notifications-device-tokens` code surface complete**: NotificationApi + DTOs + 4-shape `UnreadCountResponseParser`, `NotificationType` (21 values), `NotificationRepository` with 403 push_notifications consent auto-retry, `NotificationLifecycleManager` wired into login/logout, `PasabayanFirebaseMessagingService` with foreground display + `PendingIntent`, `NotificationRouter` SharedFlow + per-type routing table, `NotificationViewModel` with per-role unread counts + optimistic mark-as-read, full `ComprehensiveNotificationsScreen` (3 sections) + components + EN/FR strings + 79 JVM tests. **Remaining for gate**: dashboard bell-icon entry point, `MainActivity` push-tap parse → `NotificationRouter.emitFromPush`, dashboard observer → navigation, cross-feature `ActionableItem` aggregation (bookings/packages/trips/chat/verification). |
| **6** — Profile, verification, favorites & ratings | Profile editing + disclaimers/consent + verification + favorites + ratings | **Complete** — [09](09-profile-carrier-consent.md) full surface: user profile edit (EditUserProfileSheet with smart pre-fill + additional_info merge), avatar upload + delete with 512px JPEG compression, carrier profile edit + preferences with create/update branching + enableCarrier, privacy preferences (scoped consent updates + disable-confirm), disclaimer bootstrap + retry-pending-sync, account deletion + GDPR data export, Settings screen with currency picker + clear-cache. [10](10-verification.md) phone OTP (E.164 normalisation + 60s resend cooldown) + premium ID/selfie multipart with status tracking. [11](11-favorites-ratings.md) favorites list with sort filter + remove + send-request sheet + scoped error mapping (409 → AlreadyFavorited, 400 → CannotFavorite(message)), tabbed Feedback screen for received/given/pending with in-place comment editing. [20](20-profile-tab.md) all menu deep-links wired (Personal info / Vehicle info / Verification / Payments hub / Settings / Favorites / Pending reviews / Account & data). 80+ new JVM tests. |
| **7** — Legal, support, misc | Legal agreements, support ticket, location catalog, GPS home-city detection, activity logs | **Partial — code surface complete** — Spec [12](12-legal-support-misc.md) shipped end-to-end across 6 slices: legal agreement API + repo + VM + `LegalAgreementSheet` with bundled HTML articles (EN+FR) + `AssetArticleWebView`; support ticket multipart contract + repo + 9-category × 3-priority form screen with `PickVisualMedia` attachments; location catalog with version-aware persistence + shipper nearby-carriers polymorphic decoder; `HomeCityMatcher` (Haversine within 50 km) + `LocationProvider` (`FusedLocationProviderClient`) + `HomeCityDetectionService` orchestrator + manifest permissions + `play-services-location` dep; activity logger with bounded coroutine queue + camelCase wire + `Android_${first8(ANDROID_ID)}` identifier + health-check service. **98 JVM tests** across DTOs, transformers, repositories, view models, and the GPS-matcher. **Remaining**: profile-menu/help-menu deep-links into the legal sheet + support form, post-login bootstrap call to `HomeCityDetectionService.detectIfNeeded()`, and `SessionActivityLogUserProvider.update()` wired from auth flows so the logger has a current user. |

---

## Phase 0 — What is implemented (codebase)

| Item | Status | Notes |
|------|--------|--------|
| Gradle **Version Catalog** (`gradle/libs.versions.toml`) | Complete | AGP 8.8.2, Gradle 8.10.2, Kotlin 2.2.10, KSP, Compose |
| **`:core:designsystem`** | Complete | Core primitives: `PasabayanTheme`, tokens (`PasabayanBorder`, `PasabayanLayout`, `PasabayanMotion`, `PasabayanTextStyles`), `ds*` modifiers, `PButton`, `PCard`, `POutlinedTextField`, `PScaffold`, `PSnackbar`/`PSnackbarHost`, `PModalBottomSheet`, `PTopBar`, `PDivider`, `PCircularProgress`; JVM + androidTest. Spec **14** full color inventory / domain composites (e.g. `EmptyStateView`) still optional as features land. |
| **`:core:domain-error`** | Complete | `DomainError` + `ValidationError`; [01-error-taxonomy.md](01-error-taxonomy.md); **`userMessage()`**; **`ErrorAlertPolicy`** / `ErrorAlertContext` (foreground vs background, iOS parity) + JVM tests |
| **`:core:network`** | Complete | OkHttp + Retrofit + kotlinx-serialization; Hilt `NetworkModule`; `AuthInterceptor` (Bearer except `/auth/*` without `/auth/me`); **`ApiErrorMapper`** + error DTOs; **`Response.toDomainResult` / `foldDomainResult`**, **`ResponseExt` helpers**; `ProfileApi` + profile/carrier DTOs + fixtures; `BuildConfig.API_BASE_URL` = production; `consumer-rules.pro` |
| **`:app`** | Partial | `@HiltAndroidApp` `PasabayanApplication`; `@AndroidEntryPoint` `MainActivity`; main shell; **Profile** feature (`ProfileRepository`, `ProfileTabViewModel`, `ProfileTabScreen`) wired in `MainTabScreen` (tab + payments hub) |
| **Hilt** | Complete | App + `:core:network` (KSP) |
| **Google Services / Firebase** | Complete | `com.google.gms.google-services` plugin; `app/google-services.json`; Firebase BOM + **firebase-messaging**; **`PasabayanFirebaseMessagingService`** wired (token registration via `NotificationLifecycleManager`, foreground heads-up via `NotificationDisplay` + `NotificationChannelSetup`); deep-link routing of push taps remains for Phase 5 closeout. |
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
| **Onboarding (17)** | Complete | `RootViewModel` / `AppEntryContent`; `OnboardingRoute`; city + consent gates; prefs keys; consent failure still advances; **city + consent profile writes** go through `ProfileApi` (aligned with [09](09-profile-carrier-consent.md) HTTP contract) |

---

## Phase 6 — What is implemented (complete)

| Item | Status | Notes |
|------|--------|-------|
| [20-profile-tab.md](20-profile-tab.md) | **Complete** | Profile tab shell with section order + role gating; **all menu deep-links wired**: Personal info → `EditUserProfileSheet`, Vehicle info (carrier only) → `EditCarrierProfileSheet`, Verification → `PhoneVerificationSheet` (with premium upgrade entry), Payments hub sub-route, Settings → `SettingsScreen` sub-route, Favorites → `FavoritesListScreen` sub-route, Pending reviews → `RatingsScreen` sub-route, Account & data → `AccountManagementSheet`. Compose previews + EN/FR strings + JVM + androidTest. TDD checklist green: section visibility, verification card visibility, avatar cache-buster, **load dedupe + forceRefresh**, navigation routes wired in `MainTabScreen`; logout-E2E and localization-lint rows remain documented as deferred per spec. |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | **Complete** | Repository surfaces all 16 endpoints. **Edit user profile** with smart pre-fill (profile → social-auth → email/phone-verified contact) + `additional_info` merge preserving legacy `oauth_*` / `language` keys. **Avatar** Photo Picker → `BitmapImageCompressor` (512 px JPEG q70) → `POST /profile` multipart preserving form fields; delete with destructive confirm dialog. **Carrier profile** create/update branching with 409 → GET fallback, `enableCarrier` after first create, `availableRoutes` + `preferredPickupCityId` preservation, package-type + restricted-item multi-selects. **Privacy preferences** sheet with scoped single-key `PUT`, optimistic + revert-on-failure, disable-confirm for push + location. **Disclaimer sync service** (bootstrap from server + retry-pending-syncs) wired via `MainTabScreen` `LaunchedEffect(user)` alongside legacy carrier-side retry. **Account deletion** with reason input, HTTP 202 success, sign-out on success. **GDPR data export** → pretty-printed JSON written to `cacheDir` → FileProvider share intent. **Settings screen** with role display + currency picker (`PreferredCurrencyStore`) + clear-cache + sub-flow routing into carrier-preferences / privacy / account-data. |
| [10-verification.md](10-verification.md) | **Complete** | **Phone OTP**: 4 endpoints, E.164 normalisation (`(514) 555-1234` → `+15145551234`), 60s resend cooldown, bootstrap resumes OTP step when `pendingVerifications > 0`. **Premium ID**: 3 endpoints including multipart `POST /verification/request-premium`, `IdDocumentType` enum (5 values; passport single-sided, others front+back), `ImageCompressor` reuse for ID images + selfie, status surfacing for existing pending applications. Premium entry point sits on the phone-verification verified card. |
| [11-favorites-ratings.md](11-favorites-ratings.md) | **Complete** | **Favorites**: 6 endpoints, list with sort chips (recent / most_used / rating) + "upcoming trips only" filter, remove with pending-removal tracking, send-request sheet with 4 cards (pickup / delivery / package / message); custom error mapping (409 → `AlreadyFavorited`, 400 → `CannotFavorite(message)`, 404 → `NotFound`). **Ratings**: 4 endpoints, tabbed Feedback screen with Received summary card, Given list with inline `PUT /ratings/{id}/comment` editing, Pending list with route + days-since-delivery (tolerant `FlexibleDoubleSerializer` decode). |

**Phase 6 exit gate (from [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)):** *Profile editing, disclaimers/consent, verification flows, favorites and ratings* — **met**.

**Deferred to follow-up specs** (not blockers for Phase 6 gate): home-city autocomplete + 10-minute cooldown (settings polish), Coil/Glide image rendering for avatars + carrier cards (cross-cutting), addFavorite UI entry point (lives in match/booking flows from spec 05), sent-direct-requests list UI, pagination on ratings lists.

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
| [03-trips.md](03-trips.md) | [x] | Complete for current parity scope — trip creation depth/integration blockers were closed: wizard/review flow, reusable `CityAutocompleteField` + local city autocomplete UX, in-flow `SavedRoutesSheet` apply, keyboard focus chain, success + save-route prompt, and `TripTutorialOverlay` primary-journey wiring with test coverage. |
| [04-packages.md](04-packages.md) | [x] | Strict parity bridge landed: package detail/edit/cancel flow wired through dashboard sheet routes, carrier explore moved to available-packages data path, package creation journeys integrate disclaimer + tutorial + saved descriptions/routes stores, packages VM/UI copy localized (EN/FR), and package stores/view-model coverage expanded with new tests. |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | [x] | Tab shell, role switcher, carrier + shipper explore content, stats grids; Matches/Messages; Profile tab per [20-profile-tab.md](20-profile-tab.md) (shell + payments hub) |
| [05-bookings-matches.md](05-bookings-matches.md) | [x] | Unified MatchCard + MatchListScreen, 3 ViewModels, counter-offer flows, code screens, 60+ tests |
| [06-payments-stripe.md](06-payments-stripe.md) | [x] | Stripe Connect parity closed — debounced `StripeConnectViewModel`, typed `StripeConnectError` server-message mapping, `PayoutSetupScreen` (loading / not setup / partial / complete), Chrome Custom Tabs onboarding + dashboard, profile menu deep-link, EN/FR strings, tests green. |
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | [x] | Core + hardening + parity closeout landed in app + `:core:network` + `:core:designsystem`: contracts/decode tests, repository/realtime/merge logic, full-flow realtime protocol tests (including nested subscription-success payload), parser hardening for wrapped/direct string/object payloads, reconnect auto-resubscribe verification, ViewModel decode-recovery + polling coalescing + delete/retry/no-duplicate-temp coverage, thread/conversation UI parity upgrades, DS chat primitives adoption, and EN/FR chat localization resources. Phase 5 gate still depends on `08-notifications-device-tokens.md`. |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | [~] | Code surface complete (DTOs + repository + FCM lifecycle + VM + router + UI + 79 tests). Remaining for Phase 5 gate: dashboard bell entry point, push-tap parsing into `NotificationRouter.emitFromPush`, cross-feature `ActionableItem` aggregation. |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | [x] | Edit user/carrier profile, avatar upload/delete, consent prefs (scoped + revert), disclaimer sync, account deletion, GDPR export, Settings screen — see Phase 6 detail above. |
| [10-verification.md](10-verification.md) | [x] | Phone OTP flow + premium ID/selfie multipart — see Phase 6 detail above. |
| [11-favorites-ratings.md](11-favorites-ratings.md) | [x] | Favorites list + send-request + tabbed Feedback (received/given/pending) — see Phase 6 detail above. |
| [12-legal-support-misc.md](12-legal-support-misc.md) | [~] | Code surface complete (legal API+repo+VM+sheet, support multipart+form, location catalog+nearby carriers, home-city GPS detection, activity logger + health, 98 JVM tests). Remaining: profile/help menu hooks for the legal sheet + support form, post-login `HomeCityDetectionService.detectIfNeeded()` call, and `SessionActivityLogUserProvider.update()` from auth flows. |
| [15-platform-and-tab-index.md](15-platform-and-tab-index.md) | [ ] | Pending — full parity audit not completed yet. |
| [20-profile-tab.md](20-profile-tab.md) | [x] | Tab shell + all menu deep-links wired (Personal info / Vehicle info / Verification / Payments hub / Settings / Favorites / Pending reviews / Account & data); TDD checklist green except deferred logout-E2E and localization-lint rows. |

---

## 03-trips parity notes (recently resolved)

- **Trip creation UX depth (`03-trips.md` Trip creation UX detail)**  
  `TripCreationScreen` now supports wizard/full-review transitions, local city autocomplete catalog UX, in-flow saved-route apply, keyboard focus chain behavior, and success flow with save-route prompt.

- **Trips UI integration coverage (`03-trips.md` screens/components integration)**  
  `EditTripSheet`, `CreateTripFromPackageScreen`, `TripFilterSheet`, carrier `TripDetailsScreen`, and primary creation-journey integrations are now wired with deterministic route/open-close behavior coverage.

- **Local/client integration depth (`03-trips.md` Local state section)**  
  `SavedRoutesSheet` and `TripTutorialOverlay` are now integrated in primary trip creation journeys; `UsualTransportStore` and saved-route persistence are applied in the manual creation success path.

- **TDD checklist parity (`03-trips.md` TDD checklist)**  
  Coverage now includes trip creation flow-state transitions, saved-route store behavior, trip-creation view-model state machine, success-flow save/skip branch assertions, and route determinism tests alongside prior API/repository/VM/store closeout coverage.

---

## iOS feature folders → Android (from [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md))

- [x] `Authentication/` — Complete — `:core:session` + **AuthScreen** / **AuthRoute** / **AuthViewModel**; **401 → signed-out**  
- [ ] `Analytics/` — Pending — [19-analytics.md](19-analytics.md) not started.  
- [x] `Bookings/` — Complete — Phase 3 scope landed.  
- [x] `Chat/` — Complete for core Android spec slices — contracts + realtime service + repository/merge + ViewModels + UI/messages tab + localization landed; remaining Phase 5 completion depends on notifications/deep-link gate.  
- [x] `Favorites/` — Complete — list/sort/filter/remove + send-request sheet wired from profile menu; custom error mapping for 409/400/404.  
- [~] `Legal/` — Code surface complete — `LegalApi` (status/agree/withdraw) + repo + `LegalAgreementViewModel` (optimistic agree, partial outcomes) + `LegalAgreementSheet` (linear progress, doc tabs, bundled HTML articles via `AssetArticleWebView` with EN/FR fallback) + EN/FR strings; remaining: profile/help menu deep-link.  
- [~] `Notifications/` — Code surface complete — FCM token register/unregister wired into login/logout, foreground heads-up display, `NotificationRouter` SharedFlow + per-type routing table, `NotificationViewModel` with per-role counts + optimistic mark-as-read, `ComprehensiveNotificationsScreen` (3 sections) + 79 JVM tests. Remaining: dashboard bell entry point, push-tap deep-link wiring, cross-feature `ActionableItem` aggregation.  
- [x] `Onboarding/` — Complete — Phase 1 scope landed.  
- [x] `Packages/` — Complete — strict `04-packages.md` parity bridge landed (detail/edit/cancel flow wiring, carrier available-packages path split, local store journey integration, localization hardening, and test/spec sync).  
- [x] `Payments/` — Complete — PaymentSheet + transactions + Stripe Connect onboarding/dashboard with full iOS-parity states; Phase 4 exit gate met.  
- [x] `Profile/` — Complete — full edit user/carrier flows, avatar upload + delete, privacy preferences, disclaimer sync, account deletion + GDPR export, Settings screen with currency + clear-cache. All [20](20-profile-tab.md) menu deep-links wired.  
- [x] `Ratings/` — Complete — tabbed Feedback screen (received summary + cards, given with inline comment edit, pending list); rating submission flows through bookings (spec 05).  
- [ ] `RouteActivity/` — Partial — summary endpoint + `RouteActivitySummaryViewModel` + carrier dashboard summary surface implemented via Trips parity slice; remaining Phase 7 RouteActivity scope pending.  
- [~] `Shipper/` — Code surface only — `ShipperApi` (`GET /shipper/nearby-carriers`) + tolerant `KSerializer` for object-or-empty-array `data` shape + `ShipperRepository` + `NearbyCarriers` domain; UI not yet wired.  
- [~] `Support/` — Code surface complete — `SupportApi` multipart `POST /support/tickets` + 9-category × 3-priority enums + `SupportTicketDraft` validation + `AttachmentReader` (`ContentResolver` + `OpenableColumns`) + `SupportTicketFormScreen` (category grid + filter chips + `PickVisualMedia` attachments + success dialog) + EN/FR strings; remaining: profile/help menu deep-link.  
- [x] `Trips/` — Complete for current parity scope — includes My Trips detail/edit/cancel wiring, trip sheet route determinism coverage, and full trip-creation journey depth/integration slices from `03-trips.md`.  
- [x] `Verification/` — Complete — phone OTP flow (send/verify/resend/status, E.164 + 60s cooldown) and premium ID/selfie multipart with status surfacing.  

**Cross-cutting**

- [x] Shared networking stack in `:core:network` — Complete — interceptor policy + prod base URL.  
- [x] Design tokens in `:core:designsystem` — Complete.  
- [x] App shell / tabs / Explore — Complete — built in Phase 2 (`MainTabScreen`, role switcher, carrier + shipper explore); tab-specific feature depth continues in later phases.  
- [ ] `google-services.json` + Firebase project linkage for FCM — Partial — linkage done; runtime registration still TODO.  
- [x] Google / Facebook **sign-in flows** — Complete — Compose + backend done; Credential Manager / extra tests optional.
