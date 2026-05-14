package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles

/**
 * Origin → Destination display with dotted connector line.
 * Used across trip cards, package cards, and booking route sections.
 */
@Composable
fun PRouteSection(
    origin: String,
    destination: String,
    modifier: Modifier = Modifier,
    originAddress: String? = null,
    destinationAddress: String? = null,
    originLandmark: String? = null,
    destinationLandmark: String? = null,
    originCaption: String? = null,
    destinationCaption: String? = null,
    accentColor: Color = PasabayanColors.Info,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        // Dots + connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp),
        ) {
            // Origin dot
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = accentColor)
            }
            // Dashed connector
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .height(
                        if (hasSubtext(
                                originAddress,
                                originLandmark,
                                destinationAddress,
                                destinationLandmark,
                                originCaption,
                                destinationCaption,
                            )
                        ) 60.dp else 32.dp,
                    ),
            ) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                drawLine(
                    color = accentColor.copy(alpha = 0.4f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2f,
                    pathEffect = pathEffect,
                )
            }
            // Destination dot
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = accentColor)
            }
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            // Origin
            originCaption?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = origin,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            originAddress?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            originLandmark?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Destination
            destinationCaption?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = destination,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            destinationAddress?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            destinationLandmark?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun hasSubtext(vararg values: String?): Boolean = values.any { it != null }
