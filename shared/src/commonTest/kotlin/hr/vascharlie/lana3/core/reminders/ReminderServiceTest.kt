package hr.vascharlie.lana3.core.reminders

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ReminderServiceTest {
    private class MemoryRepo : ReminderRepository {
        val records = linkedMapOf<String, LanaReminder>()
        override fun insert(reminder: LanaReminder) { records[reminder.id] = reminder }
        override fun findById(id: String) = records[id]
        override fun listAll() = records.values.toList()
        override fun update(reminder: LanaReminder): Boolean =
            if (records.containsKey(reminder.id)) {
                records[reminder.id] = reminder
                true
            } else false
        override fun delete(id: String) = records.remove(id) != null
    }

    @Test fun invalidReminderNeverPersists() {
        val repo = MemoryRepo()
        val service = ReminderService(repo, { 1_000L }, { "id" })
        assertEquals(ReminderProblem.BLANK_TITLE,
            assertIs<ReminderResult.Invalid>(service.add(" ", "Auto", 5_000L, null)).reason)
        assertEquals(ReminderProblem.NOTIFICATION_AFTER_DEADLINE,
            assertIs<ReminderResult.Invalid>(service.add("Gume", "Auto", 5_000L, 6_000L)).reason)
        assertTrue(repo.records.isEmpty())
    }

    @Test fun upcomingIncludesOverdueAndExcludesCompleted() {
        val repo = MemoryRepo()
        var next = 0
        val service = ReminderService(repo, { 1_000L }, { (++next).toString() })
        service.add("Stari rok", "Auto", 2_000L, null)
        service.add("Ovaj tjedan", "Banka", 6_000L, 3_000L)
        service.add("Kasnije", "Projekt", 9_000L, null)
        assertEquals(listOf("Stari rok", "Ovaj tjedan"),
            service.attentionBefore(7_000L).map { it.title })
        val done = assertIs<ReminderResult.Updated>(service.complete("1"))
        assertTrue(done.reminder.completed)
        assertEquals(listOf("Ovaj tjedan"), service.attentionBefore(7_000L).map { it.title })
        assertEquals(listOf("Stari rok"), service.doneAll().map { it.title })
    }

    @Test fun persistedStateIsReadThroughRepositoryAfterServiceRecreation() {
        val repo = MemoryRepo()
        val first = ReminderService(repo, { 1_000L }, { "stable-id" })
        assertIs<ReminderResult.Saved>(first.add("Registracija", "Auto", 5_000L, 2_000L))
        val second = ReminderService(repo, { 2_000L }, { "unused" })
        assertEquals("stable-id", second.openAll().single().id)
        assertFalse(second.openAll().single().completed)
        assertIs<ReminderResult.Updated>(second.complete("stable-id"))
        assertTrue(first.doneAll().single().completed)
        assertIs<ReminderResult.Deleted>(second.delete("stable-id"))
        assertTrue(first.openAll().isEmpty())
    }
}
