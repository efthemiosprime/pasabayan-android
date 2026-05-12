# 09 — Profile, carrier, consent, disclaimers

**Phase:** 6 | **Feature:** Profile | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

User profile CRUD, avatar upload, carrier profile management, consent preferences, disclaimer acknowledgments, account deletion, data export, verification levels, role management, and settings screen. Parity target: iOS [`ProfileAPIService.swift`](../../Pasabayan/Features/Profile/Services/ProfileAPIService.swift), [`ProfileModels.swift`](../../Pasabayan/Features/Profile/Models/ProfileModels.swift), [`CarrierProfile.swift`](../../Pasabayan/Features/Profile/Models/CarrierProfile.swift), [`User.swift`](../../Pasabayan/Features/Authentication/Models/User.swift).

---

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/profile` | Fetch user profile |
| PUT | `/api/profile` | Update profile (JSON) |
| POST | `/api/profile` | Create/update profile + avatar (multipart) |
| DELETE | `/api/profile/picture` | Remove profile picture |
| POST | `/api/profile/request-deletion` | Request account deletion |
| GET | `/api/profile/disclaimer-acknowledgments` | List disclaimer acknowledgments |
| POST | `/api/profile/disclaimer-acknowledgments` | Acknowledge a disclaimer |
| GET | `/api/profile/consent-preferences` | Fetch consent preferences |
| PUT | `/api/profile/consent-preferences` | Update consent preferences (scoped) |
| GET | `/api/profile/export-data` | Export user data (raw JSON) |
| GET | `/api/carrier/profile` | Fetch carrier profile |
| POST | `/api/carrier/profile` | Create carrier profile |
| PUT | `/api/carrier/profile` | Update carrier profile |
| GET | `/api/carrier/stats` | Fetch carrier statistics |
| POST | `/api/carrier/enable` | Enable carrier role |
| POST | `/api/carrier/toggle-status` | Toggle carrier active/inactive |

---

## Data models

### UserProfile

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Int` | Server-assigned, `Identifiable` |
| `fullName` | `String?` | Auto-filled from social auth if null |
| `deliveryAddress` | `String?` | Default shipping address |
| `profilePicture` | `String?` | URL path to avatar |
| `preferredContactMethod` | `String?` | `"phone"` · `"email"` · `"app_notification"` |
| `additionalInfo` | `Map<String, String>?` | Arbitrary KV (timezone, language, etc.) |
| `userId` | `Int?` | FK to User |
| `createdAt` | `String?` | ISO 8601 |
| `updatedAt` | `String?` | ISO 8601 |

Computed: `isProfileComplete` = `fullName != null && deliveryAddress != null`.

JSON keys (snake_case): `full_name`, `delivery_address`, `profile_picture`, `preferred_contact_method`, `additional_info`, `user_id`, `created_at`, `updated_at`.

### UpdateProfileRequest

| Field | Type | Notes |
|-------|------|-------|
| `fullName` | `String?` | Skip encoding if null/empty |
| `deliveryAddress` | `String?` | Skip encoding if null/empty |
| `profilePicture` | `String?` | Skip encoding if null/empty |
| `preferredContactMethod` | `String?` | Skip encoding if null/empty |
| `additionalInfo` | `Map<String, String>?` | |
| `homeCityId` | `Int?` | Encode explicitly as `null` when absent (do not omit key) |

JSON keys: `full_name`, `delivery_address`, `profile_picture`, `preferred_contact_method`, `additional_info`, `home_city_id`.

### ProfileResponse (API wrapper)

```
{
  "success": true,
  "message": "...",
  "data": {
    "profile": { ...UserProfile },
    "home_city_id": 5,
    "is_complete": true
  }
}
```

### ContactMethod enum

| Case | Raw value | Display |
|------|-----------|---------|
| `PHONE` | `"phone"` | Phone |
| `EMAIL` | `"email"` | Email |
| `APP_NOTIFICATION` | `"app_notification"` | App Only |

---

