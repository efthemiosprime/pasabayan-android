package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Small tappable presentation chip — label + optional secondary text + optional leading icon.
 * Capsule shape, surface background, 1 dp outline. iOS parity with `TopCarrierChip` and
 * `CarrierPopularRouteChip`. Use for compact, scrollable lists where a full card would
 * waste vertical space.
 *
 * For filter UI with selected state, use [PFilterChip] instead.
 */
@Composable
fun PChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: String? = null,
    leadingIcon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanRadius.capsule),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(PasabayanBorder.width, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PasabayanSpacing.md,
                vertical = PasabayanSpacing.xs,
            ),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 2.dp),
                )
            }
            Text(
                text = label,
                style = PasabayanTextStyles.Caption.large,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (secondary != null) {
                Text(
                    text = secondary,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "PChip — light")
@Preview(showBackground = true, name = "PChip — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PChipPreview() {
    PasabayanTheme {
        Row(
            modifier = Modifier.padding(PasabayanSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PChip(label = "Alex Carrier", secondary = "12 completed", onClick = {})
            PChip(label = "Montreal → Toronto", secondary = "8", onClick = {})
            PChip(label = "Plain", onClick = {})
        }
    }
}
