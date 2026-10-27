package hr.vascharlie.lana3.tasks

/**
 * Durable reference to a paused execution task.
 * Does not store conversation transcripts, secrets, or side effects.
 */
data class ResumeCheckpoint(
    val schemaVersion: Int,
    val taskId: String,
    val taskType: String,
    val currentStepId: String,
    val nextActionId: String,
    val lastConfirmedResultRef: String?,
    val updatedAtEpochMillis: Long,
    val state: ResumeTaskState,
)

enum class ResumeTaskState { ACTIVE, PAUSED, COMPLETED, CANCELLED, FAILED, DEGRADED }

sealed interface ResumeValidation {
    data object ReadyForReauthorization : ResumeValidation
    data class Rejected(val reason: Reason) : ResumeValidation

    enum class Reason { UNSUPPORTED_VERSION, INVALID_REFERENCE, NOT_PAUSED, INVALID_TIME }
}

/**
 * Pure validation only. A valid checkpoint NEVER authorizes execution.
 * The caller must recheck capabilities, permissions and current context.
 */
object ResumeCheckpointPolicy {
    const val SCHEMA_VERSION = 1

    fun validate(checkpoint: ResumeCheckpoint): ResumeValidation {
        if (checkpoint.schemaVersion != SCHEMA_VERSION) {
            return ResumeValidation.Rejected(ResumeValidation.Reason.UNSUPPORTED_VERSION)
        }
        if (checkpoint.taskId.isBlank() || checkpoint.taskType.isBlank() ||
            checkpoint.currentStepId.isBlank() || checkpoint.nextActionId.isBlank()) {
            return ResumeValidation.Rejected(ResumeValidation.Reason.INVALID_REFERENCE)
        }
        if (checkpoint.updatedAtEpochMillis <= 0L) {
            return ResumeValidation.Rejected(ResumeValidation.Reason.INVALID_TIME)
        }
        if (checkpoint.state != ResumeTaskState.PAUSED) {
            return ResumeValidation.Rejected(ResumeValidation.Reason.NOT_PAUSED)
        }
        return ResumeValidation.ReadyForReauthorization
    }
}
