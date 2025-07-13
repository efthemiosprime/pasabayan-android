# Pasabayan Android - Optimized Prompting Templates

## 🎯 Quick Command Templates

### **Add ScreenContainer to Existing Screen**
```
Update [SCREEN_NAME] to use ScreenContainer for consistent background color:

1. Add import: `import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer`
2. Wrap existing content with ScreenContainer { /* existing content */ }
3. Keep existing modifier patterns intact

File: [FILE_PATH]
```

### **Create New Screen with ScreenContainer**
```
Create a new [SCREEN_TYPE] screen following Pasabayan patterns:

1. Use ScreenContainer for consistent background
2. Follow Material 3 design system
3. Import from theme: PasabayanDesignSystem.Colors/Spacing/Typography
4. Include proper @Composable documentation
5. Add preview function with PasabayanTheme

Requirements:
- Java 11 compatibility
- Kotlin 2.0.0 syntax
- Compose BOM 2024.04.01
- Package: com.efthemiosprime.pasabayan.ui.screens.[category]
```

### **Update Tab Content Implementation**
```
Update [TAB_NAME]TabContent to use ScreenContainer:

File: app/src/main/java/com/efthemiosprime/pasabayan/ui/screens/dashboard/TabContents.kt
Function: [TAB_NAME]TabContent (line [LINE_NUMBER])

1. Add ScreenContainer import
2. Wrap LazyColumn/Column with ScreenContainer
3. Maintain existing UserRole logic
4. Keep filter functionality intact
```

## 🛠️ Common Implementation Patterns

### **Standard Screen Structure**
```kotlin
@Composable
fun [ScreenName]Screen(
    modifier: Modifier = Modifier,
    // ViewModels and other parameters
) {
    ScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen content
        }
    }
}
```

### **Tab Content Structure**
```kotlin
@Composable
fun [TabName]TabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    ScreenContainer {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab content based on role
        }
    }
}
```

### **Screen with Loading States**
```kotlin
@Composable
fun [ScreenName]Screen(
    viewModel: [ViewModel] = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    ScreenContainer {
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorDisplay(state.error)
            else -> SuccessContent(state.data)
        }
    }
}
```

## 📋 File-Specific Templates

### **TabContents.kt Updates**
```
Update these functions in TabContents.kt to use ScreenContainer:

High Priority:
- BrowseTabContent (line 106)
- PackagesOrTripsTabContent (line 187)  
- CreateTabContent (line 331)
- ProfileTabContent (line 459)

Pattern: Wrap existing LazyColumn with ScreenContainer
Import: com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
```

### **Home Content Updates**
```
Update Home Content screens to use ScreenContainer:

Files:
- CarrierHomeContent.kt (line 26)
- ShipperHomeContent.kt (line 25)

Pattern: Wrap LazyColumn with ScreenContainer
Maintain: UserRole logic, stats display, and navigation
```

## 🎨 Design System Quick Reference

### **Common Imports**
```kotlin
// Always include these for new screens
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
```

### **Color Usage**
```kotlin
// Use design system colors
PasabayanDesignSystem.Colors.primary
PasabayanDesignSystem.Colors.screenBackground
PasabayanDesignSystem.Colors.surface
PasabayanDesignSystem.Colors.success/warning/error
```

### **Spacing Usage**
```kotlin
// Use design system spacing
PasabayanDesignSystem.Spacing.lg          // 16.dp
PasabayanDesignSystem.Spacing.screenPadding // 16.dp
PasabayanDesignSystem.Spacing.cardPadding   // 16.dp
PasabayanDesignSystem.Spacing.sectionSpacing // 24.dp
```

## 🔧 Development Workflow Prompts

### **Code Review Checklist**
```
Review [SCREEN_NAME] implementation for:

✅ ScreenContainer usage for background consistency
✅ Proper import statements
✅ Java 11 compatibility (no Java 17+ features)
✅ Kotlin 2.0.0 appropriate syntax
✅ Material 3 components usage
✅ PasabayanDesignSystem usage
✅ Proper @Composable documentation
✅ Preview function with PasabayanTheme
✅ Error handling and loading states
✅ Accessibility considerations
```

### **Testing Prompts**
```
Test [SCREEN_NAME] implementation:

1. Background color consistency (#faf8fe)
2. Screen rotation handling
3. Different screen sizes
4. Navigation integration
5. Loading/error states
6. User role switching (if applicable)
7. Dark mode compatibility (future)
```

## 📱 Screen-Specific Templates

### **Dashboard Screens**
```kotlin
// For dashboard-related screens
@Composable
fun [DashboardScreen]() {
    ScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { UserHeaderCard(...) }
            item { StatsGrid(...) }
            item { RecentActivitySection(...) }
        }
    }
}
```

### **Form Screens**
```kotlin
// For form-based screens
@Composable
fun [FormScreen]() {
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form fields
        }
    }
}
```

## 🎯 Priority Implementation Guide

### **Phase 1: High Priority (Tab Contents)**
```
Implement ScreenContainer for tab contents:

1. BrowseTabContent - Browse available trips/packages
2. PackagesOrTripsTabContent - User's packages/trips
3. CreateTabContent - Create new packages/trips
4. ProfileTabContent - User profile display

File: ui/screens/dashboard/TabContents.kt
Estimated: 1-2 hours
```

### **Phase 2: Medium Priority (Home Contents)**
```
Implement ScreenContainer for home contents:

1. CarrierHomeContent - Carrier dashboard home
2. ShipperHomeContent - Shipper dashboard home

Files: ui/screens/dashboard/home/[Carrier|Shipper]HomeContent.kt
Estimated: 30-45 minutes
```

### **Phase 3: Low Priority (Specialized Screens)**
```
Implement ScreenContainer for specialized screens:

1. CarrierTripsScreen - Carrier trip management
2. AuthScreen - Authentication flow
3. DeliveryRequestScreen - Package request form

Estimated: 45-60 minutes
```

## 💡 Best Practices Reminders

### **DO**
- Always use ScreenContainer for new screens
- Follow existing naming conventions
- Test background consistency
- Include proper documentation
- Use design system components

### **DON'T**
- Apply background colors directly
- Use hardcoded colors or spacing
- Skip import statements
- Forget preview functions
- Override system backgrounds unnecessarily

---

*These templates are designed to accelerate development while maintaining consistency with the Pasabayan Android app architecture and design system.* 