# Payments — Gap-Closure Implementation Plan

**Date:** 2026-05-13
**Branch:** `feature/initial-android-spec`
**Companion audit:** [PAYMENTS-PARITY-REVIEW.md](PAYMENTS-PARITY-REVIEW.md)
**Spec:** [06-payments-stripe.md](06-payments-stripe.md)
**Slicing rules:** layer-order (Model → Repo → VM → UI → Wiring), TDD red→green→refactor, stop after each slice for user commit, files < 200 lines aim.

---

## Decision call-outs (resolve before Phase A)

| # | Decision | Default | Trade-off |
|---|---|---|---|
| D1 | `RefundReason` already lives in `core/domain/enum/` — keep there? | **Reuse** core enum; move display strings to a feature helper. | Avoids fork; unblocks FR. |
| D2 | `PayoutStatus` / `RefundStatus` / `TransactionStatus` placement | **Reuse** existing core enums; add Android `StatusBadgeConfig` adapters in feature components (mirrors `UrgencyBadgeConfig`). | Cross-feature reuse of `PStatusBadge`. |
| D3 | `Money` / `Currency` helpers — core util or feature-local? | **Feature-local** `MoneyFormatter.kt`; promote on 2nd cross-feature consumer. | No speculative core surface. |
| D4 | `metadata: Map<String, Any>?` representation | `Map<String, JsonElement>?` (kotlinx native). | Avoids `Any?`; schema-drift safe. |
| D5 | `TransactionUser` placement | **Feature-local**; do NOT reuse `core/domain/model/UserSummary` (different shape: has email/avatar, no rating/verification). | Small duplication; clean schema. |
| D6 | `ReceiptWebView` placement | **Feature-local**; defer `:core:webview` until 2nd consumer. | Avoids premature module. |
| D7 | Component placement (8 primitives) | **Feature-local** under `features/payments/components/`. | None used outside payments today. |
| D8 | Should `Transaction` keep flat fallback fields during transition? | **Hard cut** in one slice; fix all consumers same slice. | Slightly bigger A2 slice; clean afterwards. |
| D9 | `RefundUiState.refundRequest: RefundRequestDataJson` leaks JSON into VM | Add domain `RefundRequest` in A4; swap in B4. | Two slices, removes real spec violation. |
| D10 | `RefundReason.displayText` is English-hardcoded | Move to Composable helper `refundReasonDisplayText(reason): String` using `stringResource`. | Keeps core enum string-free. |
| D11 | Full screens vs `PModalBottomSheet` | iOS match: `AddPaymentMethodSheet` + `RefundSheet` = `PModalBottomSheet`. `TipSelectionScreen`, `ReceiptListScreen`, `ReceiptDetailScreen`, `PaymentMethodsScreen` = full screens. | Different back-stack semantics — see R3. |

---

## Phase A — Domain models + mappers (closes audit §1)

| # | Title | Layer | Size | Deps |
|---|---|---|---|---|
| A1 | `feat(payments): add nested Transaction domain types` | Model | S | — |
| A2 | `feat(payments): widen Transaction + mapper (drop flat fallback fields)` | Model + Mapper | M | A1 |
| A3 | `feat(payments): expand PaymentReceipt domain (OtherParty, Amount, Delivery)` | Model + Mapper | S | — |
| A4 | `feat(payments): add RefundRequest domain + map RefundRequestDataJson` | Model + Mapper | S | — |
| A5 | `feat(payments): add MoneyFormatter + Currency helpers (feature-local)` | Model | S | — |
| A6 | `feat(payments): localize RefundReason via Composable helper + strings` | Model + Strings | S | — |

### A1 — Nested transaction types

**Create:** `TransactionUser.kt`, `TransactionAmounts.kt`, `StripeInfo.kt`, `RefundInfo.kt`, `TransactionTimestamps.kt`, `Payout.kt` (uses core `PayoutStatus`), `TipInfo.kt` — one file each under `features/payments/model/`.

**Tests:** `TransactionAmountsTest.kt` (computed `senderFee`, `carrierFee`, `hasTwoSidedBreakdown`), `TransactionTimestampsTest.kt`, extend `PaymentModelsTest.kt`.

