package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatusDataJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson

/**
 * Profile tab aggregate: user profile, carrier profile/stats, shipper stats, carrier toggle.
 * Returns network DTOs; mapping to UI state lives in the ViewModel.
 */
interface ProfileRepository {

    suspend fun fetchProfile(forceRefresh: Boolean = false): Result<ProfileDataJson>

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
}
