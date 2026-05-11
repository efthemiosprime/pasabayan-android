package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
import com.efthemiosprime.pasabayan.core.network.profile.AvailableRouteJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentDataJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentsDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.features.profile.model.EditCarrierProfileField
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditCarrierProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.profile_carrier_error_required_fields) } returns
            "Weight, space and price are required and must be non-negative"
        every { context.getString(R.string.profile_carrier_success) } returns
            "Carrier profile updated"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize prefills fields from existing carrier profile`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(
            initial = CarrierProfileJson(
                userId = 1,
                maxWeightCapacityKg = 50.0,
                maxSpaceCapacityLiters = 80.5,
                defaultPricePerKg = 5.5,
                insuranceCoverageAmount = 2500.0,
                bio = "Hello",
                preferredPackageTypes = listOf("Electronics", "Documents"),
                restrictedItems = listOf("Hazardous Materials"),
            ),
        )
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s.hasExistingProfile)
        assertEquals("50", s.maxWeightKg)
        assertEquals("80.5", s.maxSpaceLiters)
        assertEquals("5.5", s.pricePerKgCad)
        assertEquals("2500", s.insuranceCoverageCad)
        assertEquals("Hello", s.bio)
        assertEquals(setOf("Electronics", "Documents"), s.selectedPackageTypes)
        assertEquals(setOf("Hazardous Materials"), s.selectedRestrictedItems)
        assertTrue(s.isInitialized)
    }

    @Test
    fun `initialize with no existing carrier profile leaves form blank`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(initial = null)
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        val s = vm.state.value
        assertFalse(s.hasExistingProfile)
        assertEquals("", s.maxWeightKg)
        assertEquals("", s.bio)
        assertTrue(s.selectedPackageTypes.isEmpty())
    }

    @Test
    fun `save with missing required fields flags fieldErrors and does not call network`() =
        runTest(testDispatcher) {
            val repo = FakeCarrierRepo(initial = null)
            val vm = EditCarrierProfileViewModel(repo, context)
            vm.initialize()
            advanceUntilIdle()
            vm.onMaxWeightChange("")
            vm.onMaxSpaceChange("")
            vm.onPricePerKgChange("")
            vm.save()
            advanceUntilIdle()
            val s = vm.state.value
            assertTrue(EditCarrierProfileField.MAX_WEIGHT in s.fieldErrors)
            assertTrue(EditCarrierProfileField.MAX_SPACE in s.fieldErrors)
            assertTrue(EditCarrierProfileField.PRICE in s.fieldErrors)
            assertNull(repo.lastUpdate)
            assertNull(repo.lastCreate)
        }

    @Test
    fun `save with price zero is invalid and weight zero is allowed`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(initial = null)
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        vm.onMaxWeightChange("0")
        vm.onMaxSpaceChange("0")
        vm.onPricePerKgChange("0")
        vm.save()
        advanceUntilIdle()
        val s = vm.state.value
        assertFalse(EditCarrierProfileField.MAX_WEIGHT in s.fieldErrors)
        assertFalse(EditCarrierProfileField.MAX_SPACE in s.fieldErrors)
        assertTrue(EditCarrierProfileField.PRICE in s.fieldErrors)
    }

    @Test
    fun `save with no existing profile creates and enables carrier`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(initial = null)
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        vm.onMaxWeightChange("50")
        vm.onMaxSpaceChange("100")
        vm.onPricePerKgChange("5.50")
        vm.onInsuranceChange("2500")
        vm.onBioChange("Pasabayan veteran")
        vm.togglePackageType("Electronics")
        vm.toggleRestrictedItem("Hazardous Materials")
        vm.save()
        advanceUntilIdle()
        val req = repo.lastCreate
        assertNotNull(req)
        assertEquals(50.0, req!!.maxWeightCapacityKg, 0.0)
        assertEquals(100.0, req.maxSpaceCapacityLiters, 0.0)
        assertEquals(5.5, req.defaultPricePerKg, 0.0)
        assertEquals(2500.0, req.insuranceCoverageAmount!!, 0.0)
        assertEquals(listOf("Electronics"), req.preferredPackageTypes)
        assertEquals(listOf("Hazardous Materials"), req.restrictedItems)
        assertEquals("Pasabayan veteran", req.bio)
        assertTrue(repo.enableCalled)
        assertEquals("Carrier profile updated", vm.state.value.successMessage)
    }

    @Test
    fun `save with existing profile updates and does not call enableCarrier`() =
        runTest(testDispatcher) {
            val repo = FakeCarrierRepo(
                initial = CarrierProfileJson(
                    userId = 1,
                    maxWeightCapacityKg = 50.0,
                    maxSpaceCapacityLiters = 80.0,
                    defaultPricePerKg = 5.0,
                    availableRoutes = listOf(AvailableRouteJson("Toronto", "Montreal")),
                ),
            )
            val vm = EditCarrierProfileViewModel(repo, context)
            vm.initialize()
            advanceUntilIdle()
            vm.onMaxWeightChange("60")
            vm.save()
            advanceUntilIdle()
            val req = repo.lastUpdate
            assertNotNull(req)
            assertEquals(60.0, req!!.maxWeightCapacityKg, 0.0)
            // Preserves availableRoutes from existing profile so the update doesn't drop them.
            assertEquals(1, req.availableRoutes?.size)
            assertFalse(repo.enableCalled)
            assertNull(repo.lastCreate)
        }

    @Test
    fun `togglePackageType adds and removes`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(initial = null)
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        vm.togglePackageType("Books")
        assertTrue("Books" in vm.state.value.selectedPackageTypes)
        vm.togglePackageType("Books")
        assertFalse("Books" in vm.state.value.selectedPackageTypes)
    }

    @Test
    fun `bio is clamped to 500 characters`() = runTest(testDispatcher) {
        val repo = FakeCarrierRepo(initial = null)
        val vm = EditCarrierProfileViewModel(repo, context)
        vm.initialize()
        advanceUntilIdle()
        vm.onBioChange("a".repeat(600))
        assertEquals(500, vm.state.value.bio.length)
    }
}

private class FakeCarrierRepo(
    private var initial: CarrierProfileJson?,
) : ProfileRepository {
    var lastUpdate: CreateCarrierProfileRequestJson? = null
    var lastCreate: CreateCarrierProfileRequestJson? = null
    var enableCalled: Boolean = false

    override suspend fun fetchCarrierProfile() = Result.success(initial)

    override suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        lastUpdate = body
        val updated = (initial ?: CarrierProfileJson()).copy(
            maxWeightCapacityKg = body.maxWeightCapacityKg,
            maxSpaceCapacityLiters = body.maxSpaceCapacityLiters,
            defaultPricePerKg = body.defaultPricePerKg,
            insuranceCoverageAmount = body.insuranceCoverageAmount,
            preferredPackageTypes = body.preferredPackageTypes,
            restrictedItems = body.restrictedItems,
            availableRoutes = body.availableRoutes,
            bio = body.bio,
        )
        initial = updated
        return Result.success(updated)
    }

    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        lastCreate = body
        val created = CarrierProfileJson(
            userId = 1,
            maxWeightCapacityKg = body.maxWeightCapacityKg,
            maxSpaceCapacityLiters = body.maxSpaceCapacityLiters,
            defaultPricePerKg = body.defaultPricePerKg,
            insuranceCoverageAmount = body.insuranceCoverageAmount,
            preferredPackageTypes = body.preferredPackageTypes,
            restrictedItems = body.restrictedItems,
            availableRoutes = body.availableRoutes,
            bio = body.bio,
        )
        initial = created
        return Result.success(created)
    }

    override suspend fun enableCarrier(): Result<Unit> {
        enableCalled = true
        return Result.success(Unit)
    }

    override suspend fun fetchProfile(forceRefresh: Boolean) = error("unused")
    override suspend fun updateProfile(request: UpdateProfileRequestJson) = error("unused")
    override suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String,
        fullName: String?,
        deliveryAddress: String?,
        preferredContactMethod: String?,
        additionalInfo: Map<String, String>?,
    ) = error("unused")
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
    override suspend fun toggleCarrierStatus() = error("unused")
}
