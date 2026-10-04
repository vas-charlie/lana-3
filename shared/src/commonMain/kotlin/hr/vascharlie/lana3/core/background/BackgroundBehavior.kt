package hr.vascharlie.lana3.core.background

import hr.vascharlie.lana3.core.task.LanaTask
import hr.vascharlie.lana3.core.task.TaskPriority
import hr.vascharlie.lana3.core.task.TaskState

enum class AppVisibility { FOREGROUND, BACKGROUND, UNKNOWN }

enum class BackgroundExecutionAvailability {
    AVAILABLE,
    RESTRICTED,
    UNAVAILABLE,
    UNKNOWN,
}

data class BackgroundContext(
    val appVisibility: AppVisibility,
    val executionAvailability: BackgroundExecutionAvailability,
)

sealed interface BackgroundTaskDecision {
    data class Continue(val task: LanaTask) : BackgroundTaskDecision
    data class Pause(val task: LanaTask, val reason: String) : BackgroundTaskDecision
    data class Reject(val task: LanaTask, val reason: String) : BackgroundTaskDecision
}

/**
 * Device-independent policy for deciding whether an active task may continue when Lana
 * leaves the foreground. Platform adapters report capability; core never assumes that
 * background execution is available.
 */
class BackgroundBehaviorPolicy {
    fun evaluate(task: LanaTask, context: BackgroundContext): BackgroundTaskDecision {
        if (task.state == TaskState.COMPLETED || task.state == TaskState.CANCELLED) {
            return BackgroundTaskDecision.Reject(task, "Task is not runnable.")
        }
        if (context.appVisibility == AppVisibility.FOREGROUND) {
            return BackgroundTaskDecision.Continue(task)
        }
        return when (context.executionAvailability) {
            BackgroundExecutionAvailability.AVAILABLE ->
                BackgroundTaskDecision.Continue(task)
            BackgroundExecutionAvailability.RESTRICTED ->
                if (task.priority.rank >= TaskPriority.DRIVING_CRITICAL.rank) {
                    BackgroundTaskDecision.Continue(task)
                } else {
                    BackgroundTaskDecision.Pause(task, "Background execution is restricted for this task.")
                }
            BackgroundExecutionAvailability.UNAVAILABLE ->
                BackgroundTaskDecision.Pause(task, "Background execution is unavailable.")
            BackgroundExecutionAvailability.UNKNOWN ->
                BackgroundTaskDecision.Pause(task, "Background execution capability is unknown; Lana must not guess.")
        }
    }
}
