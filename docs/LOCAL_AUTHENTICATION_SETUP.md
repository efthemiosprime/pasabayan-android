# Local Authentication Setup Guide

## 🎯 **Issue Fixed**
The 401 Unauthorized error was caused by URL mismatch:
- **AuthService** was using production server (`https://api.pasabayan.com`)
- **PackageRepositoryImpl** was using local server (`http://10.0.2.2:8000`)

✅ **Solution Applied**: Both services now use the same local server URL.

## 🚀 **Quick Setup Options**

### **Option 1: Use Mock Authentication (Fastest)**

If you want to test the package functionality immediately:

1. **Enable mock login in your app**:
   ```kotlin
   // In your authentication screen or MainActivity
   val authService = AuthService.getInstance(this)
   lifecycleScope.launch {
       val result = authService.mockLogin()
       if (result.isSuccess) {
           Log.d("Auth", "Mock login successful")
           // Navigate to dashboard
       }
   }
   ```

2. **Create a simple test endpoint on your server**:
   ```php
   // routes/api.php
   Route::get('/packages', function () {
       return response()->json([
           'success' => true,
           'data' => [
               [
                   'id' => 1,
                   'title' => 'Test Package',
                   'description' => 'A test package for development',
                   'weight' => 5.0,
                   'status' => 'PENDING',
                   'pickup_location' => 'Manila',
                   'delivery_location' => 'Cebu',
                   'created_at' => now(),
                   'updated_at' => now()
               ]
           ],
           'message' => 'Packages fetched successfully'
       ]);
   });
   ```

### **Option 2: Setup Full Local Authentication**

If you want to test the complete authentication flow:

1. **Setup Laravel Sanctum on your local server**:
   ```bash
   # Install Sanctum
   composer require laravel/sanctum
   
   # Publish configuration
   php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
   
   # Run migrations
   php artisan migrate
   ```

2. **Create authentication routes**:
   ```php
   // routes/api.php
   use App\Http\Controllers\AuthController;
   
   Route::post('auth/google/login', [AuthController::class, 'googleLogin']);
   Route::post('auth/facebook/login', [AuthController::class, 'facebookLogin']);
   
   Route::middleware('auth:sanctum')->group(function () {
       Route::get('auth/me', [AuthController::class, 'me']);
       Route::post('auth/logout', [AuthController::class, 'logout']);
       Route::apiResource('packages', PackageController::class);
   });
   ```

3. **Create AuthController**:
   ```php
   <?php
   
   namespace App\Http\Controllers;
   
   use Illuminate\Http\Request;
   use App\Models\User;
   
   class AuthController extends Controller
   {
       public function googleLogin(Request $request)
       {
           // For testing, create a mock user
           $user = User::firstOrCreate([
               'email' => 'test@example.com'
           ], [
               'name' => 'Test User',
               'password' => bcrypt('password'),
               'provider' => 'google'
           ]);
           
           $token = $user->createToken('auth_token')->plainTextToken;
           
           return response()->json([
               'success' => true,
               'data' => [
                   'token' => $token,
                   'user' => $user,
                   'tokenType' => 'Bearer'
               ],
               'message' => 'Login successful'
           ]);
       }
       
       public function me(Request $request)
       {
           return response()->json([
               'success' => true,
               'data' => [
                   'user' => $request->user()
               ]
           ]);
       }
       
       public function logout(Request $request)
       {
           $request->user()->currentAccessToken()->delete();
           
           return response()->json([
               'message' => 'Logged out successfully'
           ]);
       }
   }
   ```

## 🧪 **Testing Steps**

### **1. Test Server Connectivity**
```bash
# Test if server is running
curl -I http://127.0.0.1:8000

# Should return 200 OK
```

### **2. Test Authentication Endpoint**
```bash
# Test Google login endpoint
curl -X POST http://127.0.0.1:8000/api/auth/google/login \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "accessToken": "test_token",
    "deviceInfo": {
      "platform": "android",
      "osVersion": "13",
      "appVersion": "1.0"
    }
  }'
```

### **3. Test Package Endpoint with Token**
```bash
# First, get a token from the auth endpoint above
# Then test packages endpoint
curl -X GET http://127.0.0.1:8000/api/packages \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Accept: application/json"
```

### **4. Test Android App**
1. Build and run the updated app
2. Check logs for improved debugging information
3. Try logging in (use mock login if available)
4. Navigate to packages screen
5. Check if packages load successfully

## 🔧 **Debugging Commands**

### **Check Authentication Status**
```kotlin
// In your Android app
lifecycleScope.launch {
    val result = PackageRepositoryImpl.testAuthentication(this@MainActivity)
    result.onSuccess { 
        Log.d("Auth", "Authentication test passed")
    }.onFailure { 
        Log.e("Auth", "Authentication test failed: ${it.message}")
    }
}
```

### **Check Laravel Logs**
```bash
# Monitor Laravel logs
tail -f storage/logs/laravel.log

# Check for authentication errors
grep -i "auth\|token\|unauthorized" storage/logs/laravel.log
```

### **Check Database**
```bash
# Check if users table exists
php artisan tinker
> User::count()

# Check if personal_access_tokens table exists (for Sanctum)
> DB::table('personal_access_tokens')->count()
```

## 🎯 **Expected Results**

After setup, you should see:

1. **Authentication logs**:
   ```
   🧪 Testing authentication status
   ✅ Authentication token found
   🎫 Token length: 40
   🎫 Token starts with: 1|abcdef...
   ```

2. **Package request logs**:
   ```
   🔐 Added Authorization header to package request
   📊 Response Debug Info:
   🔢 Status Code: 200
   📋 Content-Type: application/json
   ```

3. **Successful package loading in the app**

## 🚨 **Common Issues & Solutions**

### **Issue: Still getting 401 errors**
**Solution**: 
- Verify both AuthService and PackageRepositoryImpl use the same base URL
- Check if your token is valid by testing with curl
- Ensure Sanctum is properly configured

### **Issue: CORS errors**
**Solution**:
```php
// config/cors.php
'paths' => ['api/*', 'sanctum/csrf-cookie'],
'allowed_methods' => ['*'],
'allowed_origins' => ['*'], // For development only
'allowed_headers' => ['*'],
```

### **Issue: Database connection errors**
**Solution**:
```bash
# Check database configuration
php artisan config:show database
php artisan migrate:status
```

## 📱 **Next Steps**

1. **Choose your setup approach** (Mock vs Full Authentication)
2. **Run the test commands** to verify server setup
3. **Test the updated Android app**
4. **Check the logs** for detailed debugging information

The app should now work correctly with your local server! 🎉 