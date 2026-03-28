package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
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

        // Available packages section
        Text(
            text = stringResource(R.string.dashboard_carrier_available_packages),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )

        when {
            state.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
            state.packageRequests.isEmpty() -> {
                PEmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = stringResource(R.string.packages_empty_no_available),
                    description = stringResource(R.string.packages_empty_no_available_description),
                )
                PButton(
                    text = stringResource(R.string.dashboard_carrier_post_trip),
                    onClick = { /* TODO: switch to My Trips tab */ },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                state.packageRequests.forEach { pkg ->
                    PackageRequestCard(
                        pkg = pkg,
                        onViewDetails = { /* TODO: open detail */ },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "CarrierExplore — light", heightDp = 900)
@Preview(showBackground = true, name = "CarrierExplore — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            Text("Find Packages", style = PasabayanTextStyles.Heading.h4)
            PEmptyState(
                icon = Icons.Outlined.Inventory2,
                title = "No available packages",
                description = "Try adjusting your search.",
            )
        }
    }
}
