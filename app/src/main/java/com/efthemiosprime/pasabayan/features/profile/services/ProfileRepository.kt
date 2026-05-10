package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatusDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentDataJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentsDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson

/**
 * Profile aggregate: user profile CRUD, avatar, carrier profile/stats, consent, disclaimers,
 * account deletion, data export. Returns network DTOs; mapping to UI state lives in ViewModels.
 *
 * Cache invalidation: mutating user-profile operations clear the cached [ProfileDataJson] so the
 * next [fetchProfile] call re-fetches. Carrier endpoints are not cached.
 */
interface ProfileRepository {

    suspend fun fetchProfile(forceRefresh: Boolean = false): Result<ProfileDataJson>

    suspend fun updateProfile(request: UpdateProfileRequestJson): Result<ProfileDataJson>

    /**
     * Multipart `POST /profile` with the encoded image bytes. Optional text fields are sent only
     * when non-null. Defaults to `application/jpeg` MIME and `avatar.jpg` filename. Per
     * `09-profile-carrier-consent.md`, callers should pre-compress to 512px / 0.7 quality.
     */
    suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        fileName: String = "avatar.jpg",
        fullName: String? = null,
        deliveryAddress: String? = null,
        preferredContactMethod: String? = null,
        additionalInfo: Map<String, String>? = null,
    ): Result<ProfileDataJson>

    suspend fun deleteProfilePicture(): Result<Unit>

    suspend fun requestAccountDeletion(reason: String? = null): Result<AccountDeletionDataJson>

    suspend fun fetchDisclaimerAcknowledgments(): Result<DisclaimerAcknowledgmentsDataJson>

    suspend fun acknowledgeDisclaimer(type: String): Result<DisclaimerAcknowledgmentDataJson>

    suspend fun fetchConsentPreferences(): Result<ConsentPreferencesDataJson>

    /**
     * Scoped consent update — pass only the fields being changed (others left `null`).
     */
    suspend fun updateConsentPreferences(
        update: ConsentPreferencesUpdateJson,
    ): Result<ConsentPreferencesDataJson>

    /**
     * `GET /profile/export-data` — returns raw JSON bytes for share-sheet handoff.
     */
    suspend fun exportUserData(): Result<ByteArray>

    suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?>

    suspend fun fetchCarrierStats(): Result<CarrierStatsJson?>

    suspend fun fetchUserStats(): Result<UserStatsDataJson?>

    suspend fun toggleCarrierStatus(): Result<CarrierStatusDataJson>

    /**
     * Creates carrier profile; on **409 Conflict** falls back to [fetchCarrierProfile] per iOS/`09-profile-carrier-consent`.
     */
    suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?>

    suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?>

    /**
     * `POST /carrier/enable` — idempotent. Spec says fail silently if already enabled, but at the
     * repository layer we still surface non-2xx as failure; the caller (typically a setup flow)
     * decides whether to ignore.
     */
    suspend fun enableCarrier(): Result<Unit>
}
