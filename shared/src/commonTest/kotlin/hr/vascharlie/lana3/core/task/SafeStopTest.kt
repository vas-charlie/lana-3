package hr.vascharlie.lana3.core.task

import kotlin.test.Test
import kotlin.test.assertEquals

class SafeStopTest {
    private val safeStop = SafeStop()

    @Test
    fun cancelsAllRunnableLanaTasksAndReportsTheirIds() {
        val tasks = listOf(
            LanaTask("nav", "navigation", TaskPriority.DRIVING_CRITICAL, state = TaskState.ACTIVE),
            LanaTask("guide", "tourist-guide", TaskPriority.NORMAL, state = TaskState.PAUSED),
            LanaTask("note", "save-note", TaskPriority.BACKGROUND, state = TaskState.QUEUED),
        )

        val result = safeStop.apply(tasks)

        assertEquals(listOf("nav", "guide", "note"), result.stoppedTaskIds)
        assertEquals(listOf(TaskState.CANCELLED, TaskState.CANCELLED, TaskState.CANCELLED), result.tasks.map { it.state })
    }

    @Test
    fun leavesAlreadyTerminalTasksTerminal() {
        val tasks = listOf(
            LanaTask("done", "note", TaskPriority.NORMAL, state = TaskState.COMPLETED),
            LanaTask("cancelled", "call", TaskPriority.IMPORTANT, state = TaskState.CANCELLED),
        )

        val result = safeStop.apply(tasks)

        assertEquals(emptyList(), result.stoppedTaskIds)
        assertEquals(tasks, result.tasks)
    }

    @Test
    fun emptyTaskSetProducesClearEmptyState() {
        assertEquals(SafeStopResult(emptyList(), emptyList()), safeStop.apply(emptyList()))
    }
}
