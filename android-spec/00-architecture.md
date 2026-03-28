# 00 — Android architecture (spec)

**Phase:** 0 (foundation). **Scope:** cross-cutting — not a single product feature. For **phase order and feature mapping**, see [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md).

## Goals

- **Parity** with iOS product behavior while improving **explicit** state and navigation (reduce reliance on implicit side effects).
- **UI parity**: Android screens, navigation, hierarchy, and visible copy should **mirror and align** with the current iOS app unless a spec explicitly documents an intentional platform difference. See [13-ui-tab-explore.md](13-ui-tab-explore.md).
- **Design system (mandatory):** All spacing, typography, colors, corner radii, card chrome, and layout constants **must follow** [**14-design-system.md**](14-design-system.md), derived from [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift). Feature composables **must** use **`:core:designsystem`** tokens and **`P*`** base components where they exist; **do not** ship parallel styling or bespoke controls in feature modules (see **14** — strict conformance and PR checklist). Extend the design system when a pattern is missing.
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
- **Theme tokens and base UI:** `MaterialTheme` (under **`PasabayanTheme`**) plus **`Pasabayan*`** token objects, **`P*`** composables, and **`ds*`** modifiers — **no** raw `Color(0xFF…)` or unexplained magic `dp`/`sp` in feature code; **no** one-off `Button`/`Card` styling that bypasses **`P*`** when a primitive exists ([14-design-system.md](14-design-system.md) — strict rules).
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

## API configuration constants (mirror `APIConfiguration.swift`)

From [`APIConfiguration.swift`](../../Pasabayan/Services/APIConfiguration.swift). Create a Kotlin `object ApiConfig` in `:core:network`.

### Base URLs

```
Production: https://api.pasabayan.com/api
Debug:      http://localhost:8001/api        (or USE_PRODUCTION_API flag)
```

Use `BuildConfig` fields set per `buildType` — do not hardcode URLs in source.

### Timeouts & retry

| Setting | Value |
|---------|-------|
| Connect timeout | **15 seconds** |
| Read timeout | **30 seconds** |
| Write timeout | **30 seconds** |
| Max retry attempts | **3** |
| Retry delay | **1 second** (between attempts) |

Implement retry via an OkHttp `Interceptor` that catches `IOException` and retries up to `maxRetries` with the configured delay. Do **not** retry non-idempotent requests (POST/PUT/DELETE) unless explicitly safe.

### WebSocket (Reverb/Pusher)

| Setting | Value |
|---------|-------|
| App key | `"pasabayan"` |
| WebSocket URL | `wss://{host}:{port}/app/{key}` (dynamic from `/config/broadcasting` or fallback from baseURL) |
| Broadcasting auth URL | `{scheme}://{host}[:port]/broadcasting/auth` — **no** `/api` prefix |
| Real-time enabled | `APIConfiguration.realtimeChatEnabled` (fallback to polling when false) |

### Stripe

| Setting | Value |
|---------|-------|
| Test publishable key | `pk_test_51SJGaH...` (fallback) |
| Live key | From `GET /payments/config` response |
| Merchant ID | `merchant.com.pasabayan` |

Detail: [06-payments-stripe.md](06-payments-stripe.md), [07-chat-broadcasting.md](07-chat-broadcasting.md).

## Base feature API service pattern

iOS [`BaseFeatureAPIService.swift`](../../Pasabayan/Services/BaseFeatureAPIService.swift) provides a base class that **all** feature API services inherit from:

```swift
class BaseFeatureAPIService: ObservableObject {
    let baseService: BaseAPIServiceProtocol
    func makeRequest<T, Body>(endpoint:, method:, body:, responseType:) → Publisher<T, APIError>
    func makeCachedRequest<T>(endpoint:, responseType:, forceRefresh:) → Publisher<T, APIError>
    func encodeBody<Body: Encodable>(_ body: Body) → Data?
}
```

### Android equivalent

Create a base pattern in `:core:network` — either an abstract class or extension functions on `Retrofit`:

- **Shared request helpers:** `suspend fun <T> safeApiCall(block: suspend () -> Response<T>): Result<T, DomainError>` — wraps Retrofit calls with error mapping via `ApiErrorMapper`.
- **Cached requests:** `suspend fun <T> cachedApiCall(endpoint, forceRefresh, block)` — check cache TTL first, return stale if valid, refresh in background.
- **Multipart helper:** `suspend fun <T> multipartApiCall(endpoint, parts: List<MultipartBody.Part>, textFields: Map<String, String>): Result<T, DomainError>` — builds `MultipartBody` with correct boundary and Content-Type.

