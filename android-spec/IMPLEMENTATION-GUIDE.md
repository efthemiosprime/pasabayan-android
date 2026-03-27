# Android implementation guide (from this spec pack)

This is the **shortest path** to productive implementation without hunting through every file.

## 1. Read order (first week)

| Order | Document | Why |
|-------|----------|-----|
| 0 | [16-project-bootstrap.md](16-project-bootstrap.md) | **If the Android tree is empty:** Gradle Version Catalog, `:app` + `:core:*` modules, Hilt, Compose theme, Retrofit/OkHttp — **before** feature work |
| 1 | [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) | Phases, exit gates, workflow |
| 2 | [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md) | Confirms nothing in iOS `Features/` is missing |
| 3 | [00-architecture.md](00-architecture.md) + [14-design-system.md](14-design-system.md) | Stack + mandatory theme |
| 4 | [01-error-taxonomy.md](01-error-taxonomy.md) | Single error mapping for the whole app |
| 5 | [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) | Every path + Swift type; place DTOs next to the same feature as iOS `Models/` |
| 6 | [contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) | Endpoint groups to implement |

Then implement **by phase** using the numbered specs (`02`–`12`) and [13-ui-tab-explore.md](13-ui-tab-explore.md) / [15-platform-and-tab-index.md](15-platform-and-tab-index.md) for UI.

## 2. Per-screen / per-module workflow

1. Find the iOS **SwiftUI entry** (tab doc in [`docs/tabs/`](../tabs/) or feature `Views/`).
2. **Reuse check:** search `:core:*` and other `:feature:*` modules for an existing composable, repository, mapper, or use case that already covers the behavior — extend or parameterize before adding a parallel type ([00-architecture.md](00-architecture.md) — Reusability & DRY).
3. **Mirror structure:** place Android code in the same *roles* as iOS — `Models` → dto/domain, `Services` → data/api, `ViewModels` → presentation, `Views` → composables (see [00-architecture.md](00-architecture.md) “iOS layout / Android mirror”). If iOS splits `Views/Shipper` vs `Carrier`, mirror that split in packages.
4. Find **API** in `*APIService.swift`, [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md), or [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md).
5. Add or verify **YAML** rows for new endpoints.
6. **DTO tests first** → repository fake → ViewModel (MVVM) → Compose; shared logic → use case with tests.
7. Apply **only** [14-design-system.md](14-design-system.md) tokens for styling.

## 3. Where iOS tests live (parity targets)

| Area | Path |
|------|------|
| Unit | `PasabayanTests/*.swift` |
| UI | `PasabayanUITests/Flows/` |

Use [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) for phase-aligned checklists.

## 4. Easy mistakes to avoid

| Mistake | Fix |
|---------|-----|
| Skipping `/auth/me` Bearer rule | [01-error-taxonomy.md](01-error-taxonomy.md) |
| Wrong tab order / labels | [docs/tabs/README.md](../tabs/README.md), [13-ui-tab-explore.md](13-ui-tab-explore.md) |
| Notification type not handled | Full enum in [08-notifications-device-tokens.md](08-notifications-device-tokens.md) |
| Counter-offer only in UI spec | API in [05-bookings-matches.md](05-bookings-matches.md) |
| Analytics assumed to have API | Mock-only — [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md) |
| Stripe payment methods path | [06-payments-stripe.md](06-payments-stripe.md) (`/stripe/*` in `PaymentMethodsViewModel`) |
| New composable/repository while one exists in `:core` or another feature | [00-architecture.md](00-architecture.md) — search first, reuse; one repository per aggregate |
| Duplicated API or mapping logic in ViewModels | Route through `Repository` + shared mappers / [01-error-taxonomy.md](01-error-taxonomy.md) |
| New Gradle dependency for a one-off helper | Prefer stdlib/AndroidX; see [00-architecture.md](00-architecture.md) (Minimal third-party dependencies) |

## 5. Related repo docs (outside `android-spec/`)

| Doc | Use |
|-----|-----|
| [CLAUDE.md](../../CLAUDE.md) | High-level architecture |
| [docs/tabs/](../tabs/) | Per-tab view hierarchy |
| [BRANDING_DESIGN_GUIDE.md](../BRANDING_DESIGN_GUIDE.md) | Brand alignment with [14-design-system.md](14-design-system.md) |

## 6. When you add a new iOS feature later

1. Update **`FEATURE-COVERAGE-MATRIX.md`** (new row).
2. Update **phase spec** or add a subsection + **YAML** entries.
3. Update [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) if phase boundaries shift.
