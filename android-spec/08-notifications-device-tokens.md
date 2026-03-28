# 08 — Notifications and device tokens

**Phase:** 5 | **Feature:** Notifications | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`NotificationAPIService.swift`](../../Pasabayan/Features/Notifications/Services/NotificationAPIService.swift) — note: uses **custom** `URLSession` path for some calls; status handling may differ from `APIService` (decode-first).

Source models: [`NotificationModels.swift`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift). In-app UI + routing: [`ComprehensiveNotificationsView.swift`](../../Pasabayan/Features/Notifications/Views/ComprehensiveNotificationsView.swift). Push / tab routing: [`NotificationManager.swift`](../../Pasabayan/Features/Notifications/Services/NotificationManager.swift).

---

## Models

### `PushNotification` — primary notification model

Wire format is snake_case. All fields from iOS `PushNotification` struct.

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `id` | `Int` | `id` | Required |
| `title` | `String` | `title` | Required |
| `body` | `String` | `body` | Required |
| `type` | `String` | `type` | Raw notification type string; maps to `NotificationType` enum |
| `data` | `NotificationData?` | `data` | Optional nested payload |
| `recipientRole` | `String?` | `recipient_role` | `"carrier"` or `"shipper"` |
| `sentAt` | `String` | `sent_at` | ISO8601 |
| `readAt` | `String?` | `read_at` | Mutable — set on mark-as-read |
| `clickedAt` | `String?` | `clicked_at` | Mutable |
| `isRead` | `Boolean` | `is_read` | Mutable |
| `createdAt` | `String` | `created_at` | ISO8601 |

**Computed properties:**
- `notificationType: NotificationType` — parse `type` string; fall back to `UNKNOWN` (iOS uses `.test`; Android should use a dedicated `UNKNOWN` case).
- `timeAgo: String` — relative time from `createdAt` ("5 minutes ago", "2 hours ago", "3 days ago", "Just now"). Use `DateFormatting` from `00-architecture.md`. **Must be localized** — see localization strings below.

---

### `NotificationType` — complete enum (implement all)

Backend `type` string must map to these cases. Unknown types fall back to `UNKNOWN`.

| Raw value (`type`) | Enum case | Material Icon | Color | Typical navigation |
|--------------------|-----------|---------------|-------|--------------------|
| `match_request` | `MATCH_REQUEST` | `Inventory2` | Orange | Match / booking detail |
| `match_accepted` | `MATCH_ACCEPTED` | `CheckCircle` | Green | Match detail |
| `match_declined` | `MATCH_DECLINED` | `Cancel` | Red | Match detail |
| `chat_message` | `CHAT_MESSAGE` | `Chat` | Blue | Conversation |
| `delivery_picked_up` | `DELIVERY_PICKED_UP` | `LocalShipping` | Purple | Match detail |
| `delivery_in_transit` | `DELIVERY_IN_TRANSIT` | `DirectionsCar` | Purple | Match detail |
| `delivery_delivered` | `DELIVERY_DELIVERED` | `VerifiedUser` | Purple | Match detail |
| `match_auto_cancelled` | `MATCH_AUTO_CANCELLED` | `PauseCircle` | Orange | Match detail |
| `payment_received` | `PAYMENT_RECEIVED` | `CreditCard` | Green | Transaction |
| `payment_released` | `PAYMENT_RELEASED` | `MonetizationOn` | Green | Transaction |
| `rating_received` | `RATING_RECEIVED` | `Star` | Yellow | Ratings / match |
| `premium_approved` | `PREMIUM_APPROVED` | `VerifiedUser` | Green | Profile / verification |
| `premium_rejected` | `PREMIUM_REJECTED` | `Shield` | Red | Profile / verification |
| `test` | `TEST` | `Notifications` | Gray | Debug / list |
| `payout_completed` | `PAYOUT_COMPLETED` | `AccountBalance` | Green | Payouts / Stripe |
| `payout_scheduled` | `PAYOUT_SCHEDULED` | `CalendarMonth` | Orange | Payouts |
| `refund_processed` | `REFUND_PROCESSED` | `Replay` | Blue | Transaction / refund |
| `tip_received` | `TIP_RECEIVED` | `CardGiftcard` | Orange | Payment |
| `payment_auto_charge_failed` | `PAYMENT_AUTO_CHARGE_FAILED` | `Warning` | Red | Match / payment retry |
| `counter_offer` | `COUNTER_OFFER` | `SwapHoriz` | Purple | Match / counter-offer flow |
| _(unknown)_ | `UNKNOWN` | `Notifications` | Gray | Notification list |

