package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Non-blocking caution banner shown on match details when the package weight
 * exceeds the carrier's stated capacity. Per the advisory-weight policy this
 * is **informational** — the action button beneath remains tappable; the
 * accept call drives the "Accept Anyway?" confirmation.
 *
 * Reusable by construction: takes the two kg values + a modifier, no
 * ViewModel / domain types. The host composable decides when to render it
 * (typically `match.packageRequest.weightKg > match.carrierTrip.availableWeightKg`).
 */
@Composable
fun OverCapacityBanner(
    packageWeightKg: Double,
    availableWeightKg: Double,
    modifier: Modifier = Modifier,
) {
    val packageLabel = stringResource(R.string.matching_overage_kg_format, packageWeightKg)
    val availableLabel = stringResource(R.string.matching_overage_kg_format, availableWeightKg)
    // Warning.copy(alpha = ...) reads naturally on both light and dark schemes:
    // a low-alpha amber tint over the parent surface keeps the foreground
    // text legible without theme-conditional logic.
    val tint = PasabayanColors.Warning
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(OVER_CAPACITY_BANNER_TEST_TAG),
        shape = RoundedCornerShape(PasabayanRadius.card),
        color = tint.copy(alpha = 0.12f),
        border = BorderStroke(PasabayanBorder.width, tint.copy(alpha = 0.40f)),
    ) {
        Row(
            modifier = Modifier.padding(PasabayanSpacing.cardPadding),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(ICON_LG),
            )
            Spacer(Modifier.width(PasabayanSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = stringResource(R.string.matching_overage_banner_title),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.matching_overage_banner_body,
                        packageLabel,
                        availableLabel,
                    ),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val ICON_LG = PasabayanSpacing.xxl // 24.dp

internal const val OVER_CAPACITY_BANNER_TEST_TAG = "OverCapacity.Banner"

@Preview(showBackground = true, name = "OverCapacityBanner — light")
@Preview(showBackground = true, name = "OverCapacityBanner — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverCapacityBannerPreview() {
    PasabayanTheme {
        Box(Modifier.padding(PasabayanSpacing.lg)) {
            OverCapacityBanner(
                packageWeightKg = 2.0,
                availableWeightKg = 1.0,
            )
        }
    }
}

@Preview(showBackground = true, name = "OverCapacityBanner — large overage — light")
@Preview(showBackground = true, name = "OverCapacityBanner — large overage — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverCapacityBannerLargePreview() {
    PasabayanTheme {
        Box(Modifier.padding(PasabayanSpacing.lg)) {
            OverCapacityBanner(
                packageWeightKg = 12.5,
                availableWeightKg = 3.0,
            )
        }
    }
}
