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

## Phases and commits

- Work in **small slices** inside the active phase; **do not** jump phases without user confirmation.
- After each slice: **stop**, suggest **conventional commit** message(s), user commits manually unless they ask you to commit.
- Update **`android-spec/IMPLEMENTATION-STATUS.md`** when a gate or milestone moves.

Rules: `.cursor/rules/phase-commit-workflow.mdc`, `.cursor/rules/pasabayan-android-spec-context.mdc`.

## Build / IDE

- **Gradle, Run, JDK, emulator:** use **Android Studio** as source of truth if terminal Gradle fails (e.g. `JAVA_HOME` / `jlink`). Cursor is for editing and AI-assisted coding.

## iOS reference path (parity)

`/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios` — `Pasabayan/Features/*`, `Services/*`, `Views/*`, `docs/`.
