# 14 — Design system (iOS source → Android parity)

**Phase:** 0 (foundation) + **applies to every feature** with UI.  
**iOS source of truth:** [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift) — spacing, radii, colors, typography, shadows, cards, borders, layout, animation.

Android must implement a **single Compose theme** (Material 3) whose tokens **map 1:1** to these values so UI matches [13-ui-tab-explore.md](13-ui-tab-explore.md) and feature screens.

## Mandatory rules (follow this spec)

1. **No ad-hoc styling:** Do not hardcode raw `dp`/`sp`/colors in feature composables except where this document explicitly allows one-offs. Use **`PasabayanTheme`** (or equivalent) — spacing, `CornerRadius`, `PasabayanColors`, `PasabayanTypography`.
2. **Single source:** iOS remains [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift); Android theme values **must match** the tables below (or be derived from the same hex / pt).
3. **Changes:** Any visual change starts with an update to **`DesignSystem.swift` on iOS** (or a written exception in the feature spec), then **this doc** and the Android theme — keep them in sync.
4. **Review:** PRs that touch UI must confirm token usage (screenshots optional but recommended for high-traffic screens).

---

## Spacing (`DesignSystem.Spacing`)

Base unit **4 pt** (use **4 dp** on Android).

| Token | pt (iOS) | Semantic |
|-------|----------|----------|
| xs | 4 | — |
| sm | 8 | — |
| md | 12 | — |
| lg | 16 | screen padding, card padding |
| xl | 20 | form field spacing |
| xxl | 24 | section spacing |
| xxxl | 32 | — |
| xxxxl | 40 | — |
| xxxxxl | 48 | — |
| cardPadding | 16 (= lg) | card content |
| sectionSpacing | 24 (= xxl) | between sections |
| screenPadding | 16 (= lg) | screen horizontal |
| buttonPadding | 12 (= md) | button inner |
| itemSpacing | 8 (= sm) | list items |
| iconTextSpacing | 8 (= sm) | icon + label |
| listRowSpacing | 8 (= sm) | rows |
| formFieldSpacing | 20 (= xl) | form fields |

---

## Corner radius (`DesignSystem.CornerRadius`)

| Token | pt | Use |
|-------|-----|-----|
| xs | 4 | badges |
| sm | 8 | buttons, inputs |
| md | 12 | **default cards** |
| lg | 16 | large cards, modals |
| xl | 20 | special |
| xxl | 24 | large avatars |
| button | 8 | (= sm) |
| card | 12 | (= md) |
| modal | 16 | (= lg) |
| badge | 4 | (= xs) |
| avatar | 24 | (= xxl) |
| capsule | 999 | pill shapes |

---

## Colors (`DesignSystem.Colors`) — key values

Brand uses **black-forward** primary; system surfaces follow light/dark.

| Role | iOS | Android note |
|------|-----|----------------|
| Primary | Black | `Color(0xFF000000)` or theme `primary` |
| Semantic success / warning / error / info | Green, orange, red, blue | Map to `ColorScheme` + semantic roles |
| Status (trip/package) | e.g. `tripActive` indigo, `packageMatched` purple | Mirror named colors in theme |
| Badge hex examples | `#007AFF`, `#AF52DE`, `#FF9500`, `#34C759`, `#00A699`, `#FFB400`, `#C13515`, `#8E8E93`, `#FFD700` | Define in `Color.kt` / theme extension |
| Backgrounds | `systemBackground`, `secondarySystemBackground`, … | `MaterialTheme.colorScheme.background`, `surfaceVariant` |
| Text | `primary`, `secondary`, `tertiaryLabel` | `onBackground`, `onSurfaceVariant` |
| Border | `separator` | `outline` / divider |
| Overlay | black 0.3 | `Scrim` for modals |
| Button disabled | `systemGray4` | `onSurface` disabled alpha |

**Dark mode:** iOS uses semantic system colors; Android must use **Material dynamic color or parallel dark palette**, not only light.

---

## Typography (`DesignSystem.Typography`)

| Size token | pt | Typical use |
|------------|-----|-------------|
| xs | 12 | caption |
| sm | 14 | body small |
| md | 16 | body default |
| lg | 18 | emphasis |
| xl | 20 | subtitle |
| xxl | 24 | heading 3 |
| xxxl | 28 | heading 2 |
| xxxxl | 32 | heading 1 |
| xxxxxl | 40 | display |
| navigationTitle | **15** | nav title (non-standard; tighter than default) |

**Semantic fonts:** `Heading.h1`–`h6`, `Body.*`, `Caption.*`, `Button.*` — map to `TextStyle` in Compose with same **size + weight** (e.g. h1 = 32 bold).

---

## Shadows (`DesignSystem.Shadow`)

Elevation tuples (color opacity, radius, offset): `xs` through `xxl`; semantic `button`, `card`, `modal`, `floating`.  
**Note:** `Card` currently uses **`Shadow.none`** (flat) with **1 px border** — match that unless a spec changes.

---

## Cards (`DesignSystem.Card`)

| Property | Value |
|----------|--------|
| cornerRadius | 12 (`CornerRadius.md`) |
| shadow | none (flat) |
| borderWidth | 1 |
| borderColor | `Colors.border` |
| contentPadding | `Spacing.cardPadding` (16) |

Variants: primary / secondary / compact / large — see Swift `Card.Variants`.

---

## Borders (`DesignSystem.Border`)

Default **1 px**; component-specific radii (card 12, button/badge/input as above).

---

## Layout (`DesignSystem.Layout`)

| Token | Value |
|-------|--------|
| buttonHeight | 48 |
| buttonHeightSmall | 36 |
| buttonHeightLarge | 56 |
| inputHeight | 48 |
| avatarSizeSmall / Medium / Large | 32 / 48 / 64 |
| iconSizeSmall / Medium / Large | 16 / 24 / 32 |
| maxContentWidth | 428 (reference) |
| gridGutter | 12 (`Spacing.md`) |

---

## Animation (`DesignSystem.Animation`)

| Name | Duration (approx) |
|------|---------------------|
| fast | 0.2 s |
| medium | 0.3 s |
| slow | 0.5 s |
| spring / springBouncy | spring curves |

Use `animateDpAsState` / `Crossfade` with similar durations for parity.

---

## Android implementation checklist

- [ ] **`PasabayanTheme`**: `Spacing`, `Radius`, `PasabayanColors`, `TypeScale` objects mirroring sections above.
- [ ] **Material 3** `ColorScheme` + `Typography` wired to those tokens; nav title **15 sp** where iOS uses `navigationTitle`.
- [ ] **Cards**: 12 dp radius, 1 dp border, no shadow (match iOS `Card.shadow`).
- [ ] **Feature specs** (`03`–`13`): add a one-line “Design: use `14-design-system.md` tokens” where UI is described.
- [ ] **Screenshot / UI tests**: compare key screens to iOS for spacing and type scale.

---

## Related docs

- [00-architecture.md](00-architecture.md) — MVI + UI parity overview  
- [13-ui-tab-explore.md](13-ui-tab-explore.md) — tab and Explore hierarchy  
- [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) — Phase 0 includes this spec
