# 14 — Design system (iOS source → Android parity)

**Phase:** 0 (foundation) + **applies to every feature** with UI.
**iOS source of truth:** [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift) — spacing, radii, colors, typography, shadows, cards, borders, layout, animation.
**iOS shared components:** [`Views/Components/`](../../Pasabayan/Views/Components/) — PButton, Card, EmptyStateView, badges, popovers, pickers.

Android must implement a **single Compose theme** (Material 3) whose tokens **map 1:1** to these values, plus **base reusable composables** that feature modules extend.

## Mandatory rules

1. **No ad-hoc styling:** Do not hardcode raw `dp`/`sp`/colors in feature composables. Use **`PasabayanTheme`** tokens.
2. **Single source:** iOS remains [`DesignSystem.swift`](../../Pasabayan/Views/Components/DesignSystem.swift); Android theme values **must match** the tables below.
3. **Changes:** Update iOS `DesignSystem.swift` first → then **this doc** → then Android theme. Keep in sync.
4. **Review:** PRs that touch UI must confirm token usage (screenshots optional but recommended).

---

## Spacing (`DesignSystem.Spacing`)

Base unit **4 dp**.

| Token | dp | Semantic alias |
|-------|-----|---------------|
| xs | 4 | — |
| sm | 8 | `itemSpacing`, `iconTextSpacing`, `listRowSpacing` |
| md | 12 | `buttonPadding`, `gridGutter` |
| lg | 16 | `screenPadding`, `cardPadding` |
| xl | 20 | `formFieldSpacing` |
| xxl | 24 | `sectionSpacing` |
| xxxl | 32 | — |
| xxxxl | 40 | — |
| xxxxxl | 48 | — |

---

## Corner radius (`DesignSystem.CornerRadius`)

| Token | dp | Use |
|-------|-----|-----|
| xs | 4 | badges |
| sm | 8 | buttons, inputs |
| md | 12 | **default cards** |
| lg | 16 | large cards, modals |
| xl | 20 | special |
| xxl | 24 | large avatars |
| capsule | 999 | pill shapes |

Semantic aliases: `button` = sm (8), `card` = md (12), `modal` = lg (16), `badge` = xs (4), `avatar` = xxl (24).

---

## Colors (`DesignSystem.Colors`) — complete inventory

### Primary / brand

| Token | Value | Notes |
|-------|-------|-------|
| `primary` | Black `#000000` | Brand primary |
| `primaryLight` | Gray (systemGray) | Lighter variant |
| `primaryDark` | Black | Darker variant |
| `secondary` | Gray | |
| `secondaryLight` | systemGray4 | |
| `secondaryDark` | systemGray2 | |

### Semantic

| Token | Value |
|-------|-------|
| `success` | Green `#34C759` |
| `warning` | Orange `#FF9500` |
| `error` | Red `#C13515` |
| `info` | Blue `#007AFF` |
| `successLight` | Green 0.1 opacity |
| `warningLight` | Yellow 0.1 opacity |
| `errorLight` | Red 0.1 opacity |
| `infoLight` | Teal 0.1 opacity |

### Background / surface

| Token | iOS | Android mapping |
|-------|-----|-----------------|
| `background` | systemBackground | `colorScheme.background` |
| `secondaryBackground` | secondarySystemBackground | `colorScheme.surfaceVariant` |
| `tertiaryBackground` | tertiarySystemBackground | `colorScheme.surfaceContainerHighest` |
| `cardBackground` | systemBackground | `colorScheme.surface` |
| `modalBackground` | systemBackground | `colorScheme.surface` |
| `screenBackground` | systemBackground | `colorScheme.background` |
| `surface` | systemBackground | `colorScheme.surface` |
| `surfaceSecondary` | secondarySystemBackground | `colorScheme.surfaceVariant` |
| `surfaceTertiary` | tertiarySystemBackground | `colorScheme.surfaceContainerHighest` |
| `overlay` | Black 0.3 | `colorScheme.scrim` |

### Text

| Token | iOS | Android mapping |
|-------|-----|-----------------|
| `textPrimary` | Color.primary (label) | `colorScheme.onBackground` |
| `textSecondary` | Color.secondary (secondaryLabel) | `colorScheme.onSurfaceVariant` |
| `textTertiary` | tertiaryLabel | `colorScheme.onSurface` at 0.38 alpha |
| `textInverse` | White | `colorScheme.inverseOnSurface` |
| `textOnPrimary` | White | `colorScheme.onPrimary` |
| `textOnSecondary` | Black | `colorScheme.onSecondary` |

### Border

