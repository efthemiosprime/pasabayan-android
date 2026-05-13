# Phase 3 TDD sweep — residual items (12 / 13 / 14)

**Date:** 2026-05-13. **Phase:** 3 — Bookings & matches.

Three items left over from a Phase 3 TDD sweep. This doc records the **validation result** and the **fix recipe** for each. Cross-refs: [05-bookings-matches.md](05-bookings-matches.md) (TDD checklist lines 1934–1935), [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) (Phase 3 row), iOS reference `Pasabayan/Features/Bookings/{Models,ViewModels}/`.

---

## Item 12 — `CounterOfferContextTest.fromMatch falls back to message regex when originalPrice is null`

### Validation: **REAL — fails on HEAD**

```
./gradlew :app:testDebugUnitTest --tests "*CounterOfferContextTest*"
…
17 tests completed, 1 failed
CounterOfferContextTest > fromMatch falls back to message regex when originalPrice is null FAILED
    java.lang.AssertionError at CounterOfferContextTest.kt:107
```

The earlier survey agent's "passes" report was incorrect — likely tested a narrower subset.

### Root cause: **test expectation contradicts iOS parity**

- **Android impl** (`CounterOfferContext.kt:60–88`): when `match.isCounterOffer = true` and `originalPriceValue = null`, falls back to `originalPriceFromMessage(match)` and returns a non-null context.
- **iOS impl** (`CounterOfferContext.swift:146–190`): identical behavior — when `isCounterOffer` is true and `originalPrice` is nil, falls through to `originalPriceFromMessage` and builds a context.
- **Test** (`CounterOfferContextTest.kt:94–108`): asserts `assertNull(ctx)`. Comment claims "Regex-only fallback is intentionally rejected when isCounterOffer is true but originalPrice is null." That intent is **not** in iOS — iOS accepts the regex fallback there.

The Android implementation matches iOS. **The test is wrong.**

### Fix recipe (single slice — test only)

Update the test at `app/src/test/java/com/efthemiosprime/pasabayan/features/bookings/model/CounterOfferContextTest.kt:94–108` to assert the regex-fallback path produces a valid context, mirroring iOS:

```kotlin
@Test
fun `fromMatch falls back to message regex when originalPrice is null`() {
    val match = match(
        isCounterOffer = true,
        agreedPrice = 70.0,
        originalPrice = null,
        shipperMessage = "I would like to counter (was $100.00)",
        initiatedBy = InitiatedBy.SHIPPER,
        shipper = UserSummary(id = 7, name = "Alice"),
    )

    val ctx = CounterOfferContext.fromMatch(match)
    requireNotNull(ctx)
    assertEquals(70.0, ctx.newPrice, 0.001)
    assertEquals(100.0, ctx.originalPrice, 0.001)
    assertEquals("Alice", ctx.counterOffererName)
    assertEquals(7, ctx.counterOffererId)
    assertTrue(ctx.isCounterOffer)
}
```

Existing sibling tests already pin the **rejection** path (`fromMatch returns null when not a counter offer and no original price`, line 111) and the **regex parsers** (`originalPriceFromMessage parses parenthesized was form`, etc.). No impl change required.

### Verify

```
./gradlew :app:testDebugUnitTest --tests "*CounterOfferContextTest*"
```

Expected: 17/17 passing.

### Suggested commit

```
test(bookings): align CounterOfferContext regex-fallback test with iOS parity
```

---

## Item 13 — `AutoChargeConfirmationViewModel`: all 6 state machine transitions

### Validation: **STALE CHECKBOX — already covered**

Spec [05-bookings-matches.md:1935](05-bookings-matches.md) reads:

```
- [ ] `AutoChargeConfirmationViewModel`: all 6 state machine transitions
```

But `app/src/test/java/com/efthemiosprime/pasabayan/features/bookings/viewmodel/AutoChargeConfirmationViewModelTest.kt` already exercises **13 transitions** across the 9-state machine (`Idle`, `CheckingPaymentMethod`, `NeedsPaymentMethod`, `NeedsPaymentMethodAfterConfirm`, `ReadyToConfirm`, `AddingPaymentMethod`, `Confirming`, `Success`, `Error`) — iOS parity (`AutoChargeConfirmationViewModelTests.swift`):

