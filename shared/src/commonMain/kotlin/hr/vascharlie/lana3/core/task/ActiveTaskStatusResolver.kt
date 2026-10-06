package hr.vascharlie.lana3.core.task

enum class ActiveTaskStatusKind {
    IDLE,
    ACTIVE,
}

data class ActiveTaskStatus(
    val kind: ActiveTaskStatusKind,
    val taskId: String?,
    val taskKind: String?,
    val state: TaskState?,
    val priority: TaskPriority?,
)

/**
 * Truthful semantic snapshot for "What are you doing right now?".
 *
 * The shared core reports state only. Human-readable wording belongs at the
 * presentation boundary so the same task status can be rendered in any language.
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
                kind = ActiveTaskStatusKind.IDLE,
                taskId = null,
                taskKind = null,
                state = null,
                priority = null,
            )
        } else {
            ActiveTaskStatus(
                kind = ActiveTaskStatusKind.ACTIVE,
                taskId = active.id,
                taskKind = active.kind,
                state = active.state,
                priority = active.priority,
            )
        }
    }
}
