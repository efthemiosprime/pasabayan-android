# 13 — UI: tabs, Explore, and alignment with iOS

**Phase:** 2 primary (Explore + shell); **applies across** later phases as new screens appear. **Feature:** cross-cutting UI / navigation (not a single API module). See [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md).

**Full tab matrix (all tabs):** [15-platform-and-tab-index.md](15-platform-and-tab-index.md) — indexes every `docs/tabs/*.md` file and cross-checks iOS features.

This spec ties **Android UI behavior** to the **current iOS app** so Compose screens **mirror** structure, navigation, and chrome—not only API contracts.

## Authoritative references

| Topic | Document / code |
|-------|------------------|
| **All tab docs** | [docs/tabs/README.md](../tabs/README.md), [15-platform-and-tab-index.md](15-platform-and-tab-index.md) |
| Shipper Explore tab | [SHIPPER_TAB_EXPLORE.md](../tabs/SHIPPER_TAB_EXPLORE.md) |
| Shipper Matches (counter-offer chips, filters) | [SHIPPER_TAB_MATCHES.md](../tabs/SHIPPER_TAB_MATCHES.md) |
| Shipper Packages | [SHIPPER_TAB_PACKAGES.md](../tabs/SHIPPER_TAB_PACKAGES.md) |
| Carrier Explore tab | [CARRIER_TAB_EXPLORE.md](../tabs/CARRIER_TAB_EXPLORE.md) |
| Carrier Matches | [CARRIER_TAB_MATCHES.md](../tabs/CARRIER_TAB_MATCHES.md) |
| Carrier My Trips | [CARRIER_TAB_MYTRIPS.md](../tabs/CARRIER_TAB_MYTRIPS.md) |
| Messages (shared) | [SHARED_TAB_MESSAGES.md](../tabs/SHARED_TAB_MESSAGES.md) |
| Profile (shared) | [SHARED_TAB_PROFILE.md](../tabs/SHARED_TAB_PROFILE.md) |
| **Design system (follow)** | [**14-design-system.md**](14-design-system.md) (tokens + rules); iOS source [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift) |
| Shipper tab shell | [`ShipperDashboardContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/ShipperDashboardContent.swift) |
| Shipper Explore root | [`ShipperHomeContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/ShipperHomeContent.swift) |
| Carrier tab shell | [`CarrierDashboardContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/CarrierDashboardContent.swift) |
| Carrier Explore root | [`CarrierHomeContent.swift`](../../Pasabayan/Views/Screens/Dashboard/DashboardComponents/CarrierHomeContent.swift) |

## Non-negotiables (mirror iOS)

1. **Tab bar (per role)**  
   - Same **tab index, label, and root screen** as in the tab docs (Explore = index 0, then Matches, role-specific third tab, Messages, Profile).  
   - Same **badge semantics** where documented (e.g. Matches, Messages unread, Profile notification dot).

2. **Explore tab**  
   - Reproduce the **view hierarchy** described in the tab docs: header card, search, secondary sections (e.g. top carriers / recent searches / popular routes), and documented sheet destinations (filters, profile popover, verification).  
   - **Role-specific** differences (shipper vs carrier) must match iOS, not a single generic Explore.

3. **Global actions**  
   - Notification entry (iOS: bell → `ComprehensiveNotificationsView`) should appear in the **same relative position** (e.g. top trailing) with equivalent presentation (sheet).

4. **Design tokens (mandatory)**  
   - Implement and **only use** the shared theme defined in [**14-design-system.md**](14-design-system.md): 4 dp grid, radii, semantic colors, typography scale, card border (1 dp) and flat elevation to match iOS `Card` defaults.

5. **Copy**  
   - Section titles, empty states, and primary CTAs should match iOS **unless** localization files differ; track copy in feature specs when adding Android-only wording.

## Platform differences (document when used)

- **Navigation:** Android system back and predictive back vs iOS edge swipe; preserve **logical** stack (same order of screens for the same user path).  
- **Sheets:** Prefer `ModalBottomSheet` / dialogs to mirror SwiftUI `.sheet` where the iOS doc says “sheet”.  
- **Safe areas / status bar:** Match padding intent from iOS, not pixel-perfect cloning.

## ROP / state (presentation)

- MVI `UiState` can differ internally from iOS `@Published` fields, but **observable UI** (loading, errors, list content, badges) should match what a user sees on iOS for the same data.

## TDD checklist (UI parity)

- [ ] For each tab root, add a **mapping table**: iOS view file → Android route + main composable(s).  
- [ ] Screenshot or Compose UI test: Explore tab matches documented hierarchy for shipper and carrier.  
- [ ] Badge tests: same counts/sources as iOS ViewModels for Matches/Messages/Profile notification entry.  
- [ ] Theme test: sample card/button use only tokens from [**14-design-system.md**](14-design-system.md) (exact match to iOS `DesignSystem`).

## Related feature specs

Each feature spec (`03`–`12`) should include a **“UI (iOS reference)”** subsection pointing to SwiftUI sources (e.g. `CompatibleTripsView`, `RequestToCarrySheet`, `EditTripSheet`). **Styling** for those screens must always go through [**14-design-system.md**](14-design-system.md).
