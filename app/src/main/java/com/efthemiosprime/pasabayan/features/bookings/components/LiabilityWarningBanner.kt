package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Tap-to-open liability reminder banner shown on active booking detail
 * surfaces. Mirrors iOS `LiabilityWarningBanner.swift`.
 *
 * The full liability-waiver document (iOS' `LiabilityWaiverSheet`) is a
 * separate slice — this banner only carries the entry-point affordance.
 */
@Composable
fun LiabilityWarningBanner(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = if (isDark) AccentDark else AccentLight
    val openDescription = stringResource(R.string.bookings_liability_banner_open_cd)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PasabayanRadius.md))
            .background(accent.copy(alpha = if (isDark) 0.15f else 0.10f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (isDark) 0.50f else 0.30f),
                shape = RoundedCornerShape(PasabayanRadius.md),
            )
            .clickable(onClickLabel = openDescription, onClick = onTap)
            .padding(PasabayanSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.bookings_liability_banner_title),
                style = PasabayanTextStyles.Body.medium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.bookings_liability_banner_subtitle),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = accent,
        )
    }
}

/** iOS' `Color.orange`. Light theme — full saturation; dark — slightly lifted. */
private val AccentLight = Color(0xFFE07A1F) // warm amber
private val AccentDark = Color(0xFFFFB562)

private fun Color.luminance(): Float =
    0.2126f * red + 0.7152f * green + 0.0722f * blue

@Preview(showBackground = true, name = "LiabilityWarningBanner — light")
@Preview(showBackground = true, name = "LiabilityWarningBanner — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LiabilityWarningBannerPreview() {
    PasabayanTheme {
        LiabilityWarningBanner(
            onTap = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
