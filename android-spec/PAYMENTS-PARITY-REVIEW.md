# Payments — iOS↔Android Parity Review

**Date:** 2026-05-13
**Branch:** `feature/initial-android-spec`
**Scope:** Phase 4 — Payments, Stripe, receipts
**Spec:** [`06-payments-stripe.md`](06-payments-stripe.md)
**iOS reference:** `/Users/efthemios/Documents/projects/pasabayan/pasabayan-ios/Pasabayan/Features/Payments/`
**Android impl:** `app/src/main/java/com/efthemiosprime/pasabayan/features/payments/` + `core/network/.../payments/`

---

## Executive Summary

Phase 4 (Payments) is **scaffolding-complete**, not **feature-complete**. The foundation — Retrofit APIs, repositories, Hilt bindings, network JSON models, flexible decoders, EN/FR strings — is solid and matches the spec. Gaps cluster in four areas:

1. **Feature-level domain models are reductive** — spec-defined nested types flattened to scalar fields.
2. **ViewModels are stubbed** — `PaymentViewModel` (224 lines vs iOS 669) and `PaymentMethodsViewModel` (129 vs 685) are missing setup-intent, add-card, 3DS, capture/refund/tip orchestration.
3. **UI screens missing** — 5 of 10 spec'd screens absent (Add card sheet, Refund sheet, Tip selection, Receipt list, Receipt detail, Receipt WebView).
4. **Reusable component library missing** — spec defines 8 primitives; Android has 0 and instead exposes 5 profile-section composables embedded in `PaymentsProfileScreen`.

`IMPLEMENTATION-STATUS.md` currently claims Phase 4 "Complete" — this is **overstated** relative to the spec and should be revised to "Scaffolding complete."

This is consistent with the current branch focus (Phase 3 — bookings/chat) and a spec-first, staggered-implementation approach.

---

## 1. Models / DTOs

### 1a. Network JSON Layer (`:core:network/payments`) — ✅ Solid

| Model | Status | Notes |
|---|---|---|
| `TransactionJson` + nested (`TransactionUserJson`, `TransactionAmountsJson`, `StripeInfoJson`, `RefundInfoJson`, `PayoutJson`, `TipInfoJson`) | ✅ | `core/network/.../payments/TransactionJsonModels.kt` |
| Request/response DTOs (Create, ConfirmCapture, Refund, Cancel, Tip, Action, TransactionList) | ✅ | Includes dual-path `client_secret` deserializer matching iOS |
| `StripeConfigJson` / `StripeConfigResponseJson` | ✅ | `StripeConfigJsonModels.kt` |
| `StripeConnectStatusJson`, onboarding/dashboard responses | ✅ | `StripeConnectJsonModels.kt` |
| Receipt models (`PaymentReceiptJson`, `OtherPartyJson`, etc.) | ✅ | `ReceiptJsonModels.kt` |
| Payment-methods JSON (`PaymentMethodApiJson`, `CardDetailsJson`, `SetupIntentDataJson`, ...) | ✅ | `PaymentMethodsJsonModels.kt` |
| `PaymentApiErrorResponseJson` | ✅ | `PaymentErrorJsonModels.kt` |

- **Flexible decoder (string-or-number money):** ✅ `FlexibleDoubleSerializer` applied to all money fields (`total`, `subtotal`, `platformFee`, `carrierReceives`, `tip`, `tax`, `carrierTotal`, `baseAmount`, refund/tip amounts). Matches iOS custom decoder per spec §150.
- **`@SerialName` snake_case:** ✅ Consistent.
- **Money type:** ✅ `Double` per spec (no `Float`).

### 1b. Feature-level Domain Models — ⚠️ Reductive

| Spec'd Model | Status | Gap |
|---|---|---|
| `Transaction` | ⚠️ Stub | `features/payments/model/Transaction.kt` is ~30 lines / ~15 flat fields vs iOS struct's ~60 fields across 8 nested objects. Nesting flattened to scalars (`shipperName`, `carrierName`, `payoutStatus`, etc.). |
| `TransactionAmounts` | ❌ Missing | Spec §135–149: 9 fields. Collapsed into `Transaction` scalars. |
| `TransactionUser` | ❌ Missing | Spec §154–161. Only `shipperName`/`carrierName` strings; no id/email/avatar. |
| `StripeInfo` | ❌ Missing | Spec §165–173. Stripe IDs (paymentIntent/transfer/charge/refund) only in JSON layer. |
| `RefundInfo` | ❌ Missing | Spec §176–182. Only JSON-layer. |
| `TransactionTimestamps` | ❌ Missing | Spec §186–196. `Transaction` carries only `createdAt`/`updatedAt`. |
| `Payout` | ❌ Missing | Spec §200+. Scattered as flat fields on `Transaction`. |
| `TipInfo` | ❌ Missing | Spec §210+. Only `tipAmount` scalar. |
| `StripeConfig` | ✅ Present | `features/payments/model/StripeConfig.kt` — complete, with `isSandbox`/`isLive` computeds. |
| `PaymentReceipt` | ⚠️ Stubbed | Nested types (OtherParty, Amount, Delivery) likely not mapped. |
| `PaymentMethodDisplay` | ✅ Present | Carries id/brand/last4/expiry/isDefault. |
| `StripeConnectError` | ✅ Present | Sealed class mapping server error strings to typed cases. |

