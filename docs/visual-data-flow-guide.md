# Visual Data Flow Guide - Pasabayan Android App

**Interactive Diagrams and Practical Implementation Examples**

## 📋 Table of Contents

1. [Architecture Overview Diagrams](#architecture-overview-diagrams)
2. [Complete Data Flow Visualizations](#complete-data-flow-visualizations)  
3. [State Management Patterns](#state-management-patterns)
4. [Component Communication Examples](#component-communication-examples)
5. [Real-world Implementation Scenarios](#real-world-implementation-scenarios)
6. [Performance and Memory Management](#performance-and-memory-management)

---

## 🏗️ Architecture Overview Diagrams

### High-Level Clean Architecture + MVVM Pattern

Our architecture follows a strict unidirectional data flow pattern with five distinct layers:

```
📱 UI LAYER (Jetpack Compose)
    ↕️ StateFlow Observation, Action Callbacks
🎭 PRESENTATION LAYER (ViewModels)  
    ↕️ Repository Interfaces, StateFlow Management
🔧 DOMAIN LAYER (Repository Contracts)
    ↕️ Business Logic Abstraction
💾 DATA LAYER (Repository Implementations)
    ↕️ API Calls, Local Storage, Side Effects  
📦 MODELS LAYER (Data Classes)
    ↕️ Immutable Structures, Kotlinx Serialization
```

### Data Flow Direction and Principles

**Core Principles:**
- **🔒 Immutable State**: All state changes create new instances with `copy()`
- **⚡ Side Effect Isolation**: Network calls and storage isolated to repositories
- **🧪 Pure Functions**: Business logic functions have no side effects
- **📊 Single Source of Truth**: Each piece of state has one authoritative StateFlow
- **🔄 Unidirectional Flow**: Data flows predictably through architecture layers

---

## 🔄 Complete Data Flow Visualizations

### Authentication Flow - Step by Step

**Phase 1: User Input and Validation**
```
👤 User Types Email
    ↓
📱 AuthScreen.EmailField
    ↓ onValueChange callback
🎭 AuthViewModel.updateEmail()
    ↓ calls pure function
✅ ValidationService.validateEmail()
    ↓ returns ValidationResult
🎭 updateState { state.copy(emailValidation = result) }
    ↓ StateFlow emission
📱 AuthScreen re-composes with validation feedback
```

**Phase 2: API Call and Response**
```
👤 User Taps "Sign In"
    ↓
📱 AuthScreen PButton onClick
    ↓ coroutine launch
🎭 AuthViewModel.signInWithGoogle()
    ↓ sets loading state
🎭 _isLoading.value = true
    ↓ calls repository
💾 AuthRepositoryImpl.signInWithGoogle()
    ↓ network request
🌐 ApiService.post("/auth/google/login")
    ↓ HTTP request
🌐 Laravel Backend API
    ↓ JSON response
📦 User data class creation
    ↓ secure storage
🔐 EncryptedSharedPreferences.storeToken()
    ↓ state update
🎭 _currentUser.value = user
    ↓ navigation
📱 AuthScreen → DashboardScreen
```

### Trip Creation Flow - Carrier Workflow

**Step-by-Step Breakdown:**

1. **Initialization**
   ```kotlin
   // Carrier opens trip creation
   CarrierDashboard → TripCreationScreen
   TripCreationScreen.LaunchedEffect → CarrierViewModel.initializeTripCreation()
   ```

2. **Location Selection**
   ```kotlin
   LocationPicker.onLocationSelected → CarrierViewModel.updateStartLocation()
   updateState { currentState.copy(startLocation = location) }
   ```

3. **Form Validation**
   ```kotlin
   ValidationService.validateTrip(tripData) → ValidationResult
   updateState { currentState.copy(validationErrors = errors) }
   ```

4. **Trip Submission**
   ```kotlin
   TripRepositoryImpl.createTrip() → ApiService.post("/trips") → Laravel API
   Parse Trip data class → _trips.value = currentTrips + newTrip
   ```

### Package Request and Booking Flow

**Smart Matching System:**

```
📦 Shipper creates package request
    ↓ validation
✅ Package details validated
    ↓ repository call
💾 PackageRepositoryImpl.createRequest()
    ↓ API call
🌐 POST /package-requests
    ↓ smart matching
🧠 Trip compatibility algorithm
    ↓ results
📊 Compatible trips returned
    ↓ selection
👤 Shipper selects preferred trip
    ↓ booking
🤝 BookingRepositoryImpl.createBooking()
    ↓ notifications
📱 Both carrier and shipper StateFlow updated
```

---

## 🎯 State Management Patterns

### Immutable State Structure

```kotlin
// AuthViewModel State - Completely Immutable
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, ValidationResult> = emptyMap(),
    val formData: FormData = FormData()
) {
    data class FormData(
        val email: String = "",
        val password: String = "",
        val rememberMe: Boolean = false
    )
}
```

### StateFlow Updates

```kotlin
// Pure state update function - no side effects
private fun updateState(transform: (AuthUiState) -> AuthUiState) {
    _uiState.value = transform(_uiState.value)
}

// Example functional updates
fun updateEmail(email: String) {
    val validation = ValidationService.validateEmail(email)
    updateState { currentState ->
        currentState.copy(
            formData = currentState.formData.copy(email = email),
            validationErrors = currentState.validationErrors + ("email" to validation),
            errorMessage = null // Clear previous errors
        )
    }
}
```

### StateFlow States

**Authentication States:**
1. **Idle** - Waiting for user input
2. **Validating** - Checking form inputs
3. **Loading** - API call in progress
4. **Success** - User authenticated
5. **Error** - Handle failure gracefully

**Trip Management States:**
1. **Browsing** - Viewing available trips
2. **Creating** - Building new trip
3. **Submitting** - Saving to backend
4. **Active** - Trip is live
5. **Completed** - Trip finished

---

## 🔗 Component Communication Examples

### Parent-Child Communication Pattern

```kotlin
// MARK: - Parent Component (State Owner)
@Composable
fun TripCreationScreen(
    carrierViewModel: CarrierViewModel = hiltViewModel()
) {
    val uiState by carrierViewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PasabayanDesignSystem.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.medium)
    ) {
        // Pass state down as props, receive callbacks
        LocationPicker(
            selectedLocation = uiState.tripCreation.startLocation,
            onLocationSelected = carrierViewModel::updateStartLocation // Method reference
        )
        
        CapacitySelector(
            capacity = uiState.tripCreation.capacity,
            maxCapacity = uiState.vehicleInfo.maxCapacity,
            onCapacityChanged = carrierViewModel::updateCapacity
        )
        
        PButton(
            text = "Create Trip",
            style = PButtonStyle.Primary,
            isLoading = uiState.isCreatingTrip,
            onClick = { 
                // Launch coroutine in composition scope
                carrierViewModel.createTrip()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// MARK: - Child Component (Pure, Stateless)
@Composable
fun LocationPicker(
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Void,
    modifier: Modifier = Modifier
) {
    // Props from parent - no internal state
    var showingLocationDialog by remember { mutableStateOf(false) }
    
    PCardStandard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showingLocationDialog = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pickup Location",
                    style = PasabayanDesignSystem.Typography.labelMedium
                )
                Text(
                    text = selectedLocation?.displayName ?: "Select location",
                    style = PasabayanDesignSystem.Typography.bodyLarge,
                    color = if (selectedLocation != null) {
                        PasabayanDesignSystem.Colors.onSurface
                    } else {
                        PasabayanDesignSystem.Colors.onSurfaceVariant
                    }
                )
            }
            
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Select location"
            )
        }
    }
    
    // Location selection dialog
    if (showingLocationDialog) {
        LocationSelectionDialog(
            onLocationSelected = { location ->
                onLocationSelected(location) // Callback to parent
                showingLocationDialog = false
            },
            onDismiss = { showingLocationDialog = false }
        )
    }
}
```

### ViewModel to ViewModel Communication

```kotlin
// Cross-ViewModel communication through shared repository
class RoleViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Update in repository (single source of truth)
                val updatedUser = userRepository.updateUserRole(newRole)
                
                // Emit to all observers
                _currentRole.value = newRole
                _currentUser.value = updatedUser
                
                // Repository change triggers other ViewModels to update
                // AuthViewModel, DashboardViewModel, etc. will observe these changes
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to switch role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// AuthViewModel observes the same repository
class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    init {
        // Observe repository changes from other ViewModels
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                // UI automatically updates when StateFlow changes
            }
        }
    }
}
```

---

## 💼 Real-world Implementation Scenarios

### Scenario 1: User Profile Update with Cross-Screen Sync

```kotlin
// Step 1: User updates profile in ProfileScreen
ProfileScreen → ProfileViewModel.updateUserName("John Doe")

// Step 2: ViewModel calls repository
ProfileViewModel → UserRepositoryImpl.updateUser(updatedUser)

// Step 3: Repository makes API call and updates local storage
UserRepositoryImpl → ApiService.put("/profile") + LocalUserStorage.save()

// Step 4: Repository emits updated user to all observers
UserRepositoryImpl.currentUser.emit(updatedUser)

// Step 5: All screens automatically update
AuthViewModel.currentUser ← updatedUser (Header updates)
DashboardViewModel.user ← updatedUser (Welcome message updates)
ProfileViewModel.user ← updatedUser (Profile details update)

// Step 6: UI recomposes automatically
ProfileScreen recomposes with new name
DashboardScreen header recomposes
Navigation drawer recomposes
```

### Scenario 2: Real-time Trip Status Updates

```kotlin
// WebSocket connection in repository
class TripRepositoryImpl {
    private val _activeTrips = MutableStateFlow<List<Trip>>(emptyList())
    val activeTrips: StateFlow<List<Trip>> = _activeTrips.asStateFlow()
    
    init {
        // WebSocket listener for real-time updates
        webSocketService.onTripStatusUpdate { updatedTrip ->
            _activeTrips.value = _activeTrips.value.map { trip ->
                if (trip.id == updatedTrip.id) updatedTrip else trip
            }
        }
    }
}

// Multiple ViewModels observe the same data
CarrierViewModel.activeTrips ← TripRepository.activeTrips
ShipperViewModel.bookings ← TripRepository.activeTrips.filter { it.bookings }
AnalyticsViewModel.tripData ← TripRepository.activeTrips

// UI automatically updates when trip status changes
CarrierDashboard → Shows updated trip status badges
ShipperDashboard → Shows updated delivery progress
AnalyticsScreen → Updates real-time metrics
```

### Scenario 3: Form Validation with Pure Functions

```kotlin
// Pure validation functions
object ValidationService {
    fun validateTripData(tripData: TripCreationData): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (tripData.startLocation == null) {
            errors.add("Start location is required")
        }
        
        if (tripData.endLocation == null) {
            errors.add("End location is required")
        }
        
        if (tripData.capacity <= 0) {
            errors.add("Capacity must be greater than 0")
        }
        
        if (tripData.price <= 0.0) {
            errors.add("Price must be greater than 0")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

// ViewModel uses pure functions
fun validateAndCreateTrip() {
    val validationResult = ValidationService.validateTripData(currentTripData)
    
    updateState { currentState ->
        currentState.copy(
            validationErrors = when (validationResult) {
                is ValidationResult.Success -> emptyMap()
                is ValidationResult.Error -> validationResult.errors.associateWith { it }
            }
        )
    }
    
    if (validationResult is ValidationResult.Success) {
        // Proceed with trip creation
        createTrip()
    }
}
```

---

## ⚡ Performance and Memory Management

### StateFlow Optimization

```kotlin
// ✅ DO: Use conflated StateFlow for UI state
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// ✅ DO: Combine multiple StateFlows efficiently
val combinedState = combine(
    authRepository.currentUser,
    roleRepository.currentRole,
    tripRepository.activeTrips
) { user, role, trips ->
    DashboardUiState(user, role, trips)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardUiState()
)

// ❌ DON'T: Create new StateFlow in composables
@Composable
fun MyScreen() {
    // This creates a new StateFlow on every recomposition
    val data = remember { MutableStateFlow(Data()) } // DON'T DO THIS
}
```

### Compose Performance

```kotlin
// ✅ DO: Use stable parameters and keys
@Composable
fun TripList(
    trips: List<Trip>,
    onTripClick: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = trips,
            key = { trip -> trip.id } // Stable key for recomposition optimization
        ) { trip ->
            TripCard(
                trip = trip,
                onClick = { onTripClick(trip) }
            )
        }
    }
}

// ✅ DO: Use derivedStateOf for expensive calculations
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val rawData by viewModel.analyticsData.collectAsState()
    
    // Expensive calculation only runs when rawData changes
    val processedMetrics by remember {
        derivedStateOf {
            AnalyticsProcessor.calculateMetrics(rawData)
        }
    }
}
```

### Memory Management

```kotlin
// ✅ DO: Proper coroutine scope management
class TripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {
    
    init {
        // Use viewModelScope for automatic cancellation
        viewModelScope.launch {
            tripRepository.getActiveTrips().collect { trips ->
                _activeTrips.value = trips
            }
        }
    }
    
    // ViewModelScope automatically cancelled when ViewModel is cleared
}

// ✅ DO: Use WhileSubscribed for StateFlow
val uiState = combine(
    userRepository.currentUser,
    tripRepository.activeTrips
) { user, trips ->
    UiState(user, trips)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000), // Stop after 5 seconds of no subscribers
    initialValue = UiState()
)
```

---

## 📚 Best Practices Summary

### State Management
- **Use StateFlow** for ViewModel state that UI observes
- **Keep state immutable** with data classes and `copy()`
- **Single source of truth** - one StateFlow per piece of state
- **Pure update functions** - no side effects in state updates

### Component Design
- **Stateless composables** receive props and emit callbacks
- **Stable parameters** for better recomposition performance
- **Single responsibility** - each composable has one clear purpose
- **Consistent spacing** using design system constants

### Architecture Layers
- **UI Layer**: Only handles display and user interactions
- **ViewModel Layer**: Manages UI state and coordinates business operations
- **Repository Layer**: Single source of truth for data operations
- **Network Layer**: Isolated API calls and data transformation

### Performance
- **derivedStateOf** for expensive calculations
- **LazyColumn keys** for list performance
- **WhileSubscribed** StateFlow strategy
- **viewModelScope** for automatic coroutine management

---

**Pasabayan Android** - *Clean Architecture with Unidirectional Data Flow* 🚛📱✨ 