package com.efthemiosprime.pasabayan.features.ratings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson
import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewOtherUserJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingUserInfoJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserRatingSummaryJson
import com.efthemiosprime.pasabayan.features.ratings.model.RatingsTab
import com.efthemiosprime.pasabayan.features.ratings.model.RatingsUiState
import com.efthemiosprime.pasabayan.features.ratings.viewmodel.RatingsViewModel

object RatingsTestTags {
    const val Root = "ratings_root"
    fun tabFor(tab: RatingsTab) = "ratings_tab_${tab.name.lowercase()}"
    fun cardFor(id: Int) = "ratings_card_$id"
}

@Composable
fun RatingsScreen(
    userId: Int,
    modifier: Modifier = Modifier,
    viewModel: RatingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(userId) { viewModel.load(userId) }
    RatingsContent(
        state = state,
        onTabSelected = viewModel::selectTab,
        onStartEditing = viewModel::startEditing,
        onCommentDraftChange = viewModel::onCommentDraftChange,
        onSaveComment = viewModel::saveComment,
        onCancelEdit = viewModel::cancelEditing,
        modifier = modifier,
    )
}

@Composable
fun RatingsContent(
    state: RatingsUiState,
    onTabSelected: (RatingsTab) -> Unit,
    onStartEditing: (Int, String?) -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onSaveComment: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(RatingsTestTags.Root),
    ) {
        TabRow(selectedTabIndex = state.selectedTab.ordinal) {
            RatingsTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = stringResource(
                                when (tab) {
                                    RatingsTab.RECEIVED -> R.string.ratings_tab_received
                                    RatingsTab.GIVEN -> R.string.ratings_tab_given
                                    RatingsTab.PENDING -> R.string.ratings_tab_pending
                                },
                            ),
                        )
                    },
                    modifier = Modifier.testTag(RatingsTestTags.tabFor(tab)),
                )
            }
        }
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(PasabayanSpacing.lg), contentAlignment = Alignment.Center) {
                PCircularProgress()
            }
        } else {
            when (state.selectedTab) {
                RatingsTab.RECEIVED -> ReceivedTab(state)
                RatingsTab.GIVEN -> GivenTab(
                    state = state,
                    onStartEditing = onStartEditing,
                    onCommentDraftChange = onCommentDraftChange,
                    onSaveComment = onSaveComment,
                    onCancelEdit = onCancelEdit,
                )
                RatingsTab.PENDING -> PendingTab(state)
            }
        }
        state.errorMessage?.let { msg ->
            Box(modifier = Modifier.fillMaxWidth().padding(PasabayanSpacing.md)) {
                PCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = PasabayanTextStyles.Body.small,
                    )
                }
            }
        }
        state.infoMessage?.let { msg ->
            Box(modifier = Modifier.fillMaxWidth().padding(PasabayanSpacing.md)) {
                PCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = msg, style = PasabayanTextStyles.Body.small)
                }
            }
        }
    }
}

@Composable
private fun ReceivedTab(state: RatingsUiState) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        state.receivedSummary?.let { summary ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(text = summary.name, style = PasabayanTextStyles.Heading.h5)
                    Text(
                        text = stringResource(
                            R.string.ratings_summary_average,
                            summary.rating ?: "—",
                            summary.totalRatings,
                        ),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.received.isEmpty()) {
            EmptyTab(stringResource(R.string.ratings_empty_received))
        } else {
            state.received.forEach { row -> ReceivedRatingCard(row) }
        }
    }
}

@Composable
private fun GivenTab(
    state: RatingsUiState,
    onStartEditing: (Int, String?) -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onSaveComment: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        if (state.given.isEmpty()) {
            EmptyTab(stringResource(R.string.ratings_empty_given))
        } else {
            state.given.forEach { row ->
                GivenRatingCard(
                    row = row,
                    isEditing = state.editingRatingId == row.id,
                    draft = state.editingCommentDraft,
                    isSaving = state.savingCommentForRatingId == row.id,
                    onStartEditing = { onStartEditing(row.id, row.reviewText) },
                    onCommentDraftChange = onCommentDraftChange,
                    onSave = onSaveComment,
                    onCancel = onCancelEdit,
                )
            }
        }
    }
}

