package hr.vascharlie.lana3.core.task

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActiveTaskStatusResolverTest {
    private val resolver = ActiveTaskStatusResolver()

    @Test
    fun reportsActualActiveTaskSemantically() {
        val status = resolver.resolve(
            listOf(
                LanaTask("queued", "note", TaskPriority.NORMAL, state = TaskState.QUEUED),
                LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
            )
        )

        assertEquals(ActiveTaskStatusKind.ACTIVE, status.kind)
        assertEquals("nav", status.taskId)
        assertEquals("navigation", status.taskKind)
        assertEquals(TaskState.ACTIVE, status.state)
        assertEquals(TaskPriority.DRIVING_CRITICAL, status.priority)
    }

    @Test
    fun pausedOrQueuedTasksProduceIdleSemanticStatus() {
        val status = resolver.resolve(
            listOf(
                LanaTask("paused", "guide", TaskPriority.NORMAL, state = TaskState.PAUSED),
                LanaTask("queued", "note", TaskPriority.NORMAL, state = TaskState.QUEUED),
            )
        )

        assertEquals(ActiveTaskStatusKind.IDLE, status.kind)
        assertNull(status.taskId)
        assertNull(status.taskKind)
        assertNull(status.state)
        assertNull(status.priority)
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
        assertEquals(TaskPriority.DRIVING_CRITICAL, status.priority)
    }

    @Test
    fun safeStopLeavesSemanticIdleStatus() {
        val tasks = listOf(
            LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
        )

        val stopped = SafeStop().apply(tasks)
        val status = resolver.resolve(stopped.tasks)

        assertEquals(ActiveTaskStatusKind.IDLE, status.kind)
        assertNull(status.taskId)
    }
}
