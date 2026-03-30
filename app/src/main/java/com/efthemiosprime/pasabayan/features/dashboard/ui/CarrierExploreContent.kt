package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.UserHeaderCard
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestCard
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel

/**
 * Carrier Explore tab — browse available packages.
 * Parity with iOS `CarrierHomeContent.swift` → `CarrierDashboardView`.
 */
@Composable
fun CarrierExploreContent(
    user: AuthUser,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPackageDetails: (packageId: Int) -> Unit = {},
    packageViewModel: PackageViewModel = hiltViewModel(),
) {
    val state by packageViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { packageViewModel.loadPackages(force = true) }

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
            avatarUrl = user.avatar,
            onSwitchRole = onSwitchRole,
        )

        // Find Packages header
        Text(
            text = stringResource(R.string.dashboard_carrier_find_packages),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_carrier_find_packages_subtitle),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Search field
        POutlinedTextField(
            value = "",
            onValueChange = { /* TODO: search */ },
            label = { Text(stringResource(R.string.dashboard_carrier_search_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { /* TODO: submit search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PasabayanColors.PrimaryBlack,
                    )
                }
            },
        )

        PDivider()

        // Browse content
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
            state.hasLoadedPackages && state.packageRequests.isEmpty() -> {
                CarrierBrowseEmptyState(
                    onPostTrip = { /* TODO: switch to My Trips tab */ },
                )
            }
            else -> {
                state.packageRequests.forEach { pkg ->
                    PackageRequestCard(
                        pkg = pkg,
                        onViewDetails = { onViewPackageDetails(pkg.id) },
                    )
                }
            }
        }
    }
}

/**
 * Carrier empty state — iOS parity: CTA + tips section.
 */
@Composable
private fun CarrierBrowseEmptyState(
    onPostTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF2F6FE6)),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        PEmptyState(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(R.string.dashboard_carrier_no_packages_title),
            description = stringResource(R.string.dashboard_carrier_no_packages_description),
        )

        androidx.compose.material3.Button(
            onClick = onPostTrip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blueGradient, RoundedCornerShape(14.dp))
                    .padding(vertical = PasabayanSpacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_carrier_create_trip_cta),
                    style = PasabayanTextStyles.Body.medium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        PDivider()

        TipRow(
            color = Color(0xFF3B82F6),
            title = stringResource(R.string.dashboard_carrier_tip_demand_title),
            body = stringResource(R.string.dashboard_carrier_tip_demand_body),
        )
        TipRow(
            color = Color(0xFFF59E0B),
            title = stringResource(R.string.dashboard_carrier_tip_earning_title),
            body = stringResource(R.string.dashboard_carrier_tip_earning_body),
        )
        TipRow(
            color = Color(0xFF9EC5FF),
            title = stringResource(R.string.dashboard_carrier_tip_proactive_title),
            body = stringResource(R.string.dashboard_carrier_tip_proactive_body),
        )

        PCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2A8CFF),
                )
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.dashboard_carrier_card_tip_title),
                        style = PasabayanTextStyles.Body.medium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_carrier_card_tip_body),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TipRow(
    color: Color,
    title: String,
    body: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(top = 6.dp)
                .background(color, CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = PasabayanTextStyles.Caption.large,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "CarrierExplore — light", heightDp = 1200)
@Preview(showBackground = true, name = "CarrierExplore — dark", heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            UserHeaderCard(
                userName = "Bong Suyat",
                currentRole = UserRole.CARRIER,
                verificationLevel = "basic",
                avatarUrl = null,
                onSwitchRole = {},
            )
            Text(
                text = stringResource(R.string.dashboard_carrier_find_packages),
                style = PasabayanTextStyles.Heading.h4,
            )
            Text(
                text = stringResource(R.string.dashboard_carrier_find_packages_subtitle),
                style = PasabayanTextStyles.Body.small,
            )
            CarrierBrowseEmptyState(onPostTrip = {})
        }
    }
}
