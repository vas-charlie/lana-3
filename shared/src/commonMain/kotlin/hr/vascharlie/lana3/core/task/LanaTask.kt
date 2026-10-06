package hr.vascharlie.lana3.core.task

enum class TaskPriority(val rank: Int) {
    BACKGROUND(10),
    NORMAL(20),
    IMPORTANT(30),
    DRIVING_CRITICAL(40),
    SAFETY_CRITICAL(50)
}

enum class TaskState {
    QUEUED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED,
    DEGRADED,
}

val TaskState.isTerminal: Boolean
    get() = this in setOf(
        TaskState.COMPLETED,
        TaskState.CANCELLED,
        TaskState.FAILED,
        TaskState.DEGRADED,
    )

data class LanaTask(
    val id: String,
    val kind: String,
    val priority: TaskPriority,
    val interruptible: Boolean = true,
    val state: TaskState = TaskState.QUEUED
)
