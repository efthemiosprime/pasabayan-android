package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.R

/**
 * Card that can expand in-place to a fullscreen takeover showing detail content.
 *
 * In collapsed state, renders [collapsedContent] inside a normal [PCard].
 * When [expanded] is true, renders a fullscreen dialog with the same card surface
 * containing [expandedContent] — giving the visual effect of the card taking over.
 *
 * Usage:
 * ```
 * var expanded by remember { mutableStateOf(false) }
 * PExpandableCard(
 *     expanded = expanded,
 *     onExpandChange = { expanded = it },
 *     collapsedContent = { TripCardContent(...) },
 *     expandedContent = { TripDetailContent(...) },
 * )
 * ```
 */
@Composable
fun PExpandableCard(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    variant: PCardVariant = PCardVariant.Primary,
    collapsedContent: @Composable ColumnScope.() -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    // Collapsed card — always rendered in the list
    PCard(
        modifier = modifier,
        variant = variant,
        content = collapsedContent,
    )

    // Expanded fullscreen takeover
    if (expanded) {
        Dialog(
            onDismissRequest = { onExpandChange(false) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(0.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Close button row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = PasabayanSpacing.sm,
                                top = PasabayanSpacing.sm,
                                end = PasabayanSpacing.sm,
                            ),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        IconButton(onClick = { onExpandChange(false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.ds_close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Expanded content inside same card styling
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(PasabayanSpacing.cardPadding),
                    ) {
                        expandedContent()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "PExpandableCard collapsed — light")
@Preview(showBackground = true, name = "PExpandableCard collapsed — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PExpandableCardCollapsedPreview() {
    PasabayanTheme {
        var expanded by rememberSaveable { mutableStateOf(false) }
        PExpandableCard(
            expanded = expanded,
            onExpandChange = { expanded = it },
            modifier = Modifier.padding(PasabayanSpacing.lg),
            collapsedContent = {
                Text("Trip: Toronto → Vancouver", style = PasabayanTextStyles.Heading.h6)
                Text("Tap View Details to expand", style = PasabayanTextStyles.Body.small)
            },
            expandedContent = {
                Text("Full trip details here", style = PasabayanTextStyles.Heading.h4)
                Text("Route, schedule, capacity, pricing...", style = PasabayanTextStyles.Body.regular)
            },
        )
    }
}