| # | Transition | Test name |
|---|------------|-----------|
| 1 | `Idle/CheckingPM → ReadyToConfirm` | `prepareConfirmation with default PM transitions to ReadyToConfirm` |
| 2 | `Idle/CheckingPM → NeedsPaymentMethod` | `prepareConfirmation without default PM transitions to NeedsPaymentMethod` |
| 3 | `CheckingPM → Error` | `prepareConfirmation surfaces Error on repo failure` |
| 4 | `Confirming → Success(true)` | `confirmMatch success with queued auto-charge transitions to Success(true)` |
| 5 | `Confirming → NeedsPaymentMethodAfterConfirm` | `confirmMatch success without default PM transitions to NeedsPaymentMethodAfterConfirm` |
| 6 | `Confirming → Error` | `confirmMatch failure transitions to Error` |
| 7 | `NeedsPaymentMethod → AddingPaymentMethod` | `startAddingPaymentMethod transitions to AddingPaymentMethod` |
| 8 | `AddingPaymentMethod → emit client secret` | `startAddingPaymentMethod emits client secret on setup-intent success` |
| 9 | `AddingPaymentMethod → Error` | `startAddingPaymentMethod transitions to Error when setup-intent fails` |
| 10 | `AddingPaymentMethod (pre-confirm) → ReadyToConfirm` | `onPaymentMethodAdded pre-confirm re-checks and transitions to ReadyToConfirm` |
| 11 | `AddingPaymentMethod (post-confirm) → Success(true)` | `onPaymentMethodAdded post-confirm transitions to Success when retry queues charge` |
| 12 | `AddingPaymentMethod → NeedsPaymentMethod` | `onPaymentMethodCancelled pre-confirm returns to NeedsPaymentMethod` |
| 13 | `* → Idle` | `dismiss returns to Idle` |

The "6 transitions" wording in the checkbox was a conservative under-count. **The work is done.**

### Fix recipe (doc-only)

In [05-bookings-matches.md:1935](05-bookings-matches.md), flip the checkbox:

```diff
- - [ ] `AutoChargeConfirmationViewModel`: all 6 state machine transitions
+ - [x] `AutoChargeConfirmationViewModel`: 13 state-machine transitions across the 9-state FSM — `AutoChargeConfirmationViewModelTest` (2026-05-13).
```

Also update [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) Phase 3 row if it has a parallel entry (it currently does not — only counter-offer + IncomingRequestContext are ticked there).

### Suggested commit

```
docs(android-spec): tick AutoChargeConfirmationViewModel state-machine TDD row
```

---

## Item 14 — `LiveTrackingViewModel`: distance calc, ETA (40 km/h), stale detection (>10 min)

### Validation: **REAL GAP — feature not implemented on Android**

Spec [05-bookings-matches.md:1934](05-bookings-matches.md) reads:

```
- [ ] `LiveTrackingViewModel`: distance calc, ETA (40 km/h), stale detection (>10 min)
```

**Android `LiveTrackingViewModel.kt`** today only generates / confirms pickup + delivery codes. It does **not** implement:

- carrier/delivery coordinate state
- `calculateDistance()` (CLLocation-equivalent → km)
- `calculateETA()` (`remainingDistance / 40.0 * 60`, min 1)
- `lastUpdateTime` / stale-detection (`response.isStale == true` when >10 min old)
- `mapRegion` updates
- `fetchCarrierLocation` over `GET /matches/{id}/carrier-location`

**iOS `LiveTrackingViewModel.swift`** is the parity target (lines 14–218).

This is also flagged in [15-platform-and-tab-index.md:106](15-platform-and-tab-index.md): *"Shipper Matches — Live Delivery Tracking sheet missing — `LiveTrackingViewModel` exists, `bookings_action_track_live` string exists, no UI sheet."*

### Fix recipe — **two options**

Both end with the spec checkbox flipped. Pick at planning time.

#### Option A (recommended) — **Defer with reason, tighten the checkbox**

Tracking is a **feature gap**, not a TDD gap. Adding distance/ETA/stale tests against a VM that doesn't yet have the logic would be premature (parity tests against code that hasn't been written). Defer this row to the slice that builds the Live Delivery Tracking sheet.

**Doc-only changes:**

1. In [05-bookings-matches.md:1934](05-bookings-matches.md):
   ```diff
   - - [ ] `LiveTrackingViewModel`: distance calc, ETA (40 km/h), stale detection (>10 min)
   + - [ ] **Deferred** — `LiveTrackingViewModel`: distance calc, ETA (40 km/h), stale detection (>10 min). Will be implemented + tested together with the Live Delivery Tracking sheet (see [15-platform-and-tab-index.md:106](15-platform-and-tab-index.md)). Current VM only covers pickup/delivery code generation + confirmation; map/coordinates/ETA fields are not yet on the VM.
   ```

2. In [TDD-PARITY-BACKLOG.md](TDD-PARITY-BACKLOG.md) Phase 3, add a one-line cross-link if missing.

**Suggested commit:**

```
docs(android-spec): mark LiveTrackingViewModel tracking-math TDD row deferred to Live Tracking sheet slice
```

#### Option B — **Implement parity slice now, with tests**

Take a 4-step slice that builds iOS parity for the tracking math into the VM without yet wiring a sheet. Larger, but unblocks the TDD row.

