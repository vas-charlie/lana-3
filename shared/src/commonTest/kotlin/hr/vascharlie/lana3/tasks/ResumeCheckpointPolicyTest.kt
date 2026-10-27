package hr.vascharlie.lana3.tasks

import kotlin.test.Test
import kotlin.test.assertEquals

class ResumeCheckpointPolicyTest {
    @Test fun rejectsTerminalTask() {
        val checkpoint = ResumeCheckpoint(1, "task", "conversation", "step", "next", null, 1L, ResumeTaskState.COMPLETED)
        assertEquals(ResumeValidation.Rejected(ResumeValidation.Reason.NOT_PAUSED), ResumeCheckpointPolicy.validate(checkpoint))
    }

    @Test fun pausedTaskNeedsReauthorization() {
        val checkpoint = ResumeCheckpoint(1, "task", "conversation", "step", "next", null, 1L, ResumeTaskState.PAUSED)
        assertEquals(ResumeValidation.ReadyForReauthorization, ResumeCheckpointPolicy.validate(checkpoint))
    }
}