**Impact:** Rich UI (carrier row in transaction detail, fee breakdown, payout timeline, metadata-driven cells) is blocked. Mapper in `PaymentMappers.kt` is reductive.

---

## 2. Repositories / Network

### Repositories — ✅ Complete surface

| Repository | Methods | File |
|---|---|---|
| `PaymentRepository` | 9 (`createPayment`, `listTransactions`, `getTransaction`, `captureTransaction`, `confirmCapture`, `releaseTransaction`, `requestRefund`, `getRefundStatus`, `cancelTransaction`, `addTip`) | `services/PaymentRepository.kt` + Impl |
| `StripeConfigRepository` | 2 (`fetchConfig`, `refreshConfig`) | `services/StripeConfigRepository.kt` + Impl |
| `StripeConnectRepository` | 4 (`getStatus`, `forceRefresh`, `startOnboarding`, `getDashboardLink`) | `services/StripeConnectRepository.kt` + Impl |
| `ReceiptRepository` | 2 (`loadReceipts`, `loadReceipt`) | `services/ReceiptRepository.kt` + Impl |
| `PaymentMethodsRepository` | 5 (`loadPaymentMethods`, `loadDefaultPaymentMethod`, `removePaymentMethod`, `setDefaultPaymentMethod`, `createSetupIntent`) | `services/PaymentMethodsRepository.kt` + Impl |

### Retrofit APIs (`:core:network`) — ✅ Complete

| API | Endpoints |
|---|---|
| `PaymentApi` | 10 endpoints — all POST/GET routes per spec |
| `StripeConfigApi` | `GET /payments/config` |
| `ReceiptApi` | `GET /receipts`, `GET /receipts/{id}` |
| `PaymentMethodsApi` | 5 endpoints (Stripe payment-methods + setup-intent + default) |
| `StripeConnectApi` | 3 endpoints (onboard/status/dashboard) |

**Hilt module:** `PaymentsModule.kt` binds all 5 repos as `@Singleton`. ✅

**Note on placement:** Spec §51–67 puts Retrofit interfaces under `features/payments/services/`. Repo follows `00-architecture.md` and places them under `:core:network`. Reasonable divergence — call out in spec or in commentary.

**Gap:** JSON→domain mappers in `PaymentMappers.kt` flatten the rich JSON structure; widen when domain models are filled in (see §1b).

---

## 3. ViewModels

| ViewModel | Lines (Android) | Lines (iOS) | Status |
|---|---|---|---|
| `PaymentViewModel` | 224 | 669 | ⚠️ Stub — missing setup-intent state, card form, 3DS branching, capture/refund/tip state machines |
| `PaymentMethodsViewModel` | 129 | 685 | ⚠️ Stub — no add-card sheet state, no setup-intent secret routing, no form validation |
| `TippingViewModel` | 98 | 142 | ⚠️ Minimal — covers basics |
| `RefundViewModel` | 114 | (inline on iOS) | ✅ Android-only split, reasonable |
| `TransactionHistoryViewModel` | 44 | (inline on iOS) | ⚠️ Skeletal |
| `TransactionDetailViewModel` | 57 | (inline on iOS) | ⚠️ Skeletal |
| `StripeConnectViewModel` | 160 | 178 | ✅ Includes debounce/Mutex parity with iOS |
| `ReceiptListViewModel` | 92 | 99 | ✅ Comparable surface |
| `ReceiptDetailViewModel` | 41 | 47 | ✅ Minimal but parity |

**Critical deltas:**

- **`PaymentViewModel` (-445 lines vs iOS):** Missing payment-sheet lifecycle (Stripe key injection, mock-secret detection, sheet initialization), 3DS challenge state, post-confirmation polling. Most flow delegated to SDK + repo with no VM mediation.
- **`PaymentMethodsViewModel` (-556 lines vs iOS):** No add-card flow, no setup-intent state, no card form validation, no sheet route management. Only list/remove/default.

---

## 4. UI / Screens

