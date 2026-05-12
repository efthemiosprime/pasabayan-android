# Pasabayan Android — guidance for Claude / AI assistants

## Project

Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit + OkHttp (single HTTP stack) + kotlinx-serialization (single JSON strategy — do not mix with Moshi/Gson). Error handling: `kotlin.Result` + sealed domain errors. Parity target: iOS app at `pasabayan-ios` (behavior and UI). **Written specs live in this repo under `android-spec/`** — treat them as canonical for Android work.

## Source of truth (read first)

- **Product/API behavior:** mirror the iOS app at `pasabayan-ios` (`Features/*`, `Services/*`, `Views/*`). iOS is the **reference implementation** for endpoints, flows, and copy unless a spec documents an exception.
- **Written specs:** `android-spec/` is canonical for Android work — start with `IMPLEMENTATION-GUIDE.md`, then phases in `PHASES-AND-FEATURES.md`.
- **Coverage check:** `android-spec/FEATURE-COVERAGE-MATRIX.md` maps every iOS `Features/*` folder to a spec and phase — use it so nothing is orphaned.
- **Tabs and shell:** `android-spec/15-platform-and-tab-index.md` + iOS `docs/tabs/` (shipper/carrier tab indices 0–4, shared Messages/Profile).
- **Path note:** Specs may link to `../../Pasabayan/...` as if iOS repo sits next to `android-spec/`. Resolve against `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/`.

| Doc | Purpose |
|-----|---------|
| `android-spec/IMPLEMENTATION-GUIDE.md` | How to implement by feature |
| `android-spec/PHASES-AND-FEATURES.md` | Phases 0–7 and exit gates |
| `android-spec/IMPLEMENTATION-STATUS.md` | What’s done / in progress |
| `android-spec/FEATURE-COVERAGE-MATRIX.md` | iOS feature → spec → phase mapping |
| `android-spec/00-architecture.md` | Stack, modules, DRY |
| `android-spec/14-design-system.md` | **Tokens, `P*` components, strict UI rules, PR checklist** |
| `android-spec/15-platform-and-tab-index.md` | Platform shell, tab indices |

Cursor rule: `.cursor/rules/pasabayan-android-spec-context.mdc`.

## TDD (default)

- Prefer **tests before or with** each slice: **red → green → refactor**.
- **DTOs / API / mappers:** JVM unit tests.
- **Network / errors:** JVM tests (see `:core:network` examples).
- **Repositories / session:** JVM tests with fakes or coroutine tests when practical.
- **ViewModels:** tests for important state transitions.
- **Design system (`:core:designsystem`):** JVM tests for token invariants; `src/androidTest` + Compose for `PasabayanTheme` and `P*`.
- **Bug fixes:** add a regression test when feasible.

Cursor rule: `.cursor/rules/tdd-and-ui-conformance.mdc`.

## App module layout (feature-first, iOS parity)

The **`app`** module uses **`com.efthemiosprime.pasabayan`** with a **feature-first** tree (aligned with iOS **`Pasabayan/Features/<Name>/`**):

- **`features/<feature>/model`** — types, preference interfaces, feature-only models
- **`features/<feature>/viewmodel`** — `ViewModel` + tightly coupled UI state
- **`features/<feature>/ui`** — Compose screens and routes
- **`features/<feature>/services`** — feature-owned repos, prefs impl, Hilt `@Module`s, **and** feature-scoped SDK glue (e.g. Google/Facebook sign-in under **`features/auth/services`**)
- **`features/<feature>/components`** — Compose building blocks **used only by that feature**

Other locations:
- **`shared/root/`** — app entry (`AppEntryContent`, `RootViewModel`). **`shared/error/`** — app-layer error presentation (e.g. `DomainErrorLocalized`).
- **Top-level `services/`** — only for **multi-feature / infra** (e.g. FCM). Feature-only services go under `features/<feature>/services/`.
- **Module root** (`com.efthemiosprime.pasabayan`) — `MainActivity`, `PasabayanApplication`, and similar application entry types.
- **`:core:*` Gradle modules** — `core:network`, `core:session`, `core:designsystem`, `core:domain-error` per `00-architecture.md`. Do not duplicate DTO stacks from `core` into `app`.
- **Do not** use a root **`components/`** package unless the UI is **shared across features**; prefer **`:core:designsystem`** for real design-system primitives.

**Full rules:** `.cursor/rules/app-feature-package-layout.mdc`.

## Lean files and thin classes (strict)

