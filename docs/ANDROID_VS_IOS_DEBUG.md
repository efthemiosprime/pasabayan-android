# Android vs iOS Authentication Debug Guide

## 🎯 **Situation**
- ✅ **iOS app** works perfectly with your local server
- ❌ **Android app** gets 401 Unauthorized with the same server
- 🌐 Both should use the same local server endpoint

## 🔍 **Debug Process**

### **Step 1: Add Debug Call to Your App**

Add this to your Android app (temporarily) to trigger comprehensive debugging:

```kotlin
// In your ShipperPackagesScreen or wherever packages are loaded
// Add this button or call this method

val packageViewModel = // your PackageViewModel instance
packageViewModel.debugAndroidVsIOS()
```

### **Step 2: Check Debug Logs**

The debug method will output comprehensive information like:

```
🔍 === ANDROID vs iOS DEBUG COMPARISON ===
📱 Since iOS works with the same server, investigating Android-specific issues

🔍 === AUTHENTICATION DEBUG INFO ===
📱 Platform: Android
🌐 Base URL: http://10.0.2.2:8000/api

🎫 Token Status:
   ❌ No token found

👤 User Status:
   ❌ No user data found

🔐 Authentication State:
   📊 isAuthenticated: false
   ⏳ isLoading: false
   ❌ error: null

🌐 API Test:
   📡 Testing connectivity to: http://10.0.2.2:8000/api
   🔓 No token available for authenticated test
=== END DEBUG INFO ===

🧪 Testing authentication flow:
❌ No token found, trying mock login...
✅ Mock login successful, loading packages...
```

### **Step 3: Compare iOS vs Android**

**Key Differences to Check:**

1. **URL Differences**:
   - iOS might use: `http://192.168.1.XXX:8000` (actual IP)
   - Android uses: `http://10.0.2.2:8000` (emulator mapping)

2. **Authentication State**:
   - iOS: Logged in with valid token
   - Android: No token (fresh install or cleared data)

3. **Network Configuration**:
   - iOS: Direct network access
   - Android: May need network security config for HTTP

## 🔧 **Common Solutions**

### **Solution 1: Authentication State Mismatch**

If debug shows "No token found":

```kotlin
// Try logging in first (use your existing auth flow)
// Or use mock login for testing
val authService = AuthService.getInstance(context)
val result = authService.mockLogin()
```

### **Solution 2: URL Mismatch**

If iOS uses your computer's actual IP, update Android to match:

```kotlin
// In APIService.kt, change:
const val BASE_URL_LOCAL = "http://10.0.2.2:8000/api"

// To your computer's IP (find with: ipconfig getifaddr en0):
const val BASE_URL_LOCAL = "http://192.168.1.XXX:8000/api"
```

### **Solution 3: Network Security**

Ensure HTTP is allowed for local development:

```xml
<!-- In app/src/main/res/xml/network_security_config.xml -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
    <domain includeSubdomains="true">192.168.1.0/24</domain>
    <domain includeSubdomains="true">localhost</domain>
</domain-config>
```

## 🧪 **Quick Tests**

### **Test 1: Server Accessibility**
```bash
# From your computer (what iOS uses):
curl -X GET http://127.0.0.1:8000/api/packages

# Test with your IP (what Android might need):
curl -X GET http://192.168.1.XXX:8000/api/packages
```

### **Test 2: Authentication**
```bash
# Test if authentication is required:
curl -X GET http://127.0.0.1:8000/api/packages \
  -H "Accept: application/json"

# Should return either:
# - 401 Unauthorized (auth required)
# - 200 OK with packages (no auth required)
```

## 🎯 **Expected Results**

After debugging, you should identify:

1. **Root Cause**: Exactly why Android fails vs iOS
2. **Solution**: Specific fix needed (auth, URL, network, etc.)
3. **Verification**: Both platforms work the same way

## 🚀 **Quick Fix Checklist**

- [ ] Run `debugAndroidVsIOS()` method
- [ ] Check if token exists in Android
- [ ] Verify URL matches between iOS/Android
- [ ] Test server accessibility from Android
- [ ] Compare authentication states
- [ ] Apply identified fix
- [ ] Verify packages load successfully

This systematic approach will pinpoint exactly why iOS works but Android doesn't! 🎯 