Each type exposes:
- `displayName: String` — localized via `stringResource` (see localization section)
- `icon: ImageVector` — Material Icon mapped from iOS SF Symbols above
- `color: Color` — from `PasabayanColors` semantic tokens per [14-design-system.md](14-design-system.md)

---

### `NotificationData` — optional payload fields (decode all)

All fields optional. Snake_case on wire — see `CodingKeys` in [`NotificationModels.swift`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift).

| Field | Type | Wire key | Use |
|-------|------|----------|-----|
| `matchId` | `Int?` | `match_id` | Routing to match |
| `conversationId` | `Int?` | `conversation_id` | Routing to chat |
| `messageId` | `Int?` | `message_id` | Routing to chat message |
| `requesterId` | `Int?` | `requester_id` | Display / deep link |
| `requesterName` | `String?` | `requester_name` | Display |
| `carrierId` | `Int?` | `carrier_id` | Display / deep link |
| `carrierName` | `String?` | `carrier_name` | Display |
| `shipperId` | `Int?` | `shipper_id` | Display / deep link |
| `shipperName` | `String?` | `shipper_name` | Display |
| `transactionId` | `Int?` | `transaction_id` | Payment routing |
| `amount` | `Double?` | `amount` | Payment display |
| `currency` | `String?` | `currency` | Payment display |
| `ratingId` | `Int?` | `rating_id` | Rating routing |
| `ratingValue` | `Int?` | `rating_value` | Rating display |
| `raterId` | `Int?` | `rater_id` | Rating display |
| `raterName` | `String?` | `rater_name` | Rating display |
| `screen` | `String?` | `screen` | Generic routing hint |
| `reason` | `String?` | `reason` | Generic routing hint |
| `tipAmount` | `Double?` | `tip_amount` | Tip display |
| `tipperId` | `Int?` | `tipper_id` | Tip display |
| `tipperName` | `String?` | `tipper_name` | Tip display |
| `refundAmount` | `Double?` | `refund_amount` | Refund display |
| `scheduledAt` | `String?` | `scheduled_at` | Payout scheduling |
| `recipientRole` | `String?` | `recipient_role` | Filter by role |
| `newPrice` | `Double?` | `new_price` | Counter-offer |
| `originalPrice` | `Double?` | `original_price` | Counter-offer |
| `priceDifference` | `Double?` | `price_difference` | Counter-offer |
| `direction` | `String?` | `direction` | Counter-offer ("up"/"down") |
| `counterOffererName` | `String?` | `counter_offerer_name` | Counter-offer |
| `counterOffererId` | `Int?` | `counter_offerer_id` | Counter-offer |
| `counterOfferRound` | `Int?` | `counter_offer_round` | Counter-offer |
| `initiatedBy` | `String?` | `initiated_by` | Counter-offer (role) |
| `remainingCounterOffers` | `Int?` | `remaining_counter_offers` | Counter-offer |
| `canCounterOffer` | `Boolean?` | `can_counter_offer` | Counter-offer |

---

