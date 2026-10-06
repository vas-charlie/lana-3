package hr.vascharlie.lana3.core.update

import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateVerificationPolicyTest {
    private val policy = UpdateVerificationPolicy()
    private val signer = setOf("ABC123")

    @Test
    fun acceptsOnlyForwardSamePackageSameSignerUpdate() {
        assertEquals(
            UpdateVerificationResult.Verified,
            verify(),
        )
    }

    @Test
    fun rejectsDifferentPackage() {
        assertRejected(
            UpdateVerificationFailure.PACKAGE_MISMATCH,
            verify(archivePackageName = "attacker.example"),
        )
    }

    @Test
    fun rejectsVersionThatDoesNotMatchAdvertisedRelease() {
        assertRejected(
            UpdateVerificationFailure.REMOTE_VERSION_MISMATCH,
            verify(archiveVersion = 12L),
        )
    }

    @Test
    fun rejectsSameOrOlderVersion() {
        assertRejected(
            UpdateVerificationFailure.NON_FORWARD_VERSION,
            verify(
                installedVersion = 10L,
                remoteVersion = 10L,
                archiveVersion = 10L,
            ),
        )
    }

    @Test
    fun rejectsWhenInstalledSigningIdentityCannotBeRead() {
        assertRejected(
            UpdateVerificationFailure.INSTALLED_SIGNER_MISSING,
            verify(installedSignerDigests = emptySet()),
        )
    }

    @Test
    fun rejectsArchiveWithoutSigningIdentity() {
        assertRejected(
            UpdateVerificationFailure.ARCHIVE_SIGNER_MISSING,
            verify(archiveSignerDigests = emptySet()),
        )
    }

    @Test
    fun rejectsDifferentSigningIdentity() {
        assertRejected(
            UpdateVerificationFailure.SIGNER_MISMATCH,
            verify(archiveSignerDigests = setOf("DIFFERENT")),
        )
    }

    @Test
    fun rejectsSignerSetWithUnexpectedAdditionalSigner() {
        assertRejected(
            UpdateVerificationFailure.SIGNER_MISMATCH,
            verify(archiveSignerDigests = setOf("ABC123", "EXTRA")),
        )
    }

    private fun verify(
        expectedPackageName: String = "hr.vascharlie.lana3",
        installedVersion: Long = 10L,
        remoteVersion: Long = 11L,
        archivePackageName: String = expectedPackageName,
        archiveVersion: Long = remoteVersion,
        installedSignerDigests: Set<String> = signer,
        archiveSignerDigests: Set<String> = signer,
    ): UpdateVerificationResult =
        policy.verify(
            expectedPackageName = expectedPackageName,
            installedVersion = installedVersion,
            remoteVersion = remoteVersion,
            archivePackageName = archivePackageName,
            archiveVersion = archiveVersion,
            installedSignerDigests = installedSignerDigests,
            archiveSignerDigests = archiveSignerDigests,
        )

    private fun assertRejected(
        expected: UpdateVerificationFailure,
        result: UpdateVerificationResult,
    ) {
        val rejected = result as UpdateVerificationResult.Rejected
        assertEquals(expected, rejected.failure)
    }
}
