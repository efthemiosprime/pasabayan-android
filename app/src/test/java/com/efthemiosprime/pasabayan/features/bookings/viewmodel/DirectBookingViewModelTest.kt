package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload
import com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DirectBookingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeRepo: FakeBookingsRepositoryForDirectBooking
    private lateinit var viewModel: DirectBookingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        every { context.getString(R.string.bookings_direct_error_generic) } returns "Booking failed. Please try again."
        fakeRepo = FakeBookingsRepositoryForDirectBooking()
        viewModel = DirectBookingViewModel(context, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `form is invalid when priceAgreed is empty`() {
        assertFalse(viewModel.form.value.isValid)
    }

    @Test
    fun `form is invalid when priceAgreed parses to zero or negative`() {
        viewModel.updatePriceAgreed("0")
        assertFalse(viewModel.form.value.isValid)
        viewModel.updatePriceAgreed("-5")
        assertFalse(viewModel.form.value.isValid)
    }

    @Test
    fun `form is valid when priceAgreed parses positive`() {
        viewModel.updatePriceAgreed("120.50")
        assertTrue(viewModel.form.value.isValid)
    }

    @Test
    fun `calculatedTotal sums priceAgreed and serviceFee defensively`() {
        viewModel.updatePriceAgreed("100")
        viewModel.updateServiceFee("15.50")
        assertEquals(115.50, viewModel.form.value.calculatedTotal, 0.0001)

        viewModel.updateServiceFee("not-a-number")
        assertEquals(100.0, viewModel.form.value.calculatedTotal, 0.0001)
    }

    @Test
    fun `submit no-ops when form is invalid`() = runTest {
        viewModel.submit(tripId = 42)
        advanceUntilIdle()

        assertTrue(viewModel.submissionState.value is DirectBookingSubmissionState.Idle)
        assertTrue(fakeRepo.bookTripDirectCalls.isEmpty())
    }

    @Test
    fun `submit sends payload with calculatedTotal as priceAgreed`() = runTest {
        viewModel.updatePriceAgreed("100")
        viewModel.updateServiceFee("10")
        viewModel.updateSpaceNeeded("5.5")
        viewModel.updateWeightNeeded("2.5")
        viewModel.updatePickupLocation("123 Test St")
        viewModel.updateDeliveryLocation("456 Mock Ave")
        viewModel.updateSpecialRequirements("Handle with care")

        fakeRepo.bookTripDirectResult = Result.success(
            DirectBookingData(bookingId = 1, tripId = 42, agreedPrice = 110.0, status = "pending"),
        )

        viewModel.submit(tripId = 42)
        advanceUntilIdle()

        assertEquals(1, fakeRepo.bookTripDirectCalls.size)
        val (tripId, payload) = fakeRepo.bookTripDirectCalls[0]
        assertEquals(42, tripId)
        assertEquals(110.0, payload.priceAgreed, 0.0001)
        assertEquals(5.5, payload.spaceNeededLiters, 0.0001)
        assertEquals(2.5, payload.weightNeededKg, 0.0001)
        assertEquals("123 Test St", payload.pickupLocation)
        assertEquals("456 Mock Ave", payload.deliveryLocation)
        assertEquals("Handle with care", payload.specialRequirements)
        assertEquals(DirectBookingPayload.BOOKING_TYPE_SPACE_ONLY, payload.bookingType)
    }

    @Test
    fun `submit sends null specialRequirements when blank`() = runTest {
        viewModel.updatePriceAgreed("50")
        fakeRepo.bookTripDirectResult = Result.success(
            DirectBookingData(bookingId = 1, tripId = 7, agreedPrice = 50.0, status = "pending"),
        )
        viewModel.submit(tripId = 7)
        advanceUntilIdle()

        assertEquals(null, fakeRepo.bookTripDirectCalls[0].second.specialRequirements)
    }

    @Test
    fun `submit transitions to Success on repo success`() = runTest {
        viewModel.updatePriceAgreed("75")
        fakeRepo.bookTripDirectResult = Result.success(
            DirectBookingData(bookingId = 9001, tripId = 7, agreedPrice = 75.0, status = "pending"),
        )
        viewModel.submit(tripId = 7)
        advanceUntilIdle()

        val state = viewModel.submissionState.value
        assertTrue(state is DirectBookingSubmissionState.Success)
        assertEquals(9001, (state as DirectBookingSubmissionState.Success).booking.bookingId)
    }

    @Test
    fun `submit transitions to Error on repo failure`() = runTest {
        viewModel.updatePriceAgreed("75")
        fakeRepo.bookTripDirectResult = Result.failure(RuntimeException("trip full"))
        viewModel.submit(tripId = 7)
        advanceUntilIdle()

        val state = viewModel.submissionState.value
        assertTrue(state is DirectBookingSubmissionState.Error)
        assertEquals("trip full", (state as DirectBookingSubmissionState.Error).message)
    }

    @Test
    fun `submit uses fallback message when error has none`() = runTest {
        viewModel.updatePriceAgreed("75")
        fakeRepo.bookTripDirectResult = Result.failure(RuntimeException(null as String?))
        viewModel.submit(tripId = 7)
        advanceUntilIdle()

        assertEquals(
            "Booking failed. Please try again.",
            (viewModel.submissionState.value as DirectBookingSubmissionState.Error).message,
        )
    }

    @Test
    fun `submit is idempotent while in flight`() = runTest {
        viewModel.updatePriceAgreed("75")
        fakeRepo.bookTripDirectResult = Result.success(
            DirectBookingData(bookingId = 1, tripId = 7, agreedPrice = 75.0, status = "pending"),
        )
        viewModel.submit(tripId = 7)
        viewModel.submit(tripId = 7) // second call while still submitting
        advanceUntilIdle()

        assertEquals(1, fakeRepo.bookTripDirectCalls.size)
    }

    @Test
    fun `clearError resets only error state`() {
        viewModel.updatePriceAgreed("75")
        viewModel.clearError() // Idle → stays Idle
        assertTrue(viewModel.submissionState.value is DirectBookingSubmissionState.Idle)
    }
}

