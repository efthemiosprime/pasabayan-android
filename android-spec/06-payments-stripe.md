# 06 — Payments, Stripe, receipts

**Phase:** 4 | **Feature:** Payments | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

iOS parity sources: [`PaymentModels.swift`](../../Pasabayan/Features/Payments/Models/PaymentModels.swift), [`PaymentReceiptModels.swift`](../../Pasabayan/Features/Payments/Models/PaymentReceiptModels.swift), [`StripeConfigModels.swift`](../../Pasabayan/Features/Payments/Models/StripeConfigModels.swift), [`StripeConnectModels.swift`](../../Pasabayan/Features/Payments/Models/StripeConnectModels.swift), [`PaymentService.swift`](../../Pasabayan/Features/Payments/Services/PaymentService.swift), [`PaymentServicing.swift`](../../Pasabayan/Features/Payments/Services/PaymentServicing.swift), [`StripeConfigService.swift`](../../Pasabayan/Features/Payments/Services/StripeConfigService.swift), [`StripeConnectService.swift`](../../Pasabayan/Features/Payments/Services/StripeConnectService.swift), [`ReceiptService.swift`](../../Pasabayan/Features/Payments/Services/ReceiptService.swift), [`PaymentViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/PaymentViewModel.swift), [`PaymentMethodsViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/PaymentMethodsViewModel.swift), [`TippingViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/TippingViewModel.swift), [`StripeConnectViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/StripeConnectViewModel.swift), [`ReceiptListViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/ReceiptListViewModel.swift), [`ReceiptDetailViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/ReceiptDetailViewModel.swift).

---

## Package layout (feature-first)

```
features/payments/
├── model/
│   ├── Transaction.kt
│   ├── TransactionAmounts.kt
│   ├── TransactionUser.kt
│   ├── StripeInfo.kt
│   ├── RefundInfo.kt
│   ├── TransactionTimestamps.kt
│   ├── Payout.kt
│   ├── TipInfo.kt
│   ├── CreatePaymentRequest.kt
│   ├── CreatePaymentResponse.kt
│   ├── TransactionListResponse.kt
│   ├── TransactionResponse.kt
│   ├── ConfirmCaptureRequest.kt
│   ├── RefundRequestBody.kt
│   ├── RefundRequestData.kt
│   ├── RefundStatusResponse.kt
│   ├── CancelRequest.kt
│   ├── TipRequest.kt
│   ├── TipResponse.kt
│   ├── PaymentActionResponse.kt
│   ├── PaymentAPIErrorResponse.kt
│   ├── PaymentReceipt.kt
│   ├── PaymentReceiptListResponse.kt
│   ├── StripeConfig.kt
│   ├── StripeConfigResponse.kt
│   ├── StripeConnectStatus.kt
│   ├── StripeOnboardingResponse.kt
│   ├── StripeDashboardResponse.kt
│   ├── PaymentMethodDisplay.kt
│   ├── SetupIntentResponse.kt
│   ├── PaymentMethodsResponse.kt
│   ├── DefaultPaymentMethodResponse.kt
│   ├── TransactionStatus.kt
│   ├── RefundStatus.kt
│   ├── PayoutStatus.kt
│   ├── TransactionRole.kt
│   └── RefundReason.kt
├── services/
│   ├── PaymentRepository.kt          (interface)
│   ├── PaymentRepositoryImpl.kt
│   ├── PaymentApi.kt                 (Retrofit)
│   ├── StripeConfigRepository.kt     (interface)
│   ├── StripeConfigRepositoryImpl.kt
│   ├── StripeConfigApi.kt            (Retrofit)
│   ├── StripeConnectRepository.kt    (interface)
│   ├── StripeConnectRepositoryImpl.kt
│   ├── StripeConnectApi.kt           (Retrofit)
│   ├── ReceiptRepository.kt          (interface)
│   ├── ReceiptRepositoryImpl.kt
│   ├── ReceiptApi.kt                 (Retrofit)
│   ├── PaymentMethodsRepository.kt   (interface)
│   ├── PaymentMethodsRepositoryImpl.kt
│   ├── PaymentMethodsApi.kt          (Retrofit)
│   └── PaymentsModule.kt             (Hilt)
├── viewmodel/
│   ├── PaymentViewModel.kt
│   ├── PaymentMethodsViewModel.kt
│   ├── TippingViewModel.kt
│   ├── RefundViewModel.kt
│   ├── TransactionHistoryViewModel.kt
│   ├── TransactionDetailViewModel.kt
│   ├── StripeConnectViewModel.kt
│   ├── ReceiptListViewModel.kt
│   └── ReceiptDetailViewModel.kt
├── ui/
│   ├── PaymentMethodsScreen.kt
│   ├── AddPaymentMethodSheet.kt
│   ├── TransactionHistoryScreen.kt
│   ├── TransactionDetailScreen.kt
│   ├── RefundSheet.kt
│   ├── TipSelectionScreen.kt
│   ├── PayoutSetupScreen.kt
│   ├── ReceiptListScreen.kt
│   ├── ReceiptDetailScreen.kt
│   └── ReceiptWebView.kt
└── components/
    ├── PaymentButton.kt
    ├── PaymentMethodCard.kt
    ├── PaymentSummaryCard.kt
    ├── PayoutStatusBadge.kt
    ├── RefundStatusBadge.kt
    ├── RefundStatusCard.kt
    ├── TipBadge.kt
    └── ReceiptRow.kt
```

---

## Models

### `Transaction` — primary transaction model

Wire format is snake_case. Maps to iOS `Transaction` struct in `PaymentModels.swift`.

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `id` | `Int` | `id` | Required |
| `shipper` | `TransactionUser?` | `shipper` | Nested |
| `carrier` | `TransactionUser?` | `carrier` | Nested |
| `deliveryMatchId` | `Int?` | `delivery_match_id` | |
| `amounts` | `TransactionAmounts?` | `amounts` | Nested |
| `stripe` | `StripeInfo?` | `stripe` | Nested |
| `status` | `String` | `status` | Raw; maps to `TransactionStatus` |
| `description` | `String?` | `description` | |
| `metadata` | `Map<String, Any>?` | `metadata` | Dynamic JSON — use `JsonElement` or `AnyCodable` equivalent |
| `refund` | `RefundInfo?` | `refund` | Nested |
| `timestamps` | `TransactionTimestamps?` | `timestamps` | Nested |
| `clientSecret` | `String?` | `client_secret` | From create-payment response |
| `payoutStatus` | `String?` | `payout_status` | |
| `payoutNotes` | `String?` | `payout_notes` | |
| `payoutCompletedAt` | `String?` | `payout_completed_at` | ISO8601 |
| `payout` | `Payout?` | `payout` | Nested |
| `tip` | `TipInfo?` | `tip` | Nested |
| `customerId` | `String?` | `customer_id` | Stripe customer |
| `ephemeralKey` | `String?` | `ephemeral_key` | Stripe ephemeral key |

**Computed:**
- `transactionStatus: TransactionStatus` — parse `status` string; fall back to `UNKNOWN`.

---

### `TransactionAmounts`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `total` | `Double` | `total` | **Flexible decoder** — accept both string and number from API |
| `subtotal` | `Double?` | `subtotal` | Flexible |
| `platformFee` | `Double?` | `platform_fee` | Flexible |
| `carrierReceives` | `Double?` | `carrier_receives` | Flexible |
| `currency` | `String` | `currency` | Default `"cad"` |
| `tip` | `Double?` | `tip` | Flexible |
| `tax` | `Double?` | `tax` | Flexible |
| `taxBreakdown` | `Map<String, Any>?` | `tax_breakdown` | Dynamic |
| `carrierTotal` | `Double?` | `carrier_total` | Flexible |
| `baseAmount` | `Double?` | `base_amount` | Flexible |