**Exit:** 7 types compile + green; no consumers yet.

### A2 — Widen `Transaction`

**Modify:** `Transaction.kt`, `PaymentMappers.kt`, plus consumers: `PaymentsActivitySection`, `TippingRefundSection`, `TransactionHistoryScreen`, `TransactionDetailScreen`, `PaymentsProfileScreen`.

**New shape (spec §104–129):** `id, shipper, carrier, deliveryMatchId, amounts, stripe, transactionStatus, description, metadata: Map<String, JsonElement>?, refund, timestamps, clientSecret, payoutStatus: PayoutStatus?, payoutNotes, payoutCompletedAt, payout, tip, customerId, ephemeralKey`.

**Tests:** rewrite `PaymentModelsTest.kt`; new `TransactionMapperTest.kt` against `transaction_full.json` fixture.

**Exit:** Build green; no flat-field shims remain.

### A3 — `PaymentReceipt` nested

**Create:** `OtherParty.kt`, `PaymentReceiptAmount.kt`, `PaymentReceiptDelivery.kt`.

**Modify:** `PaymentReceipt.kt` (compose nested + computed `isShipper`, `routeDescription`, `displayAmount`, `url: Uri?`); `PaymentMappers.kt`.

**Tests:** `PaymentReceiptMapperTest.kt` against `receipt.json` — route fallback + signed amounts.

### A4 — `RefundRequest` domain

**Create:** `RefundRequest.kt` (parsed status as `RefundStatus`, `RefundReason?`).

**Modify:** `PaymentMappers.kt` (add `RefundRequestDataJson.toDomain()`). VM consumer change deferred to B4.

**Tests:** `RefundRequestMapperTest.kt`.

### A5 — `MoneyFormatter`

**Create:** `features/payments/model/MoneyFormatter.kt` — `formatCurrency(Double, currency)` via `NumberFormat.getCurrencyInstance` + `Currency.getInstance(currency.uppercase())`; `formatSignedCurrency` for tip/refund.

**Tests:** CAD/USD/missing-currency fallback/negative sign.

### A6 — Localize `RefundReason`

**Create:** `RefundReasonStrings.kt` — `@Composable refundReasonDisplayText(reason: RefundReason): String`.

**Verify:** `res/values/strings_payments.xml` + `values-fr/` keys `payments_refund_reason_*` (spec §1466).

**Replace** any `reason.displayText` call site with the helper.

---

## Phase B — VM orchestration deepening (closes audit §2)

| # | Title | Size | Deps |
|---|---|---|---|
| B1 | `feat(payments): widen PaymentMethodsViewModel with add-card flow (setup-intent + sheet state)` | M | — |
| B2 | `feat(payments): widen PaymentViewModel — sheet config + 3DS + post-confirm polling` | M | — |
| B3 | `feat(payments): tip orchestration — post-delivery secret routing + PaymentSheet bridge` | S | B2 |
| B4 | `feat(payments): RefundViewModel uses RefundRequest domain + checkRefundStatus polling` | S | A4 |
| B5 | `feat(payments): TransactionDetailViewModel exposes role-aware computed surface` | S | A2 |

### B1 — Add-card flow in `PaymentMethodsViewModel`

**New state:** `setupIntentClientSecret`, `setupIntentCustomerId`, `setupIntentEphemeralKey`, `addCardFlowState: AddCardFlowState` (sealed: IDLE/PREPARING/READY/PRESENTING/SUCCESS/FAILED), `showAddCardSheet`.

**New methods:** `prepareAddPaymentMethod()`, `onAddCardSheetPresented()`, `onAddCardCompleted()`, `onAddCardCanceled()`, `onAddCardFailed(msg)`, `showAddCardSheet(show)`.

**iOS mirror:** `PaymentMethodsViewModel.swift:124–280`.

**Tests:** setup-intent success/failure; reload after add; default-first sort intact.

### B2 — Sheet config + 3DS in `PaymentViewModel`

**New state:** `paymentSheetConfig: PaymentSheetConfig?`, `requiresAuthentication`, `authenticationClientSecret`, `pollAttempt`.

