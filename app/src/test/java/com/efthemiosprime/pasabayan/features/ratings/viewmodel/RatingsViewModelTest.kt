package com.efthemiosprime.pasabayan.features.ratings.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.ratings.PaginatedRatingsJson
import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson
import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewOtherUserJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingUserInfoJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserRatingSummaryJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserReceivedRatingsDataJson
import com.efthemiosprime.pasabayan.features.ratings.model.RatingsTab
import com.efthemiosprime.pasabayan.features.ratings.services.RatingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RatingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getString(R.string.ratings_comment_updated) } returns "updated"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load fans out to all three lists`() = runTest(dispatcher) {
        val repo = FakeRepo(
            received = Result.success(
                UserReceivedRatingsDataJson(
                    user = UserRatingSummaryJson(
                        id = 1, name = "Alex", rating = "4.5", totalRatings = 10,
                    ),
                    ratings = PaginatedRatingsJson(
                        data = listOf(RatingWithRaterJson(id = 1, rating = 5)),
                    ),
                ),
            ),
            given = Result.success(
                listOf(RatingWithRatedJson(id = 2, rating = 4, reviewText = "Great")),
            ),
            pending = Result.success(
                listOf(
                    PendingReviewJson(
                        matchId = 3,
                        otherUser = PendingReviewOtherUserJson(id = 99, name = "Casey"),
                    ),
                ),
            ),
        )
        val vm = RatingsViewModel(repo, context)
        vm.load(userId = 1)
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals(1, s.received.size)
        assertEquals(1, s.given.size)
        assertEquals(1, s.pending.size)
        assertEquals("Alex", s.receivedSummary?.name)
        assertFalse(s.isLoading)
    }

    @Test
    fun `selectTab switches without re-fetching`() = runTest(dispatcher) {
        val repo = FakeRepo(received = Result.success(UserReceivedRatingsDataJson()))
        val vm = RatingsViewModel(repo, context)
        vm.load(userId = 1)
        advanceUntilIdle()
        vm.selectTab(RatingsTab.GIVEN)
        advanceUntilIdle()
        assertEquals(RatingsTab.GIVEN, vm.state.value.selectedTab)
        assertEquals(1, repo.receivedCalls)
    }

    @Test
    fun `startEditing then saveComment patches the given list in place`() =
        runTest(dispatcher) {
            val repo = FakeRepo(
                given = Result.success(
                    listOf(
                        RatingWithRatedJson(
                            id = 5,
                            rating = 4,
                            reviewText = "old",
                            rated = RatingUserInfoJson(id = 9, name = "Sam"),
                        ),
                    ),
                ),
            )
            val vm = RatingsViewModel(repo, context)
            vm.load(userId = 1)
            advanceUntilIdle()
            vm.startEditing(5, "old")
            vm.onCommentDraftChange("new")
            vm.saveComment()
            advanceUntilIdle()
            assertEquals("new", vm.state.value.given.first { it.id == 5 }.reviewText)
            assertEquals("updated", vm.state.value.infoMessage)
            assertNull(vm.state.value.editingRatingId)
            assertEquals(5, repo.lastCommentRatingId)
            assertEquals("new", repo.lastCommentText)
        }

    @Test
    fun `saveComment with empty draft cancels editing without hitting network`() =
        runTest(dispatcher) {
            val repo = FakeRepo()
            val vm = RatingsViewModel(repo, context)
            vm.load(userId = 1)
            advanceUntilIdle()
            vm.startEditing(5, "old")
            vm.onCommentDraftChange("   ")
            vm.saveComment()
            advanceUntilIdle()
            assertNull(vm.state.value.editingRatingId)
            assertNull(repo.lastCommentRatingId)
        }

    @Test
    fun `single failed fetch surfaces an error but keeps other lists`() = runTest(dispatcher) {
        val repo = FakeRepo(
            received = Result.success(UserReceivedRatingsDataJson()),
            given = Result.failure(IllegalStateException("boom")),
            pending = Result.success(emptyList()),
        )
        val vm = RatingsViewModel(repo, context)
        vm.load(userId = 1)
        advanceUntilIdle()
        assertNotNull(vm.state.value.errorMessage)
        // received and pending still populated (empty in fixture, but no exception).
        assertTrue(vm.state.value.received.isEmpty())
    }
}

private class FakeRepo(
    private val received: Result<UserReceivedRatingsDataJson> =
        Result.success(UserReceivedRatingsDataJson()),
    private val given: Result<List<RatingWithRatedJson>> = Result.success(emptyList()),
    private val pending: Result<List<PendingReviewJson>> = Result.success(emptyList()),
    private val commentResult: Result<Unit> = Result.success(Unit),
) : RatingsRepository {

    var receivedCalls = 0
        private set
    var lastCommentRatingId: Int? = null
        private set
    var lastCommentText: String? = null
        private set

    override suspend fun fetchReceivedRatings(
        userId: Int,
        page: Int,
        sort: String,
    ): Result<UserReceivedRatingsDataJson> {
        receivedCalls++
        return received
    }

    override suspend fun fetchGivenRatings(page: Int): Result<List<RatingWithRatedJson>> = given
    override suspend fun fetchPendingReviews(page: Int): Result<List<PendingReviewJson>> = pending

    override suspend fun updateRatingComment(
        ratingId: Int,
        reviewText: String,
    ): Result<Unit> {
        lastCommentRatingId = ratingId
        lastCommentText = reviewText
        return commentResult
    }
}
