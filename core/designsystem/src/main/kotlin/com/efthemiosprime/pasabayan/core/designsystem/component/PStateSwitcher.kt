package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.R

/**
 * Generic UI state for list/detail screens.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
}

/**
 * Renders the appropriate UI for each [UiState] variant.
 * Reduces boilerplate `when` blocks across list/detail screens.
 */
@Composable
fun <T> PStateSwitcher(
    state: UiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    emptyIcon: ImageVector = Icons.Outlined.Info,
    emptyTitle: String = stringResource(R.string.ds_no_items),
    emptyDescription: String = "",
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PasabayanSpacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PasabayanSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    Text(
                        text = state.message,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.error,
                    )
                    PButton(
                        text = stringResource(R.string.ds_retry),
                        onClick = onRetry,
                        style = PButtonStyle.Secondary,
                        size = PButtonSize.Small,
                    )
                }
            }

            is UiState.Empty -> {
                PEmptyState(
                    icon = emptyIcon,
                    title = emptyTitle,
                    description = emptyDescription,
                    modifier = Modifier.padding(PasabayanSpacing.lg),
                )
            }

            is UiState.Content -> {
                content(state.data)
            }
        }
    }
}
