package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** [14-design-system.md] — nav title uses 15 sp (non-standard). */
object PasabayanTypography {
    val xs = 12.sp
    val sm = 14.sp
    val md = 16.sp
    val lg = 18.sp
    val xl = 20.sp
    val xxl = 24.sp
    val xxxl = 28.sp
    val xxxxl = 32.sp
    val xxxxxl = 40.sp
    val navigationTitle = 15.sp

    fun materialTypography(): Typography = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = xxxxxl,
            lineHeight = 48.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = xxxxl,
            lineHeight = 40.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = xxxl,
            lineHeight = 36.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = xxl,
            lineHeight = 32.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = xl,
            lineHeight = 28.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = md,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = sm,
            lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = xs,
            lineHeight = 16.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = sm,
            lineHeight = 20.sp,
        ),
    )
}
