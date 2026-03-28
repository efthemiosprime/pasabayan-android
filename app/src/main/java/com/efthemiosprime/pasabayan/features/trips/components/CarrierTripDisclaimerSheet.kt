package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierTripDisclaimerSheet(
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.trips_disclaimer_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.trips_disclaimer_body),
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PButton(
                text = stringResource(R.string.trips_disclaimer_acknowledge),
                onClick = onAcknowledge,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Disclaimer — light")
@Preview(showBackground = true, name = "Disclaimer — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DisclaimerPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.lg)) {
            Text("Disclaimer sheet would appear here")
        }
    }
}
