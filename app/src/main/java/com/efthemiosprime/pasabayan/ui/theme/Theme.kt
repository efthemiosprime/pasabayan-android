package com.efthemiosprime.pasabayan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PasabayanDesignSystem {
    
    // MARK: - Spacing System (4dp grid)
    object Spacing {
        val xs = 4.dp      // Extra small
        val sm = 8.dp      // Small
        val md = 12.dp     // Medium
        val lg = 16.dp     // Large
        val xl = 20.dp     // Extra large
        val xxl = 24.dp    // 2X large
        val xxxl = 32.dp   // 3X large
        val xxxxl = 40.dp  // 4X large
        val xxxxxl = 48.dp // 5X large
        
        // Semantic spacing
        val cardPadding = lg        // 16dp
        val screenPadding = lg      // 16dp
        val buttonPadding = md      // 12dp
        val sectionSpacing = xxl    // 24dp
        val itemSpacing = sm        // 8dp
        val iconTextSpacing = sm    // 8dp
        val formFieldSpacing = xl   // 20dp
    }
    
    // MARK: - Corner Radius System
    object CornerRadius {
        val xs = 4.dp      // Badges, chips
        val sm = 8.dp      // Buttons, inputs
        val md = 12.dp     // Cards, dialogs
        val lg = 16.dp     // Large cards, sheets
        val xl = 20.dp     // Special cases
        val xxl = 24.dp    // Very large components
        
        // Semantic corner radius
        val button = sm         // 8dp
        val card = md           // 12dp
        val modal = lg          // 16dp
        val badge = xs          // 4dp
        val avatar = xxl        // 24dp
    }
    
    // MARK: - Color System (Black Primary Theme)
    object Colors {
        // Primary colors (Black-based theme)
        val primary = Color(0xFF000000)           // Black
        val primaryLight = Color(0xFF424242)      // Dark gray
        val primaryDark = Color(0xFF000000)       // Black
        val onPrimary = Color(0xFFFFFFFF)         // White text on black
        
        // Secondary colors
        val secondary = Color(0xFF757575)         // Medium gray
        val secondaryLight = Color(0xFFBDBDBD)    // Light gray
        val secondaryDark = Color(0xFF424242)     // Dark gray
        val onSecondary = Color(0xFF000000)       // Black text on gray
        
        // Status colors
        val success = Color(0xFF4CAF50)           // Green
        val warning = Color(0xFFFF9800)           // Orange
        val error = Color(0xFFF44336)             // Red
        val info = Color(0xFF2196F3)              // Blue
        
        // Surface colors
        val surface = Color(0xFFFFFFFF)           // White
        val surfaceVariant = Color(0xFFF5F5F5)    // Light gray
        val onSurface = Color(0xFF000000)         // Black text
        val onSurfaceVariant = Color(0xFF757575)  // Gray text
        
        // Status-specific colors
        val tripScheduled = Color(0xFF2196F3)     // Blue
        val tripActive = Color(0xFF3F51B5)        // Indigo
        val tripCompleted = Color(0xFF4CAF50)     // Green
        val tripCancelled = Color(0xFFF44336)     // Red
        
        val packageOpen = Color(0xFF2196F3)       // Blue
        val packageMatched = Color(0xFF9C27B0)    // Purple
        val packageBooked = Color(0xFFFF9800)     // Orange
        val packageInTransit = Color(0xFF3F51B5)  // Indigo
        val packageDelivered = Color(0xFF4CAF50)  // Green
    }
    
    // MARK: - Typography System
    object Typography {
        val displayLarge = TextStyle(
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp
        )
        
        val headingH1 = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        )
        
        val headingH2 = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )
        
        val headingH3 = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 32.sp
        )
        
        val bodyLarge = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        )
        
        val bodyRegular = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp
        )
        
        val bodySmall = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        )
        
        val caption = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp
        )
        
        val buttonLarge = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        )
        
        val buttonMedium = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp
        )
        
        val buttonSmall = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
    
    // MARK: - Elevation System
    object Elevation {
        val none = 0.dp
        val xs = 1.dp      // Subtle elevation
        val sm = 2.dp      // Button elevation
        val md = 4.dp      // Card elevation - GLOBAL STANDARD
        val lg = 8.dp      // Modal elevation
        val xl = 12.dp     // Navigation elevation
        val xxl = 16.dp    // Very high elevation
        
        // Semantic elevation
        val button = sm         // 2dp
        val card = md           // 4dp - GLOBAL STANDARD for all cards
        val modal = lg          // 8dp
        val floating = xl       // 12dp
    }
    
    // MARK: - Global Card Standards
    object CardStandards {
        // All cards throughout the app should use these consistent values
        val elevation = Elevation.card          // 4dp elevation for all cards
        val backgroundColor = Colors.surface    // White background for all cards
        val cornerRadius = CornerRadius.card    // 12dp corner radius
        val padding = Spacing.lg               // 16dp padding
        
        // Child components inside cards should NEVER have elevation
        val childElevation = Elevation.none    // 0dp for components inside cards
    }
}

// Create Material3 color scheme
private val LightColorScheme = lightColorScheme(
    primary = PasabayanDesignSystem.Colors.primary,
    onPrimary = PasabayanDesignSystem.Colors.onPrimary,
    secondary = PasabayanDesignSystem.Colors.secondary,
    onSecondary = PasabayanDesignSystem.Colors.onSecondary,
    surface = PasabayanDesignSystem.Colors.surface,
    onSurface = PasabayanDesignSystem.Colors.onSurface,
    surfaceVariant = PasabayanDesignSystem.Colors.surfaceVariant,
    onSurfaceVariant = PasabayanDesignSystem.Colors.onSurfaceVariant,
    error = PasabayanDesignSystem.Colors.error,
    background = Color.White,
    onBackground = PasabayanDesignSystem.Colors.onSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = PasabayanDesignSystem.Colors.primaryLight,
    onPrimary = Color.Black,
    secondary = PasabayanDesignSystem.Colors.secondaryLight,
    onSecondary = Color.Black,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = PasabayanDesignSystem.Colors.error,
    background = Color(0xFF121212),
    onBackground = Color.White
)

@Composable
fun PasabayanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Status-specific color functions
@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "scheduled" -> PasabayanDesignSystem.Colors.tripScheduled
        "active" -> PasabayanDesignSystem.Colors.tripActive
        "completed" -> PasabayanDesignSystem.Colors.tripCompleted
        "cancelled" -> PasabayanDesignSystem.Colors.tripCancelled
        "pending", "open" -> PasabayanDesignSystem.Colors.packageOpen
        "matched" -> PasabayanDesignSystem.Colors.packageMatched
        "booked" -> PasabayanDesignSystem.Colors.packageBooked
        "in_transit" -> PasabayanDesignSystem.Colors.packageInTransit
        "delivered" -> PasabayanDesignSystem.Colors.packageDelivered
        else -> PasabayanDesignSystem.Colors.secondary
    }
}

// Consistent text components
@Composable
fun HeadingText(
    text: String,
    level: Int = 1,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    color: Color = PasabayanDesignSystem.Colors.onSurface
) {
    val textStyle = when (level) {
        1 -> PasabayanDesignSystem.Typography.headingH1
        2 -> PasabayanDesignSystem.Typography.headingH2
        3 -> PasabayanDesignSystem.Typography.headingH3
        else -> PasabayanDesignSystem.Typography.bodyRegular
    }
    
    androidx.compose.material3.Text(
        text = text,
        style = textStyle,
        color = color,
        modifier = modifier
    )
}