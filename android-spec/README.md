# Android rebuild — specification index

Spec-driven documentation for re-implementing Pasabayan on Android. **Work is organized in two ways:**

1. **By phase** (0→7) — implementation order, dependencies, and exit gates.  
2. **By feature** — each product area maps to iOS `Pasabayan/Features/<Name>/` and its own spec file.

**Start here:** [**IMPLEMENTATION-GUIDE.md**](IMPLEMENTATION-GUIDE.md) — read order, daily workflow, common pitfalls.

**What’s done vs pending:** [**IMPLEMENTATION-STATUS.md**](IMPLEMENTATION-STATUS.md) — phase + per-spec checklists; update when milestones land.

**Empty Android repo:** [**16-project-bootstrap.md**](16-project-bootstrap.md) — initialize Gradle, Version Catalog, `:core:*` modules, Hilt, Compose theme, network stack.

Then: [**PHASES-AND-FEATURES.md**](PHASES-AND-FEATURES.md) — phase table, exit gates, **per-phase** and **per-feature** workflows.

**No missing features:** [**FEATURE-COVERAGE-MATRIX.md**](FEATURE-COVERAGE-MATRIX.md) — every `Pasabayan/Features/*` folder + key `Services/` mapped to a spec.

**Tabs / audit:** [**15-platform-and-tab-index.md**](15-platform-and-tab-index.md) — every [`docs/tabs/`](../tabs/) file + programmatic navigation.

## Principles

- **API source of truth:** current iOS code under `Pasabayan/` (see [CLAUDE.md](../../CLAUDE.md)); request/response types: [**API-SHAPES-REFERENCE.md**](API-SHAPES-REFERENCE.md).
- **Code structure mirror:** Android modules and packages should follow the same **feature boundaries and folder roles** as iOS `Features/<Name>/{Models,Services,ViewModels,Views}` — see [00-architecture.md](00-architecture.md) (iOS layout → Android mirror table).
- **UI parity:** mirror iOS tabs, flows, hierarchy, tokens, and copy unless a spec documents an exception — [00-architecture.md](00-architecture.md), [13-ui-tab-explore.md](13-ui-tab-explore.md).
- **Design system (mandatory):** Android UI **must follow** [**14-design-system.md**](14-design-system.md) — tokens are defined from iOS [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift). No arbitrary spacing/colors outside the theme unless a spec documents an exception.
- **DRY and reuse:** Clean Architecture + MVVM, repositories as single source of truth, DI, use cases when logic is shared — **search `:core` and existing features before adding** new composables or repositories. Details: [00-architecture.md](00-architecture.md) (Reusability, DRY, and “check before you add”).
- **Lean dependencies:** Prefer **Kotlin stdlib + AndroidX/Jetpack + Kotlinx Coroutines**; add other libraries only with justification — [00-architecture.md](00-architecture.md) (Minimal third-party dependencies).

## How to use (per feature, per phase)

| Step | Action |
|------|--------|
| 0 | If the Android project is empty or new: follow [**16-project-bootstrap.md**](16-project-bootstrap.md) (modules, dependencies, Hilt, theme). |
| 0b | Track progress in [**IMPLEMENTATION-STATUS.md**](IMPLEMENTATION-STATUS.md) (tick phases/specs when done). |
| 1 | Confirm scope in [**FEATURE-COVERAGE-MATRIX.md**](FEATURE-COVERAGE-MATRIX.md) (feature → spec). |
| 2 | Open [**PHASES-AND-FEATURES.md**](PHASES-AND-FEATURES.md) and find your **phase**. |
| 3 | For any UI work, apply [**14-design-system.md**](14-design-system.md) (theme tokens — no one-off magic numbers). |
| 4 | Open the **numbered feature spec** for that area. |
| 5 | Align Gradle module / package layout with iOS `Features/<Name>/` roles ([**00-architecture.md**](00-architecture.md)). |
| 6 | Update or verify [**contracts/api-contract-matrix.yaml**](contracts/api-contract-matrix.yaml) and [**API-SHAPES-REFERENCE.md**](API-SHAPES-REFERENCE.md) for endpoints you touch. |
| 7 | Follow [**TDD-PARITY-BACKLOG.md**](TDD-PARITY-BACKLOG.md) for test order and iOS test parity. |

## Phase → specs (summary)

