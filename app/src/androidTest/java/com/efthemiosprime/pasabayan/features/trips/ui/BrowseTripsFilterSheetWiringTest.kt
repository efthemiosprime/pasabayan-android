package com.efthemiosprime.pasabayan.features.trips.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for the filter-sheet wiring bug where
 * [BrowseTripsScreen] passed no-op lambdas to `TripFilterContent`'s
 * `onOriginChange` / `onDestinationChange`, silently dropping typed
 * origin/destination filters before they reached the ViewModel.
 *
 * VM-only unit tests in `BrowseTripsViewModelTest` already prove
 * `updateOrigin` / `updateDestination` mutate state and propagate
 * through `applyFilterAndFetch` — but they cannot catch the screen
 * dropping the callback. This test wires a real VM with inline fakes
 * and asserts the filter sheet round-trips into VM state.
 */
@RunWith(AndroidJUnit4::class)
class BrowseTripsFilterSheetWiringTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun filterSheet_typingOriginAndDestination_updatesViewModelState() {
        val viewModel = BrowseTripsViewModel(
            tripsRepository = StubTripsRepository(),
            requirePhoneVerification = RequirePhoneVerificationUseCase(StubAuthRepository()),
        )
        val filterIconLabel = composeRule.activity.getString(R.string.trips_browse_title)
        val originLabel = composeRule.activity.getString(R.string.trips_create_origin)
        val destinationLabel = composeRule.activity.getString(R.string.trips_create_destination)

        composeRule.setContent {
            PasabayanTheme {
                BrowseTripsScreen(
                    onViewTripDetails = {},
                    viewModel = viewModel,
                )
            }
        }

        // Open filter sheet via the search bar's trailing filter icon
        // (uses the same content description the screen sets).
        composeRule.onNodeWithContentDescription(filterIconLabel).performClick()

        composeRule.onNodeWithText(originLabel).performTextInput("Manila")
        composeRule.onNodeWithText(destinationLabel).performTextInput("Cebu")

        assertEquals("Manila", viewModel.uiState.value.filter.origin)
        assertEquals("Cebu", viewModel.uiState.value.filter.destination)
    }
}

/**
 * Minimal in-memory [TripsRepository] for screen tests — only
 * [loadAvailableTripsPage] returns a real (empty) page so the screen's
 * initial `LaunchedEffect(Unit) { loadAvailableTrips() }` settles
 * without crashing. Other methods are unused on this screen path.
 */
private class StubTripsRepository : TripsRepository {
    override suspend fun loadCarrierTrips(): Result<List<Trip>> =
        Result.success(emptyList())

    override suspend fun loadAvailableTrips(filter: TripFilter): Result<List<Trip>> =
        Result.success(emptyList())

    override suspend fun loadAvailableTripsPage(
        filter: TripFilter,
        page: Int,
        perPage: Int,
    ): Result<AvailableTripsPage> = Result.success(
        AvailableTripsPage(
            trips = emptyList(),
            currentPage = page,
            lastPage = page,
            total = 0,
            perPage = perPage,
        ),
    )

    override suspend fun loadPopularPackageRoutes(): Result<List<PopularRoute>> =
        Result.success(emptyList())

    override suspend fun loadRouteActivitySummary(): Result<RouteActivitySummary> =
        Result.success(RouteActivitySummary(0, 0, 0, null, null))

    override suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>> =
        Result.success(emptyList())

    override suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData> =
        Result.failure(NotImplementedError("not exercised in this screen test"))

    override suspend fun getTrip(id: Int): Result<Trip> =
        Result.failure(NotImplementedError("not exercised in this screen test"))

    override suspend fun createTrip(request: CreateTripRequestJson): Result<Trip> =
        Result.failure(NotImplementedError("not exercised in this screen test"))

    override suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip> =
        Result.failure(NotImplementedError("not exercised in this screen test"))

    override suspend fun updateTrip(id: Int, request: TripUpdateRequestJson): Result<Trip> =
        Result.failure(NotImplementedError("not exercised in this screen test"))

    override suspend fun deleteTrip(id: Int): Result<Unit> =
        Result.success(Unit)
}

/**
 * Minimal [AuthRepository] for tests that need to construct
 * [RequirePhoneVerificationUseCase]. The phone-verification gate is
 * not exercised by typing into the filter sheet, so `currentUser` can
 * stay `null` and the unused methods can throw on access.
 */
private class StubAuthRepository : AuthRepository {
    private val user = MutableStateFlow<AuthUser?>(null)
    override suspend fun loginWithProviderAccessToken(provider: String, accessToken: String) =
        Result.failure<AuthUser>(NotImplementedError("not exercised"))
    override suspend fun loadCurrentUser() =
        Result.failure<AuthUser>(NotImplementedError("not exercised"))
    override suspend fun logout() = Result.success(Unit)
    override fun currentUser(): StateFlow<AuthUser?> = user
    override fun clearCurrentUser() {}
}
