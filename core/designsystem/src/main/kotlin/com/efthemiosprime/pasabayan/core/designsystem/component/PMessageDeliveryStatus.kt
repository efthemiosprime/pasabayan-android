package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

enum class PMessageDeliveryState {
    Sent,
    Delivered,
    Read,
    ReadByMe,
    Unread,
}

@Composable
fun PMessageDeliveryStatus(
    label: String,
    state: PMessageDeliveryState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconForState(state),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun iconForState(state: PMessageDeliveryState): ImageVector =
    when (state) {
        PMessageDeliveryState.Sent -> Icons.Filled.Check
        PMessageDeliveryState.Delivered -> Icons.Filled.CheckCircle
        PMessageDeliveryState.Read, PMessageDeliveryState.ReadByMe -> Icons.Filled.CheckCircle
        PMessageDeliveryState.Unread -> Icons.Filled.Check
    }

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PMessageDeliveryStatusPreview() {
    PasabayanTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            PMessageDeliveryStatus(label = "Sent", state = PMessageDeliveryState.Sent)
            PMessageDeliveryStatus(label = "Delivered", state = PMessageDeliveryState.Delivered)
            PMessageDeliveryStatus(label = "Read", state = PMessageDeliveryState.Read)
        }
    }
}

