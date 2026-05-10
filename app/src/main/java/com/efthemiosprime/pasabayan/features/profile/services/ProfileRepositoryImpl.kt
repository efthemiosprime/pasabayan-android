package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatusDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentDataJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentsDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val json: Json,
) : ProfileRepository {

    private val profileCacheLock = Mutex()
    private var cachedProfile: ProfileDataJson? = null

    override suspend fun fetchProfile(forceRefresh: Boolean): Result<ProfileDataJson> {
        try {
            if (forceRefresh) {
                profileCacheLock.withLock { cachedProfile = null }
            } else {
                val snap = profileCacheLock.withLock { cachedProfile }
                if (snap != null) {
                    return Result.success(snap)
                }
            }
            val res = profileApi.getProfile()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Profile request failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val data = body.data
            profileCacheLock.withLock { cachedProfile = data }
            return Result.success(data)
        } catch (e: Exception) {
            return Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updateProfile(
        request: UpdateProfileRequestJson,
    ): Result<ProfileDataJson> {
        return try {
            val res = profileApi.putProfile(request)
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Profile update failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val data = body.data
            invalidateProfileCache(data)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String,
        fullName: String?,
        deliveryAddress: String?,
        preferredContactMethod: String?,
        additionalInfo: Map<String, String>?,
    ): Result<ProfileDataJson> {
        return try {
            val imagePart = MultipartBody.Part.createFormData(
                name = "profile_picture",
                filename = fileName,
                body = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull()),
            )
            val res = profileApi.postProfileMultipart(
                profilePicture = imagePart,
                fullName = fullName?.takeIf { it.isNotBlank() }?.toTextPart(),
                deliveryAddress = deliveryAddress?.takeIf { it.isNotBlank() }?.toTextPart(),
                preferredContactMethod = (preferredContactMethod ?: "app_notification").toTextPart(),
                additionalInfo = additionalInfo?.takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(MapStringSerializer, it).toTextPart() },
            )
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Avatar upload failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val data = body.data
            invalidateProfileCache(data)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun deleteProfilePicture(): Result<Unit> {
        return try {
            val res = profileApi.deleteProfilePicture()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            invalidateProfileCache(null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun requestAccountDeletion(
        reason: String?,
    ): Result<AccountDeletionDataJson> {
        return try {
            val res = profileApi.requestAccountDeletion(
                AccountDeletionRequestJson(
                    confirm = true,
                    reason = reason?.takeIf { it.isNotBlank() },
                ),
            )
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Account deletion failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchDisclaimerAcknowledgments(): Result<DisclaimerAcknowledgmentsDataJson> {
        return try {
            val res = profileApi.getDisclaimerAcknowledgments()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(body.data ?: DisclaimerAcknowledgmentsDataJson())
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun acknowledgeDisclaimer(
        type: String,
    ): Result<DisclaimerAcknowledgmentDataJson> {
        return try {
            val res = profileApi.postDisclaimerAcknowledgment(
                DisclaimerAcknowledgmentRequestJson(disclaimerType = type),
            )
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchConsentPreferences(): Result<ConsentPreferencesDataJson> {
        return try {
            val res = profileApi.getConsentPreferences()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updateConsentPreferences(
        update: ConsentPreferencesUpdateJson,
    ): Result<ConsentPreferencesDataJson> {
        return try {
            val res = profileApi.putConsentPreferences(update)
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun exportUserData(): Result<ByteArray> {
        return try {
            val res = profileApi.exportUserData()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val bytes = res.body()?.bytes()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?> {
        return try {
            val res = profileApi.getCarrierProfile()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchCarrierStats(): Result<CarrierStatsJson?> {
        return try {
            val res = profileApi.getCarrierStats()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (body.success == false) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Stats failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchUserStats(): Result<UserStatsDataJson?> {
        return try {
            val res = profileApi.getUserStats()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (body.success == false) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "User stats failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun toggleCarrierStatus(): Result<CarrierStatusDataJson> {
        return try {
            val res = profileApi.postCarrierToggleStatus()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        return try {
            val res = profileApi.postCarrierProfile(body)
            when {
                res.isSuccessful -> {
                    val data = res.body()?.data
                    Result.success(data)
                }
                res.code() == 409 -> fetchCarrierProfile()
                else -> Result.failure(mapError(res))
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        return try {
            val res = profileApi.putCarrierProfile(body)
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            Result.success(res.body()?.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun enableCarrier(): Result<Unit> {
        return try {
            val res = profileApi.postCarrierEnable()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private suspend fun invalidateProfileCache(replacement: ProfileDataJson?) {
        profileCacheLock.withLock { cachedProfile = replacement }
    }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))

    private fun String.toTextPart(): RequestBody =
        toRequestBody("text/plain".toMediaTypeOrNull())

    companion object {
        private val MapStringSerializer = MapSerializer(String.serializer(), String.serializer())
    }
}
