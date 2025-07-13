# Global Background Color System

This document explains how to implement and use the global background color (#faf8fe) across all screens in the Pasabayan Android app.

## 🎨 Design System Integration

### Color Definition
The global background color is defined in `PasabayanDesignSystem.Colors.screenBackground`:

```kotlin
// In ui/theme/Theme.kt
object Colors {
    // ... other colors
    val screenBackground = Color(0xFFFAF8FE) // Global screen background #faf8fe
}
```

### Material 3 Integration
The background color is automatically integrated into the Material 3 theme:

```kotlin
private val LightColorScheme = lightColorScheme(
    // ... other colors
    background = PasabayanDesignSystem.Colors.screenBackground,
    onBackground = PasabayanDesignSystem.Colors.onSurface
)
```

## 🛠️ Implementation

### ScreenContainer Component
Use the `ScreenContainer` wrapper for all screen composables:

```kotlin
// ui/shared/ScreenContainer.kt
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PasabayanDesignSystem.Colors.screenBackground)
    ) {
        content()
    }
}
```

### Usage Pattern
Wrap your screen content with `ScreenContainer`:

```kotlin
@Composable
fun YourScreen() {
    ScreenContainer {
        // Your screen content here
        LazyColumn {
            // Content
        }
    }
}
```

## 📱 Screen Updates Required

### ✅ Completed
- **ProfileScreen** - Updated with ScreenContainer
- **AnalyticsScreen** - Updated with ScreenContainer

### 🔄 Pending Updates
Apply the ScreenContainer to these screens:

#### Home Screens
```kotlin
// CarrierHomeContent
@Composable
fun CarrierHomeContent(...) {
    ScreenContainer {
        // existing content
    }
}

// ShipperHomeContent  
@Composable
fun ShipperHomeContent(...) {
    ScreenContainer {
        // existing content
    }
}
```

#### Dashboard Tab Contents
```kotlin
// BrowseTabContent
@Composable
fun BrowseTabContent(...) {
    ScreenContainer {
        // existing content
    }
}

// PackagesOrTripsTabContent
@Composable 
fun PackagesOrTripsTabContent(...) {
    ScreenContainer {
        // existing content
    }
}

// CreateTabContent
@Composable
fun CreateTabContent(...) {
    ScreenContainer {
        // existing content
    }
}
```

#### Other Screens
```kotlin
// MyPackagesScreen
@Composable
fun MyPackagesScreen(...) {
    ScreenContainer {
        // existing content  
    }
}

// MyTripsScreen
@Composable
fun MyTripsScreen(...) {
    ScreenContainer {
        // existing content
    }
}

// MatchesScreen
@Composable
fun MatchesScreen(...) {
    ScreenContainer {
        // existing content
    }
}
```

## 🎯 Implementation Steps

### Step 1: Import ScreenContainer
Add the import to your screen file:
```kotlin
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
```

### Step 2: Wrap Content
Replace your existing screen container with ScreenContainer:

**Before:**
```kotlin
@Composable
fun YourScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // content
    }
}
```

**After:**
```kotlin
@Composable
fun YourScreen() {
    ScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // content
        }
    }
}
```

### Step 3: Test and Verify
Ensure the background color (#faf8fe) is applied correctly across the entire screen.

## 🔧 Centralized Management

### Updating the Color
To change the global background color, update it in one place:

```kotlin
// ui/theme/Theme.kt
object Colors {
    val screenBackground = Color(0xFFFAF8FE) // Change this single value
}
```

### Color Variations
For special cases, you can override the background:

```kotlin
ScreenContainer(
    modifier = Modifier.background(Color.White) // Override for special cases
) {
    // content
}
```

## 📋 Checklist

Use this checklist to track screen updates:

- [x] ProfileScreen
- [x] AnalyticsScreen  
- [ ] CarrierHomeContent
- [ ] ShipperHomeContent
- [ ] BrowseTabContent
- [ ] PackagesOrTripsTabContent
- [ ] CreateTabContent
- [ ] MyPackagesScreen
- [ ] MyTripsScreen
- [ ] MatchesScreen
- [ ] AuthScreen (if needed)
- [ ] SplashScreen (if needed)

## 🎨 Design Benefits

- **Consistency**: All screens have the same background color
- **Maintainability**: Single point of control for global background
- **Flexibility**: Easy to change globally or override locally
- **Material 3**: Integrated with the theme system
- **Performance**: Minimal overhead with efficient implementation

## 🚀 Future Enhancements

- **Dark Mode**: Automatic background color switching
- **Accessibility**: High contrast background options
- **Theming**: User-customizable background colors
- **Branding**: Season-specific or promotional backgrounds

---

## 📚 Additional Resources

- **[Global Background Usage Index](GLOBAL_BACKGROUND_USAGE_INDEX.md)** - Comprehensive analysis and status matrix
- **[Prompting Templates](PROMPTING_TEMPLATES.md)** - Optimized development templates and patterns

---

**Note**: Remember to test on different devices and screen sizes to ensure the background covers the entire screen area properly. 