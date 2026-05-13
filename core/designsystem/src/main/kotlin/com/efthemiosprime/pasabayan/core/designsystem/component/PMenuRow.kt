package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Tappable row used in profile and settings menus. iOS parity with `ProfileMenuItem`:
 * leading icon (optional, primary tint, 24 dp) → title + optional subtitle → optional
 * trailing slot → optional red unread-count badge → trailing chevron.
 *
 * For richer trailing content (e.g. an inline rating preview) use [trailing]; for a
 * simple unread count use [badgeCount]. They can coexist — [trailing] renders before
 * the badge.
 */
@Composable
fun PMenuRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    badgeCount: Int = 0,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = leadingIconTint,
                modifier = Modifier.size(LeadingIconSize),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
        if (badgeCount > 0) {
            PNotificationBadge(count = badgeCount)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val LeadingIconSize = 24.dp

@Preview(showBackground = true, name = "PMenuRow — light")
@Preview(showBackground = true, name = "PMenuRow — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PMenuRowPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PMenuRow(title = "Title only", onClick = {})
            PMenuRow(
                title = "With leading icon",
                onClick = {},
                leadingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            )
            PMenuRow(
                title = "Personal info",
                subtitle = "Update your personal information",
                onClick = {},
                leadingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            )
            PMenuRow(
                title = "Verification",
                subtitle = "Verify phone and identity",
                onClick = {},
                leadingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                badgeCount = 1,
            )
            PMenuRow(
                title = "My Ratings",
                subtitle = null,
                onClick = {},
                leadingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                trailing = {
                    Text(
                        text = "4.87 ★ (26)",
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}
