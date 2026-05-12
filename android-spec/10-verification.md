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

## Multipart field names — POST `/verification/request-premium`

From `VerificationAPIService.submitPremiumVerification`:

| Field name | Type | Notes |
|-----------|------|-------|
| `id_document` | file | Front of ID — required |
| `id_document_back` | file | Back of ID — optional (non-passport only) |
| `selfie_with_id` | file | Selfie holding ID — required |
| `id_type` | text | Required; `IDDocumentType.rawValue` |
| `id_number` | text | Optional |
| `birth_date` | text | Optional |

## Ambiguity

Premium verification mapping may use placeholder fields in some paths — follow **latest** iOS models and tests.

## UI (iOS reference)

`PhoneVerificationFlowView`, verification badges on header.

## Phone-verification gate (Android-only; behavior delta vs iOS)

iOS does **not** gate creation/booking/request flows on phone verification — `User.isPhoneVerified` is read for display only. Android enforces a hard gate at four entry points. **Two-layer defense:**

1. **UI short-circuit at button time** in `MainTabScreen` / `ShipperExploreContent`. Reads `user.phoneVerified`; when false, sets `verifyPhoneReason` and renders `VerifyPhonePromptSheet` instead of opening the action form.
2. **VM guard at function entry** via `RequirePhoneVerificationUseCase` (reads `AuthRepository.currentUser().value?.phoneVerified`). Emits `requiresPhoneVerification: VerifyPhoneReason?` on the feature's `*UiState` (one-shot); the host consumes the flag and surfaces the prompt. The repo/use-case is **not** called when the gate fires.

### Gated entries

| # | Action | VM entry | Reason |
|---|---|---|---|
| 1 | Create package | `PackageViewModel.createPackageRequest`, `createServiceRequest` | `CreatePackage` |
| 2 | Create trip | `TripCreationViewModel.createTrip`, `CreateTripFromPackageViewModel.createTrip` | `CreateTrip` |
| 3 | Book trip (shipper) | `PackageViewModel.requestTripForPackage`, `BrowseTripsViewModel.bookTrip` / `bookTripDirectly`, `MatchingViewModel.submitCounterOffer(isShipper=true)` | `BookTrip` |
| 4 | Request to carry (carrier) | `MatchingViewModel.submitCounterOffer(isShipper=false)`, `MatchingViewModel.requestPackageAsCarrier` | `RequestToCarry` |

### Shared primitives

- `core:session` — `AuthRepository.currentUser(): StateFlow<AuthUser?>` and `clearCurrentUser()`. Backing flow updated on `loginWith…` / `loadCurrentUser` success and cleared on `logout` and unauthorized-from-network.
- `features/verification/services/RequirePhoneVerificationUseCase` — `operator fun invoke(): Result<Unit>`; failure type `PhoneVerificationRequired`.
- `features/verification/model/VerifyPhoneReason` — enum with `bodyRes: Int` per reason. Body strings under `verification_gate_body_*` in `strings_verification.xml` (en + fr).
- `features/verification/ui/VerifyPhonePromptSheet` — shared modal prompt; `onVerifyNow` chains into the existing `PhoneVerificationSheet`. **Action is not auto-resumed**: user re-taps after verifying.

### Localization keys

`verification_gate_title`, `verification_gate_body_create_package`, `verification_gate_body_create_trip`, `verification_gate_body_book_trip`, `verification_gate_body_request_to_carry`, `verification_gate_cta_verify`, `verification_gate_cta_dismiss`.

## TDD checklist

- [ ] `PhoneVerificationModelsTests`, `PhoneVerificationViewModelTests`.
- [x] `RequirePhoneVerificationUseCaseTest` — signed out / unverified / verified.
- [x] `AuthRepositoryIntegrationTest` — current-user StateFlow updates on login/load/logout/clear.
- [x] `PackageViewModelTest` — gate fires on `createPackageRequest`, `createServiceRequest`, `requestTripForPackage`; consumer clears.
- [x] `TripCreationViewModelTest`, `CreateTripFromPackageViewModelTest` — gate fires on `createTrip`; consumer clears.
- [x] `BrowseTripsViewModelTest` — gate fires on `bookTrip` and `bookTripDirectly`; consumer clears.
- [x] `MatchingViewModelTest` — gate fires on shipper / carrier counter-offer and on `requestPackageAsCarrier`; consumer clears.
