# Android implementation status (living document)

**Purpose:** Track what is **done**, **in progress**, or **not started** for the Pasabayan Android app relative to this folder’s specs. Update this file whenever a phase milestone lands or a spec’s scope is implemented in code.

**Roadmap reference:** [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)  
**Feature coverage:** [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md)

---

## How to update

1. **After completing work on a spec or phase:** tick the matching checkbox (`[x]`) and add an optional **note** (PR link, commit, short description).
2. **If something is deferred:** mark **Deferred** in the Notes column and link the decision (issue or spec note).
3. **Current focus:** set **Current phase** below so the team knows where implementation is active.
4. **Keep in sync:** when [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) exit gates change, adjust the checklist here in the same PR.

---

## Current focus

| Field | Value |
|-------|--------|
| **Current phase** | Phase 0 — Foundation |
| **Last updated** | 2026-03-27 — Phase 0 scaffold (prod API base URL, `:core:*`, Hilt) |

---

## Phases (exit gates)

Tick **Exit gate** when the criteria in [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) are satisfied in the Android codebase.

- [ ] **Phase 0 — Foundation** — exit gate met
- [ ] **Phase 1 — Authentication** — exit gate met
- [ ] **Phase 2 — Trips, packages, Explore UI** — exit gate met
- [ ] **Phase 3 — Bookings & matches** — exit gate met
- [ ] **Phase 4 — Payments & Stripe** — exit gate met
- [ ] **Phase 5 — Chat & notifications** — exit gate met
- [ ] **Phase 6 — Profile, verification, favorites & ratings** — exit gate met
- [ ] **Phase 7 — Legal, support, misc & hardening** — exit gate met

---

## Phase 0 — Foundation (specs / artifacts)

| Spec / artifact | Done | Notes |
|-----------------|------|-------|
| [16-project-bootstrap.md](16-project-bootstrap.md) | [x] | `:app` + `:core:designsystem`, `:core:domain-error`, `:core:network`; Hilt; Retrofit/OkHttp; `assembleDebug` |
| [00-architecture.md](00-architecture.md) | [x] | Core modules mirror spec; lean stack (Retrofit, kotlinx-serialization) |
| [01-error-taxonomy.md](01-error-taxonomy.md) | [ ] | `DomainError` sealed in `:core:domain-error`; **HTTP → DomainError mappers** + tests still TODO |
| [14-design-system.md](14-design-system.md) | [x] | `PasabayanTheme`, spacing/radius/color/typography tokens in `:core:designsystem` (iterate vs iOS as UI lands) |
| [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) | [ ] | Reviewed / extended as DTOs land |
| [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) | [ ] | Rows verified for endpoints in use |

**Supporting (cross-phase):**

| Document | Done | Notes |
|----------|------|-------|
| [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) | [ ] | Team workflow agreed |
| [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) | [ ] | Tests scheduled per phase |

---

## Phase 1 — Authentication

| Spec | Done | Notes |
|------|------|-------|
| [02-auth-session.md](02-auth-session.md) | [ ] | |

---

## Phase 2 — Trips, packages, Explore

| Spec | Done | Notes |
|------|------|-------|
| [03-trips.md](03-trips.md) | [ ] | |
| [04-packages.md](04-packages.md) | [ ] | |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | [ ] | Tab shell / Explore (partial per phase doc) |

---

## Phase 3 — Bookings & matches

| Spec | Done | Notes |
|------|------|-------|
| [05-bookings-matches.md](05-bookings-matches.md) | [ ] | Includes counter-offer |

---

## Phase 4 — Payments & Stripe

| Spec | Done | Notes |
|------|------|-------|
| [06-payments-stripe.md](06-payments-stripe.md) | [ ] | |

---

## Phase 5 — Chat & notifications

| Spec | Done | Notes |
|------|------|-------|
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | [ ] | |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | [ ] | |

---

## Phase 6 — Profile, verification, favorites & ratings

| Spec | Done | Notes |
|------|------|-------|
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | [ ] | |
| [10-verification.md](10-verification.md) | [ ] | |
| [11-favorites-ratings.md](11-favorites-ratings.md) | [ ] | |

---

## Phase 7 — Legal, support, misc, platform audit

| Spec | Done | Notes |
|------|------|-------|
| [12-legal-support-misc.md](12-legal-support-misc.md) | [ ] | |
| [15-platform-and-tab-index.md](15-platform-and-tab-index.md) | [ ] | Tabs, onboarding, analytics mock |

---

## iOS feature folders → status (from [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md))

Tick when the Android implementation for that iOS feature area is **shipped** for the current milestone (or note partial).

- [ ] `Authentication/`
- [ ] `Analytics/` (mock UI — optional per matrix)
- [ ] `Bookings/`
- [ ] `Chat/`
- [ ] `Favorites/`
- [ ] `Legal/`
- [ ] `Notifications/`
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

**Cross-cutting services (parity):**

- [ ] `Services/APIService` rules → `:core:network` / interceptors _(auth path rules + prod `API_BASE_URL`; token provider stub until Phase 1)_
- [ ] `DesignSystem.swift` → theme tokens ([14-design-system.md](14-design-system.md))
- [ ] App shell / tabs → [13-ui-tab-explore.md](13-ui-tab-explore.md), [15-platform-and-tab-index.md](15-platform-and-tab-index.md)