**Important:** iOS uses a custom `Decoder` that parses doubles from both `String` and `Number` JSON types. Android must replicate this with a custom kotlinx-serialization deserializer or `@Serializable` adapter.

---

### `TransactionUser`

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `Int` | `id` |
| `name` | `String` | `name` |
| `email` | `String?` | `email` |
| `avatar` | `String?` | `avatar` |

---

### `StripeInfo`

| Field | Type | Wire key |
|-------|------|----------|
| `paymentIntentId` | `String?` | `payment_intent_id` |
| `transferId` | `String?` | `transfer_id` |
| `chargeId` | `String?` | `charge_id` |
| `refundId` | `String?` | `refund_id` |

---

### `RefundInfo`

| Field | Type | Wire key |
|-------|------|----------|
| `amount` | `Double?` | `amount` |
| `reason` | `String?` | `reason` |
| `refundedAt` | `String?` | `refunded_at` |

---

### `TransactionTimestamps`

| Field | Type | Wire key |
|-------|------|----------|
| `authorizedAt` | `String?` | `authorized_at` |
| `capturedAt` | `String?` | `captured_at` |
| `completedAt` | `String?` | `completed_at` |
| `failedAt` | `String?` | `failed_at` |
| `payoutCompletedAt` | `String?` | `payout_completed_at` |
| `createdAt` | `String?` | `created_at` |
| `updatedAt` | `String?` | `updated_at` |

---

### `Payout`

| Field | Type | Wire key |
|-------|------|----------|
| `status` | `String?` | `status` |
| `notes` | `String?` | `notes` |
| `completedAt` | `String?` | `completed_at` |

---

### `TipInfo`

| Field | Type | Wire key |
|-------|------|----------|
| `amount` | `Double?` | `amount` |
| `paidAt` | `String?` | `paid_at` |
| `stripePaymentIntentId` | `String?` | `stripe_payment_intent_id` |

---

### Request / response DTOs

#### `CreatePaymentRequest`

| Field | Type | Wire key |
|-------|------|----------|
| `deliveryMatchId` | `Int` | `delivery_match_id` |
| `amount` | `Double` | `amount` |
| `currency` | `String` | `currency` |

#### `CreatePaymentResponse`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `success` | `Boolean` | `success` | |
| `message` | `String?` | `message` | |
| `data` | `Transaction?` | `data` | |
| `clientSecret` | `String?` | `client_secret` | **Custom decoder:** may appear at top level OR nested under `data.client_secret` |
| `customerId` | `String?` | `customer_id` | |
| `ephemeralKey` | `String?` | `ephemeral_key` | |
| `publicKey` | `String?` | `public_key` | |
| `defaultPaymentMethodId` | `String?` | `default_payment_method_id` | |

**Important:** iOS has a custom decoder that first tries top-level `client_secret`, then falls back to `data.client_secret`. Replicate on Android.

#### `TransactionListResponse`

```json
{ "success": true, "data": [Transaction] }
```

#### `TransactionResponse`

```json
{ "success": true, "data": Transaction, "message": "..." }
```

#### `ConfirmCaptureRequest`

| Field | Type | Wire key |
|-------|------|----------|
| `deliveryMatchId` | `Int` | `delivery_match_id` |

#### `RefundRequestBody`

| Field | Type | Wire key |
|-------|------|----------|
| `amount` | `Double?` | `amount` |
| `reason` | `String` | `reason` |
| `description` | `String?` | `description` |

#### `RefundRequestData`

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `Int` | `id` |
| `transactionId` | `Int` | `transaction_id` |
| `amount` | `Double?` | `amount` |
| `reason` | `String?` | `reason` |
| `description` | `String?` | `description` |
| `status` | `String` | `status` |
| `adminNotes` | `String?` | `admin_notes` |
| `reviewedAt` | `String?` | `reviewed_at` |
| `processedAt` | `String?` | `processed_at` |
| `createdAt` | `String?` | `created_at` |

#### `RefundStatusResponse`

```json
{ "success": true, "data": RefundRequestData, "message": "..." }
```

#### `CancelRequest`

| Field | Type | Wire key |
|-------|------|----------|
| `reason` | `String?` | `reason` |

#### `TipRequest`

| Field | Type | Wire key |
|-------|------|----------|
| `amount` | `Double` | `amount` |

#### `TipResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `message` | `String?` | `message` |
| `data` | `Transaction?` | `data` |
| `clientSecret` | `String?` | `client_secret` |

#### `PaymentActionResponse`

```json
{ "success": true, "message": "...", "data": Transaction }
```

#### `PaymentAPIErrorResponse`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `success` | `Boolean` | `success` | |
| `message` | `String?` | `message` | |
| `error` | `String?` | `error` | |
| `errors` | `Map<String, List<String>>?` | `errors` | Validation errors |
| `transaction` | `Transaction?` | `transaction` | |
| `clientSecret` | `String?` | `client_secret` | For `requiresAuthentication` |
| `customerId` | `String?` | `customer_id` | |
| `ephemeralKey` | `String?` | `ephemeral_key` | |
| `publicKey` | `String?` | `public_key` | |

---

### Receipt models

#### `PaymentReceipt`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `id` | `Int` | `id` | |
| `receiptNumber` | `String` | `receipt_number` | |
| `receiptUrl` | `String?` | `receipt_url` | |
| `date` | `String` | `date` | ISO8601 |
| `dateFormatted` | `String?` | `date_formatted` | |
| `role` | `String` | `role` | `"shipper"` or `"carrier"` |
| `otherParty` | `OtherParty?` | `other_party` | Nested |
| `amount` | `PaymentReceiptAmount` | `amount` | Nested |
| `status` | `String` | `status` | |
| `delivery` | `PaymentReceiptDelivery?` | `delivery` | Nested |

**Nested: `OtherParty`**

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `Int` | `id` |
| `name` | `String` | `name` |
| `verificationLevel` | `String?` | `verification_level` |

**Nested: `PaymentReceiptAmount`**

| Field | Type | Wire key |
|-------|------|----------|
| `total` | `Double` | `total` |
| `carrierAmount` | `Double?` | `carrier_amount` |
| `platformFee` | `Double?` | `platform_fee` |
| `tip` | `Double?` | `tip` |
| `currency` | `String` | `currency` |

**Nested: `PaymentReceiptDelivery`**

| Field | Type | Wire key |
|-------|------|----------|
| `pickupCity` | `String?` | `pickup_city` |
| `deliveryCity` | `String?` | `delivery_city` |
| `packageTitle` | `String?` | `package_title` |

**Computed properties:**
- `url: Uri?` — parse `receiptUrl`
- `isShipper: Boolean` — `role == "shipper"`
- `displayAmount: String` — formatted with currency; green for carrier
- `routeDescription: String` — `"pickupCity → deliveryCity"` or fallback to party names

#### `PaymentReceiptListResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `data` | `List<PaymentReceipt>` | `data` |
| `meta` | `PaymentReceiptPaginationMeta?` | `meta` |
| `message` | `String?` | `message` |

#### `PaymentReceiptPaginationMeta`

| Field | Type | Wire key |
|-------|------|----------|
| `currentPage` | `Int` | `current_page` |
| `lastPage` | `Int` | `last_page` |
| `perPage` | `Int` | `per_page` |
| `total` | `Int` | `total` |

#### `SinglePaymentReceiptResponse`

```json
{ "success": true, "data": PaymentReceipt, "message": "..." }
```

---

### Stripe config models

