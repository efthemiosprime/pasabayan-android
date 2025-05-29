# Pasabayan Android App

A delivery service Android application built with Jetpack Compose, mirroring the functionality of the iOS version.

## Features

- **Google Authentication**: Sign in with Google account
- **Demo Login**: Test the app without Google setup
- **Delivery Management**: Create, view, and manage delivery requests
- **Real-time Updates**: Track delivery status changes
- **Modern UI**: Built with Jetpack Compose and Material 3

## Architecture

- **MVVM Pattern**: ViewModels manage UI state
- **Repository Pattern**: Centralized data management
- **Retrofit**: HTTP client for API communication
- **DataStore**: Secure local storage for user preferences
- **Kotlin Coroutines**: Asynchronous programming
- **Jetpack Compose**: Modern declarative UI

## Project Structure

```
app/src/main/java/com/efthemiosprime/pasabayan/
├── data/
│   ├── model/          # Data models (User, DeliveryRequest, etc.)
│   └── service/        # API and authentication services
├── ui/
│   ├── screen/         # Compose screens
│   ├── viewmodel/      # ViewModels for state management
│   └── theme/          # App theming
└── MainActivity.kt     # Main entry point
```

## Setup Instructions

### 1. Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 31 or higher
- Java 11 or higher

### 2. Google Authentication Setup

1. **Create a Google Cloud Project**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one

2. **Enable Google Sign-In API**:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Google Sign-In API" and enable it

3. **Create OAuth 2.0 Credentials**:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth 2.0 Client IDs"
   - Select "Android" as application type
   - Add your package name: `com.efthemiosprime.pasabayan`
   - Add your SHA-1 certificate fingerprint

4. **Get SHA-1 Fingerprint**:
   ```bash
   # For debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For release keystore
   keytool -list -v -keystore /path/to/your/keystore -alias your-alias
   ```

5. **Download google-services.json**:
   - Download the configuration file from Google Cloud Console
   - Replace the placeholder `app/google-services.json` with your actual file

6. **Update AuthService.kt**:
   - Replace `"YOUR_GOOGLE_CLIENT_ID"` with your actual client ID from google-services.json

### 3. Backend API Setup

1. **Start the Laravel API**:
   ```bash
   cd ../pasabayan-api
   php artisan serve --host=0.0.0.0 --port=8001
   ```

2. **Update API Base URL** (if needed):
   - In `ApiService.kt`, update `baseUrl` if your API runs on a different host/port
   - For emulator: `http://10.0.2.2:8001/api/`
   - For physical device: `http://YOUR_LOCAL_IP:8001/api/`

### 4. Build and Run

1. **Open in Android Studio**:
   ```bash
   cd pasabayan-android
   # Open the project in Android Studio
   ```

2. **Sync Project**:
   - Click "Sync Now" when prompted
   - Wait for Gradle sync to complete

3. **Run the App**:
   - Select a device/emulator
   - Click the "Run" button or press Ctrl+R

## API Endpoints

The app connects to the Laravel backend API with the following endpoints:

- `POST /auth/google/login` - Google authentication
- `GET /auth/me` - Get current user
- `POST /auth/logout` - Sign out
- `GET /delivery-requests` - Get delivery requests
- `POST /delivery-requests` - Create delivery request
- `PUT /delivery-requests/{id}/status` - Update delivery status
- `POST /delivery-requests/{id}/accept` - Accept delivery request
- `GET /profile` - Get user profile

## Dependencies

### Core Android
- Jetpack Compose BOM 2024.04.01
- Activity Compose 1.10.1
- Navigation Compose 2.8.4
- Lifecycle ViewModel Compose 2.8.7

### Networking
- Retrofit 2.11.0
- OkHttp 4.12.0
- Moshi 1.15.1

### Authentication
- Google Play Services Auth 21.2.0

### Storage
- DataStore Preferences 1.1.1

### Serialization
- Kotlinx Serialization JSON 1.6.3

### Coroutines
- Kotlinx Coroutines Android 1.8.1

## Usage

### Demo Login
For quick testing without Google setup:
1. Launch the app
2. Tap "Demo Login"
3. You'll be signed in as a demo user

### Google Sign-In
1. Launch the app
2. Tap "Continue with Google"
3. Select your Google account
4. Grant permissions
5. You'll be redirected to the dashboard

### Creating Delivery Requests
1. From dashboard, tap "Create Delivery"
2. Fill in pickup and delivery details
3. Set delivery fee
4. Submit the request

### Managing Deliveries
1. View all requests in the "Deliveries" tab
2. Accept pending requests from other users
3. Update status as you progress through delivery

## Troubleshooting

### Google Sign-In Issues
- Ensure google-services.json is properly configured
- Check SHA-1 fingerprint matches your keystore
- Verify package name matches exactly
- Make sure Google Sign-In API is enabled

### API Connection Issues
- Check if backend API is running
- Verify API base URL is correct
- For physical devices, use your computer's IP address
- Check network permissions in AndroidManifest.xml

### Build Issues
- Clean and rebuild project: Build > Clean Project, then Build > Rebuild Project
- Invalidate caches: File > Invalidate Caches and Restart
- Check Gradle sync completed successfully

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of the Pasabayan delivery service platform. 