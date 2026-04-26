package com.efthemiosprime.pasabayan.features.profile.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

fun shouldShowFavoritesMenu(role: UserRole): Boolean = role == UserRole.SHIPPER

fun shouldShowVehicleInfo(role: UserRole): Boolean = role == UserRole.CARRIER

fun shouldShowPayoutSetup(role: UserRole): Boolean = role == UserRole.CARRIER

fun shouldShowDeliveryHistoryMenu(role: UserRole): Boolean = role == UserRole.CARRIER

fun shouldShowPackageHistoryMenu(role: UserRole): Boolean = role == UserRole.SHIPPER

/**
 * [android-spec/20-profile-tab.md]: hidden for `premium`, shown for `basic` and `verified`.
 */
fun shouldShowVerificationCard(verificationLevel: String?): Boolean {
    val level = (verificationLevel ?: "basic").lowercase()
    return level != "premium"
}
