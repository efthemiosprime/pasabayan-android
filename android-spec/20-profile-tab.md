# 20 — Profile tab (shared shell + section parity)

**Phase:** 6 | **Feature:** Profile tab shell | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

Define Android parity for the **shared Profile tab root** (tab index 4 for shipper and carrier), including section order, role-specific visibility rules, sheet/route triggers, and strict design-system conformance.

This spec complements:
- [09-profile-carrier-consent.md](09-profile-carrier-consent.md) (profile/carrier APIs + edit flows)
- [06-payments-stripe.md](06-payments-stripe.md) (payment methods, transactions, payout setup)
- [05-bookings-matches.md](05-bookings-matches.md) (delivery/package history)
- [10-verification.md](10-verification.md), [11-favorites-ratings.md](11-favorites-ratings.md), [12-legal-support-misc.md](12-legal-support-misc.md)

## iOS parity sources

- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/docs/tabs/SHARED_TAB_PROFILE.md`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/Views/ProfileView.swift`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/ViewModels/ProfileViewModel.swift`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/Services/ProfileAPIService.swift`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/Models/ProfileModels.swift`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/Models/CarrierProfile.swift`
- `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Profile/Views/Components/ProfileComponents/*`

## API contract lock (endpoints + payload/response signatures)

Profile tab integrations must use the exact API signatures below (parity with iOS `ProfileAPIService`).  
Canonical contract details live in [09-profile-carrier-consent.md](09-profile-carrier-consent.md) and [API-SHAPES-REFERENCE.md](API-SHAPES-REFERENCE.md); this section is a strict lock for the tab shell work.

### User profile endpoints

| Method | Path | Request signature | Response signature |
|------|------|-------------------|--------------------|
| GET | `/api/profile` | none | `ProfileResponse { success, message?, data: { profile: UserProfile?, home_city_id: Int?, is_complete: Boolean } }` |
| PUT | `/api/profile` | JSON `UpdateProfileRequest` (`full_name?`, `delivery_address?`, `profile_picture?`, `preferred_contact_method?`, `additional_info?`, `home_city_id` explicit null-or-int) | `ProfileResponse` |
| POST | `/api/profile` | multipart form-data: `profile_picture` (file), optional `full_name`, `delivery_address`, required `preferred_contact_method` (default `app_notification` if omitted by caller), optional `additional_info` as JSON string | `ProfileResponse` |
| DELETE | `/api/profile/picture` | none | `APIResponse { success, message }` |
| POST | `/api/profile/request-deletion` | JSON `{ confirm: Boolean, reason?: String }` | `AccountDeletionResponse { success, message, data: { request_id, status, requested_at } }` |
| GET | `/api/profile/export-data` | none | raw JSON bytes (`Data`) |

### Consent + disclaimers

| Method | Path | Request signature | Response signature |
|------|------|-------------------|--------------------|
| GET | `/api/profile/disclaimer-acknowledgments` | none | `DisclaimerAcknowledgmentsListResponse { success, data: { carrier_trip?, shipper? } }` |
| POST | `/api/profile/disclaimer-acknowledgments` | JSON `{ disclaimer_type: String }` | `DisclaimerAcknowledgmentResponse { success, data: { disclaimer_type, acknowledged_at } }` |
| GET | `/api/profile/consent-preferences` | none | `ConsentPreferencesResponse { success, data: { push_notifications, location_tracking, analytics, marketing_communications } }` |
| PUT | `/api/profile/consent-preferences` | scoped JSON map of changed keys only (`String -> Boolean`) | `ConsentPreferencesResponse` |

### Carrier endpoints

