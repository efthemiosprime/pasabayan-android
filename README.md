# 📱 Pasabayan Android

A modern logistics and delivery platform for the Philippines, built with Jetpack Compose and following clean architecture principles with a comprehensive global design system.

## 📋 Project Overview

**Pasabayan** is an Android application that connects shippers and carriers in the Philippines, providing a seamless platform for package delivery and logistics management. The app mirrors iOS functionality while leveraging Android's unique capabilities and implements a unified design system for consistent user experience.

- **Package**: `com.efthemiosprime.pasabayan`
- **Type**: Android Kotlin/Compose Application
- **Build System**: Gradle with Kotlin DSL (.kts)
- **Architecture**: Clean Architecture with MVVM pattern
- **Design System**: Global PasabayanDesignSystem with unified card standards

## 🏗️ Technical Specifications

### Android Configuration
- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 33
- **Application ID**: `com.efthemiosprime.pasabayan`
- **Version Code**: 1
- **Version Name**: 1.0

### Java/Kotlin Versions
- **Java Compatibility**: VERSION_11 (source & target)
- **Kotlin JVM Target**: "11"
- **Kotlin Version**: 2.0.0
- **Kotlin Code Style**: official

### Key Dependencies
- **Android Gradle Plugin (AGP)**: 8.8.1
- **Kotlin Android Plugin**: 2.0.0
- **Compose BOM**: 2024.04.01
- **AndroidX Core KTX**: 1.16.0
- **Lifecycle Runtime KTX**: 2.9.0
- **Activity Compose**: 1.10.1
- **Navigation Compose**: 2.8.4
- **Retrofit**: 2.11.0
- **OkHttp**: 4.12.0
- **Coil Compose**: 2.7.0

## 🎨 Global Design System & UI Architecture

### PasabayanDesignSystem Implementation
The app implements a comprehensive global design system ensuring consistency across all components:

#### **Global Card Standards**
- **Elevation**: 4dp (consistent across all cards)
- **Background**: White (Color.White)
- **Corner Radius**: 12dp
- **Padding**: 16dp
- **Child Elevation**: 0dp (prevents nested shadows)

#### **Unified Card Components**
```kotlin
// Primary card component - single source of truth
PCardStandard(modifier = Modifier) {
    // Content with automatic global standards
}

// Compact variant with reduced padding
PCardStandardCompact(modifier = Modifier) {
    // Content with global standards, less padding
}

// Legacy functions automatically use global standards
PCard() // Forces global standards regardless of parameters
PCardCompact() // Forces global standards regardless of parameters
```

#### **Specialized Card Types**
- **MetricDisplayCard**: Analytics metrics with optional icons
- **StatusCard**: Status indicators with color-coded backgrounds
- **ActionCard**: Cards with integrated buttons
- **StatCard/StatItem**: Statistics display (elevated/flat variants)

### Jetpack Compose Implementation
- **Material 3**: Full implementation with dynamic theming
- **Theme System**: Proper color schemes (light/dark) with Android 12+ dynamic colors
- **Typography**: Custom typography definitions with semantic styles
- **Navigation**: Compose Navigation with proper state management
- **Spacing System**: 4dp grid-based spacing (xs=4dp, sm=8dp, md=12dp, lg=16dp, etc.)

### Screen Architecture
- **Authentication Flow**: Complete auth screen with Google Sign-In integration
- **Dashboard**: Role-based UI (Shipper vs Carrier) with tabbed navigation
- **Analytics**: Comprehensive analytics screens with consistent card styling
- **Profile**: Enhanced profile screen with global card standards and minimal spacing
- **Component Structure**: Organized UI components directory with shared/specialized components

## 🏛️ Application Architecture