- **Single responsibility per file.** One class/composable/interface per file. DTOs, domain models, and mappers in separate files.
- **Small files:** aim < 200 lines; split at 400. Functions < 30 lines. Classes < 150 lines of logic. Function parameters ≤ 5 (group into data class when more).
- **Thin ViewModels:** delegate to repositories and use cases — no inline API calls, mapping, or business logic. One ViewModel per screen. Extract a use case when ≥ 2 call sites share logic. Use cases: one public `operator fun invoke()`, return `Result<T>`, no held state.
- **Repositories:** one per domain aggregate, thin wrappers (call API → map DTO → return `Result`). Split at ~10 public methods.
- **Retrofit API interfaces:** one `*Api` per feature. Split at ~15 endpoints.
- **Hilt modules:** one `@Module` per feature. Split at > 10 bindings.
- **No god objects:** avoid `Utils.kt`, `Helpers.kt`, `Manager.kt` catch-alls. Name by responsibility (`DateParser`, `PriceFormatter`).
- **Compose screens** delegate to smaller composable components in `components/`; extract repeated blocks (≥ 2 uses or ≥ 30 lines). Previews live in the same file as the composable.
- **Separate for testability:** if testing a piece of logic requires unrelated dependencies, extract it into its own file/class. Use cases, mappers, components, and DTOs should each be independently testable.

Cursor rule: `.cursor/rules/lean-classes-separation.mdc`. Architecture: `android-spec/00-architecture.md` (Lean files and thin classes).

## Design system and UI (strict)

- Root UI: **`PasabayanTheme`**.
- Spacing, radius, typography, borders, layout, motion: **`Pasabayan*`** token objects in `:core:designsystem`.
- Prefer **`PButton`**, **`PCard`**, **`POutlinedTextField`**, **`PScaffold`** / **`PSnackbar`**, **`PModalBottomSheet`**, **`PTopBar`**, **`PDivider`**, **`PCircularProgress`**, and future **`P*`** primitives; use **`ds*`** modifiers for repeated patterns.
- **Do not** add ad-hoc colors, `dp`/`sp`, or bespoke `Button`/`Card` styling in feature modules. **Extend `:core:designsystem` + update `14-design-system.md`** when something is missing. Document rare exceptions in the feature spec + PR (per **14**).
- **Cross-role parity:** when the same content exists for two roles (e.g. shipper vs carrier sheets), use the same `P*` building blocks and the same section composables so the views feel identical. Differences are limited to actions and data — not chrome, spacing, or typography.
- **Light + dark theme (mandatory):** All UI must work in both light and dark mode. Use `MaterialTheme.colorScheme.*` or `PasabayanColors.*` — never `Color.White`, `Color.Black`, or `Color(0xFF...)` directly. Preview composables with `@Preview(uiMode = UI_MODE_NIGHT_YES)`. `PasabayanTheme` must define both `lightColorScheme` and `darkColorScheme`.
- **Compose previews (mandatory):** Any new or changed composable under **`features/*/ui/`**, **`features/*/components/`**, or **`:core:designsystem`** must include **`@Preview`** composables: wrap in **`PasabayanTheme`**, and include **light + dark** previews for non-trivial UI. Use fake/static state for screens; do not depend on Hilt/`ViewModel` in previews.

Cursor rules: `.cursor/rules/design-system-enforcement.mdc` (component + token catalog, workflow when a pattern is missing), `.cursor/rules/detail-sheet-scaffold-conformance.mdc` (sheets/modals), `.cursor/rules/compose-ui-previews.mdc` (preview requirements).

## Localization (strict — always apply)

- **Never hardcode user-visible strings.** Use `stringResource(R.string.key)` in Compose, `context.getString(R.string.key)` in ViewModels.
- **Both languages from the start:** add every new string to `res/values/strings_<feature>.xml` (English) **and** `res/values-fr/strings_<feature>.xml` (French). If French translation is unknown, duplicate the English value with `<!-- TODO: translate -->`.
- **Feature-organized files:** `strings_auth.xml`, `strings_bookings.xml`, `strings_common.xml`, etc. (mirrors iOS xcstrings catalogs).
- **Key naming:** `<feature>_<section>_<element>` — e.g. `bookings_list_title`, `common_buttons_ok`.
- **Enum display names** in `strings_enums.xml` — never `"Pending"` or `"Delivered"` as literals.
- **Error messages:** `DomainError.userMessage()` must resolve `R.string.*` (pass Context or StringProvider).
- **Format strings:** use Android positional args (`%1$s`, `%2$d`) — e.g. `<string name="bookings_empty_filtered">No %1$s deliveries found</string>`.
- **HTML articles** in `assets/articles/` follow `-fr.html` suffix convention per `18-localization.md`.
- **Catching violations:** flag any `Text("...")` with a literal English string, any `"Error"`/`"OK"`/`"Cancel"` used directly, any `Toast`/`Snackbar` with hardcoded text, any missing `values-fr/` counterpart.
- Full spec: `android-spec/18-localization.md`. Cursor rule: `.cursor/rules/localization-enforcement.mdc`.

## Phases and commits

- Work in **small slices** inside the active phase; **do not** jump phases without user confirmation.
- **Slice by layer** in order: (1) Contract/DTO/API + tests → (2) Repository/session + tests → (3) ViewModel + tests → (4) UI + design tokens + previews.
- After each slice: **stop** — do not start the next slice. Suggest **conventional commit** message(s), user commits manually unless they ask you to commit.
- After each full phase: same stop + confirm exit gate from `PHASES-AND-FEATURES.md`. Do not begin next phase until user confirms.
- Update **`android-spec/IMPLEMENTATION-STATUS.md`** when a gate or milestone moves.

### Commit message format

```
type(scope): short imperative description
```

| Type | Use for |
|------|---------|
| `feat` | New capability or user-visible behavior |
| `fix` | Bug fix |
| `chore` | Maintenance, tooling, deps (no feature change) |
| `perf` | Performance-only |
| `ci` | CI / automation |
| `test` | Tests only |
| `docs` | Documentation / specs |
| `refactor` | Behavior-preserving structure change |
| `revert` | Revert a prior commit |
| `build` | Gradle, packaging, codegen |
| `ui` | Compose/UI behavior or layout (product-facing) |
| `style` | Formatting, naming-only, non-functional polish |

Examples: `feat(auth): add token storage and /auth/me session load`, `test(network): add auth header policy unit tests`, `docs(android-spec): sync api-contract-matrix for trips endpoints`.

Rules: `.cursor/rules/phase-commit-workflow.mdc`, `.cursor/rules/pasabayan-android-spec-context.mdc`, `.cursor/rules/app-feature-package-layout.mdc`.

## Android gotchas (strict — always apply when writing code)

Avoid common Android bug patterns. Full reference: `.cursor/rules/android-gotchas.mdc`.

- **Memory leaks:** Never hold `Activity`/`Fragment`/`Context` in singletons or static fields. Use `viewModelScope`/`lifecycleScope`, never `GlobalScope`. Unregister listeners in matching lifecycle callbacks.
- **Null safety:** No `!!` in production code. DTOs use nullable types for any field the server may omit. Never assume `Bundle` extras are non-null.
- **Main thread:** Never perform network or disk I/O on `Dispatchers.Main`. Use `Dispatchers.IO` or `withContext(Dispatchers.IO)`. Room queries, file reads, SharedPreferences — all off main thread.
- **State atomicity:** Use `StateFlow.update {}` (atomic), not read-then-write. For complex shared state use `Mutex` or single-writer principle.
- **Lifecycle:** Use `repeatOnLifecycle`/`flowWithLifecycle` for Flow collection. Never access destroyed Activity/Fragment. In Compose, side effects in `LaunchedEffect`/`SideEffect`, not during composition.
- **Async ordering:** Never assume coroutine/callback completion order. Use `collectLatest`/`flatMapLatest` to cancel stale requests. Do not depend on `onCreate` coroutine completing before `onResume`.
- **Click reentrancy:** Debounce click listeners — double-tap can trigger navigation or API calls twice.
- **Currency:** Use `BigDecimal` or `Long` cents for money arithmetic. Never `Float`.
- **Integer overflow:** Bitmap size calculations and timestamp arithmetic use `Long`, not `Int`.

## Build / IDE

- **Gradle, Run, JDK, emulator:** use **Android Studio** as source of truth if terminal Gradle fails (e.g. `JAVA_HOME` / `jlink`). Cursor is for editing and AI-assisted coding.
- **Tests and builds:** Prefer **you** running Gradle locally. Assistants should **provide copy-paste commands** (e.g. `./gradlew :core:session:testDebugUnitTest`, `./gradlew :app:compileDebugKotlin`, `./gradlew :app:assembleDebug`) rather than relying on Cursor to execute Gradle for authoritative results.

### jlink / JAVA_HOME troubleshooting

AGP 8.8+ requires `jlink` for `compileDebugJavaWithJavac`. Some editors (Cursor, VS Code) spawn terminals with a stripped JRE that lacks `jlink`, causing:

```
jlink executable .../bin/jlink does not exist.
```

**Diagnosis:** `$JAVA_HOME/bin/jlink` must exist. SDKMAN Java 21.0.1-tem at `~/.sdkman/candidates/java/current` has it. Android Studio JBR at `/Applications/Android Studio.app/Contents/jbr/Contents/Home` also has it.

**Fix:** If the error appears after JAVA_HOME is already correct (e.g. via SDKMAN), the Gradle daemon is caching the old bad JRE. Run:

```bash
./gradlew --stop          # kill stale daemon
./gradlew assembleDebug   # restarts with correct JAVA_HOME
```

If JAVA_HOME itself is wrong, set it explicitly:

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
# or
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

`settings.gradle.kts` has a `syncJavaHomeForAgpJlink()` workaround that tries to fix this at build time, but it cannot override a daemon that started with a bad JRE.

Cursor rule: `.cursor/rules/gradle-user-runs-builds.mdc`.

## iOS reference path (parity)

`/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios` — `Pasabayan/Features/*`, `Services/*`, `Views/*`, `docs/`.
