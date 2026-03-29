package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

@Composable
fun CounterOfferBanner(
    counterOffererName: String,
    newPrice: Double,
    remainingOffers: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                PasabayanColors.BadgePurple.copy(alpha = 0.1f),
                RoundedCornerShape(PasabayanRadius.sm),
            )
            .padding(PasabayanSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$counterOffererName: ${stringResource(R.string.bookings_detail_counter_offer)}",
            style = PasabayanTextStyles.Body.medium,
            color = PasabayanColors.BadgePurple,
        )
        Text(
            text = String.format("$%.2f", newPrice),
            style = PasabayanTextStyles.Body.medium,
            color = PasabayanColors.BadgePurple,
        )
    }
}

@Preview(showBackground = true, name = "CounterOfferBanner — light")
@Preview(showBackground = true, name = "CounterOfferBanner — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferBannerPreview() {
    PasabayanTheme {
        CounterOfferBanner(
            counterOffererName = "Alice",
            newPrice = 135.0,
            remainingOffers = 2,
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