| Method | Path | Request signature | Response signature |
|------|------|-------------------|--------------------|
| GET | `/api/carrier/profile` | none | `CarrierProfileResponse { message, data: CarrierProfile }` |
| POST | `/api/carrier/profile` | JSON `CreateCarrierProfileRequest` (`preferred_pickup_city_id` explicit null-or-int, capacities, arrays, pricing, bio) | `CarrierProfileResponse` (on `already exists`, iOS falls back to GET) |
| PUT | `/api/carrier/profile` | same as POST | `CarrierProfileResponse` |
| GET | `/api/carrier/stats` | none | `CarrierStatsResponse { success, message, data: CarrierStats }` |
| POST | `/api/carrier/enable` | none | `CarrierEnableResponse { message? }` |
| POST | `/api/carrier/toggle-status` | none | `CarrierStatusResponse { success?, message, data: { is_active_carrier, carrier_status, has_carrier_profile, profile_recommended, user_types } }` |

### Decoding and shape compatibility rules

- Keep snake_case JSON key mappings exactly as in iOS models.
- Preserve tolerant numeric decoding for profile/carrier fields where backend may return string-or-number (`Double` fields in carrier/profile stats).
- Preserve tolerant `user_types` decoding for array/dictionary/numeric variants in carrier status responses.
- `preferred_pickup_city_id` and `home_city_id` must support explicit null encoding when clearing values.
- `available_routes` must map `from`/`to` keys; support string fallback route parsing where backend returns a comma-separated route string.

## Root contract

| Field | Value |
|------|-------|
| Tab index | `4` |
| Tab title | Profile |
| Root route | `ProfileTabScreen` (Android) parity to `ProfileView` (iOS) |
| Roles | Shared tab; content adapts for shipper/carrier |
| Badge source | Notifications unread (dot/count per [15-platform-and-tab-index.md](15-platform-and-tab-index.md)) |

## Section order (must match iOS)

Android profile tab content order must remain:

