package com.efthemiosprime.pasabayan.features.dashboard.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateTripFromPackageRouteActionsTest {

    @Test
    fun `onClose only dismisses active route`() {
        var dismissCalls = 0
        var refreshTripsCalls = 0
        var refreshPackagesCalls = 0
        val actions = CreateTripFromPackageRouteActions(
            dismissRoute = { dismissCalls += 1 },
            refreshCarrierTrips = { refreshTripsCalls += 1 },
            refreshPackages = { refreshPackagesCalls += 1 },
        )

        actions.onClose()

        assertEquals(1, dismissCalls)
        assertEquals(0, refreshTripsCalls)
        assertEquals(0, refreshPackagesCalls)
    }

    @Test
    fun `onTripCreated dismisses route and refreshes trips and packages`() {
        var dismissCalls = 0
        var refreshTripsCalls = 0
        var refreshPackagesCalls = 0
        val actions = CreateTripFromPackageRouteActions(
            dismissRoute = { dismissCalls += 1 },
            refreshCarrierTrips = { refreshTripsCalls += 1 },
            refreshPackages = { refreshPackagesCalls += 1 },
        )

        actions.onTripCreated(tripId = 101)

        assertEquals(1, dismissCalls)
        assertEquals(1, refreshTripsCalls)
        assertEquals(1, refreshPackagesCalls)
    }
}
