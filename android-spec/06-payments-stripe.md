# 06 — Payments, Stripe, receipts

**Phase:** 4 | **Feature:** Payments | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`PaymentService.swift`](../../Pasabayan/Features/Payments/Services/PaymentService.swift), [`StripeConfigService.swift`](../../Pasabayan/Features/Payments/Services/StripeConfigService.swift), [`StripeConnectService.swift`](../../Pasabayan/Features/Payments/Services/StripeConnectService.swift), [`ReceiptService.swift`](../../Pasabayan/Features/Payments/Services/ReceiptService.swift), [`PaymentMethodsViewModel.swift`](../../Pasabayan/Features/Payments/ViewModels/PaymentMethodsViewModel.swift) (direct URLs).

## Models

[`PaymentModels.swift`](../../Pasabayan/Features/Payments/Models/PaymentModels.swift), [`StripeConfigModels.swift`](../../Pasabayan/Features/Payments/Models/StripeConfigModels.swift), [`StripeConnectModels.swift`](../../Pasabayan/Features/Payments/Models/StripeConnectModels.swift).

## Endpoints

### Payments REST (`PaymentService`)

| Method | Path |
|--------|------|
| POST | `/payments` |
| GET | `/payments` |
| GET | `/payments/{id}` |
| POST | `/payments/{id}/capture` |
| POST | `/payments/confirm-capture` |
| POST | `/payments/{id}/release` |
| POST | `/payments/{transactionId}/refund` |
| GET | `/payments/{transactionId}/refund-status` |
| POST | `/payments/{id}/cancel` |
| POST | `/payments/{transactionId}/tip` |

### Stripe

| Method | Path |
|--------|------|
| GET | `/payments/config` |
| POST | `/stripe/connect/onboard` |
| GET | `/stripe/connect/status` |
| GET | `/stripe/connect/dashboard` |

### Payment methods (ViewModel uses raw URL)

| Method | Path |
|--------|------|
| GET | `/stripe/payment-methods` |
| GET/POST | `/stripe/default-payment-method` |
| POST | `/stripe/setup-intent` |
| DELETE | `/stripe/payment-methods/{methodId}` |

### Receipts (`ReceiptService`)

| Method | Path |
|--------|------|
| GET | `/receipts?page=&per_page=` |
| GET | `/receipts/{transactionId}` |

**Receipt HTML / WebView:** [`ReceiptWebView.swift`](../../Pasabayan/Features/Payments/Views/ReceiptWebView.swift) — mirror WebView or Chrome Custom Tabs for receipt URLs returned by API.

## UI (iOS reference)

`PaymentViewModel`, payment methods screens, Stripe onboarding flows.

## TDD checklist

- [ ] `PaymentViewModelTests`, `PaymentModelsTests`, `StripeConfigTests`, `StripeConnectViewModelTests`, `ReceiptDetailViewModelTests`.
