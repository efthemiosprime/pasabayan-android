package com.efthemiosprime.pasabayan.features.ratings.services

import com.efthemiosprime.pasabayan.core.network.ratings.RatingsApi
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

class RatingsRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: RatingsRepositoryImpl

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
        val api = retrofit.create(RatingsApi::class.java)
        repo = RatingsRepositoryImpl(api, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun json200(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    @Test
    fun `fetchReceivedRatings parses paginated list and forwards sort`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": {
                    "user": { "id": 1, "name": "Alex", "rating": "4.7", "total_ratings": 12 },
                    "ratings": {
                      "current_page": 1,
                      "data": [
                        {
                          "id": 100,
                          "rating": 5,
                          "review_text": "Great",
                          "rater": { "id": 2, "name": "Pat" }
                        }
                      ],
                      "per_page": 15,
                      "total": 1,
                      "last_page": 1
                    }
                  }
                }
                """.trimIndent(),
            ),
        )
        val r = repo.fetchReceivedRatings(userId = 1, sort = "highest")
        assertTrue(r.isSuccess)
        val data = r.getOrNull()!!
        assertEquals("4.7", data.user?.rating)
        assertEquals(1, data.ratings.data.size)
        val req = server.takeRequest()
        assertTrue(req.path?.contains("/users/1/ratings") == true)
        assertTrue(req.path?.contains("sort=highest") == true)
    }

    @Test
    fun `fetchGivenRatings parses list`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": [
                    { "id": 7, "rating": 4, "review_text": "OK", "rated": { "id": 9, "name": "Sam" } }
                  ]
                }
                """.trimIndent(),
            ),
        )
        val r = repo.fetchGivenRatings()
        assertTrue(r.isSuccess)
        assertEquals("Sam", r.getOrNull()?.firstOrNull()?.rated?.name)
    }

    @Test
    fun `fetchPendingReviews parses list with tolerant days_since_delivery`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": [
                    {
                      "match_id": 17,
                      "other_user": { "id": 13, "name": "Casey", "total_ratings": 4 },
                      "delivery_date": "2026-04-15",
                      "package_description": "Books",
                      "route": "Montreal -> Toronto",
                      "days_since_delivery": "2.5",
                      "can_rate": true,
                      "user_role": "shipper",
                      "agreed_price": "75.00",
                      "transportation_method": "car"
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )
        val r = repo.fetchPendingReviews()
        assertTrue(r.isSuccess)
        val first = r.getOrNull()?.firstOrNull()!!
        assertEquals(17, first.matchId)
        assertEquals(2.5, first.daysSinceDelivery!!, 0.001)
    }

    @Test
    fun `updateComment posts review_text and forwards rating id`() = runBlocking {
        server.enqueue(json200("""{ "success": true, "message": "ok" }"""))
        val r = repo.updateRatingComment(ratingId = 42, reviewText = "Updated thoughts")
        assertTrue(r.isSuccess)
        val req = server.takeRequest()
        assertEquals("PUT", req.method)
        assertTrue(req.path?.endsWith("/ratings/42/comment") == true)
        assertTrue(req.body.readUtf8().contains("Updated thoughts"))
    }

    @Test
    fun `fetchReceivedRatings maps 401`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "message": "nope" }"""),
        )
        val r = repo.fetchReceivedRatings(userId = 1)
        assertTrue(r.isFailure)
    }
}
