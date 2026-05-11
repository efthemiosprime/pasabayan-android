package com.efthemiosprime.pasabayan.features.favorites.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierInfoJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierRequestJson
import com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson
import com.efthemiosprime.pasabayan.features.favorites.model.FavoritesSort
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesError
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getString(R.string.favorites_removed) } returns "removed"
        every { context.getString(R.string.favorites_error_already_favorited) } returns "already"
        every { context.getString(R.string.favorites_error_not_found) } returns "not found"
        every { context.getString(R.string.favorites_error_cannot_favorite) } returns "cannot favorite"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load populates favorites and sends default sort`() = runTest(dispatcher) {
        val repo = FakeRepo(
            favorites = Result.success(
                listOf(sampleFavorite(carrierId = 1)),
            ),
        )
        val vm = FavoritesListViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.favorites.size)
        assertEquals("recent", repo.lastSort)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `setSort triggers a forced reload`() = runTest(dispatcher) {
        val repo = FakeRepo(favorites = Result.success(emptyList()))
        val vm = FavoritesListViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        vm.setSort(FavoritesSort.MOST_USED)
        advanceUntilIdle()
        assertEquals("most_used", repo.lastSort)
        assertEquals(2, repo.fetchCallCount)
    }

    @Test
    fun `setOnlyUpcomingTrips threads the filter through to repo`() = runTest(dispatcher) {
        val repo = FakeRepo(favorites = Result.success(emptyList()))
        val vm = FavoritesListViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        vm.setOnlyUpcomingTrips(true)
        advanceUntilIdle()
        assertEquals(true, repo.lastHasUpcomingTrips)
    }

    @Test
    fun `removeFavorite drops the carrier from the list on success`() = runTest(dispatcher) {
        val repo = FakeRepo(
            favorites = Result.success(
                listOf(sampleFavorite(carrierId = 5), sampleFavorite(carrierId = 7)),
            ),
        )
        val vm = FavoritesListViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        vm.removeFavorite(5)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.favorites.size)
        assertEquals(7, vm.state.value.favorites.first().carrier.id)
        assertEquals("removed", vm.state.value.infoMessage)
    }

    @Test
    fun `removeFavorite failure keeps the list intact and surfaces error`() =
        runTest(dispatcher) {
            val repo = FakeRepo(
                favorites = Result.success(listOf(sampleFavorite(carrierId = 5))),
                removeResult = Result.failure(FavoritesError.NotFound),
            )
            val vm = FavoritesListViewModel(repo, context)
            vm.load()
            advanceUntilIdle()
            vm.removeFavorite(5)
            advanceUntilIdle()
            assertEquals(1, vm.state.value.favorites.size)
            assertEquals("not found", vm.state.value.errorMessage)
        }

    @Test
    fun `load failure surfaces error from network`() = runTest(dispatcher) {
        val repo = FakeRepo(
            favorites = Result.failure(IllegalStateException("oops")),
        )
        val vm = FavoritesListViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        assertNotNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    private fun sampleFavorite(carrierId: Int) = FavoriteCarrierJson(
        id = carrierId * 100,
        carrier = FavoriteCarrierInfoJson(id = carrierId, name = "Alex $carrierId"),
    )
}

private class FakeRepo(
    private val favorites: Result<List<FavoriteCarrierJson>>,
    private val removeResult: Result<Unit> = Result.success(Unit),
) : FavoritesRepository {

    var lastSort: String? = null
        private set
    var lastHasUpcomingTrips: Boolean? = null
        private set
    var fetchCallCount: Int = 0
        private set

    override suspend fun fetchFavorites(
        sort: String,
        hasUpcomingTrips: Boolean?,
    ): Result<List<FavoriteCarrierJson>> {
        lastSort = sort
        lastHasUpcomingTrips = hasUpcomingTrips
        fetchCallCount++
        return favorites
    }

    override suspend fun addFavorite(
        carrierId: Int,
        notes: String?,
        notificationEnabled: Boolean?,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun removeFavorite(carrierId: Int): Result<Unit> = removeResult
    override suspend fun isFavorite(carrierId: Int): Result<Boolean> = Result.success(true)
    override suspend fun sendDeliveryRequest(
        carrierId: Int,
        request: SendDeliveryRequestJson,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun fetchSentRequests(): Result<List<FavoriteCarrierRequestJson>> =
        Result.success(emptyList())
}
