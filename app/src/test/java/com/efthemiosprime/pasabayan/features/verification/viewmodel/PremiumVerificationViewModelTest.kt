package com.efthemiosprime.pasabayan.features.verification.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationRequestJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationSubmissionDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson
import com.efthemiosprime.pasabayan.features.profile.services.ImageCompressor
import com.efthemiosprime.pasabayan.features.verification.model.IdDocumentType
import com.efthemiosprime.pasabayan.features.verification.model.PremiumApplicationStatus
import com.efthemiosprime.pasabayan.features.verification.services.VerificationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class PremiumVerificationViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val passthroughCompressor = ImageCompressor { it }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getString(R.string.verification_premium_submitted) } returns "submitted"
        every { context.getString(R.string.verification_premium_missing_images) } returns "missing"
        every { context.getString(R.string.verification_premium_image_decode_error) } returns "decode error"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit blocked until front, back, and selfie are set for non-passport`() =
        runTest(dispatcher) {
            val repo = FakePremiumRepo()
            val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
            vm.onIdTypeChange(IdDocumentType.DRIVERS_LICENSE)
            vm.onImageSelected(PremiumImageSlot.FRONT, byteArrayOf(1))
            vm.onImageSelected(PremiumImageSlot.SELFIE, byteArrayOf(2))
            vm.submit()
            advanceUntilIdle()
            // No back image yet → request blocked.
            assertNull(repo.lastSubmit)
            vm.onImageSelected(PremiumImageSlot.BACK, byteArrayOf(3))
            vm.submit()
            advanceUntilIdle()
            assertNotNull(repo.lastSubmit)
            assertEquals("drivers_license", repo.lastSubmit?.idType)
        }

    @Test
    fun `passport submission does not require back image`() = runTest(dispatcher) {
        val repo = FakePremiumRepo()
        val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
        vm.onIdTypeChange(IdDocumentType.PASSPORT)
        vm.onImageSelected(PremiumImageSlot.FRONT, byteArrayOf(1))
        vm.onImageSelected(PremiumImageSlot.SELFIE, byteArrayOf(2))
        vm.submit()
        advanceUntilIdle()
        assertNotNull(repo.lastSubmit)
        assertEquals("passport", repo.lastSubmit?.idType)
        assertNull(repo.lastSubmit?.idDocumentBack)
    }

    @Test
    fun `switching to passport clears any back image`() {
        val repo = FakePremiumRepo()
        val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
        vm.onIdTypeChange(IdDocumentType.DRIVERS_LICENSE)
        vm.onImageSelected(PremiumImageSlot.BACK, byteArrayOf(99))
        vm.onIdTypeChange(IdDocumentType.PASSPORT)
        assertNull(vm.state.value.idDocumentBack)
    }

    @Test
    fun `onImageSelected surfaces decode error and does not store bytes`() {
        val repo = FakePremiumRepo()
        val failingCompressor = ImageCompressor { throw IllegalArgumentException("nope") }
        val vm = PremiumVerificationViewModel(repo, failingCompressor, context)
        vm.onImageSelected(PremiumImageSlot.FRONT, byteArrayOf(1, 2, 3))
        assertEquals("decode error", vm.state.value.errorMessage)
        assertNull(vm.state.value.idDocumentFront)
    }

    @Test
    fun `submit success surfaces pending status and success message`() =
        runTest(dispatcher) {
            val repo = FakePremiumRepo(
                submitResult = Result.success(
                    PremiumVerificationSubmissionDataJson(status = "pending"),
                ),
            )
            val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
            vm.onIdTypeChange(IdDocumentType.PASSPORT)
            vm.onImageSelected(PremiumImageSlot.FRONT, byteArrayOf(1))
            vm.onImageSelected(PremiumImageSlot.SELFIE, byteArrayOf(2))
            vm.onIdNumberChange("AB123")
            vm.onBirthDateChange("1990-05-21")
            vm.submit()
            advanceUntilIdle()
            assertTrue(vm.state.value.isSubmitted)
            assertEquals(PremiumApplicationStatus.PENDING, vm.state.value.pendingStatus)
            assertEquals("submitted", vm.state.value.successMessage)
            assertEquals("AB123", repo.lastSubmit?.idNumber)
            assertEquals("1990-05-21", repo.lastSubmit?.birthDate)
        }

    @Test
    fun `bootstrap surfaces existing pending application`() = runTest(dispatcher) {
        val repo = FakePremiumRepo(
            statusResult = Result.success(
                PremiumVerificationStatusDataJson(
                    verificationLevel = "verified",
                    requests = listOf(PremiumVerificationRequestJson(status = "under_review")),
                ),
            ),
        )
        val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
        vm.bootstrap()
        advanceUntilIdle()
        assertFalse(vm.state.value.isLoadingStatus)
        assertEquals(PremiumApplicationStatus.UNDER_REVIEW, vm.state.value.pendingStatus)
        assertTrue(vm.state.value.isSubmitted)
    }

    @Test
    fun `submit with missing images surfaces validation error`() = runTest(dispatcher) {
        val repo = FakePremiumRepo()
        val vm = PremiumVerificationViewModel(repo, passthroughCompressor, context)
        vm.onIdTypeChange(IdDocumentType.PASSPORT)
        vm.submit()
        advanceUntilIdle()
        assertEquals("missing", vm.state.value.errorMessage)
        assertNull(repo.lastSubmit)
    }
}

data class SubmitCall(
    val idType: String,
    val idDocumentBack: ByteArray?,
    val idNumber: String?,
    val birthDate: String?,
)

private class FakePremiumRepo(
    private val submitResult: Result<PremiumVerificationSubmissionDataJson?> =
        Result.success(PremiumVerificationSubmissionDataJson(status = "pending")),
    private val statusResult: Result<PremiumVerificationStatusDataJson> =
        Result.success(PremiumVerificationStatusDataJson()),
) : VerificationRepository {

    var lastSubmit: SubmitCall? = null
        private set

    override suspend fun submitPremiumVerification(
        idType: String,
        idDocumentFront: ByteArray,
        idDocumentBack: ByteArray?,
        selfieWithId: ByteArray,
        idNumber: String?,
        birthDate: String?,
        mimeType: String,
    ): Result<PremiumVerificationSubmissionDataJson?> {
        lastSubmit = SubmitCall(idType, idDocumentBack, idNumber, birthDate)
        return submitResult
    }

    override suspend fun fetchPremiumStatus(): Result<PremiumVerificationStatusDataJson> =
        statusResult

    override suspend fun sendOtp(phone: String): Result<OtpDataJson?> = error("unused")
    override suspend fun verifyOtp(phone: String, otpCode: String): Result<VerifyOtpDataJson?> =
        error("unused")
    override suspend fun resendOtp(phone: String): Result<OtpDataJson?> = error("unused")
    override suspend fun fetchPhoneStatus(): Result<PhoneStatusDataJson> = error("unused")
}
