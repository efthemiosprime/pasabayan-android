package com.efthemiosprime.pasabayan.features.verification.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationSubmissionDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson
import com.efthemiosprime.pasabayan.features.verification.model.PhoneVerificationUiState
import com.efthemiosprime.pasabayan.features.verification.services.VerificationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class PhoneVerificationViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getString(R.string.verification_phone_error_invalid) } returns
            "Enter a valid 10-digit phone number"
        every { context.getString(R.string.verification_phone_error_otp_invalid) } returns
            "Enter the 6-digit code"
        every { context.getString(R.string.verification_phone_otp_sent) } returns "sent"
        every { context.getString(R.string.verification_phone_otp_resent) } returns "resent"
        every { context.getString(R.string.verification_phone_success) } returns "verified"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendOtp rejects invalid phone number`() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onPhoneNumberChange("123")
        vm.sendOtp()
        advanceUntilIdle()
        assertEquals("Enter a valid 10-digit phone number", vm.state.value.errorMessage)
        assertEquals(null, repo.lastSendPhone)
    }

    @Test
    fun `sendOtp normalizes to E_164 and starts resend timer`() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onPhoneNumberChange("(514) 555-1234")
        vm.sendOtp()
        // Let the network call and timer kickoff settle but not the entire 60s cooldown.
        advanceTimeBy(10L)
        assertEquals("+15145551234", repo.lastSendPhone)
        assertTrue(vm.state.value.isOtpSent)
        assertFalse(vm.state.value.canResend)
        assertEquals(PhoneVerificationUiState.RESEND_COOLDOWN_SECONDS, vm.state.value.remainingResendSeconds)
    }

    @Test
    fun `verifyOtp success flips isVerified and stops timer`() = runTest(dispatcher) {
        val repo = FakeRepo(verifyResult = Result.success(VerifyOtpDataJson(phone = "+15145551234")))
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onPhoneNumberChange("5145551234")
        vm.sendOtp()
        advanceUntilIdle()
        vm.onOtpCodeChange("123456")
        vm.verifyOtp()
        advanceUntilIdle()
        assertTrue(vm.state.value.isVerified)
        assertEquals("verified", vm.state.value.successMessage)
    }

    @Test
    fun `verifyOtp rejects short code without hitting network`() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onPhoneNumberChange("5145551234")
        vm.sendOtp()
        advanceUntilIdle()
        vm.onOtpCodeChange("12345")
        vm.verifyOtp()
        advanceUntilIdle()
        assertEquals("Enter the 6-digit code", vm.state.value.errorMessage)
        assertNull(repo.lastVerifyCode)
    }

    @Test
    fun `onOtpCodeChange strips non-digits and caps at six chars`() {
        val repo = FakeRepo()
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onOtpCodeChange("ab12 34cd56789")
        assertEquals("123456", vm.state.value.otpCode)
    }

    @Test
    fun `resend gated until cooldown elapses, then fires`() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = PhoneVerificationViewModel(repo, context)
        vm.onPhoneNumberChange("5145551234")
        vm.sendOtp()
        advanceUntilIdle()
        // Cooldown is 60s — try too early.
        vm.resendOtp()
        advanceUntilIdle()
        assertEquals(1, repo.sendCallCount) // No additional send/resend yet.
        // Advance past cooldown.
        advanceTimeBy(61_000L)
        advanceUntilIdle()
        assertTrue(vm.state.value.canResend)
        vm.resendOtp()
        advanceUntilIdle()
        assertEquals("+15145551234", repo.lastResendPhone)
        assertEquals("", vm.state.value.otpCode) // cleared on resend
    }

    @Test
    fun `bootstrap with pending verification resumes OTP step`() = runTest(dispatcher) {
        val repo = FakeRepo(
            statusResult = Result.success(
                PhoneStatusDataJson(
                    phone = "+15145551234",
                    phoneVerified = false,
                    pendingVerifications = 1,
                ),
            ),
        )
        val vm = PhoneVerificationViewModel(repo, context)
        vm.bootstrap()
        advanceUntilIdle()
        assertTrue(vm.state.value.isOtpSent)
        assertFalse(vm.state.value.isVerified)
    }

    @Test
    fun `bootstrap with already-verified phone shows verified state`() = runTest(dispatcher) {
        val repo = FakeRepo(
            statusResult = Result.success(
                PhoneStatusDataJson(
                    phone = "+15145551234",
                    phoneVerified = true,
                    isFullyVerified = true,
                ),
            ),
        )
        val vm = PhoneVerificationViewModel(repo, context)
        vm.bootstrap()
        advanceUntilIdle()
        assertTrue(vm.state.value.isVerified)
    }
}

private class FakeRepo(
    private val sendResult: Result<OtpDataJson?> = Result.success(OtpDataJson(phone = "")),
    private val verifyResult: Result<VerifyOtpDataJson?> = Result.success(null),
    private val resendResult: Result<OtpDataJson?> = Result.success(OtpDataJson(phone = "")),
    private val statusResult: Result<PhoneStatusDataJson> =
        Result.success(PhoneStatusDataJson()),
) : VerificationRepository {

    var lastSendPhone: String? = null
        private set
    var lastVerifyCode: String? = null
        private set
    var lastResendPhone: String? = null
        private set
    var sendCallCount: Int = 0
        private set

    override suspend fun sendOtp(phone: String): Result<OtpDataJson?> {
        lastSendPhone = phone
        sendCallCount++
        return sendResult
    }

    override suspend fun verifyOtp(phone: String, otpCode: String): Result<VerifyOtpDataJson?> {
        lastVerifyCode = otpCode
        return verifyResult
    }

    override suspend fun resendOtp(phone: String): Result<OtpDataJson?> {
        lastResendPhone = phone
        return resendResult
    }

    override suspend fun fetchPhoneStatus(): Result<PhoneStatusDataJson> = statusResult

    override suspend fun submitPremiumVerification(
        idType: String,
        idDocumentFront: ByteArray,
        idDocumentBack: ByteArray?,
        selfieWithId: ByteArray,
        idNumber: String?,
        birthDate: String?,
        mimeType: String,
    ): Result<PremiumVerificationSubmissionDataJson?> = error("unused")

    override suspend fun fetchPremiumStatus(): Result<PremiumVerificationStatusDataJson> =
        error("unused")
}
