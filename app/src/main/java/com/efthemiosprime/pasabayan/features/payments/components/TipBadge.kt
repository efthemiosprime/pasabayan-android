package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter

/**
 * Small badge surfacing a tip amount. Hidden (no UI emitted) when amount ≤ 0 — matches
 * iOS `TipBadge.swift` semantics. Tint via `PasabayanColors.BadgePink`.
 */
@Composable
fun TipBadge(
    amount: Double,
    modifier: Modifier = Modifier,
    currency: String = MoneyFormatter.DEFAULT_CURRENCY,
) {
    if (amount <= 0.0) return
    Row(
        modifier = modifier
            .background(PasabayanColors.BadgePinkLight, RoundedCornerShape(PasabayanRadius.badge))
            .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = PasabayanColors.BadgePink,
        )
        Text(
            text = MoneyFormatter.formatSignedCurrency(amount, currency),
            style = PasabayanTextStyles.Caption.regular,
            color = PasabayanColors.BadgePink,
        )
    }
}

@Preview(showBackground = true, name = "TipBadge light")
@Preview(showBackground = true, name = "TipBadge dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TipBadgePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            TipBadge(amount = 0.0) // hidden
            TipBadge(amount = 2.0)
            TipBadge(amount = 5.0)
            TipBadge(amount = 15.5)
            TipBadge(amount = 9.99, currency = "USD")
        }
    }
}
