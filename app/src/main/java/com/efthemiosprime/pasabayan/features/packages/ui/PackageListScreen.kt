package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestCard
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel

@Composable
fun PackageListScreen(
    onViewPackageDetails: (packageId: Int) -> Unit,
    onCreatePackage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PackageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadPackages() }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
            state.hasLoadedPackages && state.packageRequests.isEmpty() -> {
                PEmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = stringResource(R.string.packages_empty_no_packages),
                    description = stringResource(R.string.packages_empty_no_packages_description),
                    modifier = Modifier.padding(PasabayanSpacing.lg),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    contentPadding = PaddingValues(
                        horizontal = PasabayanSpacing.screenPadding,
                        vertical = PasabayanSpacing.sm,
                    ),
                ) {
                    items(state.packageRequests, key = { it.id }) { pkg ->
                        PackageRequestCard(
                            pkg = pkg,
                            onViewDetails = { onViewPackageDetails(pkg.id) },
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onCreatePackage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(PasabayanSpacing.lg),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.packages_create_package),
            )
        }
    }
}

@Preview(showBackground = true, name = "PackageList — light", heightDp = 700)
@Preview(showBackground = true, name = "PackageList — dark", heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageListPreview() {
    PasabayanTheme {
        PEmptyState(
            icon = Icons.Outlined.Inventory2,
            title = "No packages yet",
            description = "Create a package request to find carriers.",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
