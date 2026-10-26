package hr.vascharlie.lana3.resume

/**
 * Language-neutral, side-effect-free resume metadata.
 * This is NOT a second task lifecycle engine or a persistence implementation.
 * Existing task orchestration remains authoritative.
 */
data class ResumeCheckpoint(
    val schemaVersion: Int = 1,
    val taskId: String,
    val taskType: String,
    val currentStepId: String,
    val nextActionId: String,
    val lastConfirmedResultRef: String? = null,
    val updatedAtEpochMillis: Long
)

enum class ResumeBlockReason {
    INVALID_CHECKPOINT,
    TASK_NOT_PAUSED,
    TASK_ID_MISMATCH,
    SAFE_STOP_ACTIVE,
    PERMISSION_RECHECK_REQUIRED,
    CONTEXT_RECHECK_REQUIRED
}

sealed class ResumeAssessment {
    data object ReadyForRevalidation : ResumeAssessment()
    data class Blocked(val reason: ResumeBlockReason) : ResumeAssessment()
}

/**
 * Preflight only. ReadyForRevalidation does not authorize or execute an action.
 * The existing task engine must independently recheck permissions, context,
 * idempotency, and the task transition before any side effect.
 */
object ResumeCheckpointPolicy {
    fun assess(
        checkpoint: ResumeCheckpoint,
        currentTaskId: String,
        taskIsPaused: Boolean,
        safeStopActive: Boolean
    ): ResumeAssessment {
        if (checkpoint.schemaVersion != 1 ||
            checkpoint.taskId.isBlank() ||
            checkpoint.taskType.isBlank() ||
            checkpoint.currentStepId.isBlank() ||
            checkpoint.nextActionId.isBlank() ||
            checkpoint.updatedAtEpochMillis < 0L
        ) return ResumeAssessment.Blocked(ResumeBlockReason.INVALID_CHECKPOINT)
        if (checkpoint.taskId != currentTaskId)
            return ResumeAssessment.Blocked(ResumeBlockReason.TASK_ID_MISMATCH)
        if (!taskIsPaused)
            return ResumeAssessment.Blocked(ResumeBlockReason.TASK_NOT_PAUSED)
        if (safeStopActive)
            return ResumeAssessment.Blocked(ResumeBlockReason.SAFE_STOP_ACTIVE)
        return ResumeAssessment.ReadyForRevalidation
    }
}
