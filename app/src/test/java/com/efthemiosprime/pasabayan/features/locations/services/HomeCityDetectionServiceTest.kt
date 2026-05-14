package com.efthemiosprime.pasabayan.features.locations.services

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import com.efthemiosprime.pasabayan.core.network.SupplementalApi
import com.efthemiosprime.pasabayan.core.network.location.CitiesResponseJson
import com.efthemiosprime.pasabayan.core.network.location.CityOptionJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileResponseJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.features.locations.model.GeoPoint
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class HomeCityDetectionServiceTest {

    private lateinit var auth: ToggleableAuth
    private lateinit var location: StubLocationProvider
    private lateinit var supplemental: FakeSupplementalApi
    private lateinit var profile: FakeProfileApi
    private lateinit var confirmed: InMemoryConfirmedHomeCityStore
    private lateinit var service: DefaultHomeCityDetectionService

    @Before
    fun setUp() {
        auth = ToggleableAuth().apply { token = "bearer" }
        location = StubLocationProvider()
        supplemental = FakeSupplementalApi()
        profile = FakeProfileApi()
        confirmed = InMemoryConfirmedHomeCityStore()
        service = DefaultHomeCityDetectionService(
            locationProvider = location,
            supplementalApi = supplemental,
            profileApi = profile,
            authTokenProvider = auth,
            confirmedStore = confirmed,
        )
    }

    @Test
    fun `Skipped NOT_AUTHENTICATED when token is blank`() = runBlocking {
        auth.token = null
        val outcome = service.detectIfNeeded()
        assertEquals(
            HomeCityDetectionOutcome.Skipped(HomeCityDetectionOutcome.SkipReason.NOT_AUTHENTICATED),
            outcome,
        )
        assertEquals(0, supplemental.calls)
    }

    @Test
    fun `Skipped NO_LOCATION when GPS returns null`() = runBlocking {
        location.next = null
        val outcome = service.detectIfNeeded()
        assertEquals(
            HomeCityDetectionOutcome.Skipped(HomeCityDetectionOutcome.SkipReason.NO_LOCATION),
            outcome,
        )
    }

    @Test
    fun `Skipped EMPTY_CATALOG when no cities`() = runBlocking {
        location.next = GeoPoint(43.65, -79.35)
        supplemental.next = CitiesResponseJson(success = true, data = emptyList())
        val outcome = service.detectIfNeeded()
        assertEquals(
            HomeCityDetectionOutcome.Skipped(HomeCityDetectionOutcome.SkipReason.EMPTY_CATALOG),
            outcome,
        )
    }

    @Test
    fun `Skipped NO_MATCH_IN_RADIUS when nothing in range`() = runBlocking {
        location.next = GeoPoint(0.0, 0.0)
        supplemental.next = CitiesResponseJson(
            success = true,
            data = listOf(
                CityOptionJson(
                    id = 1, name = "Toronto", display = "Toronto, ON",
                    lat = JsonPrimitive("43.65"), lng = JsonPrimitive("-79.35"),
                ),
            ),
        )
        profile.next = profileWith(homeCityId = null)
        val outcome = service.detectIfNeeded()
        assertEquals(
            HomeCityDetectionOutcome.Skipped(HomeCityDetectionOutcome.SkipReason.NO_MATCH_IN_RADIUS),
            outcome,
        )
    }

    @Test
    fun `Updated when GPS resolves a new city`() = runBlocking {
        location.next = GeoPoint(43.65, -79.35)
        supplemental.next = CitiesResponseJson(
            success = true,
            data = listOf(
                CityOptionJson(
                    id = 87, name = "Toronto", display = "Toronto, ON",
                    lat = JsonPrimitive(43.65), lng = JsonPrimitive(-79.35),
                ),
            ),
        )
        profile.next = profileWith(homeCityId = null)
        profile.putResult = Response.success(profile.next)

        val outcome = service.detectIfNeeded() as HomeCityDetectionOutcome.Updated
        assertEquals(87, outcome.cityId)
        assertEquals("Toronto", outcome.cityName)
        assertEquals(87, confirmed.get())
        assertEquals(
            UpdateProfileRequestJson(homeCityId = 87),
            profile.putCalls.single(),
        )
    }

    @Test
    fun `NoChange when profile already matches and store is confirmed`() = runBlocking {
        location.next = GeoPoint(43.65, -79.35)
        supplemental.next = CitiesResponseJson(
            success = true,
            data = listOf(
                CityOptionJson(
                    id = 87, name = "Toronto", display = "Toronto, ON",
                    lat = JsonPrimitive(43.65), lng = JsonPrimitive(-79.35),
                ),
            ),
        )
        profile.next = profileWith(homeCityId = 87)
        confirmed.set(87)
        val outcome = service.detectIfNeeded() as HomeCityDetectionOutcome.NoChange
        assertEquals(87, outcome.cityId)
        assertTrue("put should not be called when nothing changes", profile.putCalls.isEmpty())
    }

    @Test
    fun `NoChange when server PUT fails — store is NOT confirmed so next session retries`() = runBlocking {
        location.next = GeoPoint(43.65, -79.35)
        supplemental.next = CitiesResponseJson(
            success = true,
            data = listOf(
                CityOptionJson(
                    id = 87, name = "Toronto", display = "Toronto, ON",
                    lat = JsonPrimitive(43.65), lng = JsonPrimitive(-79.35),
                ),
            ),
        )
        profile.next = profileWith(homeCityId = null)
        profile.putResult = Response.error(500, okhttp3.ResponseBody.create(null, "{}"))

        val outcome = service.detectIfNeeded()
        assertTrue(outcome is HomeCityDetectionOutcome.NoChange)
        assertNull("confirmed store should stay empty on failure", confirmed.get())
    }

    @Test
    fun `detectIfNeeded short-circuits on second call without forceRefresh`() = runBlocking {
        location.next = GeoPoint(43.65, -79.35)
        supplemental.next = CitiesResponseJson(
            success = true,
            data = listOf(
                CityOptionJson(
                    id = 87, name = "Toronto", display = "Toronto, ON",
                    lat = JsonPrimitive(43.65), lng = JsonPrimitive(-79.35),
                ),
            ),
        )
        profile.next = profileWith(homeCityId = 87)
        confirmed.set(87)
        service.detectIfNeeded()

        val second = service.detectIfNeeded()
        assertEquals(
            HomeCityDetectionOutcome.Skipped(
                HomeCityDetectionOutcome.SkipReason.ALREADY_DETECTED_THIS_SESSION,
            ),
            second,
        )
        // Only one cities fetch fired
        assertEquals(1, supplemental.calls)
    }

    private fun profileWith(homeCityId: Int?): ProfileResponseJson =
        ProfileResponseJson(
            success = true,
            data = ProfileDataJson(
                profile = null,
                homeCityId = homeCityId,
                isComplete = false,
            ),
        )
}