| Token | iOS | Notes |
|-------|-----|-------|
| `border` | Color(.separator) | Default divider/outline |
| `borderSecondary` | Color(.separator) | Secondary variant |
| `borderActive` | primary (black) | Active/focused state |

### Status colors (generic)

| Token | Color |
|-------|-------|
| `statusPending` | Orange |
| `statusActive` | Blue |
| `statusCompleted` | Green |
| `statusCancelled` | Red |
| `statusMatched` | Purple |
| `statusInTransit` | Indigo |

### Trip status colors

| Token | Color |
|-------|-------|
| `tripScheduled` | Blue |
| `tripActive` | Indigo |
| `tripCompleted` | Green |
| `tripCancelled` | Red |

### Package status colors

| Token | Color |
|-------|-------|
| `packageOpen` | Blue |
| `packageMatched` | Purple |
| `packageBooked` | Orange |
| `packageInTransit` | Indigo |
| `packageDelivered` | Green |

### Badge colors (hex-defined)

| Token | Hex | Light variant (0.1 opacity) |
|-------|-----|----------------------------|
| `blue` | `#007AFF` | `blueLight` |
| `purple` | `#AF52DE` | `purpleLight` |
| `orange` | `#FF9500` | `orangeLight` |
| `green` | `#34C759` | `greenLight` |
| `teal` | `#00A699` | `tealLight` |
| `yellow` | `#FFB400` | `yellowLight` |
| `red` | `#C13515` | `redLight` |
| `gold` | `#FFD700` | `goldLight` |
| `gray` | `#8E8E93` | `grayLight` |

### Button colors

| Token | Value |
|-------|-------|
| `buttonPrimary` | primary (black) |
| `buttonSecondary` | Color.clear (outline style) |
| `buttonDestructive` | error (red) |
| `buttonDisabled` | systemGray4 |

### Shadow colors

| Token | Value |
|-------|-------|
| `shadowLight` | Black 0.1 |
| `shadowMedium` | Black 0.2 |
| `shadowDark` | Black 0.3 |

**Dark mode:** iOS uses semantic system colors; Android must use **Material dynamic color or parallel dark palette**, not only light.

---

## Typography (`DesignSystem.Typography`)

### Size scale

| Token | sp | Use |
|-------|-----|-----|
| xs | 12 | caption |
| sm | 14 | body small |
| md | 16 | body default |
| lg | 18 | emphasis |
| xl | 20 | subtitle |
| xxl | 24 | heading 3 |
| xxxl | 28 | heading 2 |
| xxxxl | 32 | heading 1 |
| xxxxxl | 40 | display |
| navigationTitle | **15** | Nav title (non-standard; tighter than default) |

### Font weights

| Token | Weight |
|-------|--------|
| `light` | FontWeight.Light (300) |
| `regular` | FontWeight.Normal (400) |
| `medium` | FontWeight.Medium (500) |
| `semibold` | FontWeight.SemiBold (600) |
| `bold` | FontWeight.Bold (700) |

### Semantic heading styles

| Token | Size | Weight |
|-------|------|--------|
| `Heading.h1` | 32 sp | Bold |
| `Heading.h2` | 28 sp | Bold |
| `Heading.h3` | 24 sp | SemiBold |
| `Heading.h4` | 20 sp | SemiBold |
| `Heading.h5` | 18 sp | Medium |
| `Heading.h6` | 16 sp | Medium |

### Semantic body styles

| Token | Size | Weight |
|-------|------|--------|
| `Body.large` | 18 sp | Regular |
| `Body.regular` | 16 sp | Regular |
| `Body.medium` | 16 sp | Medium |
| `Body.small` | 14 sp | Regular |

### Semantic caption styles

| Token | Size | Weight |
|-------|------|--------|
| `Caption.large` | 14 sp | Medium |
| `Caption.regular` | 12 sp | Regular |
| `Caption.small` | 10 sp | Regular |

### Semantic button styles

| Token | Size | Weight |
|-------|------|--------|
| `Button.large` | 18 sp | SemiBold |
| `Button.medium` | 16 sp | SemiBold |
| `Button.small` | 14 sp | Medium |

---

## Shadows (`DesignSystem.Shadow`)

| Token | Color | Radius | X | Y |
|-------|-------|--------|---|---|
| none | clear | 0 | 0 | 0 |
| xs | shadowLight | 1 | 0 | 1 |
| sm | shadowLight | 2 | 0 | 1 |
| md | shadowLight | 4 | 0 | 2 |
| lg | shadowMedium | 8 | 0 | 4 |
| xl | shadowMedium | 12 | 0 | 6 |
| xxl | shadowDark | 16 | 0 | 8 |

