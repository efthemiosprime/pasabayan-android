# API shapes reference — endpoints, services, request/response types

**Source of truth:** Swift `Codable` types and `*APIService` / `PaymentService` in [`Pasabayan/`](../../Pasabayan/). Wire format uses **snake_case** keys unless noted; base URL [`APIConfiguration.baseURL`](../../Pasabayan/Services/APIConfiguration.swift) (path prefix `/api` in full URL).

**Android:** For each row, implement matching DTOs (Kotlinx Serialization `@SerialName`) and map to domain models. When two methods share a path with different wrappers, match the **iOS method** used at the call site.

**Package layout:** Keep DTOs and API interfaces in the **same feature module** as the iOS `Features/<Feature>/` that owns the `*APIService` (see [00-architecture.md](00-architecture.md) — iOS layout → Android mirror).

---

## Swift model index (by domain)

| Domain | Request/response structs (primary files) |
|--------|------------------------------------------|
| Auth | [`AuthResponses.swift`](../../Pasabayan/Features/Authentication/Models/AuthResponses.swift), [`User.swift`](../../Pasabayan/Features/Authentication/Models/User.swift) |
| Trips | [`Trip.swift`](../../Pasabayan/Features/Trips/Models/Trip.swift), `TripModels.swift` |
| Packages | [`PackageRequest.swift`](../../Pasabayan/Features/Packages/Models/PackageRequest.swift) |
| Bookings / matches | [`BookingModels.swift`](../../Pasabayan/Features/Bookings/Models/BookingModels.swift), [`MatchingModels.swift`](../../Pasabayan/Features/Bookings/Models/MatchingModels.swift), [`Booking.swift`](../../Pasabayan/Features/Bookings/Models/Booking.swift) |
| Payments | [`PaymentModels.swift`](../../Pasabayan/Features/Payments/Models/PaymentModels.swift), Stripe models |
| Profile | [`ProfileModels.swift`](../../Pasabayan/Features/Profile/Models/ProfileModels.swift) |
| Chat | [`ChatModels.swift`](../../Pasabayan/Features/Chat/Models/ChatModels.swift) |
| Notifications | [`NotificationModels.swift`](../../Pasabayan/Features/Notifications/Models/NotificationModels.swift) |
| Verification | [`PhoneVerificationModels.swift`](../../Pasabayan/Features/Verification/Models/PhoneVerificationModels.swift), [`PremiumVerification.swift`](../../Pasabayan/Features/Verification/Models/PremiumVerification.swift) |
| Ratings | [`Rating.swift`](../../Pasabayan/Features/Ratings/Models/Rating.swift) |

---

## `APIService` ([`APIService.swift`](../../Pasabayan/Services/APIService.swift))

| Method | Path | Request | Response type |
|--------|------|---------|----------------|
| POST | `/auth/{provider}/login` | `{ access_token }` | `BackendAuthResponse` → `AuthResponse` |
| GET | `/auth/me` | — | `UserResponse` |
| POST | `/auth/logout` | — | `APIResponse` |
| GET | `/health` | — | `HealthResponse` |
| POST | `/activity-logs` | `ActivityLogData` | `ActivityLogResponse` |

---

## `TripsAPIService` ([`TripsAPIService.swift`](../../Pasabayan/Features/Trips/Services/TripsAPIService.swift))

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/trips` | — | `TripsResponse` → `[Trip]` |
| GET | `/carrier/trips` | — | `TripsResponse` (fallback) |
| POST | `/trips` | `CreateTripRequest` | `TripResponse` (custom client) |
| PUT | `/trips/{id}` | `TripUpdateRequest` | `TripResponse` |
| DELETE | `/trips/{id}` | — | `APIResponse` |
| GET | `/trips/available?...` | — | `TripsResponse` or browse wrapper |

---

## `PackagesAPIService` ([`PackagesAPIService.swift`](../../Pasabayan/Features/Packages/Services/PackagesAPIService.swift))

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/packages` | — | `PackageRequestsResponse` / paginated |
| GET | `/packages/available?...` | — | `AvailablePackagesResponse` |
| POST | `/packages` | `CreatePackageRequest` or multipart `images[]` | `PackageRequestResponse` |
| GET | `/packages/{id}` | — | `PackageRequestResponse` |
| PUT | `/packages/{id}` | `CreatePackageRequest` / `PackageUpdateRequest` or multipart | `PackageRequestResponse` |
| DELETE | `/packages/{id}` | — | `APIResponse` |
| POST | `/services/request` | `CreateServiceRequestBody` | `PackageRequestResponse` |

