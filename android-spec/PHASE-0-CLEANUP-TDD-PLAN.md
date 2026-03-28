# Phase 0 cleanup — TDD plan (parallel tracks)

**Purpose:** Close Phase 0 gaps called out in [IMPLEMENTATION-STATUS.md](IMPLEMENTATION-STATUS.md): [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) vs Android DTOs, [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) Phase 0 rows, and optionally **ErrorAlertPolicy** from [01-error-taxonomy.md](01-error-taxonomy.md).

**Rules:** Test-first or test-with each slice; one reviewable chunk per stop; suggested commits only (user commits manually). Cross-link: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) Phase 0 exit gate.

---

## How the three tracks relate

| Track | Outcome | TDD artifact |
|-------|---------|----------------|
| **A — API shapes** | Documented parity: Kotlin DTOs match wire JSON for endpoints you actually call | Golden JSON → `kotlinx.serialization` decode (and encode where relevant) tests |
| **B — TDD backlog** | Phase 0 checkboxes in [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) reflect reality | Extend existing `:core:network` / `:core:designsystem` tests; tick boxes |
| **C — ErrorAlertPolicy** | Foreground vs background error presentation matches iOS policy | Pure JVM unit tests on a small classifier; optional UI smoke later |

Tracks **A** and **B** can run in parallel. **C** is optional until you want consistent global error UX.

---

## Track A — `API-SHAPES-REFERENCE.md` vs Android DTOs

### Principle

- **Scope guard:** Audit **only endpoints that already have** Retrofit/DTO code in this repo (today: mostly **auth** in `:core:network`). As each feature module adds DTOs, add a **domain slice** to this audit.
- **Source order:** API-SHAPES tables → iOS `Codable` types → Android `@Serializable` DTOs.

### TDD slice pattern (repeat per endpoint or small group)

1. **Fixtures:** Minimal valid JSON under e.g. `core/network/src/test/resources/api-fixtures/auth/` (or string constants in test if tiny).
2. **Test:** `Json.decodeFromString<YourDto>(json)` — assert critical fields (snake_case keys).
3. **Negative tests (optional):** Malformed payloads; `ignoreUnknownKeys` alignment with iOS.
4. **Gap fix:** Adjust `@SerialName` / nullability; keep tests green.

### Suggested order (current codebase)

1. `POST /auth/{provider}/login` response shapes.
2. `GET /auth/me` user response.
3. `POST /auth/logout` body if modeled.
4. `GET /health` when a DTO exists.

### Exit (Track A)

- [IMPLEMENTATION-STATUS.md](IMPLEMENTATION-STATUS.md): [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md) → **[x]** for **implemented** contracts, with a Note listing paths covered.
- Optional note in API-SHAPES: where Android decode tests live.

### Suggested commits

- `test(network): add golden JSON decode tests for auth DTOs per API-SHAPES`
- `fix(network): align auth DTO serial names with API-SHAPES`

---

## Track B — `TDD-PARITY-BACKLOG.md` (Phase 0)

### Verify → tick

1. **Auth header** — `AuthInterceptorTest`; add cases for `/auth/me` **with** Bearer, other `/auth/*` **without** if not already exhaustive.
2. **Domain error mapping** — `ApiErrorMapperTest` vs [01-error-taxonomy.md](01-error-taxonomy.md); add **test-first** branches for any mapper behavior not yet covered.
3. **Design system** — mark **[x]** in backlog; point to `:core:designsystem` tests.

### Rule for later phases

When you **touch** a feature, add that phase is backlog tests — do not complete Phases 1–7 in this cleanup.

### Exit (Track B)

- Phase 0 rows in [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) are **[x]** or **Deferred** with reason.

### Suggested commits

- `test(network): extend AuthInterceptor coverage for Phase 0 backlog`
- `docs(android-spec): tick Phase 0 TDD-PARITY-BACKLOG items`

---

## Track C — `ErrorAlertPolicy` (optional)

### Goal

Mirror iOS `ErrorAlertPolicy.swift`: classify errors for **foreground** vs **background** presentation.

### TDD slice

1. Read iOS policy; list rules.
2. Kotlin API: e.g. `enum class ErrorPresentation { Modal, Snackbar, Silent }` + `fun DomainError.presentation(context: ErrorContext): ErrorPresentation`.
3. **Tests first:** table-driven JVM `(DomainError, ErrorContext) → presentation`.
4. **Implement** in `:core:domain-error` (no Compose).
5. **Wire later:** ViewModels / snackbar — separate commit.

### Exit (Track C)

- [01-error-taxonomy.md](01-error-taxonomy.md) TDD checklist item for ErrorAlertPolicy **[x]**.
- IMPLEMENTATION-STATUS note for ErrorPresentation tests.

### Suggested commits

- `test(domain-error): ErrorPresentation classification tests`
- `feat(domain-error): ErrorAlertPolicy parity mapper`

---

## Execution order (one developer)

| Slice | Track | Action |
|-------|-------|--------|
| 1 | A | Auth DTO golden JSON tests + fixes |
| 1 | B | Backlog audit; tick design system; extend interceptor tests |
| 2 | A | IMPLEMENTATION-STATUS + API-SHAPES [x] for implemented scope |
| 2 | C | Optional: mapper + tests only |

**Parallel:** Track A (network), B (tests/docs), C (domain-error).

---

## After cleanup

- Re-check Phase 0 gate in [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md); update [IMPLEMENTATION-STATUS.md](IMPLEMENTATION-STATUS.md).
- Continue Phase 1 ([02-auth-session.md](02-auth-session.md), [17-onboarding.md](17-onboarding.md)) with the same slice TDD pattern.
