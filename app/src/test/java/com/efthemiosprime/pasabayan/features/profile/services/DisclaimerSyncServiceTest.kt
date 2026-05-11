package com.efthemiosprime.pasabayan.features.profile.services

import android.content.SharedPreferences
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
import com.efthemiosprime.pasabayan.features.packages.services.ShipperDisclaimerStore
import com.efthemiosprime.pasabayan.features.trips.services.CarrierDisclaimerStore
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DisclaimerSyncServiceTest {

    private lateinit var carrierStore: CarrierDisclaimerStore
    private lateinit var shipperStore: ShipperDisclaimerStore
    private lateinit var carrierPrefs: SharedPreferences
    private lateinit var shipperPrefs: SharedPreferences
    private val carrierValues = mutableMapOf<String, Boolean>()
    private val shipperValues = mutableMapOf<String, Boolean>()

    @Before
    fun setUp() {
        carrierPrefs = mockBoolPrefs(carrierValues)
        shipperPrefs = mockBoolPrefs(shipperValues)
        carrierStore = CarrierDisclaimerStore(carrierPrefs)
        shipperStore = ShipperDisclaimerStore(shipperPrefs)
    }

    private fun mockBoolPrefs(backing: MutableMap<String, Boolean>): SharedPreferences {
        val prefs: SharedPreferences = mockk()
        val editor: SharedPreferences.Editor = mockk()
        every { prefs.edit() } returns editor
        every { prefs.getBoolean(any(), any()) } answers {
            backing[arg<String>(0)] ?: arg<Boolean>(1)
        }
        every { editor.putBoolean(any(), any()) } answers {
            backing[arg<String>(0)] = arg<Boolean>(1)
            editor
        }
        every { editor.apply() } just runs
        return prefs
    }

    @Test
    fun `bootstrap flips local flags when server has acks`() = runBlocking {
        val service = DisclaimerSyncService(
            repository = FakeRepo(
                acks = DisclaimerAcknowledgmentsDataJson(
                    carrierTrip = "2026-04-01T00:00:00Z",
                    shipper = "2026-04-02T00:00:00Z",
                ),
            ),
            carrierStore = carrierStore,
            shipperStore = shipperStore,
        )
        service.bootstrapAcknowledgments(userId = 7L)
        assertTrue(carrierStore.hasAcknowledged(7))
        assertTrue(shipperStore.hasAcknowledged(7))
        assertFalse(carrierStore.isPendingSync(7))
        assertFalse(shipperStore.isPendingSync(7))
    }

    @Test
    fun `bootstrap is no-op when server has no acks`() = runBlocking {
        val service = DisclaimerSyncService(
            repository = FakeRepo(acks = DisclaimerAcknowledgmentsDataJson()),
            carrierStore = carrierStore,
            shipperStore = shipperStore,
        )
        service.bootstrapAcknowledgments(userId = 5L)
        assertFalse(carrierStore.hasAcknowledged(5))
        assertFalse(shipperStore.hasAcknowledged(5))
    }

    @Test
    fun `acknowledgeShipper sets local flag and clears pendingSync on POST success`() =
        runBlocking {
            val repo = FakeRepo(acknowledgeResult = Result.success(DisclaimerAcknowledgmentDataJson()))
            val service = DisclaimerSyncService(repo, carrierStore, shipperStore)
            val synced = service.acknowledgeShipper(userId = 3L)
            assertTrue(synced)
            assertTrue(shipperStore.hasAcknowledged(3))
            assertFalse(shipperStore.isPendingSync(3))
            assertEquals(listOf("shipper"), repo.acknowledgeTypes)
        }

    @Test
    fun `acknowledgeCarrier sets pendingSync when POST fails`() = runBlocking {
        val repo = FakeRepo(acknowledgeResult = Result.failure(IllegalStateException("boom")))
        val service = DisclaimerSyncService(repo, carrierStore, shipperStore)
        val synced = service.acknowledgeCarrier(userId = 9L)
        assertFalse(synced)
        assertTrue(carrierStore.hasAcknowledged(9))
        assertTrue(carrierStore.isPendingSync(9))
        assertEquals(listOf("carrier_trip"), repo.acknowledgeTypes)
    }

    @Test
    fun `retryPendingSyncs is no-op when neither store has pending flag`() = runBlocking {
        val repo = FakeRepo(acknowledgeResult = Result.success(DisclaimerAcknowledgmentDataJson()))
        val service = DisclaimerSyncService(repo, carrierStore, shipperStore)
        val result = service.retryPendingSyncs(userId = 11L)
        assertFalse(result.carrierAttempted)
        assertFalse(result.shipperAttempted)
        assertTrue(repo.acknowledgeTypes.isEmpty())
    }

    @Test
    fun `retryPendingSyncs retries both pending flags and clears them on success`() =
        runBlocking {
            carrierStore.setPendingSync(12, pending = true)
            shipperStore.setPendingSync(12, pending = true)
            val repo = FakeRepo(acknowledgeResult = Result.success(DisclaimerAcknowledgmentDataJson()))
            val service = DisclaimerSyncService(repo, carrierStore, shipperStore)
            val result = service.retryPendingSyncs(userId = 12L)
            assertTrue(result.carrierAttempted)
            assertTrue(result.shipperAttempted)
            assertTrue(result.carrierSynced)
            assertTrue(result.shipperSynced)
            assertFalse(carrierStore.isPendingSync(12))
            assertFalse(shipperStore.isPendingSync(12))
            assertEquals(listOf("carrier_trip", "shipper"), repo.acknowledgeTypes)
        }

    @Test
    fun `retryPendingSyncs keeps pendingSync set when retry fails`() = runBlocking {
        carrierStore.setPendingSync(14, pending = true)
        val repo = FakeRepo(acknowledgeResult = Result.failure(IllegalStateException("retry boom")))
        val service = DisclaimerSyncService(repo, carrierStore, shipperStore)
        val result = service.retryPendingSyncs(userId = 14L)
        assertTrue(result.carrierAttempted)
        assertFalse(result.carrierSynced)
        assertTrue(carrierStore.isPendingSync(14))
    }
}

private class FakeRepo(
    private val acks: DisclaimerAcknowledgmentsDataJson =
        DisclaimerAcknowledgmentsDataJson(),
    private val acknowledgeResult: Result<DisclaimerAcknowledgmentDataJson> =
        Result.success(DisclaimerAcknowledgmentDataJson()),
) : ProfileRepository {
    val acknowledgeTypes = mutableListOf<String>()

    override suspend fun fetchDisclaimerAcknowledgments() = Result.success(acks)

    override suspend fun acknowledgeDisclaimer(
        type: String,
    ): Result<DisclaimerAcknowledgmentDataJson> {
        acknowledgeTypes += type
        return acknowledgeResult
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
    override suspend fun fetchConsentPreferences(): Result<ConsentPreferencesDataJson> =
        error("unused")
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
