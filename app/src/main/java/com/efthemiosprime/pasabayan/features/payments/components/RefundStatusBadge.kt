package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Verified
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus

/**
 * [StatusBadgeConfig] mapping for [RefundStatus]. Colours use `PasabayanColors.*` tokens — no hex.
 */
data class RefundStatusBadgeConfig(
    private val status: RefundStatus,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = label

    override val backgroundColor: Color
        get() = when (status) {
            RefundStatus.PENDING -> PasabayanColors.Warning
            RefundStatus.APPROVED -> PasabayanColors.Info
            RefundStatus.REJECTED -> PasabayanColors.Error
            RefundStatus.PROCESSED -> PasabayanColors.Success
        }

    override val textColor: Color get() = backgroundColor

    override val icon: ImageVector
        get() = when (status) {
            RefundStatus.PENDING -> Icons.Filled.HourglassEmpty
            RefundStatus.APPROVED -> Icons.Filled.Verified
            RefundStatus.REJECTED -> Icons.Filled.Cancel
            RefundStatus.PROCESSED -> Icons.Filled.CheckCircle
        }
}

@Composable
fun RefundStatusBadge(
    status: RefundStatus,
    modifier: Modifier = Modifier,
    variant: StatusBadgeVariant = StatusBadgeVariant.Standard,
) {
    PStatusBadge(
        config = RefundStatusBadgeConfig(status, refundStatusLabel(status)),
        modifier = modifier,
        variant = variant,
    )
}

@Composable
fun refundStatusLabel(status: RefundStatus): String = stringResource(
    when (status) {
        RefundStatus.PENDING -> R.string.payments_refund_state_pending
        RefundStatus.APPROVED -> R.string.payments_refund_state_approved
        RefundStatus.REJECTED -> R.string.payments_refund_state_rejected
        RefundStatus.PROCESSED -> R.string.payments_refund_state_processed
    },
)

@Preview(showBackground = true, name = "RefundStatusBadge light")
@Preview(showBackground = true, name = "RefundStatusBadge dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RefundStatusBadgeAllPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            RefundStatus.entries.forEach { status ->
                RefundStatusBadge(status = status)
            }
        }
    }
}

@Preview(showBackground = true, name = "RefundStatusBadge compact")
@Composable
private fun RefundStatusBadgeCompactPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            RefundStatus.entries.forEach { status ->
                RefundStatusBadge(status = status, variant = StatusBadgeVariant.Compact)
            }
        }
    }
}
