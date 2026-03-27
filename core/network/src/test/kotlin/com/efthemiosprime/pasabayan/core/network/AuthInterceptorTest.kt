package com.efthemiosprime.pasabayan.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun bearerAttached_forNonAuthPaths() {
        assertTrue(AuthInterceptor.shouldAttachBearer("/api/trips"))
        assertTrue(AuthInterceptor.shouldAttachBearer("/api/packages"))
    }

    @Test
    fun bearerAttached_forAuthMe() {
        assertTrue(AuthInterceptor.shouldAttachBearer("/api/auth/me"))
    }

    @Test
    fun bearerSkipped_forAuthExceptMe() {
        assertFalse(AuthInterceptor.shouldAttachBearer("/api/auth/login"))
        assertFalse(AuthInterceptor.shouldAttachBearer("/api/auth/register"))
    }
}
