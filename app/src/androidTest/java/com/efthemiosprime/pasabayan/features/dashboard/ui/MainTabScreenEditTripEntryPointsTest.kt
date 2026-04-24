package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripCard
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.ui.TripDetailsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTabScreenEditTripEntryPointsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tripCard_editMenuAction_triggersEditEntryPoint() {
        val trip = testTrip(id = 15)
        var selectedTripId: Int? = null
        val editLabel = composeRule.activity.getString(R.string.trips_edit_trip)
        val moreOptionsLabel =
            composeRule.activity.getString(com.efthemiosprime.pasabayan.core.designsystem.R.string.ds_more_options)

        composeRule.setContent {
            PasabayanTheme {
                TripCard(
                    trip = trip,
                    onViewDetails = {},
                    menuActions = listOf(
                        CardMenuAction(
                            title = editLabel,
                            onClick = { selectedTripId = trip.id },
                        ),
                    ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(moreOptionsLabel).performClick()
        composeRule.onNodeWithText(editLabel).assertExists().performClick()

        assertEquals(15, selectedTripId)
    }

    @Test
    fun tripDetails_editButton_triggersEditEntryPoint() {
        var editClicks = 0
        val editLabel = composeRule.activity.getString(R.string.trips_edit_trip)

        composeRule.setContent {
            PasabayanTheme {
                TripDetailsScreen(
                    trip = testTrip(id = 42),
                    isCarrier = true,
                    onEdit = { editClicks += 1 },
                    onCancel = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText(editLabel).assertExists().performClick()
        assertEquals(1, editClicks)
    }

    private fun testTrip(id: Int): Trip = Trip(
        id = id,
        carrierId = 7,
        originCity = "Toronto",
        originCountry = "Canada",
        originLat = 43.6532,
        originLng = -79.3832,
        destinationCity = "Montreal",
        destinationCountry = "Canada",
        destinationLat = 45.5017,
        destinationLng = -73.5673,
        departureDate = "2026-05-01T08:00:00Z",
        arrivalDate = "2026-05-01T14:00:00Z",
        availableWeightKg = 20.0,
        availableSpaceLiters = 100.0,
        pricePerKg = 10.0,
        tripStatus = TripStatus.ACTIVE,
        transportationMethod = TransportationMethod.FLIGHT,
        specialNotes = "Fragile items only",
        carrier = null,
        createdAt = null,
        updatedAt = null,
        pricingType = null,
        pricingMethod = null,
        flatTripPrice = null,
        basePrice = null,
        calculatedPrice = null,
        pickupAddress = "1 Front St W",
        pickupLandmark = null,
        pickupInstructions = null,
        dropoffAddress = "500 Rue Saint-Paul",
        dropoffLandmark = null,
        dropoffInstructions = null,
        tripEarningsTotal = null,
        tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = false,
        pendingRequestCount = 0,
        pendingRequests = null,
        distanceKm = 542.0,
    )
}
