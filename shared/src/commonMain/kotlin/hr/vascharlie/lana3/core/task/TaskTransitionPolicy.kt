package hr.vascharlie.lana3.core.task

sealed interface TaskTransitionResult {
    data class Applied(val task: LanaTask) : TaskTransitionResult

    data class Rejected(
        val task: LanaTask,
        val requestedState: TaskState,
        val reason: String,
    ) : TaskTransitionResult
}

/**
 * Deterministic lifecycle policy for LANA-managed tasks.
 *
 * Terminal states never transition silently back into runnable states.
 * QUEUED may fail before execution begins, while COMPLETED is reachable only
 * from ACTIVE work that actually ran.
 */
class TaskTransitionPolicy {
    fun transition(
        task: LanaTask,
        to: TaskState,
    ): TaskTransitionResult {
        if (task.state == to) {
            return TaskTransitionResult.Rejected(
                task = task,
                requestedState = to,
                reason = "Task is already in state " + to + ".",
            )
        }

        if (task.state.isTerminal) {
            return TaskTransitionResult.Rejected(
                task = task,
                requestedState = to,
                reason = "Terminal task cannot transition to " + to + ".",
            )
        }

        val allowed = when (task.state) {
            TaskState.QUEUED -> to in setOf(
                TaskState.ACTIVE,
                TaskState.CANCELLED,
                TaskState.FAILED,
                TaskState.DEGRADED,
            )

            TaskState.ACTIVE -> to in setOf(
                TaskState.PAUSED,
                TaskState.COMPLETED,
                TaskState.CANCELLED,
                TaskState.FAILED,
                TaskState.DEGRADED,
            )

            TaskState.PAUSED -> to in setOf(
                TaskState.ACTIVE,
                TaskState.CANCELLED,
                TaskState.FAILED,
                TaskState.DEGRADED,
            )

            TaskState.COMPLETED,
            TaskState.CANCELLED,
            TaskState.FAILED,
            TaskState.DEGRADED -> false
        }

        return if (allowed) {
            TaskTransitionResult.Applied(task.copy(state = to))
        } else {
            TaskTransitionResult.Rejected(
                task = task,
                requestedState = to,
                reason = "Transition " + task.state + " -> " + to + " is not allowed.",
            )
        }
    }
}
