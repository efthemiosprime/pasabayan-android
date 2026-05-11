package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.profile.AccountDeletionDataJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentDataJson
import com.efthemiosprime.pasabayan.core.network.profile.DisclaimerAcknowledgmentsDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class AccountManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.profile_account_export_success) } returns "ok"
        every { context.getString(R.string.profile_account_export_error) } returns "err"
        every { context.getString(R.string.profile_account_deletion_success) } returns "deleted"
        every {
            context.getString(R.string.profile_account_deletion_success_formatted, any<String>())
        } answers {
            val status = secondArg<Array<Any?>>()[0] as String
            "deleted ($status)"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel(
        repo: ProfileRepository,
        writer: ExportFileWriter = RecordingWriter(),
    ): Pair<AccountManagementViewModel, RecordingWriter?> {
        val recording = writer as? RecordingWriter
        val vm = AccountManagementViewModel(repo, context)
        vm.overrideFileWriter(writer)
        return vm to recording
    }

    @Test
    fun `exportData success writes bytes and emits share event`() = runTest(testDispatcher) {
        val repo = FakeRepo(exportResult = Result.success("{\"a\":1}".toByteArray()))
        val writer = RecordingWriter()
        val (vm, recordingWriter) = newViewModel(repo, writer)
        vm.exportData()
        advanceUntilIdle()
        val request = vm.shareEvents.first()
        assertEquals("pasabayan-data-export.json", request.file.name)
        // Pretty-print expands the payload.
        assertTrue(
            "expected pretty-printed JSON, got: ${recordingWriter?.lastPayloadAsString()}",
            recordingWriter?.lastPayloadAsString()?.contains("\n") == true,
        )
        assertEquals("ok", vm.state.value.infoMessage)
        assertFalse(vm.state.value.isExporting)
    }

    @Test
    fun `exportData repo failure surfaces error`() = runTest(testDispatcher) {
        val repo = FakeRepo(exportResult = Result.failure(IllegalStateException("boom")))
        val (vm, _) = newViewModel(repo)
        vm.exportData()
        advanceUntilIdle()
        assertNotNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isExporting)
    }

    @Test
    fun `requestDelete opens confirm, cancel closes, no network`() = runTest(testDispatcher) {
        val repo = FakeRepo()
        val (vm, _) = newViewModel(repo)
        vm.requestDelete()
        assertTrue(vm.state.value.showDeleteConfirm)
        vm.cancelDelete()
        assertFalse(vm.state.value.showDeleteConfirm)
        advanceUntilIdle()
        assertNull(repo.lastDeletionReason)
    }

    @Test
    fun `confirmDelete success surfaces formatted message, emits signOut, sends reason`() =
        runTest(testDispatcher) {
            val repo = FakeRepo(
                deletionResult = Result.success(
                    AccountDeletionDataJson(status = "pending", requestId = "42"),
                ),
            )
            val (vm, _) = newViewModel(repo)
            vm.onDeletionReasonChange("Moving on")
            vm.requestDelete()
            vm.confirmDelete()
            advanceUntilIdle()
            assertEquals("Moving on", repo.lastDeletionReason)
            assertFalse(vm.state.value.isDeleting)
            assertEquals("deleted (pending)", vm.state.value.infoMessage)
            // Sign-out event fired once.
            val signedOut = vm.signOutEvents.first()
            assertEquals(Unit, signedOut)
        }

    @Test
    fun `confirmDelete failure surfaces error and does not sign out`() =
        runTest(testDispatcher) {
            val repo = FakeRepo(
                deletionResult = Result.failure(IllegalStateException("server down")),
            )
            val (vm, _) = newViewModel(repo)
            vm.onDeletionReasonChange("")
            vm.requestDelete()
            vm.confirmDelete()
            advanceUntilIdle()
            assertNotNull(vm.state.value.errorMessage)
            assertFalse(vm.state.value.isDeleting)
            // Reason was blank → sent as null.
            assertNull(repo.lastDeletionReason)
        }
}

private class RecordingWriter : ExportFileWriter {
    var lastPayload: ByteArray? = null
    override fun write(context: Context, payload: ByteArray): ExportShareRequest {
        lastPayload = payload
        return ExportShareRequest(File("/tmp/pasabayan-data-export.json"))
    }
    fun lastPayloadAsString(): String? = lastPayload?.let { String(it) }
}

private class FakeRepo(
    private val exportResult: Result<ByteArray> = Result.success(ByteArray(0)),
    private val deletionResult: Result<AccountDeletionDataJson> =
        Result.success(AccountDeletionDataJson()),
) : ProfileRepository {

    var lastDeletionReason: String? = null
        private set

    override suspend fun exportUserData(): Result<ByteArray> = exportResult

    override suspend fun requestAccountDeletion(reason: String?): Result<AccountDeletionDataJson> {
        lastDeletionReason = reason
        return deletionResult
    }

    override suspend fun fetchProfile(forceRefresh: Boolean) = error("unused")
    override suspend fun updateProfile(request: UpdateProfileRequestJson) = error("unused")
    override suspend fun uploadProfileAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String,
        fullName: String?,
        deliveryAddress: String?,
        preferredContactMethod: String?,
        additionalInfo: Map<String, String>?,
    ) = error("unused")
    override suspend fun deleteProfilePicture(): Result<Unit> = error("unused")
    override suspend fun fetchDisclaimerAcknowledgments(): Result<DisclaimerAcknowledgmentsDataJson> =
        error("unused")
    override suspend fun acknowledgeDisclaimer(type: String): Result<DisclaimerAcknowledgmentDataJson> =
        error("unused")
    override suspend fun fetchConsentPreferences(): Result<ConsentPreferencesDataJson> =
        error("unused")
    override suspend fun updateConsentPreferences(
        update: ConsentPreferencesUpdateJson,
    ): Result<ConsentPreferencesDataJson> = error("unused")
    override suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?> = error("unused")
    override suspend fun fetchCarrierStats(): Result<CarrierStatsJson?> = error("unused")
    override suspend fun fetchUserStats(): Result<UserStatsDataJson?> = error("unused")
    override suspend fun toggleCarrierStatus() = error("unused")
    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> = error("unused")
    override suspend fun updateCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> = error("unused")
    override suspend fun enableCarrier(): Result<Unit> = error("unused")
}
