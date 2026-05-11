package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.notifications.DeviceTokenRequestJson
import com.efthemiosprime.pasabayan.core.network.notifications.NotificationApi
import com.efthemiosprime.pasabayan.core.network.notifications.UnreadCountResponseParser
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationMapper
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationPage
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val profileApi: ProfileApi,
    private val consentIntent: PushNotificationsConsentIntent,
    private val deviceInfo: DeviceInfoProvider,
    private val json: Json,
) : NotificationRepository {

    override suspend fun registerDeviceToken(fcmToken: String): DeviceTokenRegisterResult {
        if (fcmToken.isBlank()) {
            return DeviceTokenRegisterResult.ServerError("Empty FCM token")
        }
        return registerOnce(fcmToken).let { first ->
            if (first is DeviceTokenRegisterResult.ConsentRequired &&
                first.purpose == PUSH_NOTIFICATIONS_PURPOSE
            ) {
                handleConsentRetry(fcmToken)
            } else {
                first
            }
        }
    }

    private suspend fun registerOnce(fcmToken: String): DeviceTokenRegisterResult {
        val body = DeviceTokenRequestJson(
            token = fcmToken,
            platform = ANDROID_PLATFORM,
            appVersion = deviceInfo.appVersion,
            deviceModel = deviceInfo.deviceModel,
            osVersion = deviceInfo.osVersion,
            deviceName = deviceInfo.deviceName,
        )
        return try {
            val response = notificationApi.registerDeviceToken(body)
            val raw = (response.body()?.string() ?: response.errorBody()?.string()).orEmpty()
            DeviceTokenRegisterResponseParser.parse(
                statusCode = response.code(),
                body = raw,
                json = json,
            )
        } catch (e: Exception) {
            DeviceTokenRegisterResult.DecodingError(e)
        }
    }

    private suspend fun handleConsentRetry(fcmToken: String): DeviceTokenRegisterResult {
        val update = profileApi.putConsentPreferences(
            ConsentPreferencesUpdateJson(pushNotifications = true),
        )
        if (!update.isSuccessful || update.body()?.success != true) {
            return DeviceTokenRegisterResult.ServerError(
                "Consent update failed (HTTP ${update.code()})",
            )
        }
        consentIntent.setOptedIn(true)
        return registerOnce(fcmToken)
    }

    override suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit> = safeCall {
        val encoded = URLEncoder.encode(fcmToken, Charsets.UTF_8.name())
        val response = notificationApi.unregisterDeviceToken(encoded)
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val body = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) throw DomainErrorMapperException(
            DomainError.ServerError(body.message ?: "Unregister failed"),
        )
        Unit
    }

    override suspend fun fetchNotifications(
        page: Int,
        unreadOnly: Boolean?,
        role: String?,
    ): Result<NotificationPage> = safeCall {
        val response = notificationApi.getNotifications(page = page, unreadOnly = unreadOnly, role = role)
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val body = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) throw DomainErrorMapperException(
            DomainError.ServerError(body.message ?: "Failed to load notifications"),
        )
        val data = body.data ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        NotificationPage(
            notifications = data.notifications.map(NotificationMapper::toDomain),
            pagination = data.pagination,
        )
    }

    override suspend fun getUnreadCount(role: String?): Result<Int> = safeCall {
        val response = notificationApi.getUnreadCount(role = role)
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val element = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        UnreadCountResponseParser.parseElement(element)
            ?: throw DomainErrorMapperException(DomainError.DecodingError)
    }

    override suspend fun markAsRead(id: Int): Result<Unit> = safeCall {
        val response = notificationApi.markAsRead(id)
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val body = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) throw DomainErrorMapperException(
            DomainError.ServerError(body.message ?: "Mark as read failed"),
        )
        Unit
    }

    override suspend fun markAllAsRead(): Result<Int> = safeCall {
        val response = notificationApi.markAllAsRead()
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val body = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) throw DomainErrorMapperException(
            DomainError.ServerError(body.message ?: "Mark all read failed"),
        )
        body.data?.resolvedCount ?: 0
    }

    override suspend fun sendTestNotification(): Result<Unit> = safeCall {
        val response = notificationApi.sendTestNotification()
        if (!response.isSuccessful) throw mapHttpError(response.code(), response.errorBody()?.bytes())
        val body = response.body() ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) throw DomainErrorMapperException(
            DomainError.ServerError(body.message ?: "Test send failed"),
        )
        Unit
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: DomainErrorMapperException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
    }

    private fun mapHttpError(code: Int, bytes: ByteArray?): Throwable =
        DomainErrorMapperException(ApiErrorMapper.map(code, bytes, json))

    private companion object {
        const val ANDROID_PLATFORM = "android"
        const val PUSH_NOTIFICATIONS_PURPOSE = "push_notifications"
    }
}
