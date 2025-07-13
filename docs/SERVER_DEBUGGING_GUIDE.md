# Server Debugging Guide for Package API

## 🚨 **Current Issue**
The Android app is receiving HTTP 500 errors with HTML content instead of JSON from the `/api/packages` endpoint.

## 🔍 **Step-by-Step Debugging**

### 1. **Test Server Connectivity**
```bash
# Test if server is running
curl -I http://127.0.0.1:8000

# Test the specific endpoint
curl -X GET http://127.0.0.1:8000/api/packages \
  -H "Accept: application/json" \
  -H "Content-Type: application/json"
```

### 2. **Test with Authentication**
```bash
# If you have a token, test with authentication
curl -X GET http://127.0.0.1:8000/api/packages \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. **Check Server Routes**
```bash
# List all routes in your Laravel app
php artisan route:list | grep packages

# Should show something like:
# GET|HEAD  api/packages ............... packages.index
# POST      api/packages ............... packages.store
# GET|HEAD  api/packages/{id} .......... packages.show
# PUT|PATCH api/packages/{id} .......... packages.update
# DELETE    api/packages/{id} .......... packages.destroy
```

### 4. **Check Laravel Logs**
```bash
# Monitor Laravel logs in real-time
tail -f storage/logs/laravel.log

# Or check the latest log entries
tail -50 storage/logs/laravel.log
```

## 🛠️ **Common Server Issues & Solutions**

### **Issue 1: Route Not Found**
**Symptoms:** 404 errors, Laravel welcome page
**Solutions:**
```php
// In routes/api.php, ensure you have:
Route::middleware('auth:sanctum')->group(function () {
    Route::apiResource('packages', PackageController::class);
});

// Or without auth for testing:
Route::apiResource('packages', PackageController::class);
```

### **Issue 2: Controller Missing**
**Symptoms:** 500 errors, class not found
**Solutions:**
```bash
# Create the controller if it doesn't exist
php artisan make:controller Api/PackageController --api --resource
```

### **Issue 3: Database Connection**
**Symptoms:** Database connection errors
**Solutions:**
```bash
# Check database connection
php artisan migrate:status

# Run migrations if needed
php artisan migrate
```

### **Issue 4: Missing Authentication**
**Symptoms:** 401 Unauthorized errors
**Solutions:**
```php
// In your PackageController, add proper authentication
public function index(Request $request)
{
    $user = $request->user();
    if (!$user) {
        return response()->json(['error' => 'Unauthorized'], 401);
    }
    
    // Return packages for the user
    return response()->json([
        'success' => true,
        'data' => $user->packages,
        'message' => 'Packages fetched successfully'
    ]);
}
```

## 📱 **Android App Debugging**

### **Check Network Security (if needed)**
The app should already have network security configured, but verify:
```xml
<!-- In app/src/main/res/xml/network_security_config.xml -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
    <domain includeSubdomains="true">127.0.0.1</domain>
    <domain includeSubdomains="true">localhost</domain>
</domain-config>
```

### **Test Different Base URLs**
Try switching between these in APIService:
```kotlin
// For emulator
const val BASE_URL_LOCAL = "http://10.0.2.2:8000/api"

// For physical device on same network
const val BASE_URL_LOCAL = "http://192.168.1.XXX:8000/api"  // Your computer's IP
```

## 🧪 **Quick Test Commands**

### **1. Test Basic Server Response**
```bash
# Simple test
curl -v http://127.0.0.1:8000/api/packages

# Check what you get back - should be JSON, not HTML
```

### **2. Test with Proper Headers**
```bash
curl -X GET http://127.0.0.1:8000/api/packages \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -v
```

### **3. Test Laravel Health**
```bash
# Test if Laravel is working
curl http://127.0.0.1:8000

# Should return Laravel welcome page or your app's homepage
```

## 🔧 **Server-Side Fixes**

### **1. Basic Package Controller**
```php
<?php
// app/Http/Controllers/Api/PackageController.php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Package;

class PackageController extends Controller
{
    public function index(Request $request)
    {
        try {
            // For testing, return empty array
            return response()->json([
                'success' => true,
                'data' => [],
                'message' => 'Packages fetched successfully'
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Error fetching packages: ' . $e->getMessage()
            ], 500);
        }
    }
}
```

### **2. Update API Routes**
```php
<?php
// routes/api.php

use App\Http\Controllers\Api\PackageController;

Route::middleware('auth:sanctum')->group(function () {
    Route::apiResource('packages', PackageController::class);
});

// Or for testing without auth:
Route::apiResource('packages', PackageController::class);
```

### **3. Enable CORS (if needed)**
```php
// In config/cors.php
'paths' => ['api/*', 'sanctum/csrf-cookie'],
'allowed_methods' => ['*'],
'allowed_origins' => ['*'], // For development only
'allowed_headers' => ['*'],
```

## 📊 **Expected Response Format**

Your server should return JSON like this:
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "title": "Package 1",
            "description": "Description",
            "weight": 5.0,
            "status": "PENDING",
            "pickup_location": "Location A",
            "delivery_location": "Location B",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-01T00:00:00Z"
        }
    ],
    "message": "Packages fetched successfully"
}
```

## 🏃‍♂️ **Quick Fix Steps**

1. **Check if server is running:** `curl -I http://127.0.0.1:8000`
2. **Test the endpoint:** `curl -X GET http://127.0.0.1:8000/api/packages`
3. **Check Laravel logs:** `tail -f storage/logs/laravel.log`
4. **Verify routes exist:** `php artisan route:list | grep packages`
5. **Test with Postman/Insomnia** to isolate server issues

## 🚀 **Next Steps**

1. Run the curl commands above to see what your server returns
2. Check your Laravel logs while making requests
3. Verify your routes and controller exist
4. Test with the updated Android app to see better error messages

Once you identify the server issue, the Android app should work correctly! 