// -- Fakes --

private class ToggleableAuth : AuthTokenProvider {
    var token: String? = null
    override fun currentToken(): String? = token
}

private class StubLocationProvider : LocationProvider {
    var next: GeoPoint? = null
    override suspend fun currentLocation(timeoutMillis: Long): GeoPoint? = next
}

private class FakeSupplementalApi : SupplementalApi {
    var next: CitiesResponseJson = CitiesResponseJson(success = false)
    var calls: Int = 0
    override suspend fun getCitiesForCountry(countryCode: String): Response<CitiesResponseJson> {
        calls++
        return Response.success(next)
    }
}

private class FakeProfileApi : ProfileApi {
    var next: ProfileResponseJson = ProfileResponseJson(
        success = false,
        data = ProfileDataJson(profile = null, homeCityId = null, isComplete = false),
    )
    var putResult: Response<ProfileResponseJson> = Response.error(500, okhttp3.ResponseBody.create(null, "{}"))
    val putCalls = mutableListOf<UpdateProfileRequestJson>()

    override suspend fun getProfile(): Response<ProfileResponseJson> = Response.success(next)

    override suspend fun putProfile(body: UpdateProfileRequestJson): Response<ProfileResponseJson> {
        putCalls += body
        return putResult
    }

    // Unused — throw to make accidental call obvious.
    override suspend fun postProfileMultipart(
        profilePicture: okhttp3.MultipartBody.Part?,
        fullName: okhttp3.RequestBody?,
        deliveryAddress: okhttp3.RequestBody?,
        preferredContactMethod: okhttp3.RequestBody?,
        additionalInfo: okhttp3.RequestBody?,
    ): Response<ProfileResponseJson> = error("not used")

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
    override suspend fun getAttention() = error("not used")
    override suspend fun getBadgeSummary(role: String?) = error("not used")
}

private class InMemoryConfirmedHomeCityStore : ConfirmedHomeCityStore {
    private var value: Int? = null
    override fun get(): Int? = value
    override fun set(cityId: Int?) { value = cityId }
}