### Clean Architecture Pattern
```
📁 app/src/main/java/com/efthemiosprime/pasabayan/
├── MainActivity.kt                    # Main activity entry point
├── PasabayanApplication.kt           # Application class
│
├── 📁 presentation/                   # Presentation Layer
│   ├── viewmodel/                    # ViewModels with StateFlow
│   │   ├── AuthViewModel.kt          # Authentication state management
│   │   ├── RoleViewModel.kt          # Role switching logic
│   │   ├── CarrierViewModel.kt       # Carrier-specific operations
│   │   ├── ShipperViewModel.kt       # Shipper-specific operations
│   │   ├── PackageViewModel.kt       # Package management
│   │   ├── AnalyticsViewModel.kt     # Analytics data processing
│   │   └── DashboardViewModel.kt     # Dashboard state management
│   └── common/                       # Presentation common utilities
│
├── 📁 ui/                            # UI Layer
│   ├── screens/                      # Feature screens
│   │   ├── auth/                     # Authentication screens
│   │   ├── dashboard/                # Role-based dashboard screens
│   │   ├── analytics/                # Analytics and insights screens
│   │   ├── profile/                  # Profile management screens
│   │   ├── carrier/                  # Carrier-specific screens
│   │   └── packagerequest/           # Package request screens
│   │
│   ├── components/                   # Feature-specific UI components
│   │   ├── cards/                    # Card components (StatCard, etc.)
│   │   ├── analytics/                # Analytics-specific components
│   │   ├── buttons/                  # Button components
│   │   ├── packages/                 # Package-related components
│   │   ├── role/                     # Role switching components
│   │   ├── status/                   # Status display components
│   │   ├── DashboardComponents.kt    # Dashboard component collection
│   │   ├── TripCard.kt              # Trip display component
│   │   ├── BadgeComposable.kt       # Badge/verification components
│   │   └── TripStatusBadge.kt       # Trip status indicators
│   │
│   ├── shared/                       # Shared/reusable components
│   │   ├── cards/                    # Global card system
│   │   │   ├── PCard.kt             # Base card component (global standards)
│   │   │   ├── MetricDisplayCard.kt  # Analytics metrics display
│   │   │   ├── ActionCard.kt        # Cards with integrated actions
│   │   │   └── StatusCard.kt        # Status indicator cards
│   │   ├── PButton.kt               # Global button component
│   │   ├── MetricCard.kt            # Legacy metric card
│   │   ├── SharedCard.kt            # Legacy shared card
│   │   ├── SharedButton.kt          # Legacy shared button
│   │   ├── EmptyStateComposable.kt  # Empty state displays
│   │   └── EmptyStateView.kt        # Simplified empty states
│   │
│   ├── theme/                        # Design system & theming
│   │   └── PasabayanDesignSystem     # Global design standards
│   ├── navigation/                   # Navigation logic & routing
│   ├── previews/                     # Compose previews
│   ├── utils/                        # UI utilities & helpers
│   └── common/                       # Common UI components
│
├── 📁 domain/                        # Domain Layer (Business Logic)
│   └── repository/                   # Repository interfaces/contracts
│
├── 📁 data/                          # Data Layer
│   ├── model/                        # Data models & entities
│   │   ├── analytics/                # Analytics-specific models
│   │   ├── User.kt                   # User entity with roles
│   │   ├── Trip.kt                   # Trip/delivery entity
│   │   ├── Booking.kt                # Booking entity
│   │   ├── PackageRequest.kt         # Package request entity
│   │   ├── CarrierStatus.kt          # Carrier status model
│   │   └── AuthResponse.kt           # Authentication response
│   ├── repository/                   # Repository implementations
│   ├── service/                      # API services & data sources
│   └── common/                       # Data layer utilities
│
└── 📁 di/                            # Dependency Injection (Hilt)
    └── [DI modules ready for implementation]
```

## 🔄 Data Flow & State Management Architecture

> 📖 **For comprehensive data flow documentation with visual diagrams and detailed examples, see [DATA_FLOW_ARCHITECTURE.md](./DATA_FLOW_ARCHITECTURE.md)**

### Overview: Unidirectional Data Flow
The Pasabayan Android app follows a **unidirectional data flow** pattern, ensuring predictable state management and clear separation of concerns across all layers.

### Core Principles
- **Actions flow UP**: User interactions travel from UI → ViewModel → Repository → Service
- **State flows DOWN**: Data travels from Service → Repository → ViewModel → UI
- **Single Source of Truth**: Each piece of state has one authoritative source
- **Reactive Updates**: UI automatically recomposes when state changes

### Data Flow Layers Explained

#### **1. 📱 UI Layer (Compose Screens & Components)**
**Role**: Displays data and captures user interactions
**State Type**: `@Composable` state and `remember` for local UI state

```kotlin
@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    // Observe state from ViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    // Local UI state
    var showingEditDialog by remember { mutableStateOf(false) }
    
    // Action flows UP to ViewModel
    PButton(
        text = "Sign Out",
        onClick = { authViewModel.signOut() } // Action UP
    )
    
    // State flows DOWN from ViewModel
    if (isLoading) {
        CircularProgressIndicator() // UI reacts to state DOWN
    }
}
```

