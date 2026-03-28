package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Semantic [TextStyle]s mirroring iOS `DesignSystem.Typography` groups.
 * Prefer [MaterialTheme.typography] where roles align; use these for explicit parity.
 */
object PasabayanTextStyles {
    object Heading {
        val h1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = PasabayanTypography.xxxxl,
            lineHeight = 40.sp,
        )
        val h2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = PasabayanTypography.xxxl,
            lineHeight = 36.sp,
        )
        val h3 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = PasabayanTypography.xxl,
            lineHeight = 32.sp,
        )
        val h4 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = PasabayanTypography.xl,
            lineHeight = 28.sp,
        )
        val h5 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = PasabayanTypography.lg,
            lineHeight = 24.sp,
        )
        val h6 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = PasabayanTypography.md,
            lineHeight = 22.sp,
        )
    }

    object Body {
        val large = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = PasabayanTypography.lg,
            lineHeight = 24.sp,
        )
        val regular = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = PasabayanTypography.md,
            lineHeight = 24.sp,
        )
        val medium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = PasabayanTypography.md,
            lineHeight = 24.sp,
        )
        val small = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = PasabayanTypography.sm,
            lineHeight = 20.sp,
        )
    }

    object Caption {
        val large = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = PasabayanTypography.sm,
            lineHeight = 20.sp,
        )
        val regular = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = PasabayanTypography.xs,
            lineHeight = 16.sp,
        )
        val small = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 14.sp,
        )
    }

    object Button {
        val large = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = PasabayanTypography.lg,
            lineHeight = 22.sp,
        )
        val medium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = PasabayanTypography.md,
            lineHeight = 22.sp,
        )
        val small = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = PasabayanTypography.sm,
            lineHeight = 18.sp,
        )
    }
}
