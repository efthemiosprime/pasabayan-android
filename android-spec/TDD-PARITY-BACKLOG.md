# TDD parity backlog (by phase)

**Workflow:** [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md). **Phase 0 cleanup TDD plan:** [PHASE-0-CLEANUP-TDD-PLAN.md](PHASE-0-CLEANUP-TDD-PLAN.md). **Feature inventory:** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md).

Organized **by implementation phase** (same order as [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)). Use with **per-feature** specs (`02`–`12`, plus [17-onboarding.md](17-onboarding.md) with Phase 1).

Test order: **contract** (DTO decode) → **repository** (fake API) → **MVI reducer** → **Compose UI** (critical paths only).

## Phase 0 — Foundation

- [x] HTTP + auth header policy matches iOS (`/auth/*` except `/auth/me` skips Bearer). *Tests:* `AuthInterceptorTest` in `:core:network` (incl. `/auth/google/login` without Bearer).
- [x] Domain error mapping matches [01-error-taxonomy.md](01-error-taxonomy.md). *Tests:* `ApiErrorMapperTest`; foreground/background presentation: `ErrorAlertPolicy` + `ErrorAlertPolicyTest` in `:core:domain-error`.
- [x] **Design system:** Compose `PasabayanTheme` (or equivalent) matches [14-design-system.md](14-design-system.md) — spacing scale, nav title **15 sp**, card 12 dp radius + 1 dp border, primary black-forward palette; no raw magic numbers in modules under test. *Tests:* JVM + androidTest in `:core:designsystem`.

## Phase 1 — Auth

- [ ] Parity: `PasabayanTests/AuthViewModelTests.swift` scenarios.
- [ ] **Onboarding:** navigation order and flags per [17-onboarding.md](17-onboarding.md) (`hasCompletedOnboarding`, city/consent gates, consent API keys); instrumented or integration smoke if iOS has UI tests for flows.

## Phase 2 — Trips & packages

- [ ] `TripDecodingTests`, `CreateTripRequestTests`, `PackagesAPIServiceAvailableParamsTests`, `PackageRequestDecoderTests`, `CreatePackageRequestTests`.

## Phase 3 — Bookings

- [ ] `MatchingViewModelTests`, `MatchingAPITests`, `BookingDecoderTests`, `BookingModelTests`, delivery/match card tests as applicable.
- [ ] **Counter-offer:** `CounterOfferTests`, `CompatibleTripsCounterOfferTests`, encode/decode `ShipperCounterOfferRequest` / `CarrierCounterOfferRequest` / `CounterOfferResponse` (see [05-bookings-matches.md](05-bookings-matches.md)).
- [x] **Counter-offer UI polish:** `CounterOfferContextTest` (helpers + `fromMatch` / `fromNotificationData` factories + regex fallback), `CounterOfferPromptValidatorTest` (positive / minimum / ±$0.005 same-price tolerance / ordering), expanded `CounterOfferErrorMapperTest` (ValidationError + ConsentRequired branches), `BookingMapperTest` propagation of `counter_offerer_id` / `counter_offerer_name` — 2026-05-12.

## Phase 4 — Payments

- [ ] `PaymentViewModelTests`, `PaymentModelsTests`, `StripeConfigTests`, `StripeConnectViewModelTests`, `ReceiptDetailViewModelTests`.

## Phase 5 — Chat & notifications

- [ ] `ChatMarkAsReadDedupeTests`, `ChatFailedMessageTests`, `DeviceTokenRegisterResponseParserTests`, `NotificationBadgeCountTests`.
- [ ] **Counter-offer notifications:** `NotificationRoutingTests`, `CounterOfferNotificationTests`, `CounterOfferContextNotificationDataTests` (type `counter_offer`, deep-link payload parity — see [08-notifications-device-tokens.md](08-notifications-device-tokens.md)).

## Phase 6 — Profile, verification, favorites

- [ ] `ProfileAPIServiceTests`, `PhoneVerificationModelsTests`, `FavoritesContractTests`.

## Phase 7 — Misc

- [ ] `RouteActivitySummaryTests`, legal/support as iOS coverage exists.
- [ ] **Tabs / cross-check:** parity with every file under `docs/tabs/` per [15-platform-and-tab-index.md](15-platform-and-tab-index.md); programmatic navigation events.

---

**Full test index:** enumerate `PasabayanTests/**/*Tests.swift` and map each file to a feature spec when adding Android tests.