1. `ProfileUserHeader` — avatar (`PAvatar`, Coil-loaded), name + inline `VerificationBadge`, email, `RoleChip`. Carrier role overlays a top-right `CarrierActiveBadge`.
2. `ProfileStatsBlock` (role-specific stats)
3. `CarrierPreferencesCard` (carrier only) — read-only summary of trip defaults: preferred pickup city, max weight (kg), max space (L). Tapping "Edit" opens the existing carrier-profile edit sheet.
4. `VerificationCallout` (visible for `basic` and `verified`, hidden for `premium`) — tinted info card with three states; see [Verification states](#verification-states-basic--verified--premium).
5. `AccountMenuSection`
6. `PaymentsMenuSection`
7. `BookingsMenuSection`
8. `FavoritesMenuSection` (shipper only)
9. `FeedbackMenuSection` — Pending Reviews row + My Ratings row (with inline avg + count preview)
10. `SupportMenuSection`
11. Logout button + app version footer

The standalone `CarrierStatusCard` was consolidated into the header's `CarrierActiveBadge` (single source of truth). Role-switcher row is intentionally omitted — switching lives in `DashboardTopBar` only. Do not reorder without updating this spec and iOS parity notes.

## UI behavior and role gating

### Header

- `PAvatar` (Coil 3 `AsyncImage`, 72 dp) — initials fallback while loading or when URL is null. Cache-busted via `state.avatarCacheBuster` after upload/delete (iOS parity with `UserProfileAvatar.cacheBustedURL`).
- Avatar URL priority: `state.userProfile?.profilePicture ?? user.avatar`.
- Name display priority: `state.userProfile?.fullName?.takeIf { it.isNotBlank() } ?: user.name`.
- Inline `VerificationBadge` (18 dp) next to the name — green check for `verified`, gold star for `premium`, nothing for `basic`. Same composable also renders in `DashboardTopBar`; promotion candidate to `:core:designsystem` if a third consumer emerges.
- Email below the name (`Body.small`, `onSurfaceVariant`).
- Static `RoleChip` (identification only — switching lives in `DashboardTopBar`).
- Carrier role overlays a top-right `CarrierActiveBadge` (green "Active" / gray "Inactive" pill).

### Role switching

- Role switching lives **only** in `DashboardTopBar`'s `SwapHoriz` icon — single source of truth. The profile tab does not have its own role switcher.
- If role activation prerequisites are missing (e.g. carrier setup), show setup/consent flow from [09-profile-carrier-consent.md](09-profile-carrier-consent.md).

### Verification states (basic → verified → premium)

`VerificationCallout` switches on `verificationLevel` + the `premiumStatus.requests[0].status` field:

| Level | Pending premium app? | Renders |
|-------|---------------------|---------|
| `basic` | n/a | "Verification" header + Why-verify benefits + **Verify Now** CTA → `PhoneVerificationSheet` |
| `verified` | none / approved / rejected | "Complete Your Verification" header + premium benefits (gold-star badge, search priority, government-ID verified) + **Verify My Identity** CTA → `PremiumVerificationSheet` |
| `verified` | `pending` or `under_review` | "Premium Application Submitted" header + Status / Estimated Review / Application ID rows. No CTA. |
| `premium` | n/a | Renders nothing (`shouldShowVerificationCard` filter). |

All three branches share `VerificationCalloutSurface` chrome (BadgeBlueLight bg + Info-tinted 1 dp border + 16 dp padding) and `VerificationCalloutHeader` (48 dp icon circle + title h4 + caption description). Per-row benefits use `VerificationBenefitRow` with role-coloured icons.

### Carrier preferences card

`CarrierPreferencesCard` (carrier-only) renders three `PDetailRow`s sourced from `state.carrierProfile`:

| Row | Source | Fallback |
|-----|--------|----------|
| Preferred Pickup City | `preferredPickupCity?.displayName()` (city + stateCode) | "Not set" |
| Max Weight Capacity | `maxWeightCapacityKg` formatted as `"%.0f kg"` | "Not set" |
| Max Space Capacity | `maxSpaceCapacityLiters` formatted as `"%.0f L"` | "Not set" |

A trailing right-aligned "Edit" link (Info color + chevron) routes to the existing carrier-profile edit sheet via the `onEdit` callback.

iOS parity reference: `CarrierPreferencesSection.swift` `.standalone`. Note that iOS also displays a "Usual transport" row sourced from a per-user preference store (`UsualTransportStore`); this row is **deferred** until the Android equivalent is ported.

### Attention signals (badges)

`ProfileAttentionViewModel` exposes `attention: StateFlow<AttentionSignalsJson>` from `GET /api/me/attention`:

| Field | Wired to |
|-------|----------|
| `phoneVerificationNeeded` | iOS shows a badge on the Account → Verification menu row. Android does **not** — the dedicated `VerificationCallout` already serves the attention purpose; a redundant badge would compete with it. |
| `payoutSetupNeeded` | Payments → **Payout Setup** row (`badgeCount = if (true) 1 else 0`); carrier-only so naturally hidden for shippers. |
| `pendingReviewsCount` | Feedback → **Pending Reviews** row (`badgeCount = pendingReviewsCount`). |
| `total` | Reserved for an eventual Profile-tab badge in `PasabayanBottomBar` (not yet wired — follow-up). |
| `degraded` | Server reports partial response. Render existing values best-effort, no error UI. |

Refresh policy: refresh on Profile-tab open via `LaunchedEffect(user, currentRole)`. The VM dedupes in-flight calls via `AtomicBoolean` (no client polling, safe for rapid tab switches). Failures are silenced into prior state — attention is best-effort, never an error UI. Future refresh triggers (after verify / payout / rating completion) call `attentionViewModel.refresh()` directly.

### Stats

- Carrier:
  - deliveries count
  - rating summary
  - earnings summary
- Shipper:
  - packages count
  - rating summary
  - delivered count
- Fallback behavior when `/user/stats` is unavailable must use existing user-level rating fields (iOS parity).

### Menus and navigation targets

All rows render via `PMenuRow` from `:core:designsystem` — leading icon (24 dp, primary tint) + title + 1-line subtitle + optional trailing slot + optional unread badge + chevron. iOS parity with `ProfileMenuItem`.

#### Account

| Item | Visibility | Icon | Target | Badge |
|------|------------|------|--------|-------|
| Personal Info | all roles | `Icons.Filled.Person` | `EditUserProfileSheet` | — |
| Vehicle Info | carrier only | `Icons.Filled.DirectionsCar` | `EditCarrierProfileSheet` | — |
| Shipping Addresses | shipper only | `Icons.Filled.LocationOn` | placeholder (future route) | — |
| Account & data | all roles | `Icons.Filled.ManageAccounts` | `AccountManagementSheet` | — |

(Verification is **not** an Account menu row on Android — it's the dedicated `VerificationCallout` above. iOS includes both; the Android consolidation avoids duplicate entry points.)

#### Payments

| Item | Visibility | Icon | Target spec | Badge |
|------|------------|------|-------------|-------|
| Payment Methods | all roles | `Icons.Filled.CreditCard` | [06-payments-stripe.md](06-payments-stripe.md) | — |
| Transaction History | all roles | `Icons.AutoMirrored.Filled.ListAlt` | [06-payments-stripe.md](06-payments-stripe.md) | — |
| Receipts | all roles | `Icons.Filled.Receipt` | [06-payments-stripe.md](06-payments-stripe.md) | — |
| Payout Setup | carrier only | `Icons.Filled.AccountBalance` | [06-payments-stripe.md](06-payments-stripe.md) | `attention.payoutSetupNeeded ? 1 : 0` |

#### Bookings

| Item | Visibility | Icon | Target spec |
|------|------------|------|-------------|
| Delivery History | carrier only | `Icons.Filled.History` | [05-bookings-matches.md](05-bookings-matches.md) |
| Package History | shipper only | `Icons.Filled.History` | [05-bookings-matches.md](05-bookings-matches.md) |

#### Favorites

| Item | Visibility | Icon | Target spec |
|------|------------|------|-------------|
| Favorites | shipper only | `Icons.Filled.Star` | [11-favorites-ratings.md](11-favorites-ratings.md) |

#### Feedback (two rows, iOS `FeedbackMenuSection` parity)

| Item | Icon | Target | Badge | Trailing |
|------|------|--------|-------|----------|
| Pending Reviews | `Icons.Filled.RateReview` | `RatingsScreen` | `attention.pendingReviewsCount` | — |
| My Ratings | `Icons.Filled.Star` (gold) | `RatingsScreen` | — | Inline `"%.2f ★ (N)"` from role-specific stats, or "No ratings yet" |

The rating preview source is `state.userStats?.averageRating` + `totalRatings` (shipper) or `state.carrierStats?.ratings?.averageRating` (parsed via `toDoubleOrNull()`) + `totalRatings` (carrier). Helper: `profileRatingPreview(role, state)`.

#### Support

| Item | Icon | Target spec |
|------|------|-------------|
| Help Center | `Icons.AutoMirrored.Filled.HelpOutline` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Settings | `Icons.Filled.Settings` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Terms and Privacy | `Icons.Filled.Description` | [12-legal-support-misc.md](12-legal-support-misc.md) |

### Actions/footer

- Destructive logout action at end of screen.
- App name/version/build footer below logout (parity with iOS `AppVersionFooter`).

## Data and state contract

Two ViewModels back the profile tab — one for tab data, one for attention signals.

### `ProfileTabViewModel.uiState`

| State | Type | Purpose |
|------|------|---------|
| `isLoading` | Boolean | show loading state while profile/bootstrap requests run |
| `errorMessage` | String? | one-shot or banner/dialog presentation |
| `successMessage` | String? | save/update success presentation |
| `userProfile` | UserProfileJson? | source of profile display values |
| `carrierProfile` | CarrierProfileJson? | carrier-specific display + carrier-prefs card |
| `carrierStats` | CarrierStatsJson? | carrier stats block + My Ratings inline preview |
| `userStats` | UserStatsDataJson? | shipper stats block + My Ratings inline preview |
| `premiumStatus` | PremiumVerificationStatusDataJson? | drives the `verified → upgrade vs pending` branch in `VerificationCallout`. Loaded only when `verificationLevel == "verified"` (basic/premium short-circuit). |
| `avatarCacheBuster` | String | refresh avatar URL after upload/delete |
| `currentRole` | UserRole | role-gated section rendering |
| `authUser` | AuthUser? | session user echoed for screens that consume the VM directly |

Behavior requirements:
- Debounce/guard duplicate profile loads on quick tab revisits via `AtomicBoolean`.
- Force refresh after avatar upload/delete and profile save events.
- Keep auth/session user representation in sync after profile updates.
- Preserve optimistic UX without stale data races (single in-flight loader guard).
- Premium status fetched after profile load; gated on level so basic/premium users avoid the API call.

### `ProfileAttentionViewModel.attention`

| State | Type | Purpose |
|------|------|---------|
| `attention` | StateFlow<AttentionSignalsJson> | drives per-row badges. Defaults to empty signals. |

Behavior requirements:
- `refresh()` is event-driven only — no client polling.
- Triggered by `ProfileTabScreen` `LaunchedEffect(user, currentRole)`, after verify/payout/rating completion.
- `AtomicBoolean` dedupes concurrent `refresh()` calls.
- Failures are silenced into prior state — attention is best-effort, never an error UI.

## Design-system conformance (strict)

All profile tab UI must use `:core:designsystem` primitives/tokens per [14-design-system.md](14-design-system.md):

| Primitive | Used by |
|-----------|---------|
| `PScaffold` + `PTopBar` | Outer chrome (delegated to `MainTabScreen`). |
| `PCard` (and `PCardVariant.Large` for the header) | All section containers. |
| `PMenuRow` | Every menu row across Account / Payments / Bookings / Favorites / Feedback / Support. |
| `PAvatar` | Header avatar — Coil 3 over OkHttp + initials fallback + `?cb=…` cache-busting. |
| `PChip` | Top Carriers + Popular Routes (Explore tab). |
| `PNotificationBadge` | Per-row unread counts (used internally by `PMenuRow`). |
| `PDetailRow` | Carrier preferences card. |
| `PButton` | Logout + Verify Now / Apply for Premium CTAs. |
| `PDivider` | Card sub-section separators. |
| `PasabayanSpacing` / `PasabayanRadius` / `PasabayanTextStyles` / `PasabayanColors` | All visual values — no raw `dp` / `Color(0x…)`. |

Feature-shared (app module, not designsystem):

| Composable | Location | Used by |
|-----------|----------|---------|
| `VerificationBadge` | `features/dashboard/components/` | `DashboardTopBar` + `ProfileUserHeader` (inline next to user name). Promote to `:core:designsystem` when a third consumer emerges. |
| `CarrierActiveBadge` | `features/dashboard/components/` | `ProfileUserHeader` top-right overlay (carrier role only). |
| `RoleChip` | `features/dashboard/components/` | `ProfileUserHeader` (static identification only). |
| `CarrierPreferencesCard` | `features/profile/components/` | Profile tab between stats and verification (carrier role). |

If a reusable profile pattern is missing, add it to `:core:designsystem` first and document it in [14-design-system.md](14-design-system.md). Per `.cursor/rules/ui-reusability-and-inheritance.mdc`, the second cross-feature consumer triggers promotion.

## Localization

All user-visible copy must be in Android resources (EN + FR) using [18-localization.md](18-localization.md):

- Add keys to `strings_profile.xml` and `values-fr/strings_profile.xml`.
- No hardcoded text in composables or ViewModels.
- Reuse `strings_common.xml` keys for shared buttons where possible.

## Loading, error, and empty states

- Loading: show DS progress indicator/skeleton while fetching profile data.
- Error: show DS-compliant error surface + retry action.
- Empty/unconfigured sections (e.g., shipping addresses placeholder): explicit placeholder state, not blank space.
- Role-switch and sheet flows must remain interactive while non-critical sections load.

## TDD checklist (profile tab shell)

### Tab-data ViewModel
- [x] `ProfileTabViewModelTest`: section visibility for shipper vs carrier (`ProfileTabVisibility` + VM tests).
- [x] `ProfileTabViewModelTest`: verification card visibility for `basic`/`verified`/`premium` (`shouldShowVerificationCard`).
- [x] `ProfileTabViewModelTest`: profile load dedupe and force-refresh rules.
- [x] `ProfileTabViewModelTest`: avatar cache-buster (`onAvatarChanged`).
- [x] `ProfileTabNavigationTest`: menu rows route correctly (callbacks wired in `MainTabScreen`).
- [x] `ProfileTabUiTest` (androidTest): role-specific sections and logout callback.
- [ ] `ProfileTabLogoutTest`: end-to-end auth — partial; existing auth flows cover it.
- [ ] `ProfileTabLocalizationTest`: no hardcoded user-facing strings — deferred (all profile UI via `stringResource`).

### Attention signals (this iteration)
- [x] `core:network` — `AttentionSignalsJsonTest`: full payload, defaults, `degraded` flag propagation, unknown extra fields.
- [x] `app` — `ProfileAttentionViewModelTest`: empty initial state, success updates state, failure preserves prior state, concurrent `refresh()` dedupes to one repo call, `degraded` flag round-trip.

### Avatar (this iteration)
- [x] `core:designsystem` — `AvatarCacheBusterTest`: null URL → null, blank URL → null, blank cacheBuster passes through, `?cb=…` appended for query-less URLs, `&cb=…` for URLs with existing query.

### Premium upgrade callout (this iteration)
- [ ] VM-level test for `premiumStatus` branch selection — pre-existing `:app` test source-set bit-rot blocks adding new tests there; tracked as **deferred**, see [Open issues](#open-issues-and-known-deferrals).

### Compose previews (light + dark, all in-file)
- [x] `PMenuRow` — title-only / icon / icon+subtitle / icon+subtitle+badge / icon+trailing-slot.
- [x] `PAvatar` — three sizes with no URL → initial fallback.
- [x] `CarrierPreferencesCard` — populated + empty.
- [x] `PChip` — three variants (label only, label + secondary, with optional icon).
- [x] `ProfileTabScreenPreview` — shipper+verified header and carrier+premium+active header in light + dark.

## Open issues and known deferrals

- **Pre-existing `:app` test bit-rot.** `MatchingViewModelTest`, `ChatRepositoryImplTest`, `TripPackageProgressViewModelTest`, and `UserProfilePopoverViewModelTest` reference DTO fields that have since been renamed/added. These pre-date the profile-tab parity work and currently block `./gradlew :app:testDebugUnitTest` from compiling end-to-end. New tests added in this iteration (`ProfileAttentionViewModelTest`) compile cleanly in isolation but cannot be executed via Gradle until the bit-rot is cleaned up in a separate maintenance PR. The DTO + designsystem tests run independently and pass.
- **Bottom-nav profile badge.** `attention.total` is exposed but not yet wired to `PasabayanBottomBar`'s `badgeCountByRoute` — follow-up.
- **Carrier preferences "Usual transport" row.** iOS reads from `UsualTransportStore`; no Android equivalent yet. Tracked as a follow-up before carrier preferences spec is fully closed.
- **Verification badge on Account → Verification row.** iOS shows `phoneVerificationNeeded` as a badge on a menu row; Android intentionally omits this menu entry because the dedicated `VerificationCallout` already serves the attention purpose. Revisit if user testing shows the callout is missed.

## Exit gate for this spec

This spec is complete when:

1. Profile tab section order and role-gating match iOS behavior.
2. All menu entries route to implemented features or explicit placeholders with documented follow-up.
3. DS and localization rules are satisfied (no ad-hoc styling/hardcoded strings).
4. TDD checklist above is green (or deferred items explicitly documented in `IMPLEMENTATION-STATUS.md`).

