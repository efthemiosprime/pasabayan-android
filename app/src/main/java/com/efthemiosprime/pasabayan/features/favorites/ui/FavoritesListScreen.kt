package com.efthemiosprime.pasabayan.features.favorites.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierInfoJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson
import com.efthemiosprime.pasabayan.features.favorites.model.FavoritesSort
import com.efthemiosprime.pasabayan.features.favorites.model.FavoritesUiState
import com.efthemiosprime.pasabayan.features.favorites.viewmodel.FavoritesListViewModel

object FavoritesTestTags {
    const val Root = "favorites_list"
    const val Empty = "favorites_empty"
    fun cardFor(carrierId: Int) = "favorites_card_$carrierId"
    fun removeFor(carrierId: Int) = "favorites_remove_$carrierId"
    fun requestFor(carrierId: Int) = "favorites_request_$carrierId"
}

@Composable
fun FavoritesListScreen(
    onRequestDelivery: (FavoriteCarrierInfoJson) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    FavoritesListContent(
        state = state,
        onSortChange = viewModel::setSort,
        onUpcomingFilterChange = viewModel::setOnlyUpcomingTrips,
        onRemove = viewModel::removeFavorite,
        onRequestDelivery = onRequestDelivery,
        modifier = modifier,
    )
}

@Composable
fun FavoritesListContent(
    state: FavoritesUiState,
    onSortChange: (FavoritesSort) -> Unit,
    onUpcomingFilterChange: (Boolean) -> Unit,
    onRemove: (Int) -> Unit,
    onRequestDelivery: (FavoriteCarrierInfoJson) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.sm,
            )
            .testTag(FavoritesTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        SortAndFilters(
            sort = state.sort,
            onlyUpcoming = state.onlyUpcomingTrips,
            onSortChange = onSortChange,
            onUpcomingFilterChange = onUpcomingFilterChange,
        )
        if (state.isLoading && state.favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PCircularProgress()
            }
        } else if (state.favorites.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                items(state.favorites, key = { it.carrier.id }) { fav ->
                    FavoriteCarrierCard(
                        favorite = fav,
                        isRemoving = fav.carrier.id in state.pendingRemovalIds,
                        onRemove = { onRemove(fav.carrier.id) },
                        onRequestDelivery = { onRequestDelivery(fav.carrier) },
                    )
                }
            }
        }
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        state.infoMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = msg, style = PasabayanTextStyles.Body.small)
            }
        }
    }
}

@Composable
private fun SortAndFilters(
    sort: FavoritesSort,
    onlyUpcoming: Boolean,
    onSortChange: (FavoritesSort) -> Unit,
    onUpcomingFilterChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FavoritesSort.entries.forEach { option ->
                FilterChip(
                    selected = option == sort,
                    onClick = { onSortChange(option) },
                    label = { Text(stringResource(option.labelRes())) },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.favorites_filter_upcoming),
                style = PasabayanTextStyles.Body.medium,
            )
            Switch(checked = onlyUpcoming, onCheckedChange = onUpcomingFilterChange)
        }
    }
}

private fun FavoritesSort.labelRes(): Int = when (this) {
    FavoritesSort.RECENT -> R.string.favorites_sort_recent
    FavoritesSort.MOST_USED -> R.string.favorites_sort_most_used
    FavoritesSort.RATING -> R.string.favorites_sort_rating
}

@Composable
private fun EmptyState() {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(FavoritesTestTags.Empty),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.favorites_empty_title),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.favorites_empty_subtitle),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FavoriteCarrierCard(
    favorite: FavoriteCarrierJson,
    isRemoving: Boolean,
    onRemove: () -> Unit,
    onRequestDelivery: () -> Unit,
) {
    val carrier = favorite.carrier
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(FavoritesTestTags.cardFor(carrier.id)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            ) {
                LetterAvatar(name = carrier.name)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = carrier.name,
                        style = PasabayanTextStyles.Body.medium,
                    )
                    val rating = carrier.rating
                    val ratings = carrier.totalRatings
                    if (rating != null && ratings != null) {
                        Text(
                            text = stringResource(
                                R.string.favorites_card_rating,
                                "%.1f".format(rating),
                                ratings,
                            ),
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.favorites_card_deliveries,
                            favorite.totalDeliveriesTogether,
                        ),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (favorite.upcomingTripsCount > 0) {
                        Text(
                            text = stringResource(
                                R.string.favorites_card_upcoming,
                                favorite.upcomingTripsCount,
                            ),
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                PButton(
                    text = stringResource(R.string.favorites_request_delivery),
                    onClick = onRequestDelivery,
                    enabled = !isRemoving,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(FavoritesTestTags.requestFor(carrier.id)),
                )
                PButton(
                    text = stringResource(R.string.favorites_remove),
                    onClick = onRemove,
                    enabled = !isRemoving,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(FavoritesTestTags.removeFor(carrier.id)),
                )
            }
        }
    }
}

@Composable
private fun LetterAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true, name = "Favorites — light")
@Preview(showBackground = true, name = "Favorites — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesListPreview() {
    PasabayanTheme {
        FavoritesListContent(
            state = FavoritesUiState(
                isInitialized = true,
                favorites = listOf(
                    FavoriteCarrierJson(
                        id = 1,
                        carrier = FavoriteCarrierInfoJson(
                            id = 11,
                            name = "Alex Stewart",
                            rating = 4.7,
                            totalRatings = 32,
                        ),
                        totalDeliveriesTogether = 6,
                        upcomingTripsCount = 2,
                    ),
                ),
            ),
            onSortChange = {},
            onUpcomingFilterChange = {},
            onRemove = {},
            onRequestDelivery = {},
        )
    }
}
