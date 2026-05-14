package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle

/**
 * Banner shown when a match's auto-charge has failed and the shipper needs to
 * retry or update their card. Mirrors iOS `AutoChargeFailureBanner.swift`.
 *
 * Stateless: caller supplies callbacks and an `isRetrying` flag (which keeps
 * the spinner in sync with the parent's retry state instead of running its
 * own timer like the iOS version).
 *
 * The compact `AutoChargeFailureBadge` (iOS' list-row variant) is deferred —
 * no Android caller needs it yet.
 */
@Composable
fun AutoChargeFailureBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onUpdateCard: (() -> Unit)? = null,
    isRetrying: Boolean = false,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = if (isDark) AccentDark else AccentLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PasabayanRadius.md))
            .background(accent.copy(alpha = if (isDark) 0.15f else 0.10f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (isDark) 0.50f else 0.30f),
                shape = RoundedCornerShape(PasabayanRadius.md),
            )
            .padding(PasabayanSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.bookings_auto_charge_failure_title),
                    style = PasabayanTextStyles.Heading.h6,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.bookings_auto_charge_failure_description),
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onUpdateCard?.let { handler ->
                PButton(
                    text = stringResource(R.string.bookings_auto_charge_failure_update_card),
                    onClick = handler,
                    style = PButtonStyle.Secondary,
                    size = PButtonSize.Small,
                    icon = Icons.Default.CreditCard,
                )
            }
            PButton(
                text = if (isRetrying) {
                    stringResource(R.string.bookings_auto_charge_failure_retrying)
                } else {
                    stringResource(R.string.bookings_auto_charge_failure_retry)
                },
                onClick = onRetry,
                size = PButtonSize.Small,
                isLoading = isRetrying,
                enabled = !isRetrying,
            )
        }
    }
}

private val AccentLight = Color(0xFFE07A1F)
private val AccentDark = Color(0xFFFFB562)

private fun Color.luminance(): Float =
    0.2126f * red + 0.7152f * green + 0.0722f * blue

@Preview(showBackground = true, name = "AutoChargeFailure — light")
@Preview(showBackground = true, name = "AutoChargeFailure — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeFailureBannerPreview() {
    PasabayanTheme {
        AutoChargeFailureBanner(
            onRetry = {},
            onUpdateCard = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "AutoChargeFailure — retrying")
@Composable
private fun AutoChargeFailureBannerRetryingPreview() {
    PasabayanTheme {
        AutoChargeFailureBanner(
            onRetry = {},
            onUpdateCard = {},
            isRetrying = true,
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "AutoChargeFailure — no update-card action")
@Composable
private fun AutoChargeFailureBannerNoUpdateCardPreview() {
    PasabayanTheme {
        AutoChargeFailureBanner(
            onRetry = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
