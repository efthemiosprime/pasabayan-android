package com.efthemiosprime.pasabayan.features.trips.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
import com.efthemiosprime.pasabayan.core.network.profile.AvailableRouteJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatusDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentDataJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentsDataJson
import com.efthemiosprime.pasabayan.core.network.profile.PreferredPickupCityJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.locations.model.LocationCatalogSnapshot
import com.efthemiosprime.pasabayan.features.locations.services.LocationCatalogRepository
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.features.trips.services.CarrierPreferencesFormStore
import com.efthemiosprime.pasabayan.features.trips.services.UsualTransportStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class CarrierPreferencesFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private lateinit var profileRepo: FakeCarrierProfileRepository
    private lateinit var authRepo: FakeAuthRepository
    private lateinit var locationRepo: FakeLocationCatalogRepository
    private lateinit var usualTransportStore: UsualTransportStore
    private lateinit var acknowledgedStore: CarrierPreferencesFormStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.carrier_prefs_validation_max_weight_required) } returns
            "Max weight is required (0–999.99 kg)."
        every { context.getString(R.string.carrier_prefs_validation_max_space_invalid) } returns
            "Max space must be between 0 and 999.99 liters."
        profileRepo = FakeCarrierProfileRepository()
        authRepo = FakeAuthRepository().apply { setUser(AUTH_USER) }
        locationRepo = FakeLocationCatalogRepository(
            LocationCatalogSnapshot(
                version = "v1",
                cityData = mapOf("CA" to listOf("Montreal, QC", "Toronto, ON")),
                aliasData = emptyMap(),
                metroAreas = emptyMap(),
                cityIds = mapOf("Montreal, QC" to 101, "Toronto, ON" to 202),
            ),
        )
        usualTransportStore = mockk(relaxed = true)
        acknowledgedStore = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newVm(): CarrierPreferencesFormViewModel = CarrierPreferencesFormViewModel(
        profileRepository = profileRepo,
        authRepository = authRepo,
        locationCatalogRepository = locationRepo,
        usualTransportStore = usualTransportStore,
        acknowledgedStore = acknowledgedStore,
        appContext = context,
    )

    @Test
    fun `load with no existing profile leaves form blank and sets initialized`() =
        runTest(testDispatcher) {
            profileRepo.initial = null
            val vm = newVm()
            vm.load()
            advanceUntilIdle()
            val s = vm.state.value
            assertTrue(s.isInitialized)
            assertFalse(s.hasExistingProfile)
            assertEquals("", s.preferredPickupCityText)
            assertNull(s.preferredPickupCityId)
            assertEquals("", s.maxWeightKg)
            assertEquals("", s.maxSpaceLiters)
            assertEquals(TransportationMethod.CAR, s.usualTransport)
        }

    @Test
    fun `load prefills fields from existing carrier profile`() = runTest(testDispatcher) {
        profileRepo.initial = CarrierProfileJson(
            id = 7,
            userId = AUTH_USER.id.toInt(),
            maxWeightCapacityKg = 25.0,
            maxSpaceCapacityLiters = 80.0,
            preferredPickupCityId = 101,
            preferredPickupCity = PreferredPickupCityJson(id = 101, name = "Montreal", stateCode = "QC"),
        )
        every { usualTransportStore.get(AUTH_USER.id.toInt()) } returns TransportationMethod.VAN
        val vm = newVm()
        vm.load()
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s.hasExistingProfile)
        assertEquals("Montreal, QC", s.preferredPickupCityText)
        assertEquals(101, s.preferredPickupCityId)
        assertEquals("25", s.maxWeightKg)
        assertEquals("80", s.maxSpaceLiters)
        assertEquals(TransportationMethod.VAN, s.usualTransport)
    }

    @Test
    fun `save with blank max weight sets validation error and does not call network`() =
        runTest(testDispatcher) {
            val vm = newVm().also { it.load(); advanceUntilIdle() }
            vm.onMaxWeightChange("")
            vm.save()
            advanceUntilIdle()
            assertEquals(
                "Max weight is required (0–999.99 kg).",
                vm.state.value.errorMessage,
            )
            assertNull(profileRepo.lastCreate)
            assertNull(profileRepo.lastUpdate)
        }

    @Test
    fun `save with weight above max sets validation error`() = runTest(testDispatcher) {
        val vm = newVm().also { it.load(); advanceUntilIdle() }
        vm.onMaxWeightChange("1000")
        vm.save()
        advanceUntilIdle()
        assertEquals(
            "Max weight is required (0–999.99 kg).",
            vm.state.value.errorMessage,
        )
        assertNull(profileRepo.lastCreate)
    }

    @Test
    fun `save with invalid optional space sets validation error`() = runTest(testDispatcher) {
        val vm = newVm().also { it.load(); advanceUntilIdle() }
        vm.onMaxWeightChange("25")
        vm.onMaxSpaceChange("abc")
        vm.save()
        advanceUntilIdle()
        assertEquals(
            "Max space must be between 0 and 999.99 liters.",
            vm.state.value.errorMessage,
        )
        assertNull(profileRepo.lastCreate)
    }

    @Test
    fun `save with blank space sends zero and goes to create when no profile`() =
        runTest(testDispatcher) {
            profileRepo.initial = null
            val vm = newVm().also { it.load(); advanceUntilIdle() }
            vm.onMaxWeightChange("25")
            vm.onMaxSpaceChange("")
            vm.onPreferredPickupCityChange("Toronto, ON")
            vm.onUsualTransportChange(TransportationMethod.TRUCK)
            vm.save()
            advanceUntilIdle()
            val req = profileRepo.lastCreate
            assertNotNull(req)
            assertEquals(25.0, req!!.maxWeightCapacityKg, 0.0)
            assertEquals(0.0, req.maxSpaceCapacityLiters, 0.0)
            assertEquals(202, req.preferredPickupCityId)
            assertEquals(5.0, req.defaultPricePerKg, 0.0) // create-default fallback
            assertTrue(profileRepo.enableCalled)
            verify { usualTransportStore.set(AUTH_USER.id.toInt(), TransportationMethod.TRUCK) }
            assertTrue(vm.state.value.saved)
            assertTrue(vm.state.value.hasExistingProfile)
        }

    @Test
    fun `save with existing profile updates and does not call enableCarrier`() =
        runTest(testDispatcher) {
            profileRepo.initial = CarrierProfileJson(
                id = 9,
                userId = AUTH_USER.id.toInt(),
                maxWeightCapacityKg = 25.0,
                defaultPricePerKg = 6.0,
                preferredPackageTypes = listOf("Documents"),
                restrictedItems = listOf("Hazardous"),
                availableRoutes = listOf(AvailableRouteJson("Toronto", "Montreal")),
                bio = "Pasabayan veteran",
                insuranceCoverageAmount = 1500.0,
            )
            val vm = newVm().also { it.load(); advanceUntilIdle() }
            vm.onMaxWeightChange("30")
            vm.onMaxSpaceChange("120")
            vm.save()
            advanceUntilIdle()
            val req = profileRepo.lastUpdate
            assertNotNull(req)
            assertEquals(30.0, req!!.maxWeightCapacityKg, 0.0)
            assertEquals(120.0, req.maxSpaceCapacityLiters, 0.0)
            // Preserves all server-only fields from the existing profile.
            assertEquals(6.0, req.defaultPricePerKg, 0.0)
            assertEquals(listOf("Documents"), req.preferredPackageTypes)
            assertEquals(listOf("Hazardous"), req.restrictedItems)
            assertEquals(1, req.availableRoutes?.size)
            assertEquals("Pasabayan veteran", req.bio)
            assertEquals(1500.0, req.insuranceCoverageAmount!!, 0.0)
            assertFalse(profileRepo.enableCalled)
            assertNull(profileRepo.lastCreate)
            assertTrue(vm.state.value.saved)
        }

    @Test
    fun `save with cleared city sends null preferred_pickup_city_id`() = runTest(testDispatcher) {
        profileRepo.initial = CarrierProfileJson(
            id = 11,
            userId = AUTH_USER.id.toInt(),
            preferredPickupCityId = 202,
            preferredPickupCity = PreferredPickupCityJson(id = 202, name = "Toronto", stateCode = "ON"),
            maxWeightCapacityKg = 40.0,
        )
        val vm = newVm().also { it.load(); advanceUntilIdle() }
        assertEquals("Toronto, ON", vm.state.value.preferredPickupCityText)
        vm.onPreferredPickupCityChange("")
        vm.save()
        advanceUntilIdle()
        val req = profileRepo.lastUpdate
        assertNotNull(req)
        assertNull(req!!.preferredPickupCityId)
    }

    @Test
    fun `save surfaces repository error as user-facing message`() = runTest(testDispatcher) {
        profileRepo.initial = null
        profileRepo.createResult = Result.failure(
            DomainErrorMapperException(DomainError.ServerError("boom")),
        )
        val vm = newVm().also { it.load(); advanceUntilIdle() }
        vm.onMaxWeightChange("25")
        vm.save()
        advanceUntilIdle()
        val s = vm.state.value
        assertFalse(s.isSaving)
        assertFalse(s.saved)
        assertNotNull(s.errorMessage)
    }

    @Test
    fun `isAcknowledged and markAcknowledged delegate to the store`() {
        every { acknowledgedStore.isAcknowledged(42L) } returns true
        val vm = newVm()
        assertTrue(vm.isAcknowledged(42L))
        vm.markAcknowledged(42L)
        verify { acknowledgedStore.markAcknowledged(42L) }
    }

    private companion object {
        val AUTH_USER = AuthUser(
            id = 42L,
            name = "Alex",
            email = "alex@example.com",
            avatar = null,
            phone = null,
            phoneVerified = true,
            profileCompleted = true,
            provider = "google",
            userTypes = listOf("carrier"),
            isActiveCarrier = true,
            isActiveShipper = false,
        )
    }
}