---

## `BookingsAPIService` ([`BookingsAPIService.swift`](../../Pasabayan/Features/Bookings/Services/BookingsAPIService.swift))

### Matches — list & detail

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/matches` | — | `BookingsResponse` or `DataResponse<PaginatedResponse<DeliveryMatch>>` |
| GET | `/matches?role=carrier` | — | `DataResponse<PaginatedResponse<DeliveryMatch>>` |
| GET | `/matches?role=carrier&status=` | — | same |
| GET | `/matches?role=shipper` | — | same |
| GET | `/matches?role=shipper&status=` | — | same |
| GET | `/matches/pending-requests` | — | same |
| GET | `/matches?package_request_id=&carrier_trip_id=` (+ optional flags) | — | `MatchCheckResponse` |
| GET | `/matches?carrier_trip_id=&package_request_id=` | — | `MatchCheckResponse` |
| GET | `/matches/{id}` | — | `DataResponse<DeliveryMatch>` |
| POST | `/matches` | `CreateBookingRequest` / `MatchCreationRequest` | `BookingResponse` / `DataResponse<DeliveryMatch>` |
| PUT | `/matches/{id}/confirm` | — | `MatchConfirmResponse` |
| PUT | `/matches/{id}/pickup` | `PickupRequest` / `MatchUpdateRequest` / `confirmation_code` | `CarrierResponseResult` / `BookingResponse` / `PickupConfirmationResponse` / `DataResponse<DeliveryMatch>` |
| PUT | `/matches/{id}/transit` | — | `CarrierResponseResult` / `DataResponse<DeliveryMatch>` |
| PUT | `/matches/{id}/deliver` | `DeliveryRequest` / `MatchUpdateRequest` | `BookingResponse` / `DataResponse<DeliveryMatch>` |
| DELETE | `/matches/{id}` | optional `{ reason }` | `BookingResponse` / `CancelMatchResponse` |
| POST | `/matches/{id}/generate-pickup-code` | — | `PickupCodeResponse` |
| POST | `/matches/{id}/generate-delivery-code` | — | `DeliveryCodeResponse` |
| POST | `/matches/{id}/rate` | `SubmitRatingRequest` | `RatingResponse` → `RatingData` |
| GET | `/matches/{id}/rating-status` | — | `RatingStatusResponse` |
| GET | `/matches/{id}/carrier-location` | — | `CarrierLocationDataResponse` |
| POST | `/matches/{id}/location` | `UpdateLocationRequest` | `UpdateLocationResponse` |
| POST | `/payments/matches/{id}/auto-charge` | — | `AutoChargeRetryResponse` |
| POST | `/services/matches/{id}/receipt` | multipart `receipt_photo` | `ReceiptUploadResponse` |
| GET | `/services/matches/{id}/receipt` | — | `ReceiptResponse` |
| GET | `/matches/{id}/receiver-access` | — | `ReceiverAccessListResponse` |
| POST | `/matches/{id}/receiver-access` | `CreateReceiverAccessRequest` | `CreateReceiverAccessResponse` |
| DELETE | `/matches/{id}/receiver-access/{tokenId}` | — | `RevokeReceiverAccessResponse` |
| PUT | `/matches/{id}/accept-shipper-request` | `CarrierAcceptRequest` | `AcceptShipperRequestResponse` |
| PUT | `/matches/{id}/decline-shipper-request` | `CarrierDeclineRequest` | `CarrierResponseResult` |
| PUT | `/matches/{id}/accept` | `ShipperAcceptRequest` | `ShipperMatchResponse` |
| PUT | `/matches/{id}/decline` | `ShipperDeclineRequest` | `ShipperMatchResponse` |

### Trips ↔ packages

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/trips/{id}/book` | `PackageTripBookingRequest` / `DirectTripBookingRequest` / `DirectBookingRequest` | `DirectTripBookingResponse` / `DirectBookingResponse` |
| POST | `/bookings` | `BookTripRequest` | `BookingResponse` |
| GET | `/trips/{id}/matches` | — | `TripMatchesResponse` |
| GET | `/trips/{id}/compatibility/{packageId}` | — | `CompatibilityResponse` |
| GET | `/trips/{id}/compatible-packages` | — | `CompatiblePackagesResponse` |
| POST | `/trips/{id}/packages/{packageId}/request` | `CarrierRequestBody` | `CarrierResponseResult` |
| POST | `/trips/{id}/packages/{packageId}/accept` | `AcceptPackageRequest` | `AcceptPackageResponse` |
| POST | `/trips/{id}/packages/{packageId}/reject` | `PackageRejectRequest` | `PackageRejectResponse` |
| POST | `/packages/{packageId}/request-trip/{tripId}` | `ShipperTripRequest` / `ShipperCounterOfferRequest` | `ShipperTripRequestResult` / `CounterOfferResponse` (custom URLSession) |
| GET | `/packages/{packageId}/compatible-trips` | — | `CompatibleTripsAPIResponse` |
| GET | `/packages/{id}/trip-template` | — | `TripTemplateResponse` |
| POST | `/packages/{packageId}/service-offer` | `ServiceOfferRequest` | `ServiceOfferResponse` (custom `URLSession` in `BookingsAPIService`) |

