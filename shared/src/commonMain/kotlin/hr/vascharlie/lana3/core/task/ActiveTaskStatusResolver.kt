package hr.vascharlie.lana3.core.task

data class ActiveTaskStatus(
    val taskId: String?,
    val kind: String?,
    val state: TaskState?,
    val priority: TaskPriority?,
    val summary: String,
)

/**
 * Truthful snapshot for "What are you doing right now?".
 *
 * It reports only the supplied task state. It never invents progress or assumes that
 * a queued/paused task is active.
 */
class ActiveTaskStatusResolver {
    fun resolve(tasks: List<LanaTask>): ActiveTaskStatus {
        val active = tasks
            .filter { it.state == TaskState.ACTIVE }
            .maxWithOrNull(
                compareBy<LanaTask> { it.priority.rank }
                    .thenByDescending { it.id }
            )

        return if (active == null) {
            ActiveTaskStatus(
                taskId = null,
                kind = null,
                state = null,
                priority = null,
                summary = "No active task.",
            )
        } else {
            ActiveTaskStatus(
                taskId = active.id,
                kind = active.kind,
                state = active.state,
                priority = active.priority,
                summary = "Active task: " + active.kind + ".",
            )
        }
    }
}
