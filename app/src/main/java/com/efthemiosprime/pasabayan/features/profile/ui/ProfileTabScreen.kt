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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ProfileTabUiState
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowDeliveryHistoryMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowFavoritesMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowPackageHistoryMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowPayoutSetup
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVehicleInfo
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVerificationCard
import com.efthemiosprime.pasabayan.features.dashboard.components.RoleChip
import com.efthemiosprime.pasabayan.features.profile.viewmodel.ProfileTabViewModel

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
        ProfileUserHeader(
            name = state.userProfile?.fullName?.takeIf { it.isNotBlank() } ?: user.name,
            email = user.email,
            currentRole = currentRole,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ProfileTestTags.Header),
        )
        ProfileStatsBlock(currentRole, state, Modifier.fillMaxWidth().testTag(ProfileTestTags.Stats))
        if (currentRole == UserRole.CARRIER) {
            PCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ProfileTestTags.CarrierStatus),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                    Text(
                        text = stringResource(R.string.profile_carrier_status_title),
                        style = PasabayanTextStyles.Heading.h4,
                    )
                    val active = (state.carrierProfile?.carrierStatus == "active") || user.isActiveCarrier
                    Text(
                        text = if (active) {
                            stringResource(R.string.profile_carrier_status_active)
                        } else {
                            stringResource(R.string.profile_carrier_status_inactive)
                        },
                        style = PasabayanTextStyles.Body.medium,
                    )
                }
            }
        }
        if (showVerify) {
            VerificationCallout(
                onVerify = onOpenVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ProfileTestTags.Verification),
            )
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_account),
            testTag = ProfileTestTags.MenuAccount,
        ) {
            MenuRow(
                stringResource(R.string.profile_menu_personal_info),
            ) { onOpenPersonalInfo() }
            if (shouldShowVehicleInfo(currentRole)) {
                MenuRow(
                    stringResource(R.string.profile_menu_vehicle_info),
                ) { onOpenVehicleInfo() }
            }
            if (currentRole == UserRole.SHIPPER) {
                MenuRow(
                    stringResource(R.string.profile_menu_shipping_addresses),
                ) { onOpenPlaceholder("shipping") }
            }
            MenuRow(
                stringResource(R.string.profile_account_menu_label),
            ) { onOpenAccountManagement() }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_payments),
            testTag = ProfileTestTags.MenuPayments,
        ) {
            MenuRow(
                stringResource(R.string.profile_menu_payment_methods),
            ) { onOpenPaymentsHub() }
            MenuRow(
                stringResource(R.string.profile_menu_transactions),
            ) { onOpenPaymentsHub() }
            MenuRow(
                stringResource(R.string.profile_menu_receipts),
            ) { onOpenPaymentsHub() }
            if (shouldShowPayoutSetup(currentRole)) {
                MenuRow(
                    stringResource(R.string.profile_menu_payout),
                ) { onOpenPayoutSetup() }
            }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_bookings),
            testTag = ProfileTestTags.MenuBookings,
        ) {
            if (shouldShowDeliveryHistoryMenu(currentRole)) {
                MenuRow(
                    stringResource(R.string.profile_menu_delivery_history),
                ) { onOpenPlaceholder("delivery_history") }
            }
            if (shouldShowPackageHistoryMenu(currentRole)) {
                MenuRow(
                    stringResource(R.string.profile_menu_package_history),
                ) { onOpenPlaceholder("package_history") }
            }
        }
        if (shouldShowFavoritesMenu(currentRole)) {
            ProfileMenuSection(
                title = stringResource(R.string.profile_menu_favorites),
                testTag = ProfileTestTags.MenuFavorites,
            ) {
                MenuRow(
                    stringResource(R.string.profile_menu_favorites),
                ) { onOpenFavorites() }
            }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_feedback),
            testTag = ProfileTestTags.MenuFeedback,
        ) {
            MenuRow(
                stringResource(R.string.profile_menu_reviews),
            ) { onOpenRatings() }
        }
        ProfileMenuSection(
            title = stringResource(R.string.profile_menu_support),
            testTag = ProfileTestTags.MenuSupport,
        ) {
            MenuRow(
                stringResource(R.string.profile_menu_help),
            ) { onOpenHelpCenter() }
            MenuRow(
                stringResource(R.string.profile_menu_settings),
            ) { onOpenSettings() }
            MenuRow(
                stringResource(R.string.profile_menu_terms),
            ) { onOpenLegal() }
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
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier, variant = PCardVariant.Large) {
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
                Text(text = name, style = PasabayanTextStyles.Heading.h3)
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
 * Attention-grabbing verification call-to-action — iOS parity with the basic-state
 * `statusCard` + `benefitsCard` combo in `VerificationStatusView.swift`. Tinted info
 * background with a colored border distinguishes this from the neutral menu cards
 * around it; "Why verify?" benefits + a full-width CTA make the action obvious.
 */
@Composable
private fun VerificationCallout(
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
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
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(VerificationIconCircleSize)
                        .clip(CircleShape)
                        .background(PasabayanColors.Info.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = PasabayanColors.Info,
                        modifier = Modifier.size(VerificationIconGlyphSize),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.profile_verification_title),
                        style = PasabayanTextStyles.Heading.h4,
                    )
                    Text(
                        text = stringResource(R.string.profile_verification_description_basic),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = stringResource(R.string.profile_verification_why_verify),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                VerificationBenefitRow(
                    text = stringResource(R.string.profile_verification_benefit_trust),
                )
                VerificationBenefitRow(
                    text = stringResource(R.string.profile_verification_benefit_priority),
                )
                VerificationBenefitRow(
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
}

@Composable
private fun VerificationBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = PasabayanColors.Info,
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
private fun MenuRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.medium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        ProfileUserHeader(
            name = "Alex",
            email = "a@b.c",
            currentRole = UserRole.SHIPPER,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
