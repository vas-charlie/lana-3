package hr.vascharlie.lana3.core.update

sealed interface UpdateDecision {
    data object UpToDate : UpdateDecision
    data object Download : UpdateDecision
    data object ResumeExistingDownload : UpdateDecision
}

class UpdatePolicy {
    fun decide(
        installedVersion: Int,
        remoteVersion: Int,
        pendingVersion: Int?,
        pendingDownloadId: Long?,
    ): UpdateDecision {
        if (remoteVersion <= installedVersion) {
            return UpdateDecision.UpToDate
        }

        if (
            pendingVersion == remoteVersion &&
            pendingDownloadId != null &&
            pendingDownloadId >= 0L
        ) {
            return UpdateDecision.ResumeExistingDownload
        }

        return UpdateDecision.Download
    }
}
