package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PMenuRow
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationStatusDataJson
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ProfileTabUiState
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowDeliveryHistoryMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowFavoritesMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowPackageHistoryMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowPayoutSetup
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVehicleInfo
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVerificationCard
import com.efthemiosprime.pasabayan.features.dashboard.components.CarrierActiveBadge
import com.efthemiosprime.pasabayan.features.dashboard.components.RoleChip
import com.efthemiosprime.pasabayan.features.dashboard.components.VerificationBadge
import com.efthemiosprime.pasabayan.features.profile.components.CarrierPreferencesCard
import com.efthemiosprime.pasabayan.features.profile.viewmodel.ProfileTabViewModel
import com.efthemiosprime.pasabayan.features.verification.model.PremiumApplicationStatus

@Composable
fun ProfileTabScreen(
    user: AuthUser,
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    onOpenPaymentsHub: () -> Unit,
    onOpenPayoutSetup: () -> Unit = onOpenPaymentsHub,
    onOpenPersonalInfo: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    onOpenVerification: () -> Unit = {},
    onOpenPremiumVerification: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAccountManagement: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenRatings: () -> Unit = {},
    onOpenHelpCenter: () -> Unit = {},
    onOpenLegal: () -> Unit = {},
    onOpenPlaceholder: (String) -> Unit = { },
    viewModel: ProfileTabViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val versionName = rememberVersionName()
    LaunchedEffect(user, currentRole) {
        viewModel.loadTabData(user, currentRole)
    }
    if (state.isLoading && state.userProfile == null && state.errorMessage == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PCircularProgress()
        }
        return
    }
    if (state.errorMessage != null && state.userProfile == null) {
        Column(
            modifier = modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = state.errorMessage ?: stringResource(R.string.profile_error_generic),
                style = PasabayanTextStyles.Body.medium,
            )
            PButton(
                text = stringResource(R.string.profile_retry),
                onClick = { viewModel.loadTabData(user, currentRole, forceRefresh = true) },
            )
        }
        return
    }
    ProfileTabContent(
        state = state,
        user = user,
        currentRole = currentRole,
        versionName = versionName,
        onSwitchRole = onSwitchRole,
        onLogout = onLogout,
        onOpenPaymentsHub = onOpenPaymentsHub,
        onOpenPayoutSetup = onOpenPayoutSetup,
        onOpenPersonalInfo = onOpenPersonalInfo,
        onOpenVehicleInfo = onOpenVehicleInfo,
        onOpenVerification = onOpenVerification,
        onOpenPremiumVerification = onOpenPremiumVerification,
        onOpenSettings = onOpenSettings,
        onOpenAccountManagement = onOpenAccountManagement,
        onOpenFavorites = onOpenFavorites,
        onOpenRatings = onOpenRatings,
        onOpenHelpCenter = onOpenHelpCenter,
        onOpenLegal = onOpenLegal,
        onOpenPlaceholder = onOpenPlaceholder,
        modifier = modifier,
    )
}

