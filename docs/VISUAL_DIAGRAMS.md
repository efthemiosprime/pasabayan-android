# 📊 Pasabayan Android - Visual Architecture Diagrams

This file contains all the visual diagrams for the Pasabayan Android app's data flow and state management architecture. These diagrams are created using Mermaid and will render as images on platforms that support Mermaid (GitHub, GitLab, etc.).

## 📋 Table of Contents
- [Overall Architecture Flow](#overall-architecture-flow)
- [User Authentication Sequence](#user-authentication-sequence)
- [Data Flow Patterns](#data-flow-patterns)
- [Component Interaction Flow](#component-interaction-flow)
- [Error Handling Flow](#error-handling-flow)

---

## Overall Architecture Flow

This diagram shows the complete data flow between all layers of the Clean Architecture pattern:

```mermaid
graph TD
    A["📱 UI Layer<br/>(Compose Screens)"] 
    B["🧠 Presentation Layer<br/>(ViewModels)"]
    C["🔧 Domain Layer<br/>(Repository Interfaces)"]
    D["💾 Data Layer<br/>(Repository Implementations)"]
    E["🌐 Services<br/>(API/Local)"]
    F["📊 Models<br/>(Data Classes)"]
    
    A -->|"User Actions<br/>(onClick, input)"| B
    B -->|"UI State<br/>(StateFlow/State)"| A
    B -->|"Business Operations<br/>(suspend functions)"| C
    C -->|"Data Requests<br/>(interface calls)"| D
    D -->|"API Calls<br/>(network/local)"| E
    E -->|"Raw Data<br/>(JSON/DB)"| F
    F -->|"Parsed Models<br/>(User, Trip, Package)"| D
    D -->|"Domain Models<br/>(business objects)"| C
    C -->|"Processed Data<br/>(business logic applied)"| B

    subgraph "State Management"
        G["StateFlow"] 
        H["Compose State"]
        I["remember/mutableStateOf"]
    end
    
    B --> G
    G --> A
    A --> H
    H --> I
```

**Key Points:**
- **Actions flow UP**: User interactions travel upward through the layers
- **State flows DOWN**: Data and state updates flow downward to the UI
- **StateFlow**: Used for cross-layer communication and persistent state
- **Compose State**: Used for local UI state that doesn't need persistence

---

## User Authentication Sequence

This sequence diagram shows a complete user authentication flow from UI interaction to state update:

```mermaid
sequenceDiagram
    participant UI as "📱 ProfileScreen"
    participant VM as "🧠 AuthViewModel"
    participant Repo as "🔧 AuthRepository"
    participant Service as "🌐 AuthService"
    participant Model as "📊 User Model"
    
    Note over UI,Model: User Login Flow Example
    
    UI->>VM: onSignInClick()
    activate VM
    VM->>VM: _isLoading.value = true
    VM->>UI: isLoading StateFlow emits true
    UI->>UI: Show loading indicator
    
    VM->>Repo: signInWithGoogle()
    activate Repo
    Repo->>Service: googleSignIn()
    activate Service
    Service->>Service: Authenticate with Google
    Service-->>Model: GoogleSignInAccount
    Model-->>Service: User(id, name, email)
    Service-->>Repo: Result<User>
    deactivate Service
    Repo-->>VM: Result<User>
    deactivate Repo
    
    VM->>VM: _currentUser.value = user
    VM->>VM: _isLoading.value = false
    VM->>UI: currentUser StateFlow emits User
    VM->>UI: isLoading StateFlow emits false
    deactivate VM
    
    UI->>UI: Navigate to Dashboard
    Note over UI: UI recomposes automatically
```

**Key Points:**
- **Reactive UI**: Loading states automatically trigger UI updates
- **Error Handling**: Each layer can handle and transform errors appropriately
- **State Synchronization**: Multiple StateFlows can be updated simultaneously
- **Automatic Recomposition**: Compose UI automatically updates when state changes

---

## Data Flow Patterns

This diagram illustrates the different data flow patterns and state types used throughout the app:

```mermaid
graph LR
    subgraph "Data Flow Patterns"
        A["🔄 Unidirectional<br/>Data Flow"]
        B["📤 Action Up"]
        C["📥 State Down"] 
    end
    
    subgraph "State Types"
        D["🌊 StateFlow<br/>(ViewModel)"]
        E["🎭 Compose State<br/>(UI Layer)"]
        F["💾 Persistent State<br/>(DataStore/DB)"]
    end
    
    subgraph "Data Sources"
        G["🌐 Remote API<br/>(Retrofit)"]
        H["💽 Local DB<br/>(Room - planned)"]
        I["📂 Preferences<br/>(DataStore)"]
        J["🔐 Auth<br/>(Firebase)"]
    end
    
    A --> B
    A --> C
    B --> D
    C --> E
    D --> F
    
    G --> H
    H --> I
    I --> J
```

**Key Points:**
- **Unidirectional Flow**: Ensures predictable state changes
- **StateFlow**: For cross-component communication and configuration-change survival
- **Compose State**: For local UI state that doesn't need persistence
- **Multiple Data Sources**: API, local database, preferences, and authentication

---

## Component Interaction Flow

This diagram shows how different components interact in real-world scenarios:

```mermaid
graph TD
    subgraph "Profile Screen Flow"
        A["User taps ProfileMenuItem"]
        B["ProfileScreen captures onClick"]
        C["Navigate to specific screen"]
    end
    
    subgraph "Role Switching Flow"
        D["User toggles RoleSwitcher"]
        E["RoleViewModel.switchRole()"]
        F["Update _currentRole StateFlow"]
        G["All observing screens recompose"]
    end
    
    subgraph "Package Creation Flow"
        H["User fills form"]
        I["PackageViewModel.createPackage()"]
        J["Repository validates & saves"]
        K["Update _packages StateFlow"]
        L["UI shows success & updates list"]
    end
    
    A --> B
    B --> C
    
    D --> E
    E --> F
    F --> G
    
    H --> I
    I --> J
    J --> K
    K --> L
```

**Key Points:**
- **User Actions**: Always start from UI components
- **Cross-Screen Updates**: Role changes affect multiple screens simultaneously
- **Form Handling**: Validation and processing happens in ViewModels
- **Real-time Updates**: External data changes automatically update relevant screens

---

## Error Handling Flow

```mermaid
graph TD
    A["User Action"] --> B["ViewModel"]
    B --> C["Repository"]
    C --> D["Service"]
    D --> E{"API Call Success?"}
    
    E -->|"✅ Success"| F["Transform Data"]
    E -->|"❌ Error"| G["Handle Error"]
    
    F --> H["Update StateFlow"]
    G --> I["Error StateFlow"]
    
    H --> J["UI Success State"]
    I --> K["UI Error State"]
    
    K --> L["Show Error Message"]
    K --> M["Retry Option"]
    K --> N["Fallback Content"]
```

---

## Real-time Data Synchronization

```mermaid
sequenceDiagram
    participant WS as "WebSocket/Push"
    participant Service as "TripService"
    participant Repo as "TripRepository"
    participant VM as "CarrierViewModel"
    participant UI as "TripTrackingScreen"
    
    Note over WS,UI: Real-time Trip Status Updates
    
    WS->>Service: New trip status
    Service->>Service: Transform to domain model
    Service->>Repo: Emit updated Trip
    Repo->>Repo: Cache locally
    Repo->>VM: Flow emits new Trip
    VM->>VM: Update _currentTrip StateFlow
    VM->>UI: StateFlow emits to UI
    UI->>UI: Automatic recomposition
    UI->>UI: Show updated status
```

---

## State Management Layers

```mermaid
graph TB
    subgraph "UI Layer"
        A["@Composable Functions"]
        B["remember { mutableStateOf() }"]
        C["collectAsState()"]
    end
    
    subgraph "ViewModel Layer"
        D["MutableStateFlow (private)"]
        E["StateFlow (public)"]
        F["viewModelScope"]
    end
    
    subgraph "Repository Layer"
        G["Flow<Data>"]
        H["suspend functions"]
        I["Result<T>"]
    end
    
    subgraph "Service Layer"
        J["API Calls"]
        K["Local Storage"]
        L["External Services"]
    end
    
    A --> B
    A --> C
    C --> E
    E --> D
    D --> F
    F --> H
    H --> G
    G --> I
    I --> J
    I --> K
    I --> L
```

---

## Notes for Developers

### Viewing These Diagrams
- **GitHub/GitLab**: These Mermaid diagrams will automatically render as images
- **Local Development**: Use Mermaid Live Editor (https://mermaid.live) to view
- **IDE Support**: Many IDEs have Mermaid preview plugins

### Integration with Documentation
These diagrams complement:
- `DATA_FLOW_ARCHITECTURE.md` - Detailed explanation
- `README.md` - High-level overview
- Actual source code - The ground truth

---

*These diagrams represent the current state of the Pasabayan Android app architecture* 