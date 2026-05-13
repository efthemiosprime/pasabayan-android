package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeConfig
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeVariant
import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus

/**
 * [StatusBadgeConfig] mapping for [PayoutStatus]. iOS parity: `PayoutStatus.color` /
 * `.icon` in `PaymentModels.swift`. Colours use `PasabayanColors.*` tokens — no hex literals.
 */
data class PayoutStatusBadgeConfig(
    private val status: PayoutStatus,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = label

    override val backgroundColor: Color
        get() = when (status) {
            PayoutStatus.PENDING -> PasabayanColors.BadgeGray
            PayoutStatus.PROCESSING -> PasabayanColors.Warning
            PayoutStatus.ON_HOLD -> PasabayanColors.BadgeAmber
            PayoutStatus.SCHEDULED -> PasabayanColors.Info
            PayoutStatus.COMPLETED -> PasabayanColors.Success
            PayoutStatus.FAILED -> PasabayanColors.Error
        }

    override val textColor: Color get() = backgroundColor

    override val icon: ImageVector
        get() = when (status) {
            PayoutStatus.PENDING -> Icons.Filled.HourglassEmpty
            PayoutStatus.PROCESSING -> Icons.Filled.Sync
            PayoutStatus.ON_HOLD -> Icons.Filled.PauseCircle
            PayoutStatus.SCHEDULED -> Icons.Filled.Schedule
            PayoutStatus.COMPLETED -> Icons.Filled.CheckCircle
            PayoutStatus.FAILED -> Icons.Filled.Error
        }
}

@Composable
fun PayoutStatusBadge(
    status: PayoutStatus,
    modifier: Modifier = Modifier,
    variant: StatusBadgeVariant = StatusBadgeVariant.Standard,
) {
    PStatusBadge(
        config = PayoutStatusBadgeConfig(status, payoutStatusLabel(status)),
        modifier = modifier,
        variant = variant,
    )
}

@Composable
fun payoutStatusLabel(status: PayoutStatus): String = stringResource(
    when (status) {
        PayoutStatus.PENDING -> R.string.payments_payout_state_pending
        PayoutStatus.PROCESSING -> R.string.payments_payout_state_processing
        PayoutStatus.ON_HOLD -> R.string.payments_payout_state_on_hold
        PayoutStatus.SCHEDULED -> R.string.payments_payout_state_scheduled
        PayoutStatus.COMPLETED -> R.string.payments_payout_state_completed
        PayoutStatus.FAILED -> R.string.payments_payout_state_failed
    },
)

@Preview(showBackground = true, name = "PayoutStatusBadge light")
@Preview(showBackground = true, name = "PayoutStatusBadge dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PayoutStatusBadgeAllPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            PayoutStatus.entries.forEach { status ->
                PayoutStatusBadge(status = status)
            }
        }
    }
}

@Preview(showBackground = true, name = "PayoutStatusBadge compact")
@Composable
private fun PayoutStatusBadgeCompactPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            PayoutStatus.entries.forEach { status ->
                PayoutStatusBadge(status = status, variant = StatusBadgeVariant.Compact)
            }
        }
    }
}