**Analytics (present in code but commented out):** `GET /analytics/shipper` → `DataResponse<ShipperAnalytics>`; `GET /analytics/carrier` → `DataResponse<CarrierAnalytics>`. Not called in current build — enable only if product restores analytics API.

---

## `ProfileAPIService` ([`ProfileAPIService.swift`](../../Pasabayan/Features/Profile/Services/ProfileAPIService.swift))

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/profile` | — | `ProfileResponse` |
| POST | `/profile` | `UpdateProfileRequest` and/or multipart (avatar) | `ProfileResponse` |
| PUT | `/profile` | `UpdateProfileRequest` | `ProfileResponse` |
| DELETE | `/profile/picture` | — | `APIResponse` |
| POST | `/profile/request-deletion` | `AccountDeletionRequestBody` | `AccountDeletionResponse` |
| GET | `/profile/disclaimer-acknowledgments` | — | `DisclaimerAcknowledgmentsListResponse` |
| POST | `/profile/disclaimer-acknowledgments` | `DisclaimerAcknowledgmentRequest` | `DisclaimerAcknowledgmentResponse` |
| GET | `/profile/consent-preferences` | — | `ConsentPreferencesResponse` |
| PUT | `/profile/consent-preferences` | `[String: Bool]` | `ConsentPreferencesResponse` |
| GET | `/profile/export-data` | — | raw `Data` |
| GET | `/carrier/profile` | — | `CarrierProfileResponse` |
| POST | `/carrier/profile` | body | `CarrierProfileResponse` |
| PUT | `/carrier/profile` | body | `CarrierProfileResponse` |
| GET | `/carrier/stats` | — | `CarrierStatsResponse` |
| POST | `/carrier/enable` | — | `CarrierEnableResponse` |
| POST | `/carrier/toggle-status` | — | `CarrierStatusResponse` |

---

## `PaymentService` ([`PaymentService.swift`](../../Pasabayan/Features/Payments/Services/PaymentService.swift))

Uses `PaymentError`; JSON **snake_case** via decoder.

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/payments` | `CreatePaymentRequest` | `CreatePaymentResponse` |
| GET | `/payments` / `?role=` | — | `TransactionListResponse` → `[Transaction]` |
| GET | `/payments/{id}` | — | `TransactionResponse` |
| POST | `/payments/{id}/capture` | — | `TransactionResponse` |
| POST | `/payments/confirm-capture` | `ConfirmCaptureRequest` | `TransactionResponse` |
| POST | `/payments/{id}/release` | — | `TransactionResponse` |
| POST | `/payments/{id}/refund` | `RefundRequestBody` | `RefundRequestResponse` |
| GET | `/payments/{id}/refund-status` | — | `RefundStatusResponse` |
| POST | `/payments/{id}/cancel` | `CancelRequest` | `TransactionResponse` |
| POST | `/payments/{id}/tip` | `TipRequest` | `TipResponse` |

