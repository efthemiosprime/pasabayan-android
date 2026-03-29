package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanMotion
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.R

/**
 * State that tells the host screen whether a card is expanded.
 * When expanded, the host hides its scaffold (top bar, bottom nav, list)
 * and renders the expanded card content fullscreen instead.
 */
class ExpandedCardState {
    var content: (@Composable () -> Unit)? by mutableStateOf(null)
        internal set

    val isExpanded: Boolean get() = content != null

    internal fun show(block: @Composable () -> Unit) {
        content = block
    }

    fun dismiss() {
        content = null
    }
}

val LocalExpandedCardState = compositionLocalOf { ExpandedCardState() }

/**
 * Wrap your screen root with this. It provides [LocalExpandedCardState] and
 * swaps between normal content and expanded card content.
 *
 * When a card is expanded:
 *  - [content] (scaffold, tabs, list) is NOT composed
 *  - The expanded card fills the screen with close button + back handler
 */
@Composable
fun PExpandableCardHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = remember { ExpandedCardState() }

    val durationMs = PasabayanMotion.MEDIUM_MS

    CompositionLocalProvider(LocalExpandedCardState provides state) {
        AnimatedContent(
            targetState = state.isExpanded,
            modifier = modifier,
            transitionSpec = {
                if (targetState) {
                    // Expanding: scale up + fade in
                    (scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(durationMs),
                    ) + fadeIn(tween(durationMs))) togetherWith
                        fadeOut(tween(durationMs / 2)) using
                        SizeTransform(clip = false)
                } else {
                    // Collapsing: fade in normal + scale down card
                    fadeIn(tween(durationMs)) togetherWith
                        (scaleOut(
                            targetScale = 0.92f,
                            animationSpec = tween(durationMs),
                        ) + fadeOut(tween(durationMs / 2))) using
                        SizeTransform(clip = false)
                }
            },
            label = "PExpandableCardHost",
        ) { isExpanded ->
            if (isExpanded) {
                // Card takes over — scaffold is gone
                BackHandler { state.dismiss() }

                val dragOffsetY = remember { Animatable(0f) }
                val dismissThresholdPx = with(LocalDensity.current) { 150.dp.toPx() }
                val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(PasabayanSpacing.xs)
                            .graphicsLayer {
                                translationY = dragOffsetY.value
                                // Scale down slightly as you drag
                                val progress = (dragOffsetY.value / dismissThresholdPx).coerceIn(0f, 1f)
                                scaleX = 1f - (progress * 0.05f)
                                scaleY = 1f - (progress * 0.05f)
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (dragOffsetY.value > dismissThresholdPx) {
                                            state.dismiss()
                                        } else {
                                            coroutineScope.launch {
                                                dragOffsetY.animateTo(0f, tween(200))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            dragOffsetY.animateTo(0f, tween(200))
                                        }
                                    },
                                    onVerticalDrag = { _, dragAmount ->
                                        // Only allow dragging down (positive)
                                        val newOffset = (dragOffsetY.value + dragAmount).coerceAtLeast(0f)
                                        coroutineScope.launch {
                                            dragOffsetY.snapTo(newOffset)
                                        }
                                    },
                                )
                            },
                        shape = RoundedCornerShape(PasabayanRadius.card),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(PasabayanBorder.width, PasabayanColors.Border),
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Close button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = PasabayanSpacing.sm, end = PasabayanSpacing.sm),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                IconButton(onClick = { state.dismiss() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.ds_close),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            // Expanded card content (scrollable)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(PasabayanSpacing.cardPadding),
                            ) {
                                state.content?.invoke()
                            }
                        }
                    }
                }
            } else {
                // Normal layout — scaffold, tabs, everything
                content()
            }
        }
    }
}

/**
 * Card that can expand to take over the screen.
 *
 * Collapsed: normal [PCard] with [collapsedContent].
 * Expanded: pushes [expandedContent] to [PExpandableCardHost] which
 * hides the scaffold and renders the card fullscreen.
 *
 * The card IS the screen when expanded — no overlay, no dialog.
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
    val expandedCardState = LocalExpandedCardState.current

    // Sync expand state with host
    androidx.compose.runtime.LaunchedEffect(expanded) {
        if (expanded) {
            expandedCardState.show {
                Column { expandedContent() }
            }
        } else if (expandedCardState.isExpanded) {
            expandedCardState.dismiss()
        }
    }

    // When host dismisses (back press / X), notify the card
    androidx.compose.runtime.LaunchedEffect(expandedCardState.isExpanded) {
        if (!expandedCardState.isExpanded && expanded) {
            onExpandChange(false)
        }
    }

    // Collapsed card in the list (only when not expanded)
    if (!expanded) {
        PCard(
            modifier = modifier,
            variant = variant,
            content = collapsedContent,
        )
    }
}

@Preview(showBackground = true, name = "PExpandableCard — light")
@Preview(showBackground = true, name = "PExpandableCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PExpandableCardPreview() {
    PasabayanTheme {
        var expanded by rememberSaveable { mutableStateOf(false) }
        PExpandableCardHost {
            PExpandableCard(
                expanded = expanded,
                onExpandChange = { expanded = it },
                modifier = Modifier.padding(PasabayanSpacing.lg),
                collapsedContent = {
                    Text("Trip: Toronto → Vancouver", style = PasabayanTextStyles.Heading.h6)
                    Text("Tap View Details to expand", style = PasabayanTextStyles.Body.small)
                },
                expandedContent = {
                    Text("Full trip details", style = PasabayanTextStyles.Heading.h4)
                    Text("Route, schedule, capacity, pricing...", style = PasabayanTextStyles.Body.regular)
                },
            )
        }
    }
}