### CarrierProfile

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Int?` | |
| `userId` | `Int` | |
| `maxWeightCapacityKg` | `Double` | 0–999.99; API may return as string |
| `maxSpaceCapacityLiters` | `Double` | 0–999.99; optional (can be 0); API may return as string |
| `preferredPickupCityId` | `Int?` | |
| `preferredPickupCity` | `PreferredPickupCity?` | Nested object |
| `preferredPackageTypes` | `List<String>?` | API may return comma-separated string |
| `restrictedItems` | `List<String>?` | API may return comma-separated string |
| `defaultPricePerKg` | `Double` | Required; API may return as string |
| `availableRoutes` | `List<AvailableRoute>?` | API may return as string "City1 → City2" |
| `carrierStatus` | `String` | `"active"` · `"inactive"` |
| `insuranceCoverageAmount` | `Double?` | CAD; API may return as string |
| `governmentIdVerified` | `Boolean` | |
| `bio` | `String?` | |
| `rating` | `Double?` | |
| `totalTrips` | `Int?` | |
| `totalEarnings` | `Double?` | |
| `createdAt` | `String` | ISO 8601 |
| `updatedAt` | `String` | ISO 8601 |
| `setupRequired` | `Boolean?` | True if initial setup incomplete |
| `isDetailedProfile` | `Boolean?` | |

Computed: `isActive` = `carrierStatus == "active"`, `vehicleCapacityDisplay` = `"50kg / 100L"`.

JSON keys: `max_weight_capacity_kg`, `max_space_capacity_liters`, `preferred_pickup_city_id`, `preferred_pickup_city`, `preferred_package_types`, `restricted_items`, `default_price_per_kg`, `available_routes`, `carrier_status`, `insurance_coverage_amount`, `government_id_verified`, `total_trips`, `total_earnings`, `created_at`, `updated_at`, `setup_required`, `is_detailed_profile`.

### PreferredPickupCity

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Int` | |
| `name` | `String` | |
| `stateCode` | `String` | Defaults to `""` if absent |
| `lat` | `Double?` | API may return as string |
| `lng` | `Double?` | API may return as string |

Computed: `displayName` = `"Montreal, QC"` or `"Montreal"` (if stateCode empty).

JSON keys: `state_code`.

### AvailableRoute

| Field | Type | JSON key |
|-------|------|----------|
| `origin` | `String` | `from` |
| `destination` | `String` | `to` |

### CreateCarrierProfileRequest

| Field | Type | Notes |
|-------|------|-------|
| `preferredPickupCityId` | `Int?` | Encode explicitly as `null` to clear |
| `maxWeightCapacityKg` | `Double` | Required |
| `maxSpaceCapacityLiters` | `Double` | |
| `preferredPackageTypes` | `List<String>?` | |
| `restrictedItems` | `List<String>?` | |
| `defaultPricePerKg` | `Double` | Required |
| `availableRoutes` | `List<AvailableRoute>?` | |
| `insuranceCoverageAmount` | `Double?` | |
| `bio` | `String?` | |

JSON keys: `preferred_pickup_city_id`, `max_weight_capacity_kg`, `max_space_capacity_liters`, `preferred_package_types`, `restricted_items`, `default_price_per_kg`, `available_routes`, `insurance_coverage_amount`.

### CarrierProfile API responses

```
CarrierProfileResponse  { message: String, data: CarrierProfile }
CarrierProfilesResponse { success: Bool, message: String, data: List<CarrierProfile> }
CarrierEnableResponse   { message: String? }
```

---

### CarrierStats

```
CarrierStats {
  deliveries: CarrierDeliveries
  ratings:    CarrierRatings
  earnings:   CarrierEarnings
  profileComplete: Boolean          // "profile_complete"
}
```

**CarrierDeliveries**

| Field | Type | JSON key |
|-------|------|----------|
| `totalTrips` | `Int` | `total_trips` |
| `activeTrips` | `Int` | `active_trips` |
| `completedTrips` | `Int` | `completed_trips` |
| `totalMatches` | `Int` | `total_matches` |
| `completedMatches` | `Int` | `completed_matches` |
| `successRate` | `Double` | `success_rate` — API may return as string |

**CarrierRatings**

| Field | Type | JSON key |
|-------|------|----------|
| `averageRating` | `String` | `average_rating` — API returns `"0.00"` |
| `totalRatings` | `Int` | `total_ratings` |
| `responseTimeHours` | `Double` | `response_time_hours` — API may return as string or double |
| `reliabilityScore` | `Int` | `reliability_score` |

