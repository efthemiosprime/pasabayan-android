package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
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
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ContactMethod
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
class EditUserProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.profile_edit_error_name_required) } returns
            "Full name is required"
        every { context.getString(R.string.profile_edit_success) } returns "Profile updated"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun authUser(
        email: String = "alex@example.com",
        phoneVerified: Boolean = false,
    ) = AuthUser(
        id = 1,
        name = "Alex Stewart",
        email = email,
        avatar = null,
        phone = null,
        phoneVerified = phoneVerified,
        profileCompleted = false,
        provider = "google",
        userTypes = listOf("shipper"),
        isActiveCarrier = false,
        isActiveShipper = true,
    )

    private fun newViewModel(
        repository: ProfileRepository,
        systemTz: String = "America/Toronto",
    ): EditUserProfileViewModel {
        val vm = EditUserProfileViewModel(repository = repository, appContext = context)
        vm.overrideSystemTimezoneProvider { systemTz }
        return vm
    }

    @Test
    fun `initialize prefills from server profile`() = runTest(testDispatcher) {
        val repository = FakeEditRepo(
            initialProfile = UserProfileJson(
                fullName = "Saved Name",
                deliveryAddress = "123 Saved St",
                preferredContactMethod = "phone",
                additionalInfo = mapOf("timezone" to "Asia/Tokyo", "language" to "en"),
            ),
        )
        val vm = newViewModel(repository)
        vm.initialize(authUser())
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals("Saved Name", s.fullName)
        assertEquals("123 Saved St", s.deliveryAddress)
        assertEquals(ContactMethod.PHONE, s.contactMethod)
        assertEquals("Asia/Tokyo", s.timezone)
        assertTrue(s.isInitialized)
    }

    @Test
    fun `initialize falls back to social auth name when profile has none`() =
        runTest(testDispatcher) {
            val repository = FakeEditRepo(initialProfile = UserProfileJson(fullName = null))
            val vm = newViewModel(repository)
            vm.initialize(authUser())
            advanceUntilIdle()
            assertEquals("Alex Stewart", vm.state.value.fullName)
            // Email present and verified-ish in fixture → email preference default.
            assertEquals(ContactMethod.EMAIL, vm.state.value.contactMethod)
        }

    @Test
    fun `initialize falls back to system timezone when supported`() = runTest(testDispatcher) {
        val repository = FakeEditRepo(initialProfile = UserProfileJson(additionalInfo = null))
        val vm = newViewModel(repository, systemTz = "America/Vancouver")
        vm.initialize(authUser())
        advanceUntilIdle()
        assertEquals("America/Vancouver", vm.state.value.timezone)
    }

    @Test
    fun `initialize defaults to Asia Manila when system timezone is unsupported`() =
        runTest(testDispatcher) {
            val repository = FakeEditRepo(initialProfile = UserProfileJson())
            val vm = newViewModel(repository, systemTz = "Europe/Stockholm")
            vm.initialize(authUser())
            advanceUntilIdle()
            assertEquals("Asia/Manila", vm.state.value.timezone)
        }

    @Test
    fun `save with blank full name sets validation error and skips network`() =
        runTest(testDispatcher) {
            val repository = FakeEditRepo(initialProfile = UserProfileJson())
            val vm = newViewModel(repository)
            vm.initialize(authUser())
            advanceUntilIdle()
            vm.onFullNameChange("   ")
            vm.save()
            advanceUntilIdle()
            assertEquals("Full name is required", vm.state.value.errorMessage)
            assertNull(repository.lastUpdateRequest)
        }

    @Test
    fun `save sends merged additional_info and surfaces success`() =
        runTest(testDispatcher) {
            val repository = FakeEditRepo(
                initialProfile = UserProfileJson(
                    fullName = "Old",
                    additionalInfo = mapOf(
                        "oauth_provider" to "google",
                        "language" to "en",
                    ),
                ),
            )
            val vm = newViewModel(repository)
            vm.initialize(authUser())
            advanceUntilIdle()
            vm.onFullNameChange("New Name")
            vm.onDeliveryAddressChange(" Some Address ")
            vm.onContactMethodChange(ContactMethod.EMAIL)
            vm.onTimezoneChange("America/Toronto")
            vm.save()
            advanceUntilIdle()
            val req = repository.lastUpdateRequest
            assertNotNull(req)
            assertEquals("New Name", req!!.fullName)
            assertEquals("Some Address", req.deliveryAddress)
            assertEquals("email", req.preferredContactMethod)
            // Preserves legacy keys + writes timezone.
            assertEquals("google", req.additionalInfo?.get("oauth_provider"))
            assertEquals("en", req.additionalInfo?.get("language"))
            assertEquals("America/Toronto", req.additionalInfo?.get("timezone"))
            assertEquals("Profile updated", vm.state.value.successMessage)
            assertFalse(vm.state.value.isLoading)
        }

    @Test
    fun `save omits blank delivery address`() = runTest(testDispatcher) {
        val repository = FakeEditRepo(initialProfile = UserProfileJson(fullName = "Name"))
        val vm = newViewModel(repository)
        vm.initialize(authUser())
        advanceUntilIdle()
        vm.onDeliveryAddressChange("   ")
        vm.save()
        advanceUntilIdle()
        assertNull(repository.lastUpdateRequest?.deliveryAddress)
    }
}

private class FakeEditRepo(
    initialProfile: UserProfileJson?,
) : ProfileRepository {
    var lastUpdateRequest: UpdateProfileRequestJson? = null
    private var currentProfile: UserProfileJson? = initialProfile

    override suspend fun fetchProfile(forceRefresh: Boolean) =
        Result.success(ProfileDataJson(profile = currentProfile))

    override suspend fun updateProfile(
        request: UpdateProfileRequestJson,
    ): Result<ProfileDataJson> {
        lastUpdateRequest = request
        val merged = currentProfile?.copy(
            fullName = request.fullName ?: currentProfile?.fullName,
            deliveryAddress = request.deliveryAddress ?: currentProfile?.deliveryAddress,
            preferredContactMethod = request.preferredContactMethod
                ?: currentProfile?.preferredContactMethod,
            additionalInfo = request.additionalInfo ?: currentProfile?.additionalInfo,
        ) ?: UserProfileJson(
            fullName = request.fullName,
            deliveryAddress = request.deliveryAddress,
            preferredContactMethod = request.preferredContactMethod,
            additionalInfo = request.additionalInfo,
        )
        currentProfile = merged
        return Result.success(ProfileDataJson(profile = merged))
    }

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
    override suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?> = error("unused")
    override suspend fun fetchCarrierStats(): Result<CarrierStatsJson?> = error("unused")
    override suspend fun fetchUserStats(): Result<UserStatsDataJson?> = error("unused")
    override suspend fun toggleCarrierStatus() = error("unused")
    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> = error("unused")
    override suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> = error("unused")
    override suspend fun enableCarrier(): Result<Unit> = error("unused")
}
