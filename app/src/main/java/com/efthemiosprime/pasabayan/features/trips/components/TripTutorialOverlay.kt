package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * One-time tutorial overlay for new carriers.
 * Dismissed state stored in SharedPreferences: `trip_tutorial_shown_v1_{userId}`.
 */
@Composable
fun TripTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PasabayanColors.OverlayScrim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        PCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.xxl),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.trips_tutorial_title),
                    style = PasabayanTextStyles.Heading.h4,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.trips_tutorial_body),
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                PButton(
                    text = stringResource(R.string.trips_tutorial_dismiss),
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Tutorial — light")
@Preview(showBackground = true, name = "Tutorial — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TutorialPreview() {
    PasabayanTheme {
        TripTutorialOverlay(onDismiss = {})
    }
}
