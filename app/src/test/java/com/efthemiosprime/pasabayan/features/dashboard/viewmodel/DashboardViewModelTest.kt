package com.efthemiosprime.pasabayan.features.dashboard.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import org.junit.Assert.assertEquals
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
