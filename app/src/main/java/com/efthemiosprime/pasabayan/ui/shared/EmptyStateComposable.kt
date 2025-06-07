package com.efthemiosprime.pasabayan.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.common.EmptyStateData
import com.efthemiosprime.pasabayan.ui.common.EventHandler
import com.efthemiosprime.pasabayan.ui.common.PureComposable
import com.efthemiosprime.pasabayan.ui.common.emptyState
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Pure EmptyState component following functional composable patterns
 * Phase 4: Refactored to use immutable data structures and pure functions
 * Maintains exact visual compatibility with original EmptyStateView
 */

// MARK: - Pure Component

/**
 * Pure composable function that takes immutable data and renders UI
 * No internal state, no side effects - purely functional
 */
val PureEmptyState: PureComposable<EmptyStateData> = { data, modifier ->
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pure icon rendering
            EmptyStateIcon(
                icon = data.icon,
                contentDescription = data.title
            )
            
            // Pure content rendering
            EmptyStateContent(
                title = data.title,
                description = data.description
            )
            
            // Conditionally render action button
            data.actionText?.let { actionText ->
                EmptyStateAction(text = actionText)
            }
        }
    }
}

/**
 * Pure composable for icon rendering
 */
@Composable
private fun EmptyStateIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = Color.Gray,
        modifier = modifier.size(60.dp)
    )
}

/**
 * Pure composable for content rendering
 */
@Composable
private fun EmptyStateContent(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pure composable for action button rendering
 */
@Composable
private fun EmptyStateAction(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

// MARK: - Enhanced Component with Event Handling

/**
 * Enhanced empty state with action handling
 */
@Composable
fun EmptyStateWithAction(
    data: EmptyStateData,
    onActionClick: EventHandler<String>? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmptyStateIcon(
                icon = data.icon,
                contentDescription = data.title
            )
            
            EmptyStateContent(
                title = data.title,
                description = data.description
            )
            
            data.actionText?.let { actionText ->
                EmptyStateAction(
                    text = actionText,
                    onClick = { onActionClick?.invoke(actionText) }
                )
            }
        }
    }
}

// MARK: - Data Conversion Utilities

/**
 * Convert from individual parameters to EmptyStateData
 */
fun createEmptyStateData(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionText: String? = null
): EmptyStateData {
    return EmptyStateData(
        icon = icon,
        title = title,
        description = description,
        actionText = actionText
    )
}

// MARK: - Higher-Order Components

/**
 * Higher-order composable for empty state with loading
 */
@Composable
fun <T> EmptyStateOrContent(
    data: T?,
    emptyStateData: EmptyStateData,
    content: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    if (data == null) {
        PureEmptyState(emptyStateData, modifier)
    } else {
        content(data)
    }
}

/**
 * Empty state with conditional rendering
 */
@Composable
fun ConditionalEmptyState(
    isEmpty: Boolean,
    emptyStateData: EmptyStateData,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEmpty) {
        PureEmptyState(emptyStateData, modifier)
    } else {
        content()
    }
}

// MARK: - Previews

@Preview("Pure Empty State - No Packages")
@Composable
fun PureEmptyStatePreview() {
    PasabayanTheme {
        val emptyStateData = emptyState {
            icon(Icons.Default.Inventory2)
            title("No packages found")
            description("There are no packages to display at the moment.")
        }
        
        emptyStateData?.let { data ->
            PureEmptyState(data, Modifier)
        }
    }
}

@Preview("Empty State with Action")
@Composable
fun EmptyStateWithActionPreview() {
    PasabayanTheme {
        val emptyStateData = emptyState {
            icon(Icons.Default.Inventory2)
            title("No trips available")
            description("Create your first trip to get started with deliveries.")
            actionText("Create Trip")
        }
        
        emptyStateData?.let { data ->
            EmptyStateWithAction(
                data = data,
                onActionClick = { actionText ->
                    println("Action clicked: $actionText")
                }
            )
        }
    }
}

@Preview("Data Conversion Example")
@Composable
fun DataConversionExamplePreview() {
    PasabayanTheme {
        val data = createEmptyStateData(
            icon = Icons.Default.Inventory2,
            title = "No packages found",
            description = "There are no packages to display at the moment."
        )
        
        PureEmptyState(data, Modifier)
    }
} 