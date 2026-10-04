package hr.vascharlie.lana3.core.task

data class SafeStopResult(
    val tasks: List<LanaTask>,
    val stoppedTaskIds: List<String>,
)

/**
 * Global safety stop for Lana-managed tasks.
 *
 * ACTIVE, QUEUED and PAUSED tasks are moved to CANCELLED so the core state becomes
 * explicit and deterministic. Platform execution adapters remain responsible for
 * stopping any external operation they started.
 */
class SafeStop {
    fun apply(tasks: List<LanaTask>): SafeStopResult {
        val stopped = mutableListOf<String>()
        val updated = tasks.map { task ->
            when (task.state) {
                TaskState.ACTIVE,
                TaskState.QUEUED,
                TaskState.PAUSED -> {
                    stopped += task.id
                    task.copy(state = TaskState.CANCELLED)
                }
                TaskState.COMPLETED,
                TaskState.CANCELLED -> task
            }
        }
        return SafeStopResult(
            tasks = updated,
            stoppedTaskIds = stopped,
        )
    }
}