Spec lists 10 screens. Android delivers 5 implemented + 1 hub.

| Screen | Status | Notes |
|---|---|---|
| `PaymentMethodsScreen` | ❌ Missing | Should be full card list + add-card UI. Currently a *section* embedded in `PaymentsProfileScreen`. |
| `AddPaymentMethodSheet` | ❌ Missing | Setup-intent + card form flow not built. |
| `TransactionHistoryScreen` | ✅ Present | `ui/TransactionHistoryScreen.kt` (~276 lines) — also reachable from `PaymentsProfileScreen`. |
| `TransactionDetailScreen` | ✅ Present | `ui/TransactionDetailScreen.kt` (~302 lines). |
| `RefundSheet` | ❌ Missing | No modal sheet for refund reason/amount. |
| `TipSelectionScreen` | ❌ Missing | No preset/custom tip picker. |
| `PayoutSetupScreen` | ✅ Present | `ui/PayoutSetupScreen.kt` (~591 lines) — full Stripe Connect onboarding with 4 iOS-parity states. |
| `ReceiptListScreen` | ❌ Missing | Currently only as section inside `PaymentsProfileScreen`. |
| `ReceiptDetailScreen` | ❌ Missing | — |
| `ReceiptWebView` | ❌ Missing | No WebView host for receipt HTML. |
| `PaymentsProfileScreen` (Android-only hub) | ✅ Present | `ui/PaymentsProfileScreen.kt` (~441 lines) — aggregates payment surfaces; reasonable adaptation, but underlying screens still need to exist as standalone composables. |

---

## 5. Components

