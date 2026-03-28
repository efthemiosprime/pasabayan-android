package com.efthemiosprime.pasabayan.core.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test

class TokenClearingHandlerTest {

    private class TestTokenStore : TokenStore {
        private var token: String? = null
        override fun getToken(): String? = token
        override fun setToken(token: String?) {
            this.token = token
        }
        override fun clear() {
            token = null
        }
    }

    /**
     * Collector must be active before [TokenClearingHandler.onUnauthorized] runs; otherwise
     * [MutableSharedFlow.tryEmit] can run with no subscribers and [kotlinx.coroutines.flow.first]
     * may never complete (Gradle appears “stuck” on this test).
     */
    @Test(timeout = 5_000)
    fun onUnauthorized_clearsTokenAndEmitsEvent() = runBlocking {
        val notifier = UnauthorizedSessionNotifier()
        val store = TestTokenStore().apply { setToken("jwt") }
        val handler = TokenClearingHandler(store, notifier)
        val job = launch(Dispatchers.Unconfined) {
            notifier.events.take(1).collect { }
        }
        handler.onUnauthorized()
        job.join()
        assertNull(store.getToken())
    }
}
