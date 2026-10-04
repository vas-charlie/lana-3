package hr.vascharlie.lana3.core.task

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActiveTaskStatusResolverTest {
    private val resolver = ActiveTaskStatusResolver()

    @Test
    fun reportsActualActiveTask() {
        val status = resolver.resolve(
            listOf(
                LanaTask("queued", "note", TaskPriority.NORMAL, state = TaskState.QUEUED),
                LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
            )
        )

        assertEquals("nav", status.taskId)
        assertEquals("navigation", status.kind)
        assertEquals(TaskState.ACTIVE, status.state)
        assertEquals("Active task: navigation.", status.summary)
    }

    @Test
    fun pausedOrQueuedTasksAreNotReportedAsActive() {
        val status = resolver.resolve(
            listOf(
                LanaTask("paused", "guide", TaskPriority.NORMAL, state = TaskState.PAUSED),
                LanaTask("queued", "note", TaskPriority.NORMAL, state = TaskState.QUEUED),
            )
        )

        assertNull(status.taskId)
        assertEquals("No active task.", status.summary)
    }

    @Test
    fun highestPriorityWinsIfStateIsTemporarilyInconsistent() {
        val status = resolver.resolve(
            listOf(
                LanaTask("music", "music", TaskPriority.NORMAL, state = TaskState.ACTIVE),
                LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
            )
        )

        assertEquals("nav", status.taskId)
    }

    @Test
    fun safeStopLeavesNoActiveTask() {
        val tasks = listOf(
            LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
        )

        val stopped = SafeStop().apply(tasks)
        val status = resolver.resolve(stopped.tasks)

        assertNull(status.taskId)
        assertEquals("No active task.", status.summary)
    }
}
