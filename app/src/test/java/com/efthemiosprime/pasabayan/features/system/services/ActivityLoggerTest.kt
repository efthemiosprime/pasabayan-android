package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.network.system.ActivityLogJson
import com.efthemiosprime.pasabayan.core.network.system.ActivityLogResponseJson
import com.efthemiosprime.pasabayan.core.network.system.HealthCheckResponseJson
import com.efthemiosprime.pasabayan.core.network.system.SystemApi
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationAppScope
import com.efthemiosprime.pasabayan.features.system.model.ActivityLogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityLoggerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var api: FakeSystemApi
    private lateinit var userProvider: ToggleableUserProvider
    private val deviceContext = object : ActivityLogDeviceContext {
        override val ipAddress = "Android_abc12345"
        override val userAgent = "Pasabayan Android 1.0"
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api = FakeSystemApi()
        userProvider = ToggleableUserProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newLogger(): DefaultActivityLogger {
        val scope = object : NotificationAppScope {
            override val scope: CoroutineScope = CoroutineScope(testDispatcher)
        }
        return DefaultActivityLogger(api, userProvider, deviceContext, scope)
    }

    @Test
    fun `log is a no-op when no current user`() = runTest {
        userProvider.user = null
        val logger = newLogger()
        logger.log(action = "open_app", description = "User opened the app", logType = ActivityLogType.SYSTEM)
        advanceUntilIdle()
        assertTrue("expected no submissions when unauthenticated", api.submissions.isEmpty())
    }

    @Test
    fun `log is a no-op when user id is not positive`() = runTest {
        userProvider.user = ActivityLogUser(id = 0, name = "Anon", email = "a@a.com", userType = "shipper")
        val logger = newLogger()
        logger.log("a", "d", ActivityLogType.SYSTEM)
        advanceUntilIdle()
        assertTrue(api.submissions.isEmpty())
    }

    @Test
    fun `log dispatches a fully populated payload`() = runTest {
        userProvider.user = ActivityLogUser(id = 42, name = "Jane", email = "jane@x.com", userType = "shipper")
        val logger = newLogger()
        logger.log(
            action = "create_package",
            description = "Created package request #123",
            logType = ActivityLogType.PACKAGE,
            subjectType = "PackageRequest",
            subjectId = 123,
            properties = mapOf("k" to "v"),
        )
        advanceUntilIdle()
        val sent = api.submissions.single()
        assertEquals(42, sent.userId)
        assertEquals("jane@x.com", sent.userEmail)
        assertEquals("shipper", sent.userType)
        assertEquals("create_package", sent.action)
        assertEquals("package", sent.logType)
        assertEquals("PackageRequest", sent.subjectType)
        assertEquals(123, sent.subjectId)
        assertEquals(mapOf("k" to "v"), sent.properties)
        assertEquals("Android_abc12345", sent.ipAddress)
        assertEquals("Pasabayan Android 1.0", sent.userAgent)
    }

    @Test
    fun `convenience helpers populate the right log type and subject type`() = runTest {
        userProvider.user = ActivityLogUser(id = 7, name = "User", email = "u@x.com", userType = "carrier")
        val logger = newLogger()
        logger.logPackageAction("pkg.update", "Updated package", packageId = 11)
        logger.logTripAction("trip.cancel", "Cancelled trip", tripId = 22)
        logger.logMatchAction("match.accept", "Accepted match", matchId = 33)
        logger.logTransactionAction("txn.refund", "Refunded txn", transactionId = 44)
        logger.logRatingAction("rate.received", "Received rating", ratingId = 55)
        logger.logUserAction("user.edit", "Edited profile")
        logger.logSystemAction("system.crash", "Crash recovered")
        advanceUntilIdle()

        val byAction = api.submissions.associateBy { it.action }
        assertEquals("package", byAction.getValue("pkg.update").logType)
        assertEquals("PackageRequest", byAction.getValue("pkg.update").subjectType)
        assertEquals("trip", byAction.getValue("trip.cancel").logType)
        assertEquals("CarrierTrip", byAction.getValue("trip.cancel").subjectType)
        assertEquals("match", byAction.getValue("match.accept").logType)
        assertEquals("DeliveryMatch", byAction.getValue("match.accept").subjectType)
        assertEquals("transaction", byAction.getValue("txn.refund").logType)
        assertEquals("Transaction", byAction.getValue("txn.refund").subjectType)
        assertEquals("rating", byAction.getValue("rate.received").logType)
        assertEquals("Rating", byAction.getValue("rate.received").subjectType)
        assertEquals("user", byAction.getValue("user.edit").logType)
        assertEquals("system", byAction.getValue("system.crash").logType)
    }

    @Test
    fun `log swallows API failures so callers never observe a crash`() = runTest {
        userProvider.user = ActivityLogUser(id = 1, name = "u", email = "u@x.com", userType = "shipper")
        api.failureModeFor = "boom"
        val logger = newLogger()
        logger.log("boom", "intentional", ActivityLogType.SYSTEM)
        advanceUntilIdle()
        // The logger still records the attempt — submission happened (caught the failure).
        assertEquals(1, api.submissions.size)
    }
}

// -- Fakes --

private class ToggleableUserProvider : ActivityLogUserProvider {
    var user: ActivityLogUser? = null
    override fun current(): ActivityLogUser? = user
}

private class FakeSystemApi : SystemApi {
    val submissions = mutableListOf<ActivityLogJson>()
    var failureModeFor: String? = null

    override suspend fun healthCheck(): Response<HealthCheckResponseJson> =
        Response.success(HealthCheckResponseJson(success = true, status = "ok"))

    override suspend fun logActivity(body: ActivityLogJson): Response<ActivityLogResponseJson> {
        submissions += body
        if (body.action == failureModeFor) error("simulated network failure")
        return Response.success(ActivityLogResponseJson(success = true))
    }
}
