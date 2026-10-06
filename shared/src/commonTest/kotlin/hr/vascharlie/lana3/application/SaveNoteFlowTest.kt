package hr.vascharlie.lana3.application

import hr.vascharlie.lana3.core.authorization.AutomationMode
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.diagnostics.DiagnosticEvent
import hr.vascharlie.lana3.core.diagnostics.DiagnosticSink
import hr.vascharlie.lana3.core.model.LanguageContext
import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteRepository
import hr.vascharlie.lana3.core.notes.NoteService
import hr.vascharlie.lana3.core.task.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SaveNoteFlowTest {
    private class FakeRepository(
        private val failInsert: Boolean = false,
    ) : NoteRepository {
        val notes = linkedMapOf<String, LanaNote>()

        override fun insert(note: LanaNote): LanaNote {
            if (failInsert) error("storage failure")
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

    private class CollectingSink : DiagnosticSink {
        val events = mutableListOf<DiagnosticEvent>()

        override fun record(event: DiagnosticEvent) {
            events += event
        }
    }

    private val context = LanaContext(
        language = LanguageContext("hr-HR"),
    )

    private fun createFlow(
        repository: FakeRepository = FakeRepository(),
        sink: CollectingSink = CollectingSink(),
    ): Triple<SaveNoteFlow, FakeRepository, CollectingSink> {
        val service = NoteService(
            repository = repository,
            nowEpochMillis = { 1_000L },
            newId = { "note-1" },
        )

        return Triple(
            SaveNoteFlow(
                noteService = service,
                diagnosticSink = sink,
                taskIdFactory = { "task-1" },
            ),
            repository,
            sink,
        )
    }

    @Test
    fun executeModeCompletesWholeBrainToHandsFlow() {
        val (flow, repository, sink) = createFlow()

        val result = flow.run(
            text = "Nazvati Roberta sutra.",
            context = context,
            automationMode = AutomationMode.EXECUTE,
        )

        val completed = assertIs<SaveNoteFlowResult.Completed>(result)
        assertEquals(TaskState.COMPLETED, completed.task.state)
        assertEquals("note-1", completed.note.id)
        assertEquals("Nazvati Roberta sutra.", completed.note.text)
        assertEquals(1, repository.notes.size)
        assertTrue(sink.events.any { it.code == "save_note_execution_started" })
        assertTrue(sink.events.any { it.code == "save_note_completed" })
    }

    @Test
    fun observeModeNeverWritesNote() {
        val (flow, repository, sink) = createFlow()

        val result = flow.run(
            text = "Ne smije se spremiti.",
            context = context,
            automationMode = AutomationMode.OBSERVE,
        )

        assertIs<SaveNoteFlowResult.Blocked>(result)
        assertTrue(repository.notes.isEmpty())
        assertTrue(sink.events.any { it.code == "save_note_mode_blocked" })
    }

    @Test
    fun confirmModeWaitsUntilUserActuallyConfirms() {
        val (flow, repository, _) = createFlow()

        val waiting = flow.run(
            text = "Spremi nakon potvrde.",
            context = context,
            automationMode = AutomationMode.CONFIRM,
            userConfirmed = false,
        )

        assertIs<SaveNoteFlowResult.AwaitingConfirmation>(waiting)
        assertTrue(repository.notes.isEmpty())

        val completed = flow.run(
            text = "Spremi nakon potvrde.",
            context = context,
            automationMode = AutomationMode.CONFIRM,
            userConfirmed = true,
        )

        assertIs<SaveNoteFlowResult.Completed>(completed)
        assertEquals(1, repository.notes.size)
    }

    @Test
    fun blankNoteIsRejectedBeforeExecutionTaskStarts() {
        val (flow, repository, sink) = createFlow()

        val result = flow.run(
            text = "   ",
            context = context,
            automationMode = AutomationMode.EXECUTE,
        )

        assertIs<SaveNoteFlowResult.Rejected>(result)
        assertTrue(repository.notes.isEmpty())
        assertFalse(sink.events.any { it.code == "save_note_execution_started" })
    }

    @Test
    fun storageFailureBecomesFailedTaskInsteadOfCrash() {
        val repository = FakeRepository(failInsert = true)
        val (flow, _, sink) = createFlow(repository = repository)

        val result = flow.run(
            text = "Ovo će imati kvar spremanja.",
            context = context,
            automationMode = AutomationMode.EXECUTE,
        )

        val failed = assertIs<SaveNoteFlowResult.Failed>(result)
        assertEquals(TaskState.FAILED, failed.task.state)
        assertEquals("Local note storage failed.", failed.reason)
        assertTrue(sink.events.any { it.code == "save_note_failed" })
    }

    @Test
    fun diagnosticsNeverContainNoteText() {
        val secretText = "privatna bilješka koju ne smijemo logirati"
        val (flow, _, sink) = createFlow()

        assertIs<SaveNoteFlowResult.Completed>(
            flow.run(
                text = secretText,
                context = context,
                automationMode = AutomationMode.EXECUTE,
            )
        )

        sink.events.forEach { event ->
            assertFalse(event.message.contains(secretText))
            assertFalse(event.attributes.values.any { it.contains(secretText) })
        }
    }
}
