package com.efthemiosprime.pasabayan.features.support.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.R

/**
 * Categories accepted by `POST /support/tickets`. iOS parity: `SupportCategory` in
 * `SupportModels.swift`. Wire `rawValue` is the canonical snake_case string the backend stores.
 */
enum class SupportCategory(
    val rawValue: String,
    val icon: ImageVector,
    @StringRes val displayNameRes: Int,
) {
    ACCOUNT_PROFILE("account_profile", Icons.Filled.AccountCircle, R.string.support_category_account_profile),
    PAYMENT_BILLING("payment_billing", Icons.Filled.CreditCard, R.string.support_category_payment_billing),
    DELIVERY_ISSUES("delivery_issues", Icons.Filled.LocalShipping, R.string.support_category_delivery_issues),
    PACKAGE_TRACKING("package_tracking", Icons.Filled.LocationOn, R.string.support_category_package_tracking),
    SAFETY_CONCERNS("safety_concerns", Icons.Filled.Security, R.string.support_category_safety_concerns),
    TECHNICAL_PROBLEM("technical_problem", Icons.Filled.Build, R.string.support_category_technical_problem),
    ACCOUNT_VERIFICATION("account_verification", Icons.Filled.VerifiedUser, R.string.support_category_account_verification),
    REFUND_REQUEST("refund_request", Icons.Filled.Replay, R.string.support_category_refund_request),
    OTHER("other", Icons.Filled.HelpOutline, R.string.support_category_other);

    companion object {
        private val byRaw = entries.associateBy { it.rawValue }
        fun fromRaw(raw: String?): SupportCategory? = raw?.let { byRaw[it] }
    }
}
