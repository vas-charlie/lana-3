package hr.vascharlie.lana3.resume

import kotlin.test.Test
import kotlin.test.assertEquals

class ResumeCheckpointPolicyTest {
    private val valid = ResumeCheckpoint(
        taskId = "task-1",
        taskType = "project-work",
        currentStepId = "step-2",
        nextActionId = "review-diff",
        lastConfirmedResultRef = "commit-123",
        updatedAtEpochMillis = 1000L
    )

    @Test fun pausedTaskOnlyReachesRevalidation() {
        assertEquals(
            ResumeAssessment.ReadyForRevalidation,
            ResumeCheckpointPolicy.assess(valid, "task-1", taskIsPaused = true, safeStopActive = false)
        )
    }

    @Test fun activeOrCompletedTaskCannotResumeThroughCheckpoint() {
        assertEquals(
            ResumeAssessment.Blocked(ResumeBlockReason.TASK_NOT_PAUSED),
            ResumeCheckpointPolicy.assess(valid, "task-1", taskIsPaused = false, safeStopActive = false)
        )
    }

    @Test fun stopOverridesResume() {
        assertEquals(
            ResumeAssessment.Blocked(ResumeBlockReason.SAFE_STOP_ACTIVE),
            ResumeCheckpointPolicy.assess(valid, "task-1", taskIsPaused = true, safeStopActive = true)
        )
    }

    @Test fun taskIdentityMustMatch() {
        assertEquals(
            ResumeAssessment.Blocked(ResumeBlockReason.TASK_ID_MISMATCH),
            ResumeCheckpointPolicy.assess(valid, "task-2", taskIsPaused = true, safeStopActive = false)
        )
    }

    @Test fun invalidMetadataFailsClosed() {
        assertEquals(
            ResumeAssessment.Blocked(ResumeBlockReason.INVALID_CHECKPOINT),
            ResumeCheckpointPolicy.assess(valid.copy(nextActionId = ""), "task-1", true, false)
        )
        assertEquals(
            ResumeAssessment.Blocked(ResumeBlockReason.INVALID_CHECKPOINT),
            ResumeCheckpointPolicy.assess(valid.copy(schemaVersion = 2), "task-1", true, false)
        )
    }
}
