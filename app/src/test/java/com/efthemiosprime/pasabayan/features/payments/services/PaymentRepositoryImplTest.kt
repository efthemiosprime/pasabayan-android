package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.network.payments.PaymentApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConfigApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PaymentRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var paymentRepo: PaymentRepositoryImpl
    private lateinit var configRepo: StripeConfigRepositoryImpl

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
        paymentRepo = PaymentRepositoryImpl(retrofit.create(PaymentApi::class.java), json)
        configRepo = StripeConfigRepositoryImpl(retrofit.create(StripeConfigApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- PaymentRepository --

    @Test
    fun `listTransactions returns transactions on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": [{"id": 1, "status": "completed", "amounts": {"total": 150.0, "currency": "cad"}}]}""",
            ),
        )
        val result = paymentRepo.listTransactions()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(1, result.getOrThrow()[0].id)
    }

    @Test
    fun `listTransactions returns failure on 401`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Unauthenticated"}"""))
        val result = paymentRepo.listTransactions()
        assertTrue(result.isFailure)
    }

    @Test
    fun `getTransaction returns transaction on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"id": 500, "status": "completed"}}""",
            ),
        )
        val result = paymentRepo.getTransaction(500)
        assertTrue(result.isSuccess)
        assertEquals(500, result.getOrThrow().id)
    }

    @Test
    fun `createPayment returns response on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"id": 501, "status": "pending"}, "client_secret": "pi_secret_123", "customer_id": "cus_abc"}""",
            ),
        )
        val result = paymentRepo.createPayment(100, 150.0, "cad")
        assertTrue(result.isSuccess)
        assertEquals("pi_secret_123", result.getOrThrow().clientSecret)
    }

    @Test
    fun `createPayment returns failure when success is false`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": false, "message": "Payment failed"}""",
            ),
        )
        val result = paymentRepo.createPayment(100, 150.0, "cad")
        assertTrue(result.isFailure)
    }

    @Test
    fun `cancelTransaction returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Cancelled", "data": {"id": 500, "status": "cancelled"}}""",
            ),
        )
        val result = paymentRepo.cancelTransaction(500)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `addTip returns response`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"id": 500, "status": "completed"}}""",
            ),
        )
        val result = paymentRepo.addTip(500, 5.0)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getTransaction returns failure when body success is false`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": false, "message": "Transaction unavailable"}""",
            ),
        )
        val result = paymentRepo.getTransaction(500)
        assertTrue(result.isFailure)
    }

    @Test
    fun `requestRefund returns failure when data missing`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": null}""",
            ),
        )
        val result = paymentRepo.requestRefund(
            transactionId = 500,
            amount = 10.0,
            reason = "reason",
            description = "desc",
        )
        assertTrue(result.isFailure)
    }

    // -- StripeConfigRepository --

    @Test
    fun `fetchConfig returns config on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "data": {"mode": "sandbox", "public_key": "pk_test", "currency": "cad", "min_delivery_price": 5.0, "sender_fee_percentage": 10.0, "carrier_fee_percentage": 5.0}}""",
            ),
        )
        val result = configRepo.fetchConfig()
        assertTrue(result.isSuccess)
        val config = result.getOrThrow()
        assertTrue(config.isSandbox)
        assertEquals("pk_test", config.publicKey)
    }

    @Test
    fun `fetchConfig returns failure on error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"Server error"}"""))
        val result = configRepo.fetchConfig()
        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchConfig returns failure when success is false`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": false, "message": "Config unavailable"}""",
            ),
        )
        val result = configRepo.fetchConfig()
        assertTrue(result.isFailure)
    }
}
