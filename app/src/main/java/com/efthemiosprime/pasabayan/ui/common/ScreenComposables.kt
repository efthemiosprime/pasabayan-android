package com.efthemiosprime.pasabayan.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.presentation.common.UiState
import com.efthemiosprime.pasabayan.ui.shared.PureEmptyState
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Pure screen composables following functional patterns
 * Phase 4: Refactored to use immutable data structures and pure functions
 * Provides reusable screen layouts with loading, error, and empty states
 */

// MARK: - Screen State Types

/**
 * Immutable screen state for pure screen composables
 */
@Immutable
data class ScreenState<T>(
    val data: T?,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val emptyStateData: EmptyStateData? = null
) {
    val isEmpty: Boolean get() = data == null && !isLoading && error == null
    val hasError: Boolean get() = error != null
    val hasData: Boolean get() = data != null
}

@Immutable
data class ListScreenData<T>(
    val items: List<T>,
    val emptyState: EmptyStateData? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val size: Int get() = items.size
    fun <R> map(transform: (T) -> R): ListScreenData<R> {
        return ListScreenData(
            items = items.map(transform),
            emptyState = emptyState,
            metadata = metadata
        )
    }
}

// MARK: - Pure Screen Components

/**
 * Pure screen composable that handles loading, error, empty, and success states
 */
@Composable
fun <T> PureScreen(
    screenState: ScreenState<T>,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (AppError) -> Unit = { DefaultErrorContent(it) },
    emptyContent: @Composable () -> Unit = { 
        screenState.emptyStateData?.let { 
            PureEmptyState(it, Modifier.fillMaxSize()) 
        } ?: DefaultEmptyContent()
    },
    successContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            screenState.isLoading -> loadingContent()
            screenState.hasError -> screenState.error?.let { errorContent(it) }
            screenState.isEmpty -> emptyContent()
            screenState.hasData -> screenState.data?.let { data -> successContent(data) }
        }
    }
}

/**
 * Pure list screen composable for handling list data
 */
@Composable
fun <T> PureListScreen(
    listData: ListScreenData<T>,
    isLoading: Boolean = false,
    error: AppError? = null,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (AppError) -> Unit = { DefaultErrorContent(it) },
    itemComposable: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            isLoading -> loadingContent()
            error != null -> errorContent(error)
            listData.isEmpty -> {
                listData.emptyState?.let { emptyState ->
                    PureEmptyState(emptyState, Modifier)
                } ?: DefaultEmptyContent()
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listData.items) { item ->
                        itemComposable(item)
                    }
                }
            }
        }
    }
}

/**
 * Pure detail screen composable for single item display
 */
@Composable
fun <T> PureDetailScreen(
    data: T?,
    isLoading: Boolean = false,
    error: AppError? = null,
    emptyStateData: EmptyStateData? = null,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (AppError) -> Unit = { DefaultErrorContent(it) },
    detailContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenState = ScreenState(
        data = data,
        isLoading = isLoading,
        error = error,
        emptyStateData = emptyStateData
    )
    
    PureScreen(
        screenState = screenState,
        loadingContent = loadingContent,
        errorContent = errorContent,
        successContent = detailContent,
        modifier = modifier
    )
}

// MARK: - Default Screen Components

/**
 * Default loading content for pure screens
 */
@Composable
fun DefaultLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Default error content for pure screens
 */
@Composable
fun DefaultErrorContent(
    error: AppError,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Default empty content for pure screens
 */
@Composable
fun DefaultEmptyContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Pure Builder Functions

/**
 * Builder function for creating screen state
 */
fun <T> screenState(
    data: T? = null,
    isLoading: Boolean = false,
    error: AppError? = null,
    emptyStateData: EmptyStateData? = null
): ScreenState<T> {
    return ScreenState(
        data = data,
        isLoading = isLoading,
        error = error,
        emptyStateData = emptyStateData
    )
}

/**
 * Builder function for creating list screen data
 */
fun <T> listScreenData(
    items: List<T>,
    emptyState: EmptyStateData? = null,
    metadata: Map<String, Any> = emptyMap()
): ListScreenData<T> {
    return ListScreenData(
        items = items,
        emptyState = emptyState,
        metadata = metadata
    )
}

// MARK: - Extension Functions

/**
 * Convert UiState to ScreenState for pure screens
 */
fun <T> UiState<T>.toScreenState(emptyStateData: EmptyStateData? = null): ScreenState<T> {
    return ScreenState(
        data = data,
        isLoading = isLoading,
        error = error,
        emptyStateData = emptyStateData
    )
}

// MARK: - Previews

@Preview("Loading Screen")
@Composable
fun LoadingScreenPreview() {
    PasabayanTheme {
        PureScreen<String>(
            screenState = screenState(isLoading = true),
            successContent = { Text("Success: $it") }
        )
    }
}

@Preview("Error Screen")
@Composable
fun ErrorScreenPreview() {
    PasabayanTheme {
        PureScreen<String>(
            screenState = screenState(error = AppError.NetworkError("Connection failed")),
            successContent = { Text("Success: $it") }
        )
    }
}

@Preview("List Screen")
@Composable
fun ListScreenPreview() {
    PasabayanTheme {
        val listData = listScreenData(
            items = listOf("Item 1", "Item 2", "Item 3")
        )
        
        PureListScreen(
            listData = listData,
            itemComposable = { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
} 