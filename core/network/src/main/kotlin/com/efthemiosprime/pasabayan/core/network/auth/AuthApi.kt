package com.efthemiosprime.pasabayan.core.network.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Auth endpoints per `android-spec/02-auth-session.md`. */
interface AuthApi {

    @POST("auth/{provider}/login")
    suspend fun loginWithProvider(
        @Path("provider") provider: String,
        @Body body: ProviderLoginRequestJson,
    ): Response<BackendAuthResponseJson>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponseJson>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponseJson>
}
