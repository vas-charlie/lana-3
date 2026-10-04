package hr.vascharlie.lana3.core.background

import hr.vascharlie.lana3.core.task.LanaTask
import hr.vascharlie.lana3.core.task.TaskPriority
import hr.vascharlie.lana3.core.task.TaskState
import kotlin.test.Test
import kotlin.test.assertIs

class BackgroundBehaviorPolicyTest {
    private val policy = BackgroundBehaviorPolicy()

    @Test
    fun foregroundTaskContinuesWithoutBackgroundCapability() {
        val result = policy.evaluate(
            task(TaskPriority.NORMAL),
            BackgroundContext(AppVisibility.FOREGROUND, BackgroundExecutionAvailability.UNAVAILABLE),
        )
        assertIs<BackgroundTaskDecision.Continue>(result)
    }

    @Test
    fun normalTaskPausesWhenBackgroundIsRestricted() {
        val result = policy.evaluate(
            task(TaskPriority.NORMAL),
            BackgroundContext(AppVisibility.BACKGROUND, BackgroundExecutionAvailability.RESTRICTED),
        )
        assertIs<BackgroundTaskDecision.Pause>(result)
    }

    @Test
    fun drivingCriticalTaskMayContinueWhenBackgroundIsRestricted() {
        val result = policy.evaluate(
            task(TaskPriority.DRIVING_CRITICAL),
            BackgroundContext(AppVisibility.BACKGROUND, BackgroundExecutionAvailability.RESTRICTED),
        )
        assertIs<BackgroundTaskDecision.Continue>(result)
    }

    @Test
    fun unknownCapabilityNeverGuesses() {
        val result = policy.evaluate(
            task(TaskPriority.SAFETY_CRITICAL),
            BackgroundContext(AppVisibility.UNKNOWN, BackgroundExecutionAvailability.UNKNOWN),
        )
        assertIs<BackgroundTaskDecision.Pause>(result)
    }

    @Test
    fun completedTaskIsRejected() {
        val result = policy.evaluate(
            task(TaskPriority.NORMAL, TaskState.COMPLETED),
            BackgroundContext(AppVisibility.FOREGROUND, BackgroundExecutionAvailability.AVAILABLE),
        )
        assertIs<BackgroundTaskDecision.Reject>(result)
    }

    private fun task(
        priority: TaskPriority,
        state: TaskState = TaskState.ACTIVE,
    ) = LanaTask(
        id = "test",
        kind = "test",
        priority = priority,
        state = state,
    )
}
