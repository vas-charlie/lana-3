package hr.vascharlie.lana3.core.task

import kotlin.test.Test
import kotlin.test.assertIs

class TaskArbiterTest {
    private val arbiter = TaskArbiter()

    @Test
    fun safetyCriticalPreemptsInterruptibleBackgroundTask() {
        val active = LanaTask("guide", "tourist-guide", TaskPriority.BACKGROUND, state = TaskState.ACTIVE)
        val incoming = LanaTask("stop", "safe-stop", TaskPriority.SAFETY_CRITICAL)
        assertIs<TaskDecision.Preempt>(arbiter.decide(active, incoming))
    }

    @Test
    fun lowerPriorityTaskWaitsDuringDrivingCriticalTask() {
        val active = LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE)
        val incoming = LanaTask("music", "music", TaskPriority.NORMAL)
        assertIs<TaskDecision.Queue>(arbiter.decide(active, incoming))
    }

    @Test
    fun nonInterruptibleTaskIsNeverSilentlyInterrupted() {
        val active = LanaTask(
            "call",
            "emergency-call",
            TaskPriority.IMPORTANT,
            interruptible = false,
            state = TaskState.ACTIVE,
        )
        val incoming = LanaTask("stop", "safe-stop", TaskPriority.SAFETY_CRITICAL)
        assertIs<TaskDecision.Queue>(arbiter.decide(active, incoming))
    }

    @Test
    fun failedIncomingTaskCannotRestartSilently() {
        val incoming = LanaTask(
            "failed",
            "sync",
            TaskPriority.NORMAL,
            state = TaskState.FAILED,
        )

        assertIs<TaskDecision.Reject>(arbiter.decide(null, incoming))
    }

    @Test
    fun degradedIncomingTaskCannotRestartSilently() {
        val incoming = LanaTask(
            "degraded",
            "guide",
            TaskPriority.NORMAL,
            state = TaskState.DEGRADED,
        )

        assertIs<TaskDecision.Reject>(arbiter.decide(null, incoming))
    }
}
