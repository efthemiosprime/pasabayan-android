# 17 — Onboarding (first launch, city, consent)

**Phase:** 1 (with auth shell); **first-launch UI** can ship in Phase 0 as a stub. **Scope:** all flows in [`Features/Onboarding/`](../../Pasabayan/Features/Onboarding/) plus the **gates** in [`PasabayanApp.swift`](../../Pasabayan/PasabayanApp.swift) and [`ContentView.swift`](../../Pasabayan/ContentView.swift).

Onboarding on iOS is **not** a single screen — it is **three layers**:

1. **Pre-auth first launch** — role education (`OnboardingScreen`) before the user sees `AuthView`.
2. **Post-auth, pre-dashboard** — home city, then privacy consent (only when authenticated).
3. **Cross-cutting flags** — `OnboardingState` for dashboard behavior after consent.

**Related:** [02-auth-session.md](02-auth-session.md) (session vs gates), [09-profile-carrier-consent.md](09-profile-carrier-consent.md) (consent API), [12-legal-support-misc.md](12-legal-support-misc.md) (home city / catalog), [14-design-system.md](14-design-system.md) (tokens). **Not** Stripe Connect “onboarding” — that is payments: [06-payments-stripe.md](06-payments-stripe.md).

---

## 1. Root navigation order (source of truth)

### 1.1 `PasabayanApp` (cold start)

| Condition | UI |
|-----------|-----|
| UI test harness flags (`--uitesting`, `--uiTest…`) | Dedicated harness views — **skip** normal onboarding |
| `!hasCompletedOnboarding && !isUITesting` | [`OnboardingScreen`](../../Pasabayan/Features/Onboarding/Views/OnboardingScreen.swift) |
| Else | [`ContentView`](../../Pasabayan/ContentView.swift) (auth + post-auth gates + dashboard) |

**`hasCompletedOnboarding`** is `@AppStorage` (UserDefaults). Set `true` when the user finishes `OnboardingScreen` ([`OnboardingScreen.handleComplete`](../../Pasabayan/Features/Onboarding/Views/OnboardingScreen.swift)). Reset to `false` on explicit logout/reset paths in [`AuthView`](../../Pasabayan/Features/Authentication/Views/AuthView.swift) (see codebase).

### 1.2 `ContentView` (after first-launch onboarding is done, or skipped)

Shown only when `PasabayanApp` routes to `ContentView` (i.e. `hasCompletedOnboarding == true` or UI tests).

| Order | Condition | UI |
|-------|-----------|-----|
| 1 | `!authViewModel.isAuthenticated` | `AuthView` |
| 2 | Authenticated & `!hasCompletedCitySetup` | [`CitySelectionOnboardingView`](../../Pasabayan/Features/Onboarding/Views/CitySelectionOnboardingView.swift) |
| 3 | Authenticated & `!hasCompletedConsentSetup` | [`ConsentOnboardingView`](../../Pasabayan/Features/Onboarding/Views/ConsentOnboardingView.swift) |
| 4 | Else | `DashboardView` |

**`@AppStorage` keys:** `hasCompletedCitySetup`, `hasCompletedConsentSetup`.

---

## 2. Flow A — `OnboardingScreen` (role journey)

**Files:** [`OnboardingScreen.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingScreen.swift), [`OnboardingViewModel.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingViewModel.swift), [`OnboardingModels.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingModels.swift), [`RoleSelectionView.swift`](../../Pasabayan/Features/Onboarding/Views/RoleSelectionView.swift), [`JourneyStepView.swift`](../../Pasabayan/Features/Onboarding/Views/JourneyStepView.swift), [`OnboardingCompletionView.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingCompletionView.swift), [`OnboardingTheme.swift`](../../Pasabayan/Features/Onboarding/Views/Components/OnboardingTheme.swift).

### State machine

