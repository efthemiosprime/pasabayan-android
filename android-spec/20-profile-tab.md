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

1. `UserProfileHeader`
2. `RoleSwitcherSection`
3. `ProfileStatsSection` (role-specific stats)
4. `CarrierStatusCard` (carrier only)
5. `VerificationStatusView` (visible for `basic` and `verified`, hidden for `premium`)
6. `AccountMenuSection`
7. `PaymentsMenuSection`
8. `BookingsMenuSection`
9. `FavoritesMenuSection` (shipper only)
10. `FeedbackMenuSection`
11. `SupportMenuSection`
12. `ProfileActionsSection` (logout + app version footer)

Do not reorder without updating this spec and iOS parity notes.

## UI behavior and role gating

### Header

- Show avatar, display name, email/phone summary, current role chip, verification badge.
- Name/avatar priority: profile record values first; auth user fallback when profile data is absent.
- Carrier role shows active/inactive carrier status badge.
- Verification badge CTA:
  - `basic`: tappable upgrade/verify path.
  - `verified` / `premium`: non-edit badge display.

### Role switching

- Role switcher must update tab shell behavior consistently with dashboard role state.
- If role activation prerequisites are missing (e.g. carrier setup), show setup/consent flow from [09-profile-carrier-consent.md](09-profile-carrier-consent.md).

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

#### Account

| Item | Visibility | Target |
|------|------------|--------|
| Personal Info | all roles | Edit user profile sheet/route |
| Verification | all roles | Verification status/detail route |
| Vehicle Info | carrier only | Edit carrier profile sheet/route |
| Shipping Addresses | shipper only | Placeholder / future route (explicitly marked if not implemented) |

#### Payments

| Item | Visibility | Target spec |
|------|------------|-------------|
| Payment Methods | all roles | [06-payments-stripe.md](06-payments-stripe.md) |
| Transaction History | all roles | [06-payments-stripe.md](06-payments-stripe.md) |
| Receipts | all roles | [06-payments-stripe.md](06-payments-stripe.md) |
| Payout Setup | carrier only | [06-payments-stripe.md](06-payments-stripe.md) |

#### Bookings

| Item | Visibility | Target spec |
|------|------------|-------------|
| Delivery History | carrier only | [05-bookings-matches.md](05-bookings-matches.md) |
| Package History | shipper only | [05-bookings-matches.md](05-bookings-matches.md) |

#### Favorites / feedback / support

- `FavoritesMenuSection`: shipper only; route to favorites list from [11-favorites-ratings.md](11-favorites-ratings.md).
- `FeedbackMenuSection`: pending reviews + my ratings; route to ratings surfaces from [11-favorites-ratings.md](11-favorites-ratings.md).
- `SupportMenuSection`:
  - Help Center
  - Settings
  - Terms and Privacy
  (targets defined in [12-legal-support-misc.md](12-legal-support-misc.md))

### Actions/footer

- Destructive logout action at end of screen.
- App name/version/build footer below logout (parity with iOS `AppVersionFooter`).

## Data and state contract

Minimum state required in Android profile tab ViewModel layer:

| State | Type | Purpose |
|------|------|---------|
| `isLoading` | Boolean | show loading state while profile/bootstrap requests run |
| `errorMessage` | String? | one-shot or banner/dialog presentation |
| `successMessage` | String? | save/update success presentation |
| `userProfile` | UserProfile? | source of profile display values |
| `carrierProfile` | CarrierProfile? | carrier-specific display/actions |
| `avatarCacheBuster` | String | refresh avatar URL after upload/delete |
| `currentRole` | UserRole | role-gated section rendering |

Behavior requirements:
- Debounce/guard duplicate profile loads on quick tab revisits.
- Force refresh after avatar upload/delete and profile save events.
- Keep auth/session user representation in sync after profile updates.
- Preserve optimistic UX without stale data races (single in-flight loader guard).

## Design-system conformance (strict)

All profile tab UI must use `:core:designsystem` primitives/tokens per [14-design-system.md](14-design-system.md):

- Use `PScaffold` + `PTopBar` for screen chrome.
- Use `PCard` and/or profile-specific `P*` section containers for menu blocks.
- Use `PButton` for logout and section CTAs.
- Use `PasabayanSpacing`, `PasabayanRadius`, `PasabayanTextStyles`, `PasabayanColors`.
- Avoid ad-hoc `Color(0x...)`, raw spacing magic numbers, or bespoke component styling in feature module.

If a reusable profile pattern is missing (e.g., menu row primitive with icon/title/subtitle/chevron), add it to `:core:designsystem` first and document it in [14-design-system.md](14-design-system.md).

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

- [x] `ProfileTabViewModelTest`: section visibility for shipper vs carrier (`ProfileTabVisibility` + VM tests).
- [x] `ProfileTabViewModelTest`: verification card visibility for `basic`/`verified`/`premium` (`shouldShowVerificationCard` + tests).
- [ ] `ProfileTabViewModelTest`: profile load dedupe and force-refresh rules — **deferred** (in-flight guard in `ProfileTabViewModel` + `ProfileRepository` cache; add focused coroutine test when stabilizing).
- [x] Avatar cache-buster: `ProfileTabViewModelTest` covers `onAvatarChanged`.
- [ ] `ProfileTabNavigationTest`: each menu row opens the expected route/sheet — **deferred** (most targets placeholders or payments hub; follow-up with navigation graph / route assertions).
- [ ] `ProfileTabLogoutTest`: logout clears session and returns to auth root — **partial** (androidTest verifies `ProfileTabContent` invokes `onLogout`; end-to-end auth is covered by existing auth flows).
- [x] `ProfileTabUiTest` (androidTest): role-specific sections and logout callback.
- [ ] `ProfileTabLocalizationTest` (or lint gate): no hardcoded user-facing strings — **deferred** (all profile UI via `stringResource`; add custom lint/CI if desired).

## Exit gate for this spec

This spec is complete when:

1. Profile tab section order and role-gating match iOS behavior.
2. All menu entries route to implemented features or explicit placeholders with documented follow-up.
3. DS and localization rules are satisfied (no ad-hoc styling/hardcoded strings).
4. TDD checklist above is green (or deferred items explicitly documented in `IMPLEMENTATION-STATUS.md`).

