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
import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteResult
import hr.vascharlie.lana3.core.notes.NoteService
import java.util.UUID

class NotesTestActivity : Activity() {
    private lateinit var repository: AndroidNoteRepository
    private lateinit var service: NoteService

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

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 44, 36, 44)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = "LANA Notes Lab"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text =
                "Lokalne bilješke bez clouda. Ovaj ekran testira spremanje, " +
                    "uređivanje, brisanje i pretragu kroz zajedničku LANA jezgru."
            textSize = 14f
            setPadding(0, 10, 0, 18)
            setTextColor(Color.rgb(180, 195, 210))
        })

        noteEditor = EditText(this).apply {
            hint = "Upiši bilješku..."
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
            text = "Spremi novu bilješku"
            isAllCaps = false
            setOnClickListener { saveOrUpdate() }
        }
        root.addView(saveButton)

        root.addView(Button(this).apply {
            text = "Odustani od uređivanja"
            isAllCaps = false
            setOnClickListener {
                clearEditor()
                status.text = "Uređivanje poništeno."
            }
        })

        status = TextView(this).apply {
            textSize = 15f
            setPadding(0, 12, 0, 18)
            setTextColor(Color.rgb(90, 180, 255))
        }
        root.addView(status)

        root.addView(TextView(this).apply {
            text = "Pretraga"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        searchEditor = EditText(this).apply {
            hint = "Traži po tekstu..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 135, 150))
        }
        root.addView(searchEditor)

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        searchRow.addView(
            Button(this).apply {
                text = "Traži"
                isAllCaps = false
                setOnClickListener { runSearch() }
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
        )
        searchRow.addView(
            Button(this).apply {
                text = "Sve"
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
            text = "Spremljene bilješke"
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
            text = "Natrag"
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(scroll)
        refreshNotes()
    }

    override fun onDestroy() {
        repository.close()
        super.onDestroy()
    }

    private fun saveOrUpdate() {
        val text = noteEditor.text?.toString().orEmpty()

        val result = editingNoteId
            ?.let { service.update(it, text) }
            ?: service.save(text)

        when (result) {
            is NoteResult.Saved -> {
                status.text = "Bilješka spremljena lokalno."
                clearEditor()
                refreshNotes()
            }

            is NoteResult.Updated -> {
                status.text = "Bilješka ažurirana."
                clearEditor()
                refreshNotes()
            }

            is NoteResult.Invalid -> {
                status.text = result.reason
            }

            is NoteResult.NotFound -> {
                status.text = "Bilješka više ne postoji."
                clearEditor()
                refreshNotes()
            }

            is NoteResult.Deleted,
            is NoteResult.Found -> {
                status.text = "Neočekivan rezultat operacije."
            }
        }
    }

    private fun runSearch() {
        val query = searchEditor.text?.toString().orEmpty()
        when (val result = service.search(query)) {
            is NoteResult.Found -> {
                renderNotes(result.notes)
                status.text = "Pronađeno: " + result.notes.size
            }

            is NoteResult.Invalid -> {
                status.text = result.reason
            }

            else -> {
                status.text = "Pretraga nije uspjela."
            }
        }
    }

    private fun refreshNotes() {
        when (val result = service.listAll()) {
            is NoteResult.Found -> {
                renderNotes(result.notes)
                status.text = "Lokalno spremljeno: " + result.notes.size
            }

            else -> {
                status.text = "Bilješke se ne mogu učitati."
            }
        }
    }

    private fun renderNotes(notes: List<LanaNote>) {
        notesContainer.removeAllViews()

        if (notes.isEmpty()) {
            notesContainer.addView(TextView(this).apply {
                text = "Nema bilješki."
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
                text = "ID: " + note.id.take(8)
                textSize = 11f
                setTextColor(Color.rgb(130, 150, 170))
            })

            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            actions.addView(
                Button(this).apply {
                    text = "Uredi"
                    isAllCaps = false
                    setOnClickListener {
                        editingNoteId = note.id
                        noteEditor.setText(note.text)
                        noteEditor.setSelection(noteEditor.text.length)
                        saveButton.text = "Spremi izmjene"
                        status.text = "Uređuješ bilješku " + note.id.take(8)
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
                    text = "Obriši"
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
                status.text = "Bilješka obrisana."
                refreshNotes()
            }

            is NoteResult.NotFound -> {
                status.text = "Bilješka više ne postoji."
                refreshNotes()
            }

            else -> {
                status.text = "Bilješku nije moguće obrisati."
            }
        }
    }

    private fun clearEditor() {
        editingNoteId = null
        noteEditor.setText("")
        saveButton.text = "Spremi novu bilješku"
    }
}