#### `StripeConfig`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `mode` | `String` | `mode` | `"sandbox"` or `"live"` |
| `publicKey` | `String` | `public_key` | Stripe publishable key |
| `currency` | `String` | `currency` | Default `"cad"` |
| `minDeliveryPrice` | `Double` | `min_delivery_price` | |
| `taxEnabled` | `Boolean` | `tax_enabled` | |
| `senderFeePercentage` | `Double` | `sender_fee_percentage` | |
| `carrierFeePercentage` | `Double` | `carrier_fee_percentage` | |
| `platformFeePercentage` | `Double` | `platform_fee_percentage` | |

**Computed:**
- `isSandbox: Boolean` — `mode == "sandbox"`
- `isLive: Boolean` — `mode == "live"`
- `modeDescription: String` — human-readable

#### `StripeConfigResponse`

```json
{ "success": true, "data": StripeConfig, "message": "..." }
```

---

### Stripe Connect models

#### `StripeOnboardingResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `message` | `String?` | `message` |
| `data` | `OnboardingData?` | `data` |

#### `OnboardingData`

| Field | Type | Wire key |
|-------|------|----------|
| `onboardingUrl` | `String` | `onboarding_url` |
| `stripeAccountId` | `String` | `stripe_account_id` |
| `expiresAt` | `String?` | `expires_at` |

#### `StripeConnectStatus`

| Field | Type | Wire key | Notes |
|-------|------|----------|-------|
| `hasStripeAccount` | `Boolean` | `has_stripe_account` | |
| `stripeAccountId` | `String?` | `stripe_account_id` | |
| `onboardingComplete` | `Boolean` | `onboarding_complete` | |
| `onboardedAt` | `String?` | `onboarded_at` | ISO8601 |
| `chargesEnabled` | `Boolean` | `charges_enabled` | |
| `payoutsEnabled` | `Boolean` | `payouts_enabled` | |
| `canReceivePayouts` | `Boolean` | `can_receive_payouts` | |

**Computed:**
- `isOnboarded: Boolean` — `onboardingComplete`
- `canPayout: Boolean` — `payoutsEnabled && canReceivePayouts`
- `hasAccount: Boolean` — `hasStripeAccount`

#### `StripeDashboardResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `message` | `String?` | `message` |
| `data` | `DashboardData?` | `data` |

#### `DashboardData`

| Field | Type | Wire key |
|-------|------|----------|
| `dashboardUrl` | `String` | `dashboard_url` |

---

### Payment methods models

#### `PaymentMethodDisplay`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` | Stripe payment method ID |
| `brand` | `String` | `"visa"`, `"mastercard"`, `"amex"`, `"discover"`, etc. |
| `last4` | `String` | |
| `expMonth` | `Int` | |
| `expYear` | `Int` | |
| `isDefault` | `Boolean` | |

**Computed:**
- `displayName: String` — `"Visa •••• 4242"`
- `expiryDisplay: String` — `"MM/YY"`

#### `SetupIntentResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `data` | `SetupIntentData` | `data` |

#### `SetupIntentData`

| Field | Type | Wire key |
|-------|------|----------|
| `clientSecret` | `String` | `client_secret` |
| `customerId` | `String` | `customer_id` |
| `ephemeralKey` | `String` | `ephemeral_key` |

#### `PaymentMethodsResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `data` | `List<PaymentMethodAPI>` | `data` |

#### `PaymentMethodAPI`

| Field | Type | Wire key |
|-------|------|----------|
| `id` | `String` | `id` |
| `card` | `CardDetails?` | `card` |

#### `CardDetails`

| Field | Type | Wire key |
|-------|------|----------|
| `brand` | `String` | `brand` |
| `last4` | `String` | `last4` |
| `expMonth` | `Int` | `exp_month` |
| `expYear` | `Int` | `exp_year` |

#### `DefaultPaymentMethodResponse`

| Field | Type | Wire key |
|-------|------|----------|
| `success` | `Boolean` | `success` |
| `data` | `DefaultPaymentMethodData?` | `data` |

#### `DefaultPaymentMethodData`

| Field | Type | Wire key |
|-------|------|----------|
| `paymentMethodId` | `String?` | `payment_method_id` |

#### `SetDefaultPaymentMethodResponse`

```json
{ "success": true, "data": { "payment_method_id": "..." } }
```

#### `DeletePaymentMethodResponse`

```json
{ "success": true, "message": "..." }
```

---

## Enums

### `TransactionStatus`

| Raw value | Enum case | displayName | color | icon |
|-----------|-----------|-------------|-------|------|
| `pending` | `PENDING` | "Pending" | orange | clock |
| `authorized` | `AUTHORIZED` | "Authorized" | blue | checkmark.circle |
| `captured` | `CAPTURED` | "Captured" | blue | arrow.down.circle |
| `completed` | `COMPLETED` | "Completed" | green | checkmark.circle.fill |
| `refunded` | `REFUNDED` | "Refunded" | purple | arrow.uturn.left |
| `cancelled` | `CANCELLED` | "Cancelled" | red | xmark.circle |
| `failed` | `FAILED` | "Failed" | red | exclamationmark.circle |
| _(unknown)_ | `UNKNOWN` | "Unknown" | gray | questionmark.circle |

Map colors to `PasabayanColors` / `MaterialTheme.colorScheme` tokens — never hardcode.

### `RefundStatus`

| Raw value | Enum case | displayText | color | icon |
|-----------|-----------|-------------|-------|------|
| `pending` | `PENDING` | "Pending" | orange | clock |
| `approved` | `APPROVED` | "Approved" | green | checkmark.circle |
| `rejected` | `REJECTED` | "Rejected" | red | xmark.circle |
| `processed` | `PROCESSED` | "Processed" | blue | arrow.uturn.left.circle |

### `PayoutStatus`

| Raw value | Enum case | displayText | color | icon |
|-----------|-----------|-------------|-------|------|
| `pending` | `PENDING` | "Pending" | orange | clock |
| `processing` | `PROCESSING` | "Processing" | orange | arrow.clockwise |
| `on_hold` | `ON_HOLD` | "On Hold" | yellow | pause.circle |
| `scheduled` | `SCHEDULED` | "Scheduled" | blue | calendar |
| `completed` | `COMPLETED` | "Completed" | green | checkmark.circle.fill |
| `failed` | `FAILED` | "Failed" | red | exclamationmark.circle |

### `TransactionRole` — history filter

| Enum case | displayName | apiValue (query param) |
|-----------|-------------|------------------------|
| `ALL` | "All" | `null` (omit param) |
| `SHIPPER` | "Shipper" | `"shipper"` |
| `CARRIER` | "Carrier" | `"carrier"` |

### `RefundReason` — preset reasons

| Enum case | displayText | requiresCustomInput |
|-----------|------------|---------------------|
| `DAMAGED` | "Package was damaged during delivery" | `false` |
| `NOT_DELIVERED` | "Package was never delivered" | `false` |
| `WRONG_ITEM` | "Wrong item was delivered" | `false` |
| `LATE_DELIVERY` | "Delivery was significantly late" | `false` |
| `PARTIAL_DELIVERY` | "Only partial items were delivered" | `false` |
| `OTHER` | "Other" | `true` |

### `PaymentError`

| Case | Description |
|------|------------|
| `INVALID_RESPONSE` | Unexpected response format |
| `API_ERROR(message: String)` | Server error with message |
| `PERMISSION_DENIED` | HTTP 403 |
| `STRIPE_ERROR(message: String)` | Stripe SDK error |
| `REQUIRES_AUTHENTICATION(clientSecret: String)` | 3DS / authentication needed |
| `CANCELLED` | User cancelled |
| `INVALID_AMOUNT` | Invalid payment amount |
| `NETWORK_ERROR` | Connectivity failure |

Map to `DomainError` via `ApiErrorMapper` when appropriate.

