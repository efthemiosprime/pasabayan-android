package com.efthemiosprime.pasabayan.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

enum class PButtonStyle {
    Primary,
    Secondary,
    Tertiary,
    Destructive,
    Filter,
    Submit
}

enum class PButtonSize {
    Small,
    Medium,
    Large
}

/**
 * PButton - Universal button component following Pasabayan Design System
 */
@Composable
fun PButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PButtonStyle = PButtonStyle.Primary,
    size: PButtonSize = PButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    count: Int? = null
) {
    val buttonColors = when (style) {
        PButtonStyle.Primary, PButtonStyle.Submit -> ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: PasabayanDesignSystem.Colors.primary,
            contentColor = textColor ?: PasabayanDesignSystem.Colors.onPrimary,
            disabledContainerColor = PasabayanDesignSystem.Colors.secondary.copy(alpha = 0.5f),
            disabledContentColor = PasabayanDesignSystem.Colors.onSecondary.copy(alpha = 0.5f)
        )
        PButtonStyle.Secondary -> ButtonDefaults.outlinedButtonColors(
            contentColor = textColor ?: PasabayanDesignSystem.Colors.primary,
            disabledContentColor = PasabayanDesignSystem.Colors.onSecondary.copy(alpha = 0.5f)
        )
        PButtonStyle.Tertiary -> ButtonDefaults.textButtonColors(
            contentColor = textColor ?: PasabayanDesignSystem.Colors.primary,
            disabledContentColor = PasabayanDesignSystem.Colors.onSecondary.copy(alpha = 0.5f)
        )
        PButtonStyle.Destructive -> ButtonDefaults.buttonColors(
            containerColor = PasabayanDesignSystem.Colors.error,
            contentColor = Color.White,
            disabledContainerColor = PasabayanDesignSystem.Colors.error.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
        PButtonStyle.Filter -> ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor ?: PasabayanDesignSystem.Colors.surfaceVariant,
            contentColor = textColor ?: PasabayanDesignSystem.Colors.onSurface,
            disabledContainerColor = PasabayanDesignSystem.Colors.surfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = PasabayanDesignSystem.Colors.onSurface.copy(alpha = 0.5f)
        )
    }
    
    val buttonPadding = when (size) {
        PButtonSize.Small -> PaddingValues(
            horizontal = PasabayanDesignSystem.Spacing.lg,
            vertical = PasabayanDesignSystem.Spacing.sm
        )
        PButtonSize.Medium -> PaddingValues(
            horizontal = PasabayanDesignSystem.Spacing.xl,
            vertical = PasabayanDesignSystem.Spacing.md
        )
        PButtonSize.Large -> PaddingValues(
            horizontal = PasabayanDesignSystem.Spacing.xxl,
            vertical = PasabayanDesignSystem.Spacing.lg
        )
    }
    
    val textStyle = when (size) {
        PButtonSize.Small -> PasabayanDesignSystem.Typography.buttonSmall
        PButtonSize.Medium -> PasabayanDesignSystem.Typography.buttonMedium
        PButtonSize.Large -> PasabayanDesignSystem.Typography.buttonLarge
    }
    
    when (style) {
        PButtonStyle.Primary, PButtonStyle.Submit, PButtonStyle.Destructive -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                colors = buttonColors,
                contentPadding = buttonPadding,
                shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    count = count
                )
            }
        }
        PButtonStyle.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                colors = buttonColors,
                contentPadding = buttonPadding,
                shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button),
                border = BorderStroke(1.dp, textColor ?: PasabayanDesignSystem.Colors.primary)
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    count = count
                )
            }
        }
        PButtonStyle.Tertiary -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                colors = buttonColors,
                contentPadding = buttonPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    count = count
                )
            }
        }
        PButtonStyle.Filter -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                colors = buttonColors,
                contentPadding = buttonPadding,
                shape = RoundedCornerShape(50) // Capsule shape for filters
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    count = count
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    loading: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    count: Int?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            leadingIcon?.invoke()
        }
        
        Text(
            text = text,
            style = textStyle
        )
        
        count?.let {
            Text(
                text = "($it)",
                style = PasabayanDesignSystem.Typography.caption
            )
        }
        
        if (!loading) {
            trailingIcon?.invoke()
        }
    }
}

/**
 * PButtonOutlined - Outlined button variant using design system
 */
@Composable
fun PButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = PasabayanDesignSystem.Colors.primary,
    textColor: Color = PasabayanDesignSystem.Colors.primary
) {
    PButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = PButtonStyle.Secondary,
        enabled = enabled,
        textColor = textColor
    )
}

/**
 * PButtonText - Text button variant using design system
 */
@Composable
fun PButtonText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = PasabayanDesignSystem.Colors.primary
) {
    PButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = PButtonStyle.Tertiary,
        enabled = enabled,
        textColor = textColor
    )
}

/**
 * PButtonSmall - Small button variant using design system
 */
@Composable
fun PButtonSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = PasabayanDesignSystem.Colors.primary,
    textColor: Color = PasabayanDesignSystem.Colors.onPrimary
) {
    PButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = PButtonStyle.Primary,
        size = PButtonSize.Small,
        enabled = enabled,
        backgroundColor = backgroundColor,
        textColor = textColor
    )
} 