Each `:feature:<name>` module defines a Retrofit `@interface XxxApi` and an `XxxRepository` that uses these shared helpers. Do **not** duplicate error mapping or cache logic per feature.

### Multipart requests

For image/file uploads (packages, profile avatar, support tickets, premium verification):

1. Use `MultipartBody.Part.createFormData(name, filename, requestBody)` for files
2. Use `MultipartBody.Part.createFormData(name, value)` for text fields
3. Set Content-Type to `multipart/form-data` (OkHttp sets boundary automatically)
4. Apply same `ApiErrorMapper` error handling as JSON requests
5. Field names must match iOS exactly — see per-spec multipart tables in [04](04-packages.md), [09](09-profile-carrier-consent.md), [10](10-verification.md), [12](12-legal-support-misc.md)

## Secure token storage

iOS uses [`KeychainHelper.swift`](../../Pasabayan/Services/KeychainHelper.swift) (iOS Keychain — persists across app reinstalls).

### Android equivalent

Use **`EncryptedSharedPreferences`** from `androidx.security:security-crypto` with **Android Keystore** (`MasterKey` with `AES256_GCM`):

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context, "pasabayan_secure_prefs", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**Rules:**
- Store auth token in encrypted prefs only — **never** plain `SharedPreferences`
- Token interface: `getToken(): String?`, `setToken(String)`, `clear()`
- Implement as `TokenStore` interface in `:core:session` for testability (swap with in-memory fake in tests)
- Note: unlike iOS Keychain, Android encrypted prefs do **not** persist across reinstalls

## Validation error parsing (Laravel)

iOS `APIService.swift` parses Laravel validation error responses (HTTP 400/422):

```json
{
  "message": "The given data was invalid.",
  "errors": {
    "pickup_city": ["The pickup city field is required."],
    "package_weight_kg": ["The package weight must be a number.", "The package weight must be at least 0.1."]
  }
}
```

### Android mapping

1. Attempt to decode response body as `ValidationErrorResponseDto` (field `errors: Map<String, List<String>>`)
2. If successful → `DomainError.ValidationError(message, fieldErrors)`
3. If decode fails → fall back to `DomainError.ServerError(message)` from plain error body
4. `ValidationError` should expose `formattedMessage`: join all field messages with newlines, prefixed by field label
5. Check for `carrier_onboarding_required` flag in 422 responses — takes precedence over validation errors

This is implemented in `ApiErrorMapper` in `:core:network`. See [01-error-taxonomy.md](01-error-taxonomy.md).

## Error alert policy (when to show errors)

From [`ErrorAlertPolicy.swift`](../../Pasabayan/Utilities/ErrorAlertPolicy.swift). Determines **when** to surface errors to the user based on context.

### Contexts

| Context | Rule |
|---------|------|
| **Foreground action** (user tapped button, submitted form) | Show **all** errors — user expects feedback |
| **Background refresh** (pull-to-refresh, auto-reload, periodic sync) | Show **only critical** errors — suppress transient failures |

### Critical errors (always show)

- `Unauthorized` / `Unauthenticated` — session expired, must re-login
- `PaymentRequired` — payment action needed
- `CarrierOnboardingRequired` — Stripe setup needed
- `ConsentRequired` — consent action needed

### Suppressed in background