**Error mapping from HTTP responses:**
- HTTP 403 → `PERMISSION_DENIED`
- Body contains `"requires_authentication"` + `client_secret` → `REQUIRES_AUTHENTICATION(clientSecret)`
- Body contains `errors` map → extract first validation message → `API_ERROR`
- Body contains `message` → `API_ERROR(message)`

### `StripeConnectError`

| Case | Server message trigger |
|------|----------------------|
| `NETWORK_ERROR` | — |
| `NOT_CARRIER` | `"must be a carrier"` |
| `ALREADY_ONBOARDED` | `"already completed"` |
| `NOT_ONBOARDED` | `"complete Stripe onboarding first"` |
| `API_ERROR(message)` | default |
| `INVALID_RESPONSE` | decode failure |

### `StripeConfigError`

| Case | Trigger |
|------|---------|
| `NETWORK_ERROR` | connectivity |
| `INVALID_RESPONSE` | decode failure |
| `NOT_AUTHENTICATED` | no token |
| `API_ERROR(message)` | default |

### `ReceiptError`

| Case | Trigger |
|------|---------|
| `NOT_AUTHENTICATED` | no token |
| `INVALID_URL` | bad URL |
| `INVALID_RESPONSE` | decode failure |
| `NETWORK_ERROR` | connectivity |
| `SERVER_ERROR(code)` | HTTP 5xx |
| `DECODING_ERROR` | JSON parse |
| `UNKNOWN` | fallback |

### `PaymentMethodError`

| Case | Trigger |
|------|---------|
| `NETWORK_ERROR` | connectivity |
| `INVALID_RESPONSE` | decode failure |
| `API_ERROR(message)` | default |
| `SETUP_FAILED(message)` | SetupIntent failure |

---

## Endpoints

### Payments REST (`PaymentApi`)

| Method | Path | Request body | Response | Query params | Notes |
|--------|------|-------------|----------|-------------|-------|
| POST | `/payments` | `CreatePaymentRequest` | `CreatePaymentResponse` | — | Creates PaymentIntent on backend |
| GET | `/payments` | — | `TransactionListResponse` | `?role=shipper\|carrier` | Optional role filter |
| GET | `/payments/{id}` | — | `TransactionResponse` | — | |
| POST | `/payments/{id}/capture` | — | `PaymentActionResponse` | — | Manual capture |
| POST | `/payments/confirm-capture` | `ConfirmCaptureRequest` | `PaymentActionResponse` | — | After PaymentSheet completion |
| POST | `/payments/{id}/release` | — | `PaymentActionResponse` | — | Release held funds |
| POST | `/payments/{transactionId}/refund` | `RefundRequestBody` | `RefundStatusResponse` | — | `amount` optional (null = full) |
| GET | `/payments/{transactionId}/refund-status` | — | `RefundStatusResponse` | — | |
| POST | `/payments/{id}/cancel` | `CancelRequest` | `PaymentActionResponse` | — | `reason` optional |
| POST | `/payments/{transactionId}/tip` | `TipRequest` | `TipResponse` | — | Returns `clientSecret` if post-delivery |

### Stripe config (`StripeConfigApi`)

| Method | Path | Response | Notes |
|--------|------|----------|-------|
| GET | `/payments/config` | `StripeConfigResponse` | Requires auth; returns mode, publicKey, fees |

### Stripe Connect (`StripeConnectApi`)

| Method | Path | Response | Notes |
|--------|------|----------|-------|
| POST | `/stripe/connect/onboard` | `StripeOnboardingResponse` | Returns onboarding URL |
| GET | `/stripe/connect/status` | `StripeStatusResponse` { success, data: `StripeConnectStatus` } | |
| GET | `/stripe/connect/dashboard` | `StripeDashboardResponse` | Returns dashboard URL |

### Payment methods (`PaymentMethodsApi`)

| Method | Path | Request body | Response | Notes |
|--------|------|-------------|----------|-------|
| GET | `/stripe/payment-methods` | — | `PaymentMethodsResponse` | |
| POST | `/stripe/setup-intent` | — | `SetupIntentResponse` | For adding new cards |
| GET | `/stripe/default-payment-method` | — | `DefaultPaymentMethodResponse` | |
| PUT | `/stripe/default-payment-method` | `{ "payment_method_id": "..." }` | `SetDefaultPaymentMethodResponse` | |
| DELETE | `/stripe/payment-methods/{methodId}` | — | `DeletePaymentMethodResponse` | |

### Receipts (`ReceiptApi`)

| Method | Path | Response | Query params | Notes |
|--------|------|----------|-------------|-------|
| GET | `/receipts` | `PaymentReceiptListResponse` | `?page=&per_page=` | Paginated; default `per_page=20` |
| GET | `/receipts/{transactionId}` | `SinglePaymentReceiptResponse` | — | Returns receipt with URL |

---

## Services / Repositories

### `StripeConfigRepository`

Manages Stripe SDK configuration and fee calculations. iOS: `StripeConfigService`.

**State (exposed as StateFlow):**
- `currentConfig: StripeConfig?`
- `isConfigured: Boolean`
- `isLoading: Boolean`
- `errorMessage: String?`

**Constants:**
```kotlin
const val DEFAULT_MIN_DELIVERY_PRICE = 5.00
const val MAX_PROPOSED_PRICE = 9999.99
const val DEFAULT_SENDER_FEE_PERCENTAGE = 10.0
const val DEFAULT_CARRIER_FEE_PERCENTAGE = 5.0
```

**Fee calculation methods:**
- `senderFeePercentage: Double` — from config or default 10%
- `carrierFeePercentage: Double` — from config or default 5%
- `platformFeePercentage: Double` — from config or sum of sender+carrier
- `totalToPay(baseAmount: Double): Double` — `baseAmount * (1 + senderFeePercentage / 100)`
- `serviceFeeAmount(baseAmount: Double): Double` — `baseAmount * senderFeePercentage / 100`
- `carrierReceives(baseAmount: Double): Double` — `baseAmount * (1 - carrierFeePercentage / 100)`

**Price validation:**
- `validateMinimumPrice(price: Double): String?` — returns error if below `minDeliveryPrice`
- `validateProposedPrice(price: Double): String?` — validates range `[minDeliveryPrice, MAX_PROPOSED_PRICE]`

**Methods:**
- `fetchConfig(): Result<StripeConfig>` — GET `/payments/config`
- `loadAndApplyConfig()` — async load → set `PaymentConfiguration.init(publishableKey)` → update state; fallback to hardcoded key on failure
- `fetchConfigAsync(): StripeConfig` — suspend version

### `PaymentRepository`

Interface mirroring iOS `PaymentServicing` protocol.

```kotlin
interface PaymentRepository {
    suspend fun createPayment(deliveryMatchId: Int, amount: Double, currency: String): Result<CreatePaymentResponse>
    suspend fun listTransactions(role: TransactionRole? = null): Result<List<Transaction>>
    suspend fun getTransaction(id: Int): Result<Transaction>
    suspend fun captureTransaction(id: Int): Result<Transaction>
    suspend fun confirmCapture(deliveryMatchId: Int): Result<Transaction>
    suspend fun releaseTransaction(id: Int): Result<Transaction>
    suspend fun requestRefund(transactionId: Int, amount: Double?, reason: String, description: String?): Result<RefundRequestData>
    suspend fun getRefundStatus(transactionId: Int): Result<RefundStatusResponse>
    suspend fun cancelTransaction(id: Int, reason: String?): Result<Transaction>
    suspend fun addTip(transactionId: Int, amount: Double): Result<TipResponse>
}
```

### `StripeConnectRepository`

```kotlin
interface StripeConnectRepository {
    suspend fun startOnboarding(): Result<StripeOnboardingResponse>
    suspend fun checkStatus(): Result<StripeConnectStatus>
    suspend fun getDashboardUrl(): Result<String>
}
```

