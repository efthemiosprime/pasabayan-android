package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

enum class PMessageBubbleStyle {
    Own,
    Other,
    System,
}

@Composable
fun PMessageBubble(
    style: PMessageBubbleStyle,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (style == PMessageBubbleStyle.System) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
        return
    }

    val alignment = if (style == PMessageBubbleStyle.Own) Alignment.End else Alignment.Start
    val containerColor = if (style == PMessageBubbleStyle.Own) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (alignment == Alignment.End) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(PasabayanRadius.md),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(PasabayanSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                content = content,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PMessageBubblePreview() {
    PasabayanTheme {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PMessageBubble(style = PMessageBubbleStyle.Other) {
                Text(text = "Hi there", color = MaterialTheme.colorScheme.onSurface)
            }
            PMessageBubble(style = PMessageBubbleStyle.Own) {
                Text(text = "On my way", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            PMessageBubble(style = PMessageBubbleStyle.System) {
                Text(text = "Delivery updated", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
