# Feature coverage matrix (iOS → Android spec)

**Use this** to confirm **no `Pasabayan/Features/` area is orphaned** before shipping Android milestones. Every row must map to a spec and a phase in [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md).

## `Pasabayan/Features/` (17 top-level modules)

| # | iOS folder | Primary spec | Phase | Notes |
|---|------------|--------------|-------|--------|
| 1 | `Authentication/` | [02-auth-session.md](02-auth-session.md) | 1 | OAuth + session; [`AuthValidationService`](../../Pasabayan/Features/Authentication/Services/AuthValidationService.swift) client rules |
| 2 | `Analytics/` | [**19-analytics.md**](19-analytics.md) | 7 | **Mock-only** analytics UI (17 iOS files); dedicated spec with all screens, components, models, charts |
| 3 | `Bookings/` | [05-bookings-matches.md](05-bookings-matches.md) | 3 | Matches, codes, receipts upload, counter-offer, live tracking, history views |
| 4 | `Chat/` | [07-chat-broadcasting.md](07-chat-broadcasting.md) | 5 | REST + [`RealtimeChatService`](../../Pasabayan/Features/Chat/Services/RealtimeChatService.swift) WebSocket |
| 5 | `Favorites/` | [11-favorites-ratings.md](11-favorites-ratings.md) | 6 | Direct URLSession patterns — match error handling |
| 6 | `Legal/` | [12-legal-support-misc.md](12-legal-support-misc.md) | 7 | [`LegalAgreementService`](../../Pasabayan/Features/Legal/Services/LegalAgreementService.swift), [`LegalAgreementViewModel`](../../Pasabayan/Features/Legal/ViewModels/LegalAgreementViewModel.swift) |
| 7 | `Notifications/` | [08-notifications-device-tokens.md](08-notifications-device-tokens.md) | 5 | All `NotificationType` cases — see that spec |
| 8 | `Onboarding/` | [**17-onboarding.md**](17-onboarding.md) | 1 | `PasabayanApp` + `ContentView` gates; `OnboardingScreen`, city, consent — see spec |
| 9 | `Packages/` | [04-packages.md](04-packages.md) | 2 | API + local: `ShipperDisclaimerStore`, `SavedPackageDescriptionsStore`, route templates |
| 10 | `Payments/` | [06-payments-stripe.md](06-payments-stripe.md) | 4 | `PaymentService`, Stripe, receipts WebView, tips, refunds, [`PaymentMethodsViewModel`](../../Pasabayan/Features/Payments/ViewModels/PaymentMethodsViewModel.swift) `/stripe/*` |
| 11 | `Profile/` | [09-profile-carrier-consent.md](09-profile-carrier-consent.md), [20-profile-tab.md](20-profile-tab.md) | 6 | Profile APIs/editing + shared tab shell order, role gating, menu routing, and DS parity |
| 12 | `Ratings/` | [11-favorites-ratings.md](11-favorites-ratings.md) | 6 | Ratings API + submission UI |
| 13 | `RouteActivity/` | [12-legal-support-misc.md](12-legal-support-misc.md) | 7 | [`RouteActivityAPIService`](../../Pasabayan/Features/RouteActivity/Services/RouteActivityAPIService.swift); [`CarrierExploreViewModel`](../../Pasabayan/Features/RouteActivity/ViewModels/CarrierExploreViewModel.swift) ties to Explore |
| 14 | `Shipper/` | [12-legal-support-misc.md](12-legal-support-misc.md) | 7 | Nearby carriers |
| 15 | `Support/` | [12-legal-support-misc.md](12-legal-support-misc.md) | 7 | Tickets, HelpCenter, bundled HTML articles, Terms/Privacy views |
| 16 | `Trips/` | [03-trips.md](03-trips.md) | 2 | [`UsualTransportStore`](../../Pasabayan/Features/Trips/Services/UsualTransportStore.swift) local prefs |
| 17 | `Verification/` | [10-verification.md](10-verification.md) | 6 | Phone + premium |

## Cross-cutting (not under `Features/`)

| Area | Path | Spec |
|------|------|------|
| Design system | `Views/Components/DesignSystem.swift` | [14-design-system.md](14-design-system.md) |
| App shell / tabs | `Views/Screens/Dashboard/`, `ContentView.swift` | [13-ui-tab-explore.md](13-ui-tab-explore.md), [15-platform-and-tab-index.md](15-platform-and-tab-index.md) |
| API client / errors | `Services/APIService.swift` | [00-architecture.md](00-architecture.md), [01-error-taxonomy.md](01-error-taxonomy.md) |
| Home city | `Services/HomeCityDetectionService.swift` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Location GPS | `Services/LocationService.swift` | Used by home city, maps — align permissions |
| Carrier GPS updates | `Services/CarrierLocationService.swift` | [05-bookings-matches.md](05-bookings-matches.md) (match location) |
| HTTP cache | `Services/HTTPCacheService.swift` | [00-architecture.md](00-architecture.md) — caching strategy section |
| Crash reporting | `Services/CrashReportingService.swift`, `AppCrashHandler.swift` | [00-architecture.md](00-architecture.md) — Firebase Crashlytics |
| Auth logging | `Services/AuthenticationLogger.swift` | [00-architecture.md](00-architecture.md) — activity logging service |
| Tracking UI | `Views/Components/Tracking/` (6 files) | [05-bookings-matches.md](05-bookings-matches.md) — live tracking UI section |
| Dashboard components | `Views/Screens/Dashboard/DashboardComponents/` (14 files) | [13-ui-tab-explore.md](13-ui-tab-explore.md) — dashboard sub-components |
| Activity logs | `POST /activity-logs` in `APIService` | [12-legal-support-misc.md](12-legal-support-misc.md) |
| Location catalog | `Services/LocationCatalogService.swift` | [12-legal-support-misc.md](12-legal-support-misc.md) |

## Gap check (run before milestone)

- [ ] Every row in **`Features/`** table has an Android module or explicit “defer”.
- [ ] [08-notifications-device-tokens.md](08-notifications-device-tokens.md) handles **all** `NotificationType` raw values.
- [ ] `docs/tabs/*.md` covered by navigation design ([13](13-ui-tab-explore.md), [15](15-platform-and-tab-index.md)).
- [ ] Onboarding + auth gate ordering matches [`ContentView`](../../Pasabayan/ContentView.swift) / [`PasabayanApp`](../../Pasabayan/PasabayanApp.swift) — [17-onboarding.md](17-onboarding.md).

## Behavior deltas vs iOS

Areas where Android intentionally diverges from iOS. Each delta documents the spec entry that records it.

| Delta | iOS today | Android | Spec |
|---|---|---|---|
| Phone-verification gate on create-package / create-trip / book-trip / request-to-carry | **Not gated** — `User.isPhoneVerified` read for display only; UI/VMs proceed regardless. | **Hard-gated** at UI button (`MainTabScreen`, `ShipperExploreContent`) and VM entry (`RequirePhoneVerificationUseCase`). Surfaces shared `VerifyPhonePromptSheet`. Action is **not** auto-resumed after verification. | [10-verification.md](10-verification.md) § Phone-verification gate |
