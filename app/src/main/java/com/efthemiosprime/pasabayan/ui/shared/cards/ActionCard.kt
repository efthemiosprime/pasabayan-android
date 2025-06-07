package com.efthemiosprime.pasabayan.ui.shared.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.shared.PButton

/**
 * ActionCard - For cards with buttons
 * Equivalent to Swift's ActionCard
 */
@Composable
fun ActionCard(
    title: String,
    description: String? = null,
    primaryButtonText: String,
    onPrimaryAction: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier
    ) {
        // Icon (optional)
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Description (optional)
        description?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Buttons
        if (secondaryButtonText != null && onSecondaryAction != null) {
            // Two buttons - horizontal layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PButton(
                    text = secondaryButtonText,
                    onClick = onSecondaryAction,
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                PButton(
                    text = primaryButtonText,
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Single button - full width
            PButton(
                text = primaryButtonText,
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Compact ActionCard variant
 */
@Composable
fun ActionCardCompact(
    title: String,
    buttonText: String,
    onAction: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    PCardCompact(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            PButton(
                text = buttonText,
                onClick = onAction
            )
        }
    }
} 