Semantic: `button` = sm, `card` = **none** (flat + 1px border), `modal` = lg, `floating` = xl.

---

## Cards (`DesignSystem.Card`)

| Property | Value |
|----------|--------|
| cornerRadius | 12 (`CornerRadius.md`) |
| shadow | **none** (flat) |
| borderWidth | 1 |
| borderColor | `Colors.border` |
| contentPadding | 16 (`Spacing.cardPadding`) |
| backgroundColor | `Colors.cardBackground` |

### Card variants

| Variant | cornerRadius | shadow | borderWidth |
|---------|-------------|--------|-------------|
| primary | 12 (md) | none | 1 |
| secondary | 8 (sm) | none | 1 |
| compact | 4 (xs) | none | 1 |
| large | 16 (lg) | none | 1 |

---

## Borders (`DesignSystem.Border`)

Default **1 dp** width, `Colors.border` color.

Component-specific border radii:

| Component | Radius |
|-----------|--------|
| Card | 12 (md) |
| Button | 8 (sm) |
| Badge | 4 (xs) |
| Input | 8 (sm) |

---

## Layout (`DesignSystem.Layout`)

| Token | Value |
|-------|--------|
| buttonHeight | 48 dp |
| buttonHeightSmall | 36 dp |
| buttonHeightLarge | 56 dp |
| inputHeight | 48 dp |
| avatarSizeSmall | 32 dp |
| avatarSizeMedium | 48 dp |
| avatarSizeLarge | 64 dp |
| iconSizeSmall | 16 dp |
| iconSizeMedium | 24 dp |
| iconSizeLarge | 32 dp |
| maxContentWidth | 428 dp |
| maxCardWidth | 400 dp |
| gridColumns | 12 |
| gridGutter | 12 dp (Spacing.md) |

---

## Animation (`DesignSystem.Animation`)

| Token | Duration | Curve |
|-------|----------|-------|
| fast | 0.2s | easeInOut |
| medium | 0.3s | easeInOut |
| slow | 0.5s | easeInOut |
| spring | — | response: 0.6, damping: 0.8 |
| springBouncy | — | response: 0.5, damping: 0.6 |

Semantic: `button` = fast, `modal` = medium, `transition` = slow.

---

## Base reusable components (`:core:designsystem`)

These iOS shared components must be implemented as **base composables** in `:core:designsystem` (or `:core:ui`) for feature modules to extend:

### `PasabayanButton` (from [`PButton.swift`](../../Pasabayan/Views/Components/PButton.swift))

| Prop | Type | Notes |
|------|------|-------|
| `title` | String | Button text |
| `style` | ButtonStyle | `primary`, `secondary`, `tertiary`, `destructive`, `filter`, `submit` |
| `size` | ButtonSize | `small` (36dp), `medium` (48dp), `large` (56dp) |
| `icon` | ImageVector? | Leading/trailing icon |
| `iconPosition` | IconPosition | `leading`, `trailing`, `only` |
| `isLoading` | Boolean | Show spinner |
| `isDisabled` | Boolean | Disabled state |
| `count` | Int? | Badge count (filter style) |
| `isSelected` | Boolean | Selected state (filter style) |
| `onClick` | () -> Unit | Tap callback |

**Styling per variant:**
- **Primary:** Filled black, white text, sm shadow
- **Secondary:** Outline (1px border), black text, no fill
- **Tertiary:** No shadow, no border, text-only
- **Destructive:** Red text, secondary outline style
- **Filter:** Capsule shape, toggleable selected state
- **Submit:** Large size, full-width primary

### `PasabayanCard` (from [`Card.swift`](../../Pasabayan/Views/Shared/Card.swift))

| Variant | Layout |
|---------|--------|
| `metric` | Centered value + small title (e.g. "$304.98" / "Total Earnings") |
| `statWithIcon` | Large icon + title + big value (left-aligned) |
| `compact` | Icon + centered value + centered title |
| `status` | Colored dot + value + title (horizontal) |

All variants use `DesignSystem.Card` tokens (12dp radius, 1dp border, 16dp padding).

### `EmptyStateView` (from [`EmptyStateView.swift`](../../Pasabayan/Views/Shared/EmptyStateView.swift))

| Prop | Type |
|------|------|
| `icon` | ImageVector or @DrawableRes |
| `title` | String |
| `description` | String |

Uses card styling (12dp radius, 1dp border), centered layout, gray icon.

### `NotificationBadge` (from [`NotificationBadge.swift`](../../Pasabayan/Views/Components/NotificationBadge.swift))

| Prop | Type |
|------|------|
| `count` | Int |

