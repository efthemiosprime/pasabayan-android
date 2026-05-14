package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * Tap-to-open card that surfaces an in-app chat with the other party.
 * Mirrors iOS `ChatAccessCard.swift`.
 *
 * Stateless: takes the other party's display name + an `onTap` callback. The
 * host wires this to the chat-conversation navigation.
 */
@Composable
fun ChatAccessCard(
    otherPartyName: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDescription = stringResource(R.string.bookings_chat_access_open_cd, otherPartyName)
    PCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = openDescription, onClick = onTap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = PasabayanColors.Info,
                modifier = Modifier.size(28.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.bookings_chat_access_message, otherPartyName),
                    style = PasabayanTextStyles.Body.medium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.bookings_chat_access_subtitle),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "ChatAccessCard — light")
@Preview(showBackground = true, name = "ChatAccessCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatAccessCardPreview() {
    PasabayanTheme {
        ChatAccessCard(
            otherPartyName = "Joy Marie Blanco",
            onTap = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
