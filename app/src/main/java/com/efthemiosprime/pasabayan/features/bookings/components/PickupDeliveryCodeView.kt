package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

@Composable
fun PickupCodeView(
    code: String,
    expiresAt: String?,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.bookings_detail_pickup_code),
                style = PasabayanTextStyles.Heading.h6,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = code,
                style = PasabayanTextStyles.Heading.h2,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            expiresAt?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun DeliveryCodeView(
    code: String,
    expiresAt: String?,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.bookings_detail_delivery_code),
                style = PasabayanTextStyles.Heading.h6,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = code,
                style = PasabayanTextStyles.Heading.h2,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            expiresAt?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "PickupCode — light")
@Preview(showBackground = true, name = "PickupCode — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PickupCodePreview() {
    PasabayanTheme {
        PickupCodeView(
            code = "123456",
            expiresAt = "Expires: Apr 1, 2026 6:00 PM",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