## Endpoints

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/device-tokens` | bearer | Register FCM token |
| DELETE | `/device-tokens/{encodedToken}` | bearer | Unregister token; **URL-encode** the FCM token (contains `/`, `+`, `=`) |
| GET | `/notifications` | bearer | Notification history (paginated) |
| GET | `/notifications/unread-count` | bearer | Unread badge count |
| PUT | `/notifications/{id}/read` | bearer | Mark single as read |
| PUT | `/notifications/mark-all-read` | bearer | Mark all as read |
| POST | `/device-tokens/test` | bearer | Debug: test push delivery |

### Timeout override

iOS uses a custom `URLSession` with **15s connect / 30s resource** timeout for notification endpoints. On Android, create a dedicated OkHttp client (or interceptor) for `NotificationApi` with:
- Connect timeout: **15 seconds**
- Read timeout: **30 seconds**

---

### POST `/device-tokens` — request body

```json
{
  "token": "fcm_token_string",
  "platform": "android",
  "app_version": "1.0.0",
  "device_model": "Pixel 8",
  "os_version": "14",
  "device_name": "User's Phone"
}
```

- `platform` must be `"android"` (iOS sends `"ios"`)
- `device_name` is optional
- `app_version` from `BuildConfig.VERSION_NAME`
- `device_model` from `Build.MODEL`
- `os_version` from `Build.VERSION.RELEASE`

### POST `/device-tokens` — response

```json
{
  "success": true,
  "message": "Device token registered",
  "data": {
    "device_token": {
      "id": 123,
      "platform": "android",
      "is_active": true,
      "created_at": "2026-03-27T10:00:00Z"
    }
  }
}
```

Android DTOs: `DeviceTokenResponseJson` → `DeviceTokenDataJson` → `DeviceTokenInfoJson` (`id: Int`, `platform: String`, `isActive: Boolean`, `createdAt: String`).

### POST `/device-tokens` — 403 consent error

```json
{
  "success": false,
  "message": "Consent required for push_notifications",
  "consent_required": "push_notifications"
}
```

On 403 with `consent_required == "push_notifications"`:
1. Call `PUT /profile/consent-preferences` with `{"push_notifications": true}`
2. Retry device token registration
3. Store consent opt-in state in preferences (`PushNotificationsConsentIntent`)

See iOS [`DeviceTokenRegisterResponseParser.swift`](../../Pasabayan/Features/Notifications/Services/DeviceTokenRegisterResponseParser.swift) for full status code handling (200/201 → parse, 403 → consent check, else → server error).

---

### DELETE `/device-tokens/{encodedToken}`

URL-encode the FCM token before inserting in path. Response: `{"success": true, "message": "..."}`.

---

### GET `/notifications` — query params

| Param | Type | Notes |
|-------|------|-------|
| `page` | Int | Pagination (default 1) |
| `unread_only` | Bool | Optional filter |
| `role` | String | Optional: `"shipper"` or `"carrier"` |

### GET `/notifications` — response

```json
{
  "success": true,
  "data": {
    "notifications": [ /* PushNotification[] */ ],
    "pagination": {
      "current_page": 1,
      "last_page": 5,
      "per_page": 15,
      "total": 75
    }
  }
}
```

Android DTOs: `NotificationHistoryResponseJson` → `NotificationHistoryDataJson` (contains `notifications: List<PushNotificationJson>` + `pagination: PaginationInfoJson`).

`PaginationInfoJson`: `currentPage`, `lastPage`, `perPage`, `total`. **Note:** This is a notification-specific pagination shape (not the generic `PaginatedResponse<T>` from `00-architecture.md`).

---

### GET `/notifications/unread-count` — multi-format response

The backend may return **multiple shapes**. Parse with fallback chain (match iOS [`UnreadCountResponse`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift)):

| Priority | Shape | Example |
|----------|-------|---------|
| 1 | Canonical | `{"success": true, "data": {"unread_count": 5}}` |
| 2 | Data as bare int | `{"success": true, "data": 3}` |
| 3 | Flat at root | `{"unread_count": 5}` or `{"unreadCount": 5}` or `{"count": 5}` |
| 4 | Bare number | `5` |

Wire key is `unread_count` (snake_case). Try keys: `unread_count`, `unreadCount`, `count`.

Optional query: `?role=carrier` or `?role=shipper` for per-role counts.

---

### PUT `/notifications/{id}/read` — response

```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "notification_id": 123,
    "read_at": "2026-03-27T10:05:00Z"
  }
}
```

---

### PUT `/notifications/mark-all-read` — response

```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": {
    "marked_count": 12
  }
}
```

**Ambiguity:** Backend may return `marked_count` **or** `updated_count`. Parse with fallback (try `marked_count` first, then `updated_count`). Match iOS `MarkAllReadData`.

---

### POST `/device-tokens/test` — response

```json
{
  "success": true,
  "message": "Test notification sent"
}
```

---

## Device token lifecycle

### Registration flow (auth-gated)

1. **FCM token received** — `FirebaseMessagingService.onNewToken(token)` stores token in SharedPreferences under `fcm_device_token` key.
2. **Check auth** — if no bearer token available, **defer** registration (do not call API).
3. **After login** — call `registerStoredFCMTokenIfNeeded()` which reads stored FCM token and registers with backend.
4. **On success** — load unread count.
5. **On 403 consent error** — execute consent retry flow (see above).

### Unregistration (logout)

On logout, call `DELETE /device-tokens/{encodedToken}` with stored FCM token, then clear the stored token from preferences.

iOS reference: `NotificationManager.unregisterDeviceToken()`.

### Lifecycle summary

| Event | Action |
|-------|--------|
| `onNewToken(token)` | Store in prefs; if authenticated, register immediately |
| After login success | `registerStoredFCMTokenIfNeeded()` |
| On logout | `unregisterDeviceToken()` → DELETE, clear stored token |
| On consent 403 | Auto-consent → retry registration |

---

## Per-role unread counts

iOS maintains **three** published unread counts:

| State field | Source |
|-------------|--------|
| `unreadCount` | `GET /notifications/unread-count` (no role filter) — total |
| `carrierUnreadCount` | `GET /notifications/unread-count?role=carrier` |
| `shipperUnreadCount` | `GET /notifications/unread-count?role=shipper` |

`loadRoleUnreadCounts()` fires both role-specific calls in parallel.

### Android ViewModel state

```kotlin
data class NotificationUiState(
    val unreadCount: Int = 0,
    val carrierUnreadCount: Int = 0,
    val shipperUnreadCount: Int = 0,
    val notifications: List<PushNotification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pagination: PaginationInfo? = null,
)
```

### Badge count updates

- **App launcher badge:** Use `ShortcutManagerCompat.setDynamicShortcuts()` or `BadgeDrawable` on `BottomNavigationView` for per-tab badges.
- **Per-tab badges:** Carrier and shipper dashboard tabs show their respective `carrierUnreadCount` / `shipperUnreadCount` on the notification bell icon.
- **On mark-as-read (single):** Optimistically decrement `unreadCount` by 1, decrement the matching role count, update local `isRead`/`readAt`, then re-sync role counts from backend.
- **On mark-all-read:** Set all counts to 0, update all local notifications to `isRead = true`.

iOS reference: `UIApplication.shared.applicationIconBadgeNumber` + `decrementedRoleCounts()` in `NotificationManager`.

---

## Push notification routing

### Push tap handling (FCM → navigation)

When user taps a push notification, FCM delivers `remoteMessage.data` to the app. Parse into route data and navigate.

### `PushNotificationRoute` — route data model

Extracted from push payload `data` map. All fields optional.

| Field | Type | Payload key |
|-------|------|-------------|
| `matchId` | `Int?` | `match_id` |
| `conversationId` | `Int?` | `conversation_id` |
| `transactionId` | `Int?` | `transaction_id` |
| `ratingId` | `Int?` | `rating_id` |
| `screen` | `String?` | `screen` |
| `reason` | `String?` | `reason` |
| `recipientRole` | `String?` | `recipient_role` |
| `amount` | `Double?` | `amount` |
| `tipAmount` | `Double?` | `tip_amount` |
| `refundAmount` | `Double?` | `refund_amount` |
| `currency` | `String?` | `currency` |
| `tipperName` | `String?` | `tipper_name` |

### Routing logic (by notification type)

Match iOS `ComprehensiveNotificationsView.handleNotificationTap()` and `CarrierDashboardContent.routeFromNotification()`:

| Notification type(s) | Carrier route | Shipper route |
|----------------------|---------------|---------------|
| `matchRequest`, `matchAccepted`, `matchDeclined`, `matchAutoCancelled` | Booking detail (via `matchId`) | Shipper matches view |
| `deliveryPickedUp`, `deliveryInTransit`, `deliveryDelivered` | Switch to Matches tab | Shipper matches view |
| `chatMessage` | Conversations / specific conversation (via `conversationId`) | Same |
| `paymentReceived`, `paymentReleased`, `payoutCompleted`, `payoutScheduled`, `refundProcessed` | Transaction history | Same |
| `tipReceived` | Transaction detail (via `transactionId`), fallback to history | Same |
| `paymentAutoChargeFailed` | Payment methods view | Same |
| `ratingReceived` | Pending reviews | Same |
| `premiumApproved`, `premiumRejected` | Profile / verification section | Same |
| `counterOffer` | Match detail with counter-offer context (via route data) | Same |
| `test` | No-op | Same |

### Two notification event channels (iOS parity)

iOS posts two distinct `NotificationCenter` events:
1. **`.notificationRouted`** — from push tap (AppDelegate) — carries `type` + `route` dict
2. **`.navigateFromNotification`** — from in-app notification list tap — carries `matchId`, `conversationId`, etc.

Android equivalent: Use a shared `NotificationRouter` (or sealed `NavigationEvent` on a `SharedFlow`) that both the FCM handler and the notification list screen can emit to. The dashboard composable observes this flow and navigates accordingly.

### Push notification auto-mark-as-read

When the user taps a push notification that carries `notification_id` in the payload, automatically call `PUT /notifications/{id}/read`. iOS does this in `AppDelegate.didReceive`.

---

## Foreground notification display

When the app is in the **foreground** and a push arrives, iOS shows banner + badge + sound (`willPresent` → `[.banner, .badge, .sound]`).

### Android equivalent

1. Create a `NotificationChannel` (e.g. `"pasabayan_general"`) with importance `HIGH` for heads-up display.
2. In `FirebaseMessagingService.onMessageReceived()`, if the app is in foreground, build a `Notification` with:
   - Title and body from the FCM message
   - Icon and color per `NotificationType`
   - `PendingIntent` that routes to the correct screen (using the routing logic above)
   - Sound and vibration matching channel defaults
3. Post via `NotificationManagerCompat.notify()`.

---

## UI

### ComprehensiveNotificationsView (3 sections)

iOS reference: [`ComprehensiveNotificationsView.swift`](../../Pasabayan/Features/Notifications/Views/ComprehensiveNotificationsView.swift).

The notification screen is a **sheet** presented from the bell icon in the top bar. It has **three sections**:

#### Section 1: Notifications (push notifications from backend)

- Scrollable list of `NotificationCard` items
- Each card shows: type icon (colored circle background) + title + body + type badge + `timeAgo`
- Unread indicator: primary-colored dot + "mark as read" button
- Tap → `handleNotificationTap()` routing (see above)
- Menu actions: "Mark All as Read", "Clear All"

#### Section 2: Action Required (role-specific actionable items)

Computed from local ViewModel state, not from a dedicated API.

**Carrier actionable items:**

| Item | Condition | Navigation |
|------|-----------|------------|
| Pending booking requests | `matches.filter { status == shipperRequested }` | Booking detail sheet |
| Active deliveries needing updates | `matches.filter { status == confirmed \|\| pickedUp }` | Matches tab with "confirmed" filter |
| Inactive trips not visible | `trips.filter { status == planning }` | My Trips tab |

**Shipper actionable items:**

| Item | Condition | Navigation |
|------|-----------|------------|
| Pending carrier responses | `matches.filter { status == shipperRequested }` | Shipper matches sheet |
| Packages ready for pickup | `packageRequests.filter { status == matched }` | Package list sheet |

**Both roles:**

| Item | Condition | Navigation |
|------|-----------|------------|
| Unread messages | `chatViewModel.unreadTotal > 0` | Conversations view |
| Phone verification prompt | `verificationLevel == "basic"` | Phone verification flow |
| Premium upgrade prompt | `verificationLevel == "verified"` | Premium verification flow |

#### Section 3: Pending Reviews

- Reviews to leave (from ratings API — currently returns empty on iOS)
- `PendingReviewCard`: star icon + "Rate Your Delivery" + timeAgo

#### Empty states

Each section has its own empty state with icon + title + description. Global empty state shown when all three sections are empty.

### NotificationCard component

Separate composable (in `features/notifications/components/`):

```
[Icon circle] [Title]              [Unread dot]
              [Body (2 lines max)]  [Mark read btn]
              [Type badge] [TimeAgo]