Spec §89–97 lists 8 reusable primitives. Android currently has **0 reusable primitives** and **5 profile-section composables** (which are not the spec's intent).

| Spec'd Primitive | Status |
|---|---|
| `PaymentButton` | ❌ Missing (uses generic `PButton`) |
| `PaymentMethodCard` | ❌ Missing (rendered inline in `PaymentMethodsSection`) |
| `PaymentSummaryCard` | ❌ Missing |
| `PayoutStatusBadge` | ❌ Missing (inline in `StripeConnectSection`) |
| `RefundStatusBadge` | ❌ Missing |
| `RefundStatusCard` | ❌ Missing |
| `TipBadge` | ❌ Missing |
| `ReceiptRow` | ❌ Missing |

**Present (not in spec):**
- `PaymentMethodsSection.kt` (75 lines) — profile-screen section
- `PaymentProcessingSection.kt` (108 lines)
- `PaymentsActivitySection.kt` (119 lines)
- `StripeConnectSection.kt` (129 lines)
- `TippingRefundSection.kt` (107 lines)
- `CustomTabsLauncher.kt` (Chrome Custom Tabs helper)

**Action:** Extract 8 reusable composables (built from `P*` + `Pasabayan*` tokens, with light + dark previews per `14-design-system.md`). Have profile sections compose them. Consider promoting widely-used ones to `:core:designsystem` per the reuse-promotion ladder.

---

## 6. Localization — ✅ Complete

- `res/values/strings_payments.xml` (~99 lines EN)
- `res/values-fr/strings_payments.xml` (FR)
- No hardcoded `Text("...")` literals found in `features/payments/` Kotlin files
- Strings keyed `payments_*` per `18-localization.md`

---

## 7. Tests

### Present

| Test File | Scope |
|---|---|
| `app/src/test/.../features/payments/viewmodel/PaymentViewModelsTest.kt` (~369 lines) | `PaymentViewModel`, `PaymentMethodsViewModel`, `StripeConnectViewModel` state flows |
| `app/src/test/.../features/payments/viewmodel/TippingRefundViewModelsTest.kt` | `TippingViewModel`, `RefundViewModel` |
| `app/src/test/.../features/payments/viewmodel/RemainingViewModelsTest.kt` | `ReceiptListViewModel`, `ReceiptDetailViewModel`, `TransactionHistoryViewModel`, `TransactionDetailViewModel` |
| `app/src/test/.../features/payments/model/PaymentModelsTest.kt` | Domain model mapping / formatters |
| `app/src/test/.../features/payments/services/PaymentRepositoryImplTest.kt` | Repository impl + error handling |
| `app/src/test/.../features/payments/services/ConnectMethodsReceiptRepoTest.kt` | Connect/Methods/Receipt repos |
| `core/network/src/test/.../payments/PaymentJsonModelsDecodeTest.kt` (~182 lines) | Flexible-double + dual-path `client_secret` |
| `core/network/src/test/.../payments/PaymentErrorMappingTest.kt` | Error response mapping |
| `core/network/src/test/.../payments/ConnectMethodsReceiptDecodeTest.kt` | Connect/Methods/Receipt JSON decode |

**Fixtures:** `transaction_full.json`, `create_payment_response.json` + nested-secret variant, `payment_error_response.json`, `refund_status_response.json`, `connect_status.json`, `payment_methods.json`, `stripe_config.json`, `set_default_payment_method_response.json`, `receipt.json`.

### Gaps

- VM coverage is sample-level; full state-machine coverage missing for `PaymentViewModel` (payment-sheet lifecycle) and `PaymentMethodsViewModel` (add-card flow).
- No `androidTest` Compose tests for payments screens/components.
- No mapper tests once domain models are widened (§1b).

---

## 8. IMPLEMENTATION-STATUS.md

Status doc currently claims Phase 4 **"Complete"** — PaymentSheet parity ✅, transactions parity ✅, Stripe Connect parity ✅, dedicated `PayoutSetupScreen` ✅, Chrome Custom Tabs ✅, EN/FR strings ✅.

**Reality check:** Network + repos + PayoutSetup + payment-creation wire-up + receipt listing are correct. But 5 of 10 screens absent, 8 of 8 reusable components absent, domain models reductive, VM orchestration stubbed.

**Recommendation:** Update Phase 4 entry to **"Scaffolding complete — pending: add-card flow, refund/tip sheets, receipt detail flows, reusable component library, rich domain models."**

---

## 9. Phase Context

- Branch `feature/initial-android-spec`, recent commits focus on Phase 3 (bookings, chat, matching).
- `PHASES-AND-FEATURES.md` places Payments at Phase 4.
- The Payments code on `master`-track is **spec'd in advance, implementation deferred** — consistent with the staggered plan. The "Complete" label in `IMPLEMENTATION-STATUS.md` is the only true inconsistency.

---

## Top 5 Gaps (Prioritized)

### 1. Domain models are reductive — **Critical**

- **Issue:** `features/payments/model/Transaction.kt` (~30 lines, 15 flat fields) collapses iOS's nested 60-field struct.
- **Missing types:** `TransactionUser`, `TransactionAmounts`, `StripeInfo`, `RefundInfo`, `TransactionTimestamps`, `Payout`, `TipInfo`.
- **Impact:** Blocks rich UI (carrier row, fee breakdown, payout timeline) and metadata-driven cells.
- **Spec:** §104–217.
- **Action:** Extract 7 nested data classes; update `PaymentMappers.toDomain()`; expand `Transaction`.

### 2. ViewModel orchestration is stubbed — **High**

- **Issue:** `PaymentViewModel` (224 vs 669) and `PaymentMethodsViewModel` (129 vs 685) under-implement state machines.
- **Missing:** setup-intent flow, add-card sheet state, card form validation, 3DS challenge, capture/refund/tip state polling.
- **Action:** Widen VM state; add suspend flows for setup-intent + 3DS confirmation polling; add tests.

### 3. UI screens missing — **High**

- **Missing as composables:** `AddPaymentMethodSheet`, `RefundSheet`, `TipSelectionScreen`, `ReceiptListScreen`, `ReceiptDetailScreen`, `ReceiptWebView`.
- **Action:** Build per spec §78–88, wire into navigation, include light+dark previews.

### 4. Reusable component library missing — **High**

- **Issue:** 0 of 8 spec'd primitives present; only profile sections.
- **Action:** Extract `PaymentButton`, `PaymentMethodCard`, `PaymentSummaryCard`, `PayoutStatusBadge`, `RefundStatusBadge`, `RefundStatusCard`, `TipBadge`, `ReceiptRow` from `P*` primitives; refactor profile sections to compose them; previews + tests.

### 5. Metadata & rich timestamps not modeled — **Low**

- **Issue:** `metadata: Map<String, Any>?` (spec §118) and `TransactionTimestamps` (spec §186–196) not in domain.
- **Action:** Add `TransactionTimestamps` domain type and `Map<String, JsonElement>` metadata once §1 is addressed.

---

## Coverage Snapshot

| Area | Spec Req | Android Status | Completeness |
|---|---|---|---|
| Network JSON models | ~30 types | ~28 types | ~95% |
| Domain models | ~15 types | 7 types | ~47% |
| Repositories | 5 | 5 | 100% |
| Network APIs | 24 endpoints | 24 endpoints | 100% |
| ViewModels | 9 | 9 (state shallow) | ~40% surface |
| Screens | 10 | 5 (incl. hub) | ~50% |
| Reusable components | 8 | 0 | 0% |
| Localization | EN + FR | EN + FR | 100% |
| Tests | VM + model + repo + JSON | All present | ~85% |
| `IMPLEMENTATION-STATUS.md` | Accurate | Overstated | Needs revision |
