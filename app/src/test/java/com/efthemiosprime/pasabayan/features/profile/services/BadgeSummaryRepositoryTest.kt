package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.network.profile.ActionRequiredJson
import com.efthemiosprime.pasabayan.core.network.profile.BadgeSummaryJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.VerificationJson
import com.efthemiosprime.pasabayan.core.session.TokenStore
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeSummaryRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var profileApi: FakeProfileApi
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var clock: Clock
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        profileApi = FakeProfileApi()
        tokenStore = FakeTokenStore(initialToken = "bearer-1")
        clock = Clock.fixed(Instant.parse("2026-05-14T18:00:00Z"), ZoneOffset.UTC)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun repository() = BadgeSummaryRepositoryImpl(profileApi, tokenStore, json, clock)

    @Test
    fun `successful refresh populates summary, hasValidData=true, lastFetchedAt set`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()

        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()

        val summary = repo.summary.value
        assertNotNull(summary)
        assertEquals(15, summary!!.total)
        assertEquals(true, repo.hasValidData.value)
        assertEquals(Instant.parse("2026-05-14T18:00:00Z"), repo.lastFetchedAt.value)
        assertEquals(UserRole.CARRIER, repo.lastRequestedRole)
        assertEquals(listOf("carrier"), profileApi.roleCalls)
    }

    @Test
    fun `null role sends no role query param`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()

        repo.refresh(null)
        advanceUntilIdle()

        assertEquals(listOf<String?>(null), profileApi.roleCalls)
    }

    @Test
    fun `failure preserves prior summary and flips hasValidData=false`() = runTest(testDispatcher) {
        // First refresh succeeds.
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()
        val priorSummary = repo.summary.value
        assertNotNull(priorSummary)
        assertTrue(repo.hasValidData.value)

        // Second refresh fails — summary must stay, hasValidData flips off.
        profileApi.nextResponse = Response.error(
            500,
            "{}".toResponseBody("application/json".toMediaTypeOrNull()),
        )
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()

        assertEquals(priorSummary, repo.summary.value)
        assertFalse(repo.hasValidData.value)
    }

    @Test
    fun `network exception preserves prior summary and flips hasValidData=false`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()
        val priorSummary = repo.summary.value

        profileApi.nextException = RuntimeException("boom")
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()

        assertEquals(priorSummary, repo.summary.value)
        assertFalse(repo.hasValidData.value)
    }

    @Test
    fun `two concurrent refresh calls for same role result in exactly one API hit`() = runTest(testDispatcher) {
        val gate = CompletableDeferred<Unit>()
        profileApi.gateBeforeReturn = gate
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()

        val first = async { repo.refresh(UserRole.CARRIER) }
        val second = async { repo.refresh(UserRole.CARRIER) }
        // Let both coroutines reach the tryLock check.
        advanceUntilIdle()

        gate.complete(Unit)
        awaitAll(first, second)
        advanceUntilIdle()

        assertEquals(1, profileApi.callCount)
    }

    @Test
    fun `different roles fetch independently (per-key coalescing)`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()

        val a = async { repo.refresh(UserRole.CARRIER) }
        val b = async { repo.refresh(UserRole.SHIPPER) }
        awaitAll(a, b)
        advanceUntilIdle()

        assertEquals(2, profileApi.callCount)
        assertTrue(profileApi.roleCalls.containsAll(listOf("carrier", "shipper")))
    }

    @Test
    fun `snapshotMode skips refresh entirely`() = runTest(testDispatcher) {
        val repo = repository()
        repo.snapshotMode = true
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)

        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()

        assertEquals(0, profileApi.callCount)
        assertNull(repo.summary.value)
        assertFalse(repo.hasValidData.value)
    }

    @Test
    fun `seedForSnapshotTesting updates state without a network call`() = runTest(testDispatcher) {
        val repo = repository()
        val seed = BadgeSummary(total = 7, unreadNotifications = 7)

        repo.seedForSnapshotTesting(seed)

        assertEquals(seed, repo.summary.value)
        assertTrue(repo.hasValidData.value)
        assertEquals(Instant.parse("2026-05-14T18:00:00Z"), repo.lastFetchedAt.value)
        assertEquals(0, profileApi.callCount)
    }

    @Test
    fun `seedForSnapshotTesting with null clears state`() {
        val repo = repository()
        repo.seedForSnapshotTesting(BadgeSummary(total = 1))
        repo.seedForSnapshotTesting(null)

        assertNull(repo.summary.value)
        assertFalse(repo.hasValidData.value)
        assertNull(repo.lastFetchedAt.value)
    }

    @Test
    fun `clear drops summary, hasValidData, lastFetchedAt`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()
        assertNotNull(repo.summary.value)

        repo.clear()

        assertNull(repo.summary.value)
        assertFalse(repo.hasValidData.value)
        assertNull(repo.lastFetchedAt.value)
    }

    @Test
    fun `refresh without auth token clears state and skips network`() = runTest(testDispatcher) {
        // Seed first so we can verify clear happens.
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()
        assertNotNull(repo.summary.value)

        tokenStore.set(null)
        profileApi.callCount = 0
        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()

        assertNull(repo.summary.value)
        assertFalse(repo.hasValidData.value)
        assertEquals(0, profileApi.callCount)
    }

    @Test
    fun `lastRequestedRole tracks the most recent refresh call`() = runTest(testDispatcher) {
        profileApi.nextResponse = Response.success(SAMPLE_PAYLOAD)
        val repo = repository()

        repo.refresh(UserRole.CARRIER)
        advanceUntilIdle()
        assertEquals(UserRole.CARRIER, repo.lastRequestedRole)

        repo.refresh(UserRole.SHIPPER)
        advanceUntilIdle()
        assertEquals(UserRole.SHIPPER, repo.lastRequestedRole)

        repo.refresh(null)
        advanceUntilIdle()
        assertNull(repo.lastRequestedRole)
    }

    private companion object {
        val SAMPLE_PAYLOAD = BadgeSummaryJson(
            unreadNotifications = 4,
            actionRequired = ActionRequiredJson(total = 6, pendingBookingRequests = 2),
            pendingReviewsCount = 3,
            verification = VerificationJson(phoneVerificationNeeded = true, payoutSetupNeeded = true),
            total = 15,
            computedAt = "2026-05-14T17:59:41Z",
            degraded = false,
        )
    }
}

