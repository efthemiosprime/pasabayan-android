package com.efthemiosprime.pasabayan.features.profile.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.UserProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.core.session.AuthUser

data class ProfileTabUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userProfile: UserProfileJson? = null,
    val carrierProfile: CarrierProfileJson? = null,
    val carrierStats: CarrierStatsJson? = null,
    val userStats: UserStatsDataJson? = null,
    /** Bumped when avatar is uploaded/removed to refresh image URLs. */
    val avatarCacheBuster: String = "",
    val currentRole: UserRole = UserRole.SHIPPER,
    val authUser: AuthUser? = null,
)
