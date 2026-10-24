package hr.vascharlie.lana3

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.util.UUID

/** Developer preview: offline manual task entry, no scheduled notifications yet. */
class TasksActivity : Activity() {
    private lateinit var store: TaskStore
    private lateinit var list: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var projectInput: Spinner
    private lateinit var statusInput: Spinner
    private lateinit var filter: Spinner
    private val projects = arrayOf("Općenito", "Taksi i klijenti", "BUSINESS OS", "MASTER Excel", "Marketing i web", "LANA 3", "Platforma za vozače", "Online zarada", "Strip i brend", "Administracija i financije")
    private val states = arrayOf("Otvoreno", "Čeka se", "Dovršeno")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = TaskStore()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 24)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        root.addView(TextView(this).apply { text = "LANA 3 · Zadaci"; textSize = 26f; setTextColor(Color.WHITE) })
        root.addView(TextView(this).apply { text = "Ručni unos · lokalno na uređaju · bez obavijesti"; setTextColor(Color.LTGRAY) })
        titleInput = EditText(this).apply { hint = "Naziv zadatka"; setSingleLine(true); setTextColor(Color.WHITE); setHintTextColor(Color.GRAY) }
        root.addView(titleInput)
        projectInput = Spinner(this).apply { adapter = adapter(projects) }
        root.addView(projectInput)
        statusInput = Spinner(this).apply { adapter = adapter(states) }
        root.addView(statusInput)
        root.addView(Button(this).apply {
            text = "Dodaj zadatak"
            setOnClickListener {
                val title = titleInput.text.toString().trim()
                if (title.isEmpty()) { titleInput.error = "Unesi naziv"; return@setOnClickListener }
                store.add(title, projects[projectInput.selectedItemPosition], states[statusInput.selectedItemPosition])
                titleInput.setText("")
                render()
            }
        })
        root.addView(TextView(this).apply { text = "Pregled zadataka"; textSize = 19f; setTextColor(Color.WHITE) })
        filter = Spinner(this).apply {
            adapter = adapter(arrayOf("Svi projekti") + projects)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { if (::list.isInitialized) render() }
            }
        }
        root.addView(filter)
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "Natrag"; setOnClickListener { finish() } })
        setContentView(root)
        render()
    }

    private fun adapter(values: Array<String>) = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)

    private fun render() {
        list.removeAllViews()
        val selected = filter.selectedItemPosition
        val entries = store.all(if (selected == 0) null else projects[selected - 1])
        if (entries.isEmpty()) list.addView(TextView(this).apply { text = "Nema zadataka u ovom prikazu."; setTextColor(Color.LTGRAY) })
        entries.forEach { task ->
            val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 14, 12, 14); setBackgroundColor(Color.rgb(19, 40, 65)) }
            card.addView(TextView(this).apply { text = task.title; textSize = 17f; setTextColor(Color.WHITE) })
            card.addView(TextView(this).apply { text = "${task.project} · ${task.state}"; setTextColor(Color.LTGRAY) })
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(Button(this).apply {
                text = if (task.state == "Dovršeno") "Ponovno otvori" else "Dovrši"
                setOnClickListener { store.setState(task.id, if (task.state == "Dovršeno") "Otvoreno" else "Dovršeno"); render() }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(Button(this).apply {
                text = "Uredi"
                setOnClickListener {
                    val edit = EditText(this@TasksActivity).apply { setText(task.title) }
                    AlertDialog.Builder(this@TasksActivity).setTitle("Uredi naziv").setView(edit)
                        .setPositiveButton("Spremi") { _, _ -> val v = edit.text.toString().trim(); if (v.isNotEmpty()) { store.rename(task.id, v); render() } }
                        .setNegativeButton("Odustani", null).show()
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(Button(this).apply {
                text = "Izbriši"
                setOnClickListener {
                    AlertDialog.Builder(this@TasksActivity).setTitle("Izbrisati zadatak?")
                        .setMessage(task.title).setPositiveButton("Izbriši") { _, _ -> store.delete(task.id); render() }
                        .setNegativeButton("Odustani", null).show()
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(row)
            list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 })
        }
    }

    override fun onDestroy() { store.close(); super.onDestroy() }

    private data class Task(val id: String, val title: String, val project: String, val state: String)

    private inner class TaskStore : SQLiteOpenHelper(this@TasksActivity, "lana_manual_tasks.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE tasks (id TEXT PRIMARY KEY, title TEXT NOT NULL, project TEXT NOT NULL, state TEXT NOT NULL)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            throw IllegalStateException("Explicit task database migration required")
        }
        fun add(title: String, project: String, state: String) {
            writableDatabase.insertOrThrow("tasks", null, ContentValues().apply {
                put("id", UUID.randomUUID().toString()); put("title", title); put("project", project); put("state", state)
            })
        }
        fun all(project: String?): List<Task> {
            val result = mutableListOf<Task>()
            val c = readableDatabase.query("tasks", arrayOf("id", "title", "project", "state"),
                if (project == null) null else "project = ?", if (project == null) null else arrayOf(project),
                null, null, "rowid DESC")
            c.use { while (it.moveToNext()) result.add(Task(it.getString(0), it.getString(1), it.getString(2), it.getString(3))) }
            return result
        }
        fun setState(id: String, state: String) { writableDatabase.update("tasks", ContentValues().apply { put("state", state) }, "id = ?", arrayOf(id)) }
        fun rename(id: String, title: String) { writableDatabase.update("tasks", ContentValues().apply { put("title", title) }, "id = ?", arrayOf(id)) }
        fun delete(id: String) { writableDatabase.delete("tasks", "id = ?", arrayOf(id)) }
    }
}
