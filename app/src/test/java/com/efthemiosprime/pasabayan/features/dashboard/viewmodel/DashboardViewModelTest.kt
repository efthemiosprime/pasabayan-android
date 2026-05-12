package com.efthemiosprime.pasabayan.features.dashboard.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.model.DashboardSheetRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        viewModel = DashboardViewModel()
    }

    @Test
    fun `initial state defaults to shipper role and tab 0`() {
        val state = viewModel.uiState.value
        assertEquals(UserRole.SHIPPER, state.currentRole)
        assertEquals(0, state.selectedTabIndex)
    }

    @Test
    fun `initializeRole sets carrier when user is active carrier`() {
        viewModel.initializeRole(testUser(isActiveCarrier = true, isActiveShipper = false))
        assertEquals(UserRole.CARRIER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `initializeRole sets shipper when user is active shipper only`() {
        viewModel.initializeRole(testUser(isActiveCarrier = false, isActiveShipper = true))
        assertEquals(UserRole.SHIPPER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `initializeRole prefers carrier when both roles active`() {
        viewModel.initializeRole(testUser(isActiveCarrier = true, isActiveShipper = true))
        assertEquals(UserRole.CARRIER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `initializeRole defaults to shipper when no roles active`() {
        viewModel.initializeRole(testUser(isActiveCarrier = false, isActiveShipper = false))
        assertEquals(UserRole.SHIPPER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `switchRole toggles from shipper to carrier`() {
        viewModel.switchRole()
        assertEquals(UserRole.CARRIER, viewModel.uiState.value.currentRole)
    }

    /**
     * Regression: `MainTabScreen`'s `LaunchedEffect(user)` calls `initializeRole` every time
     * the screen re-enters composition (e.g. after `PExpandableCardHost` dismisses an
     * expanded card). A re-run must not overwrite a user-driven `switchRole` selection.
     */
    @Test
    fun `initializeRole is idempotent — does not overwrite a later switchRole`() {
        val user = testUser(isActiveCarrier = false, isActiveShipper = true)
        viewModel.initializeRole(user)
        viewModel.switchRole()
        assertEquals(UserRole.CARRIER, viewModel.uiState.value.currentRole)

        viewModel.initializeRole(user)

        assertEquals(UserRole.CARRIER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `switchRole toggles from carrier to shipper`() {
        viewModel.initializeRole(testUser(isActiveCarrier = true))
        viewModel.switchRole()
        assertEquals(UserRole.SHIPPER, viewModel.uiState.value.currentRole)
    }

    @Test
    fun `switchRole resets tab to 0`() {
        viewModel.selectTab(3)
        assertEquals(3, viewModel.uiState.value.selectedTabIndex)
        viewModel.switchRole()
        assertEquals(0, viewModel.uiState.value.selectedTabIndex)
    }

    @Test
    fun `selectTab updates selectedTabIndex`() {
        viewModel.selectTab(2)
        assertEquals(2, viewModel.uiState.value.selectedTabIndex)
    }

    @Test
    fun `selectTrip sets selectedTripId`() {
        viewModel.selectTrip(42)
        assertEquals(42, viewModel.uiState.value.selectedTripId)
    }

    @Test
    fun `selectPackage sets selectedPackageId`() {
        viewModel.selectPackage(10)
        assertEquals(10, viewModel.uiState.value.selectedPackageId)
    }

    @Test
    fun `clearSelection resets both to null`() {
        viewModel.selectTrip(42)
        viewModel.clearSelection()
        assertNull(viewModel.uiState.value.selectedTripId)
        assertNull(viewModel.uiState.value.selectedPackageId)
    }

    @Test
    fun `selectTrip clears selectedPackageId`() {
        viewModel.selectPackage(10)
        viewModel.selectTrip(42)
        assertEquals(42, viewModel.uiState.value.selectedTripId)
        assertNull(viewModel.uiState.value.selectedPackageId)
    }

    @Test
    fun `selectPackage clears selectedTripId`() {
        viewModel.selectTrip(42)
        viewModel.selectPackage(10)
        assertEquals(10, viewModel.uiState.value.selectedPackageId)
        assertNull(viewModel.uiState.value.selectedTripId)
    }

    @Test
    fun `openTripFilterSheet sets active sheet route`() {
        viewModel.openTripFilterSheet()
        assertEquals(DashboardSheetRoute.TripFilter, viewModel.uiState.value.activeSheetRoute)
    }

    @Test
    fun `dismissActiveSheetRoute clears trip filter route`() {
        viewModel.openTripFilterSheet()
        viewModel.dismissActiveSheetRoute()
        assertNull(viewModel.uiState.value.activeSheetRoute)
    }

    @Test
    fun `openCreateTripFromPackageSheet sets package route with id`() {
        viewModel.openCreateTripFromPackageSheet(packageId = 88)
        assertEquals(
            DashboardSheetRoute.CreateTripFromPackage(packageId = 88),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `openCreateTripFromPackageSheet replaces prior package id with latest`() {
        viewModel.openCreateTripFromPackageSheet(packageId = 88)
        viewModel.openCreateTripFromPackageSheet(packageId = 99)
        assertEquals(
            DashboardSheetRoute.CreateTripFromPackage(packageId = 99),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `dismissActiveSheetRoute clears create trip from package route`() {
        viewModel.openCreateTripFromPackageSheet(packageId = 88)
        viewModel.dismissActiveSheetRoute()
        assertNull(viewModel.uiState.value.activeSheetRoute)
    }

    @Test
    fun `openEditTripSheet sets edit route with trip id`() {
        viewModel.openEditTripSheet(tripId = 42)
        assertEquals(
            DashboardSheetRoute.EditTrip(tripId = 42),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `openEditTripSheet replaces prior edit trip id with latest`() {
        viewModel.openEditTripSheet(tripId = 42)
        viewModel.openEditTripSheet(tripId = 99)
        assertEquals(
            DashboardSheetRoute.EditTrip(tripId = 99),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `dismissActiveSheetRoute clears edit trip route`() {
        viewModel.openEditTripSheet(tripId = 42)
        viewModel.dismissActiveSheetRoute()
        assertNull(viewModel.uiState.value.activeSheetRoute)
    }

    @Test
    fun `openPackageDetailSheet sets package detail route with package id`() {
        viewModel.openPackageDetailSheet(packageId = 55)
        assertEquals(
            DashboardSheetRoute.PackageDetail(packageId = 55),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `openEditPackageSheet sets edit package route with package id`() {
        viewModel.openEditPackageSheet(packageId = 56)
        assertEquals(
            DashboardSheetRoute.EditPackage(packageId = 56),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `opening a new sheet route replaces previous active route`() {
        viewModel.openTripFilterSheet()
        viewModel.openEditTripSheet(tripId = 25)
        assertEquals(
            DashboardSheetRoute.EditTrip(tripId = 25),
            viewModel.uiState.value.activeSheetRoute,
        )
    }

    @Test
    fun `all sheet routes can open in sequence and dismiss deterministically`() {
        viewModel.openTripFilterSheet()
        assertEquals(DashboardSheetRoute.TripFilter, viewModel.uiState.value.activeSheetRoute)

        viewModel.openCreateTripFromPackageSheet(packageId = 22)
        assertEquals(
            DashboardSheetRoute.CreateTripFromPackage(packageId = 22),
            viewModel.uiState.value.activeSheetRoute,
        )

        viewModel.openEditTripSheet(tripId = 33)
        assertEquals(
            DashboardSheetRoute.EditTrip(tripId = 33),
            viewModel.uiState.value.activeSheetRoute,
        )

        viewModel.openPackageDetailSheet(packageId = 44)
        assertEquals(
            DashboardSheetRoute.PackageDetail(packageId = 44),
            viewModel.uiState.value.activeSheetRoute,
        )

        viewModel.openEditPackageSheet(packageId = 45)
        assertEquals(
            DashboardSheetRoute.EditPackage(packageId = 45),
            viewModel.uiState.value.activeSheetRoute,
        )

        viewModel.dismissActiveSheetRoute()
        assertNull(viewModel.uiState.value.activeSheetRoute)
    }

    @Test
    fun `dismissActiveSheetRoute clears active route without changing tab or role`() {
        viewModel.initializeRole(testUser(isActiveCarrier = true))
        viewModel.selectTab(3)
        viewModel.openTripFilterSheet()

        viewModel.dismissActiveSheetRoute()

        val state = viewModel.uiState.value
        assertNull(state.activeSheetRoute)
        assertEquals(UserRole.CARRIER, state.currentRole)
        assertEquals(3, state.selectedTabIndex)
    }

    private fun testUser(
        isActiveCarrier: Boolean = false,
        isActiveShipper: Boolean = true,
    ) = AuthUser(
        id = 1,
        name = "Test User",
        email = "test@example.com",
        avatar = null,
        phone = null,
        phoneVerified = false,
        profileCompleted = false,
        provider = "google",
        userTypes = listOf("shipper"),
        isActiveCarrier = isActiveCarrier,
        isActiveShipper = isActiveShipper,
    )
}
