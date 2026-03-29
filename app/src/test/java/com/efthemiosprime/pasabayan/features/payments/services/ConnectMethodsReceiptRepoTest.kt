package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodsApi
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ConnectMethodsReceiptRepoTest {

    private lateinit var server: MockWebServer
    private lateinit var connectRepo: StripeConnectRepositoryImpl
    private lateinit var methodsRepo: PaymentMethodsRepositoryImpl
    private lateinit var receiptRepo: ReceiptRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        connectRepo = StripeConnectRepositoryImpl(retrofit.create(StripeConnectApi::class.java), json)
        methodsRepo = PaymentMethodsRepositoryImpl(retrofit.create(PaymentMethodsApi::class.java), json)
        receiptRepo = ReceiptRepositoryImpl(retrofit.create(ReceiptApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- StripeConnect --

    @Test
    fun `checkStatus returns status on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"has_stripe_account": true, "onboarding_complete": true, "charges_enabled": true, "payouts_enabled": true, "can_receive_payouts": true}}""",
            ),
        )
        val result = connectRepo.checkStatus()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().onboardingComplete)
    }

    @Test
    fun `startOnboarding returns url on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"onboarding_url": "https://connect.stripe.com/onboard", "stripe_account_id": "acct_new"}}""",
            ),
        )
        val result = connectRepo.startOnboarding()
        assertTrue(result.isSuccess)
        assertEquals("https://connect.stripe.com/onboard", result.getOrThrow())
    }

    // -- PaymentMethods --

    @Test
    fun `loadPaymentMethods returns methods`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": [{"id": "pm_1", "card": {"brand": "visa", "last4": "4242", "exp_month": 12, "exp_year": 2028}}]}""",
            ),
        )
        val result = methodsRepo.loadPaymentMethods()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("visa", result.getOrThrow()[0].brand)
    }

    @Test
    fun `createSetupIntent returns data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"client_secret": "seti_secret", "customer_id": "cus_1", "ephemeral_key": "ek_1"}}""",
            ),
        )
        val result = methodsRepo.createSetupIntent()
        assertTrue(result.isSuccess)
        assertEquals("seti_secret", result.getOrThrow().clientSecret)
    }

    @Test
    fun `removePaymentMethod returns success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success": true, "message": "Removed"}"""))
        val result = methodsRepo.removePaymentMethod("pm_1")
        assertTrue(result.isSuccess)
    }

    // -- Receipts --

    @Test
    fun `fetchReceipts returns paginated list`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": [{"id": 1, "receipt_number": "RCP-001", "role": "shipper", "amount": {"total": 100.0, "currency": "cad"}, "status": "completed", "date": "2026-03-29"}], "meta": {"current_page": 1, "last_page": 2, "per_page": 20, "total": 25}}""",
            ),
        )
        val result = receiptRepo.fetchReceipts(page = 1)
        assertTrue(result.isSuccess)
        val (receipts, hasMore, total) = result.getOrThrow()
        assertEquals(1, receipts.size)
        assertTrue(hasMore)
        assertEquals(25, total)
    }

    @Test
    fun `fetchReceipt returns single receipt`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"id": 1000, "receipt_number": "RCP-001", "receipt_url": "https://example.com/r.pdf", "role": "shipper", "amount": {"total": 175.50, "currency": "cad"}, "status": "completed", "date": "2026-03-29"}}""",
            ),
        )
        val result = receiptRepo.fetchReceipt(1000)
        assertTrue(result.isSuccess)
        assertEquals("RCP-001", result.getOrThrow().receiptNumber)
    }
}