#### **2. 🧠 Presentation Layer (ViewModels)**
**Role**: Manages UI state and orchestrates business operations
**State Type**: `StateFlow` and `MutableStateFlow` for reactive state management

```kotlin
class AuthViewModel : ViewModel() {
    // Private mutable state
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isLoading = MutableStateFlow(false)
    
    // Public read-only state (flows DOWN to UI)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Actions (called from UI, flow UP)
    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true // Update UI state
            try {
                val result = authRepository.signInWithGoogle() // Call repository
                _currentUser.value = result.getOrNull() // Update state
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

#### **3. 🔧 Domain Layer (Repository Interfaces)**
**Role**: Defines contracts for data operations without implementation details

```kotlin
interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
}
```

#### **4. 💾 Data Layer (Repository Implementations)**
**Role**: Implements domain contracts and coordinates between multiple data sources

```kotlin
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userPreferences: UserPreferences
) : AuthRepository {
    
    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val googleAccount = authService.signInWithGoogle() // Call service
            val user = googleAccount.toUser() // Transform to domain model
            userPreferences.saveUser(user) // Cache locally
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### **5. 🌐 Services Layer (Data Sources)**
**Role**: Handles actual data fetching from APIs, databases, or local storage

```kotlin
class AuthService {
    suspend fun signInWithGoogle(): GoogleSignInAccount {
        // Handle Google Sign-In API calls
        // Return raw data from external source
    }
    
    suspend fun signOut() {
        // Handle sign-out with Google/Firebase
    }
}
```

#### **6. 📊 Models Layer (Data Classes)**
**Role**: Defines data structures and transformation logic

```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<UserRole>,
    val verificationLevel: String
)

// Extension function for data transformation
fun GoogleSignInAccount.toUser(): User {
    return User(
        id = this.id ?: "",
        name = this.displayName ?: "",
        email = this.email ?: "",
        roles = listOf(UserRole.SHIPPER), // Default role
        verificationLevel = "unverified"
    )
}
```

## 🔄 Data Flow & State Management Architecture

### Overview: Unidirectional Data Flow
The Pasabayan Android app follows a **unidirectional data flow** pattern, ensuring predictable state management and clear separation of concerns across all layers.

### Core Principles
- **Actions flow UP**: User interactions travel from UI → ViewModel → Repository → Service
- **State flows DOWN**: Data travels from Service → Repository → ViewModel → UI
- **Single Source of Truth**: Each piece of state has one authoritative source
- **Reactive Updates**: UI automatically recomposes when state changes

### Data Flow Layers Explained

#### **1. 📱 UI Layer (Compose Screens & Components)**
**Role**: Displays data and captures user interactions
**State Type**: `@Composable` state and `remember` for local UI state

```kotlin
@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    // Observe state from ViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    // Local UI state
    var showingEditDialog by remember { mutableStateOf(false) }
    
    // Action flows UP to ViewModel
    PButton(
        text = "Sign Out",
        onClick = { authViewModel.signOut() } // Action UP
    )
    
    // State flows DOWN from ViewModel
    if (isLoading) {
        CircularProgressIndicator() // UI reacts to state DOWN
    }
}
```

#### **2. 🧠 Presentation Layer (ViewModels)**
**Role**: Manages UI state and orchestrates business operations
**State Type**: `StateFlow` and `MutableStateFlow` for reactive state management

```kotlin
class AuthViewModel : ViewModel() {
    // Private mutable state
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isLoading = MutableStateFlow(false)
    
    // Public read-only state (flows DOWN to UI)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Actions (called from UI, flow UP)
    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true // Update UI state
            try {
                val result = authRepository.signInWithGoogle() // Call repository
                _currentUser.value = result.getOrNull() // Update state
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

#### **3. 🔧 Domain Layer (Repository Interfaces)**
**Role**: Defines contracts for data operations without implementation details

```kotlin
interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
}
```

#### **4. 💾 Data Layer (Repository Implementations)**
**Role**: Implements domain contracts and coordinates between multiple data sources

```kotlin
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userPreferences: UserPreferences
) : AuthRepository {
    
    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val googleAccount = authService.signInWithGoogle() // Call service
            val user = googleAccount.toUser() // Transform to domain model
            userPreferences.saveUser(user) // Cache locally
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### **5. 🌐 Services Layer (Data Sources)**
**Role**: Handles actual data fetching from APIs, databases, or local storage

```kotlin
class AuthService {
    suspend fun signInWithGoogle(): GoogleSignInAccount {
        // Handle Google Sign-In API calls
        // Return raw data from external source
    }
    
    suspend fun signOut() {
        // Handle sign-out with Google/Firebase
    }
}
```

#### **6. 📊 Models Layer (Data Classes)**
**Role**: Defines data structures and transformation logic

```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<UserRole>,
    val verificationLevel: String
)

// Extension function for data transformation
fun GoogleSignInAccount.toUser(): User {
    return User(
        id = this.id ?: "",
        name = this.displayName ?: "",
        email = this.email ?: "",
        roles = listOf(UserRole.SHIPPER), // Default role
        verificationLevel = "unverified"
    )
}
```

### Real-World Data Flow Examples

#### **Example 1: User Authentication Flow**
```
1. User taps "Sign In" button (UI)
   ↓ Action UP
2. ProfileScreen calls authViewModel.signInWithGoogle() (ViewModel)
   ↓ Business Operation
3. AuthViewModel calls authRepository.signInWithGoogle() (Repository Interface)
   ↓ Data Request
4. AuthRepositoryImpl calls authService.signInWithGoogle() (Service)
   ↓ API Call
5. AuthService interacts with Google API, returns GoogleSignInAccount (Model)
   ↓ Data Transformation
6. Repository transforms to User model, caches locally (Data Layer)
   ↓ Domain Model
7. Repository returns Result<User> to ViewModel (Domain)
   ↓ Processed Data
8. ViewModel updates _currentUser StateFlow (Presentation)
   ↓ State Flow DOWN
9. UI automatically recomposes, shows user profile (UI)
```

#### **Example 2: Package Creation Flow**
```
1. User fills package form and taps "Create" (UI)
   ↓ Form Data UP
2. CreatePackageScreen calls packageViewModel.createPackage(packageData) (ViewModel)
   ↓ Validation & Processing
3. PackageViewModel validates data, calls packageRepository.createPackage() (Repository)
   ↓ Business Logic
4. PackageRepository calls packageService.createPackage() (Service)
   ↓ API Request
5. PackageService sends POST request to backend API (External API)
   ↓ Server Response
6. API returns created Package with ID (Model)
   ↓ Data Processing
7. Repository updates local cache, returns Package (Data Layer)
   ↓ State Update
8. ViewModel updates _packages StateFlow with new package (Presentation)
   ↓ UI Update
9. UI shows success message and updated package list (UI)
```

### Error Handling & Loading States

#### **Unified State Pattern**
```kotlin
// Common state wrapper for async operations
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}

// ViewModel usage
class PackageViewModel : ViewModel() {
    private val _packagesState = MutableStateFlow<UiState<List<Package>>>(UiState.Loading)
    val packagesState: StateFlow<UiState<List<Package>>> = _packagesState.asStateFlow()
    
    fun loadPackages() {
        viewModelScope.launch {
            _packagesState.value = UiState.Loading
            try {
                val packages = packageRepository.getPackages()
                _packagesState.value = UiState.Success(packages)
            } catch (e: Exception) {
                _packagesState.value = UiState.Error(e)
            }
        }
    }
}
```

### Data Persistence Strategy

#### **Multi-Layer Caching**
```
🌐 Remote API (Single source of truth)
    ↓ Network calls
💽 Local Database (Room - planned)
    ↓ Cache queries  
📂 In-Memory Cache (Repository layer)
    ↓ StateFlow updates
🧠 ViewModel State (UI state)
    ↓ Compose state
📱 UI Layer (Displayed data)
```

### Key Benefits
1. **Predictable State Flow**: Always flows in one direction
2. **Testability**: Each layer can be tested independently
3. **Maintainability**: Clear separation of concerns
4. **Performance**: Automatic recomposition only when necessary
5. **Error Handling**: Centralized error management

### State Management Patterns

#### **StateFlow Pattern (ViewModel ↔ UI)**
```kotlin
// ViewModel
class PackageViewModel : ViewModel() {
    private val _packages = MutableStateFlow<List<Package>>(emptyList())
    val packages: StateFlow<List<Package>> = _packages.asStateFlow()
    
    init {
        loadPackages() // Load data on initialization
    }
}

// UI Screen
@Composable
fun PackagesScreen(packageViewModel: PackageViewModel) {
    val packages by packageViewModel.packages.collectAsState()
    
    LazyColumn {
        items(packages) { package ->
            PackageCard(package = package)
        }
    }
}
```

#### **Compose State Pattern (Local UI State)**
```kotlin
@Composable
fun SearchablePackageList() {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Local state doesn't need to go through ViewModel
    SearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        expanded = isSearchExpanded,
        onExpandedChange = { isSearchExpanded = it }
    )
}
```

### Real-World Data Flow Examples

#### **Example 1: User Authentication Flow**
```
1. User taps "Sign In" button (UI)
   ↓ Action UP
2. ProfileScreen calls authViewModel.signInWithGoogle() (ViewModel)
   ↓ Business Operation
3. AuthViewModel calls authRepository.signInWithGoogle() (Repository Interface)
   ↓ Data Request
4. AuthRepositoryImpl calls authService.signInWithGoogle() (Service)
   ↓ API Call
5. AuthService interacts with Google API, returns GoogleSignInAccount (Model)
   ↓ Data Transformation
6. Repository transforms to User model, caches locally (Data Layer)
   ↓ Domain Model
7. Repository returns Result<User> to ViewModel (Domain)
   ↓ Processed Data
8. ViewModel updates _currentUser StateFlow (Presentation)
   ↓ State Flow DOWN
9. UI automatically recomposes, shows user profile (UI)
```

#### **Example 2: Package Creation Flow**
```
1. User fills package form and taps "Create" (UI)
   ↓ Form Data UP
2. CreatePackageScreen calls packageViewModel.createPackage(packageData) (ViewModel)
   ↓ Validation & Processing
3. PackageViewModel validates data, calls packageRepository.createPackage() (Repository)
   ↓ Business Logic
4. PackageRepository calls packageService.createPackage() (Service)
   ↓ API Request
5. PackageService sends POST request to backend API (External API)
   ↓ Server Response
6. API returns created Package with ID (Model)
   ↓ Data Processing
7. Repository updates local cache, returns Package (Data Layer)
   ↓ State Update
8. ViewModel updates _packages StateFlow with new package (Presentation)
   ↓ UI Update
9. UI shows success message and updated package list (UI)
```

#### **Example 3: Role Switching Flow**
```
1. User toggles role switch (UI)
   ↓ Action UP
2. ProfileScreen calls roleViewModel.switchRole(newRole) (ViewModel)
   ↓ State Management
3. RoleViewModel updates _currentRole StateFlow (Presentation)
   ↓ Reactive Update
4. All role-dependent screens automatically recompose (UI)
   ↓ Side Effects
5. Dashboard content changes based on new role (UI)
6. Navigation updates available tabs (Navigation)
```

### Error Handling & Loading States

#### **Unified State Pattern**
```kotlin
// Common state wrapper for async operations
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}

// ViewModel usage
class PackageViewModel : ViewModel() {
    private val _packagesState = MutableStateFlow<UiState<List<Package>>>(UiState.Loading)
    val packagesState: StateFlow<UiState<List<Package>>> = _packagesState.asStateFlow()
    
    fun loadPackages() {
        viewModelScope.launch {
            _packagesState.value = UiState.Loading
            try {
                val packages = packageRepository.getPackages()
                _packagesState.value = UiState.Success(packages)
            } catch (e: Exception) {
                _packagesState.value = UiState.Error(e)
            }
        }
    }
}

// UI usage
@Composable
fun PackagesScreen(viewModel: PackageViewModel) {
    val state by viewModel.packagesState.collectAsState()
    
    when (state) {
        is UiState.Loading -> CircularProgressIndicator()
        is UiState.Success -> PackageList(packages = state.data)
        is UiState.Error -> ErrorMessage(error = state.exception)
    }
}
```

### Data Persistence & Caching Strategy

#### **Multi-Layer Caching**
```
🌐 Remote API (Single source of truth)
    ↓ Network calls
💽 Local Database (Room - planned)
    ↓ Cache queries  
📂 In-Memory Cache (Repository layer)
    ↓ StateFlow updates
🧠 ViewModel State (UI state)
    ↓ Compose state
📱 UI Layer (Displayed data)
```

### Key Benefits of This Architecture

1. **Predictable State Flow**: Always flows in one direction
2. **Testability**: Each layer can be tested independently
3. **Maintainability**: Clear separation of concerns
4. **Scalability**: Easy to add new features without breaking existing code
5. **Performance**: Automatic recomposition only when necessary
6. **Error Handling**: Centralized error management
7. **Offline Support**: Ready for Room database integration

### State Synchronization Patterns

#### **Cross-ViewModel Communication**
```kotlin
// Shared repository ensures data consistency
class UserRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Both AuthViewModel and ProfileViewModel observe this
}

// Multiple ViewModels can observe and update shared state
class AuthViewModel(private val userRepository: UserRepository) {
    val currentUser = userRepository.currentUser
}

class ProfileViewModel(private val userRepository: UserRepository) {
    val currentUser = userRepository.currentUser
}
```

This architecture ensures that the Pasabayan Android app maintains consistent, predictable data flow while providing excellent user experience through reactive UI updates and proper error handling.

## 📊 Enhanced Features & Data Models

### Core Data Models
- **User Model**: Complete with roles, verification, ratings
- **Booking System**: Full booking lifecycle management
- **Trip Management**: Carrier trip tracking and management
- **Package Requests**: Shipper package request system
- **Analytics Models**: Performance insights, cost optimization, carrier preferences

### Key Features Implemented

#### **1. Global Design System**
- Unified card standards across all screens
- Consistent elevation (4dp) and white backgrounds
- Flat child components to prevent nested shadows
- Minimal spacing between UI elements (1dp between menu items)

#### **2. Enhanced Profile Screen**
- **ProfileMenuItem**: White background, black text/icons, consistent styling
- **Role-specific menus**: Carrier vs Shipper menu items
- **Statistics display**: Flat StatItem components inside elevated parent cards
- **Minimal spacing**: 1dp between menu items for clean, tight layout
- **Global card standards**: All profile cards use PCardStandard

#### **3. Analytics Implementation**
- **Performance Insights**: Delivery metrics, earnings tracking
- **Cost Optimization**: Budget alerts, spending recommendations
- **Preferred Carriers**: Carrier selection analytics
- **Metric Display**: Consistent metric cards with optional icons
- **Flat nested components**: Analytics items use Surface instead of Card to prevent shadows

#### **4. Dashboard Enhancements**
- **Role-based interfaces**: Separate Shipper and Carrier dashboards
- **Statistics grids**: CarrierStatsGrid, ShipperStatsGrid with flat StatItem components
- **Status cards**: Consistent status display with global standards
- **Empty states**: Proper empty state handling with consistent styling

#### **5. Authentication System**
- Google Sign-In with state management
- Role selection and switching
- Profile verification system
- Persistent authentication state

#### **6. Navigation System**
- Bottom tab navigation with "More" overflow
- Role-aware navigation
- Proper state preservation
- Deep linking support

### Business Logic
- **Dual Role Support**: Users can be both Shippers and Carriers with seamless switching
- **Verification System**: Phone verification and profile completion
- **Rating System**: User ratings and reviews
- **Notification Settings**: Comprehensive notification preferences
- **Analytics Tracking**: Performance metrics and insights

## 🔐 Authentication & Security

### Firebase Integration
- **Google Services**: Properly configured with project credentials
- **Authentication**: Google Sign-In implementation ready
- **Security**: Proper package name and certificate hash configuration

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17 (for Gradle) with compilation targeting JDK 11
- Android SDK 35
- Git

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd pasabayan-android
   ```

2. **Configure Java Version**
   - Ensure JDK 17 is installed for Gradle
   - Code compilation targets Java 11 (configured in build files)

3. **Firebase Configuration**
   - The project includes `google-services.json`
   - Verify Firebase project configuration matches your setup

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run the app**
   - Open in Android Studio
   - Select device/emulator
   - Run the app

### Environment Variables
Configure the following in `local.properties` if needed:
```properties
# SDK path (auto-configured by Android Studio)
sdk.dir=/path/to/android/sdk

# Optional: Custom Java home for Gradle
# org.gradle.java.home=/path/to/jdk-17
```

## 🛠️ Development Guidelines

### Code Style
- **Kotlin**: Official Kotlin code style
- **Architecture**: Follow clean architecture principles
- **Compose**: Use Material 3 components exclusively
- **State**: Prefer StateFlow over LiveData
- **Design System**: Always use PasabayanDesignSystem components

### Global Design System Rules
- **Cards**: Always use `PCardStandard` or `PCardStandardCompact`
- **Elevation**: 4dp for main cards, 0dp for child components
- **Background**: White backgrounds for all cards
- **Spacing**: Use PasabayanDesignSystem.Spacing values
- **Typography**: Use semantic typography from design system
- **Colors**: Use PasabayanDesignSystem.Colors for consistency

### Version Management
- **All versions** are managed in `