**CarrierEarnings**

| Field | Type | JSON key |
|-------|------|----------|
| `totalEarnings` | `Double` | `total_earnings` |
| `monthlyEarnings` | `Double` | `monthly_earnings` |
| `averagePerDelivery` | `Double` | `average_per_delivery` |
| `pendingPayments` | `Double` | `pending_payments` |
| `topRoutes` | `List<TopRoute>` | `top_routes` |

**TopRoute**: `route: String`, `trips: Int`, `potentialEarnings: Double` (`potential_earnings` — may arrive as string).

**CarrierStatsResponse**: `{ success: Bool, message: String, data: CarrierStats }`.

---

### CarrierStatusData

| Field | Type | JSON key |
|-------|------|----------|
| `isActiveCarrier` | `Boolean` | `is_active_carrier` |
| `carrierStatus` | `String` | `carrier_status` |
| `hasCarrierProfile` | `Boolean` | `has_carrier_profile` |
| `profileRecommended` | `Boolean` | `profile_recommended` |
| `userTypes` | `List<String>` | `user_types` — API may return as array, dictionary, or numeric IDs |

---

### UserRole enum

| Case | Raw value |
|------|-----------|
| `SHIPPER` | `"shipper"` |
| `CARRIER` | `"carrier"` |

Properties: localized `displayName`, `icon`, `description`, `capabilities: List<String>`, `oppositeRole`.

Role persistence: saved to local storage (SharedPreferences / DataStore) with key `"user_preferred_role"`, loaded on app launch, synced when app backgrounds.

---

## Verification levels

| Level | Badge | Meaning |
|-------|-------|---------|
| `"basic"` | None | Fresh signup with OAuth, no phone verification |
| `"verified"` | Checkmark | Phone verified via SMS |
| `"premium"` | Star | ID + selfie verified |

**User model fields**: `verificationLevel: String` (defaults to `"basic"`), `phoneVerified: Boolean`, `phoneVerifiedAt: String?`.

**UI**: `VerificationStatusView` shown in profile tab when level is `"basic"` or `"verified"` — hidden for `"premium"`. Displays upgrade CTA.

Computed: `effectiveVerificationLevel` (normalized), `isVerified`, `verificationBadgeText`, `verificationBadgeColor`, `verificationDescription` — all localized.

---

## Multipart profile upload — POST `/api/profile`

From iOS `uploadProfileWithPicture`:

| Field name | Type | Notes |
|-----------|------|-------|
| `profile_picture` | file | Filename: `avatar.jpg`; MIME detected: `image/png`, `image/gif`, or `image/jpeg` |
| `full_name` | text | Only if provided |
| `delivery_address` | text | Only if provided |
| `preferred_contact_method` | text | Defaults to `"app_notification"` |
| `additional_info` | text | JSON string, only if provided |

**Image handling rules:**
- Compress to max **512 px** dimension, **0.7** JPEG quality (prevents 413 errors).
- MIME detection: PNG header → `image/png`, GIF header → `image/gif`, else `image/jpeg`.
- Multipart boundary: `Boundary-{UUID}`.
- **Use POST, not PUT** — iOS URLSession strips body from PUT multipart; API expects POST for consistency.
- Avatar cache busting: after upload or delete, generate a new UUID-based cache key to force UI refresh (equivalent to iOS `avatarCacheBuster`).

**Picture-only upload** (no other fields): same endpoint, only `profile_picture` field.

---

## Consent preferences

### Model

```kotlin
data class ConsentPreferencesData(
    val pushNotifications: Boolean,      // "push_notifications"
    val locationTracking: Boolean,       // "location_tracking"
    val analytics: Boolean,              // "analytics"
    val marketingCommunications: Boolean  // "marketing_communications"
)
```

Response wrapper: `ConsentPreferencesResponse { success: Boolean, data: ConsentPreferencesData? }`.

### PUT `/api/profile/consent-preferences`

**Scoped update** — only send the changed key(s), not all four:

```json
{ "push_notifications": true }
```

### Confirmation alerts