- `NetworkError` — transient connectivity
- `DecodingError` — server format change (log, don't alert)
- `RateLimited` — temporary, will resolve
- `NotFound` / `TripNotFound` — stale data, refresh will fix
- `ServerError` — backend issue, not actionable by user
- `ValidationError` — should not occur in background

### Android implementation

Create `ErrorAlertPolicy` object in `:core:domain-error`:

```kotlin
object ErrorAlertPolicy {
    fun shouldPresentToUser(error: DomainError, context: ErrorContext): Boolean
}

enum class ErrorContext { FOREGROUND_ACTION, BACKGROUND_REFRESH }
```

## Error formatting (user-friendly messages)

From [`APIErrorFormatter.swift`](../../Pasabayan/Utilities/APIErrorFormatter.swift). Already partially implemented as `DomainError.userMessage()` in `:core:domain-error`.

Verify the Android `userMessage()` extension covers all cases with **identical copy** to iOS:

| DomainError | iOS message |
|------------|-------------|
| `InvalidUrl` | "Invalid request URL" |
| `NetworkError` | "Network error — please check your connection" |
| `DecodingError` | "Unable to process server response" |
| `Unauthorized` | "Unauthorized — Please log in again" |
| `NotFound(msg)` | Server message or "The requested resource was not found" |
| `TripNotFound(msg)` | Server message or "Trip not found" |
| `PaymentRequired(msg)` | Server message |
| `Conflict(msg, _)` | Server message or "A conflict occurred" |
| `ValidationError(msg, fields)` | Formatted field errors |
| `CarrierOnboardingRequired(msg)` | Server message |
| `ConsentRequired(_, msg)` | "Additional consent is required to continue" |
| `RateLimited` | "Too many requests — please try again later" |
| `ServerError(code, msg)` | Server message or "Server error ({code})" |
| `Unknown(msg)` | Message or "An unexpected error occurred" |

## Date formatting service

From [`DateFormatting.swift`](../../Pasabayan/Services/DateFormatting.swift) (230+ lines). Create a `DateFormatting` utility in `:core:network` or a new `:core:common` module.

### Required parsers (priority order — try each until one succeeds)

| Format | Example | Notes |
|--------|---------|-------|
| ISO8601 with fractional seconds | `2026-03-27T14:30:00.000Z` | Primary backend format |
| ISO8601 without fractional | `2026-03-27T14:30:00Z` | Fallback |
| Backend microseconds | `2026-03-27T14:30:00.123456Z` | `yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'` |
| Date only | `2026-03-27` | `yyyy-MM-dd` |

### Display formatters

| Name | Output example | Use |
|------|---------------|-----|
| `short` | "Mar 27, 2026" | Lists, cards |
| `medium` | "March 27, 2026" | Detail views |
| `long` | "Thursday, March 27, 2026" | Full display |
| `timeOnly` | "2:30 PM" | Chat messages |
| `shortDateTime` | "Mar 27, 2:30 PM" | Notifications |
| `relative` | "2 hours ago", "Yesterday" | Feed items |

### Critical: `combineLocalDateTime()`

iOS has a helper that combines a date string and time string while respecting **local timezone** (not UTC). This prevents timezone shift bugs when users enter pickup/delivery dates and times:

```kotlin
fun combineLocalDateTime(dateString: String, timeString: String): Instant {
    // Parse date and time in LOCAL timezone, then convert to UTC for API
}
```

## Centralized cache manager

From [`CacheManager.swift`](../../Pasabayan/Services/CacheManager.swift). Coordinates clearing all caches on logout or data reset.

### Android implementation

Create `CacheManager` in `:core:network`:

```kotlin
interface CacheManager {
    suspend fun clearAllCaches(): CacheClearResult
    suspend fun clearUserCaches()  // On logout
}
```

**What to clear:**
- OkHttp disk cache (`Cache.evictAll()`)
- HTTP response cache (per-endpoint invalidation)
- Room databases (if used)
- DataStore preferences (selective — preserve one-time flags)
- In-memory caches (notification counts, unread state)
- Temporary files (image cache, downloads)

**What to preserve on logout:**
- `hasCompletedOnboarding` — first-launch flag should survive logout
- `hasSeenPackageTutorial` — one-time education
- App settings (language, theme)

## Structured logging & diagnostics

From [`AppDiagnostics.swift`](../../Pasabayan/Utilities/AppDiagnostics.swift) and [`ProductionDiagnostics.swift`](../../Pasabayan/Utilities/ProductionDiagnostics.swift).

### Subsystem & categories

| Category | Subsystem | Events |
|----------|-----------|--------|
| `API_SERVICE` | `com.pasabayan.android` | `api.invalid_url`, `api.request_failed`, `api.decode_failure`, `api.timeout` |
| `AUTHENTICATION` | `com.pasabayan.android` | `auth.login_failed`, `auth.token_expired`, `auth.logout` |
| `CRASH_REPORTING` | `com.pasabayan.android` | `crash.detected`, `crash.upload_failed`, `crash.upload_success` |
| `NOTIFICATION` | `com.pasabayan.android` | `notification.received`, `notification.routing_failed`, `notification.token_registered` |

### Android implementation

Create `AppLogger` interface in `:core:network` (or `:core:common`):

```kotlin
interface AppLogger {
    fun debug(category: String, event: String, message: String)
    fun info(category: String, event: String, message: String)
    fun warning(category: String, event: String, message: String)
    fun error(category: String, event: String, message: String, throwable: Throwable? = null)
}
```

Back with `android.util.Log` in production. In debug builds, optionally add more verbose output.

### Production diagnostics

Only log **critical** errors to analytics/crash reporting:
- Auth failures (unauthorized, token expired)
- Payment failures
- Onboarding/consent errors
- Crash reports

Suppress transient errors (network timeouts, rate limiting, 404s) from production logging to avoid noise.

## Crash handling & reporting

From [`AppCrashHandler.swift`](../../Pasabayan/Services/AppCrashHandler.swift) and [`CrashReportingService.swift`](../../Pasabayan/Services/CrashReportingService.swift).

### Android implementation

Set up `Thread.setDefaultUncaughtExceptionHandler` in `PasabayanApplication`:

1. Catch uncaught exceptions
2. Build `CrashData`:
   - `timestamp` (ISO8601)
   - `appVersion` (from `BuildConfig.VERSION_NAME`)
   - `deviceModel` (`Build.MODEL`)
   - `osVersion` (`Build.VERSION.RELEASE`)
   - `userId`, `userRole` (from session, if available)
   - `errorType`, `errorMessage`
   - `stackTrace` (full stack as string)
   - `memoryUsage` (from `Runtime.getRuntime()`)
   - `batteryLevel` (from `BatteryManager`)
3. Persist crash data to disk (in case process dies before network call)
4. On next launch, check for pending crash report and POST to `/crash-reports`
5. Also consider integrating Firebase Crashlytics alongside custom reporting

## Accessibility testing IDs

From [`AccessibilityIdentifiers.swift`](../../Pasabayan/Accessibility/AccessibilityIdentifiers.swift) (250+ lines).

### Pattern

Use hierarchical naming: `feature.component.element`

```kotlin
object TestIds {
    object Auth {
        const val GOOGLE_SIGN_IN_BUTTON = "auth.login.google_button"
        const val FACEBOOK_SIGN_IN_BUTTON = "auth.login.facebook_button"
        const val ERROR_MESSAGE = "auth.login.error_message"
    }
    object Dashboard {
        const val TAB_BAR = "dashboard.tab_bar"
        const val EXPLORE_TAB = "dashboard.tab.explore"
        // ...
    }
}
```

Apply via `Modifier.testTag(TestIds.Auth.GOOGLE_SIGN_IN_BUTTON)` on all interactive composables. This enables reliable UI testing with `composeTestRule.onNodeWithTag(...)`.

## Keyboard & form helpers

From iOS extensions: [`View+FormScrollKeyboard.swift`](../../Pasabayan/Extensions/View+FormScrollKeyboard.swift), [`View+KeyboardToolbar.swift`](../../Pasabayan/Extensions/View+KeyboardToolbar.swift).

### Android equivalents

Create form utilities in `:core:designsystem`:

| iOS helper | Android equivalent |
|-----------|-------------------|
| `dismissKeyboardOnTap()` | `Modifier.clickable { focusManager.clearFocus() }` on parent container |
| `FormScrollWithStickyFooter` | `Scaffold` with `bottomBar` + `LazyColumn` content; footer stays visible above IME via `imePadding()` |
| `numericKeyboardToolbar(submit)` | Custom `TextField` with `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)` + `keyboardActions = KeyboardActions(onDone = { ... })` |
| `numericKeyboardToolbar(prev, next)` | `FocusRequester` chain — `Modifier.focusRequester()` with prev/next navigation on IME action |
| `onKeyboardVisibility` | `WindowInsets.ime.getBottom()` changes via `LaunchedEffect` |

## Shared generic DTOs

From [`SharedModels.swift`](../../Pasabayan/Models/SharedModels.swift) and [`GeneralModels.swift`](../../Pasabayan/Models/GeneralModels.swift). Place in `:core:network` as shared response wrappers.

### Required DTOs

```kotlin
@Serializable
data class PaginatedResponse<T>(
    @SerialName("current_page") val currentPage: Int,
    val data: List<T>,
    @SerialName("first_page_url") val firstPageUrl: String? = null,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("next_page_url") val nextPageUrl: String? = null,
    val total: Int
)

@Serializable
data class DataResponse<T>(val data: T)

@Serializable
data class Coordinates(val latitude: Double, val longitude: Double)

@Serializable
object EmptyResponse  // For endpoints with no body
```

Use these across all features instead of per-feature wrapper types.

## Broadcasting / WebSocket

- Base URL and paths: [`APIConfiguration`](../../Pasabayan/Services/APIConfiguration.swift) (`reverbWebSocketURL`, `broadcastingAuthURL` — note `/broadcasting/auth` is **without** `/api` prefix).
- Detail: [07-chat-broadcasting.md](07-chat-broadcasting.md).

## HTTP cache policy (iOS parity)

iOS [`HTTPCacheService.swift`](../../Pasabayan/Services/HTTPCacheService.swift) uses a two-tier cache (memory + disk) with stale-while-revalidate. Android should implement equivalent caching via OkHttp `Cache` or a custom repository-level cache.

### TTL by endpoint

| Endpoint pattern | TTL | Stale-while-revalidate |
|-----------------|-----|------------------------|
| `/profile` | 1 hour | Yes |
| `/auth/me` | 5 min | Yes |
| `/carrier/profile` | 1 hour | Yes |
| `/carrier/stats` | 5 min | Yes |
| `/trips`, `/packages` | 5 min | Yes |
| `/packages/available`, `/trips/available` | 1 min | Yes |
| `/matches` | 1 min | Yes |
| `/routes/popular*`, `/locations/catalog` | 24 hours | Yes |
| `/chat/*` | **No cache** | — |
| `/notifications*` | 1 min | Yes |

### Cache rules

- Only cache **GET** requests
- Skip auth endpoints except `/auth/me`
- Skip all chat endpoints
- Memory cache: up to 100 entries, 50 MB limit
- Disk cache: stored in app cache directory (`HTTPCache/`)

### Invalidation methods (mirror iOS)

| Method | Clears |
|--------|--------|
| `invalidateUserCaches()` | `/profile`, `/auth/me`, `/carrier/profile`, `/carrier/stats` |
| `invalidateTripCaches()` | `/trips*` |
| `invalidatePackageCaches()` | `/packages*` |
| `invalidateMatchCaches()` | `/matches*` |

Call these after mutations (e.g. after creating a trip, invalidate trip caches).

## Carrier location service (GPS tracking)

From [`CarrierLocationService.swift`](../../Pasabayan/Services/CarrierLocationService.swift). Active during delivery.

| Setting | Value |
|---------|-------|
| Update interval | **30 seconds** |
| Distance filter | **50 meters** (only update when moved 50+ m) |
| Accuracy | Best available (`kCLLocationAccuracyBest` / `PRIORITY_HIGH_ACCURACY`) |
| Background updates | Enabled (Android: foreground service with notification) |
| API call | `POST /matches/{matchId}/location` with `{latitude, longitude, accuracy}` |

On 403 "consent" error: call `PUT /profile/consent-preferences` with `{location_tracking: true}`, then retry.

Failed uploads: queue most recent and retry on network recovery.

## Location service (general)

From [`LocationService.swift`](../../Pasabayan/Services/LocationService.swift).

- Accuracy: best available
- Distance filter: 10 meters
- Geocoding: address → coordinates and reverse (use Android Geocoder)
- Delivery fee calc: base `1.23 CAD` + `0.37 CAD/km` (for distance estimates)

## TDD checklist (this spec)

- [ ] Document module boundaries and naming in Android project README (when app repo exists).
- [ ] Unit-test auth header policy matches iOS rules (mock interceptor).
- [ ] Unit-test navigation/session graph for unauthenticated vs authenticated (later, in `:app`).
- [ ] Theme snapshot or unit tests: key tokens from [14-design-system.md](14-design-system.md) (spacing scale, nav title 15 sp).
- [ ] Use case / repository tests prove shared logic is not duplicated across ViewModels.
- [ ] No duplicate DTO→domain mappers for the same type — single mapper module or `*Mapper` object tested once.
- [ ] Dependency list reviewed: no redundant libraries; new deps have a recorded rationale ([Minimal third-party dependencies](#minimal-third-party-dependencies)).
- [ ] `ErrorAlertPolicy` tests: foreground shows all errors, background suppresses transient.
- [ ] `userMessage()` extension covers all `DomainError` variants with iOS-parity copy.
- [ ] Date parsing: ISO8601 with and without fractional seconds, backend microsecond format, date-only.
- [ ] `combineLocalDateTime()` preserves local timezone (no UTC shift bug).
- [ ] Validation error parsing: decode Laravel `errors` map; `formattedMessage` includes field labels.
- [ ] Secure token storage: verify EncryptedSharedPreferences encrypts/decrypts correctly.
- [ ] `CacheManager.clearAllCaches()` clears HTTP + disk + memory but preserves one-time flags.
- [ ] Crash handler: verify `CrashData` collects device info, stack trace, user context.
- [ ] `PaginatedResponse<T>` and `DataResponse<T>` decode correctly with real backend JSON.
- [ ] Accessibility: all interactive composables have `testTag()` for UI testing.
