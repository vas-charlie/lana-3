package hr.vascharlie.lana3.core.reminders

/** Device-independent reminder. Dates are UTC epoch milliseconds; presentation uses device time zone. */
data class LanaReminder(
    val id: String,
    val title: String,
    val category: String,
    val dueAtEpochMillis: Long,
    val notifyAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val completed: Boolean = false,
)

interface ReminderRepository {
    fun insert(reminder: LanaReminder)
    fun findById(id: String): LanaReminder?
    fun listAll(): List<LanaReminder>
    fun update(reminder: LanaReminder): Boolean
    fun delete(id: String): Boolean
}

enum class ReminderProblem {
    BLANK_TITLE,
    INVALID_DUE_DATE,
    NOTIFICATION_AFTER_DEADLINE,
    INVALID_CATEGORY,
}

sealed interface ReminderResult {
    data class Saved(val reminder: LanaReminder) : ReminderResult
    data class Updated(val reminder: LanaReminder) : ReminderResult
    data class Deleted(val id: String) : ReminderResult
    data class Invalid(val reason: ReminderProblem) : ReminderResult
    data class NotFound(val id: String) : ReminderResult
}

/** No cloud/AI call. Android and other platforms provide storage and notification adapters. */
class ReminderService(
    private val repository: ReminderRepository,
    private val clock: () -> Long,
    private val newId: () -> String,
) {
    fun add(
        title: String,
        category: String,
        dueAtEpochMillis: Long,
        notifyAtEpochMillis: Long?,
    ): ReminderResult {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return ReminderResult.Invalid(ReminderProblem.BLANK_TITLE)
        if (category.isBlank()) return ReminderResult.Invalid(ReminderProblem.INVALID_CATEGORY)
        if (dueAtEpochMillis <= 0L) return ReminderResult.Invalid(ReminderProblem.INVALID_DUE_DATE)
        if (notifyAtEpochMillis != null && (notifyAtEpochMillis <= 0L ||
                    notifyAtEpochMillis > dueAtEpochMillis)) {
            return ReminderResult.Invalid(ReminderProblem.NOTIFICATION_AFTER_DEADLINE)
        }
        val reminder = LanaReminder(
            id = newId(),
            title = trimmed,
            category = category.trim(),
            dueAtEpochMillis = dueAtEpochMillis,
            notifyAtEpochMillis = notifyAtEpochMillis,
            createdAtEpochMillis = clock(),
        )
        repository.insert(reminder)
        return ReminderResult.Saved(reminder)
    }

    /** Includes overdue tasks until completed. endExclusive is in the user's local time zone. */
    fun attentionBefore(endExclusive: Long): List<LanaReminder> =
        repository.listAll()
            .filter { !it.completed && it.dueAtEpochMillis < endExclusive }
            .sortedWith(compareBy<LanaReminder> { it.dueAtEpochMillis }.thenBy { it.id })

    fun openAll(): List<LanaReminder> = repository.listAll()
        .filterNot { it.completed }
        .sortedWith(compareBy<LanaReminder> { it.dueAtEpochMillis }.thenBy { it.id })

    fun doneAll(): List<LanaReminder> = repository.listAll()
        .filter { it.completed }
        .sortedWith(compareByDescending<LanaReminder> { it.dueAtEpochMillis }.thenBy { it.id })

    fun complete(id: String): ReminderResult {
        val current = repository.findById(id) ?: return ReminderResult.NotFound(id)
        if (current.completed) return ReminderResult.Updated(current)
        val done = current.copy(completed = true)
        return if (repository.update(done)) ReminderResult.Updated(done)
        else ReminderResult.NotFound(id)
    }

    fun delete(id: String): ReminderResult = if (repository.delete(id)) {
        ReminderResult.Deleted(id)
    } else {
        ReminderResult.NotFound(id)
    }
}
