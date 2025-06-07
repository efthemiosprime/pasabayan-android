package com.efthemiosprime.pasabayan.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.presentation.common.UiState

/**
 * Pure UI patterns and utilities for Phase 4 refactoring
 * Implements pure functional composables following functional programming principles
 */

// MARK: - UI State Types

/**
 * Immutable UI data classes for pure composables
 */
@Immutable
data class EmptyStateData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val actionText: String? = null
)

@Immutable
data class BadgeData(
    val text: String,
    val icon: String? = null,
    val backgroundColor: Color,
    val textColor: Color,
    val variant: com.efthemiosprime.pasabayan.ui.components.BadgeVariant = com.efthemiosprime.pasabayan.ui.components.BadgeVariant.STANDARD
)

@Immutable
data class CardData(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val badge: BadgeData? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Immutable
data class LoadingStateData(
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isEmpty: Boolean = false,
    val emptyState: EmptyStateData? = null
)

// MARK: - Pure Component Types

/**
 * Pure composable function type definitions
 */
typealias PureComposable<T> = @Composable (data: T, modifier: Modifier) -> Unit
typealias StatelessComposable = @Composable (modifier: Modifier) -> Unit
typealias EventHandler<T> = (T) -> Unit

// MARK: - Composition Utilities

/**
 * Higher-order composable for conditional rendering
 */
@Composable
fun ConditionalCompose(
    condition: Boolean,
    content: @Composable () -> Unit,
    fallback: @Composable () -> Unit = { }
) {
    if (condition) content() else fallback()
}

/**
 * Higher-order composable for mapping over data
 * Note: This is for demonstration - in practice, use LazyColumn with items()
 */
@Composable
fun <T> MapCompose(
    items: List<T>,
    transform: @Composable (T) -> Unit
) {
    items.forEach { item ->
        transform(item)
    }
}

/**
 * Higher-order composable for state-based rendering
 */
@Composable
fun <T> StateCompose(
    uiState: UiState<T>,
    loadingContent: StatelessComposable = { },
    errorContent: @Composable (AppError, Modifier) -> Unit = { _, _ -> },
    emptyContent: StatelessComposable = { },
    successContent: @Composable (T, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> loadingContent(modifier)
        uiState.isError -> uiState.error?.let { errorContent(it, modifier) }
        uiState.data == null -> emptyContent(modifier)
        else -> successContent(uiState.data, modifier)
    }
}

// MARK: - Pure Function Helpers

/**
 * Pure functions for UI calculations and transformations
 */
object UiUtils {
    
    /**
     * Pure function to calculate badge styling
     */
    fun calculateBadgeColors(
        status: String,
        variant: com.efthemiosprime.pasabayan.ui.components.BadgeVariant = com.efthemiosprime.pasabayan.ui.components.BadgeVariant.STANDARD
    ): Pair<Color, Color> {
        // Pure color calculation logic
        return when (status.lowercase()) {
            "active", "confirmed", "completed" -> Color(0xFF4CAF50) to Color.White
            "pending", "scheduled" -> Color(0xFF2196F3) to Color.White
            "cancelled", "failed" -> Color(0xFFF44336) to Color.White
            else -> Color(0xFF9E9E9E) to Color.White
        }
    }
    
    /**
     * Pure function to format display text
     */
    fun formatDisplayText(
        text: String,
        maxLength: Int = 50,
        suffix: String = "..."
    ): String {
        return if (text.length > maxLength) {
            text.take(maxLength - suffix.length) + suffix
        } else {
            text
        }
    }
    
    /**
     * Pure function to validate UI data
     */
    fun validateCardData(data: CardData): Boolean {
        return data.title.isNotBlank()
    }
    
    /**
     * Pure function to transform data for display
     */
    fun <T, R> transformForDisplay(
        data: List<T>,
        transform: (T) -> R,
        filter: (T) -> Boolean = { true }
    ): List<R> {
        return data.filter(filter).map(transform)
    }
}

// MARK: - Component Builders

/**
 * Builder pattern for creating immutable UI data
 */
class EmptyStateBuilder {
    private var icon: ImageVector? = null
    private var title: String = ""
    private var description: String = ""
    private var actionText: String? = null
    
    fun icon(icon: ImageVector) = apply { this.icon = icon }
    fun title(title: String) = apply { this.title = title }
    fun description(description: String) = apply { this.description = description }
    fun actionText(text: String) = apply { this.actionText = text }
    
    fun build(): EmptyStateData? {
        return icon?.let { 
            EmptyStateData(
                icon = it,
                title = title,
                description = description,
                actionText = actionText
            )
        }
    }
}

class BadgeDataBuilder {
    private var text: String = ""
    private var icon: String? = null
    private var backgroundColor: Color = Color.Gray
    private var textColor: Color = Color.White
    private var variant: com.efthemiosprime.pasabayan.ui.components.BadgeVariant = com.efthemiosprime.pasabayan.ui.components.BadgeVariant.STANDARD
    
    fun text(text: String) = apply { this.text = text }
    fun icon(icon: String) = apply { this.icon = icon }
    fun colors(background: Color, text: Color) = apply { 
        this.backgroundColor = background
        this.textColor = text
    }
    fun variant(variant: com.efthemiosprime.pasabayan.ui.components.BadgeVariant) = apply { this.variant = variant }
    
    fun build(): BadgeData {
        return BadgeData(
            text = text,
            icon = icon,
            backgroundColor = backgroundColor,
            textColor = textColor,
            variant = variant
        )
    }
}

// MARK: - DSL Functions

/**
 * DSL for creating empty state data
 */
fun emptyState(block: EmptyStateBuilder.() -> Unit): EmptyStateData? {
    return EmptyStateBuilder().apply(block).build()
}

/**
 * DSL for creating badge data
 */
fun badgeData(block: BadgeDataBuilder.() -> Unit): BadgeData {
    return BadgeDataBuilder().apply(block).build()
}

// MARK: - Component State Management

/**
 * Immutable state wrapper for component data
 */
@Stable
data class ComponentState<T>(
    val data: T,
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
) {
    fun <R> map(transform: (T) -> R): ComponentState<R> {
        return ComponentState(
            data = transform(data),
            isVisible = isVisible,
            isEnabled = isEnabled,
            metadata = metadata
        )
    }
    
    fun copy(
        isVisible: Boolean = this.isVisible,
        isEnabled: Boolean = this.isEnabled,
        metadata: Map<String, Any> = this.metadata
    ): ComponentState<T> {
        return ComponentState(
            data = data,
            isVisible = isVisible,
            isEnabled = isEnabled,
            metadata = metadata
        )
    }
}

// Note: BadgeVariant enum is imported from ui.components.BadgeVariant 