package hr.vascharlie.lana3.core.search

import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteSearchProviderTest {
    private class FakeNoteRepository(
        private val notes: List<LanaNote>,
    ) : NoteRepository {
        override fun insert(note: LanaNote) = error("not used")
        override fun update(note: LanaNote) = error("not used")
        override fun delete(id: String) = error("not used")
        override fun findById(id: String) = notes.firstOrNull { it.id == id }
        override fun listAll(): List<LanaNote> = notes
    }

    @Test
    fun exposesMatchingNotesAsUniversalSearchHits() {
        val provider = NoteSearchProvider(
            FakeNoteRepository(
                listOf(
                    LanaNote("n1", "Aerodrom Zadar\nPokupiti Roberta", 10, 30),
                    LanaNote("n2", "Kupiti gorivo", 20, 40),
                )
            )
        )

        val hits = provider.search("AERODROM")

        assertEquals(1, hits.size)
        assertEquals(SearchDomain.NOTES, hits.single().domain)
        assertEquals("n1", hits.single().id)
        assertEquals("Aerodrom Zadar", hits.single().title)
        assertEquals("Aerodrom Zadar\nPokupiti Roberta", hits.single().preview)
        assertEquals(30, hits.single().updatedAtEpochMillis)
    }

    @Test
    fun adapterDoesNotReturnUnrelatedNotes() {
        val provider = NoteSearchProvider(
            FakeNoteRepository(
                listOf(LanaNote("n1", "Kupiti gorivo", 10, 10))
            )
        )

        assertEquals(emptyList(), provider.search("aerodrom"))
    }

    @Test
    fun blankQueryDoesNotExposeAllNotes() {
        val provider = NoteSearchProvider(
            FakeNoteRepository(
                listOf(LanaNote("n1", "Privatna bilješka", 10, 10))
            )
        )

        assertEquals(emptyList(), provider.search("   "))
    }

    @Test
    fun universalSearchCanAggregateRealNotesProviderBoundary() {
        val notes = NoteSearchProvider(
            FakeNoteRepository(
                listOf(LanaNote("n1", "Klijent Robert", 10, 50))
            )
        )

        val hits = (UniversalSearch(listOf(notes)).search("robert") as UniversalSearchResult.Found).hits

        assertEquals(listOf("n1"), hits.map { it.id })
        assertEquals(listOf(SearchDomain.NOTES), hits.map { it.domain })
    }
}