Red circle (16dp min), white text, `Caption2` font. Use Material 3 `BadgedBox` or custom.

### `VerificationBadge` (from [`VerificationBadge.swift`](../../Pasabayan/Views/Components/VerificationBadge.swift))

| Prop | Type |
|------|------|
| `verificationLevel` | String (`"basic"`, `"verified"`, `"premium"`) |
| `size` | BadgeSize (`small`, `medium`, `large`) |
| `showLabel` | Boolean |

Icons: checkmark seal (verified, blue), star (premium, gold). Sub-components: `UserNameWithBadge`, `VerificationStatusCard`.

### `InfoPopoverButton<Content>` (from [`InfoPopoverButton.swift`](../../Pasabayan/Views/Components/InfoPopoverButton.swift))

Generic "ⓘ" button that shows a popover/dialog with custom content. Props: `content: @Composable () -> Unit`. Sub-components: `InfoTextContent`, `InfoTableContent`, `InfoStatusTableContent`.

### `CardActionFooter` (from [`CardActionFooter.swift`](../../Pasabayan/Views/Components/CardActionFooter.swift))

| Prop | Type |
|------|------|
| `onViewDetails` | () -> Unit |
| `menuActions` | List<CardMenuAction> |
| `showViewDetailsButton` | Boolean |

Divider + "View Details" link + overflow menu. `CardMenuAction`: title, icon, action, disabled.

### `CreateOptionsSheet` (from [`CreateOptionsSheet.swift`](../../Pasabayan/Views/Components/CreateOptionsSheet.swift))

Bottom sheet for selecting creation type. Two options: ship package vs errand service. Uses `ModalBottomSheet` with `CreateOptionRow` sub-component.

### `WebView` / `ReadOnlyWebView`

| Component | Purpose |
|-----------|---------|
| `WebView` | Load HTML files (legal/articles) with localization support |
| `ReadOnlyWebView` | Display external URLs with all interaction disabled (injects CSS to block pointer events) |

Android: use `AndroidView` wrapping `android.webkit.WebView`.

### `ImagePicker` (from [`ImagePicker.swift`](../../Pasabayan/Views/Components/ImagePicker.swift))

Camera + photo library selection. Android: use Activity Result API (`ActivityResultContracts.TakePicture` / `GetContent`).

### `AvatarUploadInstructions` (from [`AvatarUploadInstructions.swift`](../../Pasabayan/Views/Components/AvatarUploadInstructions.swift))

Photo upload requirements and validation feedback. Sub-components: `RequirementRow`, `ImageValidationView`, `AvatarRequirementsBadge`.

---

## Design system Modifier extensions

iOS provides `View` extension modifiers (`dsSpacingLG()`, `dsCardStyle()`, etc.). Android should implement equivalent `Modifier` extensions in `:core:designsystem`:

### Spacing

```kotlin
fun Modifier.dsSpacingXS() = this.padding(PasabayanSpacing.xs)
fun Modifier.dsSpacingSM() = this.padding(PasabayanSpacing.sm)
fun Modifier.dsSpacingMD() = this.padding(PasabayanSpacing.md)
fun Modifier.dsSpacingLG() = this.padding(PasabayanSpacing.lg)
fun Modifier.dsScreenPadding() = this.padding(horizontal = PasabayanSpacing.screenPadding)
```

### Card styles

```kotlin
fun Modifier.dsCardStyle() = this
    .background(PasabayanColors.cardBackground, RoundedCornerShape(PasabayanRadius.card))
    .border(PasabayanBorder.width, PasabayanColors.border, RoundedCornerShape(PasabayanRadius.card))
    .padding(PasabayanSpacing.cardPadding)

fun Modifier.dsCardPrimary() = dsCardStyle()  // radius 12
fun Modifier.dsCardSecondary() = ...           // radius 8
fun Modifier.dsCardCompact() = ...             // radius 4
fun Modifier.dsCardLarge() = ...               // radius 16
```

### Shadows

```kotlin
fun Modifier.dsShadowXS() = this.shadow(1.dp, shape, ambientColor = shadowLight, spotColor = shadowLight)
fun Modifier.dsShadowCard() = this  // Card uses no shadow
fun Modifier.dsShadowButton() = dsShadowSM()
fun Modifier.dsShadowModal() = dsShadowLG()
```

### Borders

```kotlin
fun Modifier.dsBorder() = this.border(1.dp, PasabayanColors.border)
fun Modifier.dsBorderCard() = this.border(1.dp, PasabayanColors.border, RoundedCornerShape(12.dp))
fun Modifier.dsBorderButton() = this.border(1.dp, PasabayanColors.border, RoundedCornerShape(8.dp))
fun Modifier.dsBorderInput() = this.border(1.dp, PasabayanColors.border, RoundedCornerShape(8.dp))
```