**TDD order (one slice per layer per `CLAUDE.md`):**

1. **DTO + repo contract** — `CarrierLocationResponse` DTO (already mentioned at `05-bookings-matches.md:1894`). Add `BookingsRepository.getCarrierLocation(matchId: Int): Result<CarrierLocationResponse>` + impl + repo test with golden JSON (String coordinates → `latitudeDouble` / `longitudeDouble`, `is_stale` flag). **Stop / commit:** `feat(bookings): add carrier-location repo wire path with stale-flag decoding`.
2. **VM state + tracking math** — extend `LiveTrackingUiState` with `carrierLat/Lng`, `deliveryLat/Lng`, `remainingDistanceKm`, `totalDistanceKm`, `deliveryProgress`, `estimatedMinutes`, `lastUpdateTime: Instant?`, `isStale`. Add a pure-Kotlin `TrackingMath` (or `internal` companion) that computes:
   - Haversine distance in km (no Android dep; testable as plain JVM)
   - ETA: `max(1, (distanceKm / 40.0 * 60).toInt())`
   - Stale: `Duration.between(lastUpdate, now) > Duration.ofMinutes(10)` (parity with iOS server `isStale` flag — Android applies the same threshold locally if the server omits it)
3. **VM `refreshLocation(matchId:)`** — coroutine call to repo, update state, recompute math.
4. **Tests** — JVM tests:
   - `TrackingMathTest`: distance for fixed lat/lng pairs (e.g., Manila → Quezon City ≈ 11–13 km tolerance), ETA at 40 km/h, ETA min 1, stale boundary at exactly 10 min / 10 min + 1 ms.
   - `LiveTrackingViewModelTrackingTest`: repo success / repo failure / stale-flag-from-server / both-coords-nil → fields zeroed.

**Stop / commit (after each slice):**
- `test(bookings): add TrackingMath distance/ETA/stale unit tests`
- `feat(bookings): add carrier location + tracking math to LiveTrackingViewModel`
- `test(bookings): cover LiveTrackingViewModel tracking-math state transitions`

Then flip the spec checkbox to `[x]` with a link to the new test classes.

### Resolution: **Option B taken (2026-05-13)**

Per user direction "address all gaps", implemented Option B. The Live Delivery Tracking sheet remains deferred to its own slice, but the VM is now parity-complete and can be wired straight into a sheet without further plumbing work.

Landed as four slices:

| Slice | Outcome | Commit |
|-------|---------|--------|
| C.1 | iOS-parity DTOs (`CarrierLocationDataResponseJson` with nested `current_location` + `delivery_address`; `is_stale`, `MatchStatus` enum); `BookingsApi.getCarrierLocation`; `BookingsRepository.getCarrierLocation` + impl; domain `CarrierLocationSnapshot` + `toDomain()` mapper. Tests: `CarrierLocationJsonDecodeTest`, `CarrierLocationSnapshotTest`, `BookingsRepositoryImplTest.getCarrierLocation*`. | `feat(bookings): add carrier-location repo with iOS-parity DTOs and snapshot mapper` |
| D | `TrackingMath` pure-Kotlin object (Haversine km, 40 km/h ETA with 1-min floor, progress clamp, stale window with server-flag precedence). Tests: `TrackingMathTest`. | `feat(bookings): add TrackingMath for distance/ETA/stale parity with iOS` |
| E | `LiveTrackingUiState` extended with `carrierLat/Lng`, `deliveryLat/Lng`, `remainingDistanceKm`, `totalDistanceKm`, `deliveryProgress`, `estimatedMinutes`, `lastUpdatedAt`, `isStale`; `refreshLocation(matchId)` wires repo → math → state; `@VisibleForTesting` clock override. Tests added to `LiveTrackingViewModelTest`: success path, server stale flag wins, local 10-min window fallback, missing carrier coords, repo failure, totalDistance memoisation. | `feat(bookings): wire LiveTrackingViewModel to carrier-location + tracking math` |

Sheet/UI work — `Live Delivery Tracking sheet` per [15-platform-and-tab-index.md:106](15-platform-and-tab-index.md) — remains a separate slice (this VM is now ready to plug in). Will need a Compose Maps surface (Google Maps via Compose or a placeholder), period polling / pull-to-refresh, and the `bookings_action_track_live` entry point on `ShipperMatchDetailsSheet`.

---

## Execution order — done

1. **Item 12** — single-test fix; CI green. Commit: `test(bookings): align CounterOfferContext regex-fallback test with iOS parity`.
2. **Item 13** — doc-only checkbox flip. Commit: `docs(android-spec): tick AutoChargeConfirmationViewModel state-machine TDD row`.
3. **Item 14** — Option B landed across slices C, D, E (commits above).

All three are independent and can be committed separately.
