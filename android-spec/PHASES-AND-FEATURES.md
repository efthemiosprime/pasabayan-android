# Phases and features (Android rebuild)

This document is the **primary roadmap**: work **by phase**, and within each phase **by feature**. Each phase has a clear exit gate (specs + TDD) before the next phase starts.

**What’s done in the Android repo:** [IMPLEMENTATION-STATUS.md](IMPLEMENTATION-STATUS.md).

**Implementation quick start:** [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md). **Empty Gradle project:** [16-project-bootstrap.md](16-project-bootstrap.md). **Ensure no missing iOS modules:** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md).

## Feature → iOS code → spec file

| Feature (product) | iOS location | Spec |
|-------------------|--------------|------|
| Foundation / errors | `Pasabayan/Services/APIService.swift`, `Utilities/APIErrorFormatter.swift` | [00-architecture.md](00-architecture.md), [01-error-taxonomy.md](01-error-taxonomy.md) |
| **Design system** | `Views/Components/DesignSystem.swift` | [**14-design-system.md**](14-design-system.md) — **all UI must follow** |
| Authentication | `Features/Authentication/` | [02-auth-session.md](02-auth-session.md) |
| Trips | `Features/Trips/` | [03-trips.md](03-trips.md) |
| Packages | `Features/Packages/` | [04-packages.md](04-packages.md) |
| Bookings / matches (incl. counter-offer) | `Features/Bookings/` | [05-bookings-matches.md](05-bookings-matches.md) |
| Payments / Stripe | `Features/Payments/` | [06-payments-stripe.md](06-payments-stripe.md) |
| Chat | `Features/Chat/` | [07-chat-broadcasting.md](07-chat-broadcasting.md) |
| Notifications (in-app types, e.g. counter-offer) | `Features/Notifications/` | [08-notifications-device-tokens.md](08-notifications-device-tokens.md) |
| Profile / carrier / consent | `Features/Profile/` | [09-profile-carrier-consent.md](09-profile-carrier-consent.md) |
| Verification | `Features/Verification/` | [10-verification.md](10-verification.md) |
| Favorites | `Features/Favorites/` | [11-favorites-ratings.md](11-favorites-ratings.md) |
| Ratings | `Features/Ratings/` | [11-favorites-ratings.md](11-favorites-ratings.md) |
| Legal | `Features/Legal/` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Support | `Features/Support/` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Shipper (nearby) | `Features/Shipper/` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Route activity | `Features/RouteActivity/` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| UI shell / all tabs (`docs/tabs/`) | `Views/Screens/Dashboard/`, `docs/tabs/` | [13-ui-tab-explore.md](13-ui-tab-explore.md) + [14-design-system.md](14-design-system.md) + [15-platform-and-tab-index.md](15-platform-and-tab-index.md) |
| Onboarding | `Features/Onboarding/` | [**17-onboarding.md**](17-onboarding.md) (`OnboardingScreen`, city, consent, `@AppStorage` / flags) |
| Analytics (UI, mock) | `Features/Analytics/` | [15-platform-and-tab-index.md](15-platform-and-tab-index.md) — **no REST API** in folder; mock stats |

## Phase breakdown (implementation order)

### Phase 0 — Foundation

| Item | Spec / artifact |
|------|------------------|
| **Gradle scaffold** (Version Catalog, `:core:network`, `:core:domain-error`, `:core:designsystem`, Hilt, Compose) | [**16-project-bootstrap.md**](16-project-bootstrap.md) |
| Architecture, ROP, UI parity rules | [00-architecture.md](00-architecture.md) (includes **iOS `Features/` → Android module mirror**) |
| Error mapping, auth header rules | [01-error-taxonomy.md](01-error-taxonomy.md) |
| **Design tokens + mandatory theme rules** | [**14-design-system.md**](14-design-system.md) |
| **Localization (i18n) — build with from day one** | [**18-localization.md**](18-localization.md) |
| Endpoint + type catalog | [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) |
| Endpoint inventory (living document) | [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) |

**Exit gate:** [16-project-bootstrap.md](16-project-bootstrap.md) checklist complete (`assembleDebug`, Hilt `Application`, theme from `:core:designsystem`, Retrofit/OkHttp wired in `:core:network`); **Compose theme implements** [14-design-system.md](14-design-system.md) (spacing, typography, colors, radii, cards); Gradle modules align with iOS `Pasabayan/Features/<Name>/` boundaries ([00-architecture.md](00-architecture.md)); **DI**, **repository boundaries**, and **reuse/DRY** rules documented ([00-architecture.md](00-architecture.md) — Reusability & DRY); **dependency policy** agreed (stdlib + AndroidX + Kotlinx first; see [00-architecture.md](00-architecture.md) — Minimal third-party dependencies); matrix + API shapes reference list paths used by iOS services (extend as needed).

