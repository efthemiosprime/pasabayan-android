package com.efthemiosprime.pasabayan.features.ratings.model

import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserRatingSummaryJson

enum class RatingsTab {
    RECEIVED, GIVEN, PENDING,
}

data class RatingsUiState(
    val selectedTab: RatingsTab = RatingsTab.RECEIVED,
    val receivedSummary: UserRatingSummaryJson? = null,
    val received: List<RatingWithRaterJson> = emptyList(),
    val given: List<RatingWithRatedJson> = emptyList(),
    val pending: List<PendingReviewJson> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val editingRatingId: Int? = null,
    val editingCommentDraft: String = "",
    val savingCommentForRatingId: Int? = null,
)