## Stripe ([`StripeConfigService`](../../Pasabayan/Features/Payments/Services/StripeConfigService.swift), [`StripeConnectService`](../../Pasabayan/Features/Payments/Services/StripeConnectService.swift))

| Method | Path | Response |
|--------|------|----------|
| GET | `/payments/config` | `StripeConfigResponse` |
| POST | `/stripe/connect/onboard` | `StripeOnboardingResponse` |
| GET | `/stripe/connect/status` | `StripeStatusResponse` |
| GET | `/stripe/connect/dashboard` | `StripeDashboardResponse` |

## Payment methods ([`PaymentMethodsViewModel`](../../Pasabayan/Features/Payments/ViewModels/PaymentMethodsViewModel.swift))

| Method | Path |
|--------|------|
| GET | `/stripe/payment-methods` |
| GET/POST | `/stripe/default-payment-method` |
| POST | `/stripe/setup-intent` |
| DELETE | `/stripe/payment-methods/{id}` |

## Receipts ([`ReceiptService`](../../Pasabayan/Features/Payments/Services/ReceiptService.swift))

| Method | Path | Response |
|--------|------|----------|
| GET | `/receipts?page=&per_page=` | `PaymentReceiptListResponse` |
| GET | `/receipts/{transactionId}` | `SinglePaymentReceiptResponse` |

---

## `ChatAPIService` ([`ChatAPIService.swift`](../../Pasabayan/Features/Chat/Services/ChatAPIService.swift))

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/config/broadcasting` | — | `BroadcastingConfigResponse` |
| POST | `/chat/conversations/{id}/messages` | JSON `message`, `message_type` (e.g. `text`, `system`) | `SendMessageResp` → mapped to `SendMessageResult` |
| GET | `/chat/conversations` | — | `ConversationsResp` |
| GET | `/chat/conversations/{id}` | — | `ConversationResp` |
| GET | `/chat/conversations/{id}/messages?page=` | — | `MessagesResp` |
| DELETE | `/chat/messages/{id}` | — | `DeleteMessageResponse` |
| PUT | `/chat/conversations/{id}/mark-read` | — | message-only |
| PUT | `/chat/messages/{id}/read` | — | `MarkAsReadResponse` |
| GET | `/chat/messages/{id}/status` | — | `MessageStatusResponse` |

**Broadcasting auth (no `/api`):** `POST /broadcasting/auth` — form `channel_name`, `socket_id` → `ChannelAuthResponse`.

---

## `NotificationAPIService` ([`NotificationAPIService.swift`](../../Pasabayan/Features/Notifications/Services/NotificationAPIService.swift))

| Method | Path | Request | Response notes |
|--------|------|---------|----------------|
| POST | `/device-tokens` | JSON: `token`, `platform`, `app_version`, `device_model`, `os_version`, optional `device_name` | `DeviceTokenRegisterResponseParser` → `DeviceTokenInfo` |
| DELETE | `/device-tokens/{token}` | — | |
| GET | `/notifications?...` | — | `NotificationHistoryResponse` |
| GET | `/notifications/unread-count` | — | multiple decode paths in iOS |
| PUT | `/notifications/{id}/read` | — | |
| PUT | `/notifications/mark-all-read` | — | |
| POST | `/device-tokens/test` | — | |

---

## `VerificationAPIService` ([`VerificationAPIService.swift`](../../Pasabayan/Features/Verification/Services/VerificationAPIService.swift))

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/phone/send-otp` | `SendOTPRequest` | `OTPResponse` |
| POST | `/phone/verify-otp` | `VerifyOTPRequest` | `VerifyOTPResponse` |
| GET | `/phone/status` | — | `PhoneStatusResponse` |
| POST | `/phone/resend-otp` | `ResendOTPRequest` | `OTPResponse` |
| GET | `/verification/premium-status` | — | `PremiumVerificationStatusResponse` |
| POST | `/verification/request-premium` | multipart | `PremiumVerificationResponse` |
| GET | `/premium-verification/application` | — | `PremiumVerificationResponse` |

