package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.features.trips.components.TripFilterContent
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TripFilterSheet(
    filter: TripFilter,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        PDetailSheetScaffold(
            title = androidx.compose.ui.res.stringResource(R.string.trips_filter_sheet_title),
            closeContentDescription = androidx.compose.ui.res.stringResource(R.string.trips_detail_close),
            onClose = onDismiss,
        ) {
            TripFilterContent(
                filter = filter,
                onSearchChange = onSearchChange,
                onOriginChange = onOriginChange,
                onDestinationChange = onDestinationChange,
                onApply = onApply,
                onClear = onClear,
            )
        }
    }
}

@Preview(showBackground = true, name = "TripFilterSheet — light")
@Preview(showBackground = true, name = "TripFilterSheet — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripFilterSheetPreview() {
    PasabayanTheme {
        TripFilterSheet(
            filter = TripFilter(searchText = "Montreal", origin = "Toronto", destination = "Montreal"),
            onDismiss = {},
            onSearchChange = {},
            onOriginChange = {},
            onDestinationChange = {},
            onApply = {},
            onClear = {},
        )
    }
}