**New methods:** `configurePaymentSheet(...)`, `handle3DSChallenge(...)`, `pollForConfirmation(matchId, attempts=3, delayMs=1500)`, `onStripeSDKError(message)`.

**Extract:** shared `PaymentSheetConfigFactory.kt` for B3 reuse.

**iOS mirror:** `PaymentViewModel.swift:41–250`.

**Tests:** 3DS branch extracts client_secret; mock-secret short-circuit unchanged; publishable key applied; polling retries + gives up.

### B3 — `TippingViewModel` PaymentSheet bridge

**New state:** `paymentSheetReady`, `paymentSheetResult: PaymentSheetResultKind?`, `updatedTransaction: Transaction?`.

**New methods:** `setupPaymentSheet(clientSecret)`, `handlePaymentResult(kind, message)`.

**Tests:** preset→custom→submit; success without secret; success with secret routes to sheet; failure clears secret.

### B4 — `RefundViewModel` domain swap + polling

**Modify:** `refundRequest: RefundRequestDataJson?` → `RefundRequest?`; add `checkRefundStatus(transactionId)` (GET `/payments/{id}/refund-status`, **silent** failure per spec §955).

**Tests:** silent failure preserves prior state; status overwrite.

### B5 — `TransactionDetailViewModel` computeds

**New state:** `viewerRole: TransactionRole?` + computed flags `showEarningsCard`, `showAmountBreakdown`, `canRequestRefund` (shipper && status ∈ {completed, captured}), `canAddTip` (shipper && completed && tip == null), `showRefundStatusCard`, `showPayoutStatusCard`.

**Tests:** table-driven (status × role × refund × tip).

---

## Phase C — Reusable components (closes audit §4)

All 8 primitives **before** Phase D screens. All in `features/payments/components/`. All consume `P*` + `Pasabayan*` tokens. All ship light + dark previews.

| # | Component(s) | Size |
|---|---|---|
| C1 | `PayoutStatusBadge` + `RefundStatusBadge` (via `PStatusBadge`) | S |
| C2 | `TipBadge` (heart + signed currency) | S |
| C3 | `PaymentMethodCard` (brand icon + menu + confirm-remove dialog) | M |
| C4 | `PaymentSummaryCard` (Prepayment / Postpayment modes) | M |
| C5 | `PaymentButton` (CTA + status + alerts) | M |
| C6 | `RefundStatusCard` (header badge + reason + admin notes + date) | M |
| C7 | `ReceiptRow` (role icon + route + amount + tip badge) | S |

### Highlights

- **C1 colors:** map enum cases to `PasabayanColors.{Warning,Info,Success,Error}` per spec §588–606. **Never hex.**
- **C3 brand colors:** Visa/Mastercard/Amex/Discover/unknown via `PasabayanColors.*`; menu rows + `PAlertDialog` confirm-remove.
- **C4 modes:** sealed `PaymentSummarySource = Prepayment(baseAmount, config) | Postpayment(amounts)`; extract pure `PaymentSummaryComputation.kt` helper for JVM-testable fee math.
- **C5 states:** {idle, loading, error, already-paid} previews × light/dark = 8 previews.
- **C6 messages:** 4 status messages per spec §1255.
- **C7 layout:** `PCard(clickable)` + role icon (`ArrowUpward` shipper / `ArrowDownward` carrier) + `TipBadge` (C2) trailing.

---

## Phase D — UI screens (closes audit §3)

Each composes Phase C primitives. Each ships light + dark previews with fake state (no Hilt).

| # | Screen(s) | Size |
|---|---|---|
| D1 | `PaymentMethodsScreen` (full) | M |
| D2 | `AddPaymentMethodSheet` (`PModalBottomSheet`) | M |
| D3 | `RefundSheet` (`PModalBottomSheet`, validation) | L → split if > 400 |
| D4 | `TransactionDetailScreen` rebuild (role-aware) | L → split into 4 files |
| D5 | `TipSelectionScreen` (presets + custom + PaymentSheet) | M |
| D6 | `ReceiptListScreen` (paginated, pull-to-refresh, auto-paginate) | M |
| D7 | `ReceiptDetailScreen` + `ReceiptWebView` (AndroidView + share intent) | M |