| Preference | Requires confirmation to disable? |
|------------|----------------------------------|
| `push_notifications` | Yes — "You won't receive push notifications" |
| `location_tracking` | Yes — "We won't track your location" |
| `analytics` | No |
| `marketing_communications` | No |

### Revert on failure

If the API call fails, revert the toggle to its previous state in the UI.

### System permission

If the user enables `push_notifications`, request OS push permission (Android: `POST_NOTIFICATIONS` runtime permission).

### Onboarding consent flow

`ConsentOnboardingScreen` — shown **once** after first login:
- All toggles default to **OFF**.
- User can proceed without enabling anything (consent is not a blocker).
- Calls `PUT /api/profile/consent-preferences` with all 4 keys.
- Continues onboarding even on API failure.

---

## Disclaimer acknowledgments

### Types

| Disclaimer type | When shown |
|-----------------|-----------|
| `"carrier_trip"` | Before creating first trip as carrier |
| `"shipper"` | Before first shipper action |

### Models

**Request**: `DisclaimerAcknowledgmentRequest { disclaimerType: String }` → JSON key: `disclaimer_type`.

**Response (single)**:
```json
{
  "success": true,
  "data": { "disclaimer_type": "carrier_trip", "acknowledged_at": "2026-03-01T12:00:00Z" }
}
```

**Response (list)** — GET `/api/profile/disclaimer-acknowledgments`:
```json
{
  "success": true,
  "data": {
    "carrier_trip": { "acknowledged_at": "2026-03-01T12:00:00Z" },
    "shipper": null
  }
}
```

### Local stores

`CarrierDisclaimerStore` and `ShipperDisclaimerStore` — persist whether each disclaimer sheet has been shown/acknowledged locally (DataStore or SharedPreferences). UI sheets: `CarrierTripDisclaimerSheet`, `ShipperDisclaimerSheet`.

---

## Account deletion

### POST `/api/profile/request-deletion`

**Request body**:
```json
{ "confirm": true, "reason": "No longer using the app" }
```
`reason` is optional (nullable).

**Expected HTTP status**: 202 Accepted.

**Response**:
```json
{
  "success": true,
  "message": "Your account deletion request has been submitted.",
  "data": {
    "request_id": 42,
    "status": "pending",
    "requested_at": "2026-03-01T12:00:00.123456Z"
  }
}
```

`requested_at` — ISO 8601, may or may not include fractional seconds. Handle both.

**Duplicate request**: API may return a message indicating already requested.

### UI flow

1. User taps **"Disable Account"** (red, destructive).
2. **Confirmation alert**: title + message with Cancel / Disable (destructive).
3. Call API. Show loading state; disable button during request.
4. **Result alert**: show `response.message`.
5. On success: clear transient cache → call logout → dismiss settings screen.
6. On failure: show error message, do not sign out.

---

## Data export

### GET `/api/profile/export-data`

- Returns **raw JSON** (`ByteArray` / `Data`), not a typed model.
- Requires manual auth header (Bearer token).
- Status code check: 200–299 range.

### UI flow

1. User taps **"Download My Data"** with download icon.
2. Show `CircularProgressIndicator` during fetch; disable button.
3. On success: pretty-print JSON → write to temp file (`pasabayan-data-export.json`) → open Android share sheet (`Intent.ACTION_SEND` / `FileProvider`).
4. On failure: show localized error alert.

---

## Carrier enable and toggle

### POST `/api/carrier/enable`

- No request body.
- Called after carrier profile creation.
- **Idempotent** — fails silently if carrier role already enabled.
- Response: `CarrierEnableResponse { message: String? }`.

### POST `/api/carrier/toggle-status`

- No request body.
- Returns `CarrierStatusData` (see model above).
- Toggles between `"active"` and `"inactive"`.

### POST `/api/carrier/profile` — 409 handling

If the API returns **409 Conflict** (carrier profile already exists), fall back to fetching the existing profile with GET instead of showing an error.

---

## Smart defaults and pre-filling

| Field | Default logic |
|-------|---------------|
| `fullName` | Auto-fill from social auth (Google/Facebook) name if null in DB |
| `preferredContactMethod` | Email (if verified) → Phone (if verified) → `"app_notification"` |
| `additionalInfo.timezone` | System timezone (`TimeZone.currentSystemDefault()`) |
| `additionalInfo.language` | System language (`Locale.getDefault()`) |

