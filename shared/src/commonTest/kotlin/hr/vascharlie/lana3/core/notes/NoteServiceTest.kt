package hr.vascharlie.lana3.core.notes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NoteServiceTest {
    private class FakeRepository : NoteRepository {
        private val notes = linkedMapOf<String, LanaNote>()

        override fun insert(note: LanaNote): LanaNote {
            notes[note.id] = note
            return note
        }

        override fun update(note: LanaNote): LanaNote? {
            if (note.id !in notes) return null
            notes[note.id] = note
            return note
        }

        override fun delete(id: String): Boolean =
            notes.remove(id) != null

        override fun findById(id: String): LanaNote? =
            notes[id]

        override fun listAll(): List<LanaNote> =
            notes.values.toList()
    }

    private fun service(
        repository: FakeRepository = FakeRepository(),
        times: MutableList<Long> = mutableListOf(100L, 200L, 300L, 400L),
        ids: MutableList<String> = mutableListOf("n1", "n2", "n3", "n4"),
    ): NoteService =
        NoteService(
            repository = repository,
            nowEpochMillis = { times.removeFirst() },
            newId = { ids.removeFirst() },
        )

    @Test
    fun savesUnicodeNoteWithoutChangingText() {
        val service = service()
        val text = "مرحبا Charlie 👋 — račun 27,36 €"

        val result = service.save(text)
        val saved = assertIs<NoteResult.Saved>(result)

        assertEquals("n1", saved.note.id)
        assertEquals(text, saved.note.text)
        assertEquals(100L, saved.note.createdAtEpochMillis)
        assertEquals(100L, saved.note.updatedAtEpochMillis)
    }

    @Test
    fun blankNoteIsRejected() {
        val result = service().save("   ")

        val invalid = assertIs<NoteResult.Invalid>(result)
        assertTrue(invalid.reason.contains("blank", ignoreCase = true))
    }

    @Test
    fun updatePreservesCreationTimeAndChangesUpdateTime() {
        val repository = FakeRepository()
        val service = service(repository)

        val saved = assertIs<NoteResult.Saved>(service.save("Prva verzija")).note
        val updated = assertIs<NoteResult.Updated>(
            service.update(saved.id, "Druga verzija")
        ).note

        assertEquals(saved.createdAtEpochMillis, updated.createdAtEpochMillis)
        assertEquals(200L, updated.updatedAtEpochMillis)
        assertEquals("Druga verzija", updated.text)
    }

    @Test
    fun missingNoteIsNotInventedDuringUpdate() {
        val result = service().update("missing", "tekst")

        assertIs<NoteResult.NotFound>(result)
    }

    @Test
    fun deleteReportsMissingNoteHonestly() {
        val service = service()

        assertIs<NoteResult.NotFound>(service.delete("missing"))
    }

    @Test
    fun listIsOrderedByMostRecentlyUpdated() {
        val repository = FakeRepository()
        val service = service(repository)

        val first = assertIs<NoteResult.Saved>(service.save("prva")).note
        val second = assertIs<NoteResult.Saved>(service.save("druga")).note
        assertIs<NoteResult.Updated>(service.update(first.id, "prva nova"))

        val list = assertIs<NoteResult.Found>(service.listAll()).notes

        assertEquals(listOf(first.id, second.id), list.map { it.id })
    }

    @Test
    fun searchIsCaseInsensitiveAndDoesNotGuessOnBlankQuery() {
        val service = service()
        service.save("Aerodrom Zadar u 18:15")
        service.save("Kupiti gorivo")

        val found = assertIs<NoteResult.Found>(service.search("AERODROM")).notes

        assertEquals(1, found.size)
        assertEquals("Aerodrom Zadar u 18:15", found.single().text)
        assertIs<NoteResult.Invalid>(service.search("   "))
    }
}
