package com.efthemiosprime.pasabayan.features.profile.viewmodel

import com.efthemiosprime.pasabayan.core.network.profile.CarrierDeliveriesJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierEarningsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.ratings.PaginatedRatingsJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserReceivedRatingsDataJson
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesRepository
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.features.ratings.services.RatingsRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfilePopoverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var ratingsRepo: FakeRatingsRepository
    private lateinit var favoritesRepo: FakeFavoritesRepository
    private lateinit var profileRepo: FakeProfileRepository
    private lateinit var viewModel: UserProfilePopoverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ratingsRepo = FakeRatingsRepository()
        favoritesRepo = FakeFavoritesRepository()
        profileRepo = FakeProfileRepository()
        viewModel = UserProfilePopoverViewModel(
            ratingsRepository = ratingsRepo,
            favoritesRepository = favoritesRepo,
            profileRepository = profileRepo,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load fetches reviews unconditionally`() = runTest {
        ratingsRepo.receivedResult = ok(
            ratings = listOf(review(id = 1, rating = 5), review(id = 2, rating = 4)),
            currentPage = 1,
            lastPage = 1,
        )

        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.reviews.size)
        assertEquals(1, state.reviewsCurrentPage)
        assertFalse(state.reviewsHasMore)
        assertFalse(state.isLoadingReviews)
        assertEquals(0, profileRepo.carrierStatsCallCount) // not viewer self
        assertEquals(0, favoritesRepo.isFavoriteCallCount) // not shipper-viewing-carrier
    }

    @Test
    fun `load with viewerIsSelf also fetches carrier stats`() = runTest {
        ratingsRepo.receivedResult = ok(ratings = emptyList(), currentPage = 1, lastPage = 1)
        profileRepo.carrierStatsResult = Result.success(
            CarrierStatsJson(
                deliveries = CarrierDeliveriesJson(completedTrips = 4),
                earnings = CarrierEarningsJson(totalEarnings = 35.0),
            ),
        )

        viewModel.load(userId = 42, viewerIsSelf = true, shouldCheckFavorite = false)
        advanceUntilIdle()

        assertEquals(1, profileRepo.carrierStatsCallCount)
        val state = viewModel.uiState.value
        assertEquals(4, state.carrierStats!!.deliveries!!.completedTrips)
        assertEquals(35.0, state.carrierStats.earnings!!.totalEarnings!!, 0.001)
        assertFalse(state.isLoadingStats)
    }

    @Test
    fun `load with shouldCheckFavorite checks favorite status`() = runTest {
        ratingsRepo.receivedResult = ok(ratings = emptyList(), currentPage = 1, lastPage = 1)
        favoritesRepo.isFavoriteResult = Result.success(true)

        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = true)
        advanceUntilIdle()

        assertEquals(1, favoritesRepo.isFavoriteCallCount)
        assertTrue(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `loadMoreReviews appends + bumps page + dedupes by id`() = runTest {
        ratingsRepo.receivedResult = ok(
            ratings = listOf(review(1), review(2)),
            currentPage = 1,
            lastPage = 2,
        )
        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = false)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.reviewsHasMore)

        // Page 2 — overlapping id 2 should dedupe.
        ratingsRepo.receivedResult = ok(
            ratings = listOf(review(2), review(3)),
            currentPage = 2,
            lastPage = 2,
        )
        viewModel.loadMoreReviews(userId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 2, 3), state.reviews.map { it.id })
        assertEquals(2, state.reviewsCurrentPage)
        assertFalse(state.reviewsHasMore)
    }

    @Test
    fun `loadMoreReviews no-op when no more pages`() = runTest {
        ratingsRepo.receivedResult = ok(
            ratings = listOf(review(1)),
            currentPage = 1,
            lastPage = 1,
        )
        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = false)
        advanceUntilIdle()
        val before = ratingsRepo.callCount
        assertFalse(viewModel.uiState.value.reviewsHasMore)

        viewModel.loadMoreReviews(userId = 42)
        advanceUntilIdle()

        assertEquals("loadMore should not hit the repo when hasMore=false", before, ratingsRepo.callCount)
    }

    @Test
    fun `ratingDistribution counts by stars from loaded reviews`() = runTest {
        ratingsRepo.receivedResult = ok(
            ratings = listOf(
                review(1, rating = 5),
                review(2, rating = 5),
                review(3, rating = 4),
                review(4, rating = 1),
            ),
            currentPage = 1,
            lastPage = 1,
        )
        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = false)
        advanceUntilIdle()

        val dist = viewModel.uiState.value.ratingDistribution
        assertEquals(2, dist[5])
        assertEquals(1, dist[4])
        assertEquals(1, dist[1])
        assertNull(dist[3])
    }

    @Test
    fun `toggleFavorite optimistically flips and persists on success`() = runTest {
        ratingsRepo.receivedResult = ok(emptyList(), currentPage = 1, lastPage = 1)
        favoritesRepo.isFavoriteResult = Result.success(false)
        favoritesRepo.addResult = Result.success(Unit)

        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = true)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFavorite)

        viewModel.toggleFavorite(carrierId = 42)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFavorite)
        assertEquals(1, favoritesRepo.addCallCount)
    }

    @Test
    fun `toggleFavorite reverts on failure`() = runTest {
        ratingsRepo.receivedResult = ok(emptyList(), currentPage = 1, lastPage = 1)
        favoritesRepo.isFavoriteResult = Result.success(false)
        favoritesRepo.addResult = Result.failure(RuntimeException("boom"))

        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = true)
        advanceUntilIdle()

        viewModel.toggleFavorite(carrierId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("must revert on failure", state.isFavorite)
        assertEquals("boom", state.favoriteErrorMessage)
    }

    @Test
    fun `loadReviews surfaces error on failure`() = runTest {
        ratingsRepo.receivedResult = Result.failure(RuntimeException("offline"))

        viewModel.load(userId = 42, viewerIsSelf = false, shouldCheckFavorite = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingReviews)
        assertEquals("offline", state.reviewsErrorMessage)
        assertTrue(state.reviews.isEmpty())
    }

    // -- helpers --

    private fun ok(
        ratings: List<RatingWithRaterJson>,
        currentPage: Int,
        lastPage: Int,
    ): Result<UserReceivedRatingsDataJson> = Result.success(
        UserReceivedRatingsDataJson(
            user = null,
            ratings = PaginatedRatingsJson(
                currentPage = currentPage,
                lastPage = lastPage,
                data = ratings,
                total = ratings.size,
            ),
        ),
    )

    private fun review(id: Int, rating: Int = 5): RatingWithRaterJson = RatingWithRaterJson(
        id = id,
        rating = rating,
        reviewText = null,
        rater = null,
    )
}

/**
 * Local focused fake — only `fetchCarrierStats` is exercised by the popover VM.
 * Other [ProfileRepository] methods throw to flag accidental dependencies.
 */
private class FakeProfileRepository : ProfileRepository {
    var carrierStatsResult: Result<com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson?> =
        Result.success(null)
    var carrierStatsCallCount: Int = 0

    override suspend fun fetchCarrierStats(): Result<com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson?> {
        carrierStatsCallCount += 1
        return carrierStatsResult
    }

    override suspend fun fetchProfile(forceRefresh: Boolean) = error("not used")
    override suspend fun updateProfile(request: com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson) =
        error("not used")
    override suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String,
        fullName: String?,
        deliveryAddress: String?,
        preferredContactMethod: String?,
        additionalInfo: Map<String, String>?,
    ) = error("not used")
    override suspend fun deleteProfilePicture() = error("not used")
    override suspend fun requestAccountDeletion(reason: String?) = error("not used")
    override suspend fun fetchDisclaimerAcknowledgments() = error("not used")
    override suspend fun acknowledgeDisclaimer(type: String) = error("not used")
    override suspend fun fetchConsentPreferences() = error("not used")
    override suspend fun updateConsentPreferences(
        update: com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson,
    ) = error("not used")
    override suspend fun exportUserData() = error("not used")
    override suspend fun fetchCarrierProfile() = error("not used")
    override suspend fun fetchUserStats() = error("not used")
    override suspend fun toggleCarrierStatus() = error("not used")
    override suspend fun createCarrierProfile(
        body: com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson,
    ) = error("not used")
    override suspend fun updateCarrierProfile(
        body: com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson,
    ) = error("not used")
    override suspend fun enableCarrier() = error("not used")
}

private class FakeRatingsRepository : RatingsRepository {
    var receivedResult: Result<UserReceivedRatingsDataJson> = Result.success(UserReceivedRatingsDataJson())
    var callCount: Int = 0

    override suspend fun fetchReceivedRatings(
        userId: Int,
        page: Int,
        sort: String,
    ): Result<UserReceivedRatingsDataJson> {
        callCount += 1
        return receivedResult
    }

    override suspend fun fetchGivenRatings(page: Int): Result<List<com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson>> =
        Result.success(emptyList())

    override suspend fun fetchPendingReviews(page: Int): Result<List<com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson>> =
        Result.success(emptyList())

    override suspend fun updateRatingComment(ratingId: Int, reviewText: String): Result<Unit> =
        Result.success(Unit)
}

private class FakeFavoritesRepository : FavoritesRepository {
    var isFavoriteResult: Result<Boolean> = Result.success(false)
    var addResult: Result<Unit> = Result.success(Unit)
    var removeResult: Result<Unit> = Result.success(Unit)
    var isFavoriteCallCount: Int = 0
    var addCallCount: Int = 0
    var removeCallCount: Int = 0

    override suspend fun fetchFavorites(
        sort: String,
        hasUpcomingTrips: Boolean?,
    ): Result<List<com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson>> =
        Result.success(emptyList())

    override suspend fun addFavorite(
        carrierId: Int,
        notes: String?,
        notificationEnabled: Boolean?,
    ): Result<Unit> {
        addCallCount += 1
        return addResult
    }

    override suspend fun removeFavorite(carrierId: Int): Result<Unit> {
        removeCallCount += 1
        return removeResult
    }

    override suspend fun isFavorite(carrierId: Int): Result<Boolean> {
        isFavoriteCallCount += 1
        return isFavoriteResult
    }

    override suspend fun sendDeliveryRequest(
        carrierId: Int,
        request: com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun fetchSentRequests(): Result<List<com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierRequestJson>> =
        Result.success(emptyList())
}