```

- Icon: `NotificationType.icon` in colored circle (color at 15% opacity background)
- Type badge: small pill with `displayName` in type color

### NotificationBadge component

Reusable red circular badge showing count. Add to `:core:designsystem` as `PNotificationBadge`:

```kotlin
@Composable
fun PNotificationBadge(count: Int)
```

- Red circle with white bold caption text
- Minimum size 16dp
- Hidden when `count == 0`

iOS reference: [`NotificationBadge.swift`](../../Pasabayan/Views/Components/NotificationBadge.swift).

### ActionableItemCard component

Separate composable (in `features/notifications/components/`):

```
[Icon] [Title]               [Unread dot]
       [Description (2 lines)]
```

`ActionableItemType` enum with icon + color per type:

| Type | Icon | Color |
|------|------|-------|
| `bookingRequest` | `Description` | Orange |
| `statusUpdate` | `Refresh` | Blue |
| `packageRequest` | `Inventory2` | Green |
| `carrierResponse` | `Person` | Purple |
| `pickupReady` | `Inventory2` | Blue |
| `unreadMessages` | `Forum` | Blue |
| `inactiveTrips` | `Warning` | Orange |
| `upgrade` | `Star` | Yellow |

---

## Counter-offer cross-feature

- **API / match models:** [05-bookings-matches.md](05-bookings-matches.md)
- **Push → UI:** same `CounterOfferContext` behavior as iOS [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift)
- Counter-offer notification route data includes: `newPrice`, `originalPrice`, `priceDifference`, `direction`, `counterOffererName`, `counterOffererId`, `counterOfferRound`, `initiatedBy`, `remainingCounterOffers`, `canCounterOffer`

---

## Localization strings

Add to `res/values/strings_notifications.xml` (English) and `res/values-fr/strings_notifications.xml` (French).

### Required string keys

| Key | English value |
|-----|---------------|
| `notifications_title` | "Notifications & Actions" |
| `notifications_loading` | "Loading notifications..." |
| `notifications_action_mark_all_read` | "Mark All as Read" |
| `notifications_action_mark_as_read` | "Mark as Read" |
| `notifications_action_clear_all` | "Clear All" |
| `notifications_action_send_test` | "Send Test Notification" |
| `notifications_alert_test_title` | "Test Notification" |
| `notifications_empty_all_title` | "No notifications" |
| `notifications_empty_all_description` | "You don't have any notifications yet." |
| `notifications_empty_unread_title` | "All caught up" |
| `notifications_empty_unread_description` | "You don't have any unread notifications." |
| `notifications_filter_unread_only` | "Unread Only" |
| `notifications_filter_show_all` | "Show All" |
| `notifications_time_just_now` | "Just now" |
| `notifications_time_minutes_ago` | "%d minutes ago" |
| `notifications_time_hours_ago` | "%d hours ago" |
| `notifications_time_days_ago` | "%d days ago" |
| `notifications_section_notifications` | "Notifications" |
| `notifications_section_action_required` | "Action Required" |
| `notifications_section_pending_reviews` | "Pending Reviews" |
| `notifications_action_empty_title` | "No pending actions" |
| `notifications_action_empty_description` | "You don't have any requests that need your attention right now." |
| `notifications_reviews_empty_title` | "No reviews pending" |
| `notifications_reviews_empty_description` | "You don't have any deliveries to review yet." |
| `notifications_test_success` | "Test notification sent! Check your device." |
| `notifications_test_failure` | "Failed to send test notification. Make sure notifications are enabled." |

### Notification type display names

| Key | English |
|-----|---------|
| `notifications_type_match_request` | "New Request" |
| `notifications_type_match_accepted` | "Request Accepted" |
| `notifications_type_match_declined` | "Request Declined" |
| `notifications_type_chat_message` | "New Message" |
| `notifications_type_delivery_picked_up` | "Picked Up" |
| `notifications_type_delivery_in_transit` | "In Transit" |
| `notifications_type_delivery_delivered` | "Delivered" |
| `notifications_type_match_auto_cancelled` | "Auto-Cancelled" |
| `notifications_type_payment_received` | "Payment Received" |
| `notifications_type_payment_released` | "Payment Released" |
| `notifications_type_rating_received` | "New Rating" |
| `notifications_type_premium_approved` | "Premium Approved" |
| `notifications_type_premium_rejected` | "Premium Rejected" |
| `notifications_type_test` | "Test" |
| `notifications_type_payout_completed` | "Payout Completed" |
| `notifications_type_payout_scheduled` | "Payout Scheduled" |
| `notifications_type_refund_processed` | "Refund Processed" |
| `notifications_type_tip_received` | "Tip Received" |
| `notifications_type_payment_auto_charge_failed` | "Payment Failed" |
| `notifications_type_counter_offer` | "Counter Offer" |
| `notifications_type_unknown` | "Notification" |

---

## Android file layout

Following lean-classes rule (`.cursor/rules/lean-classes-separation.mdc`), split into:

```
features/notifications/
├── model/
│   ├── PushNotification.kt              # Main notification data class
│   ├── NotificationType.kt              # Enum with icon, color, displayName
│   ├── NotificationData.kt              # Nested payload data class
│   ├── ActionableItemType.kt            # Enum for actionable item types
│   └── PushNotificationRoute.kt         # Route data from push payload
├── viewmodel/
│   ├── NotificationViewModel.kt         # Manages notification list, unread counts
│   └── NotificationUiState.kt           # UI state data class
├── ui/
│   ├── ComprehensiveNotificationsScreen.kt  # Main notification screen (3 sections)
│   └── NotificationListScreen.kt            # Simple list variant (if needed)
├── services/
│   ├── NotificationRepository.kt        # Interface
│   ├── NotificationRepositoryImpl.kt    # API calls via NotificationApi
│   ├── NotificationRouter.kt            # Push tap → navigation events
│   ├── PushNotificationsConsentIntent.kt # Consent opt-in state (SharedPreferences)
│   └── NotificationModule.kt            # Hilt bindings
└── components/
    ├── NotificationCard.kt              # Single notification card
    ├── ActionableItemCard.kt            # Actionable item card
    ├── NotificationEmptyState.kt        # Empty state view
    ├── SectionHeader.kt                 # Section header with count badge
    └── NotificationPendingReviewCard.kt # Pending review card