**Error mapping in impl:** map specific server message strings to `StripeConnectError` cases (see enum section).

### `ReceiptRepository`

```kotlin
interface ReceiptRepository {
    suspend fun fetchReceipts(page: Int, perPage: Int = 20): Result<Triple<List<PaymentReceipt>, Boolean, Int>>
    // Returns (receipts, hasMore, total)
    suspend fun fetchReceipt(transactionId: Int): Result<PaymentReceipt>
}
```

### `PaymentMethodsRepository`

```kotlin
interface PaymentMethodsRepository {
    suspend fun loadPaymentMethods(): Result<List<PaymentMethodDisplay>>
    suspend fun loadDefaultPaymentMethod(): Result<String?>
    suspend fun createSetupIntent(): Result<SetupIntentData>
    suspend fun removePaymentMethod(methodId: String): Result<Unit>
    suspend fun setDefaultPaymentMethod(methodId: String): Result<Unit>
}
```

---

## ViewModels

### `PaymentViewModel`

iOS: `PaymentViewModel.swift`. Orchestrates the Stripe PaymentSheet flow.

**State:**
```kotlin
data class PaymentUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val paymentSuccess: Boolean = false,
    val transaction: Transaction? = null,
    val infoMessage: String? = null,
    val isSheetReady: Boolean = false,
    val statusMessage: String? = null,
)
```

**Key methods:**
- `initiatePayment(deliveryMatch: DeliveryMatch)` — entry point; calls `loadPaymentSheet`
- `loadPaymentSheet(deliveryMatch: DeliveryMatch)` — validates not already paid (`isPaidTransactionStatus`), POST `/payments`, extracts `clientSecret` (top-level or nested), skips for mock (`mock_pi_*`), updates Stripe key from response, configures `PaymentSheet`
- `presentPaymentSheet(activity: Activity)` — present Stripe PaymentSheet (Android uses Activity instead of UIViewController)
- `handlePaymentSheetCompleted()` — POST `/payments/confirm-capture`, update transaction, set `paymentSuccess = true`
- `cancelPayment(transactionId: Int)` — POST `/payments/{id}/cancel`

**Helpers:**
- `isPaidTransactionStatus(status: String?): Boolean` — `"completed"` or `"captured"`
- `isMockClientSecret(secret: String): Boolean` — starts with `"mock_pi_"`
- `configurePaymentSheet(clientSecret, customerId, ephemeralKey, amount, currency)` — sets merchantDisplayName `"Pasabayan"`, customer config, saved cards (`savePaymentMethodOptInBehavior = requiresOptIn`), Google Pay (instead of Apple Pay), return URL `"pasabayan://stripe-redirect"`

### `PaymentMethodsViewModel`

iOS: `PaymentMethodsViewModel.swift`.

**State:**
```kotlin
data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethodDisplay> = emptyList(),
    val defaultPaymentMethodId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
```

**Methods:**
- `loadPaymentMethods()` — GET `/stripe/payment-methods`; sort with default card first
- `loadDefaultPaymentMethod()` — GET `/stripe/default-payment-method`
- `prepareAddPaymentMethod(onReady: (PaymentSheet) -> Unit)` — POST `/stripe/setup-intent` → configure `PaymentSheet` for SetupIntent
- `removePaymentMethod(methodId: String)` — DELETE `/stripe/payment-methods/{id}`
- `setDefaultPaymentMethod(methodId: String)` — PUT `/stripe/default-payment-method`
- `clearMessages()` — reset error/success

**Computed:**
- `hasDefaultPaymentMethod: Boolean`
- `defaultPaymentMethodDisplay: PaymentMethodDisplay?`

### `TippingViewModel`

iOS: `TippingViewModel.swift`.

**State:**
```kotlin
data class TippingUiState(
    val selectedTipAmount: Double = 0.0,
    val customTipAmount: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccess: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val updatedTransaction: Transaction? = null,
)
```

**Constants:**
```kotlin
val PRESET_TIPS = listOf(2.0, 5.0, 10.0, 15.0)
const val MIN_TIP = 1.0
const val MAX_TIP = 500.0
```

**Methods:**
- `selectPresetTip(amount: Double)` — sets selected, clears custom
- `clearPresetTip()` — resets to 0
- `addTip(transactionId: Int)` — POST `/payments/{id}/tip`; if `clientSecret` returned → setup PaymentSheet for post-delivery; else → `showSuccess = true`
- `setupPaymentSheet(clientSecret: String)` — configure for SetupIntent
- `handlePaymentResult(result: PaymentSheetResult)` — completed/canceled/failed
- `reset()` — clear all state

**Computed:**
- `effectiveTipAmount: Double` — selected or parsed custom
- `isValidTip: Boolean` — between MIN_TIP and MAX_TIP

### `RefundViewModel`

iOS: `RefundViewModel.swift`.

**State:**
```kotlin
data class RefundUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val refundSuccess: Boolean = false,
    val refundRequest: RefundRequestData? = null,
    val selectedReason: RefundReason? = null,
    val customReason: String = "",
    val additionalDetails: String = "",
    val isPartialRefund: Boolean = false,
    val partialAmount: String = "",
)
```

**Constants:**
```kotlin
const val MINIMUM_REASON_LENGTH = 10
const val MAXIMUM_REASON_LENGTH = 500
const val MAXIMUM_DESCRIPTION_LENGTH = 1000
```

**Methods:**
- `submitRefundRequest(transactionId: Int)` — validates → POST `/payments/{id}/refund` → set `refundSuccess = true`
- `checkRefundStatus(transactionId: Int)` — GET `/payments/{id}/refund-status`; update `refundRequest` silently (no error display)
- `reset()` — clear all

**Computed:**
- `effectiveReason: String` — custom reason if `OTHER`, else preset display text
- `isValidRequest: Boolean` — reason length >= MINIMUM_REASON_LENGTH
- `reasonValidationMessage: String?` — error text if invalid
- `refundAmount: Double?` — parsed partial amount, or null for full refund

### `TransactionHistoryViewModel`

**State:**
```kotlin
data class TransactionHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
```

**Methods:**
- `loadTransactions(role: TransactionRole? = null)` — GET `/payments?role={role}`
- `refreshTransactions(role: TransactionRole? = null)` — delegates to `loadTransactions`

### `TransactionDetailViewModel`

**State:**
```kotlin
data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cancelSuccess: Boolean = false,
)
```

**Methods:**
- `loadTransaction(id: Int)` — GET `/payments/{id}`
- `cancelTransaction(transaction: Transaction)` — POST `/payments/{id}/cancel` → `cancelSuccess = true`

### `StripeConnectViewModel`

iOS: `StripeConnectViewModel.swift`.

**State:**
```kotlin
data class StripeConnectUiState(
    val status: StripeConnectStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showOnboarding: Boolean = false,
    val onboardingUrl: String? = null,
    val dashboardUrl: String? = null,
    val showDashboard: Boolean = false,
)
```

**Debounce / single-flight:** `loadStatus` must be debounced (2s interval) and single-flight to prevent concurrent requests. Use `Mutex` or similar.

**Methods:**
- `loadStatus(forceRefresh: Boolean = false)` — debounced; GET `/stripe/connect/status`
- `startOnboarding()` — checks `shouldStartOnboarding`; POST `/stripe/connect/onboard` → set `onboardingUrl`, `showOnboarding = true`
- `openDashboard()` — GET `/stripe/connect/dashboard` → set `dashboardUrl`, `showDashboard = true`
- `handleOnboardingReturn()` — hide sheet → `loadStatus(forceRefresh = true)`
- `handleDashboardDismiss()` — hide sheet, clear URL
- `clearError()`