---

### Phase 1 — Authentication

| Feature | Spec |
|---------|------|
| Auth & session | [02-auth-session.md](02-auth-session.md) |
| Onboarding (first launch, post-login city + consent) | [**17-onboarding.md**](17-onboarding.md) |

**Exit gate:** Login → token persist → `/auth/me` user load → logout; contract tests for auth DTOs. **Onboarding parity:** cold-start `OnboardingScreen` equivalent before auth when not completed; after login, city → consent → dashboard order and persistence keys per [17-onboarding.md](17-onboarding.md).

---

### Phase 2 — Trips, packages, Explore UI (partial)

| Feature | Spec |
|---------|------|
| Trips | [03-trips.md](03-trips.md) |
| Packages | [04-packages.md](04-packages.md) |
| Tab shell & Explore parity | [13-ui-tab-explore.md](13-ui-tab-explore.md) |

**Exit gate:** Browse/list/create flows aligned with iOS Explore + role tabs for screens that depend only on trips/packages APIs.

---

### Phase 3 — Bookings & matches

| Feature | Spec |
|---------|------|
| Matches, booking, compatibility, codes, receipts, **counter-offer** (same endpoints as request flows with `is_counter_offer`) | [05-bookings-matches.md](05-bookings-matches.md) |

**Exit gate:** Core marketplace loop (request → accept → confirm → pickup → deliver) matches iOS behavior; **shipper and carrier counter-offer** payloads and `CounterOfferResponse` parity.

---

### Phase 4 — Payments & Stripe

| Feature | Spec |
|---------|------|
| Payments, Stripe Connect, receipts | [06-payments-stripe.md](06-payments-stripe.md) |

**Exit gate:** Payment sheet / transactions / connect onboarding parity with iOS.

---

### Phase 5 — Chat & notifications

| Feature | Spec |
|---------|------|
| Chat + broadcasting | [07-chat-broadcasting.md](07-chat-broadcasting.md) |
| Device tokens + notification history + **notification types** (e.g. `counter_offer` routing) | [08-notifications-device-tokens.md](08-notifications-device-tokens.md) |

**Exit gate:** Conversations, messages, read state, push token registration, unread counts; **counter-offer** notifications open the same deep-link context as iOS (see [05-bookings-matches.md](05-bookings-matches.md)).

---

### Phase 6 — Profile, verification, favorites & ratings

| Feature | Spec |
|---------|------|
| Profile, carrier, consent | [09-profile-carrier-consent.md](09-profile-carrier-consent.md) |
| Phone / premium verification | [10-verification.md](10-verification.md) |
| Favorites & ratings | [11-favorites-ratings.md](11-favorites-ratings.md) |

**Exit gate:** Profile editing, disclaimers/consent, verification flows, favorites and ratings lists.

---

### Phase 7 — Legal, support, misc APIs & hardening

| Feature | Spec |
|---------|------|
| Legal, support, locations, shipper nearby, routes, activity, health, **home city detection** | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Tab/docs audit, onboarding, analytics mock, programmatic nav parity | [15-platform-and-tab-index.md](15-platform-and-tab-index.md) |

**Exit gate:** Remaining endpoints integrated; [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) reviewed for `ambiguous` rows; **all `docs/tabs/*.md` screens** accounted for in Android navigation; polish.

---

## Per-feature workflow (repeat every feature)

1. Read the **feature spec** (`02`–`13`) and [**15-platform-and-tab-index.md**](15-platform-and-tab-index.md) when touching tabs or shell.
2. For UI: follow [**14-design-system.md**](14-design-system.md) (theme tokens only) and align structure with [13-ui-tab-explore.md](13-ui-tab-explore.md) where that feature surfaces.
3. Add or update rows in **api-contract-matrix.yaml** for endpoints touched.
4. **TDD:** contract → repository → reducer → critical UI (see [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md)).

## Per-phase workflow

- Complete **all features** listed under the phase (or explicitly defer with a note in the spec).
- Meet the **exit gate** before starting the next phase.
- Cross-cutting **UI parity** is validated in [13-ui-tab-explore.md](13-ui-tab-explore.md) as each phase unlocks new screens.
