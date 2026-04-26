package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UserProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowFavoritesMenu
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVehicleInfo
import com.efthemiosprime.pasabayan.features.profile.model.shouldShowVerificationCard
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileTabViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeProfileRepository
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeProfileRepository()
        mockContext = mockk(relaxed = true) {
            every { applicationContext } returns this
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleUser = AuthUser(
        id = 1L,
        name = "N",
        email = "a@b.c",
        avatar = null,
        phone = null,
        phoneVerified = false,
        profileCompleted = true,
        provider = "g",
        userTypes = listOf("shipper"),
        isActiveCarrier = true,
        isActiveShipper = true,
    )

    private fun createVm() = ProfileTabViewModel(
        profileRepository = fakeRepo,
        appContext = mockContext,
    )

    @Test
    fun `loadTabData fetches profile and user stats for shipper`() = runTest {
        val vm = createVm()
        vm.loadTabData(sampleUser, UserRole.SHIPPER)
        advanceUntilIdle()
        val s = vm.uiState.value
        assertEquals("A", s.userProfile?.fullName)
        assertEquals(3, s.userStats?.packagesCount)
    }

    @Test
    fun `loadTabData fetches carrier profile and stats for carrier`() = runTest {
        val vm = createVm()
        vm.loadTabData(sampleUser, UserRole.CARRIER)
        advanceUntilIdle()
        val s = vm.uiState.value
        assertEquals(1, s.carrierProfile?.userId)
        assertEquals(2, s.carrierStats?.deliveries?.totalTrips)
    }

    @Test
    fun `onAvatarChanged rotates cache buster`() = runTest {
        val vm = createVm()
        val a = vm.uiState.value.avatarCacheBuster
        vm.onAvatarChanged()
        assertNotEquals(a, vm.uiState.value.avatarCacheBuster)
    }

    @Test
    fun `visibility helpers match spec`() {
        assertTrue(shouldShowFavoritesMenu(UserRole.SHIPPER))
        assertFalse(shouldShowFavoritesMenu(UserRole.CARRIER))
        assertTrue(shouldShowVehicleInfo(UserRole.CARRIER))
        assertFalse(shouldShowVehicleInfo(UserRole.SHIPPER))
        assertTrue(shouldShowVerificationCard("basic"))
        assertTrue(shouldShowVerificationCard("verified"))
        assertFalse(shouldShowVerificationCard("premium"))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class FakeProfileRepository : ProfileRepository {
    var fetchProfileCalls = 0
    var profileResponse: Result<ProfileDataJson> = Result.success(
        ProfileDataJson(
            profile = UserProfileJson(fullName = "A", verificationLevel = "basic"),
        ),
    )

    override suspend fun fetchProfile(forceRefresh: Boolean) = kotlin.run {
        fetchProfileCalls++
        profileResponse
    }

    override suspend fun fetchCarrierProfile() = Result.success(
        CarrierProfileJson(
            userId = 1,
        ),
    )

    override suspend fun fetchCarrierStats() = Result.success(
        CarrierStatsJson(
            deliveries = com.efthemiosprime.pasabayan.core.network.profile.CarrierDeliveriesJson(
                totalTrips = 2,
            ),
        ),
    )

    override suspend fun fetchUserStats() = Result.success(
        UserStatsDataJson(packagesCount = 3),
    )

    override suspend fun toggleCarrierStatus() = error("unused")

    override suspend fun createCarrierProfile(
        body: com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson,
    ) = error("unused")
}
