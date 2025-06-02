<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;
use Laravel\Socialite\Facades\Socialite;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;
use Exception;
use Illuminate\Support\Facades\Http;
use Google\Client as GoogleClient;

class AuthController extends Controller
{
    /**
     * @OA\Post(
     *     path="/api/auth/{provider}/login",
     *     tags={"Authentication"},
     *     summary="Initiate OAuth login and return JSON",
     *     description="Alternative OAuth flow that returns JSON response",
     *     @OA\Parameter(
     *         name="provider",
     *         in="path",
     *         required=true,
     *         description="OAuth provider (facebook or google)",
     *         @OA\Schema(type="string", enum={"facebook", "google"})
     *     ),
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             @OA\Property(property="access_token", type="string", description="OAuth access token from frontend"),
     *             @OA\Property(property="auth_code", type="string", description="OAuth authorization code"),
     *             @OA\Property(property="idToken", type="string", description="Google ID token (JWT)")
     *         )
     *     ),
     *     @OA\Response(
     *         response=200,
     *         description="Authentication successful",
     *         @OA\JsonContent(
     *             @OA\Property(property="success", type="boolean", example=true),
     *             @OA\Property(property="message", type="string", example="Authentication successful"),
     *             @OA\Property(property="data", type="object")
     *         )
     *     )
     * )
     * 
     * Handle OAuth login with access token
     */
    public function loginWithToken(string $provider, Request $request): JsonResponse
    {
        try {
            if (!in_array($provider, ['facebook', 'google'])) {
                return response()->json([
                    'success' => false,
                    'message' => 'Invalid OAuth provider'
                ], 400);
            }

            $accessToken = $request->input('access_token');
            $authCode = $request->input('auth_code');
            $idToken = $request->input('idToken');
            
            // Smart detection: Check if "access_token" is actually a Google ID token (JWT)
            if ($accessToken && $provider === 'google' && $this->isJwtToken($accessToken)) {
                $idToken = $accessToken; // Treat it as an ID token
                $accessToken = null;     // Clear the access token
            }
            
            if (!$accessToken && !$authCode && !$idToken) {
                return response()->json([
                    'success' => false,
                    'message' => 'Access token, auth code, or ID token is required'
                ], 400);
            }

            $socialUser = null;

            // Handle Google ID token flow (Android)
            if ($idToken && $provider === 'google') {
                $socialUser = $this->verifyGoogleIdToken($idToken);
                
                if (!$socialUser) {
                    return response()->json([
                        'success' => false,
                        'message' => 'Invalid Google ID token'
                    ], 400);
                }
            }
            // Handle auth code flow (Android)
            elseif ($authCode && $provider === 'google') {
                // Exchange auth code for access token
                $tokenResponse = $this->exchangeAuthCodeForToken($authCode, $provider);
                
                if ($tokenResponse && isset($tokenResponse['access_token'])) {
                    // Get user info with the access token
                    $socialUser = $this->getUserFromToken($tokenResponse['access_token'], $provider);
                } else {
                    return response()->json([
                        'success' => false,
                        'message' => 'Failed to exchange auth code for token'
                    ], 400);
                }
            } 
            // Handle access token flow (existing)
            elseif ($accessToken) {
                $socialUser = $this->getUserFromAccessToken($accessToken, $provider);
            }
            
            if (!$socialUser) {
                return response()->json([
                    'success' => false,
                    'message' => 'Invalid access token, auth code, or ID token'
                ], 400);
            }

            // Find or create user
            $user = $this->findOrCreateUser($socialUser, $provider);
            
            // Generate Sanctum token
            $token = $user->createToken('auth-token')->plainTextToken;

            return response()->json([
                'success' => true,
                'message' => 'Authentication successful',
                'data' => [
                    'user' => [
                        'id' => $user->id,
                        'name' => $user->name,
                        'email' => $user->email,
                        'avatar' => $user->avatar,
                        'phone_verified' => $user->phone_verified,
                        'profile_completed' => $user->profile_completed,
                        'provider' => $user->provider,
                    ],
                    'token' => $token,
                    'token_type' => 'Bearer'
                ]
            ]);

        } catch (Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'OAuth authentication failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Check if a token is in JWT format
     */
    private function isJwtToken(string $token): bool
    {
        // JWT tokens have 3 parts separated by dots
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            return false;
        }
        
        // Try to decode the header to verify it's a valid JWT
        try {
            $header = json_decode(base64_decode(str_replace(['-', '_'], ['+', '/'], $parts[0])), true);
            return isset($header['typ']) && $header['typ'] === 'JWT';
        } catch (Exception $e) {
            return false;
        }
    }

    /**
     * Verify Google ID token and extract user information
     */
    private function verifyGoogleIdToken(string $idToken): ?object
    {
        try {
            $client = new GoogleClient();
            $client->setClientId(config('services.google.client_id'));
            
            $payload = $client->verifyIdToken($idToken);
            
            if ($payload) {
                // ID token is valid, extract user information
                return (object) [
                    'id' => $payload['sub'], // Google's unique user ID
                    'name' => $payload['name'] ?? null,
                    'email' => $payload['email'] ?? null,
                    'avatar' => $payload['picture'] ?? null,
                ];
            }
        } catch (Exception $e) {
            \Log::error('Google ID token verification failed: ' . $e->getMessage());
        }

        return null;
    }

    // ... existing code ...
} 