---

## `RatingsAPIService` ([`RatingsAPIService.swift`](../../Pasabayan/Features/Ratings/Services/RatingsAPIService.swift))

| Method | Path | Response |
|--------|------|----------|
| GET | `/users/{userId}/ratings?...` | `UserRatingsResponse` / variants |
| GET | `/user/stats` | `UserStatsResponse` |
| GET | `/ratings/pending?...` | `PendingReviewsResponse` |
| GET | `/ratings/given?...` | list variants |
| PUT | `/ratings/{id}/comment` | `CommentUpdateRequest` → `RatingUpdateResponse` |

---

## `FavoritesAPIService` ([`FavoritesAPIService.swift`](../../Pasabayan/Features/Favorites/Services/FavoritesAPIService.swift))

Uses async `URLSession`; responses: `FavoritesResponse`, `DirectRequestsResponse`, inline `IsFavoriteResponse`.

| Method | Path | Request body (when applicable) |
|--------|------|--------------------------------|
| GET | `/favorites/carriers?sort=&has_upcoming_trips=` | — |
| POST | `/carriers/{id}/favorite` | optional `notes`, `notification_enabled` (`AddFavoriteRequest`) |
| DELETE | `/carriers/{id}/unfavorite` | — |
| GET | `/carriers/{id}/is-favorite` | — → `{ is_favorite: bool }` |
| POST | `/carriers/{id}/request-delivery` | `pickup_city`, `pickup_date_preferred`, `delivery_city`, `delivery_date_needed`, `package_description`, `package_weight_kg`, `package_type`, optional addresses, `offered_price`, `shipper_message` |
| GET | `/favorites/requests/sent` | — |

---

## Legal, support, misc

| Service | Method | Path | Request | Response |
|---------|--------|------|---------|----------|
| [`LegalAgreementService`](../../Pasabayan/Features/Legal/Services/LegalAgreementService.swift) | GET | `/legal/status` | — | `LegalStatusResponse` |
| | POST | `/legal/agree` | `AgreementRequest` | `AgreementResponse` |
| | POST | `/legal/withdraw` | `document_type` | `WithdrawalResponse` |
| [`SupportTicketService`](../../Pasabayan/Features/Support/Services/SupportTicketService.swift) | POST | `/support/tickets` | multipart | `SupportTicketResponse` |
| [`LocationCatalogService`](../../Pasabayan/Services/LocationCatalogService.swift) | GET | `/locations/catalog` | — | `LocationCatalogResponse` |
| [`ShipperAPIService`](../../Pasabayan/Features/Shipper/Services/ShipperAPIService.swift) | GET | `/shipper/nearby-carriers` | — | `NearbyCarriersResponse` |
| [`RoutesAPIService`](../../Pasabayan/Features/Trips/Services/RoutesAPIService.swift) | GET | `/routes/popular-packages` | — | `PopularRoutesResponse` |
| [`RouteActivityAPIService`](../../Pasabayan/Features/RouteActivity/Services/RouteActivityAPIService.swift) | GET | `/route-activity/summary` | — | `RouteActivitySummaryResponse` |
| Home city ([`HomeCityDetectionService`](../../Pasabayan/Services/HomeCityDetectionService.swift)) | GET | `/locations/countries/CA/cities` | — | `CitiesResponse` |

---

## Machine-readable matrix

[contracts/api-contract-matrix.yaml](contracts/api-contract-matrix.yaml) — YAML groups; extend when adding endpoints. **This markdown** is the full **type-name** catalog for implementers.

---

## Related

- [01-error-taxonomy.md](01-error-taxonomy.md) — HTTP → domain errors  
- [FEATURE-COVERAGE-MATRIX.md](FEATURE-COVERAGE-MATRIX.md) — feature folder coverage  
- [IMPLEMENTATION-GUIDE.md](IMPLEMENTATION-GUIDE.md) — workflow