### D4 split

- `TransactionDetailScreen.kt` (route composable, < 100 lines)
- `components/TransactionStatusHeader.kt`
- `components/TransactionTimelineCard.kt`
- `components/TransactionParticipantsCard.kt`
- `components/TransactionActionsCard.kt`

Sections (in order, gated by B5 flags): StatusHeader → EarningsSummary / AmountBreakdown → Participants → Timeline → PayoutStatus → RefundStatusCard (C6) → RefundInfoCard → ActionsCard.

### D7 WebView

- `AndroidView(factory = { WebView(it) })` with loading + error overlays; `DisposableEffect { onDispose { webView.destroy() } }`.
- Share via `Intent.ACTION_SEND` with `EXTRA_TEXT = receipt.receiptUrl`, `type = "text/plain"`.

---

## Phase E — Wiring (nav + profile-screen refactor)

| # | Title | Size | Deps |
|---|---|---|---|
| E1 | `feat(payments): refactor PaymentsProfileScreen sections to compose Phase C components` | M | C1–C7 |
| E2 | `feat(payments): wire new payments screens into MainTabScreen sub-routes` | M | D1–D7 |
| E3 | `feat(payments): register pasabayan://stripe-redirect intent filter + 3DS return handler` | S | B2 |
| E4 | `feat(payments): debounce payments hub navigation clicks` | XS | E2 |

### E1 — Profile section refactor (pure refactor, no behavior change)

- `PaymentMethodsSection` → render `PaymentMethodCard` (C3)
- `PaymentsActivitySection` → `TipBadge` (C2) inline; use `MoneyFormatter` (A5)
- `TippingRefundSection` → `RefundStatusCard` (C6) + `TipBadge`
- `StripeConnectSection` → `PayoutStatusBadge` (C1) in header
- `PaymentProcessingSection` → wrap CTA in `PaymentButton` (C5) when pending match exists

Hold until all of Phase C is merged. Existing JVM tests catch state regressions.

### E2 — `MainTabScreen` state extensions

Add: `profilePaymentMethodsOpen`, `profileReceiptListOpen`, `profileSelectedReceiptId: Int?`, `profileTipSelectionForTransactionId: Int? + carrierName: String?`, `addCardSheetOpen`, `refundSheetForTransactionId: Int?`.

Deep links: keep `pendingTransactionId`; add `pendingReceiptId`, `pendingAddCard` slots for Phase 5 push routing follow-up.

### E3 — Manifest intent filter

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data android:scheme="pasabayan" android:host="stripe-redirect"/>
</intent-filter>
```

`MainActivity.handleIntent` dispatches to `PaymentViewModel.handleStripeReturn(uri)`.

---

## Phase F — Status & polish

| # | Title | Size |
|---|---|---|
| F1 | `test(payments): androidTest Compose coverage for new screens + components` | M |
| F2 | `test(payments): mapper + computed-property coverage backfill` | S |
| F3 | `docs(android-spec): mark Phase 4 truly complete in IMPLEMENTATION-STATUS.md` | XS |
| F4 | `refactor(payments): regression sweep + remove dead profile-section code` | XS |

### F1 androidTest list

- `PaymentMethodsScreenTest` — list / empty / add-card / set-default / remove dialog
- `RefundSheetTest` — reason picker / min-chars validation / partial enables submit
- `TipSelectionScreenTest` — preset selection / custom clears preset / submit enabled
- `PaymentMethodCardTest` — default badge / menu / remove dialog

### F4 regression greps (must return zero)

- `*Json` types in `features/payments/viewmodel/` or `ui/`
- `Float` in payments
- `Color(0xFF…)` in payments
- `Text("…")` literals in `features/payments/`

---

## Slice ordering (linear ship order, 31 slices)

```
A1 → A2 → A3 → A4 → A5 → A6
  → B1 → B2 → B3 → B4 → B5
  → C1 → C2 → C3 → C4 → C5 → C6 → C7
  → D1 → D2 → D3 → D4 → D5 → D6 → D7
  → E1 → E2 → E3 → E4
  → F1 → F2 → F3 → F4
