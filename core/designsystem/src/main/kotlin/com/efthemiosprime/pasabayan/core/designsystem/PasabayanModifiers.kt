package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dsSpacingXS(): Modifier = padding(PasabayanSpacing.xs)

fun Modifier.dsSpacingSM(): Modifier = padding(PasabayanSpacing.sm)

fun Modifier.dsSpacingMD(): Modifier = padding(PasabayanSpacing.md)

fun Modifier.dsSpacingLG(): Modifier = padding(PasabayanSpacing.lg)

fun Modifier.dsScreenPaddingHorizontal(): Modifier =
    padding(horizontal = PasabayanSpacing.screenPadding)

@Composable
fun Modifier.dsCardStyle(
    radius: Dp = PasabayanRadius.card,
    contentPadding: Dp = PasabayanSpacing.cardPadding,
): Modifier {
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(radius)
    return this
        .background(surface, shape)
        .border(PasabayanBorder.width, PasabayanColors.Border, shape)
        .padding(contentPadding)
}

@Composable
fun Modifier.dsCardPrimary(): Modifier = dsCardStyle(radius = PasabayanRadius.card)

@Composable
fun Modifier.dsCardSecondary(): Modifier = dsCardStyle(radius = PasabayanRadius.sm)

@Composable
fun Modifier.dsCardCompact(): Modifier = dsCardStyle(radius = PasabayanRadius.xs)

@Composable
fun Modifier.dsCardLarge(): Modifier = dsCardStyle(radius = PasabayanRadius.lg)

/** Subtle button shadow — iOS `DesignSystem.Shadow.button` (~sm). */
fun Modifier.dsShadowButton(shape: RoundedCornerShape): Modifier =
    shadow(
        elevation = 2.dp,
        shape = shape,
        spotColor = Color.Black.copy(alpha = 0.18f),
        ambientColor = Color.Black.copy(alpha = 0.08f),
    )

/** iOS onboarding `RoleCard` / `CompletionActionCard` (~black 4% / radius 8). */
fun Modifier.dsShadowOnboardingCard(shape: RoundedCornerShape): Modifier =
    shadow(
        elevation = 2.dp,
        shape = shape,
        spotColor = Color.Black.copy(alpha = 0.04f),
        ambientColor = Color.Black.copy(alpha = 0.04f),
    )
