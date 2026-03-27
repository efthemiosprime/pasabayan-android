# 16 — Empty project: initialize, scaffold modules, dependencies

**Phase:** 0 (foundation). **Use when:** you have an **empty** Android Studio project (or “Empty Activity” template) and need to align it with [00-architecture.md](00-architecture.md), [14-design-system.md](14-design-system.md), and [Minimal third-party dependencies](00-architecture.md#minimal-third-party-dependencies).

This doc describes **what to create and wire** — not a substitute for Android Studio’s wizard. **Pin versions** using the Android Studio / AGP release you use; the tables below list **artifact IDs and roles**, not fixed version numbers.

---

## 1. Prerequisites

| Tool | Notes |
|------|--------|
| **Android Studio** | Current stable; use its embedded JDK or **JDK 17+** |
| **Kotlin** | Match AGP/Kotlin compatibility in the [Kotlin releases](https://kotlinlang.org/docs/releases.html) matrix |
| **Min SDK** | Choose a single floor (e.g. **24** or **26**) for the whole app; document in `build.gradle.kts` |
| **Package name** | Stable application id (e.g. `com.pasabayan.app`) — hard to change later |

---

## 2. Recommended project shape

Start from **Empty Activity** with **Jetpack Compose** enabled, then **add modules** so the app matches the mirror in [00-architecture.md](00-architecture.md).

```
settings.gradle.kts          # include all modules
gradle/libs.versions.toml    # Version Catalog (single place for versions)
app/
core/
  network/                   # OkHttp, Retrofit, auth interceptor, JSON
  domain-error/              # DomainError, HTTP → domain mappers
  designsystem/              # Theme, Material 3, tokens from 14-design-system
feature/<name>/              # optional: add as you implement phases (auth, trips, …)
```

**Phase 0 minimum:** `:app` + `:core:network` + `:core:domain-error` + `:core:designsystem`. Feature modules can appear as empty shells or be added when Phase 1+ starts.

---

## 3. Gradle: Version Catalog (`gradle/libs.versions.toml`)

Use **one catalog** for all versions. Group by concern:

| Alias (example) | Purpose |
|-------------------|---------|
| `agp`, `kotlin` | Android Gradle Plugin, Kotlin |
| `compose-bom` | Compose BOM — **import Compose libraries without per-artifact versions** |
| `kotlinx-coroutines` | `kotlinx-coroutines-android` (and `test` for unit tests) |
| `hilt`, `hilt-compiler` | Hilt + KSP (preferred) or KAPT |
| `retrofit`, `okhttp`, `okhttp-logging` | Single HTTP stack |
| `kotlinx-serialization-json` | JSON (if you chose Serialization — see [00-architecture.md](00-architecture.md)) |
| `retrofit-kotlinx-serialization` | Converter: Retrofit ↔ kotlinx.serialization |
| `androidx-navigation-compose`, `lifecycle-viewmodel-compose`, `activity-compose` | Compose integration |

**Plugins block** (root `build.gradle.kts` or `plugins { }` in `build.gradle.kts`):

- `com.android.application` / `com.android.library` (per module)
- `org.jetbrains.kotlin.android`
- `org.jetbrains.kotlin.plugin.compose` (Compose Compiler plugin — Kotlin 2.0+)
- `org.jetbrains.kotlin.plugin.serialization` (if using kotlinx.serialization)
- `com.google.dagger.hilt.android` + `com.google.devtools.ksp` (or `kotlin-kapt` for Hilt)

Apply **Hilt** to `:app` and any module that contains `@AndroidEntryPoint` / `@Module`; use `ksp("com.google.dagger:hilt-compiler")` on those modules.

---

## 4. Modules to add (Android Studio)

### 4.1 `:app`

- **Plugins:** `com.android.application`, Kotlin, Compose, Hilt, Serialization (if used).
- **Dependencies (conceptual):**
  - Compose BOM + `compose-ui`, `compose-runtime`, `material3`, `ui-tooling-preview`
  - `navigation-compose`
  - `hilt-android` + `hilt-navigation-compose`
  - `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`
  - **Project modules:** `implementation(project(":core:designsystem"))`, `implementation(project(":core:network"))`, `implementation(project(":core:domain-error"))`, and feature modules as they exist.
- **`Application` class:** `@HiltAndroidApp` in `src/main/AndroidManifest.xml` `android:name`.
- **Manifest:** `INTERNET` permission; cleartext only if debug builds need it (document).

### 4.2 `:core:designsystem`

- **Type:** Android library, **Compose** enabled.
- **Contents:** `Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt` per [14-design-system.md](14-design-system.md); no feature-specific screens.
- **Dependencies:** Compose BOM, Material3; optional `material-icons-extended` if icons are needed.

### 4.3 `:core:domain-error`

- **Type:** Kotlin JVM or Android library **without** Compose (pure Kotlin).
- **Contents:** `sealed class DomainError`, mappers from transport/HTTP aligned with [01-error-taxonomy.md](01-error-taxonomy.md).
- **Dependencies:** Minimal; Kotlin stdlib only if possible.

### 4.4 `:core:network`

- **Type:** Android library (needs `Context` only if you read BuildConfig; otherwise JVM is possible — Android library is fine).
- **Contents:** `OkHttpClient` + interceptors (Bearer token, logging in `debug`), `Retrofit` factory, JSON `ContentType`, optional `Api` interfaces used by multiple features.
- **Dependencies:**
  - `retrofit`, `okhttp`, `logging-interceptor` (debug)
  - `kotlinx-serialization-json` + converter **or** Moshi — **one** JSON stack ([00-architecture.md](00-architecture.md))
  - Hilt module binding `OkHttpClient` / `Retrofit` if DI is centralized here
- **ProGuard:** keep rules for Retrofit/OkHttp if minify is on (see R8 defaults).

---

## 5. Dependency checklist (foundation)

| Need | Artifact / setup |
|------|------------------|
| UI | Compose BOM + Material3 |
| Navigation | `navigation-compose` |
| Async | `kotlinx-coroutines-android` |
| DI | **Hilt** + KSP |
| HTTP | **Retrofit + OkHttp** only |
| JSON | **Kotlinx Serialization** *or* **Moshi** (codegen) — one |
| VM | `lifecycle-viewmodel-ktx`, `lifecycle-viewmodel-compose` |
| Tests (Phase 0) | `junit`, `kotlinx-coroutines-test`, `truth` or `assertk` (optional; keep test deps minimal) |

**Add later** (when a feature needs them — do not pull in Phase 0 for “just in case”):

- **Room** (local cache), **DataStore** (preferences)
- **Coil** (images)
- **Firebase** (FCM) — per [08-notifications-device-tokens.md](08-notifications-device-tokens.md)
- **Stripe** Android SDK — per [06-payments-stripe.md](06-payments-stripe.md)

---

## 6. Scaffold order (concrete steps)

1. **Create** project (Empty Activity, Compose) or open existing empty repo.
2. **Add** `gradle/libs.versions.toml` and move all dependency versions into it.
3. **Create** `:core:designsystem`, `:core:domain-error`, `:core:network` as Android/Kotlin libraries; **include** them in `settings.gradle.kts`.
4. **Wire** `:app` → core modules with `implementation(project(...))`.
5. **Apply** Hilt: `@HiltAndroidApp`, `Application` in manifest, `build.gradle` plugin + KSP.
6. **Implement** a minimal `PasabayanTheme` in `:core:designsystem` using tokens from [14-design-system.md](14-design-system.md).
7. **Implement** `Retrofit` + `OkHttp` + JSON in `:core:network` with a **test** base URL or mock until Phase 1.
8. **Run** `assembleDebug` and a minimal Compose screen that uses the theme from `:core:designsystem`.

---

## 7. Feature modules (`:feature:*`)

When starting Phase 1+, add `:feature:auth`, `:feature:trips`, etc.:

- **Plugins:** `com.android.library`, Kotlin, Compose (if UI), Hilt (if entry points / modules here).
- **Dependencies:** `implementation(project(":core:designsystem"))`, `implementation(project(":core:network"))`, `implementation(project(":core:domain-error"))`, plus `api`/`implementation` only as needed.

---

## 8. What not to do at bootstrap

- Do **not** add Ktor Client, second HTTP clients, or Arrow — [00-architecture.md](00-architecture.md).
- Do **not** add Gson + kotlinx.serialization for the same DTOs.
- Do **not** copy a large `dependencies { }` block from a sample — add only what Phase 0 needs, then grow per phase.

---

## Related

- [00-architecture.md](00-architecture.md) — modules, MVVM, lean deps  
- [14-design-system.md](14-design-system.md) — theme tokens  
- [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) — workflow  
- [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) — Phase 0 exit gate  

---

## TDD / verification (bootstrap)

- [ ] `./gradlew assembleDebug` succeeds on CI/local.
- [ ] `Application` is Hilt-enabled; one smoke `@Composable` uses theme from `:core:designsystem`.
- [ ] `:core:network` exposes a `Retrofit` (or `OkHttpClient`) that can be replaced in tests with a mock server.
