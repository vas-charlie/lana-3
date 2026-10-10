package hr.vascharlie.lana3.core.search

import hr.vascharlie.lana3.core.notes.NoteRepository

class NoteSearchProvider(
    private val repository: NoteRepository,
) : SearchProvider {
    override val domain: SearchDomain = SearchDomain.NOTES

    override fun search(query: String): List<SearchHit> {
        if (query.isBlank()) return emptyList()

        return repository.listAll()
            .asSequence()
            .filter { note -> note.text.contains(query, ignoreCase = true) }
            .map { note ->
                SearchHit(
                    domain = domain,
                    id = note.id,
                    title = note.text.lineSequence().firstOrNull().orEmpty(),
                    preview = note.text,
                    updatedAtEpochMillis = note.updatedAtEpochMillis,
                )
            }
            .toList()
    }
}