@Composable
fun ProfileTabContent(
    state: ProfileTabUiState,
    user: AuthUser,
    currentRole: UserRole,
    versionName: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    onOpenPaymentsHub: () -> Unit,
    onOpenPayoutSetup: () -> Unit = onOpenPaymentsHub,
    onOpenPersonalInfo: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    onOpenVerification: () -> Unit = {},
    onOpenPremiumVerification: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAccountManagement: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenRatings: () -> Unit = {},
    onOpenHelpCenter: () -> Unit = {},
    onOpenLegal: () -> Unit = {},
    onOpenPlaceholder: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val verificationLevel = state.userProfile?.verificationLevel
    val showVerify = shouldShowVerificationCard(verificationLevel)
    val scroll = rememberScrollState()
    Column(
        modifier
            .verticalScroll(scroll)
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.sm,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xl),
    ) {
        val carrierActive = (state.carrierProfile?.carrierStatus == "active") || user.isActiveCarrier
        ProfileUserHeader(
            name = state.userProfile?.fullName?.takeIf { it.isNotBlank() } ?: user.name,
            email = user.email,
            currentRole = currentRole,
            verificationLevel = VerificationLevel.normalized(verificationLevel),
            isCarrierActive = carrierActive,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ProfileTestTags.Header),
        )
        ProfileStatsBlock(currentRole, state, Modifier.fillMaxWidth().testTag(ProfileTestTags.Stats))
        if (currentRole == UserRole.CARRIER) {
            CarrierPreferencesCard(
                carrierProfile = state.carrierProfile,
                onEdit = onOpenVehicleInfo,
            )
        }
        if (showVerify) {
            VerificationCallout(
                verificationLevel = VerificationLevel.normalized(verificationLevel),
                premiumStatus = state.premiumStatus,
                onVerify = onOpenVerification,
                onUpgradeToPremium = onOpenPremiumVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ProfileTestTags.Verification),
            )
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_account),
            testTag = ProfileTestTags.MenuAccount,
        ) {
            PMenuRow(
                title = stringResource(R.string.profile_menu_personal_info),
                subtitle = stringResource(R.string.profile_menu_personal_info_subtitle),
                leadingIcon = Icons.Filled.Person,
                onClick = onOpenPersonalInfo,
            )
            if (shouldShowVehicleInfo(currentRole)) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_vehicle_info),
                    subtitle = stringResource(R.string.profile_menu_vehicle_info_subtitle),
                    leadingIcon = Icons.Filled.DirectionsCar,
                    onClick = onOpenVehicleInfo,
                )
            }
            if (currentRole == UserRole.SHIPPER) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_shipping_addresses),
                    subtitle = stringResource(R.string.profile_menu_shipping_addresses_subtitle),
                    leadingIcon = Icons.Filled.LocationOn,
                    onClick = { onOpenPlaceholder("shipping") },
                )
            }
            PMenuRow(
                title = stringResource(R.string.profile_account_menu_label),
                subtitle = stringResource(R.string.profile_menu_account_management_subtitle),
                leadingIcon = Icons.Filled.ManageAccounts,
                onClick = onOpenAccountManagement,
            )
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_payments),
            testTag = ProfileTestTags.MenuPayments,
        ) {
            PMenuRow(
                title = stringResource(R.string.profile_menu_payment_methods),
                subtitle = stringResource(R.string.profile_menu_payment_methods_subtitle),
                leadingIcon = Icons.Filled.CreditCard,
                onClick = onOpenPaymentsHub,
            )
            PMenuRow(
                title = stringResource(R.string.profile_menu_transactions),
                subtitle = stringResource(R.string.profile_menu_transactions_subtitle),
                leadingIcon = Icons.AutoMirrored.Filled.ListAlt,
                onClick = onOpenPaymentsHub,
            )
            PMenuRow(
                title = stringResource(R.string.profile_menu_receipts),
                subtitle = stringResource(R.string.profile_menu_receipts_subtitle),
                leadingIcon = Icons.Filled.Receipt,
                onClick = onOpenPaymentsHub,
            )
            if (shouldShowPayoutSetup(currentRole)) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_payout),
                    subtitle = stringResource(R.string.profile_menu_payout_subtitle),
                    leadingIcon = Icons.Filled.AccountBalance,
                    onClick = onOpenPayoutSetup,
                )
            }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_bookings),
            testTag = ProfileTestTags.MenuBookings,
        ) {
            if (shouldShowDeliveryHistoryMenu(currentRole)) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_delivery_history),
                    subtitle = stringResource(R.string.profile_menu_delivery_history_subtitle),
                    leadingIcon = Icons.Filled.History,
                    onClick = { onOpenPlaceholder("delivery_history") },
                )
            }
            if (shouldShowPackageHistoryMenu(currentRole)) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_package_history),
                    subtitle = stringResource(R.string.profile_menu_package_history_subtitle),
                    leadingIcon = Icons.Filled.History,
                    onClick = { onOpenPlaceholder("package_history") },
                )
            }
        }
        if (shouldShowFavoritesMenu(currentRole)) {
            ProfileMenuSection(
                title = stringResource(R.string.profile_menu_favorites),
                testTag = ProfileTestTags.MenuFavorites,
            ) {
                PMenuRow(
                    title = stringResource(R.string.profile_menu_favorites),
                    subtitle = stringResource(R.string.profile_menu_favorites_subtitle),
                    leadingIcon = Icons.Filled.Star,
                    onClick = onOpenFavorites,
                )
            }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_feedback),
            testTag = ProfileTestTags.MenuFeedback,
        ) {
            // iOS parity with FeedbackMenuSection.swift — two rows: pending reviews to
            // submit, and "my ratings" with an inline average + count preview.
            PMenuRow(
                title = stringResource(R.string.profile_menu_pending_reviews),
                subtitle = stringResource(R.string.profile_menu_pending_reviews_subtitle),
                leadingIcon = Icons.Filled.RateReview,
                // TODO: wire `badgeCount = attention.pendingReviewsCount` once
                // ProfileAttentionViewModel (#21) lands.
                onClick = onOpenRatings,
            )
            val (avgRating, ratingCount) = profileRatingPreview(currentRole, state)
            PMenuRow(
                title = stringResource(R.string.profile_menu_my_ratings),
                subtitle = stringResource(R.string.profile_menu_my_ratings_subtitle),
                leadingIcon = Icons.Filled.Star,
                leadingIconTint = PasabayanColors.BadgeGold,
                onClick = onOpenRatings,
                trailing = {
                    val previewText = if (avgRating != null && ratingCount > 0) {
                        stringResource(
                            R.string.profile_menu_my_ratings_inline,
                            "%.2f".format(avgRating),
                            ratingCount,
                        )
                    } else {
                        stringResource(R.string.profile_menu_no_ratings_yet)
                    }
                    Text(
                        text = previewText,
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_support),
            testTag = ProfileTestTags.MenuSupport,
        ) {
            PMenuRow(
                title = stringResource(R.string.profile_menu_help),
                subtitle = stringResource(R.string.profile_menu_help_subtitle),
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = onOpenHelpCenter,
            )
            PMenuRow(
                title = stringResource(R.string.profile_menu_settings),
                subtitle = stringResource(R.string.profile_menu_settings_subtitle),
                leadingIcon = Icons.Filled.Settings,
                onClick = onOpenSettings,
            )
            PMenuRow(
                title = stringResource(R.string.profile_menu_terms),
                subtitle = stringResource(R.string.profile_menu_terms_subtitle),
                leadingIcon = Icons.Filled.Description,
                onClick = onOpenLegal,
            )
        }
        PButton(
            text = stringResource(R.string.profile_actions_logout),
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ProfileTestTags.Logout),
        )
        Text(
            text = stringResource(
                R.string.profile_footer_version,
                stringResource(R.string.app_name),
                versionName,
            ),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PasabayanSpacing.md),
        )
    }
}

