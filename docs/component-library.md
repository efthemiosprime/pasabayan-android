# Component Library - Pasabayan Android App

**Comprehensive UI Component Documentation for Jetpack Compose**

This document provides detailed information about all reusable UI components in the Pasabayan Android app, organized by category and function.

## 📋 Table of Contents

1. [Design System Foundation](#design-system-foundation)
2. [Global Card System](#global-card-system)
3. [Button Components](#button-components)
4. [Status and Badge Components](#status-and-badge-components)
5. [Form Components](#form-components)
6. [Analytics Components](#analytics-components)
7. [Navigation Components](#navigation-components)
8. [Utility Components](#utility-components)

---

## 🎨 Design System Foundation

### PasabayanDesignSystem.kt (567 lines)

The comprehensive design system that ensures consistency across all components.

#### Color System
```kotlin
// Semantic colors
PasabayanDesignSystem.Colors.primary         // #000000 (Brand black)
PasabayanDesignSystem.Colors.primaryVariant  // #333333 (Dark variant)
PasabayanDesignSystem.Colors.secondary       // #FF6B35 (Orange accent)
PasabayanDesignSystem.Colors.success         // #4CAF50 (Green success)
PasabayanDesignSystem.Colors.warning         // #FF9800 (Orange warning)
PasabayanDesignSystem.Colors.error           // #F44336 (Red error)
PasabayanDesignSystem.Colors.surface         // #FFFFFF (White surface)
PasabayanDesignSystem.Colors.background      // #F5F5F5 (Light gray background)

// Text colors
PasabayanDesignSystem.Colors.onSurface       // #000000 (Primary text)
PasabayanDesignSystem.Colors.onSurfaceVariant // #666666 (Secondary text)
PasabayanDesignSystem.Colors.onBackground    // #000000 (Background text)
```

#### Typography Scale
```kotlin
// Heading styles
PasabayanDesignSystem.Typography.headingLarge    // 32sp, Bold
PasabayanDesignSystem.Typography.headingMedium   // 24sp, Bold
PasabayanDesignSystem.Typography.headingSmall    // 20sp, Bold

// Body styles
PasabayanDesignSystem.Typography.bodyLarge       // 16sp, Regular
PasabayanDesignSystem.Typography.bodyMedium      // 14sp, Regular
PasabayanDesignSystem.Typography.bodySmall       // 12sp, Regular

// Label styles
PasabayanDesignSystem.Typography.labelLarge      // 14sp, Medium
PasabayanDesignSystem.Typography.labelMedium     // 12sp, Medium
PasabayanDesignSystem.Typography.labelSmall      // 10sp, Medium
```

#### Spacing System (4dp Grid)
```kotlin
// Base spacing units
PasabayanDesignSystem.Spacing.xs = 4.dp          // Extra small
PasabayanDesignSystem.Spacing.sm = 8.dp          // Small
PasabayanDesignSystem.Spacing.md = 12.dp         // Medium
PasabayanDesignSystem.Spacing.lg = 16.dp         // Large
PasabayanDesignSystem.Spacing.xl = 20.dp         // Extra large
PasabayanDesignSystem.Spacing.xxl = 24.dp        // Extra extra large

// Semantic spacing
PasabayanDesignSystem.Spacing.cardPadding = 16.dp        // Card internal padding
PasabayanDesignSystem.Spacing.screenPadding = 16.dp      // Screen edge padding
PasabayanDesignSystem.Spacing.sectionSpacing = 24.dp     // Between sections
PasabayanDesignSystem.Spacing.itemSpacing = 12.dp        // Between list items
```

#### Elevation System
```kotlin
PasabayanDesignSystem.Elevation.none = 0.dp      // Flat elements
PasabayanDesignSystem.Elevation.card = 4.dp      // Standard cards
PasabayanDesignSystem.Elevation.modal = 8.dp     // Modals and dialogs
PasabayanDesignSystem.Elevation.floating = 12.dp // FABs and floating elements
```

---

## 📄 Global Card System

### PCard.kt (234 lines) - Base Card Component

The foundation card component with comprehensive customization options.

```kotlin
@Composable
fun PCard(
    modifier: Modifier = Modifier,
    elevation: Dp = PasabayanDesignSystem.Elevation.card,
    backgroundColor: Color = PasabayanDesignSystem.Colors.surface,
    contentColor: Color = PasabayanDesignSystem.Colors.onSurface,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
)
```

### PCardStandard.kt (123 lines) - Standard Card

Pre-configured card with global design standards.

```kotlin
@Composable
fun PCardStandard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    PCard(
        modifier = modifier,
        elevation = 4.dp,                    // Global standard
        backgroundColor = Color.White,       // Global standard
        shape = RoundedCornerShape(12.dp),  // Global standard
        content = content
    )
}
```

**Key Features:**
- **Forced Global Standards**: 4dp elevation, white background, 12dp corners
- **Consistent Padding**: 16dp internal padding
- **Accessibility**: Proper content descriptions
- **Material 3**: Dynamic theming support

### PCardCompact.kt (89 lines) - Compact Variant

Reduced padding version for dense layouts.

```kotlin
@Composable
fun PCardCompact(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    PCardStandard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp), // Reduced from 16dp
            content = content
        )
    }
}
```

### Specialized Card Components

#### MetricDisplayCard.kt (156 lines)
```kotlin
@Composable
fun MetricDisplayCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = PasabayanDesignSystem.Colors.primary,
    modifier: Modifier = Modifier
)
```

#### ActionCard.kt (98 lines)
```kotlin
@Composable
fun ActionCard(
    title: String,
    description: String? = null,
    actionText: String,
    onActionClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
)
```

#### StatusCard.kt (76 lines)
```kotlin
@Composable
fun StatusCard(
    status: String,
    message: String,
    statusColor: Color,
    backgroundColor: Color = statusColor.copy(alpha = 0.1f),
    modifier: Modifier = Modifier
)
```

---

## 🔘 Button Components

### PButton.kt (298 lines) - Advanced Button System

Comprehensive button component with multiple styles and states.

#### Button Styles
```kotlin
enum class PButtonStyle {
    Primary,      // Filled with primary color
    Secondary,    // Outlined with primary color
    Tertiary,     // Text-only button
    Destructive,  // Red background for dangerous actions
    Ghost,        // Transparent background
    Text          // Minimal text-only style
}
```

#### Button Sizes
```kotlin
enum class PButtonSize {
    Small,        // 32dp height, 12dp padding
    Medium,       // 40dp height, 16dp padding
    Large,        // 48dp height, 20dp padding
    ExtraLarge    // 56dp height, 24dp padding
}
```

#### Usage Examples
```kotlin
// Primary action button
PButton(
    text = "Sign In",
    style = PButtonStyle.Primary,
    size = PButtonSize.Large,
    isLoading = isLoading,
    onClick = { /* action */ }
)

// Secondary button with icon
PButton(
    text = "Cancel",
    style = PButtonStyle.Secondary,
    size = PButtonSize.Medium,
    leadingIcon = Icons.Default.Close,
    onClick = { /* action */ }
)

// Destructive action
PButton(
    text = "Delete Account",
    style = PButtonStyle.Destructive,
    size = PButtonSize.Large,
    onClick = { /* action */ }
)
```

**Key Features:**
- **Loading States**: Animated progress indicators
- **Icon Support**: Leading and trailing icons
- **Haptic Feedback**: Tactile interaction response
- **Accessibility**: Full TalkBack support
- **State Management**: Enabled/disabled states
- **Consistent Styling**: Follows design system

### Specialized Button Components

#### FABButton.kt (54 lines)
```kotlin
@Composable
fun FABButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PasabayanDesignSystem.Colors.primary
)
```

#### IconButton.kt (43 lines)
```kotlin
@Composable
fun PIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = PasabayanDesignSystem.Colors.onSurface
)
```

---

## 🏷️ Status and Badge Components

### TripStatusBadge.kt (78 lines)

Displays trip status with semantic colors and styling.

```kotlin
@Composable
fun TripStatusBadge(
    status: TripStatus,
    style: BadgeStyle = BadgeStyle.Default,
    modifier: Modifier = Modifier
)

enum class TripStatus {
    PLANNED,     // Gray - Trip created but not started
    ACTIVE,      // Blue - Trip in progress
    PICKING_UP,  // Orange - Collecting packages
    IN_TRANSIT,  // Green - Delivering packages
    DELIVERED,   // Success green - Completed successfully
    CANCELLED    // Red - Trip cancelled
}
```

### PackageStatusBadge.kt (89 lines)

Shows package delivery status with contextual information.

```kotlin
@Composable
fun PackageStatusBadge(
    status: PackageStatus,
    style: BadgeStyle = BadgeStyle.Default,
    showIcon: Boolean = true,
    modifier: Modifier = Modifier
)

enum class PackageStatus {
    REQUESTED,      // Yellow - Package request created
    MATCHED,        // Blue - Matched with carrier
    CONFIRMED,      // Green - Booking confirmed
    PICKED_UP,      // Orange - Package collected
    IN_TRANSIT,     // Blue - Being delivered
    DELIVERED,      // Success - Delivered successfully
    FAILED,         // Red - Delivery failed
    CANCELLED       // Gray - Request cancelled
}
```

### UserVerificationBadge.kt (67 lines)

Displays user verification status and level.

```kotlin
@Composable
fun UserVerificationBadge(
    verificationLevel: VerificationLevel,
    phoneVerified: Boolean,
    modifier: Modifier = Modifier
)

enum class VerificationLevel {
    UNVERIFIED,    // Gray badge
    BASIC,         // Blue badge
    VERIFIED,      // Green badge
    PREMIUM        // Gold badge
}
```

---

## 📝 Form Components

### Input Fields

#### EmailField.kt
```kotlin
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    validationResult: ValidationResult?,
    modifier: Modifier = Modifier,
    label: String = "Email Address",
    placeholder: String = "Enter your email"
)
```

#### PasswordField.kt
```kotlin
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    validationResult: ValidationResult?,
    modifier: Modifier = Modifier,
    label: String = "Password",
    showPassword: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {}
)
```

#### LocationPicker.kt
```kotlin
@Composable
fun LocationPicker(
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Location",
    placeholder: String = "Choose location"
)
```

### Form Validation

#### ValidationResult.kt (58 lines)
```kotlin
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Success
        
    val errorMessage: String?
        get() = if (this is Error) message else null
}
```

---

## 📊 Analytics Components

### MetricCard.kt (67 lines)

Displays key performance metrics with trend indicators.

```kotlin
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    trend: TrendDirection? = null,
    trendValue: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
)

enum class TrendDirection {
    UP,      // Green upward arrow
    DOWN,    // Red downward arrow
    STABLE   // Gray horizontal line
}
```

### ChartView.kt (123 lines)

Reusable chart component for data visualization.

```kotlin
@Composable
fun ChartView(
    data: List<ChartDataPoint>,
    chartType: ChartType = ChartType.LINE,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    showLabels: Boolean = true
)

enum class ChartType {
    LINE,        // Line chart
    BAR,         // Bar chart
    PIE,         // Pie chart
    AREA         // Area chart
}
```

### AnalyticsFilter.kt (89 lines)

Filter component for analytics data.

```kotlin
@Composable
fun AnalyticsFilter(
    selectedPeriod: TimePeriod,
    onPeriodChange: (TimePeriod) -> Unit,
    selectedMetric: MetricType,
    onMetricChange: (MetricType) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## 🧭 Navigation Components

### BottomNavigationBar.kt
```kotlin
@Composable
fun PasabayanBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    userRole: UserRole,
    modifier: Modifier = Modifier
)
```

### TabRow.kt
```kotlin
@Composable
fun PasabayanTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>,
    modifier: Modifier = Modifier
)
```

### DrawerMenu.kt
```kotlin
@Composable
fun PasabayanDrawerMenu(
    currentUser: User?,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
)
```

---

## 🛠️ Utility Components

### EmptyStateView.kt (67 lines)

Displays empty state with illustration and action.

```kotlin
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### LoadingView.kt (45 lines)

Consistent loading indicators.

```kotlin
@Composable
fun LoadingView(
    message: String = "Loading...",
    modifier: Modifier = Modifier
)
```

### ErrorView.kt (89 lines)

Error display with retry functionality.

```kotlin
@Composable
fun ErrorView(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)
```

### SearchBar.kt (123 lines)

Search input with filtering capabilities.

```kotlin
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
)
```

### FilterChips.kt (78 lines)

Chip-based filtering component.

```kotlin
@Composable
fun FilterChips(
    filters: List<FilterOption>,
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

### BottomSheet.kt (167 lines)

Modal bottom sheet for additional actions.

```kotlin
@Composable
fun PasabayanBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
)
```

---

## 🎯 Usage Guidelines

### Component Selection
- **Cards**: Use `PCardStandard` for most content, `PCardCompact` for dense layouts
- **Buttons**: Use `PButton` with appropriate style and size for all interactive elements
- **Status**: Use specific badge components for status indicators
- **Forms**: Use dedicated field components with validation support

### Consistency Rules
- **Always use design system constants** for colors, spacing, and typography
- **Follow Material 3 guidelines** for interaction patterns
- **Implement proper accessibility** with content descriptions
- **Test on different screen sizes** to ensure responsive design

### Performance Considerations
- **Use `remember` for expensive calculations** in composables
- **Implement proper keys** for LazyColumn items
- **Avoid creating new objects** in composition scope
- **Use derivedStateOf** for computed values

---

**Pasabayan Android** - *Comprehensive Component Library* 🚛📱✨ 