@Composable
private fun PendingTab(state: RatingsUiState) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        if (state.pending.isEmpty()) {
            EmptyTab(stringResource(R.string.ratings_empty_pending))
        } else {
            state.pending.forEach { row -> PendingReviewCard(row) }
        }
    }
}

@Composable
private fun EmptyTab(message: String) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReceivedRatingCard(row: RatingWithRaterJson) {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RatingsTestTags.cardFor(row.id)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = row.rater?.name ?: "—",
                    style = PasabayanTextStyles.Body.medium,
                )
                Text(
                    text = stringResource(R.string.ratings_card_stars, row.rating),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            row.reviewText?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = PasabayanTextStyles.Body.small,
                )
            }
            row.createdAt?.let { date ->
                Text(
                    text = date,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GivenRatingCard(
    row: RatingWithRatedJson,
    isEditing: Boolean,
    draft: String,
    isSaving: Boolean,
    onStartEditing: () -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RatingsTestTags.cardFor(row.id)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = row.rated?.name ?: "—",
                    style = PasabayanTextStyles.Body.medium,
                )
                Text(
                    text = stringResource(R.string.ratings_card_stars, row.rating),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (isEditing) {
                POutlinedTextField(
                    value = draft,
                    onValueChange = onCommentDraftChange,
                    label = { Text(stringResource(R.string.ratings_comment_placeholder)) },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    PButton(
                        text = stringResource(R.string.ratings_cancel_edit),
                        onClick = onCancel,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSaving) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            PCircularProgress()
                        }
                    } else {
                        PButton(
                            text = stringResource(R.string.ratings_save_comment),
                            onClick = onSave,
                            enabled = draft.trim().isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                row.reviewText?.takeIf { it.isNotBlank() }?.let { text ->
                    Text(text = text, style = PasabayanTextStyles.Body.small)
                }
                PButton(
                    text = stringResource(R.string.ratings_edit_comment),
                    onClick = onStartEditing,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PendingReviewCard(row: PendingReviewJson) {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RatingsTestTags.cardFor(row.matchId)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(text = row.otherUser.name, style = PasabayanTextStyles.Body.medium)
            row.route?.let {
                Text(
                    text = stringResource(R.string.ratings_card_pending_route, it),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            row.packageDescription?.let {
                Text(
                    text = stringResource(R.string.ratings_card_pending_description, it),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            row.daysSinceDelivery?.let {
                Text(
                    text = stringResource(R.string.ratings_card_pending_days, it.toInt()),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Ratings — light")
@Preview(showBackground = true, name = "Ratings — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RatingsPreview() {
    PasabayanTheme {
        RatingsContent(
            state = RatingsUiState(
                receivedSummary = UserRatingSummaryJson(
                    id = 1,
                    name = "Alex Stewart",
                    rating = "4.7",
                    totalRatings = 32,
                ),
                received = listOf(
                    RatingWithRaterJson(
                        id = 1,
                        rating = 5,
                        reviewText = "Excellent communication.",
                        rater = RatingUserInfoJson(id = 2, name = "Pat"),
                        createdAt = "2026-04-01",
                    ),
                ),
                given = listOf(
                    RatingWithRatedJson(
                        id = 7,
                        rating = 4,
                        reviewText = "Great delivery.",
                        rated = RatingUserInfoJson(id = 11, name = "Sam"),
                    ),
                ),
                pending = listOf(
                    PendingReviewJson(
                        matchId = 17,
                        otherUser = PendingReviewOtherUserJson(id = 13, name = "Casey"),
                        deliveryDate = "2026-04-15",
                        packageDescription = "Books",
                        route = "Montreal → Toronto",
                        daysSinceDelivery = 2.0,
                    ),
                ),
            ),
            onTabSelected = {},
            onStartEditing = { _, _ -> },
            onCommentDraftChange = {},
            onSaveComment = {},
            onCancelEdit = {},
        )
    }
}
