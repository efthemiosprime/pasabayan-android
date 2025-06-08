package com.efthemiosprime.pasabayan.ui.shared.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

enum class PCardPadding {
    None,
    Small,
    Medium,
    Large
}

enum class PCardElevation {
    None,
    Small,
    Medium,
    Large
}

/**
 * PCardStandard - Global Card Component with Automatic Standards
 * This is the SINGLE SOURCE OF TRUTH for all card styling in the app
 * All cards should use this component to ensure consistency
 */
@Composable
fun PCardStandard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = PasabayanDesignSystem.CardStandards.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = PasabayanDesignSystem.CardStandards.elevation)
        ) {
            Column(
                modifier = Modifier.padding(PasabayanDesignSystem.CardStandards.padding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = PasabayanDesignSystem.CardStandards.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = PasabayanDesignSystem.CardStandards.elevation)
        ) {
            Column(
                modifier = Modifier.padding(PasabayanDesignSystem.CardStandards.padding),
                content = content
            )
        }
    }
}

/**
 * PCardStandardCompact - Global Compact Card with Automatic Standards
 * For smaller cards that need less padding but same elevation/styling
 */
@Composable
fun PCardStandardCompact(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = PasabayanDesignSystem.CardStandards.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = PasabayanDesignSystem.CardStandards.elevation)
        ) {
            Column(
                modifier = Modifier.padding(PasabayanDesignSystem.Spacing.sm),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = PasabayanDesignSystem.CardStandards.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = PasabayanDesignSystem.CardStandards.elevation)
        ) {
            Column(
                modifier = Modifier.padding(PasabayanDesignSystem.Spacing.sm),
                content = content
            )
        }
    }
}

// ============================================================================
// LEGACY FUNCTIONS - All automatically use Global Standards
// These maintain backward compatibility but force global standards
// ============================================================================

/**
 * Legacy PCard - Now automatically uses Global Standards
 * All PCard instances will have consistent 4dp elevation and white background
 * Parameters kept for compatibility but global standards are always applied
 */
@Composable
fun PCard(
    modifier: Modifier = Modifier,
    elevation: Int = 8, // Parameter kept for compatibility but ignored
    backgroundColor: Color = PasabayanDesignSystem.CardStandards.backgroundColor, // Force global standard
    content: @Composable ColumnScope.() -> Unit
) {
    // Always use global standards regardless of parameters
    PCardStandard(
        modifier = modifier,
        content = content
    )
}

/**
 * Legacy PCardCompact (Int elevation) - Now automatically uses Global Standards
 * All PCardCompact instances will have consistent 4dp elevation and white background
 * Parameters kept for compatibility but global standards are always applied
 */
@Composable
fun PCardCompact(
    modifier: Modifier = Modifier,
    elevation: Int = 4, // Parameter kept for compatibility but ignored
    backgroundColor: Color = PasabayanDesignSystem.CardStandards.backgroundColor, // Force global standard
    content: @Composable ColumnScope.() -> Unit
) {
    // Always use global standards regardless of parameters
    PCardStandardCompact(
        modifier = modifier,
        content = content
    )
}

/**
 * Legacy PCardCompact (Enum elevation) - Now automatically uses Global Standards
 * All PCardCompact instances will have consistent 4dp elevation and white background
 * Parameters kept for compatibility but global standards are always applied
 */
@Composable
fun PCardCompact(
    modifier: Modifier = Modifier,
    elevation: PCardElevation = PCardElevation.Small, // Parameter kept for compatibility but ignored
    backgroundColor: Color = PasabayanDesignSystem.CardStandards.backgroundColor, // Force global standard
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Always use global standards regardless of parameters
    PCardStandardCompact(
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}

// ============================================================================
// ADVANCED FUNCTIONS - For special cases that need custom styling
// These should be used sparingly and only when global standards don't apply
// ============================================================================

/**
 * PCard - Universal card component following Pasabayan Design System
 * Equivalent to Swift's UniversalCard
 * Use this ONLY when you need custom styling that differs from global standards
 */
@Composable
fun PCard(
    modifier: Modifier = Modifier,
    padding: PCardPadding = PCardPadding.Medium,
    elevation: PCardElevation = PCardElevation.Medium,
    backgroundColor: Color = PasabayanDesignSystem.Colors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardPadding = when (padding) {
        PCardPadding.None -> 0.dp
        PCardPadding.Small -> PasabayanDesignSystem.Spacing.sm
        PCardPadding.Medium -> PasabayanDesignSystem.Spacing.lg
        PCardPadding.Large -> PasabayanDesignSystem.Spacing.xxl
    }
    
    val cardElevation = when (elevation) {
        PCardElevation.None -> PasabayanDesignSystem.Elevation.none
        PCardElevation.Small -> PasabayanDesignSystem.Elevation.sm
        PCardElevation.Medium -> PasabayanDesignSystem.Elevation.md
        PCardElevation.Large -> PasabayanDesignSystem.Elevation.lg
    }
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                content = content
            )
        }
    }
}

