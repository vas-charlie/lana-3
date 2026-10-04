package hr.vascharlie.lana3.core.task

sealed interface TaskDecision {
    data class Start(val task: LanaTask) : TaskDecision
    data class Preempt(val active: LanaTask, val incoming: LanaTask) : TaskDecision
    data class Queue(val task: LanaTask, val reason: String) : TaskDecision
    data class Reject(val task: LanaTask, val reason: String) : TaskDecision
}

class TaskArbiter {
    fun decide(active: LanaTask?, incoming: LanaTask): TaskDecision {
        if (incoming.state == TaskState.CANCELLED || incoming.state == TaskState.COMPLETED) {
            return TaskDecision.Reject(incoming, "Incoming task is not runnable")
        }
        if (active == null) return TaskDecision.Start(incoming)
        if (active.state != TaskState.ACTIVE) return TaskDecision.Start(incoming)

        val outranks = incoming.priority.rank > active.priority.rank
        return when {
            outranks && active.interruptible -> TaskDecision.Preempt(active, incoming)
            outranks -> TaskDecision.Queue(incoming, "Higher priority task cannot interrupt active task safely")
            else -> TaskDecision.Queue(incoming, "Active task has equal or higher priority")
        }
    }
}
