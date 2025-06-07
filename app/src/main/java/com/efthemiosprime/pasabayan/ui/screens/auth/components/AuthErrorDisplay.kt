package com.efthemiosprime.pasabayan.ui.screens.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Error display component for authentication screens
 * Shows error messages in a styled card with appropriate colors
 * Follows Material 3 error display patterns and iOS error styling
 */
@Composable
fun AuthErrorDisplay(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

// MARK: - Previews
@Preview("Default Error Display")
@Composable
fun AuthErrorDisplayPreview() {
    PasabayanTheme {
        AuthErrorDisplay(
            errorMessage = "Authentication failed. Please try again."
        )
    }
}

@Preview("Long Error Message")
@Composable
fun AuthErrorDisplayLongPreview() {
    PasabayanTheme {
        AuthErrorDisplay(
            errorMessage = "Network connection error. Please check your internet connection and try again. If the problem persists, contact support."
        )
    }
} 