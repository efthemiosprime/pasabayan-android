package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Push-notification and device-token endpoints (spec
 * [08-notifications-device-tokens.md](android-spec/08-notifications-device-tokens.md)).
 *
 * Per spec the timeout target is **15s connect / 30s read** — that already matches the default
 * [com.efthemiosprime.pasabayan.core.network.di.NetworkModule] OkHttp client, so a dedicated
 * client is not required.
 */
interface NotificationApi {

    /**
     * Returns the raw [ResponseBody] so the typed [com.efthemiosprime.pasabayan.features
     * .notifications.services.DeviceTokenRegisterResponseParser] can branch on HTTP status
     * (200/201 vs 403 with `consent_required`) without coupling to Retrofit deserialization.
     */
    @POST("device-tokens")
    suspend fun registerDeviceToken(
        @Body body: DeviceTokenRequestJson,
    ): Response<ResponseBody>

    /**
     * Path token must be URL-encoded by the caller — FCM tokens contain `/`, `+`, `=`.
     */
    @DELETE("device-tokens/{encodedToken}")
    suspend fun unregisterDeviceToken(
        @Path(value = "encodedToken", encoded = true) encodedToken: String,
    ): Response<DeviceTokenSimpleResponseJson>

    @POST("device-tokens/test")
    suspend fun sendTestNotification(): Response<DeviceTokenSimpleResponseJson>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("unread_only") unreadOnly: Boolean? = null,
        @Query("role") role: String? = null,
    ): Response<NotificationHistoryResponseJson>

    /**
     * Returns a raw [JsonElement] because the response shape is one of four formats; the
     * repository runs [UnreadCountResponseParser] to coerce.
     */
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(
        @Query("role") role: String? = null,
    ): Response<JsonElement>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Int,
    ): Response<MarkReadResponseJson>

    @PUT("notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<MarkAllReadResponseJson>
}
