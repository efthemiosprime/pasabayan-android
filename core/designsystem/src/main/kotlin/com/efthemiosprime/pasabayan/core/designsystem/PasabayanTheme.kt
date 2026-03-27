package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val LightScheme = lightColorScheme(
    primary = PasabayanColors.PrimaryBlack,
    onPrimary = Color.White,
    secondary = PasabayanColors.BadgeGray,
    onSecondary = Color.White,
    tertiary = PasabayanColors.Info,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color.White,
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF3C3C43),
    outline = PasabayanColors.Border,
    error = PasabayanColors.Error,
    onError = Color.White,
)

private val DarkScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = PasabayanColors.BadgeGray,
    onSecondary = Color.Black,
    tertiary = PasabayanColors.Info,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFEBEBF5),
    outline = Color(0xFF48484A),
    error = PasabayanColors.Error,
    onError = Color.White,
)

val LocalPasabayanNavigationTitleStyle = staticCompositionLocalOf {
    PasabayanTypography.materialTypography().titleMedium.copy(
        fontSize = PasabayanTypography.navigationTitle,
        fontWeight = FontWeight.SemiBold,
    )
}

/**
 * Pasabayan Compose theme — tokens from [14-design-system.md].
 * Dynamic color is **off** by default so brand black/white parity matches iOS.
 */
@Composable
fun PasabayanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme

    val navTitle = PasabayanTypography.materialTypography().titleMedium.copy(
        fontSize = PasabayanTypography.navigationTitle,
        fontWeight = FontWeight.SemiBold,
    )

    CompositionLocalProvider(LocalPasabayanNavigationTitleStyle provides navTitle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PasabayanTypography.materialTypography(),
            content = content,
        )
    }
}
