# 10 — Phone and premium verification

**Phase:** 6 | **Feature:** Verification | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`VerificationAPIService.swift`](../../Pasabayan/Features/Verification/Services/VerificationAPIService.swift), [`PhoneVerificationModels.swift`](../../Pasabayan/Features/Verification/Models/PhoneVerificationModels.swift), [`PremiumVerification.swift`](../../Pasabayan/Features/Verification/Models/PremiumVerification.swift).

## Endpoints

| Method | Path |
|--------|------|
| POST | `/phone/send-otp` |
| POST | `/phone/verify-otp` |
| GET | `/phone/status` |
| POST | `/phone/resend-otp` |
| GET | `/verification/premium-status` |
| POST | `/verification/request-premium` (multipart) |
| GET | `/premium-verification/application` |

## Ambiguity

Premium verification mapping may use placeholder fields in some paths — follow **latest** iOS models and tests.

## UI (iOS reference)

`PhoneVerificationFlowView`, verification badges on header.

## TDD checklist

- [ ] `PhoneVerificationModelsTests`, `PhoneVerificationViewModelTests`.
