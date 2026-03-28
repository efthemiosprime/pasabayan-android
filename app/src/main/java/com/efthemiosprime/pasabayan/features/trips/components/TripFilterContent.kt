package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter

/**
 * Content for the trip filter bottom sheet.
 * Search text, origin/destination, and action buttons.
 */
@Composable
fun TripFilterContent(
    filter: TripFilter,
    onSearchChange: (String) -> Unit,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        POutlinedTextField(
            value = filter.searchText,
            onValueChange = onSearchChange,
            label = { Text(stringResource(R.string.trips_browse_search)) },
            modifier = Modifier.fillMaxWidth(),
        )
        POutlinedTextField(
            value = filter.origin,
            onValueChange = onOriginChange,
            label = { Text(stringResource(R.string.trips_create_origin)) },
            modifier = Modifier.fillMaxWidth(),
        )
        POutlinedTextField(
            value = filter.destination,
            onValueChange = onDestinationChange,
            label = { Text(stringResource(R.string.trips_create_destination)) },
            modifier = Modifier.fillMaxWidth(),
        )
        PButton(
            text = stringResource(R.string.trips_browse_title),
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
        )
        PButton(
            text = stringResource(R.string.trips_filter_all),
            onClick = onClear,
            style = PButtonStyle.Tertiary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, name = "TripFilter — light")
@Preview(showBackground = true, name = "TripFilter — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripFilterContentPreview() {
    PasabayanTheme {
        TripFilterContent(
            filter = TripFilter(searchText = "Montreal"),
            onSearchChange = {},
            onOriginChange = {},
            onDestinationChange = {},
            onApply = {},
            onClear = {},
        )
    }
}