**Static:**
- `shouldStartOnboarding(status: StripeConnectStatus?): Boolean` — `status?.isOnboarded != true`

### `ReceiptListViewModel`

**State:**
```kotlin
data class ReceiptListUiState(
    val receipts: List<PaymentReceipt> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
)
```

**Private:** `currentPage: Int`

**Methods:**
- `loadReceipts()` — page 1, resets list
- `loadMore()` — increments page, appends
- `refresh()` — delegates to `loadReceipts`

### `ReceiptDetailViewModel`

**State:**
```kotlin
data class ReceiptDetailUiState(
    val receiptUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
```

**Methods:**
- `loadReceipt()` — GET `/receipts/{id}` → set `receiptUrl`

---

## UI screens

### `PaymentMethodsScreen`

iOS: `PaymentMethodsView.swift`.

**Sections:**
1. **Info card** — secure payment notice with lock icon
2. **Payment methods list** — each card as `PaymentMethodCard` component
3. **Add new card button** — triggers `prepareAddPaymentMethod` → `AddPaymentMethodSheet`
4. **View transactions link** — navigates to `TransactionHistoryScreen`

**States:** loading spinner, empty state with add prompt, error with retry.

### `AddPaymentMethodSheet` (bottom sheet)

iOS: `AddPaymentMethodSheet.swift`.

**Content:**
1. Stripe branding section with lock icon
2. Info card listing requirements: card number, expiry, CVV, billing address
3. Setup instructions — 4 numbered steps
4. "Continue" button → `createSetupIntent` → present Stripe `PaymentSheet`
5. Encryption notice + "Powered by Stripe" branding

### `TransactionHistoryScreen`

iOS: `TransactionHistoryView.swift`.

**Layout:**
1. **Segmented picker** — `TransactionRole` filter (All / Shipper / Carrier)
2. **Transaction list** — `LazyColumn` with `TransactionRowView` items
3. **Empty state** when no transactions
4. **Error message** with retry
5. **Pull-to-refresh**

**`TransactionRowView` (inline):**
- Status icon with color
- Transaction description (or default text)
- Status badge
- Amount in currency
- Tap → `TransactionDetailScreen`

### `TransactionDetailScreen`

iOS: `TransactionDetailView.swift`. **Role-aware display.**

**Sections:**
1. **StatusHeaderView** — large status icon + description
2. **EarningsSummaryCard** (carrier role) — delivery earnings, service fee deduction, tip if present, carrier total
3. **AmountBreakdownCard** (shipper role) — base amount, service/platform fee, total paid
4. **ParticipantsCard** — shipper and carrier info
5. **TimelineCard** — transaction timeline (authorized → captured → completed etc.); hidden if pending
6. **PayoutStatusCard** — payout status + dates; visible if completed or has payout info
7. **RefundStatusCard** — refund request status; visible if refund request exists
8. **RefundInfoCard** — already-refunded info; visible if status == refunded
9. **ActionsCard** — role-aware:
   - Shipper: "Request Refund" → `RefundSheet`, "Cancel" → cancel dialog
   - Post-delivery: "Add Tip" → `TipSelectionScreen`

### `RefundSheet` (bottom sheet)

iOS: `RefundSheetView.swift`.

**Form sections:**
1. **Transaction info** — amount + status
2. **Refund type** — full / partial toggle; partial shows amount input with max validation
3. **Reason selection** — `RefundReason` preset picker; `OTHER` shows custom text input
4. **Character validation** — reason: 10–500 chars; description: 0–1000 chars
5. **Additional details** — optional text editor with char count
6. **Info notice** — legal disclaimer about review process
7. **Submit button** — enabled only when `isValidRequest`; shows processing spinner

### `TipSelectionScreen`

iOS: `TipSelectionView.swift`.

**Layout:**
1. **Header** — heart icon + "Thank {carrierName} for their delivery"
2. **Subtitle** — appreciation message
3. **Preset tip buttons** — $2, $5, $10, $15 (row of chips/buttons)
4. **Custom amount input** — numeric keyboard with dollar prefix
5. **Error message** display
6. **Action buttons** — "Add Tip ${amount}" (primary) + "Skip" (text)
7. **PaymentSheet presentation** for post-delivery tips (when `clientSecret` returned)
8. **Success alert** on completion

Validation: $1 minimum, $500 maximum.

### `PayoutSetupScreen`

iOS: `PayoutSetupView.swift`. Carrier-only.

**States:**
1. **Loading** — progress indicator + "Loading status..."
2. **Not setup** — requirements list (government ID, bank account, address info) + "Setup Payout" button → starts onboarding
3. **Partial setup** — incomplete status indicators + "Continue Setup" button → resumes onboarding
4. **Complete** — "Account Active" with checkmarks for charges/payouts enabled + "View Dashboard" button

**Info card:** security notice about Stripe handling.

**Sheets:**
- Onboarding: Custom Tab (Chrome) with Stripe onboarding URL (iOS uses SafariView)
- Dashboard: Custom Tab with Stripe dashboard URL

### `ReceiptListScreen`

iOS: `ReceiptListView.swift`.

**Layout:**
1. **List** of `ReceiptRow` components
2. **"Load More" button** at bottom when `hasMore`
3. **Empty state** — "No receipts yet"
4. **Error state** with retry button
5. **Loading indicator**
6. **Pull-to-refresh**

Pagination: 20 per page.

### `ReceiptDetailScreen`

iOS: `ReceiptDetailView.swift`.

**Layout:**
1. `ReceiptWebView` for receipt URL
2. Loading state
3. Error state with retry
4. Share button in top bar toolbar

### `ReceiptWebView`

iOS: `ReceiptWebView.swift` (WKWebView). Android: `AndroidView` wrapping `WebView`.

**Features:**
- Loads receipt PDF/HTML from URL
- Loading indicator overlay during navigation
- Error overlay with message
- Invalid URL error state
- Share button (Android share intent)
- No horizontal scroll

---

## Components

### `PaymentButton`

iOS: `PaymentButton.swift`. Used in match/booking detail.

**Sections:**
- `PaymentSummaryCard` — fee breakdown:
  - Pre-payment: calculated from `StripeConfigRepository` (`serviceFeeAmount`, `totalToPay`, `carrierReceives`)
  - Post-payment: from `transaction.amounts` API values
  - Shows: base amount → service/platform fee → total to pay + "Carrier receives $X" info line
- Status message (info or status text)
- Pay button with loading spinner
- Error message
- Payment status badge (if already paid)
- Success / error alerts

### `PaymentMethodCard`

iOS: `PaymentMethodCard.swift`.

**Layout:**
- Card brand icon with brand-specific color:
  - Visa → blue
  - Mastercard → orange
  - Amex → green
  - Discover → purple
  - Default → gray
- Card display name: `"Visa •••• 4242"`
- "Default" badge if `isDefault`
- Expiry date `"MM/YY"`
- Menu/overflow button:
  - "Set as default" (if not default)
  - "Remove" (destructive, with confirmation dialog)

### `PayoutStatusBadge`

Icon + text, color-coded by `PayoutStatus`. Optional info button with status explanation table.

### `RefundStatusBadge`

Icon + text, color-coded by `RefundStatus`. Optional info button.

### `RefundStatusCard`

**Layout:**
- Header: "Refund Request" + `RefundStatusBadge`
- Refund amount (or "Full Refund")
- Reason provided
- Admin notes (if any)
- Status message with icon:
  - pending: "Refund request is under review"
  - approved: "Refund has been approved"
  - rejected: "Refund request was denied"
  - processed: "Refund has been processed"
- Submission date

### `TipBadge`

Heart icon + "+$X.XX" in pink. Empty/hidden if amount = 0.

### `ReceiptRow`

iOS: `ReceiptRow.swift`.

