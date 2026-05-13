package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles

/**
 * Compact carrier active/inactive pill — iOS parity with `CarrierStatusBadge`.
 * Used on the profile header (top-right corner) when the user is in carrier role.
 */
@Composable
fun CarrierActiveBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, label) = if (isActive) {
        Triple(
            PasabayanColors.Success.copy(alpha = 0.15f),
            PasabayanColors.Success,
            stringResource(R.string.profile_carrier_status_active),
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.profile_carrier_status_inactive),
        )
    }
    Text(
        text = label,
        style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.Medium),
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(PasabayanRadius.sm))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 3.dp),
    )
}
