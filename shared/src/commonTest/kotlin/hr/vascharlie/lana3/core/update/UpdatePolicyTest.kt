package hr.vascharlie.lana3.core.update

import kotlin.test.Test
import kotlin.test.assertEquals

class UpdatePolicyTest {
    private val policy = UpdatePolicy()

    @Test
    fun sameVersionIsUpToDate() {
        assertEquals(
            UpdateDecision.UpToDate,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 10,
                pendingVersion = null,
                pendingDownloadId = null,
            )
        )
    }

    @Test
    fun olderRemoteVersionNeverDowngrades() {
        assertEquals(
            UpdateDecision.UpToDate,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 9,
                pendingVersion = null,
                pendingDownloadId = null,
            )
        )
    }

    @Test
    fun newerVersionDownloadsWhenNoMatchingPendingDownloadExists() {
        assertEquals(
            UpdateDecision.Download,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 11,
                pendingVersion = null,
                pendingDownloadId = null,
            )
        )
    }

    @Test
    fun matchingPendingDownloadIsResumedInsteadOfDuplicated() {
        assertEquals(
            UpdateDecision.ResumeExistingDownload,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 11,
                pendingVersion = 11,
                pendingDownloadId = 42L,
            )
        )
    }

    @Test
    fun stalePendingVersionDoesNotBlockNewerDownload() {
        assertEquals(
            UpdateDecision.Download,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 12,
                pendingVersion = 11,
                pendingDownloadId = 42L,
            )
        )
    }

    @Test
    fun invalidPendingIdDoesNotCountAsDownload() {
        assertEquals(
            UpdateDecision.Download,
            policy.decide(
                installedVersion = 10,
                remoteVersion = 11,
                pendingVersion = 11,
                pendingDownloadId = -1L,
            )
        )
    }
}
