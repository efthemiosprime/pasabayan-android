package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanLayout
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.dsShadowButton

enum class PButtonStyle {
    Primary,
    Secondary,
    Tertiary,
    Destructive,
    Filter,
    Submit,
}

enum class PButtonSize {
    Small,
    Medium,
    Large,
}

enum class PIconPosition {
    Leading,
    Trailing,
    Only,
}

/** Design-system button — parity with iOS `PButton`. Use under [PasabayanTheme]. */
@Composable
fun PButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PButtonStyle = PButtonStyle.Primary,
    size: PButtonSize = PButtonSize.Medium,
    icon: ImageVector? = null,
    iconPosition: PIconPosition = PIconPosition.Leading,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    count: Int? = null,
    isSelected: Boolean = false,
) {
    val resolvedSize = if (style == PButtonStyle.Submit) PButtonSize.Large else size
    val interactionSource = remember { MutableInteractionSource() }
    val effectiveEnabled = enabled && !isLoading

    val corner = when (resolvedSize) {
        PButtonSize.Small -> PasabayanRadius.sm
        PButtonSize.Medium -> PasabayanRadius.md
        PButtonSize.Large -> PasabayanRadius.lg
    }
    val shape = if (style == PButtonStyle.Filter) {
        RoundedCornerShape(PasabayanRadius.capsule)
    } else {
        RoundedCornerShape(corner)
    }

    val (paddingH, paddingV) = when (resolvedSize) {
        PButtonSize.Small -> PasabayanSpacing.lg to PasabayanSpacing.sm
        PButtonSize.Medium -> PasabayanSpacing.xl to PasabayanSpacing.md
        PButtonSize.Large -> PasabayanSpacing.xxl to PasabayanSpacing.lg
    }

    val colorScheme = MaterialTheme.colorScheme
    val isPrimaryLike = style == PButtonStyle.Primary || style == PButtonStyle.Submit
    val backgroundColor = when {
        !effectiveEnabled -> PasabayanColors.ButtonDisabled
        style == PButtonStyle.Filter -> if (isSelected) PasabayanColors.PrimaryBlack else colorScheme.surfaceVariant
        isPrimaryLike -> PasabayanColors.PrimaryBlack
        else -> Color.Transparent
    }

    val contentColor = when {
        !effectiveEnabled -> colorScheme.onSurface.copy(alpha = 0.38f)
        style == PButtonStyle.Destructive -> colorScheme.error
        style == PButtonStyle.Filter -> if (isSelected) Color.White else colorScheme.onBackground
        isPrimaryLike -> Color.White
        else -> colorScheme.onBackground
    }

    val showBorder = style == PButtonStyle.Secondary && effectiveEnabled
    val showShadow = effectiveEnabled &&
        style != PButtonStyle.Tertiary &&
        (isPrimaryLike || style == PButtonStyle.Secondary || style == PButtonStyle.Destructive ||
            (style == PButtonStyle.Filter && isSelected))

    val textStyle = when (resolvedSize) {
        PButtonSize.Small -> PasabayanTextStyles.Button.small
        PButtonSize.Medium -> PasabayanTextStyles.Button.medium
        PButtonSize.Large -> PasabayanTextStyles.Button.large
    }
    val fontWeight = when (style) {
        PButtonStyle.Primary, PButtonStyle.Submit -> FontWeight.SemiBold
        PButtonStyle.Secondary, PButtonStyle.Tertiary, PButtonStyle.Destructive -> FontWeight.Medium
        PButtonStyle.Filter -> if (isSelected) FontWeight.SemiBold else FontWeight.Medium
    }

    val minHeight = when (resolvedSize) {
        PButtonSize.Small -> PasabayanLayout.buttonHeightSmall
        PButtonSize.Medium -> PasabayanLayout.buttonHeightMedium
        PButtonSize.Large -> PasabayanLayout.buttonHeightLarge
    }

    val rowModifier = modifier
        .then(if (style == PButtonStyle.Submit) Modifier.fillMaxWidth() else Modifier)
        .then(if (showShadow) Modifier.dsShadowButton(shape) else Modifier)
        .clip(shape)
        .background(backgroundColor, shape)
        .then(
            if (showBorder) {
                Modifier.border(PasabayanBorder.width, colorScheme.outline, shape)
            } else {
                Modifier
            },
        )
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = contentColor.copy(alpha = 0.25f)),
            enabled = effectiveEnabled,
            role = Role.Button,
            onClick = onClick,
        )
        .heightIn(min = minHeight)
        .padding(horizontal = paddingH, vertical = paddingV)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val leadingLoading = isLoading && (iconPosition == PIconPosition.Leading || iconPosition == PIconPosition.Only)
        val trailingLoading = isLoading && iconPosition == PIconPosition.Trailing

        when {
            leadingLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = contentColor,
                    strokeWidth = 2.dp,
                )
            }
            icon != null && iconPosition == PIconPosition.Leading -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor,
                )
            }
        }

        if (iconPosition != PIconPosition.Only) {
            if (leadingLoading || (icon != null && iconPosition == PIconPosition.Leading)) {
                Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Text(
                    text = text,
                    style = textStyle.copy(fontWeight = fontWeight),
                    color = contentColor,
                )
                if (style == PButtonStyle.Filter && count != null) {
                    Text(
                        text = "($count)",
                        style = PasabayanTextStyles.Caption.regular,
                        color = contentColor,
                    )
                }
            }
        } else if (icon != null && !leadingLoading) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(22.dp),
                tint = contentColor,
            )
        }

        if (trailingLoading) {
            Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else if (icon != null && iconPosition == PIconPosition.Trailing && !isLoading) {
            Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor,
            )
        }
    }
}
