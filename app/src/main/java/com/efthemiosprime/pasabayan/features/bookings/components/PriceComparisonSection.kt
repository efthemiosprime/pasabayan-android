package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow

@Composable
fun PriceComparisonSection(
    agreedPrice: Double,
    originalPrice: Double?,
    isCounterOffer: Boolean,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailRow(
                label = stringResource(R.string.bookings_detail_agreed_price),
                value = String.format(Locale.ROOT, "$%.2f", agreedPrice),
            )
            if (originalPrice != null && isCounterOffer) {
                PDetailRow(
                    label = stringResource(R.string.bookings_detail_original_price),
                    value = String.format(Locale.ROOT, "$%.2f", originalPrice),
                )
                val diff = agreedPrice - originalPrice
                val color = if (diff > 0) PasabayanColors.Success else PasabayanColors.Error
                Text(
                    text = if (diff > 0) {
                        stringResource(R.string.bookings_counter_offer_price_increase, diff)
                    } else {
                        stringResource(R.string.bookings_counter_offer_price_decrease, -diff)
                    },
                    style = PasabayanTextStyles.Caption.large,
                    color = color,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "PriceComparison — light")
@Preview(showBackground = true, name = "PriceComparison — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PriceComparisonPreview() {
    PasabayanTheme {
        PriceComparisonSection(
            agreedPrice = 135.0,
            originalPrice = 150.0,
            isCounterOffer = true,
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