| Phase | Focus | Specs |
|-------|--------|--------|
| **0** | Foundation | [**16-project-bootstrap.md**](16-project-bootstrap.md) (Gradle scaffold), [00-architecture.md](00-architecture.md), [01-error-taxonomy.md](01-error-taxonomy.md), [**14-design-system.md**](14-design-system.md) (mandatory theme), [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) |
| **1** | Auth | [02-auth-session.md](02-auth-session.md) |
| **2** | Trips, packages, Explore UI | [03-trips.md](03-trips.md), [04-packages.md](04-packages.md), [13-ui-tab-explore.md](13-ui-tab-explore.md) |
| **3** | Bookings / matches | [05-bookings-matches.md](05-bookings-matches.md) |
| **4** | Payments / Stripe | [06-payments-stripe.md](06-payments-stripe.md) |
| **5** | Chat, notifications | [07-chat-broadcasting.md](07-chat-broadcasting.md), [08-notifications-device-tokens.md](08-notifications-device-tokens.md) |
| **6** | Profile, verification, favorites & ratings | [09-profile-carrier-consent.md](09-profile-carrier-consent.md), [10-verification.md](10-verification.md), [11-favorites-ratings.md](11-favorites-ratings.md) |
| **7** | Legal, support, misc, hardening, tab audit | [12-legal-support-misc.md](12-legal-support-misc.md), [**15-platform-and-tab-index.md**](15-platform-and-tab-index.md) |

## All documents (by number)

| File | Topic |
|------|--------|
| [**IMPLEMENTATION-GUIDE.md**](IMPLEMENTATION-GUIDE.md) | **Start here** — read order, workflow, pitfalls |
| [**IMPLEMENTATION-STATUS.md**](IMPLEMENTATION-STATUS.md) | **Progress** — what’s done; phase + spec checklists (keep updated) |
| [**16-project-bootstrap.md**](16-project-bootstrap.md) | **Empty project** — Version Catalog, modules, deps, Hilt, theme |
| [**API-SHAPES-REFERENCE.md**](API-SHAPES-REFERENCE.md) | **Endpoints + Swift types** — mirror in DTOs / repositories |
| [**FEATURE-COVERAGE-MATRIX.md**](FEATURE-COVERAGE-MATRIX.md) | **Every iOS feature folder** → spec (gap check) |
| [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) | **Phase + feature matrix** (primary roadmap) |
| [00-architecture.md](00-architecture.md) | Compose, Clean Architecture, MVI, ROP, UI parity |
| [01-error-taxonomy.md](01-error-taxonomy.md) | HTTP and domain errors |
| [02-auth-session.md](02-auth-session.md) | Feature: Authentication |
| [03-trips.md](03-trips.md) | Feature: Trips |
| [04-packages.md](04-packages.md) | Feature: Packages |
| [05-bookings-matches.md](05-bookings-matches.md) | Feature: Bookings (includes **counter-offer** API + UI) |
| [06-payments-stripe.md](06-payments-stripe.md) | Feature: Payments |
| [07-chat-broadcasting.md](07-chat-broadcasting.md) | Feature: Chat |
| [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | Feature: Notifications (REST + **types** e.g. `counter_offer`, routing) |
| [09-profile-carrier-consent.md](09-profile-carrier-consent.md) | Feature: Profile / carrier / consent |
| [10-verification.md](10-verification.md) | Feature: Verification |
| [11-favorites-ratings.md](11-favorites-ratings.md) | Features: Favorites & ratings |
| [12-legal-support-misc.md](12-legal-support-misc.md) | Legal, support, catalog, shipper, routes, activity |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | UI: tabs & Explore (cross-cutting) |
| [**14-design-system.md**](14-design-system.md) | **Design tokens** (spacing, type, color, radii, cards) — **follow for all UI** |
| [**15-platform-and-tab-index.md**](15-platform-and-tab-index.md) | **Tab docs index** + iOS feature audit (`docs/tabs/`, onboarding, analytics mock, realtime) |
| [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) | TDD checklist **by phase** |
| [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) | Endpoint matrix **by feature group** |

## Updating specs

When iOS behavior changes: update the **feature spec**, then **PHASES-AND-FEATURES.md** if scope/phase shifts, then the **YAML** rows for that feature. **If `DesignSystem.swift` changes**, update [**14-design-system.md**](14-design-system.md) and the Android theme in lockstep. Use `ambiguous: true` or `notes` for legacy or multi-shape responses.