---

## Validation rules

| Field | Rule |
|-------|------|
| `fullName` | Required for `isProfileComplete` (non-null, non-empty) |
| `deliveryAddress` | Required for `isProfileComplete` |
| `preferredContactMethod` | Required by API; default `"app_notification"` |
| `maxWeightCapacityKg` | 0–999.99, required |
| `maxSpaceCapacityLiters` | 0–999.99, optional (can be 0) |
| `defaultPricePerKg` | Required, > 0 |
| `preferredPickupCityId` | Optional; send explicit `null` to clear |

---

## Resilient decoding

The API returns inconsistent types. Android must handle:

| Scenario | Example |
|----------|---------|
| `rating` as String or Double | `"4.5"` or `4.5` |
| `userTypes` as array, dictionary, or numeric IDs | `["shipper"]`, `{"1": "shipper"}`, `[1, 2]` |
| Weight/space/price fields as String or Double | `"50.0"` or `50.0` |
| `preferredPackageTypes` / `restrictedItems` as comma-separated String or array | `"Electronics,Documents"` or `["Electronics", "Documents"]` |
| `availableRoutes` as String or structured array | `"Montreal → Toronto"` or `[{"from": "Montreal", "to": "Toronto"}]` |
| `lat` / `lng` as String or Double | `"45.5017"` or `45.5017` |
| ISO 8601 dates with/without fractional seconds | `"2026-03-01T12:00:00Z"` or `"2026-03-01T12:00:00.123456Z"` |
| `successRate` / `responseTimeHours` as String or Double | `"0.95"` or `0.95` |

Use custom `kotlinx.serialization` deserializers or `JsonTransformingSerializer` to handle these.

---

## Caching strategy

- GET requests (`/api/profile`, `/api/carrier/profile`, `/api/carrier/stats`) support cached responses with `forceRefresh: Boolean` parameter.
- `getHomeCityId` defaults to `forceRefresh = true`.
- Cache **invalidated** after successful PUT/POST on the same resource.
- Avatar: UUID-based cache buster after upload/delete.

---

## Settings screen

### Sections (iOS parity)

| Section | Items |
|---------|-------|
| **Preferences** | Preferred role (shipper/carrier), Language, Currency (CAD/USD/PHP), Home city (autocomplete, 10-minute cooldown after change) |
| **Carrier Preferences** | Opens `CarrierPreferencesFormSheet` — weight, space, pickup city, transport method |
| **Privacy Preferences** | Opens `PrivacyPreferencesScreen` — 4 consent toggles with confirmation alerts |
| **Storage & Data** | Clear app cache, Export user data |
| **Account** | Disable/delete account (red, destructive), Sign out |
| **About** | App version, Build number, Terms & Privacy links |

### Settings — Home city cooldown

After changing home city, a **10-minute cooldown** prevents further city changes. Display a countdown timer in the city picker row while active. Persist cooldown timestamp in DataStore (`home_city_cooldown_until`).

### Settings — Currency selection

| Currency | Code | Symbol |
|----------|------|--------|
| Canadian Dollar | `CAD` | `$` |
| US Dollar | `USD` | `$` |
| Philippine Peso | `PHP` | `₱` |

Persist to DataStore (`preferred_currency`). Used for display formatting only — API always stores amounts in the currency of the transaction.

### Settings — Clear cache

On "Clear Cache" tap:
1. Delete OkHttp cache directory
2. Clear in-memory image cache (Coil)
3. Clear DataStore cached responses (if any)
4. Show summary: "Cleared X files, Y MB freed" (or similar)

### Settings — Sign out

Clears tokens, resets navigation to auth screen. Same flow as 401 sign-out in [02-auth-session.md](02-auth-session.md).

---

## Navigation flow

```
ProfileScreen
├── Personal Info → EditUserProfileSheet
├── Verification → VerificationStatusView
├── Vehicle Info → EditCarrierProfileSheet
├── Shipping Addresses → (placeholder)
├── Payment Methods → PaymentMethodsScreen
├── Transaction History → TransactionHistoryScreen
├── Receipts → ReceiptListScreen
├── Payout Setup → PayoutSetupScreen
├── Help Center → HelpCenterScreen
├── Settings → SettingsScreen
│   ├── Carrier Preferences → CarrierPreferencesFormSheet
│   └── Privacy Preferences → PrivacyPreferencesScreen
├── Terms & Privacy → TermsAndPrivacyScreen
├── Delivery History → DeliveryHistoryScreen
└── Package History → PackageHistoryScreen
```