**Layout:**
- Role icon: shipper = blue up arrow, carrier = green down arrow
- Receipt number
- Route description (`pickupCity → deliveryCity`) or `"from/to {partyName}"`
- Date formatted
- Amount (green text for carrier, normal for shipper)
- Tip amount badge (orange) if present

---

## Stripe SDK integration (Android)

**Dependency:** `com.stripe:stripe-android` (PaymentSheet, SetupIntent).

**Configuration:**
- `PaymentConfiguration.init(context, publishableKey)` — set from `StripeConfigRepository.loadAndApplyConfig()`
- Fallback to hardcoded key from build config if fetch fails

**PaymentSheet configuration:**
```kotlin
PaymentSheet.Configuration(
    merchantDisplayName = "Pasabayan",
    customer = PaymentSheet.CustomerConfiguration(
        id = customerId,
        ephemeralKeySecret = ephemeralKey,
    ),
    googlePay = PaymentSheet.GooglePayConfiguration(
        environment = if (config.isSandbox) PaymentSheet.GooglePayConfiguration.Environment.Test
                      else PaymentSheet.GooglePayConfiguration.Environment.Production,
        countryCode = "CA",
        currencyCode = config.currency.uppercase(),
    ),
    allowsDelayedPaymentMethods = false,
    // savedPaymentMethodOptInBehavior equivalent
)
```

**Return URL:** `"pasabayan://stripe-redirect"` — register in `AndroidManifest.xml` for 3D Secure redirect.

**Mock handling:** If `clientSecret` starts with `"mock_pi_"`, skip PaymentSheet presentation and proceed directly to confirm-capture (for testing).

---

## Payment flows

### 1. Payment flow (shipper pays)

```
DeliveryMatch
  ↓ PaymentButton.initiatePayment
PaymentViewModel.loadPaymentSheet(deliveryMatch)
  ↓ validate not already paid (isPaidTransactionStatus)
POST /payments { delivery_match_id, amount, currency }
  ↓ extract clientSecret (top-level or nested in data)
  ↓ skip for mock_pi_* OR:
Configure PaymentSheet (customer, Google Pay, saved cards)
  ↓
presentPaymentSheet(activity)
  ↓ user completes payment in Stripe UI
PaymentSheet result → handlePaymentSheetCompleted()
  ↓
POST /payments/confirm-capture { delivery_match_id }
  ↓
Transaction updated → paymentSuccess = true → refresh UI
```

### 2. Payment methods management

```
PaymentMethodsScreen
  ↓ loadPaymentMethods + loadDefaultPaymentMethod
GET /stripe/payment-methods → display list
GET /stripe/default-payment-method → mark default

User actions:
  Add → POST /stripe/setup-intent → SetupIntent PaymentSheet → refresh
  Remove → DELETE /stripe/payment-methods/{id} → refresh
  Set default → PUT /stripe/default-payment-method { payment_method_id } → refresh
```

### 3. Tipping flow (post-delivery)

```
TransactionDetailScreen (completed transaction)
  ↓ "Add Tip"
TipSelectionScreen
  ↓ select preset ($2/$5/$10/$15) or custom
POST /payments/{transactionId}/tip { amount }
  ↓
If clientSecret returned (post-delivery):
  → Setup PaymentSheet → present → handle result
Else (pre-delivery, included in payment):
  → showSuccess = true
```

### 4. Refund flow

```
TransactionDetailScreen (shipper)
  ↓ "Request Refund"
RefundSheet
  ↓ select reason + optional details
  ↓ validate (10-500 char reason, 0-1000 char description)
POST /payments/{transactionId}/refund { amount?, reason, description? }
  ↓
RefundRequestData returned (status: pending)
  ↓
Later: GET /payments/{transactionId}/refund-status
  ↓ display RefundStatusCard (pending → approved/rejected → processed)
```

### 5. Payout setup flow (carrier)

```
PayoutSetupScreen
  ↓
GET /stripe/connect/status
  ↓
If not onboarded:
  POST /stripe/connect/onboard → onboardingUrl
  ↓ open Chrome Custom Tab
  ↓ user completes Stripe Express onboarding
  ↓ return to app
  handleOnboardingReturn() → loadStatus(forceRefresh = true)
  ↓
If complete: show "Account Active" + "View Dashboard"
  GET /stripe/connect/dashboard → dashboardUrl → Custom Tab
```

### 6. Transaction history flow

```
TransactionHistoryScreen
  ↓ segmented picker (All / Shipper / Carrier)
GET /payments?role={role}
  ↓ display LazyColumn with TransactionRowView items
  ↓ pull-to-refresh
Tap row → TransactionDetailScreen
```

### 7. Receipts flow

```
ReceiptListScreen
  ↓
GET /receipts?page=1&per_page=20
  ↓ display with ReceiptRow items
"Load More" → GET /receipts?page=2&per_page=20 → append
  ↓
Tap receipt → ReceiptDetailScreen
  ↓
GET /receipts/{transactionId}
  ↓ extract receiptUrl
ReceiptWebView → display PDF/HTML
  ↓ share button via Android share intent
```

---

## Localization keys

All display strings must use `stringResource(R.string.key)`. Add to `res/values/strings_payments.xml` (English) and `res/values-fr/strings_payments.xml` (French).

**Key inventory (minimum):**

