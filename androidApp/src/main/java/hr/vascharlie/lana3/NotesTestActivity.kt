package hr.vascharlie.lana3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import hr.vascharlie.lana3.application.SaveNoteFlow
import hr.vascharlie.lana3.application.SaveNoteFlowResult
import hr.vascharlie.lana3.core.authorization.AutomationMode
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.model.LanguageContext
import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteInvalidReason
import hr.vascharlie.lana3.core.notes.NoteResult
import hr.vascharlie.lana3.core.notes.NoteService
import java.util.Locale
import java.util.UUID

class NotesTestActivity : Activity() {
    companion object {
        const val EXTRA_PREFILL_NOTE_TEXT = "prefill_note_text"
    }

    private lateinit var repository: AndroidNoteRepository
    private lateinit var service: NoteService
    private lateinit var saveFlow: SaveNoteFlow

    private lateinit var noteEditor: EditText
    private lateinit var searchEditor: EditText
    private lateinit var saveButton: Button
    private lateinit var status: TextView
    private lateinit var notesContainer: LinearLayout

    private var editingNoteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = AndroidNoteRepository(this)
        service = NoteService(
            repository = repository,
            nowEpochMillis = { System.currentTimeMillis() },
            newId = { UUID.randomUUID().toString() },
        )
        saveFlow = SaveNoteFlow(
            noteService = service,
            diagnosticSink = AndroidLogDiagnosticSink(),
            taskIdFactory = { UUID.randomUUID().toString() },
        )

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 44, 36, 44)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = getString(R.string.notes_lab_title)
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.notes_lab_description)
            textSize = 14f
            setPadding(0, 10, 0, 18)
            setTextColor(Color.rgb(180, 195, 210))
        })

        noteEditor = EditText(this).apply {
            hint = getString(R.string.notes_editor_hint)
            minLines = 3
            gravity = Gravity.TOP
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 135, 150))
        }
        root.addView(
            noteEditor,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        )

        saveButton = Button(this).apply {
            text = getString(R.string.notes_save_new)
            isAllCaps = false
            setOnClickListener { saveOrUpdate() }
        }
        root.addView(saveButton)

        root.addView(Button(this).apply {
            text = getString(R.string.notes_cancel_edit)
            isAllCaps = false
            setOnClickListener {
                clearEditor()
                status.text = getString(R.string.notes_edit_cancelled)
            }
        })

        status = TextView(this).apply {
            textSize = 15f
            setPadding(0, 12, 0, 18)
            setTextColor(Color.rgb(90, 180, 255))
        }
        root.addView(status)

        root.addView(TextView(this).apply {
            text = getString(R.string.notes_search_title)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        searchEditor = EditText(this).apply {
            hint = getString(R.string.notes_search_hint)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 135, 150))
        }
        root.addView(searchEditor)

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        searchRow.addView(
            Button(this).apply {
                text = getString(R.string.notes_search_button)
                isAllCaps = false
                setOnClickListener { runSearch() }
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
        )
        searchRow.addView(
            Button(this).apply {
                text = getString(R.string.notes_show_all)
                isAllCaps = false
                setOnClickListener {
                    searchEditor.setText("")
                    refreshNotes()
                }
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
        )
        root.addView(searchRow)

        root.addView(TextView(this).apply {
            text = getString(R.string.notes_saved_title)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 18, 0, 8)
            setTextColor(Color.WHITE)
        })

        notesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(notesContainer)

        root.addView(Button(this).apply {
            text = getString(R.string.back)
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(scroll)
        refreshNotes()

        intent.getStringExtra(EXTRA_PREFILL_NOTE_TEXT)
            ?.takeIf { it.isNotBlank() }
            ?.let { prefill ->
                noteEditor.setText(prefill)
                noteEditor.setSelection(noteEditor.text.length)
                status.text = getString(R.string.notes_voice_prefill_ready)
            }
    }

    override fun onDestroy() {
        repository.close()
        super.onDestroy()
    }

    private fun saveOrUpdate() {
        val text = noteEditor.text?.toString().orEmpty()
        val editingId = editingNoteId

        if (editingId != null) {
            handleUpdate(service.update(editingId, text))
            return
        }

        val context = LanaContext(
            language = LanguageContext(Locale.getDefault().toLanguageTag()),
        )
        when (
            val result = saveFlow.run(
                text = text,
                context = context,
                automationMode = AutomationMode.EXECUTE,
            )
        ) {
            is SaveNoteFlowResult.Completed -> {
                status.text = getString(
                    R.string.notes_saved_flow,
                    result.task.state.name,
                )
                clearEditor()
                refreshNotes()
            }

            is SaveNoteFlowResult.Rejected -> {
                status.text = formatNoteInvalidReason(result.reason)
            }

            is SaveNoteFlowResult.Blocked -> {
                status.text = getString(
                    R.string.notes_save_blocked,
                    result.reason,
                )
            }

            is SaveNoteFlowResult.AwaitingConfirmation -> {
                status.text = getString(
                    R.string.notes_awaiting_confirmation,
                    result.reason,
                )
            }

            is SaveNoteFlowResult.Failed -> {
                status.text = getString(
                    R.string.notes_save_failed,
                    result.task.state.name,
                    result.reason,
                )
            }

            is SaveNoteFlowResult.Degraded -> {
                status.text = getString(
                    R.string.notes_save_degraded,
                    result.reason,
                    if (result.note != null) {
                        getString(R.string.notes_save_degraded_persisted_suffix)
                    } else {
                        ""
                    },
                )
                refreshNotes()
            }
        }
    }

    private fun handleUpdate(result: NoteResult) {
        when (result) {
            is NoteResult.Updated -> {
                status.text = getString(R.string.notes_updated)
                clearEditor()
                refreshNotes()
            }

            is NoteResult.Invalid -> {
                status.text = result.reason
            }

            is NoteResult.NotFound -> {
                status.text = getString(R.string.notes_not_found)
                clearEditor()
                refreshNotes()
            }

            else -> {
                status.text = getString(R.string.notes_update_unexpected)
            }
        }
    }

    private fun runSearch() {
        val query = searchEditor.text?.toString().orEmpty()
        when (val result = service.search(query)) {
            is NoteResult.Found -> {
                renderNotes(result.notes)
                status.text = getString(
                    R.string.notes_found_count,
                    result.notes.size,
                )
            }

            is NoteResult.Invalid -> {
                status.text = result.reason
            }

            else -> {
                status.text = getString(R.string.notes_search_failed)
            }
        }
    }

    private fun refreshNotes() {
        when (val result = service.listAll()) {
            is NoteResult.Found -> {
                renderNotes(result.notes)
                status.text = getString(
                    R.string.notes_local_count,
                    result.notes.size,
                )
            }

            else -> {
                status.text = getString(R.string.notes_load_failed)
            }
        }
    }

    private fun renderNotes(notes: List<LanaNote>) {
        notesContainer.removeAllViews()

        if (notes.isEmpty()) {
            notesContainer.addView(TextView(this).apply {
                text = getString(R.string.notes_empty)
                textSize = 14f
                setTextColor(Color.rgb(150, 165, 180))
            })
            return
        }

        notes.forEach { note ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 14, 18, 14)
                setBackgroundColor(Color.rgb(15, 39, 67))
            }

            card.addView(TextView(this).apply {
                text = note.text
                textSize = 16f
                setTextColor(Color.WHITE)
            })

            card.addView(TextView(this).apply {
                text = getString(R.string.notes_id, note.id.take(8))
                textSize = 11f
                setTextColor(Color.rgb(130, 150, 170))
            })

            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            actions.addView(
                Button(this).apply {
                    text = getString(R.string.notes_edit)
                    isAllCaps = false
                    setOnClickListener {
                        editingNoteId = note.id
                        noteEditor.setText(note.text)
                        noteEditor.setSelection(noteEditor.text.length)
                        saveButton.text = getString(R.string.notes_save_changes)
                        status.text = getString(
                            R.string.notes_editing,
                            note.id.take(8),
                        )
                    }
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                )
            )
            actions.addView(
                Button(this).apply {
                    text = getString(R.string.notes_delete)
                    isAllCaps = false
                    setOnClickListener { deleteNote(note.id) }
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                )
            )
            card.addView(actions)

            notesContainer.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
            )
        }
    }

    private fun deleteNote(id: String) {
        when (service.delete(id)) {
            is NoteResult.Deleted -> {
                if (editingNoteId == id) {
                    clearEditor()
                }
                status.text = getString(R.string.notes_deleted)
                refreshNotes()
            }

            is NoteResult.NotFound -> {
                status.text = getString(R.string.notes_not_found)
                refreshNotes()
            }

            else -> {
                status.text = getString(R.string.notes_delete_failed)
            }
        }
    }

    private fun formatNoteInvalidReason(
        reason: NoteInvalidReason,
    ): String = when (reason) {
        NoteInvalidReason.NOTE_TEXT_BLANK ->
            getString(R.string.notes_invalid_text_blank)

        NoteInvalidReason.NOTE_ID_BLANK ->
            getString(R.string.notes_invalid_id_blank)

        NoteInvalidReason.SEARCH_QUERY_BLANK ->
            getString(R.string.notes_invalid_search_blank)

        NoteInvalidReason.ACTION_NOT_AUTHORIZED ->
            getString(R.string.notes_invalid_action_not_authorized)

        NoteInvalidReason.ACTION_NOT_SAVE_NOTE ->
            getString(R.string.notes_invalid_action_type)
    }

    private fun clearEditor() {
        editingNoteId = null
        noteEditor.setText("")
        saveButton.text = getString(R.string.notes_save_new)
    }
}