```

**Parallelizable pairs** (if a second contributor joins): `A3↔A4`, `A5↔A6`, `B1↔B2`, `B3↔B4`, all of `C1–C7` after Phase A, `D6↔D7`.

---

## Risk register

| ID | Risk | Mitigation |
|---|---|---|
| R1 | Stripe SDK `PaymentSheet` setup-intent vs payment-intent confusion | Encapsulate config in `PaymentSheetConfigFactory.kt` (B2). |
| R2 | 3DS in-app: WebView vs Custom Tabs vs Stripe SDK built-in | Use Stripe SDK's built-in flow in `PaymentSheet.presentWith*`. Custom Tab only for Connect onboarding. `pasabayan://stripe-redirect` only for rare browser-redirect challenge. |
| R3 | `PModalBottomSheet` vs full-screen back-stack confusion | Sheets are state-controlled (host owns `showXSheet`); only back-stack pops the full screen. Document in `RefundSheet` header. |
| R4 | `PaymentsProfileScreen` consumer regression in E1 | Hold E1 until all of Phase C is merged; pure refactor; JVM + androidTest cover. |
| R5 | A2 slice too large (widens `Transaction` + fixes 5 consumers) | Split into A2a (widen + mapper + tests) and A2b (fix consumers) if > 500 lines. |
| R6 | `Map<String, JsonElement>?` introduces kotlinx-serialization as domain dep | Already transitive via `:core:network`; safe. |
| R7 | Localizing `RefundReason` collides with `RefundViewModel.effectiveReason: String` | Change VM to expose `RefundReasonPayload` sealed (`Preset(reason)` / `Custom(text)`); UI localizes on render. |
| R8 | RefundUiState JSON→domain swap (B4) may break fixture-shape tests | Existing decode tests cover; add domain assertion test in B4. |
| R9 | Stripe `PaymentConfiguration.init` needs `Context`, not VM | Keep in `StripeConfigRepository.loadAndApplyConfig()`; do not push into VM. |
| R10 | `viewModelScope` doesn't survive process death; PaymentSheet state would replay on resume | Stripe SDK owns its persistence. Do **not** put PaymentSheet results in `SavedStateHandle`. |
| R12 | WebView memory/lifecycle | `AndroidView(factory)` + `DisposableEffect { onDispose { webView.destroy() } }`; disable JS unless URL requires. |

---

## Out of scope

- Promoting any payments component to `:core:designsystem` (none has a 2nd consumer).
- A dedicated `:core:webview` module (1 consumer).
- Stripe `CardElement` / custom card form (we use `PaymentSheet`).
- Push routing for `AddCard` / `Receipt` deep links (Phase 5 follow-up).
- Receipt PDF caching / offline.
- Multi-currency formatting beyond CAD/USD smoke.
- Rewriting `PaymentsProfileScreen` as a Compose Navigation graph (state routing works).

---

## Spec edits to file in `06-payments-stripe.md`

1. §118 `metadata` — clarify Android type as `Map<String, JsonElement>?`.
2. §862 `presentPaymentSheet(activity: Activity)` — replace with Compose `rememberPaymentSheet { result -> … }` signature.
3. §910 `PRESET_TIPS` placement — clarify `Companion` placement acceptable.
4. §993 `TransactionDetailViewModel.cancelTransaction(transaction)` — clarify `transactionId: Int` Android signature.
5. §1064–1203 (UI screens) — add note: `AddPaymentMethodSheet` + `RefundSheet` are `PModalBottomSheet`s; others are full screens.
6. §90 — align Retrofit interface placement with `00-architecture.md` (`:core:network`, not `features/payments/services/`).
7. §634 `STRIPE_ERROR` — note SDK exposes `StripeException` with `localizedMessage`; surface directly.
8. §1466–1471 `RefundReason` strings — verify FR translations or mark `<!-- TODO: translate -->`.

---

## Recommended starting slice

**A1 — `feat(payments): add nested Transaction domain types`** — 7 small data-class files + 2 test files. No consumers yet, so it lands cleanly and unblocks A2 (the only slice with consumer churn).