```xml
<!-- Transaction status -->
<string name="payments_status_pending">Pending</string>
<string name="payments_status_authorized">Authorized</string>
<string name="payments_status_captured">Captured</string>
<string name="payments_status_completed">Completed</string>
<string name="payments_status_refunded">Refunded</string>
<string name="payments_status_cancelled">Cancelled</string>
<string name="payments_status_failed">Failed</string>
<string name="payments_status_unknown">Unknown</string>

<!-- Refund status -->
<string name="payments_refund_pending">Pending</string>
<string name="payments_refund_approved">Approved</string>
<string name="payments_refund_rejected">Rejected</string>
<string name="payments_refund_processed">Processed</string>

<!-- Payout status -->
<string name="payments_payout_pending">Pending</string>
<string name="payments_payout_processing">Processing</string>
<string name="payments_payout_on_hold">On Hold</string>
<string name="payments_payout_scheduled">Scheduled</string>
<string name="payments_payout_completed">Completed</string>
<string name="payments_payout_failed">Failed</string>

<!-- Transaction role filter -->
<string name="payments_role_all">All</string>
<string name="payments_role_shipper">Shipper</string>
<string name="payments_role_carrier">Carrier</string>

<!-- Refund reasons -->
<string name="payments_refund_reason_damaged">Package was damaged during delivery</string>
<string name="payments_refund_reason_not_delivered">Package was never delivered</string>
<string name="payments_refund_reason_wrong_item">Wrong item was delivered</string>
<string name="payments_refund_reason_late">Delivery was significantly late</string>
<string name="payments_refund_reason_partial">Only partial items were delivered</string>
<string name="payments_refund_reason_other">Other</string>

<!-- Payment methods -->
<string name="payments_methods_title">Payment Methods</string>
<string name="payments_methods_add">Add Payment Method</string>
<string name="payments_methods_remove">Remove</string>
<string name="payments_methods_set_default">Set as Default</string>
<string name="payments_methods_default_badge">Default</string>
<string name="payments_methods_info">Your payment information is securely stored by Stripe.</string>
<string name="payments_methods_empty">No payment methods yet. Add a card to get started.</string>
<string name="payments_methods_view_transactions">View Transactions</string>

<!-- Add payment method sheet -->
<string name="payments_add_card_title">Add Card</string>
<string name="payments_add_card_requirements">You will need your card number, expiry date, CVV, and billing address.</string>
<string name="payments_add_card_step1">Tap Continue below</string>
<string name="payments_add_card_step2">Enter your card details</string>
<string name="payments_add_card_step3">Confirm your billing address</string>
<string name="payments_add_card_step4">Save your card</string>
<string name="payments_add_card_continue">Continue</string>
<string name="payments_add_card_encrypted">Your card details are encrypted end-to-end.</string>
<string name="payments_add_card_powered_by">Powered by Stripe</string>

<!-- Transaction history -->
<string name="payments_history_title">Transaction History</string>
<string name="payments_history_empty">No transactions yet.</string>

<!-- Transaction detail -->
<string name="payments_detail_title">Transaction Details</string>
<string name="payments_detail_earnings">Delivery Earnings</string>
<string name="payments_detail_service_fee">Service Fee</string>
<string name="payments_detail_carrier_total">You Receive</string>
<string name="payments_detail_base_amount">Base Amount</string>
<string name="payments_detail_platform_fee">Platform Fee</string>
<string name="payments_detail_total_paid">Total Paid</string>
<string name="payments_detail_participants">Participants</string>
<string name="payments_detail_timeline">Timeline</string>
<string name="payments_detail_payout">Payout</string>
<string name="payments_detail_actions">Actions</string>
<string name="payments_detail_request_refund">Request Refund</string>
<string name="payments_detail_add_tip">Add Tip</string>
<string name="payments_detail_cancel">Cancel Transaction</string>
<string name="payments_detail_carrier_receives">Carrier receives %s</string>

<!-- Refund sheet -->
<string name="payments_refund_title">Request Refund</string>
<string name="payments_refund_full">Full Refund</string>
<string name="payments_refund_partial">Partial Refund</string>
<string name="payments_refund_amount_label">Refund Amount</string>
<string name="payments_refund_reason_label">Reason</string>
<string name="payments_refund_details_label">Additional Details (Optional)</string>
<string name="payments_refund_disclaimer">Refund requests are reviewed by our team. Processing may take 5-10 business days.</string>
<string name="payments_refund_submit">Submit Refund Request</string>
<string name="payments_refund_reason_min">Reason must be at least %d characters</string>
<string name="payments_refund_reason_max">Reason must be less than %d characters</string>
<string name="payments_refund_success">Refund request submitted successfully.</string>

<!-- Refund status card -->
<string name="payments_refund_status_title">Refund Request</string>
<string name="payments_refund_status_under_review">Refund request is under review</string>
<string name="payments_refund_status_approved_msg">Refund has been approved</string>
<string name="payments_refund_status_rejected_msg">Refund request was denied</string>
<string name="payments_refund_status_processed_msg">Refund has been processed</string>
<string name="payments_refund_admin_notes">Admin Notes</string>

<!-- Tip selection -->
<string name="payments_tip_title">Add a Tip</string>
<string name="payments_tip_thank">Thank %s for their delivery</string>
<string name="payments_tip_subtitle">Show your appreciation with a tip</string>
<string name="payments_tip_custom">Custom Amount</string>
<string name="payments_tip_add_button">Add Tip %s</string>
<string name="payments_tip_skip">Skip</string>
<string name="payments_tip_success">Tip added successfully!</string>
<string name="payments_tip_min_max">Tip must be between $%s and $%s</string>

<!-- Payout setup -->
<string name="payments_payout_title">Payout Setup</string>
<string name="payments_payout_loading">Loading status…</string>
<string name="payments_payout_not_setup_title">Set Up Payouts</string>
<string name="payments_payout_requirement_id">Government-issued ID</string>
<string name="payments_payout_requirement_bank">Bank account information</string>
<string name="payments_payout_requirement_address">Address information</string>
<string name="payments_payout_setup_button">Setup Payout</string>
<string name="payments_payout_continue_button">Continue Setup</string>
<string name="payments_payout_active">Account Active</string>
<string name="payments_payout_dashboard_button">View Dashboard</string>
<string name="payments_payout_security">Your financial information is securely handled by Stripe.</string>

<!-- Receipts -->
<string name="payments_receipts_title">Receipts</string>
<string name="payments_receipts_empty">No receipts yet</string>
<string name="payments_receipts_load_more">Load More</string>
<string name="payments_receipts_share">Share Receipt</string>

<!-- Payment button / summary -->
<string name="payments_summary_service_fee">Service Fee (%s%%)</string>
<string name="payments_summary_total">Total</string>
<string name="payments_summary_carrier_receives">Carrier receives %s</string>
<string name="payments_pay_button">Pay %s</string>
<string name="payments_processing">Processing…</string>
<string name="payments_success">Payment successful!</string>

<!-- Errors -->
<string name="payments_error_generic">Something went wrong. Please try again.</string>
<string name="payments_error_permission">You don\'t have permission to perform this action.</string>
<string name="payments_error_network">Network error. Check your connection.</string>
<string name="payments_error_cancelled">Payment was cancelled.</string>
<string name="payments_error_invalid_amount">Invalid payment amount.</string>

<!-- Card brands -->
<string name="payments_brand_visa">Visa</string>
<string name="payments_brand_mastercard">Mastercard</string>
<string name="payments_brand_amex">American Express</string>
<string name="payments_brand_discover">Discover</string>
<string name="payments_brand_unknown">Card</string>
```

---

## TDD checklist

- [x] **`TransactionModelsTest`** — decode `Transaction`, `TransactionAmounts` (flexible double), `CreatePaymentResponse` (top-level vs nested `clientSecret`)
- [x] **`PaymentReceiptModelsTest`** — decode receipt with nested objects, computed properties
- [x] **`StripeConfigModelsTest`** — decode config, computed `isSandbox`/`isLive`, fee calculations (`totalToPay`, `serviceFeeAmount`, `carrierReceives`)
- [x] **`StripeConnectModelsTest`** — decode status, computed `isOnboarded`/`canPayout`
- [x] **`PaymentMethodModelsTest`** — decode `PaymentMethodsResponse`, `SetupIntentResponse`, `DefaultPaymentMethodResponse`
- [x] **`PaymentRepositoryTest`** — MockWebServer; create payment, list, get, capture, confirm-capture, release, refund, refund-status, cancel, tip
- [x] **`StripeConfigRepositoryTest`** — fetch config, fee calculations with defaults vs server values, price validation
- [x] **`StripeConnectRepositoryTest`** — onboard, status, dashboard; error message → enum mapping
- [x] **`ReceiptRepositoryTest`** — fetch paginated list, single receipt, `hasMore` computation
- [x] **`PaymentMethodsRepositoryTest`** — list, setup-intent, default, set-default, delete
- [x] **`PaymentViewModelTest`** — payment flow states, mock secret handling, confirm-capture, cancel
- [x] **`PaymentMethodsViewModelTest`** — load, add (setup intent), remove, set default, sort with default first
- [x] **`TippingViewModelTest`** — preset selection, custom amount, validation ($1–$500), tip with/without clientSecret, reset
- [x] **`RefundViewModelTest`** — reason selection, validation (10–500 chars), partial vs full, submit, status check, reset
- [x] **`TransactionHistoryViewModelTest`** — load with role filter, refresh
- [x] **`TransactionDetailViewModelTest`** — load, cancel
- [x] **`StripeConnectViewModelTest`** — debounced loadStatus, single-flight, onboarding flow, dashboard, return handling
- [x] **`ReceiptListViewModelTest`** — load, loadMore pagination, refresh
- [x] **`ReceiptDetailViewModelTest`** — load receipt URL
- [x] **`TransactionStatusTest`** — enum parsing, displayName, color, icon for all cases + unknown fallback
- [x] **`RefundReasonTest`** — displayText, requiresCustomInput for each case
- [x] **`PaymentErrorMappingTest`** — HTTP 403 → PERMISSION_DENIED, requires_authentication extraction, validation errors extraction