private class FakeTokenStore(initialToken: String?) : TokenStore {
    private var stored: String? = initialToken

    fun set(value: String?) { stored = value }

    override fun getToken(): String? = stored
    override fun setToken(token: String?) { stored = token }
    override fun clear() { stored = null }
}

private class FakeProfileApi : ProfileApi {
    var nextResponse: Response<BadgeSummaryJson> = Response.success(BadgeSummaryJson())
    var nextException: Throwable? = null
    var callCount: Int = 0
    val roleCalls = mutableListOf<String?>()

    /**
     * If set, [getBadgeSummary] suspends until completed — used to force overlap
     * between two concurrent callers so the second one's `tryLock` observes the
     * first one's lock as held.
     */
    var gateBeforeReturn: CompletableDeferred<Unit>? = null

    override suspend fun getBadgeSummary(role: String?): Response<BadgeSummaryJson> {
        callCount++
        roleCalls += role
        gateBeforeReturn?.await()
        nextException?.let { throw it.also { nextException = null } }
        // Yield once so concurrent test coroutines get to suspend through the
        // mutex check before this one resumes.
        delay(1)
        return nextResponse
    }

    // Unused — make accidental use obvious.
    override suspend fun getProfile() = error("not used")
    override suspend fun putProfile(body: com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson) = error("not used")
    override suspend fun postProfileMultipart(
        profilePicture: okhttp3.MultipartBody.Part?,
        fullName: okhttp3.RequestBody?,
        deliveryAddress: okhttp3.RequestBody?,
        preferredContactMethod: okhttp3.RequestBody?,
        additionalInfo: okhttp3.RequestBody?,
    ) = error("not used")
    override suspend fun deleteProfilePicture() = error("not used")
    override suspend fun requestAccountDeletion(body: com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionRequestJson) = error("not used")
    override suspend fun getDisclaimerAcknowledgments() = error("not used")
    override suspend fun postDisclaimerAcknowledgment(body: com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentRequestJson) = error("not used")
    override suspend fun getConsentPreferences() = error("not used")
    override suspend fun putConsentPreferences(body: com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson) = error("not used")
    override suspend fun exportUserData() = error("not used")
    override suspend fun getCarrierProfile() = error("not used")
    override suspend fun postCarrierProfile(body: com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson) = error("not used")
    override suspend fun putCarrierProfile(body: com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson) = error("not used")
    override suspend fun getCarrierStats() = error("not used")
    override suspend fun postCarrierEnable() = error("not used")
    override suspend fun postCarrierToggleStatus() = error("not used")
    override suspend fun getUserStats() = error("not used")
}
