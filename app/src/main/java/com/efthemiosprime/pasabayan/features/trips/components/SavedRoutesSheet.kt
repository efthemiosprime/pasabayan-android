package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRoutesSheet(
    routes: List<SavedRouteTemplate>,
    onSelectRoute: (SavedRouteTemplate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.trips_create_saved_routes_title),
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (routes.isEmpty()) {
                Text(
                    text = stringResource(R.string.trips_create_saved_routes_empty),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn {
                    items(routes) { route ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectRoute(route) }
                                .padding(vertical = PasabayanSpacing.sm),
                        ) {
                            Text(
                                text = route.displayLabel,
                                style = PasabayanTextStyles.Body.medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            route.pickupAddress?.let {
                                Text(
                                    text = it,
                                    style = PasabayanTextStyles.Caption.regular,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        PDivider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "SavedRoutes — light")
@Preview(showBackground = true, name = "SavedRoutes — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SavedRoutesPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.lg)) {
            Text(stringResource(R.string.trips_create_saved_routes_title))
        }
    }
}
