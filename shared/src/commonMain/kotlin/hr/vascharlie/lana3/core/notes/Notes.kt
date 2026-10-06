package hr.vascharlie.lana3.core.notes

data class LanaNote(
    val id: String,
    val text: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

interface NoteRepository {
    fun insert(note: LanaNote): LanaNote

    fun update(note: LanaNote): LanaNote?

    fun delete(id: String): Boolean

    fun findById(id: String): LanaNote?

    fun listAll(): List<LanaNote>
}

sealed interface NoteResult {
    data class Saved(val note: LanaNote) : NoteResult

    data class Updated(val note: LanaNote) : NoteResult

    data class Deleted(val id: String) : NoteResult

    data class Found(val notes: List<LanaNote>) : NoteResult

    data class NotFound(val id: String) : NoteResult

    data class Invalid(val reason: String) : NoteResult
}

/**
 * Device-independent note use cases.
 *
 * The repository decides how notes are persisted. The service validates input and keeps
 * timestamps/IDs outside platform-specific storage code.
 */
class NoteService(
    private val repository: NoteRepository,
    private val nowEpochMillis: () -> Long,
    private val newId: () -> String,
) {
    fun save(text: String): NoteResult {
        if (text.isBlank()) {
            return NoteResult.Invalid("Note text must not be blank.")
        }

        val now = nowEpochMillis()
        val note = LanaNote(
            id = newId(),
            text = text,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )

        return NoteResult.Saved(repository.insert(note))
    }

    fun update(id: String, text: String): NoteResult {
        if (id.isBlank()) {
            return NoteResult.Invalid("Note id must not be blank.")
        }
        if (text.isBlank()) {
            return NoteResult.Invalid("Note text must not be blank.")
        }

        val existing = repository.findById(id)
            ?: return NoteResult.NotFound(id)

        val updated = existing.copy(
            text = text,
            updatedAtEpochMillis = nowEpochMillis(),
        )

        return repository.update(updated)
            ?.let(NoteResult::Updated)
            ?: NoteResult.NotFound(id)
    }

    fun delete(id: String): NoteResult {
        if (id.isBlank()) {
            return NoteResult.Invalid("Note id must not be blank.")
        }

        return if (repository.delete(id)) {
            NoteResult.Deleted(id)
        } else {
            NoteResult.NotFound(id)
        }
    }

    fun listAll(): NoteResult =
        NoteResult.Found(
            repository.listAll()
                .sortedWith(
                    compareByDescending<LanaNote> { it.updatedAtEpochMillis }
                        .thenByDescending { it.createdAtEpochMillis }
                        .thenBy { it.id }
                )
        )

    fun search(query: String): NoteResult {
        if (query.isBlank()) {
            return NoteResult.Invalid("Search query must not be blank.")
        }

        val matches = repository.listAll()
            .filter { it.text.contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<LanaNote> { it.updatedAtEpochMillis }
                    .thenByDescending { it.createdAtEpochMillis }
                    .thenBy { it.id }
            )

        return NoteResult.Found(matches)
    }
}