---

## UI (iOS reference)

[`ProfileView`](../../Pasabayan/Features/Profile/Views/ProfileView.swift), [`EditUserProfileSheet`](../../Pasabayan/Features/Profile/Views/EditUserProfileSheet.swift), [`EditCarrierProfileSheet`](../../Pasabayan/Features/Profile/Views/EditCarrierProfileSheet.swift), [`CarrierProfileSetupView`](../../Pasabayan/Features/Profile/Views/CarrierProfileSetupView.swift), [`CarrierPreferencesFormSheet`](../../Pasabayan/Features/Profile/Views/CarrierPreferencesFormSheet.swift), [`SettingsView`](../../Pasabayan/Features/Profile/Views/SettingsView.swift), [`PrivacyPreferencesView`](../../Pasabayan/Features/Profile/Views/PrivacyPreferencesView.swift), [`VerificationStatusView`](../../Pasabayan/Features/Profile/Views/VerificationStatusView.swift), [`ConsentOnboardingView`](../../Pasabayan/Features/Onboarding/Views/ConsentOnboardingView.swift).

Profile header components: `UserProfileHeader` (avatar + name + role chip + verification badge), `ProfileStatsSection` (role-specific stats), `ProfileActionsSection` (logout + version).

---

## Quirks

- iOS uses **POST** (not PUT) for multipart profile update — URLSession strips body from PUT multipart. Android should also use POST for API consistency.
- `POST /carrier/profile` returns **409** if profile already exists — handle by fetching existing profile.
- `POST /carrier/enable` is **idempotent** — call after every carrier profile save; fail silently if already enabled.
- `ConsentOnboardingScreen` continues even on API failure — consent is not a gate.
- Account deletion expects HTTP **202**, not 200.

---

## TDD checklist

- [x] `ProfileApiServiceTest` — all 16 endpoint calls, request serialization, response deserialization → `ProfileRepositoryImplTest.kt`
- [x] `ProfileViewModelTest` — load profile, update profile, smart pre-filling logic → `EditUserProfileViewModelTest.kt` + `ProfileTabViewModelTest.kt`
- [x] `ProfileViewModelAvatarRefreshTest` — upload, delete, cache buster rotation → folded into `EditUserProfileViewModelTest.kt`
- [x] `AccountDeletionTest` — request body encoding (with/without reason), response decoding, date formats (with/without fractional seconds), duplicate request handling → `AccountManagementViewModelTest.kt`
- [x] `CarrierProfileTest` — CRUD, 409 conflict fallback, enable idempotency, toggle status → `EditCarrierProfileViewModelTest.kt` + `ProfileRepositoryImplTest.kt`
- [x] `CarrierStatsDecodingTest` — resilient decoding (string↔double, nested sub-models) → `core/network/.../profile/CarrierStatsDecodingTest.kt`
- [x] `ConsentPreferencesTest` — scoped update, revert on failure, confirmation flow → `PrivacyPreferencesViewModelTest.kt` + `ConsentOnboardingViewModelTest.kt`
- [x] `DisclaimerAcknowledgmentTest` — post acknowledgment, list acknowledgments, local store persistence → `DisclaimerSyncServiceTest.kt`
- [x] `DataExportTest` — raw data fetch, auth header, error handling → folded into `AccountManagementViewModelTest.kt`
- [x] `RoleViewModelTest` — role switching, persistence to DataStore, reload on launch → covered by `DashboardViewModelTest.kt` (role switching lives in the dashboard VM)
- [x] `VerificationLevelTest` — normalization, JSON round-trip, isVerified/isPremium matrix → `core/domain/.../enum/VerificationLevelTest.kt`. Badge text/color stays an `androidTest` follow-up (UI layer).
- [x] `ResilientDecodingTest` — all string↔double/array↔string edge cases from table above → `FlexibleDecodersTest.kt` (22 cases)
