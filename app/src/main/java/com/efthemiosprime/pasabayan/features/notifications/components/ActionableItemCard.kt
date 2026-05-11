package com.efthemiosprime.pasabayan.features.notifications.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PUnreadDot
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType

/**
 * Locally-computed "Action Required" entry. Used by section 2 of the comprehensive
 * notifications screen.
 */
@Composable
fun ActionableItemCard(
    type: ActionableItemType,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showUnreadDot: Boolean = true,
) {
    PCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(type.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    tint = type.color,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = title,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            if (showUnreadDot) PUnreadDot(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(name = "ActionableItemCard light", showBackground = true)
@Preview(name = "ActionableItemCard dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActionableItemCardPreview() {
    PasabayanTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            modifier = Modifier.padding(PasabayanSpacing.md),
        ) {
            ActionableItemCard(
                type = ActionableItemType.BOOKING_REQUEST,
                title = "Pending booking request",
                description = "Alice wants to ship a package from Toronto to Montreal",
                onClick = {},
            )
            ActionableItemCard(
                type = ActionableItemType.UPGRADE,
                title = "Premium upgrade",
                description = "Get higher-value matches and a verified badge",
                onClick = {},
            )
        }
    }
}
