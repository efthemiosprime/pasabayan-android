# 08 — Notifications and device tokens

**Phase:** 5 | **Feature:** Notifications | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`NotificationAPIService.swift`](../../Pasabayan/Features/Notifications/Services/NotificationAPIService.swift) — note: uses **custom** `URLSession` path for some calls; status handling may differ from `APIService` (decode-first).

Source models: [`NotificationModels.swift`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift). In-app UI + routing: [`ComprehensiveNotificationsView.swift`](../../Pasabayan/Features/Notifications/Views/ComprehensiveNotificationsView.swift). Push / tab routing: [`NotificationManager.swift`](../../Pasabayan/Features/Notifications/Services/NotificationManager.swift).

---

## `NotificationType` — complete enum (implement all)

Backend `type` string must map to these cases. Unknown types should fall back safely (iOS uses `?? .test` in `PushNotification.notificationType` — consider a dedicated `unknown` on Android).

| Raw value (`type`) | Enum case | Typical navigation |
|--------------------|-----------|---------------------|
| `match_request` | `matchRequest` | Match / booking detail |
| `match_accepted` | `matchAccepted` | Match detail |
| `match_declined` | `matchDeclined` | Match detail |
| `chat_message` | `chatMessage` | Conversation |
| `delivery_picked_up` | `deliveryPickedUp` | Match detail |
| `delivery_in_transit` | `deliveryInTransit` | Match detail |
| `delivery_delivered` | `deliveryDelivered` | Match detail |
| `match_auto_cancelled` | `matchAutoCancelled` | Match detail |
| `payment_received` | `paymentReceived` | Payment / transaction |
| `payment_released` | `paymentReleased` | Payment / transaction |
| `rating_received` | `ratingReceived` | Ratings / match |
| `premium_approved` | `premiumApproved` | Verification / profile |
| `premium_rejected` | `premiumRejected` | Verification / profile |
| `test` | `test` | Debug / list |
| `payout_completed` | `payoutCompleted` | Payouts / Stripe |
| `payout_scheduled` | `payoutScheduled` | Payouts |
| `refund_processed` | `refundProcessed` | Transaction / refund |
| `tip_received` | `tipReceived` | Payment |
| `payment_auto_charge_failed` | `paymentAutoChargeFailed` | Match / payment retry |
| `counter_offer` | `counterOffer` | Match / counter-offer flow |

Display names, SF Symbol icons, and SwiftUI `Color` per type are in `NotificationType` extensions in the same file — mirror **semantic** colors using [14-design-system.md](14-design-system.md) tokens.

---

## `NotificationData` — optional payload fields (decode all)

All fields optional unless noted. Snake_case on wire — see `CodingKeys` in [`NotificationModels.swift`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift).

| Field | Use |
|-------|-----|
| `match_id`, `conversation_id`, `message_id` | Routing to match or chat |
| `requester_id` / `requester_name`, `carrier_id` / `carrier_name`, `shipper_id` / `shipper_name` | Display / deep link context |
| `transaction_id`, `amount`, `currency` | Payments |
| `rating_id`, `rating_value`, `rater_id`, `rater_name` | Ratings |
| `screen`, `reason` | Generic routing hints |
| `tip_amount`, `tipper_id`, `tipper_name`, `refund_amount` | Payment / tip / refund |
| `scheduled_at` | Payout scheduling |
| `recipient_role` | Filter by shipper/carrier |
| `new_price`, `original_price`, `price_difference`, `direction` | Counter-offer context |
| `counter_offerer_name`, `counter_offerer_id`, `counter_offer_round`, `initiated_by`, `remaining_counter_offers`, `can_counter_offer` | Counter-offer negotiation |

---

## Counter-offer cross-feature

- **API / match models:** [05-bookings-matches.md](05-bookings-matches.md)
- **Push → UI:** same `CounterOfferContext` behavior as iOS [`CounterOfferContext.swift`](../../Pasabayan/Features/Bookings/Models/CounterOfferContext.swift)

---

## Endpoints

| Method | Path |
|--------|------|
| POST | `/device-tokens` |
| DELETE | `/device-tokens/{token}` |
| GET | `/notifications` (+ query) |
| GET | `/notifications/unread-count` |
| PUT | `/notifications/{id}/read` |
| PUT | `/notifications/mark-all-read` |
| POST | `/device-tokens/test` |

### Request body (register token)

JSON: `token`, `platform`, `app_version`, `device_model`, `os_version`, optional `device_name` (see iOS implementation).

## Response shapes

### GET `/notifications/unread-count`

Query: `role` (optional). Response maps to `response.data.unreadCount`:

```json
{
  "data": {
    "unreadCount": 5
  }
}
```

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

`platform` must be `"android"` (iOS sends `"ios"`). `device_name` is optional.

### GET `/notifications` — query params

| Param | Type | Notes |
|-------|------|-------|
| `page` | Int | Pagination |
| `unread_only` | Bool | Optional filter |
| `role` | String | Optional: `"shipper"` or `"carrier"` |

## Ambiguity note

Unread count and mark-all-read responses may accept multiple shapes on iOS — Android parsers should match `NotificationAPIService` / model tests. The primary shape is documented above.

## UI (iOS reference)

- In-app list / filters: `ComprehensiveNotificationsView`.
- Badges: Profile tab, tab-specific badges (see [13-ui-tab-explore.md](13-ui-tab-explore.md)).
- **Per-type** styling: icon + color from `NotificationType` (see enum above).

## TDD checklist

- [ ] `DeviceTokenRegisterResponseParserTests`, `NotificationBadgeCountTests`, `NotificationRoutingTests`.
- [ ] Decode sample JSON for **each** `NotificationType` raw value.
- [ ] Counter-offer: `CounterOfferNotificationTests`, `CounterOfferContextNotificationDataTests`.
