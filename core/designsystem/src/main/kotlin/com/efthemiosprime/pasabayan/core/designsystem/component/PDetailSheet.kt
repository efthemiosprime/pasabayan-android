package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Shared detail-sheet scaffold used by feature detail screens for consistent iOS parity styling.
 */
@Composable
fun PDetailSheetScaffold(
    title: String,
    closeContentDescription: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    // Was hardcoded #F2F2F7 — the light-mode value — which made every sheet render
    // a light-gray container in dark mode. Theme-driven default so sheets adapt
    // to dark + the inner PDetailSheetCard / PCard surfaces stay readable.
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentSpacing: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = closeContentDescription,
                    tint = PasabayanColors.Info,
                )
            }
        }
        content()
    }
}

@Composable
fun PDetailSheetCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.md),
            content = content,
        )
    }
}

@Composable
fun PDetailSectionTitle(text: String) {
    Text(
        text = text,
        style = PasabayanTextStyles.Heading.h4,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
}

@Preview(showBackground = true, name = "PDetailSheet - light", heightDp = 760)
@Preview(
    showBackground = true,
    name = "PDetailSheet - dark",
    heightDp = 760,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PDetailSheetPreview() {
    PasabayanTheme {
        PDetailSheetScaffold(
            title = "Trip Details",
            closeContentDescription = "Close details",
            onClose = {},
        ) {
            PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
                PDetailSectionTitle(text = "Route Information")
                Text(
                    text = "Toronto, Canada -> Vancouver, Canada",
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
