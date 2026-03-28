package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.StatCard
import com.efthemiosprime.pasabayan.features.dashboard.components.UserHeaderCard

/**
 * Carrier Explore tab — stats grid, recent trips, earnings stub.
 * Parity with iOS `CarrierHomeContent.swift`.
 */
@Composable
fun CarrierExploreContent(
    user: AuthUser,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        // User header
        UserHeaderCard(
            userName = user.name,
            currentRole = UserRole.CARRIER,
            verificationLevel = null,
            rating = null,
            avatarUrl = user.avatar,
            onSwitchRole = onSwitchRole,
        )

        // Stats grid (2x2)
        Text(
            text = stringResource(R.string.dashboard_carrier_stats_title),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            StatCard(
                icon = Icons.Default.LocalShipping,
                label = stringResource(R.string.dashboard_carrier_active_trips),
                value = "—",
                accentColor = PasabayanColors.Info,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.AttachMoney,
                label = stringResource(R.string.dashboard_carrier_earnings),
                value = "—",
                accentColor = PasabayanColors.Success,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            StatCard(
                icon = Icons.Default.Done,
                label = stringResource(R.string.dashboard_carrier_deliveries),
                value = "—",
                accentColor = PasabayanColors.BadgePurple,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.Star,
                label = stringResource(R.string.dashboard_carrier_rating),
                value = "—",
                accentColor = PasabayanColors.BadgeGold,
                modifier = Modifier.weight(1f),
            )
        }

        // Recent trips (placeholder)
        Text(
            text = stringResource(R.string.dashboard_carrier_recent_trips),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
        PEmptyState(
            icon = Icons.Outlined.LocalShipping,
            title = stringResource(R.string.trips_empty_no_trips),
            description = stringResource(R.string.trips_empty_no_trips_description),
        )
    }
}

@Preview(showBackground = true, name = "CarrierExplore — light", heightDp = 900)
@Preview(showBackground = true, name = "CarrierExplore — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePreview() {
    PasabayanTheme {
        CarrierExploreContent(
            user = AuthUser(
                id = 1, name = "Bong Suyat", email = "bong@example.com",
                avatar = null, phone = null, phoneVerified = false,
                profileCompleted = false, provider = "google",
                userTypes = listOf("carrier"), isActiveCarrier = true, isActiveShipper = false,
            ),
            onSwitchRole = {},
        )
    }
}