private val ProfileHeaderAvatarSize = 72.dp

@Composable
private fun ProfileUserHeader(
    name: String,
    email: String,
    currentRole: UserRole,
    verificationLevel: VerificationLevel,
    isCarrierActive: Boolean,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier, variant = PCardVariant.Large) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ProfileLetterAvatar(name = name)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = PasabayanSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    ) {
                        Text(text = name, style = PasabayanTextStyles.Heading.h3)
                        VerificationBadge(level = verificationLevel, size = 18.dp)
                    }
                    Text(
                        text = email,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Role switching lives in DashboardTopBar's SwapHoriz icon (single source
                    // of truth). Render a static chip here for identification only.
                    RoleChip(
                        role = currentRole,
                        modifier = Modifier.testTag(ProfileTestTags.Role),
                    )
                }
            }
            if (currentRole == UserRole.CARRIER) {
                CarrierActiveBadge(
                    isActive = isCarrierActive,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .testTag(ProfileTestTags.CarrierStatus),
                )
            }
        }
    }
}

@Composable
private fun ProfileLetterAvatar(name: String) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(ProfileHeaderAvatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = PasabayanTextStyles.Heading.h2,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ProfileStatsBlock(
    role: UserRole,
    state: ProfileTabUiState,
    modifier: Modifier = Modifier,
) {
    PCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_stats_title),
                style = PasabayanTextStyles.Heading.h4,
            )
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                when (role) {
                    UserRole.SHIPPER -> {
                        val s = state.userStats
                        StatLine(R.string.profile_stat_packages, s?.packagesCount?.toString() ?: "—")
                        StatLine(R.string.profile_stat_delivered, s?.deliveredCount?.toString() ?: "—")
                        StatLine(
                            R.string.profile_stat_rating,
                            s?.averageRating?.toString() ?: s?.totalRatings?.toString() ?: "—",
                        )
                    }
                    UserRole.CARRIER -> {
                        val c = state.carrierStats
                        val d = c?.deliveries
                        val r = c?.ratings
                        StatLine(
                            R.string.profile_stat_deliveries,
                            d?.totalTrips?.toString() ?: d?.completedMatches?.toString() ?: "—",
                        )
                        StatLine(R.string.profile_stat_rating, r?.averageRating ?: "—")
                        StatLine(
                            R.string.profile_stat_earnings,
                            c?.earnings?.totalEarnings?.toString() ?: "—",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(stringRes: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = stringResource(stringRes), style = PasabayanTextStyles.Body.small)
        Text(text = value, style = PasabayanTextStyles.Body.medium)
    }
}

/**
 * Resolves the (average rating, total ratings) tuple for the My Ratings inline preview.
 * Carrier ratings come back as a string from the server, shipper as a Double — coalesce
 * both into a uniform pair. Returns (null, 0) when no ratings exist.
 */
private fun profileRatingPreview(
    role: UserRole,
    state: ProfileTabUiState,
): Pair<Double?, Int> = when (role) {
    UserRole.CARRIER -> {
        val avg = state.carrierStats?.ratings?.averageRating?.toDoubleOrNull()
        val count = state.carrierStats?.ratings?.totalRatings ?: 0
        avg to count
    }
    UserRole.SHIPPER -> {
        val avg = state.userStats?.averageRating
        val count = state.userStats?.totalRatings ?: 0
        avg to count
    }
}

/**
 * Verification call-to-action — iOS parity with `VerificationStatusView.swift`.
 * Three branches based on [verificationLevel] + [premiumStatus]:
 *  - BASIC → "Verify Now" pitch with phone-verification CTA.
 *  - VERIFIED, no pending premium app → "Complete Your Verification" upgrade pitch.
 *  - VERIFIED with pending premium app → "Premium Application Submitted" status card.
 *  - PREMIUM → renders nothing (caller's `shouldShowVerificationCard` already filters).
 */
@Composable
private fun VerificationCallout(
    verificationLevel: VerificationLevel,
    premiumStatus: PremiumVerificationStatusDataJson?,
    onVerify: () -> Unit,
    onUpgradeToPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (verificationLevel) {
        VerificationLevel.BASIC -> BasicVerificationCard(onVerify = onVerify, modifier = modifier)
        VerificationLevel.VERIFIED -> {
            val pendingRequest = premiumStatus?.requests?.firstOrNull()
            val pendingStatus = pendingRequest?.status?.let { PremiumApplicationStatus.fromRaw(it) }
            val isPending = pendingStatus == PremiumApplicationStatus.PENDING ||
                pendingStatus == PremiumApplicationStatus.UNDER_REVIEW
            if (isPending) {
                PremiumPendingCard(applicationId = pendingRequest?.id, modifier = modifier)
            } else {
                PremiumUpgradeCard(onUpgradeToPremium = onUpgradeToPremium, modifier = modifier)
            }
        }
        VerificationLevel.PREMIUM -> Unit
    }
}

@Composable
private fun BasicVerificationCard(
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCalloutSurface(modifier = modifier) {
        VerificationCalloutHeader(
            icon = Icons.Filled.VerifiedUser,
            iconTint = PasabayanColors.Info,
            iconBackground = PasabayanColors.Info.copy(alpha = 0.15f),
            title = stringResource(R.string.profile_verification_title),
            description = stringResource(R.string.profile_verification_description_basic),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.profile_verification_why_verify),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            VerificationBenefitRow(
                icon = Icons.Filled.Check,
                tint = PasabayanColors.Info,
                text = stringResource(R.string.profile_verification_benefit_trust),
            )
            VerificationBenefitRow(
                icon = Icons.Filled.Check,
                tint = PasabayanColors.Info,
                text = stringResource(R.string.profile_verification_benefit_priority),
            )
            VerificationBenefitRow(
                icon = Icons.Filled.Check,
                tint = PasabayanColors.Info,
                text = stringResource(R.string.profile_verification_benefit_features),
            )
        }
        PButton(
            text = stringResource(R.string.profile_verification_action_verify),
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PremiumUpgradeCard(
    onUpgradeToPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCalloutSurface(modifier = modifier) {
        VerificationCalloutHeader(
            icon = Icons.Filled.VerifiedUser,
            iconTint = PasabayanColors.Info,
            iconBackground = PasabayanColors.Info.copy(alpha = 0.15f),
            title = stringResource(R.string.profile_premium_upgrade_title),
            description = stringResource(R.string.profile_premium_upgrade_description),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            VerificationBenefitRow(
                icon = Icons.Filled.Star,
                tint = PasabayanColors.BadgeGold,
                text = stringResource(R.string.profile_premium_benefit_gold_badge),
            )
            VerificationBenefitRow(
                icon = Icons.Filled.Search,
                tint = PasabayanColors.Info,
                text = stringResource(R.string.profile_premium_benefit_search_priority),
            )
            VerificationBenefitRow(
                icon = Icons.Filled.VerifiedUser,
                tint = PasabayanColors.Success,
                text = stringResource(R.string.profile_premium_benefit_verified_id),
            )
        }
        PButton(
            text = stringResource(R.string.profile_premium_action_apply),
            onClick = onUpgradeToPremium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PremiumPendingCard(
    applicationId: Int?,
    modifier: Modifier = Modifier,
) {
    VerificationCalloutSurface(modifier = modifier) {
        VerificationCalloutHeader(
            icon = Icons.Filled.Schedule,
            iconTint = PasabayanColors.Info,
            iconBackground = PasabayanColors.Info.copy(alpha = 0.2f),
            title = stringResource(R.string.profile_premium_submitted_title),
            description = stringResource(R.string.profile_premium_submitted_description),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            VerificationBenefitRow(
                icon = Icons.Filled.Schedule,
                tint = PasabayanColors.Warning,
                text = stringResource(R.string.profile_premium_status_label),
            )
            VerificationBenefitRow(
                icon = Icons.Filled.Schedule,
                tint = PasabayanColors.Info,
                text = stringResource(R.string.profile_premium_review_time_label),
            )
            if (applicationId != null) {
                VerificationBenefitRow(
                    icon = Icons.Filled.Description,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.profile_premium_application_id, applicationId),
                )
            }
        }
    }
}

@Composable
private fun VerificationCalloutSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanRadius.card),
        color = PasabayanColors.BadgeBlueLight,
        border = BorderStroke(PasabayanBorder.width, PasabayanColors.Info.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            content = content,
        )
    }
}

@Composable
private fun VerificationCalloutHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconBackground: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(VerificationIconCircleSize)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(VerificationIconGlyphSize),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(text = title, style = PasabayanTextStyles.Heading.h4)
            Text(
                text = description,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VerificationBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(VerificationBenefitIconSize),
        )
        Text(
            text = text,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private val VerificationIconCircleSize = 48.dp
private val VerificationIconGlyphSize = 26.dp
private val VerificationBenefitIconSize = 16.dp

@Composable
private fun ProfileMenuSection(
    title: String,
    testTag: String,
    content: @Composable () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth().testTag(testTag)) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(text = title, style = PasabayanTextStyles.Heading.h4)
            content()
        }
    }
}

@Composable
private fun rememberVersionName(): String {
    val context = LocalContext.current
    return remember(context.packageName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                ).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}

@Preview(showBackground = true, name = "Profile tab light")
@Preview(showBackground = true, name = "Profile tab dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileTabScreenPreview() {
    PasabayanTheme {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            ProfileUserHeader(
                name = "Alex",
                email = "a@b.c",
                currentRole = UserRole.SHIPPER,
                verificationLevel = VerificationLevel.VERIFIED,
                isCarrierActive = false,
                modifier = Modifier.padding(PasabayanSpacing.md),
            )
            ProfileUserHeader(
                name = "Maria Santos",
                email = "maria@example.com",
                currentRole = UserRole.CARRIER,
                verificationLevel = VerificationLevel.PREMIUM,
                isCarrierActive = true,
                modifier = Modifier.padding(PasabayanSpacing.md),
            )
        }
    }
}