```

Plus in `:core:designsystem`:
- `PNotificationBadge.kt` — reusable badge component

Plus in `:core:network` (or feature API module):
- `NotificationApi.kt` — Retrofit interface (7 endpoints)
- `dto/` — all JSON DTOs (separate files per response type)

---

## TDD checklist

### DTOs / parsing
- [ ] `PushNotificationJsonTest` — decode sample notification with all fields
- [ ] `NotificationDataJsonTest` — decode counter-offer payload fields
- [ ] `UnreadCountResponseJsonTest` — test all 4 response shapes (canonical, data-as-int, flat, bare)
- [ ] `MarkAllReadDataJsonTest` — test `marked_count` and `updated_count` fallback
- [ ] `DeviceTokenResponseJsonTest` — decode success response with nested `data.device_token`
- [ ] `DeviceTokenRegisterResponseParserTest` — 200/201 success, 403 consent, 403 other, unknown status

### Notification types
- [ ] `NotificationTypeTest` — every raw value maps correctly; unknown falls to `UNKNOWN`
- [ ] Each type has non-null `displayName`, `icon`, `color`

### Repository / service
- [ ] `NotificationRepositoryTest` — happy path for each endpoint
- [ ] `DeviceTokenConsentRetryTest` — 403 consent → auto-consent → retry → success
- [ ] `DeviceTokenRegistrationDeferredTest` — no API call when unauthenticated

### ViewModel
- [ ] `NotificationViewModelTest` — per-role unread count loading
- [ ] Optimistic mark-as-read: local state updated before server response
- [ ] Mark-all-read: all counts zeroed

### Routing
- [ ] `PushNotificationRouterTest` — each type routes to correct destination
- [ ] Parse push payload `data` map with string/int/double coercion (match iOS `intValue`/`doubleValue` helpers)

### UI
- [ ] `NotificationCardTest` — renders type icon, color, unread indicator
- [ ] `PNotificationBadgeTest` — hidden at 0, visible at 1+
- [ ] Compose previews for all components (light + dark)

### Localization
- [ ] All string keys present in both `values/` and `values-fr/`
- [ ] `timeAgo` uses localized format strings
- [ ] Notification type display names localized
