package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
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
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.features.profile.model.ConsentPreference
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
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
class PrivacyPreferencesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load populates all four toggles from server`() = runTest(testDispatcher) {
        val repo = FakeConsentRepo(
            initialFetch = ConsentPreferencesDataJson(
                pushNotifications = true,
                locationTracking = true,
                analytics = false,
                marketingCommunications = true,
            ),
        )
        val vm = PrivacyPreferencesViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s.pushNotifications)
        assertTrue(s.locationTracking)
        assertFalse(s.analytics)
        assertTrue(s.marketingCommunications)
        assertFalse(s.isLoading)
    }

    @Test
    fun `requestToggle for analytics applies optimistic update and PUTs scoped key only`() =
        runTest(testDispatcher) {
            val repo = FakeConsentRepo(
                initialFetch = baselineOff(),
                updateResponse = baselineOff().copy(analytics = true),
            )
            val vm = PrivacyPreferencesViewModel(repo, context)
            vm.load()
            advanceUntilIdle()
            vm.requestToggle(ConsentPreference.ANALYTICS, true)
            // Optimistic before coroutine resumes — analytics already true in state.
            assertTrue(vm.state.value.analytics)
            advanceUntilIdle()
            val sent = repo.lastUpdate
            assertNotNull(sent)
            assertEquals(true, sent!!.analytics)
            // Scoped: only `analytics` is non-null.
            assertNull(sent.pushNotifications)
            assertNull(sent.locationTracking)
            assertNull(sent.marketingCommunications)
            assertTrue(vm.state.value.analytics)
        }

    @Test
    fun `requestToggle disable for push opens confirm and only fires after confirm`() =
        runTest(testDispatcher) {
            val repo = FakeConsentRepo(
                initialFetch = baselineOff().copy(pushNotifications = true),
                updateResponse = baselineOff(),
            )
            val vm = PrivacyPreferencesViewModel(repo, context)
            vm.load()
            advanceUntilIdle()
            vm.requestToggle(ConsentPreference.PUSH_NOTIFICATIONS, false)
            advanceUntilIdle()
            // Confirm pending, but no update yet.
            assertEquals(ConsentPreference.PUSH_NOTIFICATIONS, vm.state.value.pendingDisable)
            assertTrue(vm.state.value.pushNotifications)
            assertNull(repo.lastUpdate)
            vm.confirmPendingDisable()
            advanceUntilIdle()
            assertNull(vm.state.value.pendingDisable)
            assertFalse(vm.state.value.pushNotifications)
            assertEquals(false, repo.lastUpdate?.pushNotifications)
        }

    @Test
    fun `cancelPendingDisable clears pending and does not call repository`() =
        runTest(testDispatcher) {
            val repo = FakeConsentRepo(
                initialFetch = baselineOff().copy(locationTracking = true),
            )
            val vm = PrivacyPreferencesViewModel(repo, context)
            vm.load()
            advanceUntilIdle()
            vm.requestToggle(ConsentPreference.LOCATION_TRACKING, false)
            vm.cancelPendingDisable()
            advanceUntilIdle()
            assertNull(vm.state.value.pendingDisable)
            assertTrue(vm.state.value.locationTracking)
            assertNull(repo.lastUpdate)
        }

    @Test
    fun `failed update reverts the optimistic toggle`() = runTest(testDispatcher) {
        val repo = FakeConsentRepo(
            initialFetch = baselineOff(),
            updateFailure = IllegalStateException("boom"),
        )
        val vm = PrivacyPreferencesViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        vm.requestToggle(ConsentPreference.MARKETING, true)
        // Optimistic ON.
        assertTrue(vm.state.value.marketingCommunications)
        advanceUntilIdle()
        // Reverted to OFF after failure.
        assertFalse(vm.state.value.marketingCommunications)
    }

    @Test
    fun `enabling push does not require confirmation`() = runTest(testDispatcher) {
        val repo = FakeConsentRepo(
            initialFetch = baselineOff(),
            updateResponse = baselineOff().copy(pushNotifications = true),
        )
        val vm = PrivacyPreferencesViewModel(repo, context)
        vm.load()
        advanceUntilIdle()
        vm.requestToggle(ConsentPreference.PUSH_NOTIFICATIONS, true)
        advanceUntilIdle()
        assertTrue(vm.state.value.pushNotifications)
        assertNull(vm.state.value.pendingDisable)
        assertEquals(true, repo.lastUpdate?.pushNotifications)
    }

    private fun baselineOff() = ConsentPreferencesDataJson(
        pushNotifications = false,
        locationTracking = false,
        analytics = false,
        marketingCommunications = false,
    )
}

private class FakeConsentRepo(
    private val initialFetch: ConsentPreferencesDataJson,
    private val updateResponse: ConsentPreferencesDataJson? = null,
    private val updateFailure: Throwable? = null,
) : ProfileRepository {
    var lastUpdate: ConsentPreferencesUpdateJson? = null

    override suspend fun fetchConsentPreferences() = Result.success(initialFetch)

    override suspend fun updateConsentPreferences(
        update: ConsentPreferencesUpdateJson,
    ): Result<ConsentPreferencesDataJson> {
        lastUpdate = update
        updateFailure?.let { return Result.failure(it) }
        return Result.success(updateResponse ?: initialFetch)
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
