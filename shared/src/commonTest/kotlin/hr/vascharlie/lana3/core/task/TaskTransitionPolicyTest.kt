package hr.vascharlie.lana3.core.task

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TaskTransitionPolicyTest {
    private val policy = TaskTransitionPolicy()

    @Test
    fun queuedTaskCanStart() {
        val task = LanaTask(
            id = "t1",
            kind = "save-note",
            priority = TaskPriority.NORMAL,
            state = TaskState.QUEUED,
        )

        val result = policy.transition(task, TaskState.ACTIVE)
        val applied = assertIs<TaskTransitionResult.Applied>(result)

        assertEquals(TaskState.ACTIVE, applied.task.state)
    }

    @Test
    fun activeTaskCanComplete() {
        val task = LanaTask(
            id = "t1",
            kind = "save-note",
            priority = TaskPriority.NORMAL,
            state = TaskState.ACTIVE,
        )

        val result = policy.transition(task, TaskState.COMPLETED)
        val applied = assertIs<TaskTransitionResult.Applied>(result)

        assertEquals(TaskState.COMPLETED, applied.task.state)
    }

    @Test
    fun queuedTaskCannotPretendItCompletedWithoutRunning() {
        val task = LanaTask(
            id = "t1",
            kind = "save-note",
            priority = TaskPriority.NORMAL,
            state = TaskState.QUEUED,
        )

        assertIs<TaskTransitionResult.Rejected>(
            policy.transition(task, TaskState.COMPLETED)
        )
    }

    @Test
    fun pausedTaskCanResumeButCannotCompleteDirectly() {
        val task = LanaTask(
            id = "t1",
            kind = "navigation",
            priority = TaskPriority.DRIVING_CRITICAL,
            state = TaskState.PAUSED,
        )

        assertIs<TaskTransitionResult.Applied>(
            policy.transition(task, TaskState.ACTIVE)
        )
        assertIs<TaskTransitionResult.Rejected>(
            policy.transition(task, TaskState.COMPLETED)
        )
    }

    @Test
    fun queuedTaskMayFailBeforeExecution() {
        val task = LanaTask(
            id = "t1",
            kind = "sync",
            priority = TaskPriority.NORMAL,
            state = TaskState.QUEUED,
        )

        val result = policy.transition(task, TaskState.FAILED)
        val applied = assertIs<TaskTransitionResult.Applied>(result)

        assertEquals(TaskState.FAILED, applied.task.state)
    }

    @Test
    fun terminalTaskCannotRestart() {
        val terminalStates = listOf(
            TaskState.COMPLETED,
            TaskState.CANCELLED,
            TaskState.FAILED,
            TaskState.DEGRADED,
        )

        terminalStates.forEach { state ->
            val task = LanaTask(
                id = state.name,
                kind = "test",
                priority = TaskPriority.NORMAL,
                state = state,
            )

            assertIs<TaskTransitionResult.Rejected>(
                policy.transition(task, TaskState.ACTIVE)
            )
        }
    }

    @Test
    fun repeatedSameStateIsRejectedAsNoOp() {
        val task = LanaTask(
            id = "t1",
            kind = "test",
            priority = TaskPriority.NORMAL,
            state = TaskState.ACTIVE,
        )

        assertIs<TaskTransitionResult.Rejected>(
            policy.transition(task, TaskState.ACTIVE)
        )
    }
}