private class FakeBookingsRepositoryForDirectBooking : BookingsRepository {
    var bookTripDirectResult: Result<DirectBookingData> = Result.failure(Exception("Not set"))
    val bookTripDirectCalls: MutableList<Pair<Int, DirectBookingPayload>> = mutableListOf()

    override suspend fun bookTripDirect(
        tripId: Int,
        payload: DirectBookingPayload,
    ): Result<DirectBookingData> {
        bookTripDirectCalls += tripId to payload
        return bookTripDirectResult
    }

    override suspend fun loadMatches(role: String?, status: String?) =
        Result.success(emptyList<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>())
    override suspend fun getMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult>(Exception("Not used"))
    override suspend fun cancelMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult>(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperAcceptCarrierRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun retryAutoCharge(matchId: Int): Result<Unit> = Result.failure(Exception("Not used"))
    override suspend fun shipperRequestTrip(
        packageId: Int, tripId: Int, offeredPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult>(Exception("Not used"))
    override suspend fun carrierRequestPackage(
        tripId: Int, packageId: Int, proposedPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult>(Exception("Not used"))
    override suspend fun getCarrierLocation(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot>(Exception("Not used"))
    override suspend fun getCompatibleTrips(packageRequestId: Int, carrierId: Int?) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip>>(Exception("Not used"))
    override suspend fun getCompatiblePackages(tripId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.packages.model.PackageRequest>>(Exception("Not used"))
    override suspend fun getReceiverAccess(matchId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>>(Exception("Not used"))
    override suspend fun createReceiverAccess(matchId: Int, generatePin: Boolean, receiverName: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>(Exception("Not used"))
    override suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
    override suspend fun submitRating(matchId: Int, rating: Int, reviewText: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
}
