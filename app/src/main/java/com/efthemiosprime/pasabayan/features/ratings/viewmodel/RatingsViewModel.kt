package com.efthemiosprime.pasabayan.features.ratings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.ratings.model.RatingsTab
import com.efthemiosprime.pasabayan.features.ratings.model.RatingsUiState
import com.efthemiosprime.pasabayan.features.ratings.services.RatingsRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the Feedback (My ratings / Given / Pending) tabbed screen. Per `11-favorites-ratings.md`,
 * the three lists are paginated separately on the backend but this slice fetches page 1 only —
 * pagination can be added once a scrolling list demands it.
 */
@HiltViewModel
class RatingsViewModel @Inject constructor(
    private val repository: RatingsRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(RatingsUiState())
    val state: StateFlow<RatingsUiState> = _state.asStateFlow()

    private var loadedUserId: Int? = null

    fun load(userId: Int) {
        loadedUserId = userId
        loadAll(userId)
    }

    fun selectTab(tab: RatingsTab) {
        if (_state.value.selectedTab == tab) return
        _state.update { it.copy(selectedTab = tab) }
    }

    fun startEditing(ratingId: Int, currentText: String?) {
        _state.update {
            it.copy(
                editingRatingId = ratingId,
                editingCommentDraft = currentText.orEmpty(),
                errorMessage = null,
            )
        }
    }

    fun onCommentDraftChange(value: String) {
        _state.update { it.copy(editingCommentDraft = value, errorMessage = null) }
    }

    fun cancelEditing() {
        _state.update { it.copy(editingRatingId = null, editingCommentDraft = "") }
    }

    fun saveComment() {
        val snapshot = _state.value
        val ratingId = snapshot.editingRatingId ?: return
        val text = snapshot.editingCommentDraft.trim()
        if (text.isEmpty()) {
            cancelEditing()
            return
        }
        _state.update { it.copy(savingCommentForRatingId = ratingId) }
        viewModelScope.launch {
            repository.updateRatingComment(ratingId, text).fold(
                onSuccess = {
                    _state.update {
                        val updatedGiven = it.given.map { row ->
                            if (row.id == ratingId) row.copy(reviewText = text) else row
                        }
                        it.copy(
                            given = updatedGiven,
                            editingRatingId = null,
                            editingCommentDraft = "",
                            savingCommentForRatingId = null,
                            infoMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.ratings_comment_updated,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(savingCommentForRatingId = null) }
                    applyError(throwable)
                },
            )
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun loadAll(userId: Int) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val receivedDeferred = repository.fetchReceivedRatings(userId = userId)
            val givenDeferred = repository.fetchGivenRatings()
            val pendingDeferred = repository.fetchPendingReviews()
            val firstFailure = listOf(receivedDeferred, givenDeferred, pendingDeferred)
                .firstOrNull { it.isFailure }
            _state.update {
                it.copy(
                    isLoading = false,
                    receivedSummary = receivedDeferred.getOrNull()?.user,
                    received = receivedDeferred.getOrNull()?.ratings?.data ?: it.received,
                    given = givenDeferred.getOrNull() ?: it.given,
                    pending = pendingDeferred.getOrNull() ?: it.pending,
                    errorMessage = firstFailure?.exceptionOrNull()?.let { e ->
                        ((e as? DomainErrorMapperException)?.domainError
                            ?: DomainError.NetworkError(e)).localizedMessage(appContext)
                    },
                )
            }
        }
    }

    private fun applyError(throwable: Throwable) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _state.update { it.copy(errorMessage = err.localizedMessage(appContext)) }
    }
}