### Text colors

```kotlin
fun Modifier.dsTextPrimary() = this  // color set via LocalContentColor
fun Modifier.dsTextSecondary() = this
```

---

## Android implementation checklist

- [ ] **`PasabayanTheme`**: `PasabayanSpacing`, `PasabayanRadius`, `PasabayanColors` (all 60+ tokens), `PasabayanTypography` (all semantic styles).
- [ ] **Material 3** `ColorScheme` + `Typography` wired to tokens; nav title **15 sp**; dark mode palette.
- [ ] **All color tokens**: primary variants, semantic + light variants, background/surface hierarchy, text hierarchy, border variants, status colors, badge hex + light variants, button colors, shadow colors.
- [ ] **All typography styles**: Heading h1-h6, Body large/regular/medium/small, Caption large/regular/small, Button large/medium/small.
- [ ] **Cards**: 4 variants (primary/secondary/compact/large) with correct radius, border, no shadow.
- [ ] **Modifier extensions**: `dsCardStyle()`, `dsSpacing*()`, `dsShadow*()`, `dsBorder*()` — see section above.
- [ ] **Base composables** (11 total): `PasabayanButton` (6 styles × 3 sizes), `PasabayanCard` (4 variants), `EmptyStateView`, `NotificationBadge`, `VerificationBadge`, `InfoPopoverButton`, `CardActionFooter`, `CreateOptionsSheet`, `WebView`, `ReadOnlyWebView`, `ImagePicker`, `AvatarUploadInstructions`.
- [ ] **Feature specs** (`03`–`13`): confirm token usage in all UI code.
- [ ] **Screenshot / UI tests**: compare key screens to iOS for spacing and type scale.

---

## Related docs

- [00-architecture.md](00-architecture.md) — MVI + UI parity overview
- [13-ui-tab-explore.md](13-ui-tab-explore.md) — tab and Explore hierarchy
- [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md) — Phase 0 includes this spec

---

## Canonical spec location

All Android written specs for this repo live under **`android-spec/` in pasabayan-android**. Implementation and PRs reference **this** `14-design-system.md` as authoritative for tokens and Compose primitives (not copies under other repos).

---

## Compose `P*` primitives (implemented in `:core:designsystem`)

Public composables use the **`P` prefix** (Pasabayan) for discoverability and parity with iOS naming:

| Type / composable | Role |
|-------------------|------|
| `PButton`, `PButtonStyle`, `PButtonSize`, `PIconPosition` | Button system (primary, secondary, tertiary, destructive, filter, submit) |
| `PCard`, `PCardVariant` | Flat bordered surface (no shadow) |
| `PasabayanTheme` | Root Material 3 theme + tokens |

**Planned next slices:** `POutlinedTextField`, `PScaffold` + snackbar host, `PModalBottomSheet`, pull-to-refresh / top bar helpers — see TDD slices below.

### Android naming: sheet vs modal

- **Bottom sheet** (iOS “sheet” for panels): use **`PModalBottomSheet`** / Material `ModalBottomSheet` — not a vague `PModal`.
- **Popover / info (“i”):** prefer **bottom sheet or dialog** on phones; optional anchored `Popup` / `DropdownMenu` for dense menus.

---

## TDD and delivery slices (Phase 0)

Work proceeds in **small slices** with tests in `:core:designsystem`:

| Slice | Content |
|-------|---------|
| **DS-0** | Android test deps + `PasabayanTheme` smoke Compose test |
| **DS-1** | Token objects (`PasabayanBorder`, `PasabayanLayout`, `PasabayanMotion`, `PasabayanTextStyles`) + JVM tests for invariants |
| **DS-2** | Compose tests: light theme primary, typography spot checks |
| **DS-3** | `PButton` + interaction tests |
| **DS-4** | `PCard` + tests |
| **DS-5** | `POutlinedTextField` + tests |
| **DS-6** | `PScaffold` / snackbar + tests |
| **DS-7** | `PModalBottomSheet` + popover guidance |

**Policy:** add or extend **tests with each slice** (JVM for tokens; `src/androidTest` + `createComposeRule` for theme and `P*`). Optional screenshot tests (e.g. Paparazzi) are non-blocking for Phase 0.

### Modifier extensions (`ds*`)

Implemented in `PasabayanModifiers.kt`: `dsSpacing*`, `dsScreenPaddingHorizontal`, `dsCardStyle` / variant aliases, `dsShadowButton`.

