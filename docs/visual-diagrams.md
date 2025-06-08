# Visual Diagrams - Pasabayan Android App

**Mermaid Diagrams for Data Flow and Architecture Visualization**

This document contains interactive diagrams that automatically render on GitHub, GitLab, and other platforms supporting Mermaid syntax.

## 📋 Table of Contents

1. [Overall Architecture Flow](#overall-architecture-flow)
2. [Authentication Sequence Diagrams](#authentication-sequence-diagrams)
3. [Data Flow Patterns](#data-flow-patterns)
4. [Component Interaction Flows](#component-interaction-flows)
5. [Error Handling Flow](#error-handling-flow)
6. [Real-time Data Synchronization](#real-time-data-synchronization)
7. [State Management Layers](#state-management-layers)

---

## 📊 Overall Architecture Flow

### Clean Architecture + MVVM Data Flow

```mermaid
graph TD
    A[👤 User Interaction] -->|Tap/Input| B[📱 Jetpack Compose UI]
    B -->|Action Callback| C[🎭 ViewModel StateFlow]
    C -->|Repository Interface| D[🔧 Domain Repository Contract]
    D -->|Implementation| E[💾 Data Repository Implementation]
    E -->|API Call| F[🌐 Retrofit Service]
    F -->|Network Request| G[🔗 Laravel Backend API]
    
    G -->|JSON Response| F
    F -->|Parsed Data| E
    E -->|Domain Model| D
    D -->|UI State| C
    C -->|StateFlow Emission| B
    B -->|Recomposition| H[✨ Updated UI]
    
    I[📦 Data Classes] -.->|Immutable Models| E
    J[🎨 Design System] -.->|Styling| B
    K[🧪 Validation Service] -.->|Pure Functions| C
    
    style A fill:#e1f5fe
    style H fill:#c8e6c9
    style C fill:#f3e5f5
    style E fill:#fff3e0
    style G fill:#ffebee
```

### StateFlow Management Architecture

```mermaid
graph LR
    A[Private MutableStateFlow] -->|Internal Updates| B[Public StateFlow]
    B -->|collectAsState| C[Compose UI]
    
    D[User Action] -->|ViewModel Function| E[Update Logic]
    E -->|Pure Function| F[New State]
    F -->|Emission| A
    
    G[Repository Data] -->|Flow Collection| E
    H[Network Response] -->|Success/Error| E
    I[Local Storage] -->|Cached Data| E
    
    style A fill:#ffcdd2
    style B fill:#c8e6c9
    style C fill:#e3f2fd
    style F fill:#fff3e0
```

---

## 🔐 Authentication Sequence Diagrams

### Google Sign-In Flow

```mermaid
sequenceDiagram
    participant U as User
    participant UI as AuthScreen
    participant VM as AuthViewModel
    participant R as AuthRepository
    participant S as AuthService
    participant API as Laravel API
    participant G as Google OAuth
    
    U->>UI: Tap "Sign in with Google"
    UI->>VM: signInWithGoogle()
    VM->>VM: _isLoading.value = true
    VM->>R: signInWithGoogle()
    R->>S: initiateGoogleSignIn()
    S->>G: Launch Google OAuth
    G-->>S: Authorization code
    S->>API: POST /auth/google/login
    API-->>S: JWT token + User data
    S->>S: Store token securely
    S-->>R: User object
    R-->>VM: Result<User>
    VM->>VM: _currentUser.value = user
    VM->>VM: _isLoading.value = false
    VM-->>UI: StateFlow emission
    UI->>UI: Recompose with success state
    UI->>U: Navigate to Dashboard
```

### Phone Verification Flow

```mermaid
sequenceDiagram
    participant U as User
    participant UI as AuthScreen
    participant VM as AuthViewModel
    participant R as AuthRepository
    participant API as Laravel API
    participant SMS as SMS Service
    
    U->>UI: Enter phone number
    UI->>VM: sendOTP(phoneNumber)
    VM->>R: sendVerificationCode(phone)
    R->>API: POST /phone/send-otp
    API->>SMS: Send SMS with code
    SMS-->>U: SMS with OTP code
    API-->>R: OTP sent confirmation
    
    U->>UI: Enter OTP code
    UI->>VM: verifyOTP(code)
    VM->>R: verifyPhoneNumber(phone, code)
    R->>API: POST /phone/verify-otp
    API-->>R: Verification result
    R-->>VM: Success/Error
    VM->>VM: Update verification status
    VM-->>UI: StateFlow update
    UI->>UI: Show verification success
```

---

## 🔄 Data Flow Patterns

### Unidirectional Data Flow

```mermaid
graph TD
    A[User Action] -->|1. Action| B[ViewModel]
    B -->|2. Repository Call| C[Repository Interface]
    C -->|3. Implementation| D[Repository Impl]
    D -->|4. Network/Storage| E[Data Source]
    
    E -->|5. Raw Data| D
    D -->|6. Domain Model| C
    C -->|7. UI State| B
    B -->|8. StateFlow| F[UI Recomposition]
    
    G[Validation] -.->|Pure Functions| B
    H[Error Handling] -.->|Try/Catch| D
    I[Loading States] -.->|State Management| B
    
    style A fill:#e1f5fe
    style F fill:#c8e6c9
    style B fill:#f3e5f5
    style D fill:#fff3e0
    style E fill:#ffebee
```

### State Types and Lifecycle

```mermaid
stateDiagram-v2
    [*] --> ViewModelState
    [*] --> ComposeState
    [*] --> PersistentState
    
    state ViewModelState {
        [*] --> Loading
        Loading --> Success: Data loaded
        Loading --> Error: API failure
        Success --> Loading: Refresh action
        Error --> Loading: Retry action
        Success --> [*]: ViewModel cleared
        Error --> [*]: ViewModel cleared
    }
    
    state ComposeState {
        [*] --> Initial
        Initial --> Modified: User input
        Modified --> Validated: Validation check
        Validated --> Modified: Continue editing
        Validated --> [*]: Screen dismissed
    }
    
    state PersistentState {
        [*] --> Cached
        Cached --> Updated: New data
        Updated --> Synchronized: Backend sync
        Synchronized --> Cached: Fetch complete
    }
```

---

## 🔗 Component Interaction Flows

### Profile Screen Role Switching

```mermaid
graph TD
    A[ProfileScreen] -->|User toggles role| B[RoleViewModel.switchRole]
    B -->|Repository call| C[UserRepository.updateRole]
    C -->|API request| D[Backend role update]
    D -->|Success response| C
    C -->|Updated user| B
    B -->|StateFlow emission| E[Multiple Observers]
    
    E --> F[DashboardScreen - Shows role-specific content]
    E --> G[NavigationBar - Updates available tabs]
    E --> H[AnalyticsScreen - Shows role metrics]
    E --> I[ProfileScreen - Updates UI]
    
    J[Analytics] -.->|Track event| K[AnalyticsRepository]
    L[Cache] -.->|Store preference| M[DataStore]
    
    style A fill:#e3f2fd
    style B fill:#f3e5f5
    style E fill:#fff3e0
    style F fill:#c8e6c9
    style G fill:#c8e6c9
    style H fill:#c8e6c9
    style I fill:#c8e6c9
```

### Trip Creation Flow

```mermaid
graph TD
    A[CarrierDashboard] -->|Tap Create Trip| B[TripCreationScreen]
    B -->|Form input| C[CarrierViewModel]
    C -->|Validation| D{Form Valid?}
    D -->|No| E[Show validation errors]
    D -->|Yes| F[TripRepository.createTrip]
    F -->|API call| G[Backend saves trip]
    G -->|Success| H[Update local state]
    H --> I[Navigate back to Dashboard]
    I --> J[Dashboard shows new trip]
    
    K[LocationService] -.->|GPS data| B
    L[ValidationService] -.->|Pure functions| C
    M[AnalyticsService] -.->|Track creation| C
    
    style A fill:#e3f2fd
    style J fill:#c8e6c9
    style C fill:#f3e5f5
    style F fill:#fff3e0
    style G fill:#ffebee
```

### Package Request and Booking

```mermaid
sequenceDiagram
    participant S as Shipper
    participant UI as PackageScreen
    participant PVM as PackageViewModel
    participant PR as PackageRepository
    participant TR as TripRepository
    participant C as Carrier
    participant CVM as CarrierViewModel
    
    S->>UI: Create package request
    UI->>PVM: createPackage(details)
    PVM->>PR: savePackageRequest()
    PR->>PR: Find compatible trips
    PR-->>PVM: Compatible trips list
    
    S->>UI: Select preferred trip
    UI->>PVM: bookTrip(tripId, packageId)
    PVM->>TR: createBooking()
    TR->>TR: Notify carrier
    TR-->>CVM: New booking notification
    CVM->>CVM: Update trip bookings
    CVM-->>C: Show booking request
    
    C->>CVM: Accept/Reject booking
    CVM->>TR: updateBookingStatus()
    TR-->>PVM: Booking status update
    PVM-->>UI: Update package status
    UI->>S: Show booking confirmation
```

---

## ⚠️ Error Handling Flow

### Unified Error State Pattern

```mermaid
graph TD
    A[User Action] --> B[ViewModel Function]
    B --> C[Repository Call]
    C --> D{Network Available?}
    D -->|No| E[NetworkException]
    D -->|Yes| F[API Call]
    F --> G{Response OK?}
    G -->|No| H[APIException]
    G -->|Yes| I[Parse Response]
    I --> J{Valid Data?}
    J -->|No| K[ParsingException]
    J -->|Yes| L[Success State]
    
    E --> M[Error State]
    H --> M
    K --> M
    
    M --> N[UI Shows Error]
    N --> O[Retry Option]
    O --> B
    
    L --> P[UI Shows Success]
    
    style A fill:#e1f5fe
    style L fill:#c8e6c9
    style M fill:#ffcdd2
    style P fill:#c8e6c9
    style N fill:#ffcdd2
```

### Error Recovery Strategies

```mermaid
stateDiagram-v2
    [*] --> Normal
    Normal --> NetworkError: Connection lost
    Normal --> ServerError: 5xx response
    Normal --> ValidationError: 4xx response
    
    NetworkError --> Retrying: Auto retry
    NetworkError --> OfflineMode: Manual fallback
    
    ServerError --> Retrying: Exponential backoff
    ServerError --> Fallback: Use cached data
    
    ValidationError --> UserInput: Show validation errors
    ValidationError --> Normal: Corrected input
    
    Retrying --> Normal: Success
    Retrying --> Failed: Max retries
    
    OfflineMode --> Normal: Connection restored
    Fallback --> Normal: Server recovered
    Failed --> UserInput: Manual intervention
    UserInput --> Normal: Issue resolved
```

---

## 📡 Real-time Data Synchronization

### WebSocket Data Flow

```mermaid
sequenceDiagram
    participant A as App
    participant WS as WebSocket Service
    participant VM as ViewModels
    participant UI as Compose UI
    participant S as Server
    
    A->>WS: Connect on app start
    WS->>S: WebSocket connection
    S-->>WS: Connection established
    
    loop Real-time Updates
        S->>WS: Trip status update
        WS->>VM: Emit to observers
        VM->>VM: Update StateFlow
        VM-->>UI: Trigger recomposition
        UI->>UI: Show updated status
    end
    
    A->>WS: Send trip update
    WS->>S: Forward update
    S->>S: Broadcast to relevant users
    S-->>WS: Confirmation
    WS-->>A: Update confirmed
```

### Push Notification Flow

```mermaid
graph TD
    A[Backend Event] -->|Trigger| B[FCM Service]
    B -->|Send notification| C[Device FCM]
    C -->|App in background| D[System notification]
    C -->|App in foreground| E[In-app notification]
    
    D -->|User taps| F[App opens]
    E --> F
    F -->|Navigation| G[Relevant screen]
    G -->|ViewModel call| H[Refresh data]
    H -->|StateFlow update| I[UI updates]
    
    J[NotificationService] -.->|Handle payload| F
    K[DeepLinkService] -.->|Parse intent| G
    
    style A fill:#ffebee
    style I fill:#c8e6c9
    style F fill:#e3f2fd
```

---

## 🏗️ State Management Layers

### Multi-Layer State Architecture

```mermaid
graph TD
    A[🎨 UI State - Compose] -->|remember/mutableStateOf| B[Local Component State]
    A -->|collectAsState| C[🎭 ViewModel State - StateFlow]
    C -->|Repository calls| D[💾 Repository State - Flow]
    D -->|Network/DB calls| E[🌐 Remote State - API]
    D -->|Local caching| F[📱 Local State - Room/DataStore]
    
    G[User Preferences] --> F
    H[Authentication] --> C
    I[Form Data] --> A
    J[Loading States] --> C
    K[Error Messages] --> C
    
    style A fill:#e3f2fd
    style C fill:#f3e5f5
    style D fill:#fff3e0
    style E fill:#ffebee
    style F fill:#e8f5e8
```

### State Lifecycle Management

```mermaid
graph LR
    A[App Launch] --> B[ViewModel Created]
    B --> C[StateFlow Initialized]
    C --> D[UI Observes State]
    D --> E[User Interaction]
    E --> F[State Update]
    F --> G[UI Recomposition]
    G --> D
    
    H[Configuration Change] --> I[ViewModel Survives]
    I --> D
    
    J[App Background] --> K[StateFlow Paused]
    K --> L[App Foreground]
    L --> M[StateFlow Resumed]
    M --> D
    
    N[Activity Destroyed] --> O[ViewModel Cleared]
    O --> P[StateFlow Disposed]
    
    style B fill:#e3f2fd
    style C fill:#f3e5f5
    style G fill:#c8e6c9
    style O fill:#ffcdd2
```

### Performance Optimization Flow

```mermaid
graph TD
    A[Large Data Set] -->|Paging| B[Chunked Loading]
    B -->|LazyColumn| C[Efficient Rendering]
    
    D[Frequent Updates] -->|Debouncing| E[Throttled Emissions]
    E -->|StateFlow| F[Batched UI Updates]
    
    G[Heavy Calculations] -->|Background Thread| H[Coroutine Dispatcher]
    H -->|Results| I[Main Thread Update]
    
    J[Memory Usage] -->|Weak References| K[Automatic Cleanup]
    K -->|GC Friendly| L[Optimized Memory]
    
    style C fill:#c8e6c9
    style F fill:#c8e6c9
    style I fill:#c8e6c9
    style L fill:#c8e6c9
```

---

## 📱 Implementation Examples

### StateFlow with Compose Integration

```mermaid
graph TD
    A["@Composable ProfileScreen"] -->|collectAsState| B[StateFlow<User?>]
    B -->|Automatic recomposition| C[UI Updates]
    
    D[User Action] -->|onClick| E[ViewModel.updateProfile]
    E -->|Repository call| F[API Request]
    F -->|Success| G[StateFlow emission]
    G --> B
    
    H[Configuration Change] -.->|Survives| B
    I[Process Death] -.->|SavedStateHandle| J[State Restoration]
    J --> B
    
    style A fill:#e3f2fd
    style C fill:#c8e6c9
    style B fill:#f3e5f5
    style G fill:#fff3e0
```

---

**Pasabayan Android** - *Visual Architecture Documentation* 🚛📱✨

> These diagrams automatically render on GitHub, GitLab, and other platforms supporting Mermaid syntax, providing interactive visualization of our app's architecture and data flow patterns. 