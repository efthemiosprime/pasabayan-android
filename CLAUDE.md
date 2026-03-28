# Pasabayan Android — guidance for Claude / AI assistants

## Project

Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit + OkHttp + kotlinx-serialization. Parity target: iOS app at `pasabayan-ios` (behavior and UI). **Written specs live in this repo under `android-spec/`** — treat them as canonical for Android work.

## Specs (read first)

| Doc | Purpose |
|-----|---------|
| `android-spec/IMPLEMENTATION-GUIDE.md` | How to implement by feature |
| `android-spec/PHASES-AND-FEATURES.md` | Phases 0–7 and exit gates |
| `android-spec/IMPLEMENTATION-STATUS.md` | What’s done / in progress |
| `android-spec/00-architecture.md` | Stack, modules, DRY |
| `android-spec/14-design-system.md` | **Tokens, `P*` components, strict UI rules, PR checklist** |

## TDD (default)

- Prefer **tests before or with** each slice: **red → green → refactor**.
- **DTOs / API / mappers:** JVM unit tests.
- **Network / errors:** JVM tests (see `:core:network` examples).
- **Repositories / session:** JVM tests with fakes or coroutine tests when practical.
- **ViewModels:** tests for important state transitions.
- **Design system (`:core:designsystem`):** JVM tests for token invariants; `src/androidTest` + Compose for `PasabayanTheme` and `P*`.
- **Bug fixes:** add a regression test when feasible.

Cursor rule: `.cursor/rules/tdd-and-ui-conformance.mdc`.

## Design system and UI (strict)

- Root UI: **`PasabayanTheme`**.
- Spacing, radius, typography, borders, layout, motion: **`Pasabayan*`** token objects in `:core:designsystem`.
- Prefer **`PButton`**, **`PCard`**, **`POutlinedTextField`**, **`PScaffold`** / **`PSnackbar`**, **`PModalBottomSheet`**, **`PTopBar`**, **`PDivider`**, **`PCircularProgress`**, and future **`P*`** primitives; use **`ds*`** modifiers for repeated patterns.
- **Do not** add ad-hoc colors, `dp`/`sp`, or bespoke `Button`/`Card` styling in feature modules. **Extend `:core:designsystem` + update `14-design-system.md`** when something is missing. Document rare exceptions in the feature spec + PR (per **14**).
- **Light + dark theme (mandatory):** All UI must work in both light and dark mode. Use `MaterialTheme.colorScheme.*` or `PasabayanColors.*` — never `Color.White`, `Color.Black`, or `Color(0xFF...)` directly. Preview composables with `@Preview(uiMode = UI_MODE_NIGHT_YES)`. `PasabayanTheme` must define both `lightColorScheme` and `darkColorScheme`.

## Localization (strict — always apply)

- **Never hardcode user-visible strings.** Use `stringResource(R.string.key)` in Compose, `context.getString(R.string.key)` in ViewModels.
- **Both languages from the start:** add every new string to `res/values/strings_<feature>.xml` (English) **and** `res/values-fr/strings_<feature>.xml` (French). If French translation is unknown, duplicate the English value with `<!-- TODO: translate -->`.
- **Feature-organized files:** `strings_auth.xml`, `strings_bookings.xml`, `strings_common.xml`, etc. (mirrors iOS xcstrings catalogs).
- **Key naming:** `<feature>_<section>_<element>` — e.g. `bookings_list_title`, `common_buttons_ok`.
- **Enum display names** in `strings_enums.xml` — never `"Pending"` or `"Delivered"` as literals.
- **Error messages:** `DomainError.userMessage()` must resolve `R.string.*` (pass Context or StringProvider).
- Full spec: `android-spec/18-localization.md`. Cursor rule: `.cursor/rules/localization-enforcement.mdc`.

## Phases and commits

- Work in **small slices** inside the active phase; **do not** jump phases without user confirmation.
- After each slice: **stop**, suggest **conventional commit** message(s), user commits manually unless they ask you to commit.
- Update **`android-spec/IMPLEMENTATION-STATUS.md`** when a gate or milestone moves.

Rules: `.cursor/rules/phase-commit-workflow.mdc`, `.cursor/rules/pasabayan-android-spec-context.mdc`.

## Build / IDE

- **Gradle, Run, JDK, emulator:** use **Android Studio** as source of truth if terminal Gradle fails (e.g. `JAVA_HOME` / `jlink`). Cursor is for editing and AI-assisted coding.

## iOS reference path (parity)

`/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios` — `Pasabayan/Features/*`, `Services/*`, `Views/*`, `docs/`.