`OnboardingScreenState` ([`OnboardingModels.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingModels.swift)):

- `roleSelection` — welcome + pick **Carrier** vs **Sender (shipper)**.
- `journeyStep(role, stepIndex)` — **four** steps per role (`stepIndex` 0…3). Content from `JourneyStep.steps(for:)` (carrier vs sender copy keys under `Onboarding` strings table).
- `completion(completedRole)` — `OnboardingCompletionView`; user can continue to app or **explore the other role’s** journey.

**`@AppStorage` in `OnboardingViewModel`:** `hasViewedCarrierJourney`, `hasViewedSenderJourney` — track which journeys were completed/skipped.

### Completion

On “Continue to app” from completion:

- [`RoleViewModel`](../../Pasabayan/Features/Profile/ViewModels/RoleViewModel.swift): `currentRole` set and persisted.
- `hasCompletedOnboarding = true`.
- Optional `onComplete?(role)` callback; `dismiss()` where applicable.

**No REST** in this flow — education only.

---

## 3. Flow B — `CitySelectionOnboardingView`

**When:** Authenticated user, `hasCompletedCitySetup == false`.

**Purpose:** Pick **home city** so backend can compute `distance_km` on browse endpoints.

**Behavior (summary):**

- Loads city list from catalog (see [12-legal-support-misc.md](12-legal-support-misc.md) — `/locations/countries/CA/cities` and related).
- Optional **GPS** match via `LocationService` / `CoreLocation`.
- Save: `ProfileAPIService.updateUserProfile` with `homeCityId` (see [`CitySelectionOnboardingView`](../../Pasabayan/Features/Onboarding/Views/CitySelectionOnboardingView.swift) — `UpdateProfileRequest` / user update).
- Local fallbacks: `pendingHomeCityId`, `confirmedHomeCityId` in `UserDefaults` for retry UX.
- On continue: `hasCompletedCitySetup = true` (caller closure in `ContentView`).

**Design:** Uses [`DesignSystem`](../../Pasabayan/Views/Components/DesignSystem.swift) for colors; align Android with [14-design-system.md](14-design-system.md).

---

## 4. Flow C — `ConsentOnboardingView`

**When:** Authenticated, city setup done, `hasCompletedConsentSetup == false`.

**Toggles (local state, all default OFF):**

| Key (API payload) | Meaning |
|-------------------|---------|
| `push_notifications` | Opt-in push |
| `location_tracking` | Location tracking consent |
| `analytics` | Analytics |
| `marketing_communications` | Marketing |

**Save:** `ProfileAPIService.updateConsentPreferences([String: Bool])` — PUT `/profile/consent-preferences` (see [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md), [09-profile-carrier-consent.md](09-profile-carrier-consent.md)).

**If request fails:** Logs warning; **still continues** — consent is not a hard blocker.

**On continue:**

- Sets `OnboardingState.didJustCompleteConsent = true` (static flag in [`OnboardingModels.swift`](../../Pasabayan/Features/Onboarding/Views/OnboardingModels.swift)) **before** `hasCompletedConsentSetup = true` in `ContentView`.
- Push: if opted in, request system permission and register FCM token path ([08-notifications-device-tokens.md](08-notifications-device-tokens.md)).

**Carrier dashboard:** [`CarrierDashboardContent`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/CarrierDashboardContent.swift) checks `OnboardingState.didJustCompleteConsent` to **avoid flashing** `CarrierPreferencesFormSheet` immediately after consent.

---

## 5. Persistence keys (parity checklist)

Use the **same logical flags** on Android (`DataStore` / `SharedPreferences`); names can mirror these strings for clarity:

| Key | Where set | Meaning |
|-----|-----------|---------|
| `hasCompletedOnboarding` | `OnboardingScreen` complete | First-launch role journey finished |
| `hasCompletedCitySetup` | `ContentView` after city flow | Post-auth city step done |
| `hasCompletedConsentSetup` | `ContentView` after consent flow | Post-auth consent step done |
| `hasViewedCarrierJourney` | `OnboardingViewModel` | Journey progress (optional parity) |
| `hasViewedSenderJourney` | `OnboardingViewModel` | Journey progress (optional parity) |
| `pendingHomeCityId` / `confirmedHomeCityId` | City flow | Retry / confirmation (optional parity) |

**`OnboardingState.didJustCompleteConsent`:** in-memory only on iOS — replicate with a **one-shot** session flag in Android (e.g. `SavedStateHandle` or `Activity` scoped) so carrier UI matches.

### Complete local storage keys (all features, for reference)

These are all the `@AppStorage` / `UserDefaults` keys found in iOS that need Android equivalents:

| Key | Type | Feature | Notes |
|-----|------|---------|-------|
| `hasCompletedOnboarding` | Bool | Onboarding | First-launch gate |
| `hasCompletedCitySetup` | Bool | Onboarding | Post-auth city gate |
| `hasCompletedConsentSetup` | Bool | Onboarding | Post-auth consent gate |
| `hasViewedCarrierJourney` | Bool | Onboarding | Optional education tracking |
| `hasViewedSenderJourney` | Bool | Onboarding | Optional education tracking |
| `pendingHomeCityId` | Int | Onboarding/City | Retry UX |
| `confirmedHomeCityId` | Int | HomeCityDetection | GPS-matched city |
| `user_preferred_role` | String | Profile/Role | `"shipper"` or `"carrier"` |
| `current_user` | JSON | Auth | JSON-encoded User model |
| `fcm_device_token` | String | Notifications | Push token |
| `push_notifications_api_consent_opt_in` | Bool | Consent | Push consent API state |
| `hasSeenPackageTutorial` | Bool | Packages | One-time tutorial overlay |
| `carrier_usual_transport_v1_{userId}` | String | Trips | TransportationMethod per user |
| `shipper_disclaimer_ack_v1_{userId}` | Bool | Packages | Per-user disclaimer ack |
| `shipper_disclaimer_pending_sync_v1_{userId}` | Bool | Packages | Offline sync tracking |
| `saved_package_descriptions_v1` | JSON [String] | Packages | Max 15, FIFO |
| `saved_pickup_templates_v1` | JSON array | Packages | Max 5, by date desc |
| `saved_handoff_templates_v1` | JSON array | Packages | Max 5, by date desc |

---

## 6. Android implementation notes

- **Single navigation graph:** Encode the order **PasabayanApp equivalent → ContentView equivalent** so QA can match iOS: first-launch onboarding **before** auth stack is shown.
- **ViewModels:** One coordinator per flow (or one with sealed state mirroring `OnboardingScreenState`); **no** direct API calls from composables — repositories for profile + catalog.
- **Tests:** UI tests skip onboarding via the same **intent/argument** pattern as iOS `--uitesting` (document in Android test runner).
- **Stripe “carrier onboarding”** (payouts) is a **different** flow — see [06-payments-stripe.md](06-payments-stripe.md) and `APIError.carrierOnboardingRequired`.

---

## TDD / exit gate

- [ ] Cold start: user without `hasCompletedOnboarding` sees role onboarding before auth.
- [ ] After login: city screen → consent screen → dashboard; flags persist across process death.
- [ ] Consent payload matches `ProfileAPIService` keys; failure still advances with logged error.
- [ ] Carrier dashboard does not flash carrier sheet when `didJustCompleteConsent` equivalent is set.
