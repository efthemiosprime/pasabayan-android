package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.StatCard
import com.efthemiosprime.pasabayan.features.dashboard.components.UserHeaderCard

/**
 * Shipper Explore tab — stats, search, recent activity.
 * Parity with iOS `ShipperHomeContent.swift`.
 */
@Composable
fun ShipperExploreContent(
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
            currentRole = UserRole.SHIPPER,
            verificationLevel = null,
            rating = null,
            avatarUrl = user.avatar,
            onSwitchRole = onSwitchRole,
        )

        // Stats (3-column row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            StatCard(
                icon = Icons.Default.LocalShipping,
                label = stringResource(R.string.dashboard_shipper_in_transit),
                value = "—",
                accentColor = PasabayanColors.StatusInTransit,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.Done,
                label = stringResource(R.string.dashboard_shipper_delivered),
                value = "—",
                accentColor = PasabayanColors.Success,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.dashboard_shipper_pending),
                value = "—",
                accentColor = PasabayanColors.StatusPending,
                modifier = Modifier.weight(1f),
            )
        }

        // Find carriers section
        Text(
            text = stringResource(R.string.dashboard_shipper_find_carriers),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_shipper_find_carriers_subtitle),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Search
        POutlinedTextField(
            value = "",
            onValueChange = { /* TODO: search */ },
            label = { Text(stringResource(R.string.dashboard_shipper_search_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
        )

        // Recent activity (placeholder)
        Text(
            text = stringResource(R.string.dashboard_shipper_recent_activity),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
        PEmptyState(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(R.string.packages_empty_no_packages),
            description = stringResource(R.string.packages_empty_no_packages_description),
        )
    }
}

@Preview(showBackground = true, name = "ShipperExplore — light", heightDp = 900)
@Preview(showBackground = true, name = "ShipperExplore — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShipperExplorePreview() {
    PasabayanTheme {
        ShipperExploreContent(
            user = AuthUser(
                id = 1, name = "Alice Shipper", email = "alice@example.com",
                avatar = null, phone = null, phoneVerified = false,
                profileCompleted = false, provider = "google",
                userTypes = listOf("shipper"), isActiveCarrier = false, isActiveShipper = true,
            ),
            onSwitchRole = {},
        )
    }
}
