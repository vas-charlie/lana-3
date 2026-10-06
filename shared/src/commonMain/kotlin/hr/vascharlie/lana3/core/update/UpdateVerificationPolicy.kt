package hr.vascharlie.lana3.core.update

enum class UpdateVerificationFailure {
    PACKAGE_MISMATCH,
    REMOTE_VERSION_MISMATCH,
    NON_FORWARD_VERSION,
    INSTALLED_SIGNER_MISSING,
    ARCHIVE_SIGNER_MISSING,
    SIGNER_MISMATCH,
}

sealed interface UpdateVerificationResult {
    data object Verified : UpdateVerificationResult

    data class Rejected(
        val failure: UpdateVerificationFailure,
    ) : UpdateVerificationResult
}

/**
 * Pure policy for deciding whether a downloaded application package is a valid
 * forward update of the currently installed application.
 *
 * Platform code remains responsible for reading package metadata and certificate
 * digests. This policy never reads files and never decides installer permission.
 */
class UpdateVerificationPolicy {
    fun verify(
        expectedPackageName: String,
        installedVersion: Long,
        remoteVersion: Long,
        archivePackageName: String,
        archiveVersion: Long,
        installedSignerDigests: Set<String>,
        archiveSignerDigests: Set<String>,
    ): UpdateVerificationResult {
        if (archivePackageName != expectedPackageName) {
            return rejected(UpdateVerificationFailure.PACKAGE_MISMATCH)
        }

        if (archiveVersion != remoteVersion) {
            return rejected(UpdateVerificationFailure.REMOTE_VERSION_MISMATCH)
        }

        if (archiveVersion <= installedVersion) {
            return rejected(UpdateVerificationFailure.NON_FORWARD_VERSION)
        }

        if (installedSignerDigests.isEmpty()) {
            return rejected(UpdateVerificationFailure.INSTALLED_SIGNER_MISSING)
        }

        if (archiveSignerDigests.isEmpty()) {
            return rejected(UpdateVerificationFailure.ARCHIVE_SIGNER_MISSING)
        }

        if (installedSignerDigests != archiveSignerDigests) {
            return rejected(UpdateVerificationFailure.SIGNER_MISMATCH)
        }

        return UpdateVerificationResult.Verified
    }

    private fun rejected(
        failure: UpdateVerificationFailure,
    ): UpdateVerificationResult.Rejected =
        UpdateVerificationResult.Rejected(failure)
}