private class FakeCarrierProfileRepository(
    var initial: CarrierProfileJson? = null,
) : ProfileRepository {
    var lastCreate: CreateCarrierProfileRequestJson? = null
    var lastUpdate: CreateCarrierProfileRequestJson? = null
    var enableCalled = false
    var createResult: Result<CarrierProfileJson?>? = null
    var updateResult: Result<CarrierProfileJson?>? = null

    override suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?> = Result.success(initial)

    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        lastCreate = body
        createResult?.let { return it }
        val updated = (initial ?: CarrierProfileJson(id = 1)).copy(
            id = initial?.id ?: 1,
            preferredPickupCityId = body.preferredPickupCityId,
            maxWeightCapacityKg = body.maxWeightCapacityKg,
            maxSpaceCapacityLiters = body.maxSpaceCapacityLiters,
            preferredPackageTypes = body.preferredPackageTypes,
            restrictedItems = body.restrictedItems,
            defaultPricePerKg = body.defaultPricePerKg,
            availableRoutes = body.availableRoutes,
            insuranceCoverageAmount = body.insuranceCoverageAmount,
            bio = body.bio,
        )
        initial = updated
        return Result.success(updated)
    }

    override suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        lastUpdate = body
        updateResult?.let { return it }
        val updated = (initial ?: CarrierProfileJson(id = 1)).copy(
            preferredPickupCityId = body.preferredPickupCityId,
            maxWeightCapacityKg = body.maxWeightCapacityKg,
            maxSpaceCapacityLiters = body.maxSpaceCapacityLiters,
            preferredPackageTypes = body.preferredPackageTypes,
            restrictedItems = body.restrictedItems,
            defaultPricePerKg = body.defaultPricePerKg,
            availableRoutes = body.availableRoutes,
            insuranceCoverageAmount = body.insuranceCoverageAmount,
            bio = body.bio,
        )
        initial = updated
        return Result.success(updated)
    }

    override suspend fun enableCarrier(): Result<Unit> {
        enableCalled = true
        return Result.success(Unit)
    }

    override suspend fun fetchProfile(forceRefresh: Boolean): Result<ProfileDataJson> = error("unused")
    override suspend fun updateProfile(request: UpdateProfileRequestJson): Result<ProfileDataJson> =
        error("unused")
    override suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String,
        fullName: String?,
        deliveryAddress: String?,
        preferredContactMethod: String?,
        additionalInfo: Map<String, String>?,
    ): Result<ProfileDataJson> = error("unused")
    override suspend fun deleteProfilePicture(): Result<Unit> = error("unused")
    override suspend fun requestAccountDeletion(reason: String?): Result<AccountDeletionDataJson> =
        error("unused")
    override suspend fun fetchDisclaimerAcknowledgments(): Result<DisclaimerAcknowledgmentsDataJson> =
        error("unused")
    override suspend fun acknowledgeDisclaimer(type: String): Result<DisclaimerAcknowledgmentDataJson> =
        error("unused")
    override suspend fun fetchConsentPreferences(): Result<ConsentPreferencesDataJson> = error("unused")
    override suspend fun updateConsentPreferences(
        update: ConsentPreferencesUpdateJson,
    ): Result<ConsentPreferencesDataJson> = error("unused")
    override suspend fun exportUserData(): Result<ByteArray> = error("unused")
    override suspend fun fetchCarrierStats(): Result<CarrierStatsJson?> = error("unused")
    override suspend fun fetchUserStats(): Result<UserStatsDataJson?> = error("unused")
    override suspend fun toggleCarrierStatus(): Result<CarrierStatusDataJson> = error("unused")
}

private class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    fun setUser(user: AuthUser?) { _currentUser.value = user }
    override fun currentUser(): StateFlow<AuthUser?> = _currentUser
    override fun clearCurrentUser() { _currentUser.value = null }
    override suspend fun loginWithProviderAccessToken(
        provider: String,
        accessToken: String,
    ): Result<AuthUser> = error("unused")
    override suspend fun loadCurrentUser(): Result<AuthUser> = error("unused")
    override suspend fun logout(): Result<Unit> = Result.success(Unit)
}

private class FakeLocationCatalogRepository(
    initial: LocationCatalogSnapshot,
) : LocationCatalogRepository {
    private val _snapshot = MutableStateFlow(initial)
    override val snapshot: StateFlow<LocationCatalogSnapshot> = _snapshot
    override val isLoaded: Boolean = true
    override suspend fun refreshIfNeeded(forceRefresh: Boolean): Result<LocationCatalogSnapshot> =
        Result.success(_snapshot.value)
}
