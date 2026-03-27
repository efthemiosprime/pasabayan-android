# 00 — Android architecture (spec)

**Phase:** 0 (foundation). **Scope:** cross-cutting — not a single product feature. For **phase order and feature mapping**, see [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md).

## Goals

- **Parity** with iOS product behavior while improving **explicit** state and navigation (reduce reliance on implicit side effects).
- **UI parity**: Android screens, navigation, hierarchy, and visible copy should **mirror and align** with the current iOS app unless a spec explicitly documents an intentional platform difference. See [13-ui-tab-explore.md](13-ui-tab-explore.md).
- **Design system (mandatory):** All spacing, typography, colors, corner radii, card chrome, and layout constants **must follow** [**14-design-system.md**](14-design-system.md), derived from [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift). Feature composables must not introduce unstructured magic numbers.
- **Spec-driven**: each feature has a markdown spec + YAML contract rows before implementation.
- **TDD**: contract tests → repository fakes → MVI reducer tests → minimal Compose UI tests for critical journeys.
- **Lean dependencies:** Prefer **Kotlin stdlib + AndroidX / Jetpack + Kotlinx Coroutines**; add other artifacts only when necessary and document why — see [Minimal third-party dependencies](#minimal-third-party-dependencies).
- **New / empty Android repo:** Gradle modules, Version Catalog, and baseline deps — [16-project-bootstrap.md](16-project-bootstrap.md).

## Stack (target)

| Layer | Choice |
|-------|--------|
| UI | Jetpack Compose |
| Structure | **Clean Architecture** + **MVVM**: presentation (ViewModel + Compose) / domain / data per feature |
| UI state | ViewModel owns **one** observable state: `StateFlow<UiState>` (or `MutableStateFlow` internally); complex screens may use explicit **events + reducer** (MVI-style) inside the same ViewModel — still one ViewModel per screen |
| Async | Kotlin Coroutines + Flow (official Kotlin extension; keep on default BOM) |
| Errors | **ROP** with **`kotlin.Result`** and **`DomainError`** sealed types — use `fold` / `map` / `getOrElse`; **no** Arrow or other FP libraries unless explicitly approved |
| DI | **Hilt** (Jetpack) preferred — one graph, generated; avoid a second DI framework. Constructors receive interfaces; do not `SomeRepository()` in Composables |

## Minimal third-party dependencies

**Default:** solve problems with **platform APIs**, **Kotlin stdlib**, **AndroidX** (Compose, Navigation, Lifecycle, Room, DataStore, WorkManager, **Hilt**), and **Kotlinx** (Coroutines; Serialization **or** a single JSON strategy — pick **one**).

| Prefer | Avoid / defer |
|--------|----------------|
| `java.time` / `kotlinx.datetime` (if needed) for dates | Multiple date libraries |
| Kotlin **`Result`**, sealed errors, extension functions | Arrow, additional FP stacks |
| One HTTP stack: **OkHttp + Retrofit** — do not add Ktor Client or a second HTTP client for the same API | Parallel HTTP libraries, duplicate stacks |
| **Coil** for images in Compose (if needed) — one loader | Several image/cache libraries |
| **Room** or **DataStore** for persistence | ORMs beyond what Jetpack provides without justification |
| Jetpack **Navigation Compose** | Multiple navigation libraries |
| Logging: `android.util.Log` behind a thin `AppLogger` interface in `:core` | Pluggable logging frameworks unless product requires them |

**Adding a dependency:** require a short rationale (problem, why stdlib/AndroidX is insufficient, maintenance, size). **Do not** add a library because a blog post used it; **check first** for an AndroidX or Kotlin stdlib solution.

**JSON:** choose **Kotlinx Serialization** *or* **Moshi** (with codegen) — **not both** for the same DTOs. Prefer kotlinx Serialization to stay in the Kotlin ecosystem.

**Networking:** `OkHttp` + `Retrofit` (or OkHttp-only for a tiny surface) is enough; do not layer extra REST clients on top.

This keeps the tree small, security review surface smaller, and upgrades predictable.

## Reusability, DRY, and “check before you add”

These rules sit **on top of** feature mirroring ([module layout](#module-layout-suggested)) and the [**design system**](14-design-system.md). Goal: **one implementation** of cross-cutting behavior; **compose** small units instead of copy-pasting.

### Layer responsibilities

| Layer | Owns | Must not |
|-------|------|----------|
| **Presentation** | `ViewModel` + Compose; `UiState`, user actions | Call Retrofit/Room directly; duplicate HTTP or mapping logic |
| **Domain** | Use cases, pure models, `Result`/errors | Android framework types |
| **Data** | `Repository` implementations, DTOs, `Api` interfaces, mappers | Emit UI strings or `NavDirections` |

- **Single source of truth per aggregate:** e.g. current user flows through **one** `UserRepository` (or auth session repository) backed by API + local cache — not parallel “fetch user” helpers in each feature.
- **One repository per domain aggregate** (or cohesive slice), not one per screen. Screens share the repository via DI.
- **Use cases (interactors):** extract logic into a class when **two or more** ViewModels (or features) need the same rule. Keeps ViewModels thin and logic testable without UI.

### Before creating something new

1. Search **`:core:*`** and **`:feature:*`** for an existing composable, extension, wrapper, or use case (e.g. primary button, date format, `safeApiCall`, error mapper).
2. Prefer **extending** a shared component with parameters over forking a second “almost the same” composable.
3. If the iOS app uses a shared helper (e.g. [`APIErrorFormatter`](../../Pasabayan/Utilities/APIErrorFormatter.swift)), Android should have **one** mapped equivalent in `:core:domain-error` or `:core:network`, not per-screen string building.

### UI reuse (Compose)

- **Stateless composables:** data + callbacks in, no hidden `ViewModel` lookups. Reusable pieces live under `.../presentation/components/` or `:core:designsystem` when used across features.
- **Theme tokens only:** `MaterialTheme` / design-system extensions — **no** raw `Color(0xFF…)` or magic `dp` in feature code ([14-design-system.md](14-design-system.md)).
- **Strings:** `strings.xml` (or Compose `stringResource`) for product copy — not literals scattered in Kotlin (mirrors localization expectations).
- **Prefer composition over inheritance** for UI; use small building blocks.

### State and effects

- Prefer **`StateFlow`** / **`SharedFlow`** as the single observable for screen state and one-shot events — avoid parallel `LiveData` + Flow for the same data.
- Use **`sealed class` / `sealed interface`** for `UiState` and navigation effects instead of many independent booleans (`isLoading`, `isError`, …) where a single discriminated state is clearer.
- **Effects** (snackbar, navigation): `Channel` or `SharedFlow` from ViewModel — one pipeline, not ad hoc callbacks from deep children.

### Anti-patterns vs fixes

| Avoid | Prefer |
|-------|--------|
| Copy-pasted API calls in ViewModels | `Repository` + optional shared `suspend` helper in data layer with uniform error mapping |
| Duplicate error-to-message logic per screen | Central mapper aligned with [01-error-taxonomy.md](01-error-taxonomy.md) |
| New `*Repository` per screen | Shared repository + use cases |
| Hardcoded dimensions/colors | Theme / [14-design-system.md](14-design-system.md) |
| Two ways to load the same entity | Single repository API; cache policy in one place |

### Summary principles

- **One ViewModel per screen** (not per small widget); widgets receive state via parameters.
- **Repositories** abstract network and local storage; **UI never** talks to OkHttp or Room directly.
- **Use cases** for shared business rules (≥ 2 call sites).
- **DI** for all services and repositories — testability and a single object graph.
- **Check existing code** before adding parallel types or helpers.

## Module layout (suggested)

Gradle modules stay **Clean Architecture** (presentation / domain / data), but **names and boundaries should mirror** iOS so engineers can jump between repos by path.

### iOS layout (reference)

| iOS path | Role |
|----------|------|
| [`Pasabayan/Features/<Feature>/Models/`](../../Pasabayan/Features/) | `Codable` types, API-aligned structs |
| `Features/<Feature>/Services/` | Feature HTTP clients (`*APIService.swift`) |
| `Features/<Feature>/ViewModels/` | Observable state + business logic |
| `Features/<Feature>/Views/` | SwiftUI; subfolders like `Shipper/`, `Carrier/`, `Components/` where the product splits by role |
| [`Pasabayan/Services/`](../../Pasabayan/Services/) | Shared clients: [`APIService`](../../Pasabayan/Services/APIService.swift), [`APIConfiguration`](../../Pasabayan/Services/APIConfiguration.swift), catalog/home-city helpers |
| [`Pasabayan/Views/`](../../Pasabayan/Views/) | App shell, dashboard, shared components — **not** under `Features/` |
| [`Pasabayan/Models/`](../../Pasabayan/Models/) | Cross-feature models (e.g. shared DTOs) |
| [`Pasabayan/Utilities/`](../../Pasabayan/Utilities/) | Formatters, policies |

Use [**FEATURE-COVERAGE-MATRIX.md**](FEATURE-COVERAGE-MATRIX.md) as the **1:1 feature list**: each iOS `Features/<Name>/` folder should have a matching `:feature:<name>` (or single module with the same top-level package name).

### Android mirror (same roles, different layer names)

```
:app                          # NavHost, Application — like PasabayanApp + ContentView
:core:network                 # APIService rules; OkHttp + Retrofit only (no parallel HTTP stacks)
:core:domain-error            # sealed DomainError + HTTP → domain mappers
:core:designsystem            # theme tokens — parity with Views/Components/DesignSystem.swift
:feature:<feature>            # one Gradle module per iOS Features/<Feature>/ when feasible
```

Inside each `:feature:<feature>` package, **mirror iOS subfolders by responsibility** (not necessarily identical folder strings):

| iOS | Android (typical package paths) |
|-----|----------------------------------|
| `Models/` | `.../data/dto/` (wire types) + `.../domain/model/` (pure types), or `model/dto` + `model/domain` |
| `Services/*APIService.swift` | `.../data/api/<Feature>Api.kt` (Retrofit) + `.../data/repository/` |
| `ViewModels/` | `.../presentation/` (`*ViewModel`, MVI reducer, `UiState`) |
| `Views/` + `Views/Components/` | `.../presentation/<Screen>.kt`, `.../presentation/components/` |
| `Views/Shipper/` vs `Carrier/` | Same split: `presentation/shipper/`, `presentation/carrier/` when iOS does |

Naming: Kotlin packages use lowercase with underscores if needed (`route_activity` vs `routeactivity` — pick one convention for the Android repo and stick to it). Map **feature names** from iOS PascalCase folder names to Gradle module names (`Bookings` → `:feature:bookings`).

### Shared vs feature-owned (match iOS)

- **Do not** fold everything into `:app` if iOS keeps it in `Features/<X>/` — keep a **separate feature module** for parity and ownership.
- Shell / tab chrome that lives in iOS `Pasabayan/Views/Screens/Dashboard/` belongs in `:app` or a small `:core:shell` module, not inside `:feature:trips`, mirroring the fact it is not under `Features/` on iOS.

```
:app
:core:network      # OkHttp/Retrofit, interceptors, auth header policy
:core:domain-error # sealed DomainError + mappers from transport
:feature:auth:...
:feature:trips:...
...
```

Each feature module exposes:

- **Presentation**: Compose screens, `ViewModel` implementing reducer + `StateFlow<UiState>`, `Channel`/`SharedFlow` for effects.
- **Domain**: use cases returning `Result`; pure types; no Android framework.
- **Data**: DTOs (single JSON strategy — **Kotlinx Serialization or Moshi**, not both), `Api` interfaces, `RepositoryImpl`, mappers DTO ↔ domain.

## Parity notes vs iOS

| iOS pattern | Android direction |
|-------------|-------------------|
| Root: [`PasabayanApp.swift`](../../Pasabayan/PasabayanApp.swift), [`ContentView.swift`](../../Pasabayan/ContentView.swift) | Single-activity, `NavHost`, session gate from auth state |
| [`APIService`](../../Pasabayan/Services/APIService.swift) central client | One `HttpClient` + interceptors replicating auth skip rules for `/auth/*` except `/auth/me` |
| Combine publishers | `suspend` / `Flow` in data layer |
| [`NotificationCenter`](../../Pasabayan/AppDelegate.swift) for push/tab routing | Typed navigation events or deep-link `NavController` routes; document in feature specs |
| Mixed ViewModel styles (immutable `State` in auth vs mutable `@Published` elsewhere) | **Uniform** immutable `UiState` + explicit events for all features |
| Some calls bypass `APIService` (custom `URLSession`) | Prefer **one** pipeline; if a call must differ (multipart, timeouts), isolate in a dedicated `Api` with same error mapping |

## UI parity with iOS (mirror and align)

Implementation should **match iOS UX structure**: same role modes (shipper vs carrier), **tab order, labels, badges, and primary actions**, and equivalent modal vs full-screen presentation for the same flows. Use iOS as the reference implementation for layout hierarchy (sections, headers, lists, empty states), not a generic Material-only pattern that diverges from product.

| Area | iOS reference | Android alignment |
|------|----------------|-------------------|
| Design tokens | [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift) | **Single Compose theme** per [**14-design-system.md**](14-design-system.md); no ad-hoc `dp`/`Color` in features |
| Shipper shell | [`ShipperDashboardContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/ShipperDashboardContent.swift), [`ShipperHomeContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/ShipperHomeContent.swift) | Bottom navigation: same five tabs and indices; Explore root mirrors `ShipperHomeContent` hierarchy |
| Carrier shell | [`CarrierDashboardContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/CarrierDashboardContent.swift), [`CarrierHomeContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/CarrierHomeContent.swift) | Same tab parity as iOS carrier tab bar |
| Global chrome | Notification bell → `ComprehensiveNotificationsView` (see tab docs) | Same entry point (e.g. top app bar action) and sheet behavior |
| Strings | SwiftUI `Text` / localized strings in iOS | Same user-visible copy for fixed product strings; use same string keys or a shared glossary where the app is localized |
| Errors/alerts | [`APIErrorFormatter`](../../Pasabayan/Utilities/APIErrorFormatter.swift), [`ErrorAlertPolicy`](../../Pasabayan/Utilities/ErrorAlertPolicy.swift) | Same messages and when to show blocking vs non-blocking feedback |

**Platform allowance:** Use Jetpack Compose and Material 3 components, but **spacing, hierarchy, and information architecture** should still read as the same app as iOS. Document any unavoidable divergence (e.g. system back gesture vs iOS back button) in [13-ui-tab-explore.md](13-ui-tab-explore.md).

**Acceptance:** For each major screen, trace **iOS view file → Android route/composable** in the feature spec; add screenshot or UI test reference when the Android project exists.

## ROP application

1. **Data layer**: map HTTP status + body to `Result<Dto, TransportError>` then to `Result<DomainModel, DomainError>`.
2. **Use cases**: compose with `map`, `flatMap`, `recover` / `fold` on **`kotlin.Result`** — no thrown exceptions outward.
3. **Presentation**: reducer receives `Result` outcomes as sealed `Message` / `Result` events; set loading/error flags from **one** place.

## Broadcasting / WebSocket

- Base URL and paths: [`APIConfiguration`](../../Pasabayan/Services/APIConfiguration.swift) (`reverbWebSocketURL`, `broadcastingAuthURL` — note `/broadcasting/auth` is **without** `/api` prefix).
- Detail: [07-chat-broadcasting.md](07-chat-broadcasting.md).

## TDD checklist (this spec)

- [ ] Document module boundaries and naming in Android project README (when app repo exists).
- [ ] Unit-test auth header policy matches iOS rules (mock interceptor).
- [ ] Unit-test navigation/session graph for unauthenticated vs authenticated (later, in `:app`).
- [ ] Theme snapshot or unit tests: key tokens from [14-design-system.md](14-design-system.md) (spacing scale, nav title 15 sp).
- [ ] Use case / repository tests prove shared logic is not duplicated across ViewModels.
- [ ] No duplicate DTO→domain mappers for the same type — single mapper module or `*Mapper` object tested once.
- [ ] Dependency list reviewed: no redundant libraries; new deps have a recorded rationale ([Minimal third-party dependencies](#minimal-third-party-